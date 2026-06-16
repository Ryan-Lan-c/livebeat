package com.livebeat.order.domain.port;

import com.livebeat.order.domain.model.Order;
import com.livebeat.order.domain.model.OrderStatus;

import java.time.Instant;
import java.util.List;
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

    /** 已過期且仍 PENDING 的訂單（供過期回收）。 */
    List<Order> findExpiredPending(Instant now);

    /** 更新訂單狀態（不動明細）。 */
    void updateStatus(UUID orderId, OrderStatus status);

    /** 某票區進行中（PENDING 未過期）訂單的鎖定張數加總（供對帳 / 復原）。 */
    int sumActivePendingQuantity(UUID zoneId, Instant now);
}
