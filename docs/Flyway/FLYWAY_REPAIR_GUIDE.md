# Flyway Repair 使用指南

## 什麼時候需要執行 Flyway Repair？

當你修改了**已經執行過**的 migration 檔案時，Flyway 會檢測到 checksum 不符，導致應用程式無法啟動。此時需要執行 `flyway repair` 來重新計算 checksum。

## 執行方式

### 方法 1：使用腳本（推薦）

```bash
# 在專案根目錄執行
./flyway-repair.sh
```

### 方法 2：手動執行 Maven 指令

```bash
# 切換到 cheng-admin 目錄
cd cheng-admin

# 執行 flyway:repair
mvn flyway:repair -Dflyway.driver=com.mysql.cj.jdbc.Driver
```

### 方法 3：在 IDE 中執行

在 IntelliJ IDEA 中：
1. 開啟 Maven 面板（View → Tool Windows → Maven）
2. 展開 `cheng-admin` → `Plugins` → `flyway`
3. 執行 `flyway:repair`

## Flyway 其他常用指令

### 檢視 Migration 狀態

```bash
cd cheng-admin
mvn flyway:info
```

### 執行 Migration

```bash
cd cheng-admin
mvn flyway:migrate
```

### 清除資料庫（⚠️ 危險操作）

```bash
cd cheng-admin
mvn flyway:clean
```

## 注意事項

1. **正式環境不應執行 repair**
   - `flyway:repair` 應該只在開發環境使用
   - 正式環境不應該修改已執行的 migration 檔案

2. **資料庫備份**
   - 執行任何 Flyway 操作前，建議先備份資料庫

3. **團隊協作**
   - 修改 migration 檔案後，需要通知團隊成員執行 repair
   - 或考慮建立新的 migration 檔案而不是修改舊的

## Flyway Schema History 表

Flyway 使用 `flyway_schema_history` 表來追蹤 migration 狀態：

```sql
-- 檢視 migration 歷史
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 查看特定版本的 checksum
SELECT version, description, checksum, installed_on 
FROM flyway_schema_history 
WHERE version = '0.1';
```

## 故障排除

### 問題 1：Checksum 驗證失敗

**錯誤訊息**：
```
Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 0.1
```

**解決方案**：
```bash
./flyway-repair.sh
```

### 問題 2：連線資料庫失敗

**檢查項目**：
- 資料庫是否正在執行
- `pom.xml` 中的資料庫連線資訊是否正確
- 防火牆設定是否允許連線

### 問題 3：找不到 Migration 檔案

**檢查項目**：
- Migration 檔案是否在 `src/main/resources/db/migration` 目錄下
- 檔案命名是否符合 Flyway 規範（如 `V0.1__init_system_core.sql`）

## 相關資源

- [Flyway 官方文件](https://flywaydb.org/documentation/)
- [Flyway Maven Plugin](https://flywaydb.org/documentation/usage/maven)
- [Migration 檔案命名規範](https://flywaydb.org/documentation/concepts/migrations#naming)

## 配置檔案

Flyway Maven Plugin 配置位於 `/cheng-admin/pom.xml`：

```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>11.14.1</version>
    <configuration>
        <url>jdbc:mysql://localhost:3306/cool_apps?...</url>
        <user>cool_apps</user>
        <password>Z7BKD.jc.gq23#6t</password>
        <locations>
            <location>classpath:db/migration</location>
        </locations>
    </configuration>
</plugin>
```

Spring Boot 配置位於 `/cheng-admin/src/main/resources/application.yml`：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-version: 0
    baseline-on-migrate: true
    validate-on-migrate: true
    encoding: UTF-8
```
