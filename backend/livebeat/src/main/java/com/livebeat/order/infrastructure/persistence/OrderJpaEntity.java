package com.livebeat.order.infrastructure.persistence;

import com.livebeat.order.domain.model.Order;
import com.livebeat.order.domain.model.OrderItem;
import com.livebeat.order.domain.model.OrderStatus;
import com.livebeat.shared.persistence.TimestampedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * [order] Order JPA 實體
 *
 * 負責：對應 "order".orders 資料表（order 為保留字，schema 以反引號交由 Hibernate 加引號）；
 *       繼承 TimestampedEntity（created_at/updated_at）。明細以扁平方式由 Adapter 另存。
 */
@Entity
@Table(name = "orders", schema = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderJpaEntity extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "session_id", nullable = false, columnDefinition = "uuid")
    private UUID sessionId;

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "expires_at")
    private Instant expiresAt;

    Order toDomain(List<OrderItem> items) {
        return Order.builder()
                .id(id).userId(userId).sessionId(sessionId).orderNo(orderNo)
                .status(status).totalAmount(totalAmount).currency(currency)
                .idempotencyKey(idempotencyKey).expiresAt(expiresAt)
                .items(items)
                .createdAt(getCreatedAt()).updatedAt(getUpdatedAt())
                .build();
    }

    static OrderJpaEntity fromDomain(Order order) {
        return OrderJpaEntity.builder()
                .id(order.getId()).userId(order.getUserId()).sessionId(order.getSessionId())
                .orderNo(order.getOrderNo()).status(order.getStatus())
                .totalAmount(order.getTotalAmount()).currency(order.getCurrency())
                .idempotencyKey(order.getIdempotencyKey()).expiresAt(order.getExpiresAt())
                .build();
    }
}
