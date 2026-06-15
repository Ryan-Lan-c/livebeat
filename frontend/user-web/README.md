# LiveBeat — user-web

LiveBeat 使用者前台（消費者購票端）。Vue 3 + Vite + TypeScript，UI 採 shadcn-vue（Reka UI + Tailwind CSS）。

## 技術棧

- Vue 3（`<script setup>`）+ TypeScript
- Vite（dev server / build）
- Pinia（狀態管理；access token 僅存記憶體，不落 localStorage）
- Vue Router（含 auth/guest route guard）
- axios（`withCredentials`；401 自動以 HttpOnly refresh cookie 換新 token）
- @tanstack/vue-query（演唱會列表 / 詳情資料抓取）
- vue-i18n（v1 僅 zh-TW，架構保留）
- Tailwind CSS + shadcn-vue（Reka UI）

## 與後端的關係

- API base 預設 `/api/v1`；開發時由 Vite proxy 轉發至 `http://localhost:8080`（見 `vite.config.ts`）。
- 型別契約以後端 DTO 為準，定義於 `src/types/index.ts`。
- 認證：登入/註冊取得 access token（記憶體）+ HttpOnly refresh cookie；過期時 `src/api/http.ts` 的攔截器自動呼叫 `/auth/refresh` 換新並重送請求，失敗則導回登入。

## 開發

```sh
npm install          # 安裝相依
npm run dev          # 開發伺服器（需後端在 :8080 或調整 proxy）
npm run type-check   # 型別檢查（vue-tsc）
npm run lint         # oxlint + eslint（--fix）
npm run build        # type-check + production build
```

## 目錄

```
src/
  api/         # http client 與各模組 API（auth, concerts）
  components/  # UI 元件（shadcn-vue：button, input）
  layouts/     # 版型（DefaultLayout）
  locales/     # i18n 訊息（zh-TW）
  router/      # 路由與 guard
  stores/      # Pinia store（auth）
  types/       # 與後端對齊的型別契約
  views/       # 頁面（登入 / 註冊 / 演唱會列表 / 詳情）
```
