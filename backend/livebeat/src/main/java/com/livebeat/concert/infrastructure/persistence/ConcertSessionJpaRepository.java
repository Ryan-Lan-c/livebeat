package com.livebeat.concert.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * [concert] ConcertSession Spring Data JPA Repository
 */
interface ConcertSessionJpaRepository extends JpaRepository<ConcertSessionJpaEntity, UUID> {
    List<ConcertSessionJpaEntity> findByConcertIdOrderByEventDateAsc(UUID concertId);
}
