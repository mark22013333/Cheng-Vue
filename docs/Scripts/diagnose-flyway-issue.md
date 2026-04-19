# 🔍 Flyway 未建立資料表問題診斷

## 問題描述
建立了新資料庫 `cool-test`，但啟動服務時 Flyway 沒有建立資料表。

## 可能原因分析

### ⚠️ 原因 1：Flyway 11.14.1 版本問題

你將 Flyway 從 10.21.0 升級到 **11.14.1**，這個新版本可能：
- 有新的配置要求
- 與 MySQL Driver 或 Spring Boot 版本不相容
- 需要額外的依賴

**建議**：先回退到穩定版本

```xml
<!-- pom.xml -->
<flyway.version>10.21.0</flyway.version>
```

### ⚠️ 原因 2：資料庫連線問題

資料庫連線配置：
```yaml
url: jdbc:mysql://localhost:23506/cool-test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
```

**檢查項目**：
- Port 23506 是否正確（通常是 3306）
- 資料庫 `cool-test` 是否已建立
- 使用者名稱和密碼是否正確

### ⚠️ 原因 3：Profile 未正確啟用

確認使用的是 `local` profile：

```bash
# 正確的啟動方式
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### ⚠️ 原因 4：Flyway 遷移檔案路徑問題

檢查遷移檔案是否存在：
```bash
ls -la cheng-admin/src/main/resources/db/migration/
```

應該看到：
- V0.1__init_system_core.sql
- V0.2__init_quartz_tables.sql
- V1__init_inventory_tables.sql
- 等等...

### ⚠️ 原因 5：已修改的遷移檔案導致 Checksum 錯誤

你修改了 `V0.1__init_system_core.sql` 的內容（更改 email、部門名稱等），如果資料庫中已經有 `flyway_schema_history` 記錄，會導致 checksum 不符。

---

## 🛠️ 診斷步驟

### 步驟 1：檢查資料庫連線

```bash
# 連線到資料庫（請替換成你的實際密碼）
mysql -u root -p -P 23506 -h localhost

# 在 MySQL 中執行
SHOW DATABASES;
USE cool-test;
SHOW TABLES;
```

**預期結果**：
- 可以成功連線
- 看到 `cool-test` 資料庫
- 如果 Flyway 有執行，應該至少有 `flyway_schema_history` 表

### 步驟 2：啟動應用程式並查看日誌

```bash
cd cheng-admin

# 啟動應用程式
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

**觀察日誌中的關鍵字**：

#### ✅ 成功的日誌範例：
```
[INFO] o.f.c.i.license.VersionPrinter: Flyway Community Edition 11.14.1
[INFO] o.f.c.i.database.base.BaseDatabaseType: Database: jdbc:mysql://localhost:23506/cool-test
[INFO] o.f.core.internal.command.DbValidate: Successfully validated 8 migrations
[INFO] o.f.c.i.s.JdbcTableSchemaHistory: Creating Schema History table `cool-test`.`flyway_schema_history`
[INFO] o.f.core.internal.command.DbMigrate: Current version of schema `cool-test`: << Empty Schema >>
[INFO] o.f.core.internal.command.DbMigrate: Migrating schema `cool-test` to version "0.1 - init system core"
[INFO] o.f.core.internal.command.DbMigrate: Migrating schema `cool-test` to version "0.2 - init quartz tables"
[INFO] o.f.core.internal.command.DbMigrate: Successfully applied 8 migrations to schema `cool-test`
```

#### ❌ 錯誤的日誌範例：
```
[ERROR] Connection refused
[ERROR] Access denied for user
[ERROR] Unknown database 'cool-test'
[ERROR] Migration checksum mismatch
[ERROR] Flyway failed to initialize
```

### 步驟 3：檢查 Flyway 執行結果

```sql
-- 連線到資料庫
USE cool-test;

-- 檢查 Flyway 歷史表
SELECT 
    installed_rank,
    version,
    description,
    success,
    installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

-- 檢查表數量
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'cool-test';
```

---

## 🔧 解決方案

### 解決方案 1：回退 Flyway 版本（推薦）

修改 `pom.xml`：

```xml
<flyway.version>10.21.0</flyway.version>
```

然後重新載入 Maven 依賴：

```bash
cd cheng-admin
mvn clean
mvn compile
```

### 解決方案 2：確認資料庫連線資訊

確認 Port 是否正確：

```yaml
# application-local.yml
url: jdbc:mysql://localhost:3306/cool-test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
```

如果你的 MySQL 確實在 23506 Port，請確認：

```bash
# 檢查 MySQL 是否在該 Port 執行
lsof -i :23506
# 或
netstat -an | grep 23506
```

### 解決方案 3：清除舊的遷移記錄（如果資料庫不是全新的）

如果 `cool-test` 資料庫之前有執行過舊的遷移檔案，而你修改了 `V0.1__init_system_core.sql`，會導致 checksum 錯誤。

**解決方式**：

```sql
-- 刪除資料庫重新建立
DROP DATABASE IF EXISTS cool-test;
CREATE DATABASE cool-test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然後重新啟動應用程式。

### 解決方案 4：啟用更詳細的 Flyway 日誌

修改 `application.yml`：

```yaml
logging:
  level:
    com.cheng: debug
    org.springframework: warn
    org.flywaydb: debug  # 新增這行
```

重新啟動後可以看到更詳細的 Flyway 執行資訊。

### 解決方案 5：手動執行 Flyway（測試用）

如果應用程式啟動有問題，可以先用 Maven Flyway 外掛測試：

```bash
cd cheng-admin

# 檢查遷移狀態
mvn flyway:info \
  -Dflyway.url=jdbc:mysql://localhost:23506/cool-test \
  -Dflyway.user=root \
  -Dflyway.password=你的密碼 \
  -Dflyway.locations=classpath:db/migration

# 執行遷移
mvn flyway:migrate \
  -Dflyway.url=jdbc:mysql://localhost:23506/cool-test \
  -Dflyway.user=root \
  -Dflyway.password=你的密碼 \
  -Dflyway.locations=classpath:db/migration
```

---

## 📋 快速檢查清單

- [ ] 確認 MySQL 在 23506 Port 執行（或改回 3306）
- [ ] 確認 `cool-test` 資料庫已建立
- [ ] 回退 Flyway 版本到 10.21.0
- [ ] 啟動時加上正確的 profile 和 Jasypt 密碼
- [ ] 檢查啟動日誌中的 Flyway 訊息
- [ ] 如果有 checksum 錯誤，刪除資料庫重建

---

## 🎯 最可能的原因

根據你的修改，**最可能的原因是 Flyway 版本升級導致的問題**。

### 建議處理步驟：

1. **回退 Flyway 版本到 10.21.0**
2. **確認 MySQL Port（通常是 3306，而不是 23506）**
3. **如果資料庫不是全新的，刪除後重建**
4. **重新啟動應用程式**

```bash
# 1. 修改 pom.xml 的 Flyway 版本
# <flyway.version>10.21.0</flyway.version>

# 2. 確認資料庫是全新的
mysql -u root -p -e "DROP DATABASE IF EXISTS cool-test; CREATE DATABASE cool-test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 重新啟動
cd cheng-admin
mvn clean compile
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

---

**請先執行上述診斷步驟，並將啟動日誌中的 Flyway 相關訊息提供給我，我可以進一步協助你解決問題。**
