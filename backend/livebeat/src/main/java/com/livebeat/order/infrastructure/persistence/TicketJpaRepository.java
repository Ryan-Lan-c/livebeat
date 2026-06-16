package com.livebeat.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * [order] Ticket Spring Data JPA Repository（module 內部，package-private）
 */
interface TicketJpaRepository extends JpaRepository<TicketJpaEntity, UUID> {
    List<TicketJpaEntity> findByOrderItemId(UUID orderItemId);
}
