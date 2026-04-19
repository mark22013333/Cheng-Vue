#!/bin/bash

# ============================
# 自動執行所有部署 SQL
# ============================

# 設定顏色輸出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 設定資料庫連線資訊
DB_HOST="localhost"
DB_USER="root"
DB_NAME=""

# 函數：顯示標題
print_header() {
    echo -e "${GREEN}================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}================================${NC}"
}

# 函數：顯示錯誤
print_error() {
    echo -e "${RED}❌ 錯誤: $1${NC}"
}

# 函數：顯示成功
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# 函數：顯示警告
print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# 檢查參數
if [ $# -ne 1 ]; then
    print_error "請提供資料庫名稱"
    echo "使用方式: ./deploy_all.sh <資料庫名稱>"
    echo "範例: ./deploy_all.sh cool_apps_db"
    exit 1
fi

DB_NAME=$1

# 確認執行
print_header "部署 SQL 腳本"
echo "資料庫主機: $DB_HOST"
echo "資料庫名稱: $DB_NAME"
echo "使用者: $DB_USER"
echo ""
print_warning "執行前請確認已備份資料庫！"
read -p "是否繼續執行？(y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warning "取消執行"
    exit 1
fi

# 取得腳本所在目錄
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# SQL 檔案列表（按執行順序）
SQL_FILES=(
    "01_fix_dirty_book_data.sql"
    "02_fix_category_default.sql"
    "03_category_permissions.sql"
    "04_fix_category_menu.sql"
)

# 執行 SQL 檔案
for sql_file in "${SQL_FILES[@]}"; do
    file_path="$SCRIPT_DIR/$sql_file"
    
    if [ ! -f "$file_path" ]; then
        print_error "找不到檔案: $sql_file"
        exit 1
    fi
    
    print_header "執行: $sql_file"
    
    if mysql -h "$DB_HOST" -u "$DB_USER" -p "$DB_NAME" < "$file_path"; then
        print_success "$sql_file 執行成功"
    else
        print_error "$sql_file 執行失敗"
        exit 1
    fi
    
    echo ""
done

# 完成
print_header "部署完成"
print_success "所有 SQL 檔案已成功執行！"
echo ""
echo "接下來請執行："
echo "1. 重新編譯後端: mvn clean package -DskipTests"
echo "2. 重啟後端服務"
echo "3. 重新編譯前端: npm run build:prod"
echo "4. 清除瀏覽器快取並重新登入"
echo ""
print_warning "請參考 00_DEPLOYMENT_README.md 進行功能測試"
