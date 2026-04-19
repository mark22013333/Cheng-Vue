## ADDED Requirements

### Requirement: 前端 API 模組
系統 SHALL 建立 `src/api/shop/marketing.js` 模組，提供以下方法：
- `reportBrowse(data)` → POST `/shop/member/tracking/browse`
- `reportSearch(data)` → POST `/shop/member/tracking/search`
- `getRecentViews(limit)` → GET `/shop/member/tracking/recent-views`
- `getPopularSearches(days, limit)` → GET `/shop/front/tracking/popular-searches`
- `getHotProducts(days, limit)` → GET `/shop/front/tracking/hot-products`

#### Scenario: API 模組可用
- **WHEN** 前端引入 `@/api/shop/marketing`
- **THEN** 所有五個方法皆可呼叫，使用專案統一的 request 工具

### Requirement: 追蹤器後端同步
`useMarketingTracker` 的 `trackProductView` 與 `trackSearch` 方法 SHALL 在使用者已登入（有 Member-Token）且行銷 Cookie 已同意時，除了寫入 localStorage 外，同時以 fire-and-forget 方式呼叫後端 API 上報。API 呼叫失敗時靜默忽略，不影響前端功能。

#### Scenario: 已登入且同意時雙寫
- **WHEN** 已登入使用者同意行銷 Cookie 且瀏覽商品頁
- **THEN** 瀏覽記錄同時寫入 localStorage 與後端 API

#### Scenario: 已登入但未同意時不寫入
- **WHEN** 已登入使用者未同意行銷 Cookie 且瀏覽商品頁
- **THEN** 不寫入 localStorage 也不呼叫後端 API

#### Scenario: 未登入時僅 localStorage
- **WHEN** 未登入使用者同意行銷 Cookie 且瀏覽商品頁
- **THEN** 僅寫入 localStorage，不呼叫後端 API

#### Scenario: API 失敗時不影響體驗
- **WHEN** 後端 API 不可用或回傳錯誤
- **THEN** 前端靜默忽略，localStorage 資料仍正常保留
