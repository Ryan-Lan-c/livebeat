package com.livebeat.order.application.service;

import com.livebeat.concert.ConcertQueryApi;
import com.livebeat.concert.ZoneInventorySnapshot;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.order.domain.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * [order] 庫存一致性服務：warm-up、對帳與冷啟動 / 故障復原
 *
 * 負責：開賣時播種 Redis 庫存；定期 / 復原時以 PG 為準重算可售剩餘（total - sold - 進行中鎖定），
 *       與 Redis 比對：遺失則重建、偏移則校正。見 docs/10-order-design.md §3-4~3-6。
 */
@Service
@RequiredArgsConstructor
public class InventoryConsistencyService {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsistencyService.class);

    private final ConcertQueryApi concertQuery;
    private final OrderRepository orderRepository;
    private final InventoryPort inventory;

    /** 開賣 warm-up：以 total - sold 播種該場次每個票區。 */
    @Transactional(readOnly = true)
    public void warmUpSession(UUID sessionId) {
        for (ZoneInventorySnapshot zone : concertQuery.zonesForSession(sessionId)) {
            inventory.warmUp(zone.zoneId(), zone.atRestRemaining());
            log.info("Warmed up zone {} with {} sellable seats", zone.zoneId(), zone.atRestRemaining());
        }
    }

    /**
     * 對帳 / 復原：對所有 ON_SALE 票區，以 PG 推導期望可售並與 Redis 比對。
     * Redis 遺失（null）→ 重建；數值不符 → 校正。回傳被修正的票區數。
     */
    @Transactional(readOnly = true)
    public int reconcileAll(Instant now) {
        int corrected = 0;
        for (ZoneInventorySnapshot zone : concertQuery.onSaleZones()) {
            int expected = zone.atRestRemaining()
                    - orderRepository.sumActivePendingQuantity(zone.zoneId(), now);
            Long actual = inventory.remaining(zone.zoneId());
            if (actual == null) {
                inventory.warmUp(zone.zoneId(), expected);
                log.warn("Inventory for zone {} was missing; rebuilt remaining={}", zone.zoneId(), expected);
                corrected++;
            } else if (actual != expected) {
                inventory.warmUp(zone.zoneId(), expected);
                log.warn("Inventory drift for zone {}: redis={} expected={}; corrected",
                        zone.zoneId(), actual, expected);
                corrected++;
            }
        }
        return corrected;
    }
}
