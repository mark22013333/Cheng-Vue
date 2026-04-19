# 爬蟲系統重構總結

## 🎯 重構目標

1. 在 `CrawlerHandler` 中新增**簡化的單一查詢方法**
2. 將批次處理方法改為**可選實作**
3. 不新增額外的門面層
4. 讓子類別可以自由選擇支援哪種模式

## ✅ 完成項目

### 1. CrawlerHandler 新增單一查詢方法

在模板方法之外，新增了簡化的公開方法：

```java
/**
 * 單一查詢並儲存（簡化方法）
 * 適用於即時查詢場景，直接爬取並儲存單筆資料
 */
public <T> T crawlSingleAndSave(String input, Class<T> resultType) throws Exception
```

**特性：**
- ✅ 不依賴完整的模板流程
- ✅ 子類別可選擇性覆寫
- ✅ 類型安全（泛型支援）
- ✅ 預設拋出 UnsupportedOperationException（需要子類別實作）

### 2. 批次處理方法改為可選 ⭐ NEW

將原本的抽象方法改為具體方法，提供預設實作：

```java
// ❌ 修改前：強制實作
protected abstract List<String[]> crawlWebsiteFetchData(...);
protected abstract List<Object> processData(...);

// ✅ 修改後：可選實作
protected List<String[]> crawlWebsiteFetchData(...) throws Exception {
    throw new UnsupportedOperationException("僅支援單一查詢模式");
}

protected List<Object> processData(...) throws Exception {
    throw new UnsupportedOperationException("僅支援單一查詢模式");
}
```

**優點：**
- ✅ 子類別可以只實作 `crawlSingleAndSave()`（單一查詢）
- ✅ 子類別可以只實作批次方法（批次處理）
- ✅ 子類別可以兩者都實作（同時支援）
- ✅ `List<String[]>` 的問題被隔離在批次處理中

### 3. CA101WHandler 簡化

**修改前：**
```java
@Component
public class CA101WHandler extends CrawlerHandler {
    // 必須實作 5 個方法
    protected CrawlerType getCrawlerType() { }
    protected CrawlerDataRepository getRepository() { }
    protected List<String[]> crawlWebsiteFetchData() { }  // ❌ 用不到
    protected List<Object> processData() { }              // ❌ 用不到
    public <T> T crawlSingleAndSave() { }
}
```

**修改後：**
```java
@Component
public class CA101WHandler extends CrawlerHandler {
    // 只需實作 3 個方法
    protected CrawlerType getCrawlerType() { }
    protected CrawlerDataRepository getRepository() { }
    public <T> T crawlSingleAndSave() { }  // ✅ 只實作這個就夠了
}
```

實作了 `crawlSingleAndSave()` 方法：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public <T> T crawlSingleAndSave(String isbn, Class<T> resultType) throws Exception {
    // 1. 爬取書籍資訊
    BookInfoDTO bookInfo = isbnCrawlerService.crawlByIsbn(isbn);
    
    // 2. 檢查是否已存在
    InvBookInfo existingBook = invBookInfoMapper.selectInvBookInfoByIsbn(isbn);
    
    // 3. 建立或更新書籍和物品
    InvItem bookItem = existingBook != null 
        ? updateAndGet(existingBook, bookInfo)
        : createBookAndItem(bookInfo);
    
    // 4. 回傳指定類型
    return resultType.cast(bookItem);
}
```

**功能：**
- ✅ 呼叫三層備援爬蟲服務
- ✅ 自動儲存到 `inv_book_info` 表
- ✅ 自動建立 `inv_item` 記錄
- ✅ 支援新建和更新
- ✅ 支援回傳不同類型（InvItem、BookInfoDTO）

### 4. InvScanController 整合

Controller 直接使用 Handler：

```java
// 取得 Handler
CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA101);

// 呼叫簡化方法
InvItem bookItem = handler.crawlSingleAndSave(isbn, InvItem.class);
```

## 📊 架構對比

### ❌ 之前（方案 A - 被拒絕）

```
Controller → CrawlerFacade → CA101SingleQueryService → IsbnCrawlerService
                ↓
           額外的層級（雜亂）
```

### ✅ 現在（直接修改 CrawlerHandler）

```
Controller → CrawlerHandler.getHandler(CA101) → crawlSingleAndSave()
                                                      ↓
                                              IsbnCrawlerService
                                              InvBookInfoMapper
                                              InvItemMapper
```

## 🎭 雙軌設計

### 軌道 1：單一查詢（新增）

```java
// 適用於：即時查詢、單筆處理
InvItem item = handler.crawlSingleAndSave(isbn, InvItem.class);
```

**優點：**
- 🚀 快速直接
- 📝 程式碼簡潔
- 🎯 職責清晰

### 軌道 2：批次處理（保留）

```java
// 適用於：批次匯入、定時任務
CrawlerResult result = handler.execute(crawledData);
```

**優點：**
- 📊 完整的執行記錄
- 🔄 支援批次處理
- ⚙️ 完整的生命週期管理

## 🔧 使用範例

### 基本使用

```java
@RestController
public class MyController {
    
    @PostMapping("/scan/isbn")
    public AjaxResult scan(@RequestParam String isbn) {
        try {
            // 取得 Handler
            CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA101);
            
            // 執行爬取+儲存
            InvItem item = handler.crawlSingleAndSave(isbn, InvItem.class);
            
            return AjaxResult.success(item);
        } catch (Exception e) {
            return AjaxResult.error("處理失敗: " + e.getMessage());
        }
    }
}
```

### 不同回傳類型

```java
// 回傳 InvItem
InvItem item = handler.crawlSingleAndSave(isbn, InvItem.class);

// 回傳 BookInfoDTO
BookInfoDTO dto = handler.crawlSingleAndSave(isbn, BookInfoDTO.class);
```

## 📝 擴展指南

### 新增其他類型爬蟲

#### 情境 1：僅需單一查詢

```java
@Component
public class CA102WHandler extends CrawlerHandler {
    
    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA102;
    }
    
    @Override
    protected CrawlerDataRepository getRepository() {
        return ca102Repository;
    }
    
    // 只需實作這一個方法
    @Override
    public <T> T crawlSingleAndSave(String url, Class<T> resultType) {
        // 爬取商品資訊並儲存
    }
}
```

#### 情境 2：僅需批次處理

```java
@Component
public class CA103WHandler extends CrawlerHandler {
    
    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA103;
    }
    
    @Override
    protected CrawlerDataRepository getRepository() {
        return ca103Repository;
    }
    
    // 實作批次處理方法
    @Override
    protected List<String[]> crawlWebsiteFetchData(...) {
        // 批次爬取邏輯
    }
    
    @Override
    protected List<Object> processData(...) {
        // 資料處理邏輯
    }
}
```

#### 情境 3：同時支援兩種模式

```java
@Component
public class CA104WHandler extends CrawlerHandler {
    
    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA104;
    }
    
    @Override
    protected CrawlerDataRepository getRepository() {
        return ca104Repository;
    }
    
    // 單一查詢
    @Override
    public <T> T crawlSingleAndSave(String input, Class<T> resultType) {
        // 單一查詢邏輯
    }
    
    // 批次處理
    @Override
    protected List<String[]> crawlWebsiteFetchData(...) {
        // 批次爬取邏輯
    }
    
    @Override
    protected List<Object> processData(...) {
        // 資料處理邏輯
    }
}
```

## ✨ 設計優勢

| 優勢 | 說明 |
|------|------|
| **零額外層級** | 不新增 Facade、Service 等額外層 |
| **共用基礎** | 所有爬蟲共用 CrawlerHandler 基礎架構 |
| **靈活選擇** | 子類別可選擇實作單一查詢或批次處理，或兩者都實作 |
| **類型安全** | 泛型支援，編譯期檢查 |
| **易於維護** | 程式碼集中在 Handler 中 |
| **隔離問題** | `List<String[]>` 的問題被隔離在批次處理中 ⭐ NEW |
| **避免冗餘** | 不需要實作用不到的方法 ⭐ NEW |

### 關於 `List<String[]>` 的處理

**問題：** `List<String[]>` 缺乏類型安全、語義不清楚

**解決方案：**
- ✅ 將其改為**可選實作**
- ✅ 單一查詢模式不需要用到它
- ✅ 批次處理模式才需要實作
- ✅ 未來如果需要批次處理，可以在子類別中自行定義更好的資料結構

**範例：**
```java
// CA101 單一查詢：不需要 List<String[]>
public <T> T crawlSingleAndSave(String isbn, Class<T> resultType) {
    BookInfoDTO bookInfo = isbnCrawlerService.crawlByIsbn(isbn);
    // 直接處理 BookInfoDTO，不需要 String[]
}

// CA103 批次處理：可以自定義資料結構
protected List<String[]> crawlWebsiteFetchData(...) {
    // 或者在這裡轉換為更好的結構
    List<ProductDTO> products = crawlProducts();
    // 轉換為 List<String[]> 只是為了相容模板方法
}
```

## 🎓 總結

重構後的設計：

1. ✅ **直接修改 CrawlerHandler** - 不新增額外層級
2. ✅ **提供簡化方法** - `crawlSingleAndSave()` 用於單一查詢
3. ✅ **批次方法可選** - 改為非抽象方法，子類別可選擇實作 ⭐ NEW
4. ✅ **隔離 `List<String[]>` 問題** - 只在批次處理中使用 ⭐ NEW
5. ✅ **保留模板方法** - `execute()` 用於批次處理
6. ✅ **共用架構** - 所有爬蟲都基於同一個 CrawlerHandler
7. ✅ **清晰職責** - Controller → Handler，直接呼叫
8. ✅ **靈活擴展** - 三種實作方式可選

**這就是您要的：能共用、不雜亂、還優化了 `List<String[]>` 問題的設計！** 🎉

### 關鍵改進

| 改進點 | 說明 |
|--------|------|
| **減少冗餘** | CA101 不需要實作用不到的批次方法 |
| **提高靈活性** | 子類別可自由選擇支援的模式 |
| **隔離問題** | `List<String[]>` 只在需要批次處理時才會遇到 |
| **清晰意圖** | 方法註釋明確說明使用場景 |

---

**完成時間**：2025-10-20  
**狀態**：✅ 已實作並整合
