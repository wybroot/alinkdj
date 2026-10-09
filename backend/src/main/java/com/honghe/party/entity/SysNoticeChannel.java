package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notice_channel")
public class SysNoticeChannel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String channelCode; // SMS, EMAIL, WECHAT_WORK, DINGTALK, IN_APP
    private String channelName;
    private Integer channelType; // 1: 站内消息, 2: 手机短信, 3: 邮件, 4: 企业微信, 5: 钉钉
    private Integer enabled; // 1启用 0停用
    private String configJson; // 鉴权配置及API Key
    private String templateJson; // 预设模板配置
    private String remark;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
