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
public class DingTalkChannelHandler extends AbstractChannelHandler {
    private final ProviderHttpClient http;
    private final AccessTokenCache tokens;

    public DingTalkChannelHandler(ChannelConfig configs, ProviderHttpClient http, AccessTokenCache tokens) {
        super(configs);
        this.http = http;
        this.tokens = tokens;
    }

    public String getChannelCode() { return "DINGTALK"; }
    public String getChannelName() { return "钉钉工作通知"; }

    public void validateConfig(SysNoticeChannel channel) {
        JsonNode cfg = configs.read(channel);
        if (!ChannelConfig.required(cfg, "corpId").matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException("钉钉组织 ID 格式不正确");
        ChannelConfig.required(cfg, "clientId");
        ChannelConfig.positiveLong(cfg, "agentId");
        configs.secret(cfg, "clientSecretEnv");
    }

    protected ChannelSendResult deliver(SysNoticeChannel channel, NoticeMessagePayload payload) {
        JsonNode cfg = configs.read(channel);
        String target = payload.getReceiverTarget().trim();
        if ("ALL".equals(payload.getReceiverType()) || !target.matches("[a-zA-Z0-9_.-]+(,[a-zA-Z0-9_.-]+)*")
                || target.split(",").length > 100) throw new IllegalArgumentException("请填写钉钉成员 UserID（最多100个，英文逗号分隔），不能填写邮箱或全员代号");
        String corpId = ChannelConfig.required(cfg, "corpId");
        String clientId = ChannelConfig.required(cfg, "clientId");
        String secret = configs.secret(cfg, "clientSecretEnv");
        String key = getChannelCode() + ":" + corpId + ":" + clientId + ":" + secret;
        var body = Map.of("agent_id", ChannelConfig.positiveLong(cfg, "agentId"), "userid_list", target,
                "to_all_user", false, "msg", Map.of("msgtype", "text", "text", Map.of("content", text(payload, 2048))));
        for (int attempt = 0; attempt < 2; attempt++) {
            String token = tokens.get(key, () -> http.post(UriComponentsBuilder
                    .fromHttpUrl("https://api.dingtalk.com/v1.0/oauth2/" + corpId + "/token").build().toUri(),
                    Map.of("client_id", clientId, "client_secret", secret, "grant_type", "client_credentials")));
            JsonNode response = http.post(UriComponentsBuilder
                    .fromHttpUrl("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2")
                    .queryParam("access_token", token).build().encode().toUri(), body);
            if (response == null || !response.has("errcode")) return ChannelSendResult.unknown(getChannelCode(), "钉钉响应缺少业务状态码，请查询任务记录");
            int code = response.path("errcode").asInt(-1);
            if (attempt == 0 && Set.of(88, 40014, 42001).contains(code)) {
                tokens.invalidate(key);
                continue;
            }
            if (code != 0) return ChannelSendResult.fail(getChannelCode(), "钉钉拒绝请求，错误码：" + code);
            String taskId = response.path("task_id").asText("");
            if (taskId.isBlank()) return ChannelSendResult.unknown(getChannelCode(), "钉钉未返回任务编号，请查询发送记录");
            return ChannelSendResult.ok(getChannelCode(), taskId, "钉钉已受理异步任务，尚不代表送达");
        }
        return ChannelSendResult.fail(getChannelCode(), "钉钉令牌更新后仍不可用");
    }
}
