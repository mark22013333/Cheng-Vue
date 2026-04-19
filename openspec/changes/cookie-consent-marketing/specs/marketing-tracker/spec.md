## ADDED Requirements

### Requirement: 瀏覽足跡記錄
當使用者同意行銷層級後，系統 SHALL 記錄使用者的瀏覽足跡至 localStorage key `browsing_history`，包含瀏覽的商品 ID、商品名稱、瀏覽時間。記錄上限為最近 50 筆。

#### Scenario: 同意行銷後瀏覽商品頁
- **WHEN** 使用者已同意行銷 Cookie 且進入商品詳情頁
- **THEN** 系統記錄該商品 ID、名稱、當前時間戳至 `browsing_history`

#### Scenario: 未同意行銷時瀏覽商品頁
- **WHEN** 使用者未同意行銷 Cookie 且進入商品詳情頁
- **THEN** 系統不記錄任何瀏覽資料

#### Scenario: 記錄超過上限
- **WHEN** `browsing_history` 已有 50 筆記錄且新增一筆
- **THEN** 移除最舊的一筆，保持總數 50 筆

### Requirement: 搜尋關鍵字記錄
當使用者同意行銷層級後，系統 SHALL 記錄使用者的搜尋關鍵字至 localStorage key `search_history`，包含關鍵字與搜尋時間。記錄上限為最近 20 筆。

#### Scenario: 同意行銷後搜尋商品
- **WHEN** 使用者已同意行銷 Cookie 且在搜尋框輸入關鍵字並送出
- **THEN** 系統記錄該關鍵字與時間戳至 `search_history`

#### Scenario: 重複搜尋相同關鍵字
- **WHEN** 使用者搜尋與最近一筆相同的關鍵字
- **THEN** 更新該筆記錄的時間戳，不新增重複項目

### Requirement: 統一事件追蹤介面
系統 SHALL 提供 `trackEvent(name, payload)` 方法作為統一的事件追蹤介面。當行銷層級未同意時，該方法 SHALL 靜默不執行。此介面預留供未來第三方追蹤工具（GA4、LINE Tag、Facebook Pixel）接入。

#### Scenario: 行銷已同意時觸發事件
- **WHEN** 行銷層級已同意且呼叫 `trackEvent('view_product', { productId: 123 })`
- **THEN** 事件被記錄，未來可轉發至第三方追蹤平台

#### Scenario: 行銷未同意時觸發事件
- **WHEN** 行銷層級未同意且呼叫 `trackEvent('view_product', { productId: 123 })`
- **THEN** 方法靜默返回，不執行任何操作

### Requirement: 第三方追蹤碼注入點
系統 SHALL 提供 `registerTracker(config)` 方法，允許未來注冊第三方追蹤工具。config 包含 `name`、`scriptUrl`（可選）、`onEvent` callback。註冊的追蹤器僅在行銷層級同意後才載入腳本並接收事件。

#### Scenario: 未來注冊 GA4
- **WHEN** 開發者呼叫 `registerTracker({ name: 'ga4', scriptUrl: 'https://...', onEvent: (name, payload) => gtag('event', name, payload) })`
- **THEN** 系統記錄該追蹤器，當行銷層級同意時動態載入 scriptUrl 並開始轉發事件

#### Scenario: 行銷層級從同意改為拒絕
- **WHEN** 使用者變更同意設定，行銷層級從 `true` 變為 `false`
- **THEN** 系統停止向所有已註冊追蹤器轉發事件（不移除已載入的腳本，但不再發送資料）

### Requirement: 瀏覽資料可用於推薦
系統 SHALL 提供 `getRecentlyViewed(limit)` 方法，回傳最近瀏覽的商品列表，可供首頁或商品頁的「最近瀏覽」推薦區塊使用。當行銷層級未同意時回傳空陣列。

#### Scenario: 取得最近瀏覽商品
- **WHEN** 行銷已同意且 `browsing_history` 有 10 筆記錄，呼叫 `getRecentlyViewed(5)`
- **THEN** 回傳最近 5 筆瀏覽記錄（依時間倒序）

#### Scenario: 行銷未同意時取得瀏覽記錄
- **WHEN** 行銷未同意且呼叫 `getRecentlyViewed(5)`
- **THEN** 回傳空陣列 `[]`
