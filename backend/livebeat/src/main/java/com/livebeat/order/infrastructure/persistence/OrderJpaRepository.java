package com.livebeat.order.infrastructure.persistence;

import com.livebeat.order.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [order] Order Spring Data JPA Repository（module 內部，package-private）
 */
interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);

    List<OrderJpaEntity> findByStatusAndExpiresAtBefore(OrderStatus status, Instant now);
}
