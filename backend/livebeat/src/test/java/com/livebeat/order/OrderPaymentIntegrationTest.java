package com.livebeat.order;

import com.livebeat.order.api.dto.CreateOrderRequest;
import com.livebeat.order.application.dto.OrderResponse;
import com.livebeat.order.application.service.OrderService;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import com.livebeat.support.RedisPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [order] 付款 + 出票端到端測試（真實 Redis + PostgreSQL）。
 *
 * 對應 docs/10-order-design.md §6：付款成功 → 訂單 PAID、票區 sold_seats++、出票，
 * 且 Redis 可售剩餘不變（扣減保留、由鎖定轉為售出，維持不變式）。
 */
class OrderPaymentIntegrationTest extends RedisPostgresIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired InventoryPort inventory;
    @Autowired JdbcTemplate jdbc;

    @Test
    void pay_marks_paid_increments_sold_and_issues_tickets() {
        Seed s = seed(100);
        inventory.warmUp(s.zoneId(), 100);
        OrderResponse order = orderService.createOrder(
                new CreateOrderRequest(s.sessionId(), s.zoneId(), 2, UUID.randomUUID().toString()), s.userId());
        assertThat(inventory.remaining(s.zoneId())).isEqualTo(98L);

        OrderResponse paid = orderService.payOrder(order.orderId(), s.userId());

        assertThat(paid.status()).isEqualTo("PAID");
        assertThat(jdbc.queryForObject(
                "SELECT status FROM \"order\".orders WHERE id = ?", String.class, order.orderId()))
                .isEqualTo("PAID");
        assertThat(jdbc.queryForObject(
                "SELECT sold_seats FROM concert.ticket_zones WHERE id = ?", Integer.class, s.zoneId()))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM "order".tickets t
                JOIN "order".order_items i ON t.order_item_id = i.id
                WHERE i.order_id = ?""", Integer.class, order.orderId()))
                .isEqualTo(2);
        // Redis 可售不變（保留扣減，鎖定轉售出）
        assertThat(inventory.remaining(s.zoneId())).isEqualTo(98L);
    }

    @Test
    void cannot_pay_an_already_paid_order() {
        Seed s = seed(100);
        inventory.warmUp(s.zoneId(), 100);
        OrderResponse order = orderService.createOrder(
                new CreateOrderRequest(s.sessionId(), s.zoneId(), 1, UUID.randomUUID().toString()), s.userId());
        orderService.payOrder(order.orderId(), s.userId());

        assertThatThrownBy(() -> orderService.payOrder(order.orderId(), s.userId()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_PAYABLE);
    }

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
