package com.livebeat.order.infrastructure.persistence;

import com.livebeat.order.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * [order] OrderItem Spring Data JPA Repository（module 內部，package-private）
 */
interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, UUID> {
    List<OrderItemJpaEntity> findByOrderId(UUID orderId);

    /** 某票區進行中（指定狀態且未過期）訂單的鎖定張數加總。 */
    @Query("""
            select coalesce(sum(i.quantity), 0)
            from OrderItemJpaEntity i, OrderJpaEntity o
            where i.orderId = o.id
              and i.zoneId = :zoneId
              and o.status = :status
              and o.expiresAt > :now
            """)
    int sumActiveQuantity(@Param("zoneId") UUID zoneId,
                          @Param("status") OrderStatus status,
                          @Param("now") Instant now);
}
