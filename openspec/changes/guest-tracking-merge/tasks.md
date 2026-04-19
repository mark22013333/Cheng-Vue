## 1. 資料庫遷移

- [x] 1.1 ~~建立 V48~~ 已合併至 `V47__shop_marketing_tracking.sql`：CREATE TABLE 直接包含 `guest_id VARCHAR(36)` 欄位、`member_id` 可 NULL、`idx_browsing_guest_time` 和 `idx_search_guest_time` 索引

## 2. 後端 Domain + Mapper 調整

- [x] 2.1 修改 `ShopBrowsingLog.java`、`ShopSearchLog.java`：新增 `guestId` 欄位
- [x] 2.2 修改 `BrowseEventRequest.java`、`SearchEventRequest.java`：新增 `guestId` 欄位
- [x] 2.3 修改 `ShopBrowsingLogMapper.xml`：insert 加入 guest_id、selectRecentByMemberId 保持不變、新增 `mergeGuestToMember(guestId, memberId)` UPDATE 語句
- [x] 2.4 修改 `ShopSearchLogMapper.xml`：同上，insert 加入 guest_id、新增 `mergeGuestToMember`
- [x] 2.5 修改 `ShopBrowsingLogMapper.java`、`ShopSearchLogMapper.java`：新增 `mergeGuestToMember` 方法簽名

## 3. 後端 Service + Controller 調整

- [x] 3.1 修改 `ShopTrackingServiceImpl.java`：`logBrowse`/`logSearch` 支援 memberId 為 null + guestId 的情境；新增 `mergeGuestRecords(guestId, memberId)` 方法
- [x] 3.2 修改追蹤上報端點為公開：將 browse 和 search 端點移至 `ShopFrontTrackingController`（加 `@Anonymous`），後端檢查 token 有效性決定用 memberId 或 guestId
- [x] 3.3 新增合併端點：在 `ShopMemberTrackingController` 新增 `POST /merge`，接收 `{ guestId }`，呼叫 service.mergeGuestRecords

## 4. 前端訪客 ID 管理

- [x] 4.1 修改 `useMarketingTracker.js`：新增 `getOrCreateGuestId()` 方法（讀取/產生 `guest_tracking_id` Cookie），用 `Cookies` 套件操作，有效期 90 天
- [x] 4.2 修改 `trackProductView` 和 `trackSearch`：移除 `if (getMemberToken())` 的限制，改為「已登入 → 傳 memberId、未登入 → 傳 guestId」皆上報
- [x] 4.3 修改 `consent.js` store 的 `_clearNonEssentialData`：撤銷行銷同意時也清除 `guest_tracking_id` Cookie

## 5. 前端登入合併流程

- [x] 5.1 在 `api/shop/marketing.js` 新增 `mergeGuestTracking(guestId)` API 方法
- [x] 5.2 修改 `login.vue` 的登入成功回調：登入後檢查 `guest_tracking_id` Cookie，有則呼叫合併 API + 清除 Cookie
- [x] 5.3 修改 `oauth-callback.vue` 的 OAuth 登入成功回調：同上

## 6. 驗證

- [x] 6.1 後端編譯驗證：`mvn compile` 無錯誤
- [x] 6.2 前端建置驗證：`pnpm run build` 無錯誤
