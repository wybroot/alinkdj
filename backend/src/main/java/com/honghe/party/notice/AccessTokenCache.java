package com.honghe.party.notice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class AccessTokenCache {
    private record Entry(String value, Instant expiresAt) { }
    private final Map<String, Entry> entries = new HashMap<>();

    public synchronized String get(String key, Supplier<JsonNode> loader) {
        Entry cached = entries.get(key);
        if (cached != null && Instant.now().isBefore(cached.expiresAt())) return cached.value();
        JsonNode response = loader.get();
        if (response == null || (response.has("errcode") && response.path("errcode").asInt(-1) != 0)) {
            throw new IllegalArgumentException("获取应用令牌失败，请检查凭据、应用权限和服务器 IP 白名单");
        }
        String token = ChannelConfig.required(response, "access_token");
        int ttl = response.path("expires_in").asInt(0);
        if (ttl <= 0) throw new IllegalArgumentException("令牌响应缺少有效期");
        entries.entrySet().removeIf(entry -> Instant.now().isAfter(entry.getValue().expiresAt()));
        if (entries.size() >= 32) entries.clear();
        entries.put(key, new Entry(token, Instant.now().plusSeconds(Math.max(1, ttl - Math.min(120, ttl / 10)))));
        return token;
    }

    public synchronized void invalidate(String key) { entries.remove(key); }
}
