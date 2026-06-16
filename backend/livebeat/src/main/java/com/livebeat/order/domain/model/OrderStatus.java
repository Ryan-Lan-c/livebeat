package com.livebeat.order.domain.model;

/**
 * [order] 訂單狀態
 *
 * 負責：v1 同步落地的訂單生命週期。PROCESSING / FAILED 屬 Phase 4 非同步化才需要（見 docs/10-order-design.md §4-3）。
 */
public enum OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
    REFUNDED
}
