package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.UserProfile;
import com.livebeat.auth.domain.port.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] UserProfileRepository Port 的 JPA 實作（Adapter Out）
 *
 * 負責：銜接 domain port 與 Spring Data JPA；PK=user_id 非自動產生，
 *       save 時先 findById 決定走 update 或 insert 路徑
 */
@Repository
@RequiredArgsConstructor
class UserProfileRepositoryAdapter implements UserProfileRepository {

    private final UserProfileJpaRepository jpa;

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        return jpa.findById(userId).map(UserProfileJpaEntity::toDomain);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return jpa.findById(profile.getUserId())
                .map(existing -> {
                    existing.setAvatarUrl(profile.getAvatarUrl());
                    existing.setBio(profile.getBio());
                    existing.setPhone(profile.getPhone());
                    existing.setBirthDate(profile.getBirthDate());
                    existing.setAddress(profile.getAddress());
                    return jpa.save(existing).toDomain();
                })
                .orElseGet(() -> jpa.save(UserProfileJpaEntity.fromDomain(profile)).toDomain());
    }
}
