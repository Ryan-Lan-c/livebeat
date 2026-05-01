package com.livebeat.auth.application.dto;

import com.livebeat.auth.domain.model.UserProfile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * [auth] 使用者擴充個人資料回應 DTO
 *
 * 負責：GET/PUT /api/v1/auth/profile 的回應格式；及嵌入 MeResponse.profile 子物件
 */
public record UserProfileResponse(
        UUID userId,
        String avatarUrl,
        String bio,
        String phone,
        LocalDate birthDate,
        String address,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserProfileResponse from(UserProfile p) {
        return new UserProfileResponse(
                p.getUserId(), p.getAvatarUrl(), p.getBio(),
                p.getPhone(), p.getBirthDate(), p.getAddress(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
