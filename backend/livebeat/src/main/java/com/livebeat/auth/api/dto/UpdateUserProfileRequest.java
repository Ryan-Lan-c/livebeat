package com.livebeat.auth.api.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * [auth] 更新使用者個人資料請求 DTO（所有欄位可選，null 表示不變更）
 */
public record UpdateUserProfileRequest(
        @Size(max = 500) String avatarUrl,
        String bio,
        @Size(max = 20) String phone,
        LocalDate birthDate,
        String address
) {}
