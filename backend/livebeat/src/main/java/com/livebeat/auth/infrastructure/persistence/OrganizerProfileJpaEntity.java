package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.OrganizerProfile;
import com.livebeat.shared.persistence.TimestampedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * [auth] OrganizerProfile JPA 實體
 *
 * 負責：對應 auth.organizer_profiles 資料表；PK 為 user_id（非自動產生，對應 users.id）；
 *       繼承 TimestampedEntity（created_at/updated_at）；提供 toDomain / fromDomain 轉換
 */
@Entity
@Table(name = "organizer_profiles", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerProfileJpaEntity extends TimestampedEntity {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_tax_id", length = 20)
    private String companyTaxId;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private String website;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "is_blacklisted", nullable = false)
    private boolean isBlacklisted;

    @Column(name = "blacklist_reason", columnDefinition = "text")
    private String blacklistReason;

    @Column(name = "blacklisted_at")
    private Instant blacklistedAt;

    OrganizerProfile toDomain() {
        return OrganizerProfile.builder()
                .userId(userId).companyName(companyName).companyTaxId(companyTaxId)
                .contactPerson(contactPerson).contactPhone(contactPhone)
                .description(description).website(website).contactEmail(contactEmail)
                .isBlacklisted(isBlacklisted).blacklistReason(blacklistReason).blacklistedAt(blacklistedAt)
                .createdAt(getCreatedAt()).updatedAt(getUpdatedAt())
                .build();
    }

    static OrganizerProfileJpaEntity fromDomain(OrganizerProfile profile) {
        return OrganizerProfileJpaEntity.builder()
                .userId(profile.getUserId())
                .companyName(profile.getCompanyName())
                .companyTaxId(profile.getCompanyTaxId())
                .contactPerson(profile.getContactPerson())
                .contactPhone(profile.getContactPhone())
                .description(profile.getDescription())
                .website(profile.getWebsite())
                .contactEmail(profile.getContactEmail())
                .isBlacklisted(profile.isBlacklisted())
                .blacklistReason(profile.getBlacklistReason())
                .blacklistedAt(profile.getBlacklistedAt())
                .build();
    }
}
