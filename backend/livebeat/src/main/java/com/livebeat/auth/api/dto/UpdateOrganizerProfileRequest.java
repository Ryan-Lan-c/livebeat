package com.livebeat.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * [auth] 更新主辦方業務資料請求 DTO（所有欄位可選，null 表示不變更；is_blacklisted 為 ADMIN 專屬欄位，不在此開放）
 */
public record UpdateOrganizerProfileRequest(
        @Size(max = 255) String companyName,
        @Size(max = 20) String companyTaxId,
        @Size(max = 100) String contactPerson,
        @Size(max = 20) String contactPhone,
        String description,
        @Size(max = 500) String website,
        @Email @Size(max = 255) String contactEmail
) {}
