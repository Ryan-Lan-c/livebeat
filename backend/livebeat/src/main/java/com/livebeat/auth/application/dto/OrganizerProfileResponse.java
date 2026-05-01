package com.livebeat.auth.application.dto;

import com.livebeat.auth.domain.model.OrganizerProfile;

import java.time.Instant;
import java.util.UUID;

/**
 * [auth] 主辦方業務資料回應 DTO
 *
 * 負責：GET/PUT /api/v1/admin/organizer/profile 的回應格式；is_blacklisted 等黑名單欄位為唯讀輸出
 */
public record OrganizerProfileResponse(
        UUID userId,
        String companyName,
        String companyTaxId,
        String contactPerson,
        String contactPhone,
        String description,
        String website,
        String contactEmail,
        boolean isBlacklisted,
        String blacklistReason,
        Instant blacklistedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrganizerProfileResponse from(OrganizerProfile p) {
        return new OrganizerProfileResponse(
                p.getUserId(), p.getCompanyName(), p.getCompanyTaxId(),
                p.getContactPerson(), p.getContactPhone(),
                p.getDescription(), p.getWebsite(), p.getContactEmail(),
                p.isBlacklisted(), p.getBlacklistReason(), p.getBlacklistedAt(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
