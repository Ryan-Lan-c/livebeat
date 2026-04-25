# LiveBeat — 專案總覽

> 最後更新：2026-04-26（UTC+8）
> 狀態：**架構討論完成，準備開始實作**

---

## 專案簡介

一套供全球使用、支援極高並發的演唱會訂票平台。  
票務公司員工透過後台管理演唱會與場次；使用者在前台網站或 Flutter App 登入後購票。  
技術核心為 Java 25 + Spring Boot 3 Modular Monolith，搭配 Vue 3 前端與 Flutter 行動 App。

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

| 項目 | 狀態 |
|---|---|
| 商業模式 | ✅ 票務公司，員工後台管理演唱會 |
| 架構策略 | ✅ Hexagonal Modular Monolith → 按需拆 Microservices |
| 技術選型 | ✅ 確認完成（見 [01 — 技術選型](docs/01-tech-stack.md)）|
| 資料模型初稿 | ✅ 完成（見 [03 — 資料模型](docs/03-data-model.md)）|
| API 初稿 | ✅ 完成（見 [04 — API 設計](docs/04-api.md)）|
| 前台 UI 參考 | ✅ LiveBeat_Vue3 Demo 版 |
| 後端實作 | ⏳ Phase 1 準備開始 |
| 前台實作 | ⏳ 尚未開始 |
| 後台實作 | ⏳ 尚未開始 |
| Flutter App | ⏳ 尚未開始 |

---

## 待確認事項

| 項目 | 備註 |
|---|---|
| 正式環境雲端平台 | 本機 Docker 先行，建議 Hetzner / Vultr 亞洲機房 |
| SMS 通知服務商 | Twilio 或台灣業者，待選定 |
| 多幣別支援範圍 | Stripe 支援，需確認幣別清單 |
| 票券轉讓防黃牛機制 | 實名綁定或轉讓次數限制，待設計 |
| 特定演唱會實名制 | 是否需要身份驗證，待確認 |
