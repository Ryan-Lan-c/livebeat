package com.livebeat.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RefreshToken {
    private final Long id;
    private final Long userId;
    private final String token;
    private final LocalDateTime expiresAt;
    private final boolean revoked;
    private final LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public static RefreshToken create(Long userId, String token, long expirationSeconds) {
        LocalDateTime now = LocalDateTime.now();
        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .revoked(false)
                .createdAt(now)
                .build();
    }
}
