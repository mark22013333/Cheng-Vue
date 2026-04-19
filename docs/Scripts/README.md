# 工具腳本

本目錄包含部署、測試、除錯用的各類腳本與 SQL 檔案。

## 📚 檔案列表

### 部署腳本
- **deploy_all.sh** - 完整部署腳本（前端 + 後端）

### 測試腳本
- **test-flyway.sh** - Flyway 資料庫遷移測試
- **test-task-types.sh** - 任務類型功能測試
- **test-db-connection.sql** - 資料庫連線測試

### 資料庫設定
- **setup-cool-test-db.sh** - 測試資料庫建立腳本

### 除錯工具
- **debug-stock-status.sql** - 庫存狀態除錯 SQL
- **diagnose-flyway-issue.md** - Flyway 問題診斷指南

### 環境配置
- **tomcat-setenv.sh** - Tomcat 環境變數設定腳本

## 🚀 使用方式

### 部署專案
```bash
# 完整部署（需要權限）
./docs/Scripts/deploy_all.sh
```

### 測試資料庫
```bash
# 建立測試資料庫
./docs/Scripts/setup-cool-test-db.sh

# 測試資料庫連線
mysql < ./docs/Scripts/test-db-connection.sql
```

### 測試 Flyway
```bash
# 執行 Flyway 測試
./docs/Scripts/test-flyway.sh
```

### 除錯庫存狀態
```sql
-- 在 MySQL 中執行
source ./docs/Scripts/debug-stock-status.sql
```

## ⚠️ 注意事項

### 正式環境
- ❌ 不要在正式環境執行測試腳本
- ❌ 不要執行 `setup-cool-test-db.sh` 清除正式資料
- ✅ 部署前先在測試環境驗證

### 測試環境
- ✅ 可以自由執行所有測試腳本
- ✅ 使用獨立的測試資料庫
- ✅ 定期重建測試環境

### 腳本權限
```bash
# 給予執行權限
chmod +x *.sh
```

## 🔗 相關連結
- [部署指南](../Deployment/)
- [Flyway 文件](../Flyway/)
- [快速開始](../Guides/QUICK_START.md)
