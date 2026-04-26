package com.livebeat.auth.application.dto;

import java.util.UUID;

/**
 * [auth] 認證成功後的 API 回應 DTO（不含 Refresh Token，僅含 Access Token）
 *
 * 負責：返回給前端的 Token 回應；Refresh Token 透過 Cookie 傳遞，不在此 DTO 中
 */
public record TokenResponse(
        String accessToken,
        UUID userId,
        String email,
        String username,
        String role
) {
    public static TokenResponse from(AuthResponse authResponse) {
        return new TokenResponse(
                authResponse.accessToken(),
                authResponse.userId(),
                authResponse.email(),
                authResponse.username(),
                authResponse.role()
        );
    }
}
