package com.livebeat.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * [auth] OrganizerProfile Spring Data JPA Repository
 */
interface OrganizerProfileJpaRepository extends JpaRepository<OrganizerProfileJpaEntity, UUID> {
}
