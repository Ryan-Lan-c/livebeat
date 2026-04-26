package com.livebeat.auth.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String username,
        String role
) {}
