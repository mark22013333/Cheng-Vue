# ISBN 書籍資訊爬取與管理功能

## 功能概述

實現完整的 ISBN 掃描、書籍資訊自動爬取、物品建立與庫存管理功能，類似圖書館管理系統。

## 核心功能

### 1. ISBN 掃描與爬取
- 支援 ISBN-10 和 ISBN-13 格式
- 自動從 `https://isbn.tw/{ISBN}` 爬取書籍資訊
- 使用 Jsoup 解析 HTML 擷取以下欄位：
  - 書名 (title)
  - 作者 (author)
  - 出版社 (publisher)
  - 出版日期 (publishDate)
  - 出版地 (publishLocation)
  - 語言 (language)
  - 版本 (edition)
  - 裝訂 (binding)
  - 分級 (classification)
  - 封面圖片 (coverImage)
  - 簡介 (introduction)

### 2. 自動建立物品與庫存
- **檢查機制**：掃描 ISBN 時先檢查是否已存在
- **自動建立**：
  1. 建立物品記錄（`inv_item`）
  2. 建立書籍資訊記錄（`inv_book_info`）
  3. 初始化庫存記錄（`inv_stock`）
  4. 下載並儲存封面圖片到伺服器

### 3. 圖片下載與儲存
- 封面圖片自動下載到 `{cheng.profile}/book-covers/` 目錄
- 檔案命名格式：`isbn_{ISBN}.{副檔名}`
- 支援多種圖片格式（jpg, png, gif 等）
- 資料庫記錄完整檔案路徑

### 4. 書籍與庫存關聯
- 書籍作為特殊類型的物品存在
- 支援借出、歸還流程（使用既有的庫存借出功能）
- 可追蹤每本書的借閱狀態

## 資料庫結構

### 書籍分類（inv_category）
```sql
-- 自動建立書籍分類
category_id: 2000 - 圖書（主分類）
category_id: 2001 - 文學小說
category_id: 2002 - 商業理財
category_id: 2003 - 電腦資訊
category_id: 2004 - 語言學習
category_id: 2005 - 藝術設計
category_id: 2006 - 其他圖書
```

### 書籍資訊表（inv_book_info）
| 欄位 | 類型 | 說明 |
|------|------|------|
| book_info_id | BIGINT | 書籍資訊ID（主鍵）|
| item_id | BIGINT | 關聯物品ID |
| isbn | VARCHAR(20) | ISBN（唯一索引）|
| title | VARCHAR(200) | 書名 |
| author | VARCHAR(200) | 作者 |
| publisher | VARCHAR(100) | 出版社 |
| publish_date | VARCHAR(50) | 出版日期 |
| publish_location | VARCHAR(50) | 出版地 |
| language | VARCHAR(50) | 語言 |
| edition | VARCHAR(50) | 版本 |
| binding | VARCHAR(50) | 裝訂 |
| classification | VARCHAR(50) | 分級 |
| cover_image_path | VARCHAR(300) | 封面圖片路徑 |
| introduction | TEXT | 簡介 |
| source_url | VARCHAR(300) | 來源網址 |
| crawl_time | DATETIME | 爬取時間 |

## 後端架構

### 1. 爬蟲模組（cheng-crawler）

#### IsbnCrawlerService
```java
// 介面
IIsbnCrawlerService.crawlByIsbn(String isbn)

// 實現
IsbnCrawlerServiceImpl
- 連接 isbn.tw 網站
- 使用 Jsoup 解析 HTML
- 擷取書籍資訊並封裝為 BookInfoDTO
- 下載封面圖片
```

#### ImageDownloadUtil
```java
// 工具類別
downloadImage(String imageUrl, String savePath, String fileName)
- 下載遠端圖片到本地
- 支援多種圖片格式
- 自動建立目錄
```

### 2. 系統模組（cheng-system）

#### 實體類別（Domain）
- `InvBookInfo` - 書籍資訊實體

#### 資料存取層（Mapper）
- `InvBookInfoMapper` - 書籍資訊 Mapper 介面
- `InvBookInfoMapper.xml` - MyBatis XML 映射檔案

#### 服務層（Service）
- `IInvBookInfoService` - 書籍資訊服務介面
- `InvBookInfoServiceImpl` - 書籍資訊服務實現
- `IBookItemService` - 書籍物品整合服務介面
- `BookItemServiceImpl` - 書籍物品整合服務實現

### 3. 控制器模組（cheng-admin）

#### InvScanController
```java
// ISBN 掃描端點
POST /inventory/scan/isbn
{
  "isbn": "9789865406417"
}

// 回傳
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "item": {...},  // 物品資訊
    "message": "書籍建立成功"
  }
}
```

#### InvBookInfoController
```java
// 查詢書籍列表
GET /inventory/bookInfo/list

// 根據 ISBN 查詢
GET /inventory/bookInfo/isbn/{isbn}

// 新增/修改/刪除書籍
POST /inventory/bookInfo
PUT /inventory/bookInfo
DELETE /inventory/bookInfo/{bookInfoIds}
```

## API 端點

### 1. ISBN 掃描
**POST** `/inventory/scan/isbn`

**請求參數：**
```json
{
  "isbn": "9789865406417"
}
```

**回應範例：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "item": {
      "itemId": 10001,
      "itemCode": "BOOK-9789865406417",
      "itemName": "地表最強告解室",
      "categoryId": 2000,
      "barcode": "9789865406417",
      "imageUrl": "/Users/cheng/uploadPath/book-covers/isbn_9789865406417.jpg"
    },
    "message": "書籍建立成功"
  }
}
```

### 2. 查詢書籍資訊
**GET** `/inventory/bookInfo/isbn/{isbn}`

**回應範例：**
```json
{
  "code": 200,
  "data": {
    "bookInfoId": 1,
    "itemId": 10001,
    "isbn": "9789865406417",
    "title": "地表最強告解室",
    "author": "Cherng, 地表最強敗犬",
    "publisher": "大塊文化",
    "publishDate": "2020年01月17日",
    "language": "繁體中文",
    "coverImagePath": "/Users/cheng/uploadPath/book-covers/isbn_9789865406417.jpg",
    "introduction": "..."
  }
}
```

## 權限設定

需要在選單管理中設定以下權限：

```
inventory:scan:isbn      - ISBN 掃描權限
inventory:bookInfo:list  - 書籍列表查詢
inventory:bookInfo:query - 書籍詳細查詢
inventory:bookInfo:add   - 新增書籍
inventory:bookInfo:edit  - 修改書籍
inventory:bookInfo:remove - 刪除書籍
inventory:bookInfo:export - 匯出書籍
```

## 部署步驟

### 1. 執行 SQL 腳本
```bash
# 執行書籍資訊表建立腳本
mysql -u root -p cheng < sql/inv_book_info.sql
```

### 2. 設定檔案上傳路徑
確認 `application-local.yml` 和 `application-prod.yml` 中的路徑設定：

```yaml
# 本地環境
cheng:
  profile: /Users/cheng/uploadPath

# 正式環境  
cheng:
  profile: /opt/cool-apps/uploadFile
```

### 3. 重新啟動應用程式
```bash
# 使用 Maven 啟動
mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

## 使用流程

### 場景：掃描書籍 ISBN 並建立庫存

1. **前端掃描 ISBN**（使用 html5-qrcode 或手動輸入）
   ```javascript
   const isbn = "9789865406417";
   ```

2. **呼叫 ISBN 掃描 API**
   ```javascript
   POST /inventory/scan/isbn
   { "isbn": "9789865406417" }
   ```

3. **後端自動處理**
   - 驗證 ISBN 格式
   - 檢查書籍是否已存在
   - 若不存在，爬取 isbn.tw 取得資訊
   - 下載封面圖片
   - 建立物品記錄
   - 建立書籍資訊記錄
   - 初始化庫存（數量為 0）

4. **回傳結果給前端**
   - 顯示書籍資訊
   - 提示建立成功或已存在

5. **後續操作**
   - 入庫：增加書籍數量
   - 借出：使用既有的借出功能
   - 歸還：使用既有的歸還功能

## 優化建議

### 1. 效能優化
- ✅ 實現 ISBN 查詢快取機制
- ✅ 批次處理多本書籍掃描
- ✅ 圖片壓縮處理

### 2. 功能擴充
- 支援更多書籍資料來源（如博客來、誠品等）
- 書籍推薦系統
- 借閱統計報表
- 書籍評分與評論

### 3. 異常處理
- 網路連線失敗重試機制
- 爬蟲反爬蟲策略
- 圖片下載失敗的備用方案

## 檔案清單

### SQL 腳本
- `/sql/inv_book_info.sql` - 書籍資訊表與分類初始化

### 後端檔案
#### cheng-crawler
- `/cheng-crawler/src/main/java/com/cheng/crawler/dto/BookInfoDTO.java`
- `/cheng-crawler/src/main/java/com/cheng/crawler/service/IIsbnCrawlerService.java`
- `/cheng-crawler/src/main/java/com/cheng/crawler/service/impl/IsbnCrawlerServiceImpl.java`
- `/cheng-crawler/src/main/java/com/cheng/crawler/util/ImageDownloadUtil.java`

#### cheng-system
- `/cheng-system/src/main/java/com/cheng/system/domain/InvBookInfo.java`
- `/cheng-system/src/main/java/com/cheng/system/mapper/InvBookInfoMapper.java`
- `/cheng-system/src/main/java/com/cheng/system/service/IInvBookInfoService.java`
- `/cheng-system/src/main/java/com/cheng/system/service/impl/InvBookInfoServiceImpl.java`
- `/cheng-system/src/main/java/com/cheng/system/service/IBookItemService.java`
- `/cheng-system/src/main/java/com/cheng/system/service/impl/BookItemServiceImpl.java`
- `/cheng-system/src/main/resources/mapper/system/InvBookInfoMapper.xml`

#### cheng-admin
- `/cheng-admin/src/main/java/com/cheng/web/controller/inventory/InvScanController.java`
- `/cheng-admin/src/main/java/com/cheng/web/controller/inventory/InvBookInfoController.java`

## 測試範例

### 測試 ISBN
- `9789865406417` - 地表最強告解室
- `9789571375304` - 原子習慣
- `9789863208891` - 人類大歷史

### 測試指令
```bash
# 測試 ISBN 掃描
curl -X POST http://localhost:8080/inventory/scan/isbn \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9789865406417"}'

# 查詢書籍資訊
curl http://localhost:8080/inventory/bookInfo/isbn/9789865406417
```

## 注意事項

1. **爬蟲禮節**：請遵守 isbn.tw 的使用規範，避免頻繁爬取造成伺服器負擔
2. **圖片版權**：下載的封面圖片僅供內部系統使用
3. **資料準確性**：爬取的資料可能不完整，需人工複核
4. **磁碟空間**：定期清理未使用的書籍封面圖片
5. **備份機制**：定期備份書籍資訊資料表

## 聯絡資訊

如有問題或建議，請聯繫開發團隊。

---

**開發完成日期**：2025-10-03  
**版本**：v1.0.0  
**作者**：cheng
