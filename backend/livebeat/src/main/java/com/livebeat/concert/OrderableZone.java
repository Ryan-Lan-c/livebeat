package com.livebeat.concert;

import java.util.UUID;

/**
 * [concert] 可下單票區的跨模組查詢結果（公開命名介面）
 *
 * 負責：提供 order 模組下單所需的最小票區資訊，避免外洩 concert 內部模型
 */
public record OrderableZone(
        UUID zoneId,
        UUID sessionId,
        int price,
        boolean saleOpen,
        int maxTicketsPerOrder) {
}
