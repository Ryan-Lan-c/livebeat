package com.livebeat.order.application.service;

import com.livebeat.concert.SessionOnSaleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * [order] 場次開賣事件監聽器
 *
 * 負責：concert 場次轉 ON_SALE 並提交後，對該場次票區做 Redis 庫存 warm-up。
 *       AFTER_COMMIT 確保只在 ON_SALE 狀態確實落地後才播種。
 */
@Component
@RequiredArgsConstructor
class InventoryWarmUpListener {

    private final InventoryConsistencyService inventoryConsistency;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionOnSale(SessionOnSaleEvent event) {
        inventoryConsistency.warmUpSession(event.sessionId());
    }
}
