package com.honghe.party.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiry;

    public JwtService(@Value("${party.jwt.secret}") String secret, @Value("${party.jwt.expire-seconds:7200}") long expiry) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("请设置至少32字节的随机 PARTY_JWT_SECRET");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiry = expiry;
    }

    public String issue(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder().issuer("honghe-party").subject(userId.toString()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiry))).signWith(key).compact();
    }

    public Long userId(String token) {
        return Long.valueOf(Jwts.parser().verifyWith(key).requireIssuer("honghe-party").build()
                .parseSignedClaims(token).getPayload().getSubject());
    }
}
