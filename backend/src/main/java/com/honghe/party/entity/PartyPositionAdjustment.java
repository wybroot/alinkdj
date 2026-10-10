package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 党员党内职务调整备案实体
 */
@Data
@TableName("party_position_adjustment")
public class PartyPositionAdjustment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;
    private String memberName;
    private String workNo;
    private Long orgId;
    private String orgName;
    private String oldPost;
    private String newPost;
    private String adjustType; // 任职任命、换届任命、届中调整、兼任、免去职务
    private String documentNo; // 批准文号 / 任免批复号
    private LocalDate effectiveDate; // 生效日期 / 发文日期
    private String approvalUnit; // 批准单位 / 决定机关
    private String dutyDescription; // 职责分工说明
    private String operatorName; // 备案登记人
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
