# Flyway 資料庫遷移文件

本目錄包含 Flyway 資料庫版本控制的使用指南、測試方法和問題排除。

## 📚 文件列表

### 使用指南
- **FLYWAY_MIGRATION_GUIDE.md** - Flyway 遷移完整指南
- **FLYWAY_REPAIR_GUIDE.md** - Flyway Repair 使用說明（修改已執行的 migration 時使用）
- **FLYWAY_QUICK_CHECK.md** - Flyway 狀態快速檢查

### 測試與驗證
- **FLYWAY_TESTING_GUIDE.md** - Flyway 測試指南
- **check-flyway.sql** - Flyway 狀態檢查 SQL 腳本

### 技術文件
- **FLYWAY_BEAN_DEPENDENCY.md** - Flyway Bean 依賴關係說明

### 工具腳本
- **flyway-repair.sh** - Flyway Repair 自動執行腳本

## 🎯 常用指令

### Maven Plugin
```bash
# 查看 migration 狀態
mvn flyway:info

# 執行 migration
mvn flyway:migrate

# 修復 checksum（修改已執行的 migration 後使用）
mvn flyway:repair

# 清除資料庫（⚠️ 危險操作）
mvn flyway:clean
```

### 使用腳本
```bash
# 執行 Repair
./docs/Flyway/flyway-repair.sh
```

## 📝 Migration 檔案命名

```
V{版本號}__{描述}.sql

範例：
V0.1__init_system_core.sql
V0.2__add_inventory_tables.sql
V1.0__add_crawler_config.sql
```

## ⚠️ 重要注意事項

### Checksum 驗證
- Flyway 會計算每個 migration 檔案的 checksum
- 修改已執行的 migration 會導致驗證失敗
- 需要執行 `flyway:repair` 重新計算 checksum

### 正式環境
- ❌ 不要在正式環境執行 `flyway:clean`
- ❌ 不要修改已執行的 migration 檔案
- ✅ 使用新的 migration 檔案進行變更

### 開發環境
- ✅ 可以使用 `flyway:repair` 修復 checksum
- ✅ 可以使用 `flyway:clean` 清除資料庫重來

## 🔍 問題排除

### Checksum 不符
```bash
# 執行 repair
./docs/Flyway/flyway-repair.sh
```

### 查看執行歷史
```sql
SELECT * FROM flyway_schema_history 
ORDER BY installed_rank;
```

## 🔗 相關連結
- [部署指南](../Deployment/)
- [測試腳本](../Scripts/test-flyway.sh)
- [Flyway 官方文件](https://flywaydb.org/documentation/)
