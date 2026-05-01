package com.livebeat.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * [auth] UserProfile Spring Data JPA Repository
 */
interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, UUID> {
}
