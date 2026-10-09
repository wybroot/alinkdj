package com.honghe.party.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.entity.PartyMember;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.entity.SysNoticeLog;
import com.honghe.party.mapper.PartyMemberMapper;
import com.honghe.party.mapper.SysNoticeChannelMapper;
import com.honghe.party.mapper.SysNoticeLogMapper;
import com.honghe.party.notice.NoticeChannelFactory;
import com.honghe.party.notice.NoticeChannelHandler;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
import com.honghe.party.service.NoticeDispatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class NoticeDispatchServiceImpl implements NoticeDispatchService {

    @Autowired
    private SysNoticeLogMapper noticeLogMapper;

    @Autowired
    private SysNoticeChannelMapper noticeChannelMapper;

    @Autowired
    private PartyMemberMapper partyMemberMapper;

    @Autowired
    private NoticeChannelFactory channelFactory;

    @Override
    public SysNoticeLog sendNotice(String channelCode, String noticeType, String title, String content,
                                   String receiverType, Long receiverId, String receiverName, String receiverTarget,
                                   Long relatedMemberId, Integer relatedStepCode) {
        SysNoticeLog logRecord = new SysNoticeLog();
        String targetChannelCode = (channelCode != null && !channelCode.trim().isEmpty()) ? channelCode.trim().toUpperCase() : "IN_APP";
        logRecord.setChannelCode(targetChannelCode);
        logRecord.setNoticeType(noticeType != null ? noticeType : "REGULAR");
        logRecord.setTitle(title);
        logRecord.setContent(content);
        logRecord.setReceiverType(receiverType != null ? receiverType : "USER");
        logRecord.setReceiverId(receiverId);
        logRecord.setReceiverName(receiverName);
        logRecord.setReceiverTarget(receiverTarget);
        logRecord.setRelatedMemberId(relatedMemberId);
        logRecord.setRelatedStepCode(relatedStepCode);
        logRecord.setIsRead(0);
        logRecord.setCreatedAt(LocalDateTime.now());
        logRecord.setSendTime(LocalDateTime.now());

        // 1. 查询对应渠道配置状态
        SysNoticeChannel channel = noticeChannelMapper.selectOne(
                new LambdaQueryWrapper<SysNoticeChannel>().eq(SysNoticeChannel::getChannelCode, targetChannelCode)
        );

        if (channel != null && channel.getEnabled() == 0) {
            logRecord.setSendStatus(2);
            logRecord.setErrorMsg("渠道 [" + channel.getChannelName() + "] 已被管理员停用");
            log.warn("【党建通知调度中心】渠道已停用，拒绝发送: {}", channel.getChannelName());
        } else {
            // 2. 根据渠道策略工厂获取差异化 Handler 执行真实协议封包与发送
            NoticeChannelHandler handler = channelFactory.getHandler(targetChannelCode);
            if (handler == null) {
                logRecord.setSendStatus(2);
                logRecord.setErrorMsg("未识别的通知渠道服务: " + targetChannelCode);
            } else {
                NoticeMessagePayload payload = NoticeMessagePayload.builder()
                        .noticeType(logRecord.getNoticeType())
                        .title(title)
                        .content(content)
                        .receiverType(logRecord.getReceiverType())
                        .receiverId(receiverId)
                        .receiverName(receiverName)
                        .receiverTarget(receiverTarget)
                        .relatedMemberId(relatedMemberId)
                        .relatedStepCode(relatedStepCode)
                        .build();

                ChannelSendResult sendResult = handler.send(channel, payload);
                if (sendResult.isSuccess()) {
                    logRecord.setSendStatus(1);
                    log.info("【党建通知调度中心】通知经渠道 [{}] 成功分发 -> 消息ID: {}, 接收人: {}", 
                            handler.getChannelName(), sendResult.getMessageId(), receiverName);
                } else {
                    logRecord.setSendStatus(2);
                    logRecord.setErrorMsg(sendResult.getErrorMsg());
                    log.error("【党建通知调度中心】渠道 [{}] 分发失败: {}", handler.getChannelName(), sendResult.getErrorMsg());
                }
            }
        }

        noticeLogMapper.insert(logRecord);
        return logRecord;
    }

    @Override
    public int triggerComplianceWarningNotices() {
        int count = 0;
        List<PartyMember> members = partyMemberMapper.selectList(null);
        LocalDate today = LocalDate.now();

        for (PartyMember m : members) {
            // 1. 递交申请谈话超期预警 (30天红线)
            if (m.getCurrentStep() != null && m.getCurrentStep() == 2 && m.getApplyDate() != null) {
                long days = ChronoUnit.DAYS.between(m.getApplyDate(), today);
                if (days >= 20) {
                    sendNotice("WECHAT_WORK", "DEADLINE_WARNING",
                            "【时限预警】入党申请谈话临期提醒",
                            String.format("【%s】同志于 %s 递交入党申请书，已满 %d 天，距离“1个月内必须谈话”红线仅剩 %d 天，请支部抓紧开展谈话并归档。",
                                    m.getRealName(), m.getApplyDate(), days, Math.max(0, 30 - days)),
                            "ROLE", null, "支部组织委员", "org_admin", m.getId(), 2);
                    count++;
                }
            }

            // 2. 预备党员转正催办提醒 (满1年前30天)
            if (m.getCurrentStep() != null && m.getCurrentStep() == 23 && m.getProbationaryDate() != null) {
                LocalDate dueDate = m.getProbationaryDate().plusYears(1);
                long daysLeft = ChronoUnit.DAYS.between(today, dueDate);
                if (daysLeft <= 30 && daysLeft >= 0) {
                    sendNotice("SMS", "TRANS_PROBATION",
                            "【转正催办】预备党员转正申请提醒",
                            String.format("预备党员【%s】同志预备期将于 %s 届满（剩余 %d 天），请在满期前1-2周主动提交书面《转正申请书》。",
                                    m.getRealName(), dueDate, daysLeft),
                            "USER", m.getId(), m.getRealName(), "13800000000", m.getId(), 23);
                    count++;
                }
            }

            // 3. 发展对象廉洁审查纪检会签把关 (第13步)
            if (m.getCurrentStep() != null && m.getCurrentStep() == 13) {
                sendNotice("DINGTALK", "DISCIPLINE_AUDIT",
                        "【廉政把关】发展对象纪检会签待办提醒",
                        String.format("发展对象【%s】同志已完成政审综合调研，当前流转至集团纪检风控部出具《廉洁从业意见书》（一票否决权），请及时会签。",
                                m.getRealName()),
                        "ROLE", null, "周国平 (总支纪检委员)", "zhouguoping@honghe.com", m.getId(), 13);
                count++;
            }
        }
        return count;
    }

    @Override
    public int sendMeetingBroadcast(Long meetingId, String meetingTitle, String meetingDate, String meetingPlace, List<String> attendeeNames) {
        int count = 0;
        if (attendeeNames != null) {
            for (String attendee : attendeeNames) {
                sendNotice("IN_APP", "MEETING_NOTICE",
                        "【会议通知】" + meetingTitle,
                        String.format("党支部定于 %s 在 %s 召开【%s】，请您准时佩戴党徽参会并完成签到。",
                                meetingDate, meetingPlace != null ? meetingPlace : "党员活动室", meetingTitle),
                        "USER", null, attendee, attendee, null, null);
                count++;
            }
        }
        return count;
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SysNoticeLog> query = new LambdaQueryWrapper<SysNoticeLog>()
                .eq(SysNoticeLog::getIsRead, 0);
        if (userId != null) {
            query.and(w -> w.eq(SysNoticeLog::getReceiverId, userId).or().eq(SysNoticeLog::getReceiverType, "ALL"));
        }
        return noticeLogMapper.selectCount(query);
    }

    @Override
    public Map<String, Object> getNoticeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long totalCount = noticeLogMapper.selectCount(null);
        long successCount = noticeLogMapper.selectCount(new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getSendStatus, 1));
        long failCount = noticeLogMapper.selectCount(new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getSendStatus, 2));
        long unreadCount = noticeLogMapper.selectCount(new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getIsRead, 0));

        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failCount", failCount);
        stats.put("unreadCount", unreadCount);

        List<SysNoticeChannel> channels = noticeChannelMapper.selectList(null);
        stats.put("channels", channels);
        return stats;
    }
}
