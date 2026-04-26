# 08 — 排程任務（Spring Batch）

> [← 返回總覽](../PROJECT_PLAN.md)

---

## 目錄

1. [技術選型](#技術選型)
2. [Job 清單](#job-清單)
3. [Quartz 分散式排程設定](#quartz-分散式排程設定)
4. [後台手動觸發](#後台手動觸發)

---

## 技術選型

| 工具 | 用途 |
|---|---|
| **Spring Batch** | 定義 Job、Step、Reader / Processor / Writer，管理批次處理 |
| **Quartz Scheduler** | 分散式排程，多實例部署時確保同一 Job 同時只有一個 instance 執行 |
| `@Scheduled` | 單實例用的簡單排程，Phase 1 可用，Phase 2 水平擴展後改 Quartz |

---

## Job 清單

### `OrderExpiryJob` — 訂單超時自動取消

| 項目 | 說明 |
|---|---|
| 排程 | 每 5 分鐘 |
| 功能 | 掃描所有 `status = PENDING` 且 `expires_at < NOW()` 的訂單，自動取消並釋放 Redis 庫存 |
| 步驟 | 1. 查詢過期訂單 → 2. 更新 `status = CANCELLED` → 3. Redis 庫存加回 → 4. 發送 RabbitMQ 事件觸發通知 |
| 注意 | Redis 庫存釋放必須與 DB 狀態更新保持一致，使用 Lua Script 原子操作 |

---

### `SeatLockReleaseJob` — 座位鎖定釋放

| 項目 | 說明 |
|---|---|
| 排程 | 每 10 分鐘 |
| 功能 | 對號入座模式：掃描 `status = LOCKED` 且 `locked_until < NOW()` 的座位，釋放回 `AVAILABLE` |
| 適用 | 僅在有對號入座場次時執行（Phase 2+）|

---

### `FailedPaymentRetryJob` — 失敗付款重試

| 項目 | 說明 |
|---|---|
| 排程 | 每小時 |
| 功能 | 掃描 `status = FAILED` 的付款記錄，嘗試重新發起付款通知，超過重試次數上限則自動退票並釋放庫存 |
| 重試上限 | 3 次（可設定）|

---

### `DailyRevenueReportJob` — 每日財報產生

| 項目 | 說明 |
|---|---|
| 排程 | 每天 00:05（凌晨 0 時 5 分，等待昨日最後訂單完成）|
| 功能 | 彙整昨日所有付款成功的訂單，計算各場次 / 票區銷售數量與金額，寫入報表快取 |
| 輸出 | 寫入 DB `daily_revenue_report` 表（後台報表頁直接查詢）|

---

### `InvoiceBatchJob` — 電子發票補開

| 項目 | 說明 |
|---|---|
| 排程 | 每天 01:00 |
| 功能 | 掃描付款成功但尚未開立電子發票的訂單（可能因金流 API 異常未開立），補開並記錄 |
| 條件 | `payment.status = SUCCESS` AND `electronic_invoice IS NULL` |

---

### `ConcertReminderJob` — 開演提醒推播

| 項目 | 說明 |
|---|---|
| 排程 | 每小時 |
| 功能 | 掃描「24 小時後」與「2 小時後」開演的場次，對該場次的有效票券持有者發送 Email + LINE 通知 |
| 通知內容 | 演唱會名稱、場次時間、場館地址、入場注意事項 |
| 去重 | 同一場次同一使用者，每種提醒類型（24h / 2h）只發一次 |

---

### `ReportExportJob` — 報表匯出

| 項目 | 說明 |
|---|---|
| 排程 | 手動觸發（後台管理員操作）或定期排程 |
| 功能 | 匯出指定日期範圍的訂單記錄為 CSV / Excel，上傳至 S3 / MinIO，後台提供下載連結 |
| 格式 | CSV（預設）、Excel（.xlsx，使用 Apache POI）|

---

## Quartz 分散式排程設定

當應用程式水平擴展到多個 instance 時，`@Scheduled` 會在每個 instance 各執行一次，造成重複。  
Quartz Cluster Mode 使用 DB 鎖（`QRTZ_LOCKS` 表）確保同一個 Job 同時只有一個 instance 執行。

### Quartz 所需 DB 表

Quartz 啟動時自動建立（透過 `spring.quartz.jdbc.initialize-schema=always`）：
- `QRTZ_JOB_DETAILS`
- `QRTZ_TRIGGERS`
- `QRTZ_LOCKS`
- 等（約 11 個表）

### 設定重點

```yaml
spring:
  quartz:
    job-store-type: jdbc          # 使用 DB 儲存排程資訊（支援叢集）
    jdbc:
      initialize-schema: always   # 自動建立 Quartz 所需資料表
    properties:
      org.quartz.jobStore.isClustered: true
      org.quartz.scheduler.instanceId: AUTO
```

---

## 後台手動觸發

後台管理 Web 提供 Spring Batch Job 管理介面，讓管理員可以：

- 查看所有 Job 的最近執行記錄（開始時間、結束時間、狀態、處理筆數）
- 手動觸發指定 Job（如 `ReportExportJob`、`InvoiceBatchJob`）
- 查看 Job 執行中的即時進度

對應 API：`POST /api/v1/admin/batch/jobs/{jobName}/run`（需 ADMIN 角色）
