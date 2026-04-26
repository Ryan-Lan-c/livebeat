package com.livebeat.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] User Spring Data JPA Repository
 *
 * 負責：提供使用者的資料庫查詢（依 email 查詢、存在性檢查）
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, UUID id);
}
