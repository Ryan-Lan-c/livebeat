-- pg_trgm: trigram-based GIN index for ILIKE keyword search on title/artist/venue
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE SCHEMA IF NOT EXISTS concert;

-- ============================================================
-- concert.concerts
-- ============================================================
CREATE TABLE concert.concerts (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(255) NOT NULL,
    artist        VARCHAR(255) NOT NULL,
    description   TEXT,
    venue         VARCHAR(255) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    country       VARCHAR(100) NOT NULL DEFAULT 'TW',
    category      VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    image_url     VARCHAR(500),
    organizer_id  UUID         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by    UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    updated_by    UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    CONSTRAINT fk_concerts_organizer FOREIGN KEY (organizer_id) REFERENCES auth.users(id)
);

COMMENT ON TABLE  concert.concerts               IS 'Concert master records | 演唱會主檔';
COMMENT ON COLUMN concert.concerts.id            IS 'Primary key | 主鍵';
COMMENT ON COLUMN concert.concerts.title         IS 'Concert title | 演唱會名稱';
COMMENT ON COLUMN concert.concerts.artist        IS 'Performing artist or band | 表演藝人或樂團';
COMMENT ON COLUMN concert.concerts.description   IS 'Full description shown on detail page | 詳情頁完整描述';
COMMENT ON COLUMN concert.concerts.venue         IS 'Venue name | 場館名稱';
COMMENT ON COLUMN concert.concerts.city          IS 'City where the venue is located | 場館所在城市';
COMMENT ON COLUMN concert.concerts.country       IS 'ISO 3166-1 alpha-2 country code | 國家代碼';
COMMENT ON COLUMN concert.concerts.category      IS 'POP | ROCK | HIP_HOP | ELECTRONIC | CLASSICAL | JAZZ | OTHER';
COMMENT ON COLUMN concert.concerts.status        IS 'DRAFT | PUBLISHED | ON_SALE | CANCELLED | ENDED';
COMMENT ON COLUMN concert.concerts.image_url     IS 'Cover image URL stored in MinIO/S3 | 封面圖 URL';
COMMENT ON COLUMN concert.concerts.organizer_id  IS 'UUID of the ADMIN or ORGANIZER who owns this concert | 負責此演唱會的使用者 UUID';
COMMENT ON COLUMN concert.concerts.created_at    IS 'Creation timestamp (UTC) | 建立時間';
COMMENT ON COLUMN concert.concerts.updated_at    IS 'Last update timestamp (UTC) | 最後更新時間';
COMMENT ON COLUMN concert.concerts.created_by    IS 'UUID of creator; 00000000-... for system | 建立者';
COMMENT ON COLUMN concert.concerts.updated_by    IS 'UUID of last updater | 最後更新者';

-- GIN indexes for pg_trgm ILIKE keyword search
CREATE INDEX idx_concerts_title_trgm  ON concert.concerts USING GIN (title  gin_trgm_ops);
CREATE INDEX idx_concerts_artist_trgm ON concert.concerts USING GIN (artist gin_trgm_ops);
CREATE INDEX idx_concerts_venue_trgm  ON concert.concerts USING GIN (venue  gin_trgm_ops);

-- Filtering indexes
CREATE INDEX idx_concerts_status   ON concert.concerts(status);
CREATE INDEX idx_concerts_category ON concert.concerts(category);
CREATE INDEX idx_concerts_city     ON concert.concerts(city);

-- ============================================================
-- concert.concert_sessions
-- ============================================================
CREATE TABLE concert.concert_sessions (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    concert_id            UUID         NOT NULL,
    session_name          VARCHAR(100) NOT NULL,
    event_date            TIMESTAMPTZ  NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    has_assigned_seats    BOOLEAN      NOT NULL DEFAULT FALSE,
    max_tickets_per_order INTEGER      NOT NULL DEFAULT 4,
    sale_start_at         TIMESTAMPTZ,
    sale_end_at           TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_sessions_concert FOREIGN KEY (concert_id)
        REFERENCES concert.concerts(id) ON DELETE CASCADE
);

COMMENT ON TABLE  concert.concert_sessions                      IS 'Individual sessions of a concert | 演唱會場次';
COMMENT ON COLUMN concert.concert_sessions.id                   IS 'Primary key | 主鍵';
COMMENT ON COLUMN concert.concert_sessions.concert_id           IS 'Parent concert | 所屬演唱會';
COMMENT ON COLUMN concert.concert_sessions.session_name         IS 'Display name for this session (e.g. Day 1) | 場次名稱';
COMMENT ON COLUMN concert.concert_sessions.event_date           IS 'Scheduled event start time | 活動開始時間';
COMMENT ON COLUMN concert.concert_sessions.status               IS 'DRAFT | ON_SALE | SOLD_OUT | CANCELLED | ENDED';
COMMENT ON COLUMN concert.concert_sessions.has_assigned_seats   IS 'True = assigned seating mode; False = zone-only mode | 是否對號入座';
COMMENT ON COLUMN concert.concert_sessions.max_tickets_per_order IS 'Max tickets per order to prevent scalping | 每筆訂單上限張數';
COMMENT ON COLUMN concert.concert_sessions.sale_start_at        IS 'Ticket sale start time; shown as countdown on frontend | 開賣時間';
COMMENT ON COLUMN concert.concert_sessions.sale_end_at          IS 'Ticket sale end time | 售票截止時間';
COMMENT ON COLUMN concert.concert_sessions.created_at           IS 'Creation timestamp (UTC) | 建立時間';
COMMENT ON COLUMN concert.concert_sessions.updated_at           IS 'Last update timestamp (UTC) | 最後更新時間';

CREATE INDEX idx_concert_sessions_concert_id ON concert.concert_sessions(concert_id);
CREATE INDEX idx_concert_sessions_status     ON concert.concert_sessions(status);

-- ============================================================
-- concert.ticket_zones
-- ============================================================
CREATE TABLE concert.ticket_zones (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id   UUID        NOT NULL,
    zone_code    VARCHAR(20) NOT NULL,
    zone_name    VARCHAR(100) NOT NULL,
    price        INTEGER     NOT NULL,
    total_seats  INTEGER     NOT NULL,
    sold_seats   INTEGER     NOT NULL DEFAULT 0,
    locked_seats INTEGER     NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_zones_session FOREIGN KEY (session_id)
        REFERENCES concert.concert_sessions(id) ON DELETE CASCADE,
    CONSTRAINT uq_zones_session_code UNIQUE (session_id, zone_code),
    CONSTRAINT chk_zones_price_positive    CHECK (price >= 0),
    CONSTRAINT chk_zones_total_positive    CHECK (total_seats > 0),
    CONSTRAINT chk_zones_sold_nonnegative  CHECK (sold_seats >= 0),
    CONSTRAINT chk_zones_locked_nonnegative CHECK (locked_seats >= 0)
);

COMMENT ON TABLE  concert.ticket_zones               IS 'Ticket zones (pricing tiers) within a session | 場次票區';
COMMENT ON COLUMN concert.ticket_zones.id            IS 'Primary key | 主鍵';
COMMENT ON COLUMN concert.ticket_zones.session_id    IS 'Parent session | 所屬場次';
COMMENT ON COLUMN concert.ticket_zones.zone_code     IS 'Short code used in seat map (VIP / A / B / C) | 票區代碼';
COMMENT ON COLUMN concert.ticket_zones.zone_name     IS 'Display name for the zone | 票區名稱';
COMMENT ON COLUMN concert.ticket_zones.price         IS 'Ticket price in smallest currency unit (TWD cents or TWD) | 票價';
COMMENT ON COLUMN concert.ticket_zones.total_seats   IS 'Total capacity for this zone | 總座位數';
COMMENT ON COLUMN concert.ticket_zones.sold_seats    IS 'Confirmed sold count (paid orders) | 已售出票數';
COMMENT ON COLUMN concert.ticket_zones.locked_seats  IS 'Redis-locked seats pending payment; released on order expiry | 訂單鎖定中的票數';
COMMENT ON COLUMN concert.ticket_zones.created_at    IS 'Creation timestamp (UTC) | 建立時間';
COMMENT ON COLUMN concert.ticket_zones.updated_at    IS 'Last update timestamp (UTC) | 最後更新時間';

CREATE INDEX idx_ticket_zones_session_id ON concert.ticket_zones(session_id);
