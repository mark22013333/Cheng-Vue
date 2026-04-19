## ADDED Requirements

### Requirement: DB 結構支援訪客
`shop_browsing_logs` 和 `shop_search_logs` 表 SHALL 新增 `guest_id VARCHAR(36)` 欄位（可 NULL），並將 `member_id` 改為可 NULL。每筆記錄 SHALL 至少有 `member_id` 或 `guest_id` 其一非 NULL。

#### Scenario: Flyway 遷移
- **WHEN** 執行 V48 遷移
- **THEN** 兩張表新增 `guest_id` 欄位，`member_id` 改為可 NULL，新增 `idx_browsing_guest_time` 和 `idx_search_guest_time` 索引

### Requirement: 追蹤上報端點支援訪客
`POST /shop/front/tracking/browse` 和 `POST /shop/front/tracking/search` SHALL 改為公開端點（`@Anonymous`），接收 JSON body 中的 `guestId` 欄位。後端邏輯：
1. 檢查 request 是否帶有有效 Member-Token → 有則寫入 `member_id`
2. 否則使用 body 中的 `guestId` → 寫入 `guest_id`
3. 兩者皆無 → 回傳 400

#### Scenario: 訪客上報瀏覽
- **WHEN** 未登入使用者發送 POST `/shop/front/tracking/browse` 帶 `{ productId: 123, guestId: "uuid-xxx" }`
- **THEN** 系統插入記錄，`guest_id = "uuid-xxx"`，`member_id = NULL`

#### Scenario: 已登入上報瀏覽
- **WHEN** 已登入使用者發送 POST `/shop/front/tracking/browse` 帶 `{ productId: 123 }`（有 Member-Token）
- **THEN** 系統插入記錄，`member_id = 會員ID`，`guest_id = NULL`

#### Scenario: 無身份上報
- **WHEN** 未登入且未帶 guestId 發送 POST `/shop/front/tracking/browse`
- **THEN** 系統回傳 400

### Requirement: 熱門統計包含訪客資料
`GET /shop/front/tracking/hot-products` 和 `GET /shop/front/tracking/popular-searches` SHALL 統計所有記錄（含 member_id 和 guest_id 的），不區分訪客或會員。

#### Scenario: 統計含訪客
- **WHEN** 查詢熱門商品，資料庫有 50 筆 member 記錄 + 100 筆 guest 記錄
- **THEN** 統計結果包含全部 150 筆的瀏覽數據
