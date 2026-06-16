# LiveBeat — 專案總覽

> 最後更新：2026-06-16（UTC+8）
> 狀態：**Phase 2 進行中 — Auth / Concert / Order（購票垂直切片）後端已實作；前台主線 view 建立中**

---

## 專案簡介

一套供全球使用、支援極高並發的演唱會訂票平台。  
票務公司員工透過後台管理演唱會與場次；使用者在前台網站或 Flutter App 登入後購票。  
技術核心為 Java 25 + Spring Boot 4 Modular Monolith，搭配 Vue 3 前端與 Flutter 行動 App。

---

## 文件導覽

| 文件 | 說明 |
|---|---|
| [00 — 啟動指南](docs/00-getting-started.md) | 環境安裝、IDE 設定、各端啟動步驟（Windows / macOS / Linux）|
| [01 — 技術選型](docs/01-tech-stack.md) | 後端、前端、App、金流、CI/CD、基礎設施完整清單與 Tech Stack 圖 |
| [02 — 系統架構](docs/02-architecture.md) | Hexagonal 架構、模組邊界規則、Monolith → Microservices 拆分路徑、程式碼註解規範 |
| [03 — 資料模型](docs/03-data-model.md) | ERD 圖、各資料表欄位說明 |
| [04 — API 設計](docs/04-api.md) | 使用者端與後台端 REST API 總覽（v1）|
| [05 — 功能清單](docs/05-features.md) | 前台 Web、Flutter App、後台管理系統功能項目 |
| [06 — 安全設計](docs/06-security.md) | JWT 策略、動態 QR Code HMAC、OAuth2、Idempotency Key |
| [07 — 外部整合](docs/07-integrations.md) | WebSocket 即時票況、LINE Bot、金流串接、HTML Email |
| [08 — 排程任務](docs/08-batch-jobs.md) | Spring Batch Job 清單、Quartz 分散式排程 |
| [09 — 開發里程碑](docs/09-milestones.md) | Gantt 圖、部署策略、待確認事項 |

---

## 當前進度

> 本表為「一眼看進度」的單一來源（done / 進行中 / 未開始）。

| 項目 | 狀態 |
|---|---|
| 商業模式 / 架構策略 / 技術選型 | ✅ 確認完成 |
| 資料模型 / API 初稿 | ✅ 完成 |
| Docker Compose 本機環境 | ✅ 完成 |
| Auth Module（後端）| ✅ JWT register / login / refresh / logout、profile |
| Concert Module（後端）| ✅ 演唱會 / 場次 / 票區 CRUD、pg_trgm 搜尋、前後台 Controller |
| **Order Module（後端購票垂直切片）** | ✅ 同步下單防超賣（Redis Lua）、開賣 warm-up、過期回補、Redis 故障復原 + 對帳、sandbox 付款 + 出票（見 [docs/10](docs/10-order-design.md)）|
| 程式碼健康度（P0–P3 修正輪）| ✅ 安全 / CI / 測試 / 例外 / migration 等 19 項已修 |
| 規劃決策 D1–D4 | ✅ 已拍板（台灣優先、單機誠實容量、NFR 初始值）|
| 前台實作（user-web）| 🚧 auth flow、型別契約、主線 view（瀏覽）已建立；完整訂票 / 付款 UI 未接 |
| 真實金流 + 電子發票（payment 模組）| ⏳ 尚未開始（order 已有 sandbox 付款佔位）|
| 後台 Admin Module + admin-web | ⏳ 尚未開始 |
| Flutter App | ⏳ 尚未開始 |

### 下一步（待拍板）

order 購票切片已端到端打通並以真實 Redis + PG 整合測試驗證；接下來可選：

1. **真實金流串接**（綠界 ECPay → 電子發票）—— 銜接目前的 sandbox 付款。
2. **Admin 模組 + 後台 Web** —— 讓票務公司能管理演唱會 / 訂單 / 報表。
3. **前台訂票 / 付款 UI** —— 把後端切片接到 user-web。
4. 規劃決策 **D5–D8**（過度設計、PII 合規、版本風險、GTM）。

---

## 待確認事項

| 項目 | 備註 |
|---|---|
| 正式環境雲端平台 | 本機 Docker 先行，建議 Hetzner / Vultr 亞洲機房 |
| SMS 通知服務商 | Twilio 或台灣業者，待選定 |
| 多幣別支援範圍 | Stripe 支援，需確認幣別清單 |
| 票券轉讓防黃牛機制 | 實名綁定或轉讓次數限制，待設計 |
| 特定演唱會實名制 | 是否需要身份驗證，待確認 |
