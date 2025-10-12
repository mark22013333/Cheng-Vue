#!/bin/bash
# ============================================
# CoolApps 完整建置腳本
# ============================================
# 描述：依序建置前端和後端映像
# 作者：Cheng
# ============================================

set -e

# ============================================
# 顏色輸出
# ============================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

print_header() {
    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════╗"
    echo "║     CoolApps 完整系統建置腳本               ║"
    echo "╚══════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_separator() {
    echo -e "${CYAN}════════════════════════════════════════════${NC}"
}

# ============================================
# 主程序
# ============================================
main() {
    print_header
    
    # 記錄開始時間
    START_TIME=$(date +%s)
    
    # 檢查腳本是否存在
    if [ ! -f "build-frontend.sh" ] || [ ! -f "build-backend.sh" ]; then
        print_error "建置腳本不存在，請確認 build-frontend.sh 和 build-backend.sh 存在"
        exit 1
    fi
    
    # 1. 建置前端
    print_separator
    print_info "步驟 1/2：建置前端映像"
    print_separator
    echo ""
    
    AUTO_CONFIRM=true bash build-frontend.sh
    
    if [ $? -ne 0 ]; then
        print_error "前端建置失敗，停止後續流程"
        exit 1
    fi
    
    echo ""
    print_success "前端建置完成"
    echo ""
    sleep 2
    
    # 2. 建置後端
    print_separator
    print_info "步驟 2/2：建置後端映像"
    print_separator
    echo ""
    
    AUTO_CONFIRM=true bash build-backend.sh
    
    if [ $? -ne 0 ]; then
        print_error "後端建置失敗"
        exit 1
    fi
    
    echo ""
    print_success "後端建置完成"
    echo ""
    
    # 計算總耗時
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    MINUTES=$((DURATION / 60))
    SECONDS=$((DURATION % 60))
    
    # 顯示總結
    print_separator
    print_success "🎉 全部建置完成！"
    print_separator
    echo ""
    echo "⏱️  總耗時: ${MINUTES} 分 ${SECONDS} 秒"
    echo ""
    echo "📦 建置的映像："
    echo "  - android106/coolapps-frontend:latest"
    echo "  - android106/coolapps-backend:latest"
    echo ""
    echo "🚀 後續步驟："
    echo "  1. 前往 Zeabur 控制台"
    echo "  2. 點擊「Redeploy」重新部署服務"
    echo "  3. 等待服務啟動完成"
    echo "  4. 測試應用功能是否正常"
    echo ""
    print_separator
}

main "$@"
