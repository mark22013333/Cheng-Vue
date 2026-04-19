# Cloudflare Turnstile 驗證處理指南

## 問題說明

`isbn.tw` 網站使用 **Cloudflare Turnstile** 進行反爬蟲驗證，這是一種自動化的人機驗證機制。

## 改進內容

### 1. 修正滑鼠移動錯誤
- ❌ 原本：使用 Selenium Actions，容易超出範圍
- ✅ 現在：使用 JavaScript 事件模擬，避免座標問題

### 2. 增加驗證等待時間
- ❌ 原本：10 秒
- ✅ 現在：20 秒（Cloudflare Turnstile 通常需要 5-15 秒自動驗證）

### 3. 優化驗證偵測
- ✅ 偵測驗證頁面元素
- ✅ 偵測「驗證成功」訊息
- ✅ 偵測頁面是否已跳轉（直接檢查書名元素）
- ✅ 偵測 Turnstile iframe

### 4. Cookie 管理
- ✅ 驗證成功後自動儲存 Cookie（7 天有效）
- ✅ 下次訪問自動載入 Cookie，無需重複驗證

## 測試方式

### 方式一：自動驗證（推薦）
程式會自動等待 Cloudflare Turnstile 驗證完成（20 秒內）。

**預期日誌：**
```
偵測到 Cloudflare 驗證頁面，等待自動驗證...
偵測到 Turnstile 驗證框，等待自動驗證... (1)
Cloudflare 驗證進行中... (2)
Cloudflare 驗證已通過，頁面已跳轉到書籍資訊頁！
成功儲存 3 個 Cookie
```

### 方式二：手動建立有效 Cookie（開發環境）

如果自動驗證持續失敗，可以手動通過驗證一次：

#### Step 1：在本地瀏覽器手動訪問
1. 開啟瀏覽器訪問 `https://isbn.tw/9789863876212`
2. 完成 Cloudflare 驗證（通常會自動完成）
3. 確認看到書籍資訊頁面

#### Step 2：匯出 Cookie
使用瀏覽器開發者工具：
1. 按 F12 開啟開發者工具
2. 切換到 Console 標籤
3. 執行以下 JavaScript 取得 Cookie：

```javascript
document.cookie.split(';').forEach(cookie => {
    const [name, value] = cookie.trim().split('=');
    console.log(`${name}: ${value}`);
});
```

#### Step 3：手動建立 Cookie 檔案
建立檔案 `/tmp/selenium-cookies/isbn.tw.json`：

```json
{
  "savedAt": "2025-11-04 14:30:00",
  "cookies": [
    {
      "name": "cf_clearance",
      "value": "YOUR_CF_CLEARANCE_VALUE",
      "domain": ".isbn.tw",
      "path": "/",
      "expiry": null,
      "isSecure": true,
      "isHttpOnly": true
    }
  ]
}
```

**重要**：將 `YOUR_CF_CLEARANCE_VALUE` 替換為實際的 `cf_clearance` Cookie 值。

#### Step 4：測試
再次執行爬蟲，應該會看到：
```
成功載入先前的 Cookie，可能無需重新驗證
未偵測到驗證頁面，Cookie 可能仍有效，直接載入書籍資訊
```

## 為什麼 Cloudflare 驗證會失敗？

### 可能原因：
1. **Docker 容器環境被識別**
   - Cloudflare 可能偵測到執行在 Docker 中的無頭瀏覽器
   - 解決方案：使用有效的 Cookie

2. **IP 地址信譽**
   - 如果 IP 被標記為可疑，驗證會更嚴格
   - 解決方案：使用本地 IP 或信譽良好的 IP

3. **瀏覽器指紋**
   - Cloudflare 會檢查瀏覽器指紋、Canvas、WebGL 等
   - 解決方案：使用更進階的反偵測技術（如 undetected-chromedriver）

4. **驗證需要互動**
   - 某些情況下 Turnstile 需要點擊確認框
   - 解決方案：增加手動驗證步驟或使用驗證服務

## 多層備援機制

即使台灣站驗證失敗，系統會自動切換到備援來源：

```
第1層：isbn.tw（台灣站）
  ↓ 20秒內驗證失敗
第2層：us.nicebooks.com（美國站）
  ↓ 失敗
第3層：Google Books API
```

## 長期解決方案

### 選項 1：使用代理服務
使用第三方 Cloudflare 繞過服務（例如：FlareSolverr）

### 選項 2：優先使用備援來源
如果台灣站持續失敗率高，可以考慮：
- 優先使用美國站或 Google Books API
- 只在特定情況下使用台灣站

### 選項 3：人工輔助驗證
- 開發管理介面，讓使用者手動通過驗證
- 系統自動保存並使用 Cookie

## 監控建議

可以在系統中加入統計：
- 台灣站成功率
- 美國站成功率  
- Google Books API 成功率
- 平均響應時間

根據統計數據動態調整策略。

## 相關檔案

- Cookie 儲存位置：`/tmp/selenium-cookies/isbn.tw.json`
- Cookie 有效期：7 天
- 工具類：
  - `CookieManager.java` - Cookie 管理
  - `HumanBehaviorSimulator.java` - 真人行為模擬
  - `IsbnCrawlerServiceImpl.java` - 爬蟲主邏輯
