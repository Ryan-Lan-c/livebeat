package com.livebeat.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {
    Optional<RefreshTokenJpaEntity> findByToken(String token);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiresAt < :now")
    void deleteByExpiresAtBefore(LocalDateTime now);
}
