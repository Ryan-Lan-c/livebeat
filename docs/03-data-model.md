# 03 — 資料模型

> [← 返回總覽](../PROJECT_PLAN.md)

---

## ERD 圖

```mermaid
erDiagram
    USER {
        uuid id PK
        string email UK
        string password_hash
        string full_name
        string phone
        string role "USER / STAFF / ADMIN"
        string oauth_provider "google / apple / null"
        string oauth_id
        timestamp created_at
        timestamp updated_at
    }

    LINE_BINDING {
        uuid id PK
        uuid user_id FK
        string line_user_id UK
        string line_display_name
        timestamp bound_at
    }

    CONCERT {
        uuid id PK
        string title
        string artist
        text description
        string venue
        string city
        string country
        string category "流行 / 搖滾 / 嘻哈 / 電子 / 古典 / 爵士 / 其他"
        string status "DRAFT / ON_SALE / CANCELLED / ENDED"
        string image_url
        timestamp created_at
        timestamp updated_at
    }

    CONCERT_SESSION {
        uuid id PK
        uuid concert_id FK
        string session_name "Day1 / 第一場 / ..."
        timestamp event_date
        string status "DRAFT / ON_SALE / SOLD_OUT / CANCELLED / ENDED"
        boolean has_assigned_seats "true = 對號入座模式"
        integer max_tickets_per_order "每筆訂單上限張數"
        timestamp sale_start_at
        timestamp sale_end_at
        timestamp created_at
    }

    TICKET_ZONE {
        uuid id PK
        uuid session_id FK
        string zone_code "VIP / A / B / C / ..."
        string zone_name
        integer price
        integer total_seats
        integer sold_seats
        integer locked_seats "Redis 鎖定中，尚未付款"
        timestamp created_at
    }

    SEAT {
        uuid id PK
        uuid zone_id FK
        string row_label "A / B / C / ..."
        string seat_number "1 / 2 / 3 / ..."
        string status "AVAILABLE / LOCKED / SOLD"
        timestamp locked_until "鎖定到期時間"
        timestamp created_at
    }

    SEAT_MAP {
        uuid id PK
        uuid concert_id FK
        string image_url "上傳的 SVG 或圖片 URL"
        integer canvas_width
        integer canvas_height
        timestamp created_at
    }

    ZONE_HOTSPOT {
        uuid id PK
        uuid seat_map_id FK
        uuid zone_id FK
        json coordinates "多邊形座標陣列 [{x,y},...]"
        timestamp created_at
    }

    ORDER {
        uuid id PK
        uuid user_id FK
        uuid session_id FK
        string order_no UK "系統產生的訂單編號"
        string status "PENDING / PAID / CANCELLED / REFUNDED"
        integer total_amount
        string currency "TWD / USD"
        string idempotency_key UK "防重複下單"
        timestamp expires_at "訂單鎖定到期（10 分鐘）"
        timestamp created_at
        timestamp updated_at
    }

    ORDER_ITEM {
        uuid id PK
        uuid order_id FK
        uuid zone_id FK
        integer quantity
        integer unit_price
        integer subtotal
    }

    TICKET {
        uuid id PK
        uuid order_item_id FK
        uuid seat_id FK "null = 區域票（無對號入座）"
        string ticket_code UK "用於 QR Code 的唯一識別碼"
        string status "VALID / USED / CANCELLED"
        timestamp used_at
        timestamp created_at
    }

    PAYMENT {
        uuid id PK
        uuid order_id FK
        string payment_no UK
        string idempotency_key UK "防重複付款"
        string gateway "ECPAY / NEWEBPAY / STRIPE"
        string gateway_tx_id "金流平台的交易 ID"
        integer amount
        string currency
        string status "PENDING / SUCCESS / FAILED / REFUNDED"
        timestamp paid_at
        timestamp created_at
    }

    ELECTRONIC_INVOICE {
        uuid id PK
        uuid order_id FK
        string invoice_number "AB12345678（財政部核發）"
        string random_number "4 碼隨機碼"
        string status "ISSUED / VOID"
        string carrier_type "PHONE / CERT / PAPER"
        string carrier_number "手機條碼 / 自然人憑證號"
        string buyer_tax_id "統編（公司戶才有）"
        integer tax_amount
        integer total_amount
        string gateway "ECPAY / NEWEBPAY"
        string gateway_invoice_id
        timestamp issued_at
        timestamp created_at
    }

    USER ||--o{ ORDER : "places"
    USER ||--o| LINE_BINDING : "binds"
    CONCERT ||--o{ CONCERT_SESSION : "has sessions"
    CONCERT ||--o| SEAT_MAP : "has map"
    SEAT_MAP ||--o{ ZONE_HOTSPOT : "has hotspots"
    CONCERT_SESSION ||--o{ TICKET_ZONE : "has zones"
    TICKET_ZONE ||--o{ SEAT : "has seats（對號入座）"
    ZONE_HOTSPOT }o--|| TICKET_ZONE : "maps to"
    CONCERT_SESSION ||--o{ ORDER : "for"
    ORDER ||--|{ ORDER_ITEM : "contains"
    ORDER_ITEM }o--|| TICKET_ZONE : "for zone"
    ORDER_ITEM ||--o{ TICKET : "generates"
    TICKET }o--o| SEAT : "assigned（對號入座）"
    ORDER ||--o| PAYMENT : "paid by"
    ORDER ||--o| ELECTRONIC_INVOICE : "invoiced"
```

---

## 資料表說明

### `USER`（使用者）

| 欄位 | 說明 |
|---|---|
| `role` | `USER`：一般購票用戶；`STAFF`：票務公司員工（可管理演唱會、驗票）；`ADMIN`：系統管理員（可退款、管理帳號）|
| `oauth_provider` | Google / Apple OAuth 登入來源；純 Email 登入時為 `null` |
| `password_hash` | OAuth 登入用戶可為 `null` |

---

### `LINE_BINDING`（LINE 帳號綁定）

| 欄位 | 說明 |
|---|---|
| `line_user_id` | LINE 平台的使用者唯一 ID（`U` 開頭的字串）|
| `line_display_name` | LINE 顯示名稱，綁定時儲存 |

---

### `CONCERT`（演唱會）

| 欄位 | 說明 |
|---|---|
| `status` | `DRAFT`：草稿（未公開）；`ON_SALE`：開賣中；`CANCELLED`：取消；`ENDED`：已結束 |
| `category` | 音樂類型，用於前台篩選 |

---

### `CONCERT_SESSION`（演唱會場次）

同一個演唱會可有多個場次（例如連演兩天）。

| 欄位 | 說明 |
|---|---|
| `has_assigned_seats` | `false`：區域票模式（只選票區 + 數量）；`true`：對號入座模式 |
| `max_tickets_per_order` | 每筆訂單最多可購買幾張，防止黃牛大量掃票 |
| `sale_start_at` | 開賣時間；前台可顯示倒數計時 |

---

### `TICKET_ZONE`（票區）

| 欄位 | 說明 |
|---|---|
| `sold_seats` | 已確定售出的票數（已付款）|
| `locked_seats` | Redis 中被訂單鎖定但尚未付款的票數；訂單過期後歸零 |
| 可售剩餘 | `total_seats - sold_seats - locked_seats`（Redis 中計算）|

---

### `SEAT`（座位，對號入座模式）

| 欄位 | 說明 |
|---|---|
| `status` | `AVAILABLE`：可選；`LOCKED`：被某張訂單暫時鎖定；`SOLD`：已售出 |
| `locked_until` | 鎖定到期時間，Spring Batch `SeatLockReleaseJob` 定期掃描並釋放 |

---

### `SEAT_MAP` + `ZONE_HOTSPOT`（SVG 座位圖，選配）

僅在後台上傳過 SVG 座位圖時使用。`coordinates` 為 JSON 格式的多邊形座標，前台使用 SVG pan-zoom 渲染，後台使用 Fabric.js 標記熱區。

---

### `ORDER`（訂單）

| 欄位 | 說明 |
|---|---|
| `order_no` | 對外顯示的訂單號（可讀性高，如 `ORD-20260501-0001`）|
| `status` | `PENDING`：待付款；`PAID`：已付款；`CANCELLED`：已取消；`REFUNDED`：已退款 |
| `idempotency_key` | 前端產生的唯一 key，防止網路重送造成重複建立訂單 |
| `expires_at` | 訂單建立後 10 分鐘，`OrderExpiryJob` 掃描並自動取消過期訂單 |

---

### `TICKET`（票券）

| 欄位 | 說明 |
|---|---|
| `ticket_code` | UUID，用於 QR Code 內容；配合 HMAC 簽名做動態 QR Code |
| `seat_id` | 對號入座時指向具體座位；區域票時為 `null` |
| `status` | `VALID`：有效；`USED`：已入場；`CANCELLED`：已取消 |

---

### `PAYMENT`（付款）

| 欄位 | 說明 |
|---|---|
| `gateway` | `ECPAY`：綠界；`NEWEBPAY`：藍新；`STRIPE`：Stripe |
| `gateway_tx_id` | 金流平台返回的交易流水號，用於對帳 |
| `idempotency_key` | 防止付款 API 重複呼叫造成重複扣款 |

---

### `ELECTRONIC_INVOICE`（台灣電子發票）

| 欄位 | 說明 |
|---|---|
| `invoice_number` | 財政部核發的發票號碼，格式為兩個英文字母 + 八位數字 |
| `carrier_type` | `PHONE`：手機條碼載具；`CERT`：自然人憑證；`PAPER`：紙本發票 |
| `buyer_tax_id` | 公司戶統一編號，個人購票時為 `null` |
