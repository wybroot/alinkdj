package com.honghe.party.notice.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.*;
import com.honghe.party.notice.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Set;

@Component
public class WeChatWorkChannelHandler extends AbstractChannelHandler {
    private final ProviderHttpClient http;
    private final AccessTokenCache tokens;
    private static final String BASE = "https://qyapi.weixin.qq.com";

    public WeChatWorkChannelHandler(ChannelConfig configs, ProviderHttpClient http, AccessTokenCache tokens) {
        super(configs);
        this.http = http;
        this.tokens = tokens;
    }

    public String getChannelCode() { return "WECHAT_WORK"; }
    public String getChannelName() { return "企业微信应用消息"; }

    public void validateConfig(SysNoticeChannel channel) {
        JsonNode cfg = configs.read(channel);
        ChannelConfig.required(cfg, "corpId");
        ChannelConfig.positiveLong(cfg, "agentId");
        configs.secret(cfg, "secretEnv");
    }

    protected ChannelSendResult deliver(SysNoticeChannel channel, NoticeMessagePayload payload) {
        JsonNode cfg = configs.read(channel);
        String target = payload.getReceiverTarget().trim();
        if (target.contains("@all") || "ALL".equals(payload.getReceiverType())
                || !target.matches("[a-zA-Z0-9_.@-]+(\\|[a-zA-Z0-9_.@-]+)*") || target.split("\\|").length > 1000) {
            throw new IllegalArgumentException("请填写企业微信成员 UserID；多个成员用 | 分隔，不支持隐式全员广播");
        }
        String corpId = ChannelConfig.required(cfg, "corpId");
        String secret = configs.secret(cfg, "secretEnv");
        String key = getChannelCode() + ":" + corpId + ":" + secret;
        var body = Map.of("touser", target, "agentid", ChannelConfig.positiveLong(cfg, "agentId"),
                "msgtype", "text", "text", Map.of("content", text(payload, 2048)),
                "enable_duplicate_check", 1, "duplicate_check_interval", 1800);
        for (int attempt = 0; attempt < 2; attempt++) {
            String token = tokens.get(key, () -> http.get(UriComponentsBuilder.fromHttpUrl(BASE + "/cgi-bin/gettoken")
                    .queryParam("corpid", corpId).queryParam("corpsecret", secret).build().encode().toUri()));
            JsonNode response = http.post(UriComponentsBuilder.fromHttpUrl(BASE + "/cgi-bin/message/send")
                    .queryParam("access_token", token).build().encode().toUri(), body);
            if (response == null || !response.has("errcode")) return ChannelSendResult.unknown(getChannelCode(), "企业微信响应缺少业务状态码，请查询发送记录");
            int code = response.path("errcode").asInt(-1);
            if (attempt == 0 && Set.of(40014, 42001, 40001).contains(code)) {
                tokens.invalidate(key);
                continue;
            }
            if (code != 0) return ChannelSendResult.fail(getChannelCode(), "企业微信拒绝请求，错误码：" + code);
            String id = response.path("msgid").asText("");
            if (id.isBlank()) return ChannelSendResult.unknown(getChannelCode(), "企业微信未返回消息编号，请查询发送记录");
            ChannelSendResult result = ChannelSendResult.ok(getChannelCode(), id, "企业微信已受理");
            for (String field : Set.of("invaliduser", "invalidparty", "invalidtag", "unlicenseduser")) {
                if (!response.path(field).asText("").isBlank()) {
                    result.setSuccess(false);
                    result.setSendStatus(4);
                    result.setErrorMsg("部分接收人无效或不在应用可见范围/许可内，请在企业微信核对；勿整批重发");
                }
            }
            return result;
        }
        return ChannelSendResult.fail(getChannelCode(), "企业微信令牌更新后仍不可用");
    }
}
