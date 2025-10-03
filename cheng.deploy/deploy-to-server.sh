#!/bin/bash

# Cheng-Vue 部署到 Linux 伺服器腳本
# 用途：將前端檔案和 Nginx 設定部署到伺服器

set -euo pipefail  # 遇到錯誤立即停止，未定義變數視為錯誤，管線錯誤即失敗

# 部署設定（可由環境覆蓋，避免把敏感資訊寫死在檔案中）
SERVER_HOST="${DEPLOY_HOST:-${SERVER_HOST:-example.com}}"
SERVER_USER="${DEPLOY_USER:-${SERVER_USER:-deploy}}"
SERVER_PORT="${SSH_PORT:-${SERVER_PORT:-22}}"
FRONTEND_DIR="${FRONTEND_DIR:-/var/www/cheng-vue/frontend}"
NGINX_CONFIG_DIR="${NGINX_CONFIG_DIR:-/etc/nginx/conf.d}"

# 本機檔案路徑（可由環境覆蓋）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOCAL_FRONTEND_DIR="${LOCAL_FRONTEND_DIR:-frontend-dist}"
LOCAL_NGINX_CONFIG="${LOCAL_NGINX_CONFIG:-$SCRIPT_DIR/nginx/proxy-ssl-corrected.conf}"

echo "🚀 開始部署 Cheng-Vue 前端到伺服器..."

# 檢查本機檔案是否存在
if [ ! -d "$LOCAL_FRONTEND_DIR" ]; then
    echo "❌ 錯誤: 找不到前端建置檔案，請先執行 build-frontend.sh"
    exit 1
fi

if [ ! -f "$LOCAL_NGINX_CONFIG" ]; then
    echo "❌ 錯誤: 找不到 Nginx 設定檔案"
    exit 1
fi

echo "📋 部署設定："
echo "  🖥️  伺服器: $SERVER_USER@$SERVER_HOST:$SERVER_PORT"
echo "  📁 前端目錄: $FRONTEND_DIR"
echo "  ⚙️  Nginx 設定: $NGINX_CONFIG_DIR"

# 非互動模式（CI 友善）
echo "✅ 確認部署（非互動模式）"

echo "📦 開始上傳檔案..."

# 建立伺服器目錄並設定權限
echo "📁 建立伺服器目錄..."
ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$HOME/.ssh/known_hosts" \
  "$SERVER_USER@$SERVER_HOST" "sudo mkdir -p '$FRONTEND_DIR' && sudo chown -R '$SERVER_USER':'$SERVER_USER' '$FRONTEND_DIR' && sudo chmod -R 755 '$FRONTEND_DIR'"

# 清除目標目錄
echo "🧹 與目標目錄同步（刪除遠端多餘檔案）..."

# 上傳前端檔案
echo "📤 上傳前端檔案..."
rsync -avz --delete -e "ssh -p $SERVER_PORT -o StrictHostKeyChecking=yes -o UserKnownHostsFile=$HOME/.ssh/known_hosts" \
  "$LOCAL_FRONTEND_DIR/" "$SERVER_USER@$SERVER_HOST:$FRONTEND_DIR/"

# 上傳 Nginx 設定（以環境變數套版）
echo "⚙️ 準備並上傳 Nginx 設定（注入 DEPLOY_HOST）..."
TMP_NGINX_FILE="$(mktemp)"
trap 'rm -f "$TMP_NGINX_FILE"' EXIT
if command -v envsubst >/dev/null 2>&1; then
  DEPLOY_HOST="$SERVER_HOST" FRONTEND_DIR="$FRONTEND_DIR" \
    envsubst '${DEPLOY_HOST} ${FRONTEND_DIR}' < "$LOCAL_NGINX_CONFIG" > "$TMP_NGINX_FILE"
else
  # 簡易後備：處理 ${DEPLOY_HOST} 與 ${FRONTEND_DIR}
  sed "s#\${DEPLOY_HOST}#${SERVER_HOST}#g; s#\${FRONTEND_DIR}#${FRONTEND_DIR}#g" \
    "$LOCAL_NGINX_CONFIG" > "$TMP_NGINX_FILE"
fi

scp -P "$SERVER_PORT" -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$HOME/.ssh/known_hosts" \
  "$TMP_NGINX_FILE" "$SERVER_USER@$SERVER_HOST:/tmp/proxy-ssl-new.conf"

# 在伺服器上執行設定
echo "🔧 設定 Nginx..."
ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$HOME/.ssh/known_hosts" \
  "$SERVER_USER@$SERVER_HOST" << EOF
    # 備份現有設定
    if [ -f "$NGINX_CONFIG_DIR/proxy-ssl.conf" ]; then
      sudo cp "$NGINX_CONFIG_DIR/proxy-ssl.conf" "$NGINX_CONFIG_DIR/proxy-ssl.conf.backup.$(date +%Y%m%d_%H%M%S)"
    fi
    
    # 移動新設定到正確位置
    sudo mv /tmp/proxy-ssl-new.conf "$NGINX_CONFIG_DIR/proxy-ssl.conf"
    sudo chown root:root "$NGINX_CONFIG_DIR/proxy-ssl.conf"
    sudo chmod 644 "$NGINX_CONFIG_DIR/proxy-ssl.conf"
    
    # 測試 Nginx 設定
    echo "🧪 測試 Nginx 設定..."
    sudo nginx -t
    
    if [ $? -eq 0 ]; then
        echo "✅ Nginx 設定測試通過"
        
        # 重新載入 Nginx
        echo "🔄 重新載入 Nginx..."
        sudo systemctl reload nginx
        
        # 確認 Nginx 開機自動啟動
        sudo systemctl enable nginx
        
        echo "✅ Nginx 已重新載入"
    else
        echo "❌ Nginx 設定測試失敗"
        exit 1
    fi
    
    # 檢查服務狀態
    echo "📊 檢查服務狀態..."
    echo "Nginx 狀態:"
    sudo systemctl status nginx --no-pager -l
    
    echo "Tomcat 狀態:"
    sudo systemctl status tomcat10 --no-pager -l || echo "請確認 Tomcat10 正在執行"
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 部署完成！"
    echo ""
    echo "📍 訪問地址:"
    echo "  🌐 前端應用: https://$SERVER_HOST"
    echo "  🔧 後端 API: https://$SERVER_HOST/prod-api"
    echo "  📚 API 文檔: https://$SERVER_HOST/prod-api/swagger-ui/index.html"
    echo ""
    echo "📋 後續步驟:"
    echo "  1. 確認 Tomcat10 正在執行並已部署 apps WAR"
    echo "  2. 檢查防火牆設定 (開放 80, 443 端口)"
    echo "  3. SSL 憑證已設定 (Let's Encrypt)"
    echo "  4. 域名 DNS 解析已設定"
    echo ""
    echo "🔍 故障排除:"
    echo "  - 檢查 Nginx 日誌: sudo tail -f /var/log/nginx/cheng_vue_error.log"
    echo "  - 檢查 Tomcat 日誌: sudo tail -f /usr/libexec/tomcat10/logs/catalina.out"
    echo "  - 測試後端 API: curl https://localhost/prod-api/system/user/profile"
else
    echo "❌ 部署過程中發生錯誤"
    exit 1
fi
