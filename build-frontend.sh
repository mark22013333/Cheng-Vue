#!/bin/bash
# ============================================
# CoolApps 前端映像建置腳本
# ============================================
# 描述：建置 Vue.js 前端應用並推送到 Docker Registry
# 作者：Cheng
# ============================================

set -e  # 遇到錯誤立即退出

# ============================================
# 配置區
# ============================================
REGISTRY="android106"
IMAGE_NAME="coolapps-frontend"
BASE_VERSION="v1.3.1"
DOCKERFILE="cheng-ui/Dockerfile"
BUILD_CONTEXT="cheng-ui"

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
    
    print_success "工具檢查通過"
}

# ============================================
# 產生版本號
# ============================================
generate_version() {
    local BUILD_DATE=$(date +%Y%m%d-%H%M)
    local GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    
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
    echo -e "${YELLOW}即將建置前端映像：${NC}"
    echo "  映像名稱: ${REGISTRY}/${IMAGE_NAME}"
    echo "  版本標籤: ${VERSION_TAG}"
    echo "  額外標籤: latest"
    echo "  平台架構: linux/amd64"
    echo "  Dockerfile: ${DOCKERFILE}"
    print_separator
    
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
    print_info "開始建置前端映像（使用詳細輸出模式）..."
    print_separator
    
    local BUILD_ARGS=""
    BUILD_ARGS="--build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
    BUILD_ARGS="$BUILD_ARGS --build-arg VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
    BUILD_ARGS="$BUILD_ARGS --build-arg VERSION=${VERSION_TAG}"
    
    docker buildx build \
        -f "$DOCKERFILE" \
        --platform linux/amd64 \
        --progress=plain \
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
    print_success "🎉 前端映像建置完成！"
    print_separator
    echo ""
    echo "📦 映像資訊："
    echo "  - ${REGISTRY}/${IMAGE_NAME}:${VERSION_TAG}"
    echo "  - ${REGISTRY}/${IMAGE_NAME}:latest"
    echo ""
    echo "🚀 部署指令："
    echo "  docker pull ${REGISTRY}/${IMAGE_NAME}:${VERSION_TAG}"
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
    print_info "CoolApps 前端建置腳本"
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
# 範例：AUTO_CONFIRM=true BASE_VERSION=v1.3.0 ./build-frontend.sh
main "$@"
