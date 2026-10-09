package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_party_org")
public class PartyOrg {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private Integer orgType; // 1: 集团党总支, 2: 子公司党支部, 3: 直属党小组
    private Boolean hasApprovalRight;
    private String leaderName;
    private String businessScope;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
