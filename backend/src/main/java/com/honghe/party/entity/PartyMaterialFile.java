package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("party_material_file")
public class PartyMaterialFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Integer stepCode;
    private String materialCode;
    private String materialName;
    private String filePath;
    private Long fileSize;
    private Boolean isRequired;
    private Integer reviewStatus; // 0未交 1待审 2合格 3退回
    private String rejectReason;
    private LocalDateTime createdAt;
}
