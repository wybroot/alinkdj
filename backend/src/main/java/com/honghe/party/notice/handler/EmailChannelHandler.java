package com.honghe.party.notice.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.*;
import com.honghe.party.notice.dto.*;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EmailChannelHandler extends AbstractChannelHandler {
    private final SmtpGateway gateway;

    public EmailChannelHandler(ChannelConfig configs, SmtpGateway gateway) {
        super(configs);
        this.gateway = gateway;
    }

    public String getChannelCode() { return "EMAIL"; }
    public String getChannelName() { return "邮件通知（SMTP）"; }

    private void validateAddress(String address) {
        if (!address.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) {
            throw new IllegalArgumentException("请填写单个有效邮箱地址");
        }
    }

    public void validateConfig(SysNoticeChannel channel) {
        JsonNode cfg = configs.read(channel);
        if (!ChannelConfig.required(cfg, "host").matches("[A-Za-z0-9.-]+")) throw new IllegalArgumentException("SMTP 主机应为域名或 IP，不含协议和路径");
        if (ChannelConfig.positiveLong(cfg, "port") > 65535) throw new IllegalArgumentException("SMTP 端口超出范围");
        if (!Set.of("SSL", "STARTTLS").contains(ChannelConfig.required(cfg, "security"))) throw new IllegalArgumentException("SMTP 必须选择 SSL 或 STARTTLS");
        ChannelConfig.required(cfg, "username");
        validateAddress(ChannelConfig.required(cfg, "from"));
        configs.secret(cfg, "passwordEnv");
    }

    protected ChannelSendResult deliver(SysNoticeChannel channel, NoticeMessagePayload payload) throws Exception {
        validateAddress(payload.getReceiverTarget().trim());
        JsonNode cfg = configs.read(channel);
        try {
            String id = gateway.send(cfg, configs.secret(cfg, "passwordEnv"), payload.getReceiverTarget().trim(), payload.getTitle(), payload.getContent());
            return ChannelSendResult.ok(getChannelCode(), id, "SMTP 服务器已受理，最终投递以收件箱或退信为准");
        } catch (MailAuthenticationException e) {
            return ChannelSendResult.fail(getChannelCode(), "SMTP 鉴权失败，请核对账号、授权码及 SMTP 服务开关");
        }
    }
}
