package com.livebeat.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

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
