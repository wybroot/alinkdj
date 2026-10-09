package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("party_member")
public class PartyMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orgId;
    private String deptName;
    private String workNo;
    private String realName;
    private String idCard;
    private Integer gender; // 1:男, 2:女
    private LocalDate birthDate;
    private String education;
    private String jobTitle;
    
    // 党员花名册核心属性
    private Integer partyStatus;         // 1: 正式党员, 2: 预备党员, 3: 发展对象, 4: 积极分子, 5: 申请人
    private String partyPost;            // 党内职务 (总支书记、支部书记、副书记、组织委员、纪检委员、宣传委员、党小组长、普通党员)
    private Integer partyStandingYears;  // 党龄 (年)
    private LocalDate joinPartyDate;     // 入党日期 (接收预备党员日期)
    private LocalDate officialPartyDate; // 正式转正日期
    private Integer duesStatus;          // 1: 正常, 2: 本月待缴, 3: 异常
    private String nationalPartyCode;    // 全国党员信息系统编码
    private Integer annualStudyHours;    // 年度集中培训时长 (学时)
    private Integer studyTargetHours = 40; // 目标集中培训时长 (标准40学时达标)

    // 组织关系转接属性
    private String originBranch;         // 原所在党支部 (转入前所在支部/入党时原支部)
    private LocalDate transferInDate;    // 转入本支部时间
    private LocalDate transferOutDate;   // 转出本支部时间
    private String transferOutBranch;    // 转出支部
    
    // 国企特色标识
    private Boolean isFrontline;         // 生产/业务一线
    private Boolean isTechnicalTalent;   // 大数据/技术研发骨干
    private Boolean isDualCultivate;     // 双培养骨干
    
    // 流程步骤状态
    private Integer currentStage;        // 1~5
    private Integer currentStep;         // 1~25
    private Integer stepStatus;          // 1进行中 2审核中 3完成 4临期预警 5阻断
    
    // 关键核心时间节点 (合规引擎计算基础)
    private LocalDate applyDate;
    private LocalDate firstTalkDate;
    private LocalDate activistDate;
    private LocalDate targetDate;
    private LocalDate examPassDate;
    private Integer disciplineCheckPass; // 1通过 0未审 -1否决
    private LocalDate probationaryDate;
    private LocalDate committeePassDate;
    private LocalDate officialApplyDate;
    private LocalDate officialDate;
    
    private Integer status;              // 1正常 2延期 3取消 4转出
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
