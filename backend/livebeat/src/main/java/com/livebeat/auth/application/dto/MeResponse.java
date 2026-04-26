package com.livebeat.auth.application.dto;

import com.livebeat.auth.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * [auth] 當前登入使用者的個人資料回應 DTO
 *
 * 負責：GET /api/v1/auth/me 與 PUT /api/v1/auth/me 的回應格式
 */
public record MeResponse(
        UUID id,
        String email,
        String username,
        String role,
        String authProvider,
        boolean enabled,
        UUID organizerId,
        Instant createdAt,
        Instant updatedAt
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                user.getAuthProvider().name(),
                user.isEnabled(),
                user.getOrganizerId(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
