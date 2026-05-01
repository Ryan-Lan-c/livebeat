package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.OrganizerProfile;
import com.livebeat.auth.domain.port.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] OrganizerProfileRepository Port 的 JPA 實作（Adapter Out）
 *
 * 負責：銜接 domain port 與 Spring Data JPA；PK=user_id 非自動產生，
 *       save 時先 findById 決定走 update 或 insert 路徑
 */
@Repository
@RequiredArgsConstructor
class OrganizerProfileRepositoryAdapter implements OrganizerProfileRepository {

    private final OrganizerProfileJpaRepository jpa;

    @Override
    public Optional<OrganizerProfile> findByUserId(UUID userId) {
        return jpa.findById(userId).map(OrganizerProfileJpaEntity::toDomain);
    }

    @Override
    public OrganizerProfile save(OrganizerProfile profile) {
        return jpa.findById(profile.getUserId())
                .map(existing -> {
                    existing.setCompanyName(profile.getCompanyName());
                    existing.setCompanyTaxId(profile.getCompanyTaxId());
                    existing.setContactPerson(profile.getContactPerson());
                    existing.setContactPhone(profile.getContactPhone());
                    existing.setDescription(profile.getDescription());
                    existing.setWebsite(profile.getWebsite());
                    existing.setContactEmail(profile.getContactEmail());
                    existing.setBlacklisted(profile.isBlacklisted());
                    existing.setBlacklistReason(profile.getBlacklistReason());
                    existing.setBlacklistedAt(profile.getBlacklistedAt());
                    return jpa.save(existing).toDomain();
                })
                .orElseGet(() -> jpa.save(OrganizerProfileJpaEntity.fromDomain(profile)).toDomain());
    }
}
