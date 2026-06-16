package com.livebeat.order.domain.port;

import java.util.UUID;

/**
 * [order] 票區即時庫存埠（Port）
 *
 * 負責：售票期間「可售與否」的權威操作。實作以 Redis 計數 + Lua 原子扣減保證防超賣
 *       （見 docs/10-order-design.md §3）。PG 僅為最終帳本與可重建來源。
 */
public interface InventoryPort {

    /** 原子扣減結果。 */
    enum Reservation {
        /** 扣減成功，已鎖定。 */
        RESERVED,
        /** 可售不足，售罄（→ 409）。 */
        SOLD_OUT,
        /** 庫存未就緒（未開賣 / 復原中，→ 503）。 */
        NOT_READY
    }

    /** 原子嘗試扣減 quantity 張；不足回 SOLD_OUT、未就緒回 NOT_READY。 */
    Reservation tryReserve(UUID zoneId, int quantity);

    /** 回補 quantity 張（訂單取消 / 過期 / 同步落地失敗補償）。 */
    void release(UUID zoneId, int quantity);

    /** 票區開賣前播種可售數並標記就緒（warm-up）。 */
    void warmUp(UUID zoneId, int remaining);

    /** 目前可售剩餘（ops/測試用），未就緒回 null。 */
    Long remaining(UUID zoneId);
}
