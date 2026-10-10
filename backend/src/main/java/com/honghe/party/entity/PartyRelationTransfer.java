package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 党员组织关系转接记录实体
 */
@Data
@TableName("party_relation_transfer")
public class PartyRelationTransfer {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long memberId;
    private String memberName;
    private String idCard;
    private String workNo;
    private Integer gender; // 1:男, 2:女
    private String phone;
    private Integer partyStatus; // 1:正式党员, 2:预备党员
    private String partyPost;
    private Integer transferType; // 1:转入, 2:转出
    private Long fromOrgId;
    private String fromOrgName;
    private Long toOrgId;
    private String toOrgName;
    private String letterNo;
    private LocalDate transferDate;
    private String transferReason;
    private LocalDate duesPaidToDate;
    private String operatorName;
    private Integer approvalStatus; // 1:审核中, 2:已办结, 3:已驳回
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
