#!/bin/bash

echo "======================================"
echo "VM 環境問題排查腳本"
echo "======================================"
echo ""

# 1. 檢查後端進程
echo "1. 檢查後端 Java 進程..."
ps aux | grep java | grep -v grep
if [ $? -eq 0 ]; then
    echo "✅ 後端進程正在執行"
else
    echo "❌ 後端進程未執行！"
fi
echo ""

# 2. 檢查後端埠號
echo "2. 檢查後端埠號 8080..."
netstat -tuln | grep 8080
if [ $? -eq 0 ]; then
    echo "✅ 埠號 8080 正在監聽"
else
    echo "❌ 埠號 8080 未監聽！"
fi
echo ""

# 3. 測試後端 API
echo "3. 測試後端 API (http://localhost:8080/)..."
curl -I http://localhost:8080/ 2>&1
echo ""

# 4. 測試後端健康檢查
echo "4. 測試後端健康檢查..."
curl -s http://localhost:8080/actuator/health 2>&1 || echo "健康檢查端點不可用"
echo ""

# 5. 檢查 Nginx 狀態
echo "5. 檢查 Nginx 狀態..."
systemctl status nginx --no-pager | head -10
echo ""

# 6. 檢查 Nginx 配置
echo "6. 測試 Nginx 配置..."
nginx -t
echo ""

# 7. 檢查前端靜態檔案
echo "7. 檢查前端靜態檔案..."
FRONTEND_DIR="/var/www/cheng-vue"  # 請根據實際情況修改
if [ -d "$FRONTEND_DIR" ]; then
    echo "✅ 前端目錄存在: $FRONTEND_DIR"
    ls -lh "$FRONTEND_DIR/index.html" 2>&1 || echo "❌ index.html 不存在"
else
    echo "❌ 前端目錄不存在: $FRONTEND_DIR"
fi
echo ""

# 8. 檢查 Nginx 錯誤日誌（最後 20 行）
echo "8. Nginx 錯誤日誌（最後 20 行）..."
tail -20 /var/log/nginx/cheng_vue_error.log 2>&1 || echo "日誌檔案不存在"
echo ""

# 9. 檢查後端日誌（最後 20 行）
echo "9. 後端日誌（最後 20 行）..."
tail -20 /opt/cool-apps/logs/sys-info.log 2>&1 || echo "日誌檔案不存在"
echo ""

# 10. 測試 HTTPS 訪問
echo "10. 測試 HTTPS 訪問..."
curl -I https://localhost/ -k 2>&1
echo ""

echo "======================================"
echo "排查完成"
echo "======================================"
