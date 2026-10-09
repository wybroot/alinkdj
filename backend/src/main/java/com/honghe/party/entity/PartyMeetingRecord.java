package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("party_meeting_record")
public class PartyMeetingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;                  // 所属支部ID
    private Integer meetingType;         // 1:支委会, 2:党员大会, 3:党课, 4:主题党日
    private String meetingTitle;         // 会议主题
    private LocalDate meetingDate;       // 开会日期
    private String meetingPlace;         // 地点
    private String moderatorName;        // 主持人
    private String speakerName;          // 主讲人(党课)
    private Integer expectedCount;       // 应到人数
    private Integer actualCount;         // 实到人数
    private String attendeeNames;        // 参会人员
    private String meetingContent;       // 决议内容
    private String docFilePath;          // 会议纪要文件路径
    private Long relatedMemberId;        // 关联的发展党员ID
    private Integer relatedStepCode;     // 关联的25步步骤号 (4, 10, 18, 24)
    private LocalDateTime createdAt;
}
