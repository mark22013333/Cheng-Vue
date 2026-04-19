# 架構設計文件

本目錄包含 CoolApps 系統的架構設計、設計模式和最佳實踐相關文件。

## 📚 文件列表

### 核心架構
- **ARCHITECTURE_SUMMARY.md** - 爬蟲框架完整架構總結，包含模板方法模式和 Repository 架構設計
- **INTEGRATION_SUMMARY.md** - 系統模組整合總結

### 資料存取層
- **MYBATIS_BEST_PRACTICES.md** - MyBatis 開發最佳實踐指南
- **JDBC_TEMPLATE_JPA_REMOVAL.md** - 移除 JdbcTemplate 和 JPA 的架構決策
- **JDBC_TRANSACTION_MANAGEMENT.md** - JDBC 交易管理機制與實作

## 🎯 設計原則

本系統採用以下設計原則：
- **Template Method Pattern** - 爬蟲框架基類設計
- **Repository Pattern** - 資料存取抽象層
- **Dependency Injection** - Spring 依賴注入
- **Transaction Management** - 宣告式交易管理

## 🔗 相關連結
- [爬蟲系統文件](../Crawler/)
- [排程系統文件](../Schedule/)
