package com.livebeat.order.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * [order] 訂單明細領域模型（純 Java，無框架依賴）
 *
 * 負責：單一票區的購買數量與單價快照；subtotal() 為小計
 */
@Getter
@Builder
public class OrderItem {
    private final UUID zoneId;
    private final int quantity;
    private final int unitPrice;

    public int subtotal() {
        return quantity * unitPrice;
    }

    public static OrderItem of(UUID zoneId, int quantity, int unitPrice) {
        return OrderItem.builder().zoneId(zoneId).quantity(quantity).unitPrice(unitPrice).build();
    }
}
