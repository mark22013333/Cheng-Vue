## Why

前一階段 `marketing-data-persistence` 的 DB 追蹤僅限已登入會員（`member_id NOT NULL`），訪客行為只存在 localStorage 中。這導致：
- 無法分析「瀏覽 → 註冊 → 購買」的完整轉換漏斗
- 熱門商品統計遺漏大量訪客流量（通常訪客佔 60-80%）
- 訪客登入後，其先前的瀏覽/搜尋歷史無法與帳號關聯

方案 A：前端產生 `guest_id`（UUID），DB 表新增 `guest_id` 欄位、`member_id` 改為可 NULL，訪客登入後自動合併歷史記錄。

## What Changes

- **DB 結構調整**：`shop_browsing_logs` 和 `shop_search_logs` 表新增 `guest_id VARCHAR(36)` 欄位，`member_id` 改為可 NULL，新增 `idx_browsing_guest_time` 等索引
- **訪客 ID 管理**：前端產生 UUID 存入 Cookie（`guest_tracking_id`，歸類為行銷層級），有效期 90 天
- **後端 API 調整**：追蹤上報端點改為公開（`@Anonymous`），接收 `guestId` 或 `memberId`（擇一）；新增合併端點
- **登入後自動合併**：會員登入成功後，後端將同一 `guest_id` 的歷史記錄的 `member_id` 更新為登入會員 ID

## Capabilities

### New Capabilities
- `guest-id-management`: 訪客 ID 生命週期管理——前端產生/讀取/清除 guest UUID，登入後清除
- `guest-tracking-api`: 訪客追蹤 API——擴展現有追蹤端點支援訪客上報，新增合併 API
- `login-merge`: 登入後歷史合併——會員登入時自動將 guest_id 的歷史記錄歸屬到 member_id

### Modified Capabilities

（無現有 spec 需修改）

## Impact

- **資料庫**：Flyway V48 遷移，ALTER 兩張現有表 + 新增索引
- **後端**：修改 `ShopMemberTrackingController`、`ShopTrackingServiceImpl`、兩個 Mapper XML；修改或擴展登入流程
- **前端**：修改 `useMarketingTracker.js`（訪客 ID + 無 token 也上報）、修改 `useSocialLogin.js` 或登入流程（登入後觸發合併）
- **Cookie 同意**：`guest_tracking_id` Cookie 歸類為行銷層級，未同意時不產生
