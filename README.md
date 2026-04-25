# LiveBeat 🎵

> 供全球使用、支援極高並發的演唱會訂票平台

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs)
![Flutter](https://img.shields.io/badge/Flutter-3-02569B?logo=flutter)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

---

## 專案簡介

LiveBeat 是一套類似 KKTIX 的票務平台，票務公司員工透過後台管理演唱會與場次，使用者在前台網站或 Flutter App 登入後購票。

**核心特色：**
- Redis Lua Script 原子庫存扣減，防止超賣
- WebSocket 即時票況廣播
- 動態 HMAC QR Code，每 60 秒刷新，防截圖偽造
- Hexagonal Modular Monolith 架構，模組邊界清晰，可按需拆分 Microservices

---

## 技術架構

| 層級 | 技術 |
|---|---|
| 後端 | Java 25 · Spring Boot 3 · Spring Modulith · Spring Security |
| 資料層 | PostgreSQL 16 · Redis 7 · Flyway |
| 訊息佇列 | RabbitMQ |
| 前台 Web | Vue 3 · Element Plus · TanStack Query · Pinia |
| 後台 Web | Vue 3 · Element Plus · ECharts |
| 行動 App | Flutter 3（Android + iOS）|
| 金流 | ECPay · NewebPay · Stripe |
| 基礎設施 | Docker Compose · Nginx · MinIO · Jaeger |

---

## 快速啟動

> 完整環境安裝說明請參閱 [docs/00-getting-started.md](docs/00-getting-started.md)

```bash
# 1. 啟動所有依賴服務
docker compose -f infrastructure/docker-compose.yml up -d

# 2. 啟動後端
cd backend/livebeat && ./gradlew bootRun

# 3. 啟動使用者前台 Web
cd frontend/user-web && npm install && npm run dev

# 4. 啟動後台管理 Web
cd frontend/admin-web && npm install && npm run dev
```

| 服務 | 位址 |
|---|---|
| 使用者前台 | http://localhost:5173 |
| 後台管理 | http://localhost:5174 |
| API / Swagger | http://localhost:8080/swagger-ui.html |
| Mailpit | http://localhost:8025 |

---

## 文件

詳細技術文件請見 [PROJECT_PLAN.md](PROJECT_PLAN.md)

| 文件 | 說明 |
|---|---|
| [00 — 啟動指南](docs/00-getting-started.md) | 環境安裝、各端啟動步驟 |
| [01 — 技術選型](docs/01-tech-stack.md) | 完整技術棧清單 |
| [02 — 系統架構](docs/02-architecture.md) | Hexagonal 架構、模組設計、高並發策略 |
| [03 — 資料模型](docs/03-data-model.md) | ERD、資料表說明 |
| [04 — API 設計](docs/04-api.md) | REST API 總覽 |
| [09 — 開發里程碑](docs/09-milestones.md) | Gantt 圖、部署策略 |

---

## License

[MIT](LICENSE)
