package com.livebeat.concert.infrastructure.persistence;

import com.livebeat.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [concert] 對真實 PostgreSQL 的持久層整合測試
 *
 * 涵蓋：
 *   - pg_trgm similarity() 在真實 DB 可用（搜尋 native query 的前提）。
 *   - ticket_zones 的 sold + locked <= total CHECK 約束（防超賣最後防線）。
 * 註：併發扣減不超賣的端到端測試見 order 模組的 OrderConcurrencyIntegrationTest。
 */
class ConcertPersistenceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void pg_trgm_similarity_function_is_available() {
        Double similarity = jdbc.queryForObject("SELECT similarity('jay', 'jay chou')", Double.class);
        assertThat(similarity).isNotNull().isGreaterThan(0.0);
    }

    @Test
    void check_constraint_rejects_overselling() {
        UUID zoneId = seedZoneWithTotal(100);
        // sold(60) + locked(60) = 120 > total(100) → 違反 chk_zones_sold_locked_within_total
        assertThatThrownBy(() -> jdbc.update(
                "UPDATE concert.ticket_zones SET sold_seats = 60, locked_seats = 60 WHERE id = ?", zoneId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void check_constraint_allows_within_total() {
        UUID zoneId = seedZoneWithTotal(100);
        int rows = jdbc.update(
                "UPDATE concert.ticket_zones SET sold_seats = 60, locked_seats = 40 WHERE id = ?", zoneId);
        assertThat(rows).isEqualTo(1);
    }

    /** 建立 user → concert → session → zone 的最小 FK 鏈，回傳 zone id。 */
    private UUID seedZoneWithTotal(int totalSeats) {
        UUID userId = UUID.randomUUID();
        jdbc.update("INSERT INTO auth.users (id, email, username, role) VALUES (?, ?, ?, 'ORGANIZER')",
                userId, userId + "@test.com", "user-" + userId);

        UUID concertId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO concert.concerts (id, title, artist, venue, city, country, category, status, organizer_id)
                VALUES (?, 'T', 'A', 'V', 'Taipei', 'TW', 'POP', 'DRAFT', ?)""", concertId, userId);

        UUID sessionId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO concert.concert_sessions (id, concert_id, session_name, event_date)
                VALUES (?, ?, 'Day 1', NOW() + INTERVAL '30 days')""", sessionId, concertId);

        UUID zoneId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO concert.ticket_zones
                    (id, session_id, zone_code, zone_name, price, total_seats, sold_seats, locked_seats)
                VALUES (?, ?, 'A', 'A Zone', 1000, ?, 0, 0)""", zoneId, sessionId, totalSeats);
        return zoneId;
    }
}
