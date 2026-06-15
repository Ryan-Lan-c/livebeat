# Flyway Migration 規範

## 版本命名規則

採「模組.序號」格式 `V<module>.<seq>__<description>.sql`：

| 模組編號 | 模組 | 檔案 |
|---|---|---|
| 1 | auth | `V1__create_auth_tables.sql` |
| 2 | concert | `V2.1__...`、`V2.2__...`、`V2.3__...` |
| 3 | profile | `V3.1__create_profile_tables.sql` |

- `<module>` 對應功能模組，`<seq>` 為該模組內的遞增序號（從 1 起；auth 目前僅一個檔，故省略為 `V1`）。
- `2.3` 不是 `2` 的 patch，而是 concert 模組的第 3 個 migration。
- 新增 migration 一律在對應模組編號下接續下一個序號。

## 鐵則：已套用的 migration 不可再編輯

- 任何已在某環境套用過的 migration 檔案內容（含 SQL、空白）**不得修改**——Flyway 以 checksum 驗證，改動會導致 `validate` 失敗。
- 需要變更時，**一律新增一個 migration 檔**（additive），不要回頭改舊檔。
- CI 會跑 Flyway `validate`（搭配 Testcontainers）以偵測 checksum 漂移。

> 歷史備註：舊的 `V2.3__add_concert_fts_indexes.sql` 與 `V2.1` 重複建立同樣的 pg_trgm
> GIN index（V2.1 被事後編輯、把 index 併入），已於整理時移除；`V2.3` 改作為票區
> 樂觀鎖 + 庫存約束的 migration。此次整理僅因尚無共用/已部署環境而可行。
