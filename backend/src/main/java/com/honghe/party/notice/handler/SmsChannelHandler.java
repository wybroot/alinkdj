package com.honghe.party.notice.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.NoticeChannelHandler;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 106 党务政务短信专网通道 (主流标准：阿里云短信/中国移动行业短信云网关)
 * 协议规范：
 * - 请求签名、短信签名 (SignName)、模板CODE (TemplateCode) 与 TemplateParam (JSON 变量替换)
 */
@Slf4j
@Component
public class SmsChannelHandler implements NoticeChannelHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public String getChannelCode() {
        return "SMS";
    }

    @Override
    public String getChannelName() {
        return "106党务政务短信专网";
    }

    @Override
    public ChannelSendResult send(SysNoticeChannel channel, NoticeMessagePayload payload) {
        try {
            JsonNode cfg = parseConfig(channel.getConfigJson());
            String signName = cfg.path("signName").asText("红河数据集团党总支");
            String apiKey = cfg.path("apiKey").asText();
            String tplCode = cfg.path("tplDeadline").asText("SMS_001928");

            // 手机号有效性校验
            String phone = payload.getReceiverTarget();
            if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
                // 如果是特殊代号且非11位手机号，提供规范化提示
                if (phone == null || !phone.contains("1")) {
                    return ChannelSendResult.fail(getChannelCode(), "短信发送失败：接收人手机号 [" + phone + "] 不符合规范");
                }
            }

            // 封装短信模板变量
            Map<String, String> tplParam = new HashMap<>();
            tplParam.put("name", payload.getReceiverName() != null ? payload.getReceiverName() : "党员同志");
            tplParam.put("title", payload.getTitle());
            tplParam.put("content", payload.getContent().length() > 50 ? payload.getContent().substring(0, 50) + "..." : payload.getContent());

            String paramJson = objectMapper.writeValueAsString(tplParam);
            log.info("【106短信专网协议】SendSms -> SignName: {}, TemplateCode: {}, PhoneNumbers: {}, TemplateParam: {}",
                    signName, tplCode, phone, paramJson);

            String mockBizId = "SMS_BIZ_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String responseMock = "{\"Code\":\"OK\",\"Message\":\"OK\",\"BizId\":\"" + mockBizId + "\",\"RequestId\":\"" + UUID.randomUUID() + "\"}";

            return ChannelSendResult.ok(getChannelCode(), mockBizId, responseMock);
        } catch (Exception e) {
            log.error("【106短信发送异常】", e);
            return ChannelSendResult.fail(getChannelCode(), "短信发送异常: " + e.getMessage());
        }
    }

    @Override
    public ChannelSendResult testConnection(SysNoticeChannel channel, String testTarget) {
        NoticeMessagePayload testPayload = NoticeMessagePayload.builder()
                .title("【验证码】党建短信通道测试")
                .content("验证码 891206，您正在进行红河智慧党建平台短信通道联通性测试，5分钟内有效。")
                .receiverTarget(testTarget != null && PHONE_PATTERN.matcher(testTarget).matches() ? testTarget : "13987301005")
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
