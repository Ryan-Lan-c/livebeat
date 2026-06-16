-- ============================================================
-- order schema：訂單與訂單明細（最薄垂直切片：同步下單）
-- 設計見 docs/10-order-design.md：庫存即時可售權威在 Redis，PG 為訂單最終帳本。
-- 註：order 為 SQL 保留字，schema 名一律以雙引號標識。
-- ============================================================
CREATE SCHEMA IF NOT EXISTS "order";

CREATE TABLE "order".orders (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL,
    session_id      UUID         NOT NULL,
    order_no        VARCHAR(40)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_amount    INTEGER      NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'TWD',
    idempotency_key VARCHAR(80),
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_orders_user    FOREIGN KEY (user_id)    REFERENCES auth.users(id),
    CONSTRAINT fk_orders_session FOREIGN KEY (session_id) REFERENCES concert.concert_sessions(id),
    CONSTRAINT uq_orders_order_no        UNIQUE (order_no),
    CONSTRAINT uq_orders_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_orders_total_nonnegative CHECK (total_amount >= 0)
);

COMMENT ON TABLE  "order".orders                 IS 'Customer orders | 顧客訂單';
COMMENT ON COLUMN "order".orders.id              IS 'Primary key | 主鍵';
COMMENT ON COLUMN "order".orders.user_id         IS 'Buyer user UUID | 購票者 UUID';
COMMENT ON COLUMN "order".orders.session_id      IS 'Concert session being ordered | 下單的場次';
COMMENT ON COLUMN "order".orders.order_no        IS 'Human-readable unique order number | 對外顯示的訂單號';
COMMENT ON COLUMN "order".orders.status          IS 'PENDING | PAID | CANCELLED | REFUNDED';
COMMENT ON COLUMN "order".orders.total_amount    IS 'Total amount in smallest currency unit | 訂單總金額';
COMMENT ON COLUMN "order".orders.currency        IS 'ISO 4217 currency code | 幣別';
COMMENT ON COLUMN "order".orders.idempotency_key IS 'Client-supplied key to dedupe duplicate submits | 防重複下單的冪等鍵';
COMMENT ON COLUMN "order".orders.expires_at      IS 'Order lock expiry (10 min); unpaid orders auto-cancelled after | 訂單鎖定到期，逾時自動取消';
COMMENT ON COLUMN "order".orders.created_at      IS 'Creation timestamp (UTC) | 建立時間';
COMMENT ON COLUMN "order".orders.updated_at      IS 'Last update timestamp (UTC) | 最後更新時間';

CREATE TABLE "order".order_items (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID    NOT NULL,
    zone_id    UUID    NOT NULL,
    quantity   INTEGER NOT NULL,
    unit_price INTEGER NOT NULL,
    subtotal   INTEGER NOT NULL,
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES "order".orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_zone  FOREIGN KEY (zone_id)  REFERENCES concert.ticket_zones(id),
    CONSTRAINT chk_items_quantity_positive CHECK (quantity > 0)
);

COMMENT ON TABLE  "order".order_items            IS 'Line items of an order (one per ticket zone) | 訂單明細（每票區一筆）';
COMMENT ON COLUMN "order".order_items.id         IS 'Primary key | 主鍵';
COMMENT ON COLUMN "order".order_items.order_id   IS 'Parent order | 所屬訂單';
COMMENT ON COLUMN "order".order_items.zone_id    IS 'Ticket zone purchased | 購買的票區';
COMMENT ON COLUMN "order".order_items.quantity   IS 'Number of tickets | 票數';
COMMENT ON COLUMN "order".order_items.unit_price IS 'Unit price snapshot at order time | 下單當下的單價快照';
COMMENT ON COLUMN "order".order_items.subtotal   IS 'quantity * unit_price | 小計';

CREATE INDEX idx_orders_user_id      ON "order".orders(user_id);
CREATE INDEX idx_orders_session_id   ON "order".orders(session_id);
CREATE INDEX idx_order_items_order_id ON "order".order_items(order_id);
