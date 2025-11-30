#!/bin/bash

# 前端建置和部署腳本
# 用途：建置 Vue.js 應用程式並準備部署檔案

set -e  # 遇到錯誤立即停止

# 設定日誌
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
LOG_FILE="$PROJECT_ROOT/deploy/logs/build-$(date +%Y%m%d_%H%M%S).log"
mkdir -p "$(dirname "$LOG_FILE")"

# 日誌函數
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "🚀 開始建置 Cheng-Vue 前端應用程式..."

# 設定變數（可由環境覆蓋）
VUE_APP_DIR="${VUE_APP_DIR:-$PROJECT_ROOT/cheng-ui}"
BUILD_OUTPUT_DIR="${BUILD_OUTPUT_DIR:-$PROJECT_ROOT/cheng.deploy/frontend-dist}"
NGINX_CONFIG_DIR="${NGINX_CONFIG_DIR:-$PROJECT_ROOT/cheng.deploy/nginx}"

# 檢查 Node.js 和 pnpm
log "📋 檢查環境..."
if ! command -v node &> /dev/null; then
    log "❌ 錯誤: 未找到 Node.js，請先安裝 Node.js 18+ 版本"
    exit 1
fi

if ! command -v pnpm &> /dev/null; then
    log "❌ 錯誤: 未找到 pnpm，請先安裝 pnpm"
    exit 1
fi

# 檢查 Node.js 版本
NODE_VERSION=$(node --version | sed 's/v//')
NODE_MAJOR=$(echo $NODE_VERSION | cut -d. -f1)
if [ "$NODE_MAJOR" -lt 18 ]; then
    log "❌ 錯誤: Node.js 版本過低 ($NODE_VERSION)，需要 18+ 版本"
    exit 1
fi

log "✅ Node.js 版本: $(node --version)"
log "✅ pnpm 版本: $(pnpm --version)"

# 檢查 Vue.js 專案目錄
if [ ! -d "$VUE_APP_DIR" ]; then
    log "❌ 錯誤: Vue.js 專案目錄不存在: $VUE_APP_DIR"
    exit 1
fi

if [ ! -f "$VUE_APP_DIR/package.json" ]; then
    log "❌ 錯誤: 找不到 package.json 檔案"
    exit 1
fi

# 進入 Vue.js 專案目錄
cd "$VUE_APP_DIR"

# 安裝依賴（使用 pnpm）
log "📦 安裝前端依賴..."
if ! pnpm install 2>&1 | tee -a "$LOG_FILE"; then
    log "❌ 錯誤: pnpm install 失敗"
    exit 1
fi

# 清理舊的建置檔案
VUE_OUTPUT_DIR="$VUE_APP_DIR/dist"
log "🧹 清理舊的建置檔案..."
if [ -d "$VUE_OUTPUT_DIR" ]; then
    rm -rf "$VUE_OUTPUT_DIR"
    log "✅ 已清理舊的 dist 目錄"
fi

# 建置正式版本 (Vite 建置，不需要 NODE_OPTIONS)
log "🔨 建置正式版本..."
if ! pnpm run build:prod 2>&1 | tee -a "$LOG_FILE"; then
    log "❌ 錯誤: pnpm run build:prod 失敗"
    exit 1
fi

# 檢查建置結果 - Vite 輸出到 dist 目錄
if [ ! -d "$VUE_OUTPUT_DIR" ]; then
    log "❌ 錯誤: 建置失敗，找不到輸出目錄: $VUE_OUTPUT_DIR"
    exit 1
fi

if [ ! -f "$VUE_OUTPUT_DIR/index.html" ]; then
    log "❌ 錯誤: 建置失敗，找不到 index.html"
    exit 1
fi

# 建立部署目錄
cd "$PROJECT_ROOT"
mkdir -p "$BUILD_OUTPUT_DIR"
mkdir -p "$NGINX_CONFIG_DIR"

# 清理部署目錄
log "🧹 清理部署目錄..."
if [ -d "$BUILD_OUTPUT_DIR" ]; then
    rm -rf "$BUILD_OUTPUT_DIR"/*
    log "✅ 已清理部署目錄"
fi

# 複製建置檔案
log "📁 複製建置檔案..."
if ! cp -r "$VUE_OUTPUT_DIR/"* "$BUILD_OUTPUT_DIR/" 2>&1 | tee -a "$LOG_FILE"; then
    log "❌ 錯誤: 複製建置檔案失敗"
    exit 1
fi

# 建立建置時間戳記
BUILD_TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
echo "建置時間: $BUILD_TIMESTAMP" > "$BUILD_OUTPUT_DIR/build-info.txt"
echo "建置版本: $(cd "$VUE_APP_DIR" && pnpm version --json | grep '"cheng"' | cut -d'"' -f4)" >> "$BUILD_OUTPUT_DIR/build-info.txt"
log "✅ 已建立建置資訊檔案"

# 驗證複製結果
if [ ! -f "$BUILD_OUTPUT_DIR/index.html" ]; then
    log "❌ 錯誤: 複製失敗，找不到 index.html"
    exit 1
fi

# 建立目錄結構
echo "📂 建立部署目錄結構..."
echo "前端檔案已準備完成："
echo "  📁 $BUILD_OUTPUT_DIR/"
echo "    ├── 📄 index.html"
echo "    ├── 📁 static/"
echo "    │   ├── 📁 js/"
echo "    │   ├── 📁 css/"
echo "    │   ├── 📁 fonts/"
echo "    │   └── 📁 img/"
echo "    └── 📄 favicon.ico"

# 驗證建置結果
log "🔍 驗證建置結果..."
if [ -f "$BUILD_OUTPUT_DIR/index.html" ] && [ -d "$BUILD_OUTPUT_DIR/static" ]; then
    log "✅ 建置檔案驗證成功"
else
    log "❌ 錯誤: 建置檔案驗證失敗"
    exit 1
fi

log "✅ 前端建置完成！"
log "📍 建置檔案位置: $BUILD_OUTPUT_DIR"
log "📊 建置統計:"
log "  檔案數量: $(find "$BUILD_OUTPUT_DIR" -type f | wc -l)"
log "  總大小: $(du -sh "$BUILD_OUTPUT_DIR" | cut -f1)"
log "  建置時間: $BUILD_TIMESTAMP"
log "📋 日誌檔案: $LOG_FILE"
log "📄 建置資訊: $BUILD_OUTPUT_DIR/build-info.txt"
log "🔄 下一步: 執行 deploy-backend.sh 部署後端，然後執行 deploy-to-server.sh 部署前端到伺服器"
