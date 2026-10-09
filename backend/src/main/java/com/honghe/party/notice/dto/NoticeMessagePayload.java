package com.honghe.party.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeMessagePayload {
    private String noticeType; // DEADLINE_WARNING, TRANS_PROBATION, DISCIPLINE_AUDIT, MEETING_NOTICE, REGULAR
    private String title;
    private String content;
    private String receiverType; // USER, ROLE, ORG, ALL
    private Long receiverId;
    private String receiverName;
    private String receiverTarget; // 手机号 / 邮箱 / 企微账号 / 钉钉userid
    private Long relatedMemberId;
    private Integer relatedStepCode;
    private Map<String, Object> extraParams;
}
