package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notice_log")
public class SysNoticeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String noticeType; // DEADLINE_WARNING, DISCIPLINE_AUDIT, TRANS_PROBATION, MEETING_NOTICE, REGULAR
    private String title;
    private String content;
    private String receiverType; // USER, ROLE, ORG, ALL
    private Long receiverId;
    private String receiverName;
    private String receiverTarget; // 手机号/邮箱/企微userId
    private String channelCode;
    private Integer sendStatus; // 1成功 2失败 0待发送
    private String errorMsg;
    private Integer isRead; // 0未读 1已读
    private Long relatedMemberId;
    private Integer relatedStepCode;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
    private LocalDateTime createdAt;
}
