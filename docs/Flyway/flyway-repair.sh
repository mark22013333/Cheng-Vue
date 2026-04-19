#!/bin/bash

# Flyway Repair 腳本
# 用於重新計算 migration 檔案的 checksum

set -e

echo "================================"
echo "  Flyway Repair 工具"
echo "================================"
echo ""

# 切換到 cheng-admin 目錄
cd "$(dirname "$0")/cheng-admin"

echo "🔧 執行 Flyway Repair..."
echo ""

# 執行 flyway:repair
mvn flyway:repair -Dflyway.driver=com.mysql.cj.jdbc.Driver

echo ""
echo "✅ Flyway Repair 完成！"
echo ""
echo "📋 接下來你可以："
echo "   1. 檢查 flyway_schema_history 表確認 checksum 已更新"
echo "   2. 重新啟動應用程式"
echo ""
echo "💡 如需查看 migration 狀態，請執行："
echo "   cd cheng-admin && mvn flyway:info"
echo ""
