package com.livebeat.auth.application.dto;

import java.util.UUID;

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
