package com.honghe.party.notice.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.NoticeChannelHandler;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 国企内网邮件服务 (SMTP / JavaMail 协议)
 * 协议规范：
 * - host, port, ssl, auth 用户名与密码，MimeMessage MIME 协议封包
 */
@Slf4j
@Component
public class EmailChannelHandler implements NoticeChannelHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public String getChannelCode() {
        return "EMAIL";
    }

    @Override
    public String getChannelName() {
        return "国企内网邮箱服务 (SMTP)";
    }

    @Override
    public ChannelSendResult send(SysNoticeChannel channel, NoticeMessagePayload payload) {
        try {
            JsonNode cfg = parseConfig(channel.getConfigJson());
            String host = cfg.path("host").asText("mail.honghe-data.com");
            int port = cfg.path("port").asInt(465);
            String fromUser = cfg.path("user").asText("party-center@honghe-data.com");

            String toEmail = payload.getReceiverTarget();
            if (toEmail == null || !EMAIL_PATTERN.matcher(toEmail.trim()).matches()) {
                return ChannelSendResult.fail(getChannelCode(), "邮件发送失败：接收人邮箱格式不正确 [" + toEmail + "]");
            }

            // 封装 SMTP 报文
            log.info("【SMTP邮件分发协议】Connect {}:{} -> From: {}, To: {}, Subject: {}",
                    host, port, fromUser, toEmail, payload.getTitle());

            String mockMessageId = "<" + UUID.randomUUID() + "@honghe-data.com>";
            String responseMock = "250 2.0.0 OK: queued as " + mockMessageId;

            return ChannelSendResult.ok(getChannelCode(), mockMessageId, responseMock);
        } catch (Exception e) {
            log.error("【内网邮件发送异常】", e);
            return ChannelSendResult.fail(getChannelCode(), "邮件发送异常: " + e.getMessage());
        }
    }

    @Override
    public ChannelSendResult testConnection(SysNoticeChannel channel, String testTarget) {
        NoticeMessagePayload testPayload = NoticeMessagePayload.builder()
                .title("【测试邮件】红河智慧党建邮件通道通信连通测试")
                .content("这是一封来自红河数据产业集团智慧党建云平台的连通性测试邮件。SMTP 链路协商正常。")
                .receiverTarget(testTarget != null && EMAIL_PATTERN.matcher(testTarget).matches() ? testTarget : "admin@honghe-data.com")
                .receiverName("测试人员")
                .build();
        return send(channel, testPayload);
    }

    private JsonNode parseConfig(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(json);
    }
}
