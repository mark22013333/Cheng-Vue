## Why

前一階段 `cookie-consent-marketing` 建立了前端行銷追蹤基礎建設，但瀏覽足跡與搜尋記錄僅存在 localStorage，存在跨裝置不同步、清快取即遺失、無法後端分析等問題。要真正發揮行銷價值（個人化推薦、再行銷推播、熱門商品統計），必須將已登入使用者的行為資料持久化到 DB，並提供 API 供前端與未來的推薦引擎使用。

## What Changes

- **新增 DB 表**：`shop_browsing_logs`（瀏覽足跡）、`shop_search_logs`（搜尋記錄），以 `member_id` 關聯會員
- **新增後端 API**：提供非同步寫入端點（瀏覽/搜尋事件上報）與查詢端點（最近瀏覽、熱門商品、搜尋建議）
- **前端整合**：`useMarketingTracker` 在已登入使用者同意行銷 Cookie 後，非同步上報行為事件至後端 API，localStorage 作為快取層保留
- **Flyway 遷移**：新增 V47 遷移檔案建立行銷資料表

## Capabilities

### New Capabilities
- `browsing-log-api`: 瀏覽足跡 API——後端接收並儲存已登入會員的商品瀏覽記錄，提供最近瀏覽查詢與熱門商品統計
- `search-log-api`: 搜尋記錄 API——後端接收並儲存搜尋關鍵字，提供搜尋建議（熱門搜尋）
- `tracker-backend-sync`: 前端追蹤器後端同步——擴展 `useMarketingTracker`，已登入會員的行為事件非同步上報至後端 API

### Modified Capabilities

（無現有 spec 需要修改）

## Impact

- **資料庫**：新增 2 張表 + 索引，Flyway V47 遷移
- **後端模組**：`cheng-shop` 新增 controller/service/mapper/domain 各一組
- **前端**：修改 `useMarketingTracker.js` 新增後端同步邏輯，新增 `api/shop/marketing.js` API 模組
- **效能考量**：瀏覽記錄為高頻寫入，需考慮非同步處理與批次寫入策略
