package com.livebeat.order;

import com.livebeat.order.api.dto.CreateOrderRequest;
import com.livebeat.order.application.service.OrderService;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import com.livebeat.support.RedisPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [order] 併發不超賣端到端測試（真實 Redis + PostgreSQL）。
 *
 * 對 total=100 的票區發動 500 筆併發下單，驗證 Redis Lua 原子扣減使成功訂單恰為 100、
 * 其餘為售罄、Redis 剩餘歸零、PG 落地訂單數恰為 100（零超賣）。對應 docs/10-order-design.md §6-3。
 */
class OrderConcurrencyIntegrationTest extends RedisPostgresIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired InventoryPort inventory;
    @Autowired JdbcTemplate jdbc;

    @Test
    void concurrent_orders_never_oversell() throws InterruptedException {
        int totalSeats = 100;
        int attempts = 500;
        Seed seed = seed(totalSeats);
        inventory.warmUp(seed.zoneId(), totalSeats);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(attempts);

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < attempts; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        CreateOrderRequest req = new CreateOrderRequest(
                                seed.sessionId(), seed.zoneId(), 1, UUID.randomUUID().toString());
                        orderService.createOrder(req, seed.userId());
                        success.incrementAndGet();
                    } catch (ApiException e) {
                        if (e.getErrorCode() == ErrorCode.SEATS_SOLD_OUT) {
                            soldOut.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertThat(done.await(120, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(success.get()).isEqualTo(totalSeats);
        assertThat(soldOut.get()).isEqualTo(attempts - totalSeats);
        assertThat(inventory.remaining(seed.zoneId())).isZero();

        Integer persisted = jdbc.queryForObject(
                "SELECT COUNT(*) FROM \"order\".orders WHERE session_id = ?", Integer.class, seed.sessionId());
        assertThat(persisted).isEqualTo(totalSeats);
    }

    /** 建立 user → concert(ON_SALE) → session(ON_SALE) → zone 的最小資料，回傳所需 id。 */
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
