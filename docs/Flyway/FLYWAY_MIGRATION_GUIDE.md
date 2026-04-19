# 🚀 Flyway 遷移指南 - SQL 檔案整理說明

## 📋 總覽

本專案已整合 **Flyway** 資料庫版本管理工具，將原本散落的 SQL 檔案整理為版本化的遷移檔案，實現自動化資料庫部署和版本控制。

## 🎯 整合目標

1. **自動化**：應用程式啟動時自動執行資料庫遷移
2. **版本控制**：追蹤每次資料庫變更的歷史記錄
3. **一致性**：確保開發、測試、正式環境的資料庫結構一致
4. **可追溯**：清楚記錄每次變更的時間和內容

## 📂 SQL 檔案整理結果

### ✅ 已整合到 Flyway 的檔案

以下檔案的內容已經整合到 Flyway 遷移檔案中，**不再需要手動執行**：

| 原檔案 | 整合到 | 狀態 | 說明 |
|--------|--------|------|------|
| `inventory_management.sql` | V1, V2 | ✅ 已整合 | 拆分為表結構（V1）和初始資料（V2） |
| `inventory_menu.sql` | V3 | ✅ 已整合 | 庫存管理選單和權限配置 |
| `add_lost_qty.sql` | V4 | ✅ 已整合 | 新增遺失數量欄位 |
| `fix_return_table.sql` | V1 | ✅ 已整合 | 歸還表結構已在 V1 中優化 |
| `inv_book_info.sql` | V5 | ✅ 已整合 | 書籍資訊表和分類 |

### 📦 保留的系統核心檔案

以下檔案為系統核心資料庫結構，保留作為參考和手動初始化使用：

| 檔案 | 用途 | 說明 |
|------|------|------|
| `quartz-tw.sql` | Quartz 調度器 | Spring Boot Quartz 排程系統所需表結構 |
| `cool-apps_20250922-tw.sql` | 系統主資料庫 | 完整的系統資料庫備份（含用戶、角色、權限等） |

### 🗑️ 可歸檔的檔案

以下檔案已整合到 Flyway，建議移到 `sql/archive/` 目錄保存：

```
sql/
├── archive/                           # 歸檔目錄（新建）
│   ├── inventory_management.sql      # 已整合到 V1, V2
│   ├── inventory_menu.sql            # 已整合到 V3
│   ├── add_lost_qty.sql              # 已整合到 V4
│   ├── fix_return_table.sql          # 已整合到 V1
│   ├── inv_book_info.sql             # 已整合到 V5
│   ├── fix_dirty_book_data.sql       # 資料修正腳本
│   ├── fix_category_default.sql      # 資料修正腳本
│   ├── fix_category_menu.sql         # 資料修正腳本
│   └── update_inventory_menu_integration.sql  # 資料修正腳本
```

### 📝 編號腳本說明

以下檔案為資料修正腳本，已執行過可歸檔：

| 檔案 | 說明 | 建議處理 |
|------|------|----------|
| `01_fix_dirty_book_data.sql` | 修正書籍資料問題 | 歸檔 |
| `02_fix_category_default.sql` | 修正分類預設值 | 歸檔 |
| `03_category_permissions.sql` | 分類權限配置 | 歸檔 |
| `04_fix_category_menu.sql` | 修正分類選單 | 歸檔 |

## 🔧 Flyway 遷移檔案位置

```
cheng-admin/src/main/resources/db/migration/
├── README.md                              # Flyway 使用說明
├── V1__init_inventory_tables.sql          # 庫存表結構
├── V2__init_inventory_data.sql            # 庫存初始資料
├── V3__init_inventory_menu.sql            # 庫存選單配置
├── V4__add_lost_qty_column.sql            # 新增遺失數量欄位
└── V5__add_book_info_tables.sql           # 書籍資訊表
```

## 🚀 使用方式

### 1. 全新專案部署

```bash
# 1. 建立資料庫
CREATE DATABASE cool_apps CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 匯入系統核心資料（手動執行一次）
mysql -u root -p cool_apps < sql/cool-apps_20250922-tw.sql
mysql -u root -p cool_apps < sql/quartz-tw.sql

# 3. 啟動應用程式（Flyway 會自動執行庫存相關的遷移）
mvn spring-boot:run -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

**執行流程**：
1. 系統核心表（用戶、角色、選單等）由 `cool-apps_20250922-tw.sql` 建立
2. Quartz 調度器表由 `quartz-tw.sql` 建立
3. 庫存管理表由 Flyway 自動建立（V1 → V5）

### 2. 已存在專案更新

如果專案已經在執行，只需：
```bash
# 啟動應用程式，Flyway 會自動執行新的遷移檔案
mvn spring-boot:run -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

Flyway 會：
- 檢查 `flyway_schema_history` 表
- 只執行尚未執行的遷移檔案
- 記錄新的遷移歷史

### 3. 新增資料庫變更

當需要修改資料庫時：

```bash
# 1. 建立新的遷移檔案
# cheng-admin/src/main/resources/db/migration/V6__your_description.sql

# 2. 編寫 SQL 內容

# 3. 重啟應用程式
mvn spring-boot:run
```

## 📊 遷移執行順序

```
系統啟動
    ↓
檢查 flyway_schema_history 表
    ↓
╔═══════════════════════════════╗
║  首次啟動（資料庫已有核心表） ║
╚═══════════════════════════════╝
    ↓
執行 V1__init_inventory_tables.sql    ← 建立庫存表結構
    ↓
執行 V2__init_inventory_data.sql      ← 初始化庫存資料
    ↓
執行 V3__init_inventory_menu.sql      ← 建立庫存選單
    ↓
執行 V4__add_lost_qty_column.sql      ← 新增遺失數量欄位
    ↓
執行 V5__add_book_info_tables.sql     ← 新增書籍資訊表
    ↓
記錄到 flyway_schema_history
    ↓
應用程式啟動完成
```

## 🔍 查看遷移歷史

### SQL 查詢
```sql
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

### Maven 命令
```bash
# 查看遷移資訊
mvn flyway:info

# 驗證遷移狀態
mvn flyway:validate

# 清理資料庫（謹慎使用，會刪除所有表）
mvn flyway:clean
```

## ⚠️ 重要注意事項

### 1. 系統核心 vs 庫存模組

| 類別 | 管理方式 | 檔案位置 |
|------|----------|----------|
| **系統核心** | 手動執行 | `sql/cool-apps_20250922-tw.sql` |
| **Quartz** | 手動執行 | `sql/quartz-tw.sql` |
| **庫存模組** | Flyway 自動 | `cheng-admin/src/main/resources/db/migration/` |

### 2. 執行順序
```
1. 手動執行系統核心 SQL（一次性）
   └─ cool-apps_20250922-tw.sql
   └─ quartz-tw.sql

2. 啟動應用程式（每次啟動時檢查）
   └─ Flyway 自動執行庫存相關遷移
```

### 3. 環境配置

**本地開發環境**：
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cool_apps
```

**正式環境**：
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://your-prod-server:3306/cool_apps
```

### 4. 遷移失敗處理

如果 Flyway 遷移失敗：
```sql
-- 1. 查看失敗記錄
SELECT * FROM flyway_schema_history WHERE success = 0;

-- 2. 修正問題後，刪除失敗記錄
DELETE FROM flyway_schema_history WHERE version = 'X' AND success = 0;

-- 3. 重啟應用程式重新執行
```

## 📁 建議的檔案結構調整

```
sql/
├── FLYWAY_MIGRATION_GUIDE.md          # 本文件
├── quartz-tw.sql                       # Quartz 核心（保留）
├── cool-apps_20250922-tw.sql          # 系統核心（保留）
└── archive/                            # 歸檔目錄（新建）
    ├── inventory_management.sql        # 已整合到 Flyway
    ├── inventory_menu.sql              # 已整合到 Flyway
    ├── add_lost_qty.sql                # 已整合到 Flyway
    ├── fix_return_table.sql            # 已整合到 Flyway
    ├── inv_book_info.sql               # 已整合到 Flyway
    ├── 01_fix_dirty_book_data.sql      # 資料修正腳本
    ├── 02_fix_category_default.sql     # 資料修正腳本
    ├── 03_category_permissions.sql     # 資料修正腳本
    └── 04_fix_category_menu.sql        # 資料修正腳本

cheng-admin/src/main/resources/db/migration/
├── README.md                           # Flyway 詳細說明
├── V1__init_inventory_tables.sql       # 庫存表結構
├── V2__init_inventory_data.sql         # 庫存初始資料
├── V3__init_inventory_menu.sql         # 庫存選單配置
├── V4__add_lost_qty_column.sql         # 結構調整
└── V5__add_book_info_tables.sql        # 書籍擴充
```

## 🎓 學習資源

- **Flyway 官方文件**：https://flywaydb.org/documentation/
- **Spring Boot Flyway**：https://docs.spring.io/spring-boot/reference/howto/data-initialization.html
- **專案內說明**：`cheng-admin/src/main/resources/db/migration/README.md`

## 📝 常見問題

### Q1: 為什麼不把系統核心也整合到 Flyway？
**A**: 系統核心（用戶、角色、權限）通常在專案初始化時一次性建立，變更頻率低。庫存模組是後續開發的功能，變更頻繁，更適合用 Flyway 管理。

### Q2: 如何在測試環境驗證遷移？
**A**: 
```bash
# 1. 建立測試資料庫
CREATE DATABASE cool_apps_test;

# 2. 匯入核心 SQL
mysql -u root -p cool_apps_test < sql/cool-apps_20250922-tw.sql

# 3. 使用測試配置啟動
mvn spring-boot:run -Dspring.profiles.active=test
```

### Q3: 如何回滾遷移？
**A**: Flyway 的免費版不支援自動回滾。建議：
1. 在新遷移檔案中編寫回滾 SQL
2. 或手動執行 SQL 回滾
3. 考慮升級到 Flyway Teams 版（支援 undo 遷移）

### Q4: 正式環境如何部署？
**A**: 
```bash
# 1. 備份正式資料庫
mysqldump -u root -p cool_apps > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. 部署新版應用程式
# Flyway 會自動執行新的遷移檔案

# 3. 驗證遷移結果
mysql -u root -p cool_apps -e "SELECT * FROM flyway_schema_history"
```

## ✅ 遷移檢查清單

部署前請確認：
- [ ] 已備份正式資料庫
- [ ] 在測試環境驗證過所有遷移
- [ ] 遷移檔案命名正確
- [ ] SQL 語法在目標資料庫版本測試通過
- [ ] 回滾方案已準備
- [ ] 相關人員已被通知

---

**最後更新**：2025-10-21  
**維護者**：Cheng  
**專案**：CoolApps 管理系統
