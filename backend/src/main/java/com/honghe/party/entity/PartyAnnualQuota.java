package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("party_annual_quota")
public class PartyAnnualQuota {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer yearVal;
    private Long orgId;
    private Integer totalQuota;
    private Integer usedQuota;
    private BigDecimal frontlineTargetRatio;
    private BigDecimal youthTargetRatio;
    private BigDecimal talentTargetRatio;
    private LocalDateTime createdAt;
}
