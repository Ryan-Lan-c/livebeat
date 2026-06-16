package com.livebeat.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * [order] Order Spring Data JPA Repository（module 內部，package-private）
 */
interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
