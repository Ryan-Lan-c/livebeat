package com.livebeat.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

/**
 * [auth] 主辦方業務資料領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝主辦方的公司資訊、黑名單狀態；對應 auth.organizer_profiles（1:1 關聯 users）
 */
@Getter
@Builder
@With
public class OrganizerProfile {
    private final UUID userId;
    private final String companyName;
    private final String companyTaxId;
    private final String contactPerson;
    private final String contactPhone;
    private final String description;
    private final String website;
    private final String contactEmail;
    private final boolean isBlacklisted;
    private final String blacklistReason;
    private final Instant blacklistedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
