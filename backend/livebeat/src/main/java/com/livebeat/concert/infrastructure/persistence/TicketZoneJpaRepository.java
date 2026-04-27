package com.livebeat.concert.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * [concert] TicketZone Spring Data JPA Repository
 */
interface TicketZoneJpaRepository extends JpaRepository<TicketZoneJpaEntity, UUID> {

    List<TicketZoneJpaEntity> findBySessionIdOrderByZoneCodeAsc(UUID sessionId);

    @Query("SELECT CASE WHEN (z.soldSeats > 0 OR z.lockedSeats > 0) THEN TRUE ELSE FALSE END " +
           "FROM TicketZoneJpaEntity z WHERE z.id = :id")
    boolean hasActiveSales(@Param("id") UUID id);
}
