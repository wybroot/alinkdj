package com.honghe.party.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.entity.*;
import com.honghe.party.mapper.*;
import com.honghe.party.notice.*;
import com.honghe.party.notice.dto.*;
import com.honghe.party.service.NoticeDispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class NoticeDispatchServiceImpl implements NoticeDispatchService {
    @Autowired private SysNoticeLogMapper noticeLogMapper;
    @Autowired private SysNoticeChannelMapper noticeChannelMapper;
    @Autowired private PartyMemberMapper partyMemberMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private SysUserRoleMapper userRoleMapper;
    @Autowired private SysRoleMapper roleMapper;
    @Autowired private NoticeChannelFactory channelFactory;

    @Override
    public SysNoticeLog sendNotice(String channelCode, String noticeType, String title, String content,
                                   String receiverType, Long receiverId, String receiverName, String receiverTarget,
                                   Long relatedMemberId, Integer relatedStepCode) {
        SysNoticeLog record = new SysNoticeLog();
        record.setChannelCode(channelCode == null ? "IN_APP" : channelCode.trim().toUpperCase(Locale.ROOT));
        record.setNoticeType(noticeType == null ? "REGULAR" : noticeType);
        record.setTitle(title); record.setContent(content); record.setReceiverType(receiverType == null ? "USER" : receiverType);
        record.setReceiverId(receiverId); record.setReceiverName(receiverName); record.setReceiverTarget(receiverTarget);
        record.setRelatedMemberId(relatedMemberId); record.setRelatedStepCode(relatedStepCode);
        return dispatch(record);
    }

    private SysNoticeLog dispatch(SysNoticeLog record) {
        if (record.getTitle() == null || record.getTitle().isBlank() || record.getTitle().length() > 200
                || record.getContent() == null || record.getContent().isBlank() || record.getContent().length() > 10000) {
            throw new IllegalArgumentException("通知标题须为1至200字，正文须为1至10000字");
        }
        record.setIsRead(0); record.setSendStatus(0);
        record.setCreatedAt(LocalDateTime.now()); record.setSendTime(LocalDateTime.now());
        // Resolve stored contact data for named recipients; never invent a destination.
        if (record.getReceiverId() != null) {
            SysUser user = userMapper.selectById(record.getReceiverId());
            if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                record.setSendStatus(2); record.setErrorMsg("接收账号不存在或已停用");
            } else {
                record.setReceiverName(user.getRealName());
                record.setReceiverTarget(switch (record.getChannelCode()) {
                    case "WECHAT_WORK" -> user.getWecomUserId();
                    case "DINGTALK" -> user.getDingtalkUserId();
                    case "SMS" -> user.getPhone();
                    case "EMAIL" -> user.getEmail();
                    default -> user.getId().toString();
                });
            }
        }
        try {
            // Commit audit intent before any remote side effect. No transaction spans the network call.
            noticeLogMapper.insert(record);
        } catch (DuplicateKeyException e) {
            if (record.getDedupeKey() != null) return null;
            throw e;
        }
        if (Integer.valueOf(2).equals(record.getSendStatus())) return record;
        SysNoticeChannel channel = noticeChannelMapper.selectOne(new LambdaQueryWrapper<SysNoticeChannel>()
                .eq(SysNoticeChannel::getChannelCode, record.getChannelCode()));
        NoticeChannelHandler handler = channelFactory.getHandler(record.getChannelCode());
        ChannelSendResult result;
        if (channel == null || !Integer.valueOf(1).equals(channel.getEnabled())) {
            result = ChannelSendResult.fail(record.getChannelCode(), "渠道尚未配置或未启用");
        } else if (handler == null) {
            result = ChannelSendResult.fail(record.getChannelCode(), "不支持该通知渠道");
        } else if (!"USER".equals(record.getReceiverType())) {
            result = ChannelSendResult.fail(record.getChannelCode(), "须将广播或角色收件范围解析为明确的接收人后发送");
        } else if ("IN_APP".equals(record.getChannelCode()) && record.getReceiverId() == null) {
            result = ChannelSendResult.fail(record.getChannelCode(), "站内通知必须选择一个系统用户");
        } else {
            try {
                result = handler.send(channel, NoticeMessagePayload.builder().noticeType(record.getNoticeType())
                        .title(record.getTitle()).content(record.getContent()).receiverType("USER")
                        .receiverId(record.getReceiverId()).receiverName(record.getReceiverName()).receiverTarget(record.getReceiverTarget())
                        .relatedMemberId(record.getRelatedMemberId()).relatedStepCode(record.getRelatedStepCode()).build());
            } catch (Exception e) {
                result = ChannelSendResult.unknown(record.getChannelCode(), "发送结果无法确认，请查询服务商记录");
            }
        }
        record.setSendStatus(result.getSendStatus());
        record.setProviderMessageId(result.getMessageId());
        record.setErrorMsg(result.getErrorMsg());
        noticeLogMapper.updateById(record);
        return record;
    }

    @Override
    public int triggerComplianceWarningNotices() {
        int accepted = 0;
        LocalDate today = LocalDate.now();
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        Map<Long, Set<String>> userRoles = new HashMap<>();
        Map<Long, String> roles = new HashMap<>();
        for (SysRole role : roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1))) roles.put(role.getId(), role.getRoleCode());
        for (SysUserRole link : userRoleMapper.selectList(null)) {
            if (roles.containsKey(link.getRoleId())) userRoles.computeIfAbsent(link.getUserId(), ignored -> new HashSet<>()).add(roles.get(link.getRoleId()));
        }
        for (PartyMember member : partyMemberMapper.selectList(null)) {
            if (Integer.valueOf(2).equals(member.getCurrentStep()) && member.getApplyDate() != null
                    && ChronoUnit.DAYS.between(member.getApplyDate(), today) >= 20) {
                List<SysUser> targets = users.stream().filter(user -> Objects.equals(user.getOrgId(), member.getOrgId())
                        && userRoles.getOrDefault(user.getId(), Set.of()).stream().anyMatch(code -> Set.of("BRANCH_ADMIN", "GENERAL_BRANCH_ADMIN").contains(code))).toList();
                accepted += warning(member, targets, "WECHAT_WORK", "DEADLINE_WARNING", "入党申请谈话临期提醒",
                        member.getRealName() + "同志于" + member.getApplyDate() + "递交入党申请书，请及时开展谈话并归档。", today);
            }
            if (Integer.valueOf(23).equals(member.getCurrentStep()) && member.getProbationaryDate() != null) {
                LocalDate due = member.getProbationaryDate().plusYears(1);
                long days = ChronoUnit.DAYS.between(today, due);
                if (days >= 0 && days <= 30) {
                    List<SysUser> targets = users.stream().filter(user -> Objects.equals(user.getMemberId(), member.getId())
                            || (member.getWorkNo() != null && member.getWorkNo().equals(user.getWorkNo()))).toList();
                    accepted += warning(member, targets, "SMS", "TRANS_PROBATION", "预备党员转正申请提醒",
                            "预备期将于" + due + "届满，请及时提交转正申请书。", today);
                }
            }
            if (Integer.valueOf(13).equals(member.getCurrentStep())) {
                List<SysUser> targets = users.stream().filter(user -> userRoles.getOrDefault(user.getId(), Set.of()).contains("GENERAL_BRANCH_ADMIN")).toList();
                accepted += warning(member, targets, "DINGTALK", "DISCIPLINE_AUDIT", "发展对象纪检会签提醒",
                        member.getRealName() + "同志正在政治审查阶段，请协调集团风险管控部完成廉洁从业审查会签。", today);
            }
        }
        return accepted;
    }

    private int warning(PartyMember member, List<SysUser> targets, String channel, String type, String title, String content, LocalDate date) {
        int accepted = 0;
        List<SysUser> recipients = targets.isEmpty() ? Collections.singletonList(null) : targets;
        for (SysUser target : recipients) {
            SysNoticeLog record = new SysNoticeLog();
            record.setChannelCode(channel); record.setNoticeType(type); record.setTitle(title); record.setContent(content);
            record.setReceiverType("USER"); record.setReceiverId(target == null ? null : target.getId());
            record.setReceiverName(target == null ? "未配置接收人" : target.getRealName());
            record.setRelatedMemberId(member.getId()); record.setRelatedStepCode(member.getCurrentStep());
            record.setDedupeKey(type + ":" + member.getId() + ":" + (target == null ? 0 : target.getId()) + ":" + date);
            SysNoticeLog result = dispatch(record);
            if (result != null && Integer.valueOf(1).equals(result.getSendStatus())) accepted++;
        }
        return accepted;
    }

    @Override
    public int sendMeetingBroadcast(Long meetingId, String title, String date, String place, List<String> names) {
        int count = 0;
        if (names == null) return count;
        for (String name : new HashSet<>(names)) {
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRealName, name).eq(SysUser::getStatus, 1));
            if (users.size() != 1) continue; // ambiguous names require explicit recipient selection
            SysNoticeLog result = sendNotice("IN_APP", "MEETING_NOTICE", title, "会议时间：" + date + "；地点：" + place,
                    "USER", users.get(0).getId(), name, null, null, null);
            if (Integer.valueOf(1).equals(result.getSendStatus())) count++;
        }
        return count;
    }

    @Override
    public long getUnreadCount(Long userId) {
        if (userId == null) return 0;
        return noticeLogMapper.selectCount(new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getIsRead, 0)
                .eq(SysNoticeLog::getSendStatus, 1).eq(SysNoticeLog::getReceiverId, userId));
    }

    @Override
    public Map<String, Object> getNoticeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", noticeLogMapper.selectCount(null));
        stats.put("acceptedCount", noticeLogMapper.selectCount(new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getSendStatus, 1)));
        stats.put("failCount", noticeLogMapper.selectCount(new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getSendStatus, 2)));
        stats.put("unknownCount", noticeLogMapper.selectCount(new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getSendStatus, 3)));
        return stats;
    }
}
