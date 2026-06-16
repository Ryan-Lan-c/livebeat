package com.livebeat.order.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * [order] 訂單領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝訂單屬性與明細；totalAmount 由明細小計加總；create() 以 PENDING 起始
 */
@Getter
@Builder
@With
public class Order {
    private final UUID id;
    private final UUID userId;
    private final UUID sessionId;
    private final String orderNo;
    private final OrderStatus status;
    private final int totalAmount;
    private final String currency;
    private final String idempotencyKey;
    private final Instant expiresAt;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private final Instant updatedAt;

    public static Order create(UUID userId, UUID sessionId, String orderNo, String currency,
                               String idempotencyKey, Instant expiresAt, List<OrderItem> items) {
        int total = items.stream().mapToInt(OrderItem::subtotal).sum();
        return Order.builder()
                .userId(userId).sessionId(sessionId).orderNo(orderNo)
                .status(OrderStatus.PENDING)
                .totalAmount(total).currency(currency)
                .idempotencyKey(idempotencyKey).expiresAt(expiresAt)
                .items(List.copyOf(items))
                .build();
    }
}
