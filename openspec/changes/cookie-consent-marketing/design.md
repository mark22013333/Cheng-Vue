## Context

CoolApps 商城目前的 CookieConsent 元件以技術導向方式展示所有 Cookie 參數（名稱、類型、保留時間），缺乏拒絕選項。現有的 Cookie/Storage 使用包括：
- **必要**：`Member-Token`（登入 JWT）、`oauth_*`（第三方登入暫存）
- **功能性**：`username`/`password`/`rememberMe`（記住帳號）、`guest_cart`（訪客購物車）、`mall_theme`（主題偏好）
- **行銷**：目前無，但需預留架構

前端技術棧：Vue 3 + Pinia + Element Plus + Vite，無 SSR。

## Goals / Non-Goals

**Goals:**
- 簡化 Cookie 提示為使用者友善的文案式說明，不暴露技術參數
- 提供「全部接受」與「僅必要」兩個明確選項
- 建立三層同意架構（必要/功能性/行銷），透過 Pinia store + composable 統一管理
- 當使用者拒絕功能性 Cookie 時，禁止寫入偏好相關的 Cookie/localStorage
- 預留行銷追蹤注入點，使未來接入 GA4、LINE Tag、Facebook Pixel 不需重構架構

**Non-Goals:**
- 不在此階段實際接入任何第三方行銷工具（僅建架構）
- 不實作 GDPR 完整合規（如跨境資料傳輸聲明）
- 不建立後端 API 記錄同意狀態（前端 localStorage 即可）
- 不實作細粒度的逐項勾選（簡化為兩個按鈕即可）

## Decisions

### 1. 同意層級設計：三層模型

**選擇**：必要（essential）/ 功能性（functional）/ 行銷（marketing）三層

| 層級 | 接受後啟用 | 拒絕後行為 |
|------|-----------|-----------|
| 必要 | `Member-Token`、`oauth_*` | 永遠啟用，不可拒絕 |
| 功能性 | `username`/`password`/`rememberMe`、`guest_cart`、`mall_theme` | 不記錄偏好，每次都用預設主題，記住帳號功能停用 |
| 行銷 | 瀏覽足跡、搜尋記錄、第三方追蹤碼 | 不追蹤任何行為 |

**理由**：相比只分「必要/非必要」兩層，三層模型讓使用者可以接受功能性但拒絕行銷，提高整體同意率。但 UI 上「全部接受」= 三層全開、「僅必要」= 只開必要層，簡化使用者決策。

**替代方案**：
- 兩層（必要/全部）：太粗糙，無法區分功能性與行銷
- 逐項勾選：過於複雜，降低同意率

### 2. 狀態管理：Pinia Store + Composable

**選擇**：建立 `useConsentStore`（Pinia）管理同意狀態，搭配 `useConsent()` composable 提供便捷 API

```
useConsentStore (Pinia)
├── state: { essential: true, functional: boolean, marketing: boolean }
├── actions: acceptAll(), acceptEssentialOnly(), isAllowed(level)
└── persist: localStorage('cookie_consent')

useConsent() (composable)
├── canUseFeature(level) → boolean
├── onConsentChange(callback) → watcher
└── guardedStorage(level) → { get, set, remove }
```

**理由**：Pinia 提供響應式狀態，當同意狀態改變時，所有依賴的元件自動更新。Composable 提供簡潔的 API 避免各模組直接操作 store。

**替代方案**：
- 純 localStorage + 事件：缺乏響應式，各元件需自行監聽
- Provide/Inject：無法在 store 層級使用

### 3. 行銷追蹤架構：Event Bus + Script Injector 模式

**選擇**：建立 `useMarketingTracker` composable，內含：
- **瀏覽足跡記錄器**：記錄商品瀏覽、搜尋關鍵字、分類瀏覽到 localStorage（`browsing_history`）
- **第三方腳本注入器**：預留 `injectTrackingScript(scriptConfig)` 方法，未來可動態載入 GA4/LINE Tag
- **事件發送器**：統一的 `trackEvent(name, payload)` 介面，未來可橋接到任何分析平台

**理由**：先建資料收集層，日後接第三方工具時只需加 adapter，不需改動業務邏輯。

**替代方案**：
- 直接嵌入 GA4 SDK：過早綁定特定平台，未來替換成本高
- 後端 event streaming：增加後端複雜度，此階段不需要

### 4. UI 設計：簡約底部橫幅

**選擇**：精簡文案 + 兩個按鈕（「全部接受」主按鈕 + 「僅必要」次要連結），可展開簡要分類說明（以自然語言描述三個類別用途，不列技術參數）

**理由**：研究顯示，簡潔的 Cookie 提示有更高的「全部接受」點擊率。主按鈕引導接受，次要連結提供拒絕但不強調。

## Risks / Trade-offs

- **[功能降級體驗]** → 拒絕功能性 Cookie 後，使用者每次都要重選主題、無法記住帳號。透過首次使用提示（toast）告知使用者「啟用 Cookie 可獲得更好體驗」作為軟性引導。
- **[行銷資料準確性]** → 瀏覽足跡存在 localStorage，使用者可手動清除或換裝置就失效。此階段可接受，未來登入使用者可考慮後端儲存。
- **[同意率]** → UI 設計刻意將「全部接受」設為主要按鈕（實色），「僅必要」設為次要（文字連結），可能被認為是 dark pattern。透過確保兩個選項都清晰可見來平衡。
- **[既有使用者遷移]** → 舊版已接受的使用者（localStorage 有 `cookie_consent_accepted`），需遷移為新的同意格式。偵測到舊 key 時視為已全部接受並轉換。
