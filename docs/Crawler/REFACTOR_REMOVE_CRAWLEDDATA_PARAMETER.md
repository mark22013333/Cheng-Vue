# 移除 CrawledData 參數重構

## 📋 重構資訊
- **重構日期**: 2025-03-28
- **重構類型**: 介面簡化 - 移除不必要的參數
- **影響範圍**: CrawlerHandler、所有 Repository、CA102WHandler

## 🎯 問題分析

### 用戶提問
> "這個方法有必要放 crawledData 嗎？因為最後有回傳 CrawlerResult 了，請確認是否有必要？"

### 問題發現

#### 1. `CrawledData` 參數完全未使用
```java
// CA102WHandler
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
    // crawledData 從未被使用 ❌
}

protected List<CompanyNewsDTO> processData(List<String[]> rawData, CrawledData crawledData) {
    // crawledData 從未被使用 ❌
}

protected void beforeCrawl(CrawledData crawledData) {
    // crawledData 從未被使用 ❌
}
```

#### 2. Repository 只需要 `CrawlerType`
```java
// GenericCrawlerRepository
public int batchSave(List<Object> dataList, CrawledData crawledData) {
    CrawlerType crawlerType = crawledData.getCrawlerType();  // ← 只用這個
    // ...
}
```

**但是！** `CrawledData` 已經被用戶移除了 `crawlerType` 欄位：
```java
@Data
@Builder
public class CrawledData {
    private String input;
    private String title;
    private String summary;
    private Map<String, Object> attributes;
    // ❌ 沒有 crawlerType 欄位！
}
```

這會導致**編譯錯誤**！

#### 3. `CrawlerType` 已經在 Handler 中
每個 Handler 都有：
```java
protected abstract CrawlerType getCrawlerType();
```

**不需要透過參數傳遞！**

---

## ✅ 重構方案

### 核心原則
- ✅ **移除不必要的參數** - `CrawledData` 完全沒有實際用途
- ✅ **使用 `getCrawlerType()`** - Handler 已有此方法
- ✅ **簡化介面** - 讓 API 更清晰易用

---

## 🔧 重構內容

### 1. CrawlerHandler 基類

#### execute() 方法簡化
```java
// 改前
public final CrawlerResult execute(CrawledData crawledData) {
    beforeCrawl(crawledData);
    List<R> rawData = crawlWebsiteFetchData(driver, crawledData);
    List<P> processedData = processData(rawData, crawledData);
    int savedCount = saveData(processedData, crawledData);
    afterCrawl(crawledData, success);
}

// 改後
public final CrawlerResult execute() {
    beforeCrawl();
    List<R> rawData = crawlWebsiteFetchData(driver);
    List<P> processedData = processData(rawData);
    int savedCount = saveData(processedData);
    afterCrawl(success);
}
```

#### 抽象方法簡化
```java
// 改前
protected List<R> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) throws Exception;
protected List<P> processData(List<R> rawData, CrawledData crawledData) throws Exception;
protected void beforeCrawl(CrawledData crawledData) throws Exception;
protected void afterCrawl(CrawledData crawledData, boolean success);

// 改後
protected List<R> crawlWebsiteFetchData(WebDriver driver) throws Exception;
protected List<P> processData(List<R> rawData) throws Exception;
protected void beforeCrawl() throws Exception;
protected void afterCrawl(boolean success);
```

#### saveData() 方法改用 CrawlerType
```java
// 改前
protected int saveData(List<P> processedData, CrawledData crawledData) throws Exception {
    return repository.batchSave(processedData, crawledData);
}

// 改後
protected int saveData(List<P> processedData) throws Exception {
    return repository.batchSave(processedData, getCrawlerType());
}
```

### 2. CrawlerDataRepository 介面

```java
// 改前
import com.cheng.crawler.dto.CrawledData;

public interface CrawlerDataRepository<T> {
    int batchSave(List<T> dataList, CrawledData crawledData) throws Exception;
    boolean save(T data, CrawledData crawledData) throws Exception;
}

// 改後
import com.cheng.crawler.enums.CrawlerType;

public interface CrawlerDataRepository<T> {
    int batchSave(List<T> dataList, CrawlerType crawlerType) throws Exception;
    boolean save(T data, CrawlerType crawlerType) throws Exception;
}
```

### 3. GenericCrawlerRepository 實作

```java
// 改前
@Override
public int batchSave(List<Object> dataList, CrawledData crawledData) throws Exception {
    CrawlerType crawlerType = crawledData.getCrawlerType();  // ← 編譯錯誤！
    // ...
}

// 改後
@Override
public int batchSave(List<Object> dataList, CrawlerType crawlerType) throws Exception {
    // 直接使用參數
    String sql = sqlRegistry.get(crawlerType);
    // ...
}
```

### 4. CA102WHandler 實作

```java
// 改前
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
    // crawledData 未使用
}

@Override
protected List<CompanyNewsDTO> processData(List<String[]> rawData, CrawledData crawledData) {
    // crawledData 未使用
}

// 改後
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver) {
    // 更簡潔
}

@Override
protected List<CompanyNewsDTO> processData(List<String[]> rawData) {
    // 更簡潔
}
```

### 5. BookInventoryRepository 實作

```java
// 改前
@Override
public int batchSave(List<BookInfoDTO> dataList, CrawledData crawledData) throws Exception {
    if (save(data, crawledData)) {  // ...
}

// 改後
@Override
public int batchSave(List<BookInfoDTO> dataList, CrawlerType crawlerType) throws Exception {
    if (save(data, crawlerType)) {  // ...
}
```

---

## 📊 重構效益

### 1. 程式碼簡化

| 項目 | 改前 | 改後 | 改善 |
|-----|------|------|------|
| **execute() 參數** | 1 個 | 0 個 | -100% |
| **crawlWebsiteFetchData() 參數** | 2 個 | 1 個 | -50% |
| **processData() 參數** | 2 個 | 1 個 | -50% |
| **beforeCrawl() 參數** | 1 個 | 0 個 | -100% |
| **afterCrawl() 參數** | 2 個 | 1 個 | -50% |
| **Repository 參數** | CrawledData | CrawlerType | 類型明確 |

### 2. 介面清晰度

```java
// 改前 - 令人困惑
handler.execute(crawledData);  // ← crawledData 是什麼？要傳什麼？

// 改後 - 清晰簡潔
handler.execute();  // ← 直接執行，無需參數
```

### 3. 避免編譯錯誤

```java
// 改前 - 編譯錯誤
CrawlerType crawlerType = crawledData.getCrawlerType();  
// ❌ CrawledData 沒有 getCrawlerType() 方法！

// 改後 - 正確
CrawlerType crawlerType = getCrawlerType();
// ✅ Handler 自己就知道類型
```

---

## 🚀 使用範例

### 執行爬蟲（簡化）

```java
// 改前
CrawledData crawledData = new CrawledData();
crawledData.setCrawlerType(CrawlerType.CA102);  // ← 多此一舉
CrawlerResult result = handler.execute(crawledData);

// 改後
CrawlerResult result = handler.execute();  // ← 簡單！
```

### 實作新爬蟲（參數減少）

```java
@Component
public class CA103WHandler extends CrawlerHandler<String[], ProductDTO> {
    
    // 改前 - 5 個參數
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) { }
    protected List<ProductDTO> processData(List<String[]> rawData, CrawledData crawledData) { }
    protected void beforeCrawl(CrawledData crawledData) { }
    protected void afterCrawl(CrawledData crawledData, boolean success) { }
    
    // 改後 - 2 個參數
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver) { }
    protected List<ProductDTO> processData(List<String[]> rawData) { }
    protected void beforeCrawl() { }
    protected void afterCrawl(boolean success) { }
}
```

---

## ✅ 設計原則應用

### 1. YAGNI (You Aren't Gonna Need It)
> 不要加入不需要的功能

- ❌ `CrawledData` 參數從未被使用
- ✅ 移除它讓介面更簡潔

### 2. KISS (Keep It Simple, Stupid)
> 保持簡單

```java
// 複雜
execute(new CrawledData().setCrawlerType(CrawlerType.CA102))

// 簡單
execute()
```

### 3. Single Source of Truth
> 單一真相來源

```java
// 改前 - 兩個地方都有 CrawlerType
class Handler {
    CrawlerType getCrawlerType() { return CA102; }
}
CrawledData data = new CrawledData();
data.setCrawlerType(CA102);  // ← 重複！

// 改後 - 只有一個地方
class Handler {
    CrawlerType getCrawlerType() { return CA102; }  // ← 唯一來源
}
```

---

## 🔍 相關檔案變更

### 變更的檔案列表
1. ✅ `CrawlerHandler.java` - 移除所有方法的 `crawledData` 參數
2. ✅ `CrawlerDataRepository.java` - 介面改用 `CrawlerType`
3. ✅ `GenericCrawlerRepository.java` - 實作改用 `CrawlerType`
4. ✅ `BookInventoryRepository.java` - 實作改用 `CrawlerType`
5. ✅ `CA102WHandler.java` - 移除未使用的參數

### 不需變更的檔案
- ✅ `CrawledData.java` - 保留為輸出用途的 DTO
- ✅ `CrawlerResult.java` - 執行結果物件
- ✅ `JdbcSqlTemplate.java` - 底層工具類

---

## 📝 遷移指南

### 對於現有爬蟲

如果您有自訂的爬蟲 Handler：

#### 步驟 1：移除參數
```java
// 舊寫法
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
    // ...
}

// 新寫法
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver) {
    // ...
}
```

#### 步驟 2：執行時不傳參數
```java
// 舊寫法
CrawledData data = new CrawledData();
handler.execute(data);

// 新寫法
handler.execute();
```

#### 步驟 3：Repository 傳 CrawlerType
```java
// 舊寫法
repository.batchSave(dataList, crawledData);

// 新寫法
repository.batchSave(dataList, getCrawlerType());
```

---

## 💡 總結

### 問題
- ❌ `CrawledData` 參數完全未被使用
- ❌ 造成 API 混亂
- ❌ `CrawledData` 沒有 `crawlerType` 欄位會導致編譯錯誤

### 解決方案
- ✅ 移除所有不必要的 `crawledData` 參數
- ✅ Repository 直接使用 `CrawlerType`
- ✅ Handler 透過 `getCrawlerType()` 提供類型

### 效益
- ✅ **介面更簡潔** - 參數減少 50%
- ✅ **避免混淆** - 不需要建立無用的物件
- ✅ **類型明確** - 直接使用 `CrawlerType` enum
- ✅ **易於使用** - `handler.execute()` 更直觀

**這次重構讓爬蟲框架的 API 更加簡潔和易用！** ✨

---

## 🔗 相關文件
- [爬蟲框架架構總結](./ARCHITECTURE_SUMMARY.md)
- [Repository 架構設計](./CRAWLER_REPOSITORY_ARCHITECTURE.md)
- [批次儲存重構](./CRAWLER_REFACTORING_BATCH_SAVE.md)

---

**重構日期**: 2025-03-28  
**維護者**: Cheng  
**版本**: 4.0 (簡化 API)
