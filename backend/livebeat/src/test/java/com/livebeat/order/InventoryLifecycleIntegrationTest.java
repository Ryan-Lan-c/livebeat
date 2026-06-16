package com.livebeat.order;

import com.livebeat.order.api.dto.CreateOrderRequest;
import com.livebeat.order.application.dto.OrderResponse;
import com.livebeat.order.application.service.InventoryConsistencyService;
import com.livebeat.order.application.service.OrderExpiryService;
import com.livebeat.order.application.service.OrderService;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.support.RedisPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [order] 庫存生命週期整合測試（真實 Redis + PostgreSQL）。
 *
 * 涵蓋 docs/10-order-design.md 的：warm-up（§3-4）、過期回補（§4-1）、
 * Redis 故障復原（§3-5）、對帳校正（§3-6）。
 */
class InventoryLifecycleIntegrationTest extends RedisPostgresIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired OrderExpiryService orderExpiryService;
    @Autowired InventoryConsistencyService inventoryConsistency;
    @Autowired InventoryPort inventory;
    @Autowired JdbcTemplate jdbc;
    @Autowired StringRedisTemplate redis;

    @Test
    void warmUpSession_seeds_redis_from_pg() {
        Seed s = seed(100);

        inventoryConsistency.warmUpSession(s.sessionId());

        assertThat(inventory.remaining(s.zoneId())).isEqualTo(100L);
    }

    @Test
    void expiry_cancels_order_and_releases_inventory() {
        Seed s = seed(100);
        inventory.warmUp(s.zoneId(), 100);
        OrderResponse order = orderService.createOrder(
                new CreateOrderRequest(s.sessionId(), s.zoneId(), 3, UUID.randomUUID().toString()), s.userId());
        assertThat(inventory.remaining(s.zoneId())).isEqualTo(97L);

        // 將訂單改為已過期後執行回收
        jdbc.update("UPDATE \"order\".orders SET expires_at = ? WHERE id = ?",
                Timestamp.from(Instant.now().minusSeconds(60)), order.orderId());
        int expired = orderExpiryService.expireOverdueOrders(Instant.now());

        assertThat(expired).isEqualTo(1);
        assertThat(inventory.remaining(s.zoneId())).isEqualTo(100L);
        String status = jdbc.queryForObject(
                "SELECT status FROM \"order\".orders WHERE id = ?", String.class, order.orderId());
        assertThat(status).isEqualTo("CANCELLED");
    }

    @Test
    void recovery_rebuilds_remaining_from_pg_after_redis_loss() {
        Seed s = seed(100);
        inventory.warmUp(s.zoneId(), 100);
        // 3 筆 PENDING 訂單，各 1 張 → Redis 97、PG 有 3 張進行中鎖定
        for (int i = 0; i < 3; i++) {
            orderService.createOrder(
                    new CreateOrderRequest(s.sessionId(), s.zoneId(), 1, UUID.randomUUID().toString()), s.userId());
        }
        assertThat(inventory.remaining(s.zoneId())).isEqualTo(97L);

        // 模擬 Redis 遺失
        redis.delete("zone:remaining:" + s.zoneId());
        redis.delete("zone:ready:" + s.zoneId());
        // 復原前：庫存未就緒，下單應被擋（503 / NOT_READY），不會超賣
        assertThat(inventory.tryReserve(s.zoneId(), 1)).isEqualTo(InventoryPort.Reservation.NOT_READY);

        // 復原：由 PG 重建（100 - 3 進行中 = 97）
        int corrected = inventoryConsistency.reconcileAll(Instant.now());

        assertThat(corrected).isGreaterThanOrEqualTo(1);
        assertThat(inventory.remaining(s.zoneId())).isEqualTo(97L);
        assertThat(inventory.tryReserve(s.zoneId(), 1)).isEqualTo(InventoryPort.Reservation.RESERVED);
    }

    @Test
    void reconcile_corrects_drift() {
        Seed s = seed(100);
        inventory.warmUp(s.zoneId(), 100);
        // 人為製造偏移（無進行中訂單 → 期望 100）
        redis.opsForValue().set("zone:remaining:" + s.zoneId(), "50");

        int corrected = inventoryConsistency.reconcileAll(Instant.now());

        assertThat(corrected).isGreaterThanOrEqualTo(1);
        assertThat(inventory.remaining(s.zoneId())).isEqualTo(100L);
    }

    /** 建立 user → concert(ON_SALE) → session(ON_SALE) → zone，回傳所需 id。 */
    private Seed seed(int totalSeats) {
        UUID userId = UUID.randomUUID();
        jdbc.update("INSERT INTO auth.users (id, email, username, role) VALUES (?, ?, ?, 'USER')",
                userId, userId + "@test.com", "user-" + userId);

        UUID concertId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO concert.concerts (id, title, artist, venue, city, country, category, status, organizer_id)
                VALUES (?, 'T', 'A', 'V', 'Taipei', 'TW', 'POP', 'ON_SALE', ?)""", concertId, userId);

        UUID sessionId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO concert.concert_sessions
                    (id, concert_id, session_name, event_date, status, max_tickets_per_order)
                VALUES (?, ?, 'Day 1', NOW() + INTERVAL '30 days', 'ON_SALE', 4)""", sessionId, concertId);

        UUID zoneId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO concert.ticket_zones
                    (id, session_id, zone_code, zone_name, price, total_seats, sold_seats, locked_seats)
                VALUES (?, ?, 'A', 'A Zone', 1000, ?, 0, 0)""", zoneId, sessionId, totalSeats);

        return new Seed(userId, sessionId, zoneId);
    }

    private record Seed(UUID userId, UUID sessionId, UUID zoneId) {}
}
