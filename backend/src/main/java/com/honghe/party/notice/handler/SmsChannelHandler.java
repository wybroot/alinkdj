package com.honghe.party.notice.handler;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.*;
import com.honghe.party.notice.dto.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class SmsChannelHandler extends AbstractChannelHandler {
    private final AliyunSmsGateway gateway;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> SOURCES = Set.of("receiverName", "title", "content");

    public SmsChannelHandler(ChannelConfig configs, AliyunSmsGateway gateway) {
        super(configs);
        this.gateway = gateway;
    }

    public String getChannelCode() { return "SMS"; }
    public String getChannelName() { return "阿里云短信"; }

    public void validateConfig(SysNoticeChannel channel) {
        JsonNode cfg = configs.read(channel);
        if (!"ALIYUN".equals(ChannelConfig.required(cfg, "provider"))) throw new IllegalArgumentException("当前短信通道仅支持阿里云短信");
        ChannelConfig.required(cfg, "signName");
        configs.secret(cfg, "accessKeyIdEnv");
        configs.secret(cfg, "accessKeySecretEnv");
        JsonNode templates = configs.parse(channel.getTemplateJson());
        if (templates.isEmpty()) throw new IllegalArgumentException("请配置已审核通过的短信模板及变量映射");
        templates.fields().forEachRemaining(entry -> {
            JsonNode template = entry.getValue();
            if (!ChannelConfig.required(template, "templateCode").matches("SMS_[A-Za-z0-9]+")) throw new IllegalArgumentException("短信模板编码格式不正确");
            JsonNode params = template.path("parameters");
            if (!params.isObject()) throw new IllegalArgumentException("短信模板 parameters 必须是变量映射对象，无变量时填写空对象");
            params.fields().forEachRemaining(param -> {
                if (!SOURCES.contains(param.getValue().asText())) throw new IllegalArgumentException("短信变量来源仅支持 receiverName、title、content");
            });
        });
    }

    protected ChannelSendResult deliver(SysNoticeChannel channel, NoticeMessagePayload payload) throws Exception {
        String phone = payload.getReceiverTarget().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) throw new IllegalArgumentException("短信接收地址必须是一个有效的中国大陆手机号");
        JsonNode cfg = configs.read(channel);
        JsonNode template = configs.parse(channel.getTemplateJson()).path(payload.getNoticeType() == null ? "REGULAR" : payload.getNoticeType());
        String templateCode = ChannelConfig.required(template, "templateCode");
        Map<String, String> values = Map.of("receiverName", payload.getReceiverName() == null ? "" : payload.getReceiverName(),
                "title", payload.getTitle(), "content", payload.getContent());
        Map<String, String> params = new LinkedHashMap<>();
        template.path("parameters").fields().forEachRemaining(entry -> {
            String value = values.get(entry.getValue().asText());
            if (value == null || value.isBlank()) throw new IllegalArgumentException("短信模板变量 " + entry.getKey() + " 缺少值");
            params.put(entry.getKey(), value);
        });
        var request = new SendSmsRequest().setPhoneNumbers(phone).setSignName(ChannelConfig.required(cfg, "signName"))
                .setTemplateCode(templateCode).setTemplateParam(mapper.writeValueAsString(params));
        SendSmsResponseBody response;
        try {
            response = gateway.send(configs.secret(cfg, "accessKeyIdEnv"), configs.secret(cfg, "accessKeySecretEnv"), request);
        } catch (com.aliyun.tea.TeaException e) {
            String code = e.getCode() != null ? e.getCode() : "TeaException";
            return ChannelSendResult.fail(getChannelCode(), "阿里云短信拒绝请求，错误码：" + code);
        }
        if (response == null || response.getCode() == null) return ChannelSendResult.unknown(getChannelCode(), "短信服务未返回业务状态，请查询回执");
        if (!"OK".equals(response.getCode())) return ChannelSendResult.fail(getChannelCode(), "阿里云短信拒绝请求，错误码：" + response.getCode());
        if (response.getBizId() == null || response.getBizId().isBlank()) return ChannelSendResult.unknown(getChannelCode(), "短信服务未返回回执编号，请查询发送记录");
        return ChannelSendResult.ok(getChannelCode(), response.getBizId(), "短信已提交运营商，实际送达以服务商回执为准");
    }
}
