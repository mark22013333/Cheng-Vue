## 1. Consent Manager（同意管理核心）

- [x] 1.1 建立 `src/store/modules/consent.js` Pinia store，實作三層同意狀態（essential/functional/marketing）、`acceptAll()`、`acceptEssentialOnly()`、`isAllowed(level)` 方法
- [x] 1.2 實作同意狀態 localStorage 持久化（key: `cookie_consent`，格式含 timestamp）
- [x] 1.3 實作舊版 `cookie_consent_accepted` key 遷移邏輯（偵測 → 轉換為全部接受 → 移除舊 key）
- [x] 1.4 實作 `acceptEssentialOnly()` 時的資料清除：移除功能性 Cookie（`username`/`password`/`rememberMe`）與 localStorage（`mall_theme`）、行銷資料（`browsing_history`/`search_history`）
- [x] 1.5 建立 `src/composables/useConsent.js` composable，提供 `canUseFeature(level)`、`guardedStorage(level)` API

## 2. Consent UI（同意提示介面）

- [x] 2.1 重構 `src/components/CookieConsent/index.vue`：移除技術參數表格，改為簡潔文案式說明
- [x] 2.2 實作雙按鈕配置：「全部接受」（主要實色按鈕）+ 「僅必要」（次要文字按鈕）
- [x] 2.3 實作「了解更多」展開區域：以自然語言描述三個類別用途（不列技術參數）
- [x] 2.4 確保響應式設計：手機端按鈕全寬排列、字體適當縮小
- [x] 2.5 使用 `--mall-primary` CSS 變數適配主題配色

## 3. 現有模組整合

- [x] 3.1 修改 `src/store/modules/mallTheme.js`：localStorage 寫入前檢查 `canUseFeature('functional')`，未同意時使用預設主題不持久化
- [x] 3.2 修改 `src/views/shop-front/auth/login.vue`：「記住帳號」功能在 `functional` 未同意時停用（隱藏勾選框或顯示提示）
- [x] 3.3 修改 `src/store/modules/cart.js`：訪客購物車歸類為功能性，未同意時不持久化至 localStorage（僅記憶體暫存）

## 4. Marketing Tracker（行銷追蹤基礎建設）

- [x] 4.1 建立 `src/composables/useMarketingTracker.js`：實作 `trackEvent(name, payload)` 統一介面，行銷未同意時靜默不執行
- [x] 4.2 實作瀏覽足跡記錄：商品頁瀏覽時記錄至 `browsing_history`（上限 50 筆，含 productId/name/timestamp）
- [x] 4.3 實作搜尋關鍵字記錄：搜尋時記錄至 `search_history`（上限 20 筆，重複關鍵字更新時間戳）
- [x] 4.4 實作 `getRecentlyViewed(limit)` 方法，行銷未同意時回傳空陣列
- [x] 4.5 實作 `registerTracker(config)` 第三方追蹤碼注入點，預留 scriptUrl 動態載入與 onEvent 轉發機制

## 5. 整合驗證

- [x] 5.1 在商品詳情頁整合 `useMarketingTracker`，同意後自動記錄瀏覽足跡
- [x] 5.2 在 ShopLayout 搜尋框整合搜尋關鍵字記錄
- [x] 5.3 端對端測試：全部接受 → 瀏覽商品 → 確認 browsing_history 有資料 → 切換為僅必要 → 確認資料被清除
- [x] 5.4 端對端測試：僅必要 → 確認主題不持久化、記住帳號停用、瀏覽足跡不記錄
