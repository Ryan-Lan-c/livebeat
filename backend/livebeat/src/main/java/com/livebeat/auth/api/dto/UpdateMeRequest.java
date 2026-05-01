package com.livebeat.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * [auth] 更新個人資料請求 DTO
 *
 * 負責：PUT /api/v1/auth/me 的請求格式，目前支援更新顯示名稱（username）
 */
public record UpdateMeRequest(
        @NotBlank @Size(min = 3, max = 50) String username
) {}
