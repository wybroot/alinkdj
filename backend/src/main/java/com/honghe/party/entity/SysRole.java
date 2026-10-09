package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.honghe.party.common.JsonStringTypeHandler;

import java.time.LocalDateTime;

@Data
@TableName(value = "sys_role", autoResultMap = true)
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer sortOrder;
    private Integer status; // 1正常 0禁用
    @TableField(typeHandler = JsonStringTypeHandler.class)
    private String permissions; // JSON 权限标识数组
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
