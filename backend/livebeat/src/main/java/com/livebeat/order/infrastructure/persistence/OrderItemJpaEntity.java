package com.livebeat.order.infrastructure.persistence;

import com.livebeat.order.domain.model.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * [order] OrderItem JPA 實體
 *
 * 負責：對應 "order".order_items 資料表；以 order_id（UUID）扁平關聯所屬訂單，不用 JPA 關係映射
 */
@Entity
@Table(name = "order_items", schema = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "uuid")
    private UUID orderId;

    @Column(name = "zone_id", nullable = false, columnDefinition = "uuid")
    private UUID zoneId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int subtotal;

    OrderItem toDomain() {
        return OrderItem.builder()
                .id(id).zoneId(zoneId).quantity(quantity).unitPrice(unitPrice)
                .build();
    }

    static OrderItemJpaEntity fromDomain(OrderItem item, UUID orderId) {
        return OrderItemJpaEntity.builder()
                .orderId(orderId).zoneId(item.getZoneId())
                .quantity(item.getQuantity()).unitPrice(item.getUnitPrice())
                .subtotal(item.subtotal())
                .build();
    }
}
