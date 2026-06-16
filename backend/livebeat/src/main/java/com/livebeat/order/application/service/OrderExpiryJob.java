package com.livebeat.order.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * [order] 過期訂單回收排程
 *
 * 負責：每 5 分鐘掃描逾時未付款訂單，取消並回補 Redis 庫存（見 docs/08-batch-jobs.md）。
 * 備註：採單實例 @Scheduled；多實例部署時應改用分散式排程（如 Quartz 叢集）避免重複執行。
 */
@Component
@RequiredArgsConstructor
class OrderExpiryJob {

    private final OrderExpiryService orderExpiryService;

    @Scheduled(cron = "0 */5 * * * *")
    public void run() {
        orderExpiryService.expireOverdueOrders(Instant.now());
    }
}
