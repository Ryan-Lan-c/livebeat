package com.livebeat.order.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * [order] Redis↔PG 庫存對帳排程
 *
 * 負責：每 10 分鐘對所有 ON_SALE 票區比對並校正 Redis 庫存（含遺失重建），見 docs/10-order-design.md §3-6。
 * 備註：採單實例 @Scheduled；多實例部署時應改用分散式排程避免重複執行。
 */
@Component
@RequiredArgsConstructor
class InventoryReconcileJob {

    private final InventoryConsistencyService inventoryConsistency;

    @Scheduled(cron = "0 */10 * * * *")
    public void run() {
        inventoryConsistency.reconcileAll(Instant.now());
    }
}
