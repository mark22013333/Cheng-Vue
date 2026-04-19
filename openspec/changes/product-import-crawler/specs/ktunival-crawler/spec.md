## ADDED Requirements

### Requirement: 條碼搜尋商品
系統 SHALL 使用條碼編號搜尋 ktunival.com.tw，解析搜尋結果頁面，取得符合的商品數量和商品詳情頁 URL。網頁編碼為 Big5。

#### Scenario: 搜到 1 筆商品
- **WHEN** 條碼 "4975163325016" 搜尋結果為 1 筆
- **THEN** 系統取得商品詳情頁 URL（如 `product.asp?productid=612`）

#### Scenario: 搜到 0 筆商品
- **WHEN** 條碼搜尋結果為 0 筆
- **THEN** 系統記錄 NOT_FOUND 到 crawl_import_log，跳過此商品

#### Scenario: 搜到多筆商品
- **WHEN** 條碼搜尋結果超過 1 筆
- **THEN** 系統記錄 MULTIPLE 到 crawl_import_log，跳過此商品

### Requirement: 解析商品詳情頁
系統 SHALL 從商品詳情頁解析：產品名稱、產品類別、商品圖片 URL、產品介紹。

#### Scenario: 成功解析詳情頁
- **WHEN** 存取 `product.asp?productid=612`
- **THEN** 系統取得產品名稱（如 "Prostaff輪胎泡沫清潔劑"）、類別（如 "化工用品類"）、圖片 URL、介紹文字

#### Scenario: 詳情頁不可用
- **WHEN** 詳情頁返回錯誤或無法解析
- **THEN** 系統記錄 ERROR 到 crawl_import_log

### Requirement: 下載商品圖片
系統 SHALL 從供應商網站下載商品圖片到本地 `cheng.profile` 路徑下的 `upload/product/` 子目錄。圖片 URL 中的反斜線 SHALL 轉為正斜線。

#### Scenario: 成功下載圖片
- **WHEN** 圖片 URL 有效
- **THEN** 圖片下載到 `{cheng.profile}/upload/product/{timestamp}_{filename}.jpg`，回傳相對路徑

#### Scenario: 圖片下載失敗
- **WHEN** 圖片 URL 無效或下載超時
- **THEN** 系統記錄 IMG_FAIL 到 crawl_import_log，商品仍然建立但 main_image 為空

### Requirement: 請求頻率控制
系統 SHALL 在每次 HTTP 請求之間等待 1-2 秒，避免對供應商網站造成過大負擔。

#### Scenario: 請求間隔
- **WHEN** 連續爬取多筆商品
- **THEN** 每次 HTTP 請求（搜尋頁或詳情頁）之間至少間隔 1 秒

### Requirement: 失敗重試
系統 SHALL 在 HTTP 請求失敗時重試最多 3 次，每次重試間隔 2 秒。

#### Scenario: 第一次失敗後重試成功
- **WHEN** 第一次請求超時，第二次成功
- **THEN** 系統正常取得資料，不記錄錯誤

#### Scenario: 3 次重試均失敗
- **WHEN** 連續 3 次請求都失敗
- **THEN** 系統記錄 ERROR 到 crawl_import_log，繼續處理下一筆
