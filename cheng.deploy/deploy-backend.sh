#!/bin/bash

# Cheng-Vue 後端 WAR 檔案編譯和部署腳本
# 用途：編譯 Spring Boot 應用程式為 WAR 檔案並部署到伺服器

set -euo pipefail  # 遇到錯誤立即停止，未定義變數視為錯誤，管線錯誤即失敗

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 設定變數（可由環境覆蓋，避免把敏感資訊寫死在檔案中）
# 例如在 GitHub Actions 以 secrets 傳入：DEPLOY_USER/DEPLOY_HOST/SSH_PORT/TOMCAT_WEBAPPS_DIR
SERVER_USER="${DEPLOY_USER:-${SERVER_USER:-cheng}}"
SERVER_HOST="${DEPLOY_HOST:-${SERVER_HOST:-example.com}}"
SERVER_PORT="${SSH_PORT:-${SERVER_PORT:-22}}"
TOMCAT_WEBAPPS_DIR="${TOMCAT_WEBAPPS_DIR:-/usr/libexec/tomcat10/webapps}"
APP_NAME="${APP_NAME:-apps}"

# 可選：使用既有 WAR 檔（CI 產物）時，設定 WAR_FILE 路徑即可略過本地編譯
WAR_FILE="${WAR_FILE:-}"

# 健康檢查設定
HEALTH_PROTOCOL="${HEALTH_PROTOCOL:-https}"
HEALTH_PATH="${HEALTH_PATH:-/prod-api/system/user/profile}"

echo -e "${BLUE}=== Cheng-Vue 後端部署腳本 ===${NC}"
echo -e "${YELLOW}開始編譯和部署流程...${NC}"

if [ -z "$WAR_FILE" ]; then
  # 步驟 1: 清理並編譯 WAR 檔案
  echo -e "\n${BLUE}步驟 1: 編譯 WAR 檔案${NC}"
  echo "執行 Maven 清理和打包..."
  # 切換到專案根目錄
  cd "$(dirname "$0")/.."
  mvn clean package -DskipTests

  if [ $? -ne 0 ]; then
      echo -e "${RED}❌ Maven 編譯失敗！${NC}"
      exit 1
  fi

  echo -e "${GREEN}✅ WAR 檔案編譯成功${NC}"

  # 預設 WAR 檔案 - cheng-admin 模組的 target 目錄
  WAR_FILE="cheng-admin/target/${APP_NAME}.war"
fi

# 檢查 WAR 檔案是否存在
if [ ! -f "$WAR_FILE" ]; then
  echo -e "${RED}❌ WAR 檔案不存在: $WAR_FILE${NC}"
  echo -e "${YELLOW}💡 提示：請確認 cheng-admin 模組已正確編譯或於環境變數提供 WAR_FILE${NC}"
  exit 1
fi

# 步驟 2: 上傳 WAR 檔案到伺服器
echo -e "\n${BLUE}步驟 2: 上傳 WAR 檔案到伺服器${NC}"
echo "上傳 $WAR_FILE 到 ${SERVER_USER}@${SERVER_HOST}..."

scp -P "$SERVER_PORT" -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$HOME/.ssh/known_hosts" \
  "$WAR_FILE" "${SERVER_USER}@${SERVER_HOST}:/tmp/${APP_NAME}-new.war"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ WAR 檔案上傳失敗！${NC}"
    exit 1
fi

echo -e "${GREEN}✅ WAR 檔案上傳成功${NC}"

# 步驟 2.5: 檢查遠端伺服器 JDK 版本
echo -e "\n${BLUE}步驟 2.5: 檢查遠端伺服器 JDK 版本${NC}"
REMOTE_JAVA_VERSION=$(ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$HOME/.ssh/known_hosts" \
  "${SERVER_USER}@${SERVER_HOST}" "java -version 2>&1 | grep -oP 'version \"?\K[0-9]+' | head -1" || echo "0")

REQUIRED_JAVA_VERSION="25"
echo "遠端 JDK 版本: $REMOTE_JAVA_VERSION (需要: $REQUIRED_JAVA_VERSION)"

if [ "$REMOTE_JAVA_VERSION" != "$REQUIRED_JAVA_VERSION" ]; then
    echo -e "${YELLOW}⚠️  警告：遠端伺服器 JDK 版本為 $REMOTE_JAVA_VERSION，但 WAR 檔案需要 JDK $REQUIRED_JAVA_VERSION${NC}"
    echo -e "${YELLOW}💡 提示：請在遠端伺服器升級 JDK 版本，否則應用程式可能無法啟動${NC}"
    echo -e "${YELLOW}    升級指令：sudo apt install openjdk-${REQUIRED_JAVA_VERSION}-jdk${NC}"
else
    echo -e "${GREEN}✅ JDK 版本符合要求${NC}"
fi

# 步驟 3: 在伺服器上部署
echo -e "\n${BLUE}步驟 3: 部署到 Tomcat${NC}"
echo "停止 Tomcat，部署新 WAR 檔案，然後重啟..."

ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$HOME/.ssh/known_hosts" \
  "${SERVER_USER}@${SERVER_HOST}" << EOF
    echo "停止 Tomcat 服務..."
    sudo systemctl stop tomcat10

    echo "備份舊的 WAR 檔案（如果存在）..."
    if [ -f "${TOMCAT_WEBAPPS_DIR}/${APP_NAME}.war" ]; then
        sudo mv "${TOMCAT_WEBAPPS_DIR}/${APP_NAME}.war" "${TOMCAT_WEBAPPS_DIR}/${APP_NAME}.war.backup.$(date +%Y%m%d_%H%M%S)"
    fi
    
    echo "清理舊的應用程式目錄..."
    sudo rm -rf "${TOMCAT_WEBAPPS_DIR}/${APP_NAME}/"
    
    echo "部署新的 WAR 檔案..."
    sudo cp "/tmp/${APP_NAME}-new.war" "${TOMCAT_WEBAPPS_DIR}/${APP_NAME}.war"
    sudo chown tomcat:tomcat "${TOMCAT_WEBAPPS_DIR}/${APP_NAME}.war"
    
    echo "啟動 Tomcat 服務..."
    sudo systemctl start tomcat10
    
    echo "清理臨時檔案..."
    rm -f "/tmp/${APP_NAME}-new.war"
    
    echo "等待 Tomcat 啟動..."
    sleep 10
    
    echo "檢查 Tomcat 服務狀態..."
    sudo systemctl status tomcat10 --no-pager -l
EOF

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 伺服器部署失敗！${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 伺服器部署完成${NC}"

# 步驟 4: 健康檢查
echo -e "\n${BLUE}步驟 4: 應用程式健康檢查${NC}"
echo "等待應用程式完全啟動..."
sleep 30

echo "測試 API 端點..."
HEALTH_CHECK_URL="${HEALTH_PROTOCOL}://${SERVER_HOST}${HEALTH_PATH}"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_CHECK_URL" || echo "000")

if [ "$HTTP_STATUS" = "200" ]; then
    echo -e "${GREEN}✅ 應用程式健康檢查通過 (HTTP $HTTP_STATUS)${NC}"
    echo -e "${GREEN}🎉 部署成功完成！${NC}"
else
    echo -e "${YELLOW}⚠️  健康檢查異常 (HTTP $HTTP_STATUS)，請檢查應用程式日誌${NC}"
    echo -e "${YELLOW}💡 提示：應用程式可能還在啟動中，請稍後再次檢查${NC}"
fi

echo -e "\n${BLUE}=== 部署完成 ===${NC}"
echo -e "應用程式 URL: ${GREEN}https://${SERVER_HOST}/${NC}"
echo -e "API 端點: ${GREEN}https://${SERVER_HOST}/prod-api/${NC}"
echo -e "管理後台: ${GREEN}https://${SERVER_HOST}/${NC}"
echo -e "Swagger 文檔: ${GREEN}https://${SERVER_HOST}/prod-api/swagger-ui/index.html${NC}"

# 顯示有用的除錯指令
echo -e "\n${YELLOW}除錯指令：${NC}"
echo "檢查 Tomcat 日誌: ssh -p $SERVER_PORT ${SERVER_USER}@${SERVER_HOST} 'sudo tail -f /usr/libexec/tomcat10/logs/catalina.out'"
echo "檢查應用程式日誌: ssh -p $SERVER_PORT ${SERVER_USER}@${SERVER_HOST} 'sudo tail -f /usr/libexec/tomcat10/logs/localhost.*.log'"
echo "檢查 apps 應用日誌: ssh -p $SERVER_PORT ${SERVER_USER}@${SERVER_HOST} 'sudo find /usr/libexec/tomcat10/logs -name "*apps*" -exec tail -f {} +'"
echo "重啟 Tomcat: ssh -p $SERVER_PORT ${SERVER_USER}@${SERVER_HOST} 'sudo systemctl restart tomcat10'"
