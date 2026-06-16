package com.livebeat.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * [order] OrderItem Spring Data JPA Repository（module 內部，package-private）
 */
interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, UUID> {
    List<OrderItemJpaEntity> findByOrderId(UUID orderId);
}
