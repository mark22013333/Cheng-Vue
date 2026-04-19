#!/bin/bash
# 測試任務類型 API
# 使用方式：./test-task-types.sh [token]

BASE_URL="http://localhost:8080"
TOKEN="${1}"

echo "=========================================="
echo "測試任務類型動態配置 API"
echo "=========================================="
echo ""

if [ -z "$TOKEN" ]; then
    echo "⚠️  警告：未提供 Token，可能需要登入後取得"
    echo "使用方式：./test-task-types.sh 'YOUR_TOKEN'"
    echo ""
fi

# 測試 1：取得所有任務類型
echo "📋 測試 1：取得所有任務類型"
echo "GET ${BASE_URL}/monitor/job/taskTypes"
echo "---"

if [ -n "$TOKEN" ]; then
    curl -s -X GET "${BASE_URL}/monitor/job/taskTypes" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" | jq '.'
else
    curl -s -X GET "${BASE_URL}/monitor/job/taskTypes" \
        -H "Content-Type: application/json" | jq '.'
fi

echo ""
echo "=========================================="
echo ""

# 測試 2：取得爬蟲類型
echo "🕷️  測試 2：取得爬蟲類型"
echo "GET ${BASE_URL}/monitor/job/taskTypes/crawler"
echo "---"

if [ -n "$TOKEN" ]; then
    curl -s -X GET "${BASE_URL}/monitor/job/taskTypes/crawler" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" | jq '.'
else
    curl -s -X GET "${BASE_URL}/monitor/job/taskTypes/crawler" \
        -H "Content-Type: application/json" | jq '.'
fi

echo ""
echo "=========================================="
echo ""

# 測試 3：取得推播類型
echo "📢 測試 3：取得推播類型"
echo "GET ${BASE_URL}/monitor/job/taskTypes/notification"
echo "---"

if [ -n "$TOKEN" ]; then
    curl -s -X GET "${BASE_URL}/monitor/job/taskTypes/notification" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" | jq '.'
else
    curl -s -X GET "${BASE_URL}/monitor/job/taskTypes/notification" \
        -H "Content-Type: application/json" | jq '.'
fi

echo ""
echo "=========================================="
echo "測試完成！"
echo "=========================================="
