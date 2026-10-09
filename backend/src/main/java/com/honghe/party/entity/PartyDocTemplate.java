package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("party_doc_template")
public class PartyDocTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer stepCode;
    private String templateCode;
    private String templateName;
    private Boolean isCustomized; // false: 系统默认, true: 管理员自定义
    private String defaultFileName;
    private String defaultFilePath;
    private String customFileName;
    private String customFilePath;
    private String fileVersion;
    private String placeholders;
    private String updateUserName;
    private LocalDateTime updatedAt;
}
