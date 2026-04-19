# 🚀 Flyway 快速檢查指南

## 最快速的測試方式

### 方法一：使用自動化腳本 ⭐️ 推薦

```bash
# 1. 快速檢查現有資料庫狀態
./docs/test-flyway.sh check

# 2. 執行完整測試（建立測試資料庫）
./docs/test-flyway.sh full

# 3. 測試新增遷移檔案
./docs/test-flyway.sh test-new

# 4. 清理測試環境
./docs/test-flyway.sh clean
```

---

### 方法二：使用 SQL 檢查腳本

```bash
# 執行完整檢查
mysql -u root -p cool_apps < docs/check-flyway.sql
```

---

### 方法三：手動 SQL 檢查

#### 1️⃣ 檢查遷移歷史（最基本）

```sql
-- 登入資料庫
mysql -u root -p cool_apps

-- 查看所有遷移記錄
SELECT 
    installed_rank AS '順序',
    version AS '版本',
    description AS '描述',
    installed_on AS '執行時間',
    execution_time AS '耗時(ms)',
    success AS '成功'
FROM flyway_schema_history 
ORDER BY installed_rank;
```

**預期結果**：應該看到 8 筆記錄（V0.1, V0.2, V1-V5），所有 `success` 都是 `1`

#### 2️⃣ 檢查最新版本

```sql
SELECT version, description 
FROM flyway_schema_history 
ORDER BY installed_rank DESC 
LIMIT 1;
```

**預期結果**：版本應該是 `5`（或更高）

#### 3️⃣ 檢查是否有失敗記錄

```sql
SELECT * FROM flyway_schema_history WHERE success = 0;
```

**預期結果**：應該沒有任何記錄（空結果）

#### 4️⃣ 檢查表是否正確建立

```sql
-- 檢查庫存管理表
SELECT table_name, table_comment 
FROM information_schema.tables 
WHERE table_schema = 'cool_apps' 
AND table_name LIKE 'inv_%'
ORDER BY table_name;
```

**預期結果**：應該看到至少 7 個表（inv_category, inv_item, inv_stock 等）

---

### 方法四：啟動應用程式觀察日誌

```bash
cd cheng-admin

# 啟動應用程式
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

**觀察重點**：

在啟動日誌中尋找以下關鍵字：

```
✅ 正常日誌範例：
[INFO] Flyway Community Edition 10.21.0
[INFO] Database: jdbc:mysql://localhost:3306/cool_apps
[INFO] Schema version: 5
[INFO] Successfully validated X migrations
[INFO] Schema version is up to date
```

```
❌ 異常日誌範例：
[ERROR] Migration checksum mismatch
[ERROR] Validate failed: Migration checksum mismatch
[ERROR] Unable to connect to database
```

---

## 🎯 快速判斷標準

### ✅ Flyway 正常運作

- [ ] `flyway_schema_history` 表存在
- [ ] 有 8 筆以上遷移記錄
- [ ] 所有記錄的 `success = 1`
- [ ] 最新版本是 5 或更高
- [ ] 所有庫存管理表（inv_*）都存在
- [ ] 應用程式啟動無錯誤

### ❌ 可能有問題

- 沒有 `flyway_schema_history` 表 → Flyway 未執行
- 有 `success = 0` 的記錄 → 遷移失敗
- 表數量不足 → 遷移不完整
- 啟動時出現 checksum 錯誤 → 檔案被修改

---

## 🛠️ 常用測試命令速查

### 一行命令檢查

```bash
# 檢查版本
mysql -u root -p cool_apps -e "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1"

# 檢查失敗記錄
mysql -u root -p cool_apps -e "SELECT COUNT(*) AS failed FROM flyway_schema_history WHERE success = 0"

# 檢查表數量
mysql -u root -p cool_apps -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'cool_apps' AND table_name LIKE 'inv_%'"

# 完整狀態
mysql -u root -p cool_apps -e "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"
```

### Maven Flyway 指令

```bash
cd cheng-admin

# 查看遷移資訊
mvn flyway:info \
  -Dflyway.url=jdbc:mysql://localhost:3306/cool_apps \
  -Dflyway.user=root \
  -Dflyway.password=你的密碼

# 驗證遷移狀態
mvn flyway:validate \
  -Dflyway.url=jdbc:mysql://localhost:3306/cool_apps \
  -Dflyway.user=root \
  -Dflyway.password=你的密碼
```

---

## ⚡ 30 秒快速檢查流程

```bash
# 步驟 1：執行自動化腳本（20 秒）
./docs/test-flyway.sh check

# 步驟 2：如果發現問題，查看詳細日誌（10 秒）
mysql -u root -p cool_apps < docs/check-flyway.sql
```

完成！ 🎉

---

## 📊 健康度評分標準

| 評分 | 條件 | 說明 |
|-----|------|------|
| 🟢 優秀 (100%) | 所有遷移成功 + 表完整 + 無錯誤 | Flyway 完美運作 |
| 🟡 良好 (80-99%) | 遷移成功但有警告 | 基本功能正常，需檢查警告 |
| 🟠 注意 (60-79%) | 有少量失敗記錄 | 需要修正失敗的遷移 |
| 🔴 異常 (<60%) | 多個失敗或表缺失 | 需要重新部署 |

---

## 🔧 問題排除速查

| 問題 | 快速解決 |
|------|---------|
| 找不到 `flyway_schema_history` | 啟動應用程式，Flyway 會自動建立 |
| Checksum 不符 | 不要修改已執行的遷移檔案 |
| 遷移失敗 | 檢查 SQL 語法，刪除失敗記錄後重試 |
| 表已存在錯誤 | 使用 `IF NOT EXISTS` 或清空資料庫重建 |

---

## 📚 相關文件

- **詳細測試指南**：[FLYWAY_TESTING_GUIDE.md](./FLYWAY_TESTING_GUIDE.md)
- **Flyway 說明**：[../cheng-admin/src/main/resources/db/migration/README.md](../cheng-admin/src/main/resources/db/migration/README.md)
- **遷移指南**：[../sql/FLYWAY_MIGRATION_GUIDE.md](../sql/FLYWAY_MIGRATION_GUIDE.md)

---

**最後更新**：2025-10-21  
**預計閱讀時間**：3 分鐘
