package com.honghe.party.service;

import com.honghe.party.entity.SysNoticeLog;

import java.util.List;
import java.util.Map;

public interface NoticeDispatchService {
    /**
     * 发送单条通知（根据通知渠道配置分发）
     */
    SysNoticeLog sendNotice(String channelCode, String noticeType, String title, String content,
                            String receiverType, Long receiverId, String receiverName, String receiverTarget,
                            Long relatedMemberId, Integer relatedStepCode);

    /**
     * 触发党务合规预警自动分发
     */
    int triggerComplianceWarningNotices();

    /**
     * 发送会议通知给支部或指定党员
     */
    int sendMeetingBroadcast(Long meetingId, String meetingTitle, String meetingDate, String meetingPlace, List<String> attendeeNames);

    /**
     * 获取未读通知总数
     */
    long getUnreadCount(Long userId);

    /**
     * 获取通知统计数据（各渠道发送成功数、失败数等）
     */
    Map<String, Object> getNoticeStatistics();
}
