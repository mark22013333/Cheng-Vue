#!/bin/bash

# 檢查最新的 Flyway 遷移版本號
# 使用方法：./check-latest-migration.sh

echo "=== 檢查最新的 Flyway 遷移版本 ==="

MIGRATION_DIR="$(dirname "$0")/../cheng-admin/src/main/resources/db/migration"

if [ ! -d "$MIGRATION_DIR" ]; then
    echo "❌ 錯誤：找不到遷移目錄 $MIGRATION_DIR"
    exit 1
fi

# 取得最新的版本號
# shellcheck disable=SC2010
LATEST_VERSION=$(ls -1 "$MIGRATION_DIR"/V*.sql | grep -E "V[0-9]+" | sort -V | tail -1 | grep -o "V[0-9]\+" | head -1)

if [ -z "$LATEST_VERSION" ]; then
    echo "❌ 錯誤：找不到任何遷移文件"
    exit 1
fi

echo "📌 最新版本號：$LATEST_VERSION"
echo "📌 下一個版本號應該是：V$(( ${LATEST_VERSION#V} + 1 ))"

# 顯示最近5個遷移文件
echo ""
echo "=== 最近的遷移文件 ==="
# shellcheck disable=SC2010
ls -1 "$MIGRATION_DIR"/V*.sql | grep -E "V[0-9]+" | sort -V | tail -5

echo ""
echo "⚠️  建立新遷移文件時，請使用版本號：V$(( ${LATEST_VERSION#V} + 1 ))"
echo "💡 範例：V$(( ${LATEST_VERSION#V} + 1 ))__your_migration_description.sql"
