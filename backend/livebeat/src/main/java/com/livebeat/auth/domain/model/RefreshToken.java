package com.livebeat.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * [auth] Refresh Token 領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝 Refresh Token 的狀態（revoked、expiresAt），提供有效性判斷（isValid）
 */
@Getter
@Builder
public class RefreshToken {
    private final UUID id;
    private final UUID userId;
    private final String token;
    private final Instant expiresAt;
    private final boolean revoked;
    private final Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public static RefreshToken create(UUID userId, String token, long expirationSeconds) {
        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(Instant.now().plusSeconds(expirationSeconds))
                .revoked(false)
                .build();
    }
}
