# 爬蟲系統文件

本目錄包含爬蟲框架的設計、實作和重構相關文件。

## 📚 文件列表

### 設計與架構
- **CRAWLER-DESIGN-PATTERN.md** - 爬蟲設計模式詳解
- **CRAWLER_REPOSITORY_ARCHITECTURE.md** - Repository 架構設計與實作

### 重構記錄
- **CRAWLER-REFACTOR-SUMMARY.md** - 爬蟲框架重構總結
- **CRAWLER_REFACTORING_BATCH_SAVE.md** - 批次儲存功能重構
- **REFACTOR_REMOVE_CRAWLEDDATA_PARAMETER.md** - 移除不必要的 CrawledData 參數

### 整合與配置
- **CRAWLER_PARAMS_AND_QUARTZ.md** - 爬蟲參數與 Quartz 排程整合
- **SELENIUM_CONFIGURATION.md** - Selenium WebDriver 配置說明

### 實作範例
- **CA102_CRAWLER_IMPLEMENTATION.md** - CA102 圖書館爬蟲實作案例

## 🎯 核心概念

### 爬蟲框架設計
```
CrawlerHandler (抽象基類)
  ├── execute() - 模板方法
  ├── crawlWebsiteFetchData() - 爬取資料
  ├── processData() - 處理資料
  └── saveData() - 儲存資料
      └── CrawlerDataRepository
```

### 使用流程
1. 繼承 `CrawlerHandler` 基類
2. 實作抽象方法（爬取、處理、儲存）
3. 註冊到 Spring Container
4. 透過 Quartz 排程執行

## 🔗 相關連結
- [架構設計](../Architecture/)
- [排程系統](../Schedule/)
