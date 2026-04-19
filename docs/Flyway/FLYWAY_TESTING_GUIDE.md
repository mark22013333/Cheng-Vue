# 🧪 Flyway 測試指南

## 📋 測試目標

驗證 Flyway 資料庫遷移功能是否正常運作，確保：
- ✅ 遷移檔案可以正確執行
- ✅ 遷移歷史正確記錄
- ✅ 資料完整性正確
- ✅ 冪等性（重複執行不會出錯）
- ✅ 新遷移檔案可以正常新增

---

## 🎯 測試方案

### 方案一：全新資料庫測試（推薦）

**最完整的測試方式**，模擬全新部署情境。

#### 步驟 1：建立測試資料庫

```bash
# 登入 MySQL
mysql -u root -p

# 建立測試資料庫
CREATE DATABASE cool_apps_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 確認資料庫已建立
SHOW DATABASES LIKE 'cool_apps_test';

# 離開 MySQL
exit;
```

#### 步驟 2：設定測試環境配置

建立測試環境配置檔案（如果不存在）：

```yaml
# cheng-admin/src/main/resources/application-test.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cool_apps_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: ENC(你的加密使用者名稱)
    password: ENC(你的加密密碼)
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-version: 0
    baseline-on-migrate: true
    validate-on-migrate: true
    encoding: UTF-8

logging:
  file:
    path: /tmp/cool-logs-test
```

#### 步驟 3：執行測試腳本

使用提供的測試腳本：

```bash
# 執行 Flyway 測試
./docs/test-flyway.sh
```

或手動執行：

```bash
# 1. 啟動應用程式（使用測試環境）
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin
mvn spring-boot:run \
  -Dspring-boot.run.profiles=test \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido

# 2. 觀察啟動日誌，尋找 Flyway 執行訊息：
#    - "Flyway Community Edition"
#    - "Successfully applied X migrations"
#    - "Schema version: X"
```

#### 步驟 4：驗證遷移結果

```sql
-- 登入測試資料庫
mysql -u root -p cool_apps_test

-- 1. 檢查 Flyway 歷史表是否建立
SHOW TABLES LIKE 'flyway_schema_history';

-- 2. 查看所有遷移記錄
SELECT 
    installed_rank AS '執行順序',
    version AS '版本',
    description AS '描述',
    script AS '腳本名稱',
    installed_on AS '執行時間',
    execution_time AS '執行時間(ms)',
    success AS '是否成功'
FROM flyway_schema_history
ORDER BY installed_rank;

-- 3. 統計遷移數量
SELECT COUNT(*) AS '總遷移數量' FROM flyway_schema_history;

-- 4. 檢查是否有失敗的遷移
SELECT * FROM flyway_schema_history WHERE success = 0;
```

**預期結果**：
- 應該看到 8 筆遷移記錄（V0.1 到 V5）
- 所有記錄的 `success` 欄位都是 `1`
- 沒有失敗記錄

#### 步驟 5：驗證資料表是否正確建立

```sql
-- 檢查核心系統表
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'cool_apps_test' 
AND table_name LIKE 'sys_%';

-- 檢查 Quartz 表
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'cool_apps_test' 
AND table_name LIKE 'QRTZ_%';

-- 檢查庫存管理表
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'cool_apps_test' 
AND table_name LIKE 'inv_%';

-- 列出所有表
SHOW TABLES;
```

**預期結果**：
- 系統核心表：約 15 個（sys_ 開頭）
- Quartz 表：11 個（QRTZ_ 開頭）
- 庫存管理表：8 個（inv_ 開頭）

#### 步驟 6：測試冪等性

```bash
# 重新啟動應用程式
mvn spring-boot:run \
  -Dspring-boot.run.profiles=test \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

**預期結果**：
- 應用程式正常啟動
- 日誌顯示 "Schema version: 5"
- 不會重複執行已執行的遷移
- 沒有錯誤訊息

---

### 方案二：現有資料庫測試

如果你的本地資料庫已經有資料，可以快速驗證 Flyway 狀態。

#### 快速檢查

```sql
-- 檢查 Flyway 歷史表
USE cool_apps;

-- 查看遷移歷史
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 檢查最新版本
SELECT version, description FROM flyway_schema_history 
ORDER BY installed_rank DESC LIMIT 1;
```

#### 測試新增遷移

1. **建立測試遷移檔案**：

```bash
cat > /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin/src/main/resources/db/migration/V99__test_migration.sql << 'EOF'
-- ============================
-- 測試遷移檔案（測試完成後請刪除）
-- ============================

-- 建立測試表
CREATE TABLE IF NOT EXISTS test_flyway_migration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_column VARCHAR(100) NOT NULL COMMENT '測試欄位',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flyway測試表';

-- 新增測試資料
INSERT INTO test_flyway_migration (test_column) VALUES ('測試資料1');
EOF
```

2. **重啟應用程式**：

```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

3. **驗證新遷移**：

```sql
-- 檢查是否有 V99 版本
SELECT * FROM flyway_schema_history WHERE version = '99';

-- 檢查測試表是否建立
SHOW TABLES LIKE 'test_flyway_migration';

-- 檢查測試資料
SELECT * FROM test_flyway_migration;
```

4. **清理測試檔案**：

```bash
# 刪除測試表
mysql -u root -p cool_apps -e "DROP TABLE IF EXISTS test_flyway_migration;"

# 刪除遷移記錄
mysql -u root -p cool_apps -e "DELETE FROM flyway_schema_history WHERE version = '99';"

# 刪除測試遷移檔案
rm /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin/src/main/resources/db/migration/V99__test_migration.sql
```

---

### 方案三：使用 Maven Flyway 外掛

直接使用 Maven Flyway 外掛進行測試（無需啟動應用程式）。

#### 步驟 1：查看遷移資訊

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin

# 查看遷移狀態
mvn flyway:info \
  -Dflyway.url=jdbc:mysql://localhost:3306/cool_apps \
  -Dflyway.user=你的資料庫使用者 \
  -Dflyway.password=你的資料庫密碼
```

**預期輸出**：
```
+----------+---------+------------------------+------+--------------+---------+
| Category | Version | Description            | Type | Installed On | State   |
+----------+---------+------------------------+------+--------------+---------+
| Versioned| 0.1     | init system core       | SQL  | 2025-xx-xx   | Success |
| Versioned| 0.2     | init quartz tables     | SQL  | 2025-xx-xx   | Success |
| Versioned| 1       | init inventory tables  | SQL  | 2025-xx-xx   | Success |
| Versioned| 2       | init inventory data    | SQL  | 2025-xx-xx   | Success |
| Versioned| 3       | init inventory menu    | SQL  | 2025-xx-xx   | Success |
| Versioned| 4       | add lost qty column    | SQL  | 2025-xx-xx   | Success |
| Versioned| 5       | add book info tables   | SQL  | 2025-xx-xx   | Success |
+----------+---------+------------------------+------+--------------+---------+
```

#### 步驟 2：驗證遷移檔案

```bash
# 驗證所有遷移檔案的 checksum 是否正確
mvn flyway:validate \
  -Dflyway.url=jdbc:mysql://localhost:3306/cool_apps \
  -Dflyway.user=你的資料庫使用者 \
  -Dflyway.password=你的資料庫密碼
```

**預期輸出**：
```
[INFO] Successfully validated 7 migrations (execution time 00:00.023s)
```

---

## 🔍 常見測試場景

### 場景 1：測試遷移失敗處理

模擬遷移失敗並修正：

1. **建立故意失敗的遷移**：

```sql
-- V98__test_failure.sql
-- 故意寫錯的 SQL
SELEEEECT * FROM non_existent_table;
```

2. **啟動應用程式**：
   - 應用程式會啟動失敗
   - 查看錯誤日誌

3. **檢查資料庫**：

```sql
-- 查看失敗記錄
SELECT * FROM flyway_schema_history WHERE success = 0;
```

4. **修正問題**：

```bash
# 刪除失敗的遷移檔案
rm V98__test_failure.sql

# 刪除失敗記錄
mysql -u root -p cool_apps -e "DELETE FROM flyway_schema_history WHERE version = '98';"
```

### 場景 2：測試 checksum 驗證

模擬修改已執行的遷移檔案：

1. **修改已執行的遷移檔案**（例如 V5）
2. **重啟應用程式**
3. **預期結果**：應用程式啟動失敗，顯示 checksum 不符錯誤

**錯誤訊息範例**：
```
Migration checksum mismatch for migration version 5
-> Applied to database : 1234567890
-> Resolved locally    : 9876543210
```

**解決方式**：
- 不要修改已執行的遷移檔案
- 如需變更，建立新的遷移檔案

---

## 📊 測試結果評估

### ✅ 成功標準

Flyway 正常運作應該滿足：

1. **遷移執行成功**
   - 所有遷移檔案都被執行
   - `flyway_schema_history` 表有正確記錄
   - 所有記錄的 `success = 1`

2. **資料完整性**
   - 所有預期的表都被建立
   - 初始資料正確載入
   - 外鍵約束正確設定

3. **冪等性**
   - 重複啟動不會出錯
   - 不會重複執行已執行的遷移

4. **版本控制**
   - 版本號正確遞增
   - Checksum 驗證正常

### ❌ 常見問題

| 問題 | 可能原因 | 解決方式 |
|------|----------|----------|
| 應用程式啟動失敗 | SQL 語法錯誤 | 檢查遷移檔案的 SQL 語法 |
| Checksum 不符 | 修改了已執行的遷移 | 恢復原檔案或建立修正遷移 |
| 表已存在錯誤 | 手動建立了表 | 清理資料庫或使用 baseline |
| 連線失敗 | 資料庫配置錯誤 | 檢查 application.yml 配置 |

---

## 🛠️ 測試工具

### 1. 檢查腳本

執行快速健康檢查：

```bash
./docs/test-flyway.sh check
```

### 2. 完整測試

執行完整測試流程：

```bash
./docs/test-flyway.sh full
```

### 3. 清理測試環境

清理測試資料庫：

```bash
./docs/test-flyway.sh clean
```

---

## 📝 測試檢查清單

執行測試前：
- [ ] 已備份重要資料
- [ ] 資料庫服務正常執行
- [ ] Maven 環境配置正確
- [ ] Jasypt 密碼正確

測試過程中：
- [ ] 遷移檔案全部執行成功
- [ ] 沒有失敗記錄
- [ ] 所有表正確建立
- [ ] 初始資料載入正確

測試完成後：
- [ ] 驗證應用程式功能正常
- [ ] 檢查日誌無異常
- [ ] 清理測試資料（如使用測試資料庫）

---

## 🚀 快速測試命令

### 最快速的驗證方式

```bash
# 1. 檢查遷移歷史
mysql -u root -p cool_apps -e "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"

# 2. 檢查最新版本
mysql -u root -p cool_apps -e "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;"

# 3. 啟動應用程式（觀察 Flyway 日誌）
cd cheng-admin && mvn spring-boot:run -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

---

## 📚 相關文件

- [Flyway 官方測試文件](https://flywaydb.org/documentation/usage/commandline/validate)
- [專案 Flyway 說明](/cheng-admin/src/main/resources/db/migration/README.md)
- [Flyway 遷移指南](/sql/FLYWAY_MIGRATION_GUIDE.md)

---

**最後更新**：2025-10-21  
**維護者**：Cheng
