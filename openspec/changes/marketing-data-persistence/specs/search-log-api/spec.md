## ADDED Requirements

### Requirement: 搜尋記錄資料表
系統 SHALL 建立 `shop_search_logs` 資料表，包含 `log_id`（PK, BIGINT AUTO_INCREMENT）、`member_id`（BIGINT, NOT NULL）、`keyword`（VARCHAR(100), NOT NULL）、`result_count`（INT）、`create_time`（DATETIME, NOT NULL）。須建立 `idx_search_member_time`（member_id, create_time DESC）與 `idx_search_keyword`（keyword）索引。

#### Scenario: 資料表建立
- **WHEN** Flyway 執行 V47 遷移
- **THEN** `shop_search_logs` 表被建立，含所有欄位與索引

### Requirement: 上報搜尋事件 API
系統 SHALL 提供 `POST /shop/member/tracking/search` 端點，接收 `{ keyword, resultCount }` JSON body。系統從登入 token 取得 member_id，非同步寫入 `shop_search_logs`。成功回傳 HTTP 200。

#### Scenario: 已登入會員上報搜尋
- **WHEN** 已登入會員發送 POST `/shop/member/tracking/search` 帶 `{ keyword: "藍牙耳機", resultCount: 15 }`
- **THEN** 系統回傳 200，非同步插入一筆記錄至 `shop_search_logs`

#### Scenario: 未登入上報搜尋
- **WHEN** 未登入使用者發送 POST `/shop/member/tracking/search`
- **THEN** 系統回傳 401

### Requirement: 熱門搜尋關鍵字 API
系統 SHALL 提供 `GET /shop/front/tracking/popular-searches?days=N&limit=M` 端點（公開，無需登入），回傳最近 N 天內搜尋次數最多的關鍵字列表。預設 days=7, limit=10。

#### Scenario: 查詢熱門搜尋
- **WHEN** 發送 GET `/shop/front/tracking/popular-searches?days=7&limit=10`
- **THEN** 系統回傳最近 7 天內搜尋次數最多的 10 個關鍵字，包含 keyword、searchCount

#### Scenario: 無搜尋記錄
- **WHEN** 最近 7 天內無任何搜尋記錄
- **THEN** 系統回傳空陣列
