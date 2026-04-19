# ISBN 三層備援搜尋功能測試文件

## 功能概述
系統實作了三層備援搜尋機制，確保能從多個來源取得書籍資料：
1. **第一層**：台灣主站 (isbn.tw)
2. **第二層**：NiceBooks 美國站 (us.nicebooks.com)
3. **第三層**：Google Books API (googleapis.com)

## 實作內容

### 1. 介面定義 (`IIsbnCrawlerService.java`)
```java
// 主方法：自動 fallback 邏輯
BookInfoDTO crawlByIsbn(String isbn);

// 台灣主站搜尋
BookInfoDTO crawlByIsbnFromTw(String isbn);

// 美國站備援搜尋
BookInfoDTO crawlByIsbnFromUs(String isbn);

// Google Books API 搜尋（第三層備援）
BookInfoDTO crawlByIsbnFromGoogle(String isbn);
```

### 2. 搜尋流程
系統採用三層備援機制，依序嘗試：

1. **第一層（台灣站）**：
   - URL: `https://isbn.tw/{isbn}`
   - 優點：資料最完整，包含繁體中文書籍資訊
   - 缺點：僅收錄部分書籍

2. **第二層（美國站）**：
   - URL: `https://us.nicebooks.com/search/isbn?isbn={isbn}`
   - 優點：收錄大量外文書籍
   - 缺點：部分欄位可能缺失（如簡介、語言等）

3. **第三層（Google Books API）**：
   - URL: `https://www.googleapis.com/books/v1/volumes?q=isbn:{isbn}`
   - 優點：資料庫最大，涵蓋全球書籍
   - 缺點：API 回應速度較慢，部分書籍無封面圖片

### 3. 各資料來源解析欄位對照表

| 欄位 | 台灣站 | 美國站 | Google Books API |
|------|-------|-------|-----------------|
| 書名 (title) | ✅ | ✅ | ✅ |
| 作者 (author) | ✅ | ✅ | ✅ |
| 出版社 (publisher) | ✅ | ✅ | ✅ |
| 出版日期 (publishDate) | ✅ | ✅ | ✅ |
| 出版地 (publishLocation) | ✅ | ❌ | ❌ |
| 語言 (language) | ✅ | ❌ | ✅ |
| 版本 (edition) | ✅ | ❌ | ❌ |
| 裝訂 (binding) | ✅ | ✅ | ❌ |
| 分級 (classification) | ✅ | ❌ | ❌ |
| 簡介 (introduction) | ✅ | ❌ | ✅ |
| 封面圖片 (coverImage) | ✅ | ✅ | ✅ |

### 4. 特殊處理

#### 美國站圖片
- 圖片寬度自動調整為 500px（`width=240` → `width=500`）

#### Google Books API
- 語言代碼自動轉換為中文（如：`zh-cn` → `簡體中文`）
- 圖片 URL 自動轉換為 HTTPS
- 優先使用較大尺寸的圖片（large > medium > thumbnail）
- 支援多位作者（自動合併為逗號分隔字串）

## 測試方法

### 方法 1：透過掃描功能測試

#### 1.1 準備測試資料
選擇一個在台灣站找不到，但在美國站存在的 ISBN：
- **測試 ISBN**: `9787541756573`（範例書籍：動物是如何生活的）

#### 1.2 測試步驟
1. 啟動後端服務：
   ```bash
   cd /Users/cheng/IdeaProjects/R/Cheng-Vue
   mvn spring-boot:run -Dspring-boot.run.profiles=local \
     -Djasypt.encryptor.password=diDsd]3FsGO@4dido
   ```

2. 啟動前端服務：
   ```bash
   cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui
   npm run dev
   ```

3. 開啟瀏覽器訪問系統：
   - URL: `http://localhost:80`
   - 登入系統

4. 前往「掃描功能」頁面
   - 路徑：庫存管理 > 掃描功能

5. 輸入測試 ISBN：`9787541756573`
   - 可以手動輸入或使用掃描槍掃描

6. 觀察結果：
   - 查看 LOG 輸出，應該會看到三層備援流程：
     ```
     [第1層] 嘗試從台灣主站搜尋 ISBN: 9787541756573
     台灣站爬取 ISBN 資訊時發生錯誤: ...
     [第2層] 台灣主站搜尋失敗或資訊不足，切換到美國站備援搜尋 ISBN: 9787541756573
     開始從美國站爬取 ISBN 資訊: https://us.nicebooks.com/search/isbn?isbn=9787541756573
     美國站 ISBN 資訊爬取成功: 動物是如何生活的(精)
     ```
   - 若美國站也失敗，會看到第三層 Google Books API：
     ```
     [第3層] 美國站搜尋失敗或資訊不足，切換到 Google Books API 搜尋 ISBN: 9787541756573
     開始從 Google Books API 搜尋 ISBN 資訊: https://www.googleapis.com/books/v1/volumes?q=isbn:9787541756573
     Google Books API 搜尋成功: 動物是如何生活的
     ```

### 方法 2：透過 API 直接測試

#### 2.1 使用 Postman 或 curl 測試

```bash
# 測試台灣站無法找到，使用美國站備援的 ISBN
curl -X POST "http://localhost:8080/inventory/scan/process" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "scanContent": "9787541756573",
    "scanType": "MANUAL"
  }'
```

#### 2.2 預期回應
```json
{
  "code": 200,
  "msg": "掃描成功",
  "data": {
    "itemId": xxx,
    "itemName": "動物是如何生活的(精)",
    "bookInfo": {
      "isbn": "9787541756573",
      "title": "動物是如何生活的(精)",
      "author": "匿名",
      "publisher": "Future publishing House",
      "publishDate": "September 1, 2015",
      "binding": "Hardcover",
      "coverImagePath": "/profile/book-covers/isbn_9787541756573.jpg",
      "success": true
    }
  }
}
```

### 方法 3：查看資料庫記錄

執行 SQL 查詢確認書籍資訊已正確儲存：

```sql
-- 查詢書籍資訊
SELECT * FROM inv_book_info 
WHERE isbn = '9787541756573';

-- 查詢關聯的物品資訊
SELECT i.*, b.title, b.author, b.publisher, b.source_url
FROM inv_item i
LEFT JOIN inv_book_info b ON i.item_id = b.item_id
WHERE i.barcode = '9787541756573';
```

## 日誌觀察重點

### 第一層：台灣站搜尋
```
INFO  - [第1層] 嘗試從台灣主站搜尋 ISBN: 9787541756573
INFO  - 開始從台灣站爬取 ISBN 資訊: https://isbn.tw/9787541756573
```

**成功情境**：
```
INFO  - 台灣站 ISBN 資訊爬取成功: [書名]
```

**失敗情境**：
```
ERROR - 台灣站爬取 ISBN 資訊時發生錯誤: ...
```

### 第二層：自動切換到美國站
```
INFO  - [第2層] 台灣主站搜尋失敗或資訊不足，切換到美國站備援搜尋 ISBN: 9787541756573
INFO  - 開始從美國站爬取 ISBN 資訊: https://us.nicebooks.com/search/isbn?isbn=9787541756573
```

**成功情境**：
```
INFO  - 美國站 ISBN 資訊爬取成功: 動物是如何生活的(精)
INFO  - 封面圖片下載成功: /Users/cheng/uploadPath/book-covers/isbn_9787541756573.jpg
INFO  - 建立物品成功，ItemId: xxx, ISBN: 9787541756573
```

**失敗情境**：
```
ERROR - 美國站爬取 ISBN 資訊時發生錯誤: ...
```

### 第三層：自動切換到 Google Books API
```
INFO  - [第3層] 美國站搜尋失敗或資訊不足，切換到 Google Books API 搜尋 ISBN: 9787541756573
INFO  - 開始從 Google Books API 搜尋 ISBN 資訊: https://www.googleapis.com/books/v1/volumes?q=isbn:9787541756573
```

**成功情境**：
```
INFO  - Google Books API 搜尋成功: 動物是如何生活的
INFO  - 封面圖片下載成功: /Users/cheng/uploadPath/book-covers/isbn_9787541756573.jpg
INFO  - 建立物品成功，ItemId: xxx, ISBN: 9787541756573
```

**失敗情境**：
```
ERROR - 在 Google Books 找不到此 ISBN 的書籍資訊
```

## 測試案例清單

| ISBN | 預期行為 | 資料來源 |
|------|---------|----------|
| 9789571372808 | 第1層：台灣站成功 | isbn.tw |
| 9787541756573 | 第2層：台灣站失敗 → 美國站成功 | us.nicebooks.com |
| 9780134685991 | 第3層：台灣站、美國站失敗 → Google 成功 | Google Books API |
| 1234567890123 | 三層都失敗 | 無 |

## 圖片寬度驗證

美國站的封面圖片 URL 應該已將 `width` 參數調整為 500：

**原始 URL**:
```
https://images.nicebooks.com/images/96/e2/96e25c74f537c7de98d91e5fd0804fc33c2159f4?width=240&quality=90&optimizer=image
```

**調整後 URL**:
```
https://images.nicebooks.com/images/96/e2/96e25c74f537c7de98d91e5fd0804fc33c2159f4?width=500&quality=90&optimizer=image
```

## 常見問題

### Q1: 為什麼台灣站會失敗？
A: 可能原因：
- ISBN 是外文書，台灣站沒有收錄
- 網路連線問題
- 台灣站網站結構變更

### Q2: 美國站的資料會比較少嗎？
A: 是的，美國站可能缺少：
- 出版地 (publishLocation)
- 語言 (language)
- 分級 (classification)
- 簡介 (introduction)

### Q3: Google Books API 需要 API Key 嗎？
A: 不需要。我們使用的是 Google Books 公開 API，無需申請 API Key。但要注意：
- 有請求頻率限制（每日 1000 次查詢）
- 若超過限制，建議申請 API Key 以提高配額

### Q4: 如何強制只使用特定資料來源？
A: 可以直接調用對應的方法：
```java
// 只使用台灣站
BookInfoDTO bookInfo = isbnCrawlerService.crawlByIsbnFromTw(isbn);

// 只使用美國站
BookInfoDTO bookInfo = isbnCrawlerService.crawlByIsbnFromUs(isbn);

// 只使用 Google Books API
BookInfoDTO bookInfo = isbnCrawlerService.crawlByIsbnFromGoogle(isbn);
```

### Q5: 備援搜尋會影響效能嗎？
A: 影響很小：
- 只有在前一層失敗時才會嘗試下一層
- 第一層成功時，不會調用後續 API
- 每層搜尋逾時時間為 10 秒
- 最壞情況下（三層都失敗）總耗時約 30 秒

## 開發細節

### 原始碼位置
- 介面：`cheng-crawler/src/main/java/com/cheng/crawler/service/IIsbnCrawlerService.java`
- 實作：`cheng-crawler/src/main/java/com/cheng/crawler/service/impl/IsbnCrawlerServiceImpl.java`
- 使用處：`cheng-system/src/main/java/com/cheng/system/service/impl/BookItemServiceImpl.java`

### 依賴套件
- **Fastjson2**: 阿里巴巴 JSON 解析器，用於解析 Google Books API 的 JSON 回應
- **Jsoup**: 用於台灣站和 Google Books 的 HTML 爬取和 HTTP 請求
- **Selenium**: 用於美國站的動態內容爬取（處理 lazy loading 圖片）

### 環境需求
美國站使用 Selenium 需要安裝 ChromeDriver：

#### macOS
```bash
brew install chromedriver
```

#### Linux
```bash
sudo apt-get install chromium-chromedriver
```

查看 [IMAGE-FIX-SUMMARY.md](./IMAGE-FIX-SUMMARY.md) 了解詳細安裝說明

### 參考文件
- 美國站 HTML 結構：`cheng.deploy/docs/isbn-ref.html`
- Google Books API 文件：https://developers.google.com/books/docs/v1/using

## 結論

此三層備援機制確保系統能從多個來源取得書籍資訊：

✅ **可靠性提升**：三個獨立資料來源，大幅降低查詢失敗率  
✅ **資料覆蓋面廣**：涵蓋繁體中文、簡體中文、外文書籍  
✅ **自動化切換**：無需人工干預，系統自動選擇最佳資料來源  
✅ **資料完整性**：優先使用資料最完整的台灣站  
✅ **效能優化**：只在必要時才調用備援 API  

此功能大幅提升了庫存管理系統的書籍資料取得能力，確保使用者能夠快速建立書籍物品資訊。
