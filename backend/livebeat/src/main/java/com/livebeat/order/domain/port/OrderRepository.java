package com.livebeat.order.domain.port;

import com.livebeat.order.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

/**
 * [order] 訂單資料存取介面（Port）
 *
 * 負責：定義訂單與明細的存取契約，由 infrastructure 層實作
 */
public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
