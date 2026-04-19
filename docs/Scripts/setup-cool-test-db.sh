#!/bin/bash

# ============================
# 建立 cool-test 資料庫腳本
# ============================

set -e

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}==================================================${NC}"
echo -e "${BLUE}  建立 cool-test 資料庫${NC}"
echo -e "${BLUE}==================================================${NC}\n"

# 檢查 MySQL 連線
echo -e "${BLUE}正在連線到 MySQL...${NC}"

# 請根據你的 MySQL 配置修改 Port 和密碼
MYSQL_PORT=${1:-23506}  # 預設使用 23506，也可以用 3306
MYSQL_HOST="localhost"
DB_NAME="cool-test"

echo -e "${BLUE}MySQL 連線資訊：${NC}"
echo "  - Host: ${MYSQL_HOST}"
echo "  - Port: ${MYSQL_PORT}"
echo "  - Database: ${DB_NAME}"
echo ""

# 建立資料庫
echo -e "${BLUE}建立資料庫 ${DB_NAME}...${NC}"
mysql -u root -p -P ${MYSQL_PORT} -h ${MYSQL_HOST} <<EOF
-- 刪除舊資料庫（如果存在）
DROP DATABASE IF EXISTS ${DB_NAME};

-- 建立新資料庫
CREATE DATABASE ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 顯示所有資料庫
SHOW DATABASES;

-- 使用新資料庫
USE ${DB_NAME};

-- 顯示目前資料庫的表（應該是空的）
SHOW TABLES;
EOF

echo ""
echo -e "${GREEN}資料庫 ${DB_NAME} 已成功建立！${NC}"
echo ""
echo -e "${BLUE}下一步：${NC}"
echo "1. 確認 application-local.yml 中的資料庫連線設定"
echo "2. 啟動應用程式："
echo ""
echo "   cd cheng-admin"
echo "   mvn spring-boot:run \\"
echo "     -Dspring-boot.run.profiles=local \\"
echo "     -Djasypt.encryptor.password=\"\$JASYPT_ENCRYPTOR_PASSWORD\""
echo ""
echo "（請先 export JASYPT_ENCRYPTOR_PASSWORD，詳見 ~/.zshrc 或 CLAUDE.md）"
echo ""
