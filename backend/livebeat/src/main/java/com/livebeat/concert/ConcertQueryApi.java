package com.livebeat.concert;

import java.util.Optional;
import java.util.UUID;

/**
 * [concert] 跨模組查詢 API（公開命名介面）
 *
 * 負責：供 order 等其他模組以 Modulith 允許的方式查詢可下單的票區資訊，
 *       不暴露 concert 內部的 domain / repository。實作於 application 層（package-private）。
 */
public interface ConcertQueryApi {

    /** 查詢可下單票區；票區不存在或不屬於該場次時回 empty。 */
    Optional<OrderableZone> findOrderableZone(UUID sessionId, UUID zoneId);
}
