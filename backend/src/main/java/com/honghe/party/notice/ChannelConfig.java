package com.honghe.party.notice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honghe.party.entity.SysNoticeChannel;
import org.springframework.stereotype.Component;

/** Credentials are referenced by environment variable name; they never enter the database or browser. */
@Component
public class ChannelConfig {
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode parse(String json) {
        try {
            JsonNode node = mapper.readTree(json == null || json.isBlank() ? "{}" : json);
            if (node == null || !node.isObject()) throw new IllegalArgumentException();
            return node;
        } catch (Exception e) {
            throw new IllegalArgumentException("渠道配置必须是 JSON 对象");
        }
    }

    public JsonNode read(SysNoticeChannel channel) {
        if (channel == null) throw new IllegalArgumentException("通知渠道尚未配置");
        return parse(channel.getConfigJson());
    }

    public static String required(JsonNode node, String key) {
        String value = node.path(key).asText("").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("缺少渠道参数：" + key);
        return value;
    }

    public static long positiveLong(JsonNode node, String key) {
        try {
            long value = Long.parseLong(required(node, key));
            if (value > 0) return value;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(key + " 必须是正整数");
    }

    public String secret(JsonNode node, String field) {
        String name = required(node, field);
        if (!name.matches("PARTY_NOTICE_[A-Z0-9_]+")) {
            throw new IllegalArgumentException(field + " 必须引用 PARTY_NOTICE_ 开头的环境变量");
        }
        String value = environment(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("服务器尚未设置凭据环境变量：" + name);
        return value;
    }

    protected String environment(String name) {
        return System.getenv(name);
    }
}
