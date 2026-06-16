package com.livebeat.order.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * [order] 建立訂單請求 DTO
 *
 * 負責：最薄垂直切片的單票區下單；quantity 上限由場次 max_tickets_per_order 於服務層強制
 */
public record CreateOrderRequest(
        @NotNull UUID sessionId,
        @NotNull UUID zoneId,
        @Min(1) int quantity,
        @Size(max = 80) String idempotencyKey) {
}
