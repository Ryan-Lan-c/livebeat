package com.livebeat.concert;

import java.util.UUID;

/**
 * [concert] 場次開賣事件（跨模組，Spring ApplicationEvent）
 *
 * 負責：場次轉為 ON_SALE 時由 concert 發布，供 order 模組監聽以對該場次票區做 Redis 庫存 warm-up。
 *       以事件解耦，concert 不需依賴 order（見 docs/10-order-design.md §3-4）。
 */
public record SessionOnSaleEvent(UUID sessionId) {
}
