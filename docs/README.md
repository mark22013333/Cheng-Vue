# CoolApps 文件索引

## 📚 文件分類結構

### 📁 [Architecture](./Architecture/) - 架構設計
系統架構、設計模式、最佳實踐相關文件
- `ARCHITECTURE_SUMMARY.md` - 爬蟲框架完整架構總結
- `INTEGRATION_SUMMARY.md` - 系統整合總結
- `MYBATIS_BEST_PRACTICES.md` - MyBatis 最佳實踐指南
- `JDBC_TEMPLATE_JPA_REMOVAL.md` - JDBC Template 與 JPA 移除說明
- `JDBC_TRANSACTION_MANAGEMENT.md` - JDBC 交易管理機制

### 📁 [Deployment](./Deployment/) - 部署相關
應用程式部署、環境配置相關文件
- `00_DEPLOYMENT_README.md` - 部署主要說明文件
- `DEPLOYMENT.md` - 部署步驟說明
- `DEPLOYMENT_CHECKLIST.md` - 部署檢查清單
- `DEPLOYMENT_CHECKLIST_QUARTZ.md` - Quartz 排程部署檢查清單
- `CATEGORY_DEPLOYMENT.md` - 分類功能部署說明

### 📁 [Crawler](./Crawler/) - 爬蟲系統
爬蟲框架、實作、重構相關文件
- `CRAWLER-DESIGN-PATTERN.md` - 爬蟲設計模式
- `CRAWLER-REFACTOR-SUMMARY.md` - 爬蟲重構總結
- `CRAWLER_REPOSITORY_ARCHITECTURE.md` - Repository 架構設計
- `CRAWLER_REFACTORING_BATCH_SAVE.md` - 批次儲存重構
- `REFACTOR_REMOVE_CRAWLEDDATA_PARAMETER.md` - 移除 CrawledData 參數重構
- `CRAWLER_PARAMS_AND_QUARTZ.md` - 爬蟲參數與 Quartz 整合
- `CA102_CRAWLER_IMPLEMENTATION.md` - CA102 爬蟲實作
- `SELENIUM_CONFIGURATION.md` - Selenium 配置說明

### 📁 [Schedule](./Schedule/) - 排程系統
Quartz 排程、定時任務相關文件
- `排程開發與測試指南.md` - 完整的排程開發指南
- `任務類型動態配置整合說明.md` - 任務類型動態配置
- `架構重構說明.md` - 排程架構重構
- `修正完成總結.md` - 排程修正總結
- `QUARTZ_FRONTEND_INTEGRATION.md` - Quartz 前端整合
- `QUARTZ_JOB_TYPE_ENUM.md` - 任務類型列舉設計
- `QUARTZ_TEMPLATE_FEATURE.md` - 任務範本功能
- `QUARTZ_QUICK_START.md` - Quartz 快速開始
- `DEBUG_QUARTZ_PARAMS.md` - Quartz 參數除錯
- `FIXES_QUARTZ_TEMPLATE.md` - Quartz 範本修正

### 📁 [Inventory](./Inventory/) - 庫存管理
庫存系統功能、整合相關文件
- `INVENTORY_SYSTEM_README.md` - 庫存系統說明
- `INVENTORY_OPTIMIZATION.md` - 庫存系統最佳化
- `inventory_integration_guide.md` - 庫存整合指南
- `STOCK_STATUS_FIX.md` - 庫存狀態修正

### 📁 [Book](./Book/) - 圖書管理
圖書借閱、ISBN 查詢相關文件
- `ISBN_BOOK_FEATURE.md` - ISBN 圖書功能
- `ISBN-FALLBACK-TEST.md` - ISBN 三層備援搜尋測試
- `BOOK_DATA_FIX.md` - 圖書資料修正
- `BORROW_APPROVAL_CHANGES.md` - 借閱審核變更
- `BORROW_BUGFIX.md` - 借閱功能 Bug 修復
- `RETURN_RECORDS_FEATURE.md` - 歸還記錄功能

### 📁 [BugFix](./BugFix/) - Bug 修復
各類 Bug 修復記錄與總結
- `CRITICAL_FIX_SUMMARY.md` - 關鍵問題修復總結
- `BUGFIX_RETURN_ERRORS.md` - 歸還錯誤修復
- `IMAGE-FIX-SUMMARY.md` - 圖片問題修復總結
- `IMAGE_DISPLAY_FIX.md` - 圖片顯示修復
- `IMAGE_PATH_FIX_SUMMARY.md` - 圖片路徑修復總結
- `MANAGEMENT_PAGE_FIXES.md` - 管理頁面修正
- `SIDEBAR_RESIZE_SUMMARY.md` - 側邊欄調整總結
- `sidebar_resize_feature.md` - 側邊欄調整功能

### 📁 [Flyway](./Flyway/) - 資料庫遷移
Flyway 資料庫版本控制相關文件
- `FLYWAY_REPAIR_GUIDE.md` - Flyway Repair 使用指南
- `FLYWAY_MIGRATION_GUIDE.md` - Flyway 遷移指南
- `FLYWAY_TESTING_GUIDE.md` - Flyway 測試指南
- `FLYWAY_QUICK_CHECK.md` - Flyway 快速檢查
- `FLYWAY_BEAN_DEPENDENCY.md` - Flyway Bean 相依性
- `flyway-repair.sh` - Flyway Repair 執行腳本
- `check-flyway.sql` - Flyway 檢查 SQL

### 📁 [Scripts](./Scripts/) - 工具腳本
部署、測試、除錯用腳本
- `deploy_all.sh` - 完整部署腳本
- `setup-cool-test-db.sh` - 測試資料庫建立腳本
- `test-flyway.sh` - Flyway 測試腳本
- `test-task-types.sh` - 任務類型測試腳本
- `test-db-connection.sql` - 資料庫連線測試 SQL
- `debug-stock-status.sql` - 庫存狀態除錯 SQL
- `tomcat-setenv.sh` - Tomcat 環境變數設定
- `diagnose-flyway-issue.md` - Flyway 問題診斷

### 📁 [Guides](./Guides/) - 指南與快速開始
快速入門、驗證、開發指南
- `QUICK_START.md` - 快速開始指南
- `FINAL_VERIFICATION.md` - 最終驗證清單
- `COMMIT_MESSAGE.txt` - Git Commit Message 規範

### 📁 [Development](./Development/) - 開發指南
開發標準、規範、模板
- `SCHEDULED_TASK_GUIDE.md` - 排程任務開發完整指南
- `SCHEDULED_TASK_RULES.md` - 排程任務開發規範

---

## 🚀 快速導航

### 新手入門
1. 📖 [快速開始](./Guides/QUICK_START.md)
2. 🏗️ [系統架構](./Architecture/ARCHITECTURE_SUMMARY.md)
3. 🚢 [部署說明](./Deployment/00_DEPLOYMENT_README.md)

### 功能開發
- 🕷️ **爬蟲開發**: [爬蟲設計模式](./Crawler/CRAWLER-DESIGN-PATTERN.md)
- ⏰ **排程開發**: [排程開發與測試指南](./Schedule/排程開發與測試指南.md) | [排程任務開發規範](./Development/SCHEDULED_TASK_GUIDE.md)
- 📦 **庫存功能**: [庫存系統說明](./Inventory/INVENTORY_SYSTEM_README.md)
- 📚 **圖書功能**: [ISBN 圖書功能](./Book/ISBN_BOOK_FEATURE.md)

### 問題排除
- 🐛 [Bug 修復記錄](./BugFix/)
- 🔍 [Flyway 問題診斷](./Scripts/diagnose-flyway-issue.md)
- ⚙️ [部署檢查清單](./Deployment/DEPLOYMENT_CHECKLIST.md)

### 維護與最佳化
- 💾 [MyBatis 最佳實踐](./Architecture/MYBATIS_BEST_PRACTICES.md)
- 🔄 [JDBC 交易管理](./Architecture/JDBC_TRANSACTION_MANAGEMENT.md)
- 📊 [庫存系統最佳化](./Inventory/INVENTORY_OPTIMIZATION.md)

---

## 📝 文件更新記錄

### 2025-10-26
- ✅ 完成文件分類整理
- ✅ 建立目錄結構
- ✅ 新增 README 索引文件

---

## 💡 文件撰寫規範

所有文件應遵循以下規範：
1. 使用 Markdown 格式
2. 包含更新日期
3. 清楚的標題結構
4. 適當的程式碼範例
5. 必要時附上相關文件連結

---

## 🔗 相關連結

- **專案首頁**: [GitHub Repository](https://github.com/mark22013333/Cheng-Vue)
- **線上文件**: [CoolApps 文件](https://cool-apps.zeabur.app)
- **API 文件**: [Swagger UI](http://localhost:8080/swagger-ui/index.html)
