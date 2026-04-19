# Books.com.tw 智能降級方案

## 🎯 功能說明

當 FlareSolver 無法成功下載 isbn.tw 的封面圖片時，自動切換到 books.com.tw 搜尋並下載封面圖片。

---

## 📋 降級觸發條件

### 主流程
```
下載 isbn.tw 圖片
└─ FlareSolver 處理
   ├─ ✅ 成功 → 下載圖片
   └─ ❌ 失敗 → 觸發降級方案
       └─ 從 books.com.tw 搜尋書名
```

### 觸發情境
1. **FlareSolver 訪問失敗** - 無法處理 Cloudflare 驗證
2. **FlareSolver 異常** - 拋出任何異常
3. **圖片 URL 重定向失敗** - 無法取得最終圖片 URL

---

## 🔄 降級方案流程

### 步驟 1: 建立搜尋 URL
```java
String encodedTitle = URLEncoder.encode(bookTitle.trim(), "UTF-8");
String searchUrl = String.format(
    "https://search.books.com.tw/search/query/cat/1/sort/1/v/1/page/1/spell/3/ms2/ms2_1/key/%s",
    encodedTitle
);
```

**範例**:
- 書名: `米米玩收拾`
- 搜尋 URL: `https://search.books.com.tw/search/query/cat/1/sort/1/v/1/page/1/spell/3/ms2/ms2_1/key/%E7%B1%B3%E7%B1%B3%E7%8E%A9%E6%94%B6%E6%8B%BE`

### 步驟 2: 解析搜尋結果
使用 Jsoup 取得搜尋結果頁面並解析圖片元素：
```java
Document doc = Jsoup.connect(searchUrl)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .timeout(10000)
        .get();

// 找到所有圖片元素
Elements images = doc.select("img.b-lazy.b-loaded");
```

**HTML 結構參考**:
```html
<img class="b-lazy b-loaded"
     src="https://im1.book.com.tw/image/getImage?i=https://www.books.com.tw/img/001/093/66/0010936682.jpg&amp;w=187&amp;h=187&amp;v=63299639"
     alt="米米玩收拾 (掃碼聽雙語故事、精裝圓角)"
     srcset="...">
```

### 步驟 3: 智能匹配書名
優先尋找 alt 屬性包含書名的圖片：
```java
Element targetImage = null;
for (Element img : images) {
    String alt = img.attr("alt");
    if (alt != null && alt.contains(bookTitle)) {
        targetImage = img;
        log.info("找到匹配書名的圖片: alt='{}'", alt);
        break;
    }
}

// 如果沒有找到匹配的，使用第一個
if (targetImage == null) {
    targetImage = images.first();
    log.info("使用第一個圖片元素: alt='{}'", targetImage.attr("alt"));
}
```

**匹配邏輯**:
| alt 內容 | 書名 | 匹配結果 |
|---------|------|---------|
| `米米玩收拾 (掃碼聽雙語故事、精裝圓角)` | `米米玩收拾` | ✅ 優先使用 |
| `米米玩收拾(圓角大書、兒歌CD與樂譜)` | `米米玩收拾` | ✅ 優先使用 |
| `其他無關書籍` | `米米玩收拾` | ❌ 不匹配 |

### 步驟 4: 提取真實圖片 URL

books.com.tw 的圖片 URL 格式：
```
https://im1.book.com.tw/image/getImage?i=https://www.books.com.tw/img/001/093/66/0010936682.jpg&w=187&h=187&v=63299639
                                       ↑                                                      ↑
                                    i= 參數                                                .jpg
```

需要提取 `i=` 參數後的真實 URL（到 `.jpg` 為止）：
```java
private String extractRealImageUrl(String src) {
    // 找到 i= 參數
    int startIndex = src.indexOf("i=");
    if (startIndex == -1) return null;
    
    startIndex += 2; // 跳過 "i="
    
    // 找到圖片副檔名（.jpg, .jpeg, .png, .gif, .webp）
    int endIndex = -1;
    String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    for (String ext : imageExtensions) {
        int idx = src.indexOf(ext, startIndex);
        if (idx != -1) {
            endIndex = idx + ext.length();
            break;
        }
    }
    
    if (endIndex == -1) return null;
    
    // 提取並處理 HTML 實體編碼
    String realUrl = src.substring(startIndex, endIndex);
    realUrl = realUrl.replace("&amp;", "&");
    
    return realUrl;
}
```

**提取範例**:
| 輸入 src | 提取結果 |
|---------|---------|
| `https://im1.book.com.tw/image/getImage?i=https://www.books.com.tw/img/001/093/66/0010936682.jpg&w=187&h=187&v=63299639` | `https://www.books.com.tw/img/001/093/66/0010936682.jpg` |
| `https://im1.book.com.tw/image/getImage?i=https://www.books.com.tw/img/001/070/58/0010705830.jpg&amp;w=187&amp;h=187&amp;v=56ab3f58` | `https://www.books.com.tw/img/001/070/58/0010705830.jpg` |

### 步驟 5: 下載並儲存圖片
```java
String bookCoverPath = uploadPath + "/book-covers";
String fileName = "isbn_" + isbn;

String savedPath = ImageDownloadUtil.downloadImage(realImageUrl, bookCoverPath, fileName);

if (savedPath != null && savedPath.contains("/book-covers/")) {
    String relativePath = "/profile/book-covers/" +
            savedPath.substring(savedPath.lastIndexOf("/") + 1);
    log.info("從 books.com.tw 下載封面圖片成功: {}", relativePath);
    return relativePath;
}
```

---

## 📊 完整降級流程圖

```
┌─────────────────────────────────────────────────┐
│ 步驟 1: 嘗試從 isbn.tw 下載圖片                 │
│   使用 FlareSolver 處理 Cloudflare              │
└──────────────────┬──────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         │                   │
    ✅ 成功              ❌ 失敗
         │                   │
         v                   v
    下載圖片         ┌─────────────────────────────┐
         │           │ 步驟 2: 降級到 books.com.tw │
         │           │   搜尋書名                   │
         │           └──────────┬──────────────────┘
         │                      │
         │                      v
         │           ┌─────────────────────────────┐
         │           │ 步驟 3: 解析搜尋結果        │
         │           │   找到圖片元素               │
         │           └──────────┬──────────────────┘
         │                      │
         │                      v
         │           ┌─────────────────────────────┐
         │           │ 步驟 4: 智能匹配書名        │
         │           │   優先使用匹配的圖片         │
         │           └──────────┬──────────────────┘
         │                      │
         │                      v
         │           ┌─────────────────────────────┐
         │           │ 步驟 5: 提取真實圖片 URL    │
         │           │   解析 i= 參數               │
         │           └──────────┬──────────────────┘
         │                      │
         │                      v
         │           ┌─────────────────────────────┐
         │           │ 步驟 6: 下載圖片            │
         │           └──────────┬──────────────────┘
         │                      │
         └──────────────────────┴───────────────────┐
                                                     │
                                                     v
                                            ┌────────────────┐
                                            │ 完成！         │
                                            │ 封面圖片已下載 │
                                            └────────────────┘
```

---

## 🎁 優勢分析

### 1. **高可用性**
- ✅ 雙重來源保障
- ✅ 一個失敗自動切換另一個
- ✅ 大幅提升圖片下載成功率

### 2. **智能匹配**
- ✅ 優先使用書名完全匹配的圖片
- ✅ 降低錯誤圖片的可能性
- ✅ 提升圖片準確度

### 3. **穩定可靠**
- ✅ books.com.tw 無 Cloudflare 驗證
- ✅ 直接使用 Jsoup，無需 WebDriver
- ✅ 效能更好，成功率更高

### 4. **詳細日誌**
```
INFO  使用 FlareSolver 處理圖片下載...
WARN  FlareSolver 訪問圖片 URL 失敗，嘗試從 books.com.tw 搜尋
INFO  從 books.com.tw 搜尋書籍封面: 米米玩收拾
INFO  找到 2 個圖片元素
INFO  找到匹配書名的圖片: alt='米米玩收拾 (掃碼聽雙語故事、精裝圓角)'
INFO  成功提取圖片 URL: https://www.books.com.tw/img/001/093/66/0010936682.jpg
INFO  從 books.com.tw 下載封面圖片成功: /profile/book-covers/isbn_9789863877363.jpg
```

---

## 🔍 技術細節

### URL 編碼處理
```java
// 書名可能包含中文、空格、特殊字元
String encodedTitle = java.net.URLEncoder.encode(bookTitle.trim(), "UTF-8");

// 範例
"米米玩收拾" → "%E7%B1%B3%E7%B1%B3%E7%8E%A9%E6%94%B6%E6%8B%BE"
"Harry Potter" → "Harry+Potter"
```

### HTML 實體編碼處理
```java
// books.com.tw 的 src 屬性中可能包含 &amp;
realUrl = realUrl.replace("&amp;", "&");

// 範例
"...jpg&amp;w=187" → "...jpg&w=187"
```

### 支援多種圖片格式
```java
String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
```

### CSS 選擇器
```java
// 選擇同時有 b-lazy 和 b-loaded 的 img 元素
Elements images = doc.select("img.b-lazy.b-loaded");
```

---

## 📁 相關檔案

| 檔案 | 說明 |
|------|------|
| `IsbnCrawlerServiceImpl.java` | 主要實作 |
| `searchAndDownloadFromBooksComTw()` | 降級搜尋方法 |
| `extractRealImageUrl()` | URL 提取方法 |
| `book2.html` | HTML 結構參考 |

---

## 🧪 測試案例

### 測試案例 1: FlareSolver 失敗
**輸入**:
- 書名: `米米玩收拾`
- ISBN: `9789863877363`
- FlareSolver: 失敗

**預期結果**:
1. 自動觸發降級
2. 搜尋 books.com.tw
3. 找到匹配書名的圖片
4. 成功下載封面

### 測試案例 2: 書名部分匹配
**輸入**:
- 書名: `米米玩收拾`
- 搜尋結果包含:
  - `米米玩收拾 (掃碼聽雙語故事、精裝圓角)` ✅
  - `米米玩收拾(圓角大書、兒歌CD與樂譜)` ✅

**預期結果**:
- 優先使用第一個匹配的圖片

### 測試案例 3: 無匹配結果
**輸入**:
- 書名: `特殊書籍名稱`
- 搜尋結果: 無匹配

**預期結果**:
- 使用第一個圖片元素
- 記錄警告日誌

---

## ✅ 驗證清單

- [x] 編譯成功
- [x] URL 編碼正確
- [x] HTML 解析正確
- [x] 圖片 URL 提取正確
- [x] 智能匹配邏輯正確
- [x] 錯誤處理完善
- [x] 日誌記錄清晰
- [x] 支援多種圖片格式

---

**實作完成時間**: 2025-11-05 10:08  
**改進類型**: 降級機制優化  
**影響範圍**: isbn.tw (第一層) 爬蟲圖片下載  
**向下相容**: ✅ 完全相容，不影響現有功能
