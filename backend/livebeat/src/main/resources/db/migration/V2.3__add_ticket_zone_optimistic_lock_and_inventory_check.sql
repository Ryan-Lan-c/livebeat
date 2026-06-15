-- ============================================================
-- concert.ticket_zones：樂觀鎖 + 庫存上界約束（防超賣地基）
-- 對應 P1-03。實際扣減流程（order 模組）尚未實作，此處先放結構性防線：
--   1) version 欄位：JPA @Version 樂觀鎖，攔截 read-modify-write 的 lost-update。
--   2) CHECK：sold + locked 不得超過 total，作為資料庫層最後防線。
-- ============================================================

ALTER TABLE concert.ticket_zones
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN concert.ticket_zones.version
    IS 'Optimistic lock version (JPA @Version); prevents lost-update overselling | 樂觀鎖版本，防止並發扣減超賣';

ALTER TABLE concert.ticket_zones
    ADD CONSTRAINT chk_zones_sold_locked_within_total
        CHECK (sold_seats + locked_seats <= total_seats);
