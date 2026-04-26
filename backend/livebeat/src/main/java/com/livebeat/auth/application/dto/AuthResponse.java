package com.livebeat.auth.application.dto;

import java.util.UUID;

/**
 * [auth] 認證操作的內部回應 DTO（包含 Refresh Token，不對外暴露）
 *
 * 負責：AuthService 與 AuthController 之間傳遞 Token 與使用者基本資訊
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String email,
        String username,
        String role
) {}
