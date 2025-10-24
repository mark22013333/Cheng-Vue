# Flyway 資料庫遷移說明

## 📚 概述

本專案使用 **Flyway** 作為資料庫版本管理工具，所有庫存管理系統相關的 SQL 遷移檔案都放置在此目錄下。

## 📂 遷移檔案結構

```
db/migration/
├── README.md                              # 本說明文件
├── V0.1__init_system_core.sql             # 系統核心資料表
├── V0.2__init_quartz_tables.sql           # Quartz 調度器表
├── V1__init_inventory_tables.sql          # 初始化庫存表結構
├── V2__init_inventory_data.sql            # 初始化庫存資料
├── V3__init_inventory_menu.sql            # 初始化庫存選單
├── V4__add_lost_qty_column.sql            # 新增遺失數量欄位
└── V5__add_book_info_tables.sql           # 新增書籍資訊表
```

## 🗂️ 遷移檔案說明

### V0.1__init_system_core.sql
**說明**：建立 CoolApps 系統核心資料表和初始資料

**包含表格**：
- `sys_dept` - 部門表
- `sys_user` - 使用者資訊表
- `sys_role` - 角色資訊表
- `sys_menu` - 選單權限表
- `sys_user_role` - 使用者和角色關聯表
- `sys_role_menu` - 角色和選單關聯表
- `sys_dict_type` - 字典類型表
- `sys_dict_data` - 字典資料表
- `sys_config` - 參數配置表
- `sys_notice` - 通知公告表
- `sys_post` - 職位資訊表
- `sys_oper_log` - 操作日誌記錄
- `sys_logininfor` - 系統訪問記錄
- `gen_table` - 程式碼產生業務表
- 以及初始管理員帳號、預設角色、系統選單等資料

**執行時機**：首次部署時最先執行

---

### V0.2__init_quartz_tables.sql
**說明**：建立 Quartz 調度器所需的資料表

**包含表格**：
- `QRTZ_JOB_DETAILS` - 任務詳細資訊表
- `QRTZ_TRIGGERS` - 觸發器資訊表
- `QRTZ_CRON_TRIGGERS` - Cron 觸發器表
- `QRTZ_SIMPLE_TRIGGERS` - 簡單觸發器表
- `QRTZ_SIMPROP_TRIGGERS` - 同步機制的行鎖表
- `QRTZ_BLOB_TRIGGERS` - Blob 類型觸發器表
- `QRTZ_CALENDARS` - 日曆資訊表
- `QRTZ_PAUSED_TRIGGER_GRPS` - 暫停的觸發器表
- `QRTZ_FIRED_TRIGGERS` - 已觸發的觸發器表
- `QRTZ_SCHEDULER_STATE` - 調度器狀態表
- `QRTZ_LOCKS` - 悲觀鎖資訊表

**執行時機**：在 V0.1 之後執行

---

### V1__init_inventory_tables.sql
**說明**：建立庫存管理系統的所有資料表結構

**包含表格**：
- `inv_category` - 物品分類表
- `inv_item` - 物品資訊表
- `inv_stock` - 庫存表
- `inv_stock_record` - 庫存異動記錄表
- `inv_borrow` - 借出記錄表
- `inv_return` - 歸還記錄表
- `inv_scan_log` - 掃描記錄表

**執行時機**：首次部署或資料庫初始化時

---

### V2__init_inventory_data.sql
**說明**：初始化基礎資料

**包含資料**：
- 物品分類初始資料（辦公用品、電子產品、家具等）
- 範例物品資料（原子筆、影印紙、印表機等）
- 範例庫存資料

**執行時機**：在 V1 之後自動執行

---

### V3__init_inventory_menu.sql
**說明**：初始化系統選單和權限配置

**包含內容**：
- 庫存管理主選單
- 物品管理選單及按鈕權限
- 庫存查詢選單及按鈕權限
- 借出管理選單及按鈕權限
- 掃描功能選單及按鈕權限
- 庫存報表選單及按鈕權限
- 物品分類選單及按鈕權限
- 管理員角色權限分配

**執行時機**：在 V2 之後自動執行

---

### V4__add_lost_qty_column.sql
**說明**：庫存表結構調整，新增遺失數量欄位

**修改內容**：
- 在 `inv_stock` 表新增 `lost_qty` 欄位

**執行時機**：在 V3 之後自動執行

---

### V5__add_book_info_tables.sql
**說明**：擴充書籍管理功能

**包含內容**：
- 新增書籍相關分類（圖書、文學小說、商業理財等）
- 建立 `inv_book_info` 書籍詳細資訊表
- 初始化範例書籍資料

**執行時機**：在 V4 之後自動執行

---

## ⚙️ Flyway 配置

### application.yml 配置
```yaml
spring:
  flyway:
    enabled: true                          # 啟用 Flyway
    locations: classpath:db/migration      # SQL 遷移檔案位置
    baseline-version: 0                    # 基準版本號
    baseline-on-migrate: true              # 首次執行時建立基準版本
    validate-on-migrate: true              # 驗證遷移檔案的 checksum
    encoding: UTF-8                        # 遷移檔案編碼
```

### pom.xml 依賴
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>10.21.0</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
    <version>10.21.0</version>
</dependency>
```

## 🚀 使用方式

### 1. 全新部署（資料庫為空）
當應用程式啟動時，Flyway 會自動：
1. 建立 `flyway_schema_history` 表追蹤遷移歷史
2. 依序執行所有遷移檔案（V1 → V2 → V3 → V4 → V5）
3. 記錄每個遷移檔案的執行時間和 checksum

**操作步驟**：
```bash
# 1. 確保資料庫已建立
CREATE DATABASE cool_apps CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 啟動應用程式
mvn spring-boot:run -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 2. 已存在的資料庫
如果資料庫已經有資料，Flyway 會根據 `baseline-on-migrate: true` 配置：
1. 建立基準版本（version 0）
2. 只執行尚未執行的遷移檔案

### 3. 新增遷移檔案
當需要新增資料庫變更時：

1. **建立新的遷移檔案**
   ```
   檔名格式：V{版本號}__{描述}.sql
   例如：V6__add_user_avatar_column.sql
   ```

2. **遵守命名規範**
   - 使用大寫 `V` 開頭
   - 版本號遞增（V6, V7, V8...）
   - 使用雙底線 `__` 分隔版本號和描述
   - 描述使用小寫英文和底線

3. **編寫 SQL 內容**
   ```sql
   -- 在檔案開頭加入註解說明
   -- ============================
   -- 說明：此次變更的目的
   -- ============================
   
   -- 實際的 SQL 語句
   ALTER TABLE ...
   ```

4. **重啟應用程式**
   - Flyway 會自動檢測並執行新的遷移檔案

## 📝 命名規範

### 遷移檔案命名格式
```
V{版本號}__{描述}.sql

V - 固定前綴（大寫）
版本號 - 遞增的數字（1, 2, 3...）
__ - 雙底線分隔符
描述 - 英文描述，使用底線連接單字
.sql - 檔案副檔名
```

### 範例
```
✅ 正確：
V1__init_inventory_tables.sql
V2__init_inventory_data.sql
V10__add_user_email_column.sql

❌ 錯誤：
v1_init_tables.sql              # v 應該大寫
V1_init_tables.sql              # 只有一個底線
V1__Init_Tables.sql             # 描述不應該使用大寫
V01__init_tables.sql            # 不需要補零
```

## 🔍 查看遷移歷史

### 方式一：查看資料庫表
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### 方式二：使用 Flyway 命令
```bash
mvn flyway:info
```

## ⚠️ 注意事項

### 1. 遷移檔案不可修改
一旦遷移檔案被執行過，**絕對不能修改其內容**。原因：
- Flyway 使用 checksum 驗證檔案完整性
- 修改會導致 checksum 不匹配，應用程式無法啟動

**解決方案**：如需修改，建立新的遷移檔案來更正

### 2. 版本號不可重複
每個遷移檔案的版本號必須唯一且遞增

### 3. 遷移失敗處理
如果遷移失敗：
1. 檢查錯誤日誌
2. 修正問題
3. 手動清理 `flyway_schema_history` 表中的失敗記錄
4. 重新執行遷移

```sql
-- 查看失敗的遷移
SELECT * FROM flyway_schema_history WHERE success = 0;

-- 刪除失敗記錄（謹慎操作）
DELETE FROM flyway_schema_history WHERE version = '版本號' AND success = 0;
```

### 4. 環境隔離
- **本地開發**：使用 `application-local.yml` 配置
- **正式環境**：使用 `application-prod.yml` 配置
- 不同環境的資料庫應該獨立管理

## 📖 相關文件

- [Flyway 官方文件](https://flywaydb.org/documentation/)
- [Spring Boot Flyway 整合指南](https://docs.spring.io/spring-boot/reference/howto/data-initialization.html)

## 🔄 原 SQL 檔案對應關係

| 原檔案 | Flyway 遷移檔案 | 說明 |
|--------|----------------|------|
| `cool-apps_20250922-tw.sql` | V0.1 | 系統核心資料表 |
| `quartz-tw.sql` | V0.2 | Quartz 調度器表 |
| `inventory_management.sql` | V1, V2 | 拆分為表結構和初始資料 |
| `inventory_menu.sql` | V3 | 選單和權限配置 |
| `add_lost_qty.sql` | V4 | 庫存表結構調整 |
| `fix_return_table.sql` | V1 | 已整合到初始表結構 |
| `inv_book_info.sql` | V5 | 書籍資訊擴充 |

## ✅ 遷移檔案檢查清單

新增遷移檔案前請確認：
- [ ] 檔名符合命名規範
- [ ] 版本號正確遞增
- [ ] SQL 語法正確無誤
- [ ] 包含清楚的註解說明
- [ ] 考慮了回滾方案
- [ ] 在測試環境驗證過
- [ ] 備份了生產資料庫

---

**最後更新**：2025-10-21
**維護者**：Cheng
