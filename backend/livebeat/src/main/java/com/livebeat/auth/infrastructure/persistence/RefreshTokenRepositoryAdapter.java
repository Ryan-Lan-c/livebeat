package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.RefreshToken;
import com.livebeat.auth.domain.port.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository jpa;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpa.findByToken(token).map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpa.save(RefreshTokenJpaEntity.fromDomain(token)).toDomain();
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpa.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteExpired() {
        jpa.deleteByExpiresAtBefore(Instant.now());
    }
}
