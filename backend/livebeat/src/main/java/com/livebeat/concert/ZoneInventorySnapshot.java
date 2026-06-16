package com.livebeat.concert;

import java.util.UUID;

/**
 * [concert] 票區庫存快照（跨模組查詢結果）
 *
 * 負責：提供 order 模組做 warm-up / 對帳 / 復原所需的票區容量資訊（總座位、已售）。
 *       atRestRemaining() 為無進行中鎖定時的可售數（total - sold）。
 */
public record ZoneInventorySnapshot(UUID zoneId, UUID sessionId, int totalSeats, int soldSeats) {

    public int atRestRemaining() {
        return totalSeats - soldSeats;
    }
}
