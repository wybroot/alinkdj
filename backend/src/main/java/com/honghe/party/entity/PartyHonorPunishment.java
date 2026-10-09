package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("party_honor_punishment")
public class PartyHonorPunishment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer category;        // 1: 个人奖惩/荣誉, 2: 组织奖惩/荣誉
    private Integer recordType;      // 1: 荣誉表彰, 2: 纪律处分/负面惩戒
    
    // 关联主体 (个人或党组织)
    private Long memberId;           // 个人主体ID (若类别为个人)
    private String targetName;       // 获奖/受处分主体名称 (如 党员姓名、或 某党支部名称)
    private Long orgId;              // 所属党组织ID
    private String orgName;          // 所属党组织名称
    
    // 奖惩/荣誉核心要素
    private String title;            // 奖惩/荣誉名称 (如：红河州优秀共产党员、先进基层党组织)
    private String level;            // 级别 (集团级、州级/市级、省部级、国家级)
    private String grantOrg;         // 授予/决定单位 (如：云南省国资委党委、集团党总支)
    private String docNo;            // 表彰文号/处分文号 (如：红数党总发〔2024〕12号)
    private LocalDate recordDate;    // 决定/表彰日期
    private String reasonContent;    // 主要事迹 / 处分原因与事实
    private String attachmentPath;   // 荣誉证书/红头文件扫描件存储路径
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
