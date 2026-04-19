#!/bin/bash

################################################################################
# FlareSolverr 測試腳本
# 用途：測試 FlareSolverr 服務是否正常運作
# 作者：cheng
# 日期：2025-01-04
################################################################################

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# FlareSolverr 服務位址
FLARESOLVERR_URL="http://localhost:8191/v1"

# 測試 ISBN
TEST_ISBN="9789863877363"
TEST_URL="https://isbn.tw/${TEST_ISBN}"

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}   FlareSolverr 測試腳本${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

################################################################################
# 1. 檢查 Docker 是否執行
################################################################################
echo -e "${YELLOW}[步驟 1/6] 檢查 Docker 服務...${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker 未安裝${NC}"
    exit 1
fi

if ! docker ps &> /dev/null; then
    echo -e "${RED}✗ Docker 服務未執行${NC}"
    exit 1
fi

echo -e "${GREEN}Docker 服務正常${NC}"
echo ""

################################################################################
# 2. 檢查 FlareSolverr 容器
################################################################################
echo -e "${YELLOW}[步驟 2/6] 檢查 FlareSolverr 容器...${NC}"

if ! docker ps | grep -q flaresolverr; then
    echo -e "${RED}✗ FlareSolverr 容器未執行${NC}"
    echo -e "${YELLOW}提示：請執行 'docker-compose up -d' 啟動服務${NC}"
    exit 1
fi

echo -e "${GREEN}FlareSolverr 容器正在執行${NC}"

# 顯示容器資訊
CONTAINER_INFO=$(docker ps --filter "name=flaresolverr" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}")
echo ""
echo "$CONTAINER_INFO"
echo ""

################################################################################
# 3. 測試服務連接
################################################################################
echo -e "${YELLOW}[步驟 3/6] 測試 FlareSolverr 服務連接...${NC}"

HEALTH_RESPONSE=$(curl -s -X POST "${FLARESOLVERR_URL}" \
  -H "Content-Type: application/json" \
  -d '{"cmd":"sessions.list"}' \
  --max-time 10 2>&1)

if [[ $? -ne 0 ]]; then
    echo -e "${RED}✗ 無法連接到 FlareSolverr 服務${NC}"
    echo "錯誤訊息: $HEALTH_RESPONSE"
    exit 1
fi

if [[ $HEALTH_RESPONSE == *"ok"* ]]; then
    echo -e "${GREEN}FlareSolverr 服務正常回應${NC}"
    echo "回應: $(echo $HEALTH_RESPONSE | jq -r '.message' 2>/dev/null || echo $HEALTH_RESPONSE)"
else
    echo -e "${RED}✗ FlareSolverr 服務回應異常${NC}"
    echo "回應: $HEALTH_RESPONSE"
    exit 1
fi

echo ""

################################################################################
# 4. 測試 Session 建立
################################################################################
echo -e "${YELLOW}[步驟 4/6] 測試 Session 管理...${NC}"

SESSION_ID="test-session-$(date +%s)"
echo "建立 Session ID: $SESSION_ID"

CREATE_SESSION_RESPONSE=$(curl -s -X POST "${FLARESOLVERR_URL}" \
  -H "Content-Type: application/json" \
  -d "{\"cmd\":\"sessions.create\",\"session\":\"${SESSION_ID}\"}" \
  --max-time 10)

if [[ $CREATE_SESSION_RESPONSE == *"ok"* ]]; then
    echo -e "${GREEN}Session 建立成功${NC}"
else
    echo -e "${RED}✗ Session 建立失敗${NC}"
    echo "回應: $CREATE_SESSION_RESPONSE"
fi

echo ""

################################################################################
# 5. 測試頁面取得（關鍵測試）
################################################################################
echo -e "${YELLOW}[步驟 5/6] 測試 Cloudflare 驗證處理...${NC}"
echo "目標 URL: $TEST_URL"
echo "預計耗時: 5-15 秒（請耐心等待）"
echo ""

START_TIME=$(date +%s)

PAGE_RESPONSE=$(curl -s -X POST "${FLARESOLVERR_URL}" \
  -H "Content-Type: application/json" \
  -d "{
    \"cmd\": \"request.get\",
    \"url\": \"${TEST_URL}\",
    \"maxTimeout\": 60000
  }" \
  --max-time 70)

END_TIME=$(date +%s)
ELAPSED_TIME=$((END_TIME - START_TIME))

echo "實際耗時: ${ELAPSED_TIME} 秒"
echo ""

# 解析回應
if [[ $PAGE_RESPONSE == *"ok"* ]]; then
    echo -e "${GREEN}頁面取得成功！${NC}"
    
    # 提取 HTML 長度
    HTML_LENGTH=$(echo "$PAGE_RESPONSE" | jq -r '.solution.response' 2>/dev/null | wc -c)
    STATUS_CODE=$(echo "$PAGE_RESPONSE" | jq -r '.solution.status' 2>/dev/null)
    
    echo "HTTP 狀態碼: $STATUS_CODE"
    echo "HTML 長度: $HTML_LENGTH 字元"
    
    # 檢查是否包含書籍資訊
    if echo "$PAGE_RESPONSE" | grep -q "h3.mb-2.font-weight-bold"; then
        echo -e "${GREEN}頁面包含書籍資訊結構${NC}"
        
        # 嘗試提取書名
        BOOK_TITLE=$(echo "$PAGE_RESPONSE" | jq -r '.solution.response' | grep -oP '(?<=<h1 class="h3 mb-2 font-weight-bold">)[^<]+' | head -1)
        if [[ -n "$BOOK_TITLE" ]]; then
            echo -e "${GREEN}成功解析書名: ${BOOK_TITLE}${NC}"
        fi
    else
        echo -e "${YELLOW}⚠ 頁面結構異常，可能仍在驗證頁面${NC}"
    fi
    
    # 儲存完整回應到檔案（供除錯）
    echo "$PAGE_RESPONSE" > /tmp/flaresolverr-test-response.json
    echo ""
    echo "完整回應已儲存到: /tmp/flaresolverr-test-response.json"
    
else
    echo -e "${RED}✗ 頁面取得失敗${NC}"
    echo "錯誤訊息: $(echo $PAGE_RESPONSE | jq -r '.message' 2>/dev/null || echo $PAGE_RESPONSE)"
    
    # 儲存錯誤回應
    echo "$PAGE_RESPONSE" > /tmp/flaresolverr-test-error.json
    echo "錯誤詳情已儲存到: /tmp/flaresolverr-test-error.json"
fi

echo ""

################################################################################
# 6. 清理測試 Session
################################################################################
echo -e "${YELLOW}[步驟 6/6] 清理測試 Session...${NC}"

DESTROY_SESSION_RESPONSE=$(curl -s -X POST "${FLARESOLVERR_URL}" \
  -H "Content-Type: application/json" \
  -d "{\"cmd\":\"sessions.destroy\",\"session\":\"${SESSION_ID}\"}" \
  --max-time 10)

if [[ $DESTROY_SESSION_RESPONSE == *"ok"* ]]; then
    echo -e "${GREEN}Session 清理成功${NC}"
else
    echo -e "${YELLOW}⚠ Session 清理失敗（不影響測試結果）${NC}"
fi

echo ""

################################################################################
# 總結
################################################################################
echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}   測試完成！${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

if [[ $PAGE_RESPONSE == *"ok"* ]]; then
    echo -e "${GREEN}所有測試通過！FlareSolverr 運作正常${NC}"
    echo ""
    echo -e "${GREEN}您現在可以：${NC}"
    echo "1. 啟動 Spring Boot 應用"
    echo "2. 呼叫 ISBN 爬蟲 API 測試實際功能"
    echo "3. 查看應用日誌確認 FlareSolverr 整合是否正常"
    echo ""
    echo "測試 API:"
    echo "curl http://localhost:8080/isbn/9789863877363"
    exit 0
else
    echo -e "${RED}✗ 測試失敗，請檢查以下項目：${NC}"
    echo "1. FlareSolverr 容器是否正常執行"
    echo "2. 網路連接是否正常"
    echo "3. 目標網站是否可訪問"
    echo "4. 檢查 FlareSolverr 容器日誌："
    echo "   docker logs flaresolverr"
    exit 1
fi
