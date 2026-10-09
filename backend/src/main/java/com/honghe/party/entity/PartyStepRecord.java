package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("party_step_record")
public class PartyStepRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Integer stepCode;       // 1~25
    private String stepName;
    private Long handlerUserId;
    private String handlerName;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime dueTime;
    private Integer auditStatus;    // 0待办 1处理中 2通过 3退回 4阻断
    private String meetingInfo;     // 会议届次/文号
    private String auditOpinion;
    private LocalDateTime createdAt;
}
