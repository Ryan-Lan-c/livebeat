-- ============================================================
-- order.tickets：付款成功後出票（最薄垂直切片）
-- 設計見 docs/10-order-design.md §6。區域票 seat_id 為 NULL；對號入座為 Phase 2+。
-- ============================================================
CREATE TABLE "order".tickets (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id UUID         NOT NULL,
    seat_id       UUID,
    ticket_code   VARCHAR(64)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'VALID',
    used_at       TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tickets_order_item FOREIGN KEY (order_item_id)
        REFERENCES "order".order_items(id) ON DELETE CASCADE,
    CONSTRAINT uq_tickets_code UNIQUE (ticket_code)
);

COMMENT ON TABLE  "order".tickets               IS 'Issued tickets (one row per ticket) | 已出票券（每張一筆）';
COMMENT ON COLUMN "order".tickets.id            IS 'Primary key | 主鍵';
COMMENT ON COLUMN "order".tickets.order_item_id IS 'Parent order line item | 所屬訂單明細';
COMMENT ON COLUMN "order".tickets.seat_id       IS 'Assigned seat; NULL for zone-only tickets | 對號座位，區域票為 NULL';
COMMENT ON COLUMN "order".tickets.ticket_code   IS 'Unique code embedded in QR | QR Code 內的唯一識別碼';
COMMENT ON COLUMN "order".tickets.status        IS 'VALID | USED | CANCELLED';
COMMENT ON COLUMN "order".tickets.used_at       IS 'Check-in timestamp | 入場核銷時間';
COMMENT ON COLUMN "order".tickets.created_at    IS 'Creation timestamp (UTC) | 建立時間';

CREATE INDEX idx_tickets_order_item_id ON "order".tickets(order_item_id);
