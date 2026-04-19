## ADDED Requirements

### Requirement: 瀏覽足跡資料表
系統 SHALL 建立 `shop_browsing_logs` 資料表，包含 `log_id`（PK, BIGINT AUTO_INCREMENT）、`member_id`（BIGINT, NOT NULL）、`product_id`（BIGINT, NOT NULL）、`product_name`（VARCHAR(200)）、`category_id`（BIGINT）、`source`（VARCHAR(20), 預設 'DIRECT'）、`create_time`（DATETIME, NOT NULL）。須建立 `idx_browsing_member_time`（member_id, create_time DESC）與 `idx_browsing_product`（product_id）索引。

#### Scenario: 資料表建立
- **WHEN** Flyway 執行 V47 遷移
- **THEN** `shop_browsing_logs` 表被建立，含所有欄位與索引

### Requirement: 上報瀏覽事件 API
系統 SHALL 提供 `POST /shop/member/tracking/browse` 端點，接收 `{ productId, productName, categoryId, source }` JSON body。系統從登入 token 取得 member_id，非同步寫入 `shop_browsing_logs`。成功回傳 HTTP 200。

#### Scenario: 已登入會員上報瀏覽
- **WHEN** 已登入會員發送 POST `/shop/member/tracking/browse` 帶 `{ productId: 123, productName: "商品A", categoryId: 5, source: "SEARCH" }`
- **THEN** 系統回傳 200，非同步插入一筆記錄至 `shop_browsing_logs`

#### Scenario: 未登入上報瀏覽
- **WHEN** 未登入使用者發送 POST `/shop/member/tracking/browse`
- **THEN** 系統回傳 401

### Requirement: 查詢個人最近瀏覽 API
系統 SHALL 提供 `GET /shop/member/tracking/recent-views?limit=N` 端點，回傳當前會員最近瀏覽的商品列表（依 create_time DESC），預設 limit=10，最大 50。回傳中同一商品僅保留最近一次瀏覽記錄。

#### Scenario: 查詢最近瀏覽
- **WHEN** 已登入會員發送 GET `/shop/member/tracking/recent-views?limit=5`
- **THEN** 系統回傳最近 5 筆不重複商品的瀏覽記錄，包含 productId、productName、categoryId、create_time

#### Scenario: 無瀏覽記錄
- **WHEN** 已登入會員無任何瀏覽記錄
- **THEN** 系統回傳空陣列

### Requirement: 熱門瀏覽商品 API
系統 SHALL 提供 `GET /shop/front/tracking/hot-products?days=N&limit=M` 端點（公開，無需登入），回傳最近 N 天內瀏覽次數最多的商品列表。預設 days=7, limit=10。

#### Scenario: 查詢熱門商品
- **WHEN** 發送 GET `/shop/front/tracking/hot-products?days=7&limit=5`
- **THEN** 系統回傳最近 7 天內瀏覽次數最多的 5 個商品，包含 productId、productName、viewCount
