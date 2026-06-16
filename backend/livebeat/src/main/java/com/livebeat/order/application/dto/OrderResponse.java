package com.livebeat.order.application.dto;

import com.livebeat.order.domain.model.Order;

import java.time.Instant;
import java.util.UUID;

/**
 * [order] 訂單回應 DTO
 */
public record OrderResponse(
        UUID orderId,
        String orderNo,
        UUID sessionId,
        String status,
        int totalAmount,
        String currency,
        Instant expiresAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getOrderNo(), order.getSessionId(),
                order.getStatus().name(), order.getTotalAmount(),
                order.getCurrency(), order.getExpiresAt());
    }
}
