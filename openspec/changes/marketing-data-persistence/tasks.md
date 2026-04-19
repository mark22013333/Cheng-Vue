## 1. 資料庫遷移

- [x] 1.1 建立 Flyway 遷移檔 `V47__shop_marketing_tracking.sql`，建立 `shop_browsing_logs` 表（含 `idx_browsing_member_time`、`idx_browsing_product` 索引）
- [x] 1.2 同遷移檔內建立 `shop_search_logs` 表（含 `idx_search_member_time`、`idx_search_keyword` 索引）

## 2. 後端 Domain 層

- [x] 2.1 建立 `ShopBrowsingLog.java` 實體類（log_id, member_id, product_id, product_name, category_id, source, create_time）
- [x] 2.2 建立 `ShopSearchLog.java` 實體類（log_id, member_id, keyword, result_count, create_time）
- [x] 2.3 建立 `BrowseEventRequest.java` DTO（productId, productName, categoryId, source）
- [x] 2.4 建立 `SearchEventRequest.java` DTO（keyword, resultCount）
- [x] 2.5 建立 `HotProductVO.java` VO（productId, productName, viewCount）
- [x] 2.6 建立 `PopularSearchVO.java` VO（keyword, searchCount）

## 3. 後端 Mapper 層

- [x] 3.1 建立 `ShopBrowsingLogMapper.java` 介面與 XML：insert、selectRecentByMemberId（去重取最近 N 筆）、selectHotProducts（按瀏覽數統計）
- [x] 3.2 建立 `ShopSearchLogMapper.java` 介面與 XML：insert、selectPopularKeywords（按搜尋數統計）

## 4. 後端 Service 層

- [x] 4.1 建立 `IShopTrackingService.java` 介面，定義 logBrowse、logSearch、getRecentViews、getHotProducts、getPopularSearches 方法
- [x] 4.2 建立 `ShopTrackingServiceImpl.java` 實作，logBrowse 和 logSearch 使用 `@Async` 非同步寫入

## 5. 後端 Controller 層

- [x] 5.1 建立 `ShopMemberTrackingController.java`（路由 `/shop/member/tracking`，需登入），提供 POST browse、POST search、GET recent-views 端點
- [x] 5.2 建立 `ShopFrontTrackingController.java`（路由 `/shop/front/tracking`，公開），提供 GET hot-products、GET popular-searches 端點

## 6. 前端 API 與整合

- [x] 6.1 建立 `src/api/shop/marketing.js`，封裝 reportBrowse、reportSearch、getRecentViews、getPopularSearches、getHotProducts 方法
- [x] 6.2 修改 `src/composables/useMarketingTracker.js`：在 trackProductView 和 trackSearch 中，判斷已登入時 fire-and-forget 呼叫後端 API
- [x] 6.3 確保 API 呼叫失敗時靜默忽略（catch 空處理），不影響 localStorage 寫入

## 7. 驗證

- [x] 7.1 執行 Flyway 遷移驗證：`mvn flyway:validate` 或啟動應用確認表建立成功
- [x] 7.2 前端建置驗證：`pnpm run build` 無錯誤
