#!/bin/bash
# ============================================
# CoolApps 後端映像建置腳本
# ============================================
# 描述：建置 Spring Boot 後端應用並推送到 Docker Registry
# 作者：Cheng
# ============================================

set -e  # 遇到錯誤立即退出

# ============================================
# 配置區
# ============================================
REGISTRY="android106"
IMAGE_NAME="coolapps-backend"
BASE_VERSION="v1.2.2"
DOCKERFILE="Dockerfile.backend-tomcat"
BUILD_CONTEXT="."

# ============================================
# 顏色輸出
# ============================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# 輔助函數
# ============================================
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_separator() {
    echo -e "${BLUE}============================================${NC}"
}

# ============================================
# 檢查必要工具
# ============================================
check_requirements() {
    print_info "檢查必要工具..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安裝，請先安裝 Docker"
        exit 1
    fi
    
    if ! docker buildx version &> /dev/null; then
        print_error "Docker Buildx 未安裝，請執行: docker buildx install"
        exit 1
    fi
    
    if [ ! -f "$DOCKERFILE" ]; then
        print_error "Dockerfile 不存在: $DOCKERFILE"
        exit 1
    fi
    
    if [ ! -f "pom.xml" ]; then
        print_error "pom.xml 不存在，請確認在專案根目錄執行"
        exit 1
    fi
    
    print_success "工具檢查通過"
}

# ============================================
# 生成版本號
# ============================================
generate_version() {
    local BUILD_DATE=$(date +%Y%m%d-%H%M)
    local GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    
    # 從 pom.xml 讀取版本（可選）
    if command -v mvn &> /dev/null; then
        POM_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null || echo "")
        if [ -n "$POM_VERSION" ]; then
            print_info "從 pom.xml 讀取到版本: $POM_VERSION"
        fi
    fi
    
    # 預設版本號格式：v1.2.2-20251013-0008
    VERSION_TAG="${BASE_VERSION}-${BUILD_DATE}"
    
    print_info "版本資訊："
    echo "  - 基礎版本: $BASE_VERSION"
    echo "  - 建置日期: $BUILD_DATE"
    echo "  - Git Commit: $GIT_COMMIT"
    echo "  - 完整標籤: $VERSION_TAG"
}

# ============================================
# 確認建置
# ============================================
confirm_build() {
    print_separator
    echo -e "${YELLOW}即將建置後端映像：${NC}"
    echo "  映像名稱: ${REGISTRY}/${IMAGE_NAME}"
    echo "  版本標籤: ${VERSION_TAG}"
    echo "  額外標籤: latest"
    echo "  平台架構: linux/amd64"
    echo "  Dockerfile: ${DOCKERFILE}"
    print_separator
    
    print_warning "注意：後端建置包含 Maven 編譯，可能需要較長時間（約 3-5 分鐘）"
    echo ""
    
    if [ "${AUTO_CONFIRM}" != "true" ]; then
        read -p "確認建置並推送？(y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_warning "已取消建置"
            exit 0
        fi
    fi
}

# ============================================
# 建置映像
# ============================================
build_image() {
    print_separator
    print_info "開始建置後端映像..."
    print_info "這可能需要幾分鐘時間，請耐心等待..."
    print_separator
    
    local BUILD_ARGS=""
    BUILD_ARGS="--build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
    BUILD_ARGS="$BUILD_ARGS --build-arg VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
    BUILD_ARGS="$BUILD_ARGS --build-arg VERSION=${VERSION_TAG}"
    
    docker buildx build \
        -f "$DOCKERFILE" \
        --platform linux/amd64 \
        $BUILD_ARGS \
        -t "${REGISTRY}/${IMAGE_NAME}:${VERSION_TAG}" \
        -t "${REGISTRY}/${IMAGE_NAME}:latest" \
        "$BUILD_CONTEXT" \
        --push
    
    if [ $? -eq 0 ]; then
        print_success "映像建置成功！"
    else
        print_error "映像建置失敗"
        exit 1
    fi
}

# ============================================
# 顯示建置資訊
# ============================================
show_build_info() {
    print_separator
    print_success "🎉 後端映像建置完成！"
    print_separator
    echo ""
    echo "📦 映像資訊："
    echo "  - ${REGISTRY}/${IMAGE_NAME}:${VERSION_TAG}"
    echo "  - ${REGISTRY}/${IMAGE_NAME}:latest"
    echo ""
    echo "🚀 部署指令："
    echo "  docker pull ${REGISTRY}/${IMAGE_NAME}:${VERSION_TAG}"
    echo ""
    echo "⚙️  環境變數提醒："
    echo "  部署時需設定以下環境變數："
    echo "    - JASYPT_ENCRYPTOR_PASSWORD (Jasypt 解密密碼)"
    echo "    - SPRING_PROFILES_ACTIVE (環境設定：prod/local)"
    echo "    - 資料庫連線相關環境變數"
    echo ""
    echo "🔍 查看映像："
    echo "  docker images | grep ${IMAGE_NAME}"
    echo ""
    print_separator
}

# ============================================
# 主程序
# ============================================
main() {
    print_separator
    print_info "CoolApps 後端建置腳本"
    print_separator
    echo ""
    
    check_requirements
    generate_version
    confirm_build
    build_image
    show_build_info
}

# ============================================
# 執行主程序
# ============================================
# 支援環境變數覆蓋
# 範例：AUTO_CONFIRM=true BASE_VERSION=v1.3.0 ./build-backend.sh
main "$@"
