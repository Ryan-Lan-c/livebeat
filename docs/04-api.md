# 04 — API 設計

> [← 返回總覽](../PROJECT_PLAN.md)

---

## 目錄

1. [API 版本規範](#api-版本規範)
2. [使用者端 API](#使用者端-api)
3. [後台管理端 API](#後台管理端-api)
4. [通用規範](#通用規範)

---

## API 版本規範

所有 API 路徑以 `/api/v1/` 開頭。  
未來若有重大 Breaking Change，新版本使用 `/api/v2/`，舊版本維持一段時間並行，確保舊版 App 不中斷。

Swagger UI（開發環境）：http://localhost:8080/swagger-ui.html  
OpenAPI JSON：http://localhost:8080/v3/api-docs

---

## 使用者端 API

### 認證（Auth）

| Method | Path | 說明 | 權限 |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Email 註冊（姓名、Email、密碼、電話）| Public |
| POST | `/api/v1/auth/login` | Email 登入，回傳 Access Token + Refresh Token | Public |
| POST | `/api/v1/auth/refresh` | 用 Refresh Token 換新的 Access Token | Public |
| POST | `/api/v1/auth/logout` | 登出，使 Refresh Token 失效 | USER |
| GET | `/api/v1/auth/oauth2/google` | 發起 Google OAuth 登入 | Public |
| GET | `/api/v1/auth/oauth2/apple` | 發起 Apple OAuth 登入 | Public |
| GET | `/api/v1/auth/me` | 取得當前登入使用者資訊 | USER |
| PUT | `/api/v1/auth/me` | 更新個人資料（姓名、電話）| USER |
| POST | `/api/v1/auth/line/bind` | 發起 LINE 帳號綁定流程，回傳 LINE Login URL | USER |
| DELETE | `/api/v1/auth/line/bind` | 解除 LINE 帳號綁定 | USER |

---

### 演唱會（Concert）

| Method | Path | 說明 | 權限 |
|---|---|---|---|
| GET | `/api/v1/concerts` | 演唱會列表（支援：關鍵字搜尋、分類篩選、分頁、排序）| Public |
| GET | `/api/v1/concerts/{id}` | 演唱會詳情 + 場次列表 | Public |
| GET | `/api/v1/concerts/{id}/sessions/{sessionId}` | 場次詳情 + 各票區剩餘數量 | Public |
| GET | `/api/v1/concerts/{id}/sessions/{sessionId}/seats` | 對號入座座位圖資料 | Public |

**GET `/api/v1/concerts` 支援的 Query Parameters：**

| 參數 | 說明 | 範例 |
|---|---|---|
| `q` | 關鍵字搜尋（演唱會名稱、藝人、場館）| `?q=周杰倫` |
| `category` | 分類篩選 | `?category=流行` |
| `city` | 城市篩選 | `?city=台北` |
| `page` | 頁碼（從 0 開始）| `?page=0` |
| `size` | 每頁筆數（預設 20）| `?size=20` |
| `sort` | 排序欄位 | `?sort=event_date,asc` |

---

### 訂單（Order）

| Method | Path | 說明 | 權限 |
|---|---|---|---|
| POST | `/api/v1/orders` | 建立訂單（搶票），回傳 orderId + 倒數秒數 | USER |
| GET | `/api/v1/orders` | 我的訂單列表（分頁）| USER |
| GET | `/api/v1/orders/{id}` | 訂單詳情 | USER |
| DELETE | `/api/v1/orders/{id}` | 取消訂單（僅 PENDING 狀態可取消）| USER |

**POST `/api/v1/orders` Request Body：**
```json
{
  "sessionId": "uuid",
  "items": [
    {
      "zoneId": "uuid",
      "quantity": 2,
      "seatIds": ["uuid", "uuid"]
    }
  ],
  "idempotencyKey": "client-generated-uuid"
}
```

> `seatIds` 僅對號入座模式需要填入；區域票模式傳空陣列即可。

---

### 付款（Payment）

| Method | Path | 說明 | 權限 |
|---|---|---|---|
| POST | `/api/v1/payments` | 發起付款，回傳金流跳轉 URL | USER |
| GET | `/api/v1/payments/{id}` | 查詢付款狀態 | USER |
| POST | `/api/v1/payments/webhook/ecpay` | 綠界金流回調（IP 白名單驗證）| Public |
| POST | `/api/v1/payments/webhook/newebpay` | 藍新金流回調（IP 白名單驗證）| Public |
| POST | `/api/v1/payments/webhook/stripe` | Stripe 回調（Webhook 簽名驗證）| Public |

---

### 票券（Ticket）

| Method | Path | 說明 | 權限 |
|---|---|---|---|
| GET | `/api/v1/tickets` | 我的票券列表 | USER |
| GET | `/api/v1/tickets/{ticketCode}` | 單張票券詳情 | USER |
| GET | `/api/v1/tickets/{ticketCode}/qr-token` | 取得動態 QR Code Token（60 秒有效）| USER |

---

### LINE Bot

| Method | Path | 說明 | 權限 |
|---|---|---|---|
| POST | `/api/v1/line/webhook` | LINE 平台事件回調（訊息、Follow、Unfollow）| LINE 簽名驗證 |

---

## 後台管理端 API

> 角色說明：`ORGANIZER`（主辦方）、`STAFF`（驗票工作人員）、`ADMIN`（系統管理員）。  
> ORGANIZER 只能操作自己建立的演唱會與相關資料；ADMIN 可操作全部。

### 總覽

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| GET | `/api/v1/admin/dashboard` | 即時總覽（今日銷量、金額、用戶數）| ORGANIZER / ADMIN |

---

### 演唱會管理

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| POST | `/api/v1/admin/concerts` | 新增演唱會 | ORGANIZER / ADMIN |
| PUT | `/api/v1/admin/concerts/{id}` | 更新演唱會基本資訊 | ORGANIZER / ADMIN |
| DELETE | `/api/v1/admin/concerts/{id}` | 刪除演唱會（僅 DRAFT 狀態可刪）| ADMIN |
| POST | `/api/v1/admin/concerts/{id}/sessions` | 新增場次 | ORGANIZER / ADMIN |
| PUT | `/api/v1/admin/concerts/{id}/sessions/{sid}` | 更新場次資訊 | ORGANIZER / ADMIN |
| PATCH | `/api/v1/admin/concerts/{id}/sessions/{sid}/status` | 更新場次狀態（上架 / 下架）| ORGANIZER / ADMIN |
| POST | `/api/v1/admin/concerts/{id}/seat-map` | 上傳 SVG 座位圖 | ORGANIZER / ADMIN |
| POST | `/api/v1/admin/concerts/{id}/seat-map/hotspots` | 儲存票區熱區座標 | ORGANIZER / ADMIN |

---

### 訂單管理

> ORGANIZER 只能查看自己演唱會的訂單。

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| GET | `/api/v1/admin/orders` | 訂單列表（支援搜尋、篩選、分頁）| ORGANIZER / ADMIN |
| GET | `/api/v1/admin/orders/{id}` | 訂單詳情 | ORGANIZER / ADMIN |
| GET | `/api/v1/admin/orders/export` | 匯出訂單 CSV | ORGANIZER / ADMIN |

---

### 退款

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| POST | `/api/v1/admin/payments/{id}/refund` | 手動退款 | ADMIN |

---

### 統計報表

> ORGANIZER 只能查看自己演唱會的報表。

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| GET | `/api/v1/admin/revenue` | 收入報表（支援 `?period=day/week/month/year`）| ORGANIZER / ADMIN |
| GET | `/api/v1/admin/concerts/{id}/stats` | 單場演唱會各場次銷售統計 | ORGANIZER / ADMIN |

---

### 驗票

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| POST | `/api/v1/admin/tickets/verify` | 驗票（掃描 QR Code 內容後呼叫）| STAFF / ORGANIZER / ADMIN |
| GET | `/api/v1/admin/tickets/scan-log` | 入場記錄查詢 | STAFF / ORGANIZER / ADMIN |

**POST `/api/v1/admin/tickets/verify` Request Body：**
```json
{
  "qrContent": "ticket_code.timestamp_window.hmac_signature"
}
```

---

### STAFF 帳號管理

> ORGANIZER 建立與管理自己旗下的驗票工作人員帳號。

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| POST | `/api/v1/admin/staff` | 建立 STAFF 帳號（自動綁定至當前 ORGANIZER）| ORGANIZER / ADMIN |
| GET | `/api/v1/admin/staff` | 查看自己旗下的 STAFF 列表 | ORGANIZER / ADMIN |
| DELETE | `/api/v1/admin/staff/{id}` | 停用 STAFF 帳號 | ORGANIZER / ADMIN |

---

### 推播

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| POST | `/api/v1/admin/broadcast` | 發送全體 LINE / Email 公告 | ADMIN |

---

### 系統管理

| Method | Path | 說明 | 所需角色 |
|---|---|---|---|
| GET | `/api/v1/admin/users` | 使用者列表 | ADMIN |
| PATCH | `/api/v1/admin/users/{id}/role` | 修改使用者角色 | ADMIN |
| GET | `/api/v1/admin/audit-log` | 操作日誌 | ADMIN |

---

## 通用規範

### HTTP 狀態碼

| 狀態碼 | 使用情境 |
|---|---|
| 200 OK | 查詢成功 |
| 201 Created | 建立成功（如 POST /orders）|
| 202 Accepted | 非同步處理中（如搶票後非同步寫 DB）|
| 400 Bad Request | 請求格式錯誤、驗證失敗 |
| 401 Unauthorized | 未登入或 Token 失效 |
| 403 Forbidden | 已登入但權限不足 |
| 404 Not Found | 資源不存在 |
| 409 Conflict | 業務衝突（如票券已售罄、訂單重複）|
| 422 Unprocessable Entity | 業務邏輯錯誤（如訂單已過期無法付款）|
| 500 Internal Server Error | 系統錯誤 |

### 統一錯誤回應格式

```json
{
  "code": "ORDER_EXPIRED",
  "message": "訂單已過期，庫存已釋放",
  "timestamp": "2026-05-01T10:00:00Z"
}
```

### 分頁回應格式

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false
}
```
