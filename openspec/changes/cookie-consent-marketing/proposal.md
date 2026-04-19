## Why

目前 CookieConsent 元件以技術導向方式列出所有 Cookie 參數名稱與細節，對一般使用者不友善且缺乏拒絕選項。更重要的是，現行設計完全忽略了「使用者同意」所帶來的商業價值——當使用者主動接受 Cookie，代表我們獲得了合法的行為追蹤授權，可用於個人化推薦、再行銷、瀏覽分析等，這是電商成長的關鍵驅動力。

## What Changes

- **簡化 Cookie 提示**：移除技術參數表格，改為簡潔的文案式說明，僅告知「我們使用 Cookie 來改善您的購物體驗」
- **新增拒絕選項**：使用者可選擇「接受」或「僅必要」，拒絕後不記錄任何偏好設定（主題、記住帳號等），僅保留登入必要的 session Cookie
- **建立同意層級架構**：將 Cookie/Storage 分為「必要」「功能性」「行銷」三個層級，依據使用者同意程度啟用不同功能
- **行銷能力基礎建設**：當使用者接受行銷 Cookie 後，啟用瀏覽記錄追蹤、個人化推薦、再行銷標籤等能力，為未來接入 GA4、Facebook Pixel、LINE Tag 等行銷工具預留架構

## Capabilities

### New Capabilities
- `consent-manager`: Cookie 同意管理器——統一管理使用者同意狀態（必要/功能性/行銷），提供響應式 API 供各模組查詢同意層級，控制 Cookie/localStorage 的讀寫權限
- `consent-ui`: Cookie 同意提示介面——簡潔友善的底部橫幅，支援「全部接受」「僅必要」兩個選項，可展開查看簡要分類說明（非技術參數）
- `marketing-tracker`: 行銷追蹤基礎建設——當使用者同意行銷 Cookie 後，記錄瀏覽足跡（瀏覽商品、停留時間、搜尋關鍵字），為個人化推薦與再行銷提供資料基礎，預留第三方追蹤碼注入點

### Modified Capabilities

（無現有 spec 需要修改）

## Impact

- **前端元件**：重構 `CookieConsent/index.vue`，新增 `composables/useConsent.js` 管理同意狀態
- **Store 模組**：`mallTheme.js`、`cart.js` 需依據同意層級決定是否寫入 localStorage
- **登入模組**：`login.vue` 的「記住帳號/密碼」功能需檢查功能性 Cookie 同意
- **未來擴展**：預留 `useMarketingTracker` composable，後續可整合 GA4、LINE Tag、Facebook Pixel
- **商業模型影響**：
  - 接受行銷 Cookie 的使用者 → 可追蹤瀏覽行為 → 個人化推薦 → 提升轉換率
  - 拒絕的使用者 → 僅提供基本購物功能，不記錄偏好 → 體驗較陽春但尊重隱私
