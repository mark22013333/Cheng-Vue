#!/bin/bash

# ============================
# Flyway 測試腳本
# ============================

set -e  # 遇到錯誤立即停止

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置變數
DB_NAME="cool_apps"
TEST_DB_NAME="cool_apps_test"
# 從環境變數讀取 Jasypt 密碼（請先 export JASYPT_ENCRYPTOR_PASSWORD）
JASYPT_PASSWORD="${JASYPT_ENCRYPTOR_PASSWORD:?請先 export JASYPT_ENCRYPTOR_PASSWORD}"
PROJECT_ROOT="/Users/cheng/IdeaProjects/R/Cheng-Vue"
ADMIN_MODULE="${PROJECT_ROOT}/cheng-admin"

# 函數：印出標題
print_header() {
    echo -e "\n${BLUE}==================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}==================================================${NC}\n"
}

# 函數：印出成功訊息
print_success() {
    echo -e "${GREEN}$1${NC}"
}

# 函數：印出錯誤訊息
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# 函數：印出警告訊息
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# 函數：印出資訊訊息
print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# 函數：檢查 MySQL 連線
check_mysql() {
    print_info "檢查 MySQL 連線..."
    
    if ! command -v mysql &> /dev/null; then
        print_error "MySQL 客戶端未安裝"
        exit 1
    fi
    
    if mysql -u root -p --execute="SELECT 1" &> /dev/null; then
        print_success "MySQL 連線正常"
    else
        print_error "無法連線到 MySQL，請確認密碼是否正確"
        exit 1
    fi
}

# 函數：檢查現有資料庫的 Flyway 狀態
check_existing_db() {
    print_header "檢查現有資料庫 Flyway 狀態"
    
    print_info "檢查資料庫: ${DB_NAME}"
    
    # 檢查資料庫是否存在
    if mysql -u root -p -e "USE ${DB_NAME}" 2>/dev/null; then
        print_success "資料庫 ${DB_NAME} 存在"
    else
        print_error "資料庫 ${DB_NAME} 不存在"
        return 1
    fi
    
    # 檢查 flyway_schema_history 表
    if mysql -u root -p ${DB_NAME} -e "SELECT 1 FROM flyway_schema_history LIMIT 1" &> /dev/null; then
        print_success "Flyway 歷史表存在"
        
        echo ""
        print_info "遷移歷史記錄："
        mysql -u root -p ${DB_NAME} -e "
            SELECT 
                installed_rank AS '順序',
                version AS '版本',
                description AS '描述',
                script AS '腳本',
                installed_on AS '執行時間',
                execution_time AS '耗時(ms)',
                success AS '成功'
            FROM flyway_schema_history 
            ORDER BY installed_rank;
        "
        
        # 統計資訊
        echo ""
        local total_count=$(mysql -u root -p ${DB_NAME} -N -e "SELECT COUNT(*) FROM flyway_schema_history")
        local success_count=$(mysql -u root -p ${DB_NAME} -N -e "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1")
        local failed_count=$(mysql -u root -p ${DB_NAME} -N -e "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 0")
        
        print_info "總遷移數量: ${total_count}"
        print_success "成功: ${success_count}"
        
        if [ ${failed_count} -gt 0 ]; then
            print_error "失敗: ${failed_count}"
            echo ""
            print_info "失敗的遷移記錄："
            mysql -u root -p ${DB_NAME} -e "SELECT * FROM flyway_schema_history WHERE success = 0"
        else
            print_success "失敗: 0"
        fi
        
        # 檢查最新版本
        echo ""
        local latest_version=$(mysql -u root -p ${DB_NAME} -N -e "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1")
        print_info "目前版本: ${latest_version}"
        
    else
        print_warning "Flyway 歷史表不存在（可能尚未執行過遷移）"
        return 1
    fi
}

# 函數：建立測試資料庫
create_test_db() {
    print_header "建立測試資料庫"
    
    print_info "建立資料庫: ${TEST_DB_NAME}"
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ${TEST_DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    
    print_success "測試資料庫建立完成"
}

# 函數：清理測試資料庫
clean_test_db() {
    print_header "清理測試資料庫"
    
    read -p "確定要刪除測試資料庫 ${TEST_DB_NAME} 嗎？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "刪除資料庫: ${TEST_DB_NAME}"
        mysql -u root -p -e "DROP DATABASE IF EXISTS ${TEST_DB_NAME}"
        print_success "測試資料庫已刪除"
    else
        print_info "取消操作"
    fi
}

# 函數：執行完整測試
full_test() {
    print_header "執行 Flyway 完整測試"
    
    # 1. 建立測試資料庫
    create_test_db
    
    # 2. 啟動應用程式進行測試
    print_info "準備啟動應用程式..."
    print_warning "請注意觀察 Flyway 相關日誌"
    print_info "按 Ctrl+C 停止應用程式"
    echo ""
    
    sleep 2
    
    cd ${ADMIN_MODULE}
    
    # 使用測試資料庫配置啟動
    print_info "啟動應用程式（使用本地配置，請手動修改為測試資料庫）..."
    mvn spring-boot:run \
        -Dspring-boot.run.profiles=local \
        -Djasypt.encryptor.password=${JASYPT_PASSWORD} || true
    
    echo ""
    print_info "應用程式已停止"
    
    # 3. 驗證結果
    echo ""
    read -p "是否要查看測試資料庫的遷移結果？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        check_test_result
    fi
}

# 函數：檢查測試結果
check_test_result() {
    print_header "檢查測試結果"
    
    if mysql -u root -p -e "USE ${TEST_DB_NAME}" 2>/dev/null; then
        print_success "測試資料庫存在"
        
        # 檢查 Flyway 歷史
        if mysql -u root -p ${TEST_DB_NAME} -e "SELECT 1 FROM flyway_schema_history LIMIT 1" &> /dev/null; then
            print_success "Flyway 遷移已執行"
            
            echo ""
            mysql -u root -p ${TEST_DB_NAME} -e "
                SELECT 
                    installed_rank AS '順序',
                    version AS '版本',
                    description AS '描述',
                    installed_on AS '執行時間',
                    execution_time AS '耗時(ms)',
                    success AS '成功'
                FROM flyway_schema_history 
                ORDER BY installed_rank;
            "
            
            # 檢查表數量
            echo ""
            print_info "檢查資料表..."
            
            local sys_count=$(mysql -u root -p ${TEST_DB_NAME} -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${TEST_DB_NAME}' AND table_name LIKE 'sys_%'")
            local qrtz_count=$(mysql -u root -p ${TEST_DB_NAME} -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${TEST_DB_NAME}' AND table_name LIKE 'QRTZ_%'")
            local inv_count=$(mysql -u root -p ${TEST_DB_NAME} -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${TEST_DB_NAME}' AND table_name LIKE 'inv_%'")
            
            print_info "系統核心表 (sys_*): ${sys_count}"
            print_info "Quartz 表 (QRTZ_*): ${qrtz_count}"
            print_info "庫存管理表 (inv_*): ${inv_count}"
            
            # 評估結果
            echo ""
            if [ ${sys_count} -gt 10 ] && [ ${qrtz_count} -eq 11 ] && [ ${inv_count} -ge 7 ]; then
                print_success "測試通過！所有表都正確建立"
            else
                print_warning "⚠ 表數量可能不符預期，請檢查"
            fi
        else
            print_error "Flyway 遷移未執行"
        fi
    else
        print_error "測試資料庫不存在"
    fi
}

# 函數：測試新增遷移
test_new_migration() {
    print_header "測試新增遷移檔案"
    
    local test_migration_file="${ADMIN_MODULE}/src/main/resources/db/migration/V99__test_migration.sql"
    
    # 建立測試遷移檔案
    print_info "建立測試遷移檔案: V99__test_migration.sql"
    cat > ${test_migration_file} << 'EOF'
-- ============================
-- 測試遷移檔案（測試完成後會自動刪除）
-- ============================

-- 建立測試表
CREATE TABLE IF NOT EXISTS test_flyway_migration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_column VARCHAR(100) NOT NULL COMMENT '測試欄位',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flyway測試表';

-- 新增測試資料
INSERT INTO test_flyway_migration (test_column) VALUES ('測試資料1'), ('測試資料2');
EOF
    
    print_success "測試遷移檔案已建立"
    
    # 提示使用者啟動應用程式
    echo ""
    print_warning "請按以下步驟操作："
    echo "1. 啟動應用程式"
    echo "2. 觀察 Flyway 是否執行 V99 遷移"
    echo "3. 按 Ctrl+C 停止應用程式"
    echo ""
    read -p "準備好了嗎？按 Enter 繼續..." 
    
    # 啟動應用程式
    cd ${ADMIN_MODULE}
    mvn spring-boot:run \
        -Dspring-boot.run.profiles=local \
        -Djasypt.encryptor.password=${JASYPT_PASSWORD} || true
    
    # 驗證測試遷移
    echo ""
    print_info "驗證測試遷移..."
    
    if mysql -u root -p ${DB_NAME} -e "SELECT 1 FROM flyway_schema_history WHERE version = '99' LIMIT 1" &> /dev/null; then
        print_success "測試遷移已執行"
        
        if mysql -u root -p ${DB_NAME} -e "SELECT 1 FROM test_flyway_migration LIMIT 1" &> /dev/null; then
            print_success "測試表已建立"
            mysql -u root -p ${DB_NAME} -e "SELECT * FROM test_flyway_migration"
        fi
    else
        print_error "測試遷移未執行"
    fi
    
    # 清理測試資料
    echo ""
    read -p "是否清理測試資料？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "清理測試資料..."
        
        mysql -u root -p ${DB_NAME} -e "DROP TABLE IF EXISTS test_flyway_migration" 2>/dev/null || true
        mysql -u root -p ${DB_NAME} -e "DELETE FROM flyway_schema_history WHERE version = '99'" 2>/dev/null || true
        rm -f ${test_migration_file}
        
        print_success "測試資料已清理"
    fi
}

# 函數：顯示幫助訊息
show_help() {
    echo "Flyway 測試腳本"
    echo ""
    echo "用法: ./test-flyway.sh [command]"
    echo ""
    echo "可用命令:"
    echo "  check         - 檢查現有資料庫的 Flyway 狀態"
    echo "  full          - 執行完整測試（建立測試資料庫並執行遷移）"
    echo "  test-new      - 測試新增遷移檔案"
    echo "  clean         - 清理測試資料庫"
    echo "  help          - 顯示此幫助訊息"
    echo ""
    echo "範例:"
    echo "  ./test-flyway.sh check       # 快速檢查現有資料庫"
    echo "  ./test-flyway.sh full        # 完整測試流程"
    echo "  ./test-flyway.sh test-new    # 測試新增遷移"
    echo ""
}

# 主程式
main() {
    print_header "Flyway 測試工具"
    
    # 檢查參數
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    case "$1" in
        check)
            check_existing_db
            ;;
        full)
            check_mysql
            full_test
            ;;
        test-new)
            check_mysql
            test_new_migration
            ;;
        clean)
            check_mysql
            clean_test_db
            ;;
        help)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 執行主程式
main "$@"
