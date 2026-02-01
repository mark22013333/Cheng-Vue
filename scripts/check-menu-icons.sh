#!/usr/bin/env bash
# ==================================================
# check-menu-icons.sh
# 檢查 SQL 遷移檔中的選單 icon 值是否都有對應的 SVG 檔案
#
# 用法:
#   ./scripts/check-menu-icons.sh                    # 檢查所有遷移檔
#   ./scripts/check-menu-icons.sh V39__xxx.sql       # 檢查指定檔案
# ==================================================

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SVG_DIR="$PROJECT_ROOT/cheng-ui/src/assets/icons/svg"
MIGRATION_DIR="$PROJECT_ROOT/cheng-admin/src/main/resources/db/migration"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 收集所有已有的 SVG 檔名 (不含副檔名)，每行一個
EXISTING_ICONS=$(ls "$SVG_DIR"/*.svg 2>/dev/null | xargs -I{} basename {} .svg | sort)
ICON_COUNT=$(echo "$EXISTING_ICONS" | wc -l | tr -d ' ')

# 決定掃描哪些檔案
if [ $# -gt 0 ]; then
    TARGET_FILES=()
    for arg in "$@"; do
        if [ -f "$MIGRATION_DIR/$arg" ]; then
            TARGET_FILES+=("$MIGRATION_DIR/$arg")
        elif [ -f "$arg" ]; then
            TARGET_FILES+=("$arg")
        else
            echo -e "${RED}[錯誤] 找不到檔案: $arg${NC}"
            exit 1
        fi
    done
else
    TARGET_FILES=("$MIGRATION_DIR"/V*.sql)
fi

echo "====================================="
echo " 選單 Icon 檢查工具"
echo "====================================="
echo ""
echo "SVG 目錄: $SVG_DIR"
echo "掃描檔案: ${#TARGET_FILES[@]} 個"
echo "已有 SVG 圖示: $ICON_COUNT 個"
echo ""

icon_exists() {
    echo "$EXISTING_ICONS" | grep -qx "$1"
}

MISSING_COUNT=0
FOUND_COUNT=0
MISSING_LIST=""

for sql_file in "${TARGET_FILES[@]}"; do
    filename=$(basename "$sql_file")

    # 從 sys_menu INSERT 語句中提取 icon 欄位值
    # 策略: 找含 sys_menu 的行，提取所有單引號內的值，篩選出看起來像 icon 名的
    icons=$(grep -i "sys_menu" "$sql_file" 2>/dev/null | \
        grep -oE "'[a-z][a-z0-9-]*'" | \
        tr -d "'" | \
        sort -u 2>/dev/null || true)

    if [ -z "$icons" ]; then
        continue
    fi

    file_has_output=false

    while IFS= read -r icon; do
        [ -z "$icon" ] && continue

        # 跳過 '#' 等特殊值
        [ "$icon" = "#" ] && continue

        # 跳過 perms 格式 (shop:gift:list 等)
        echo "$icon" | grep -qE '^[a-z]+:[a-z]+:[a-z]+$' && continue

        # 跳過路徑型字串 (shop/gift/index 等)
        echo "$icon" | grep -qE '/' && continue

        # 跳過過長的值 (可能是 component 路徑)
        [ ${#icon} -gt 30 ] && continue

        # 跳過資料庫常見值 (status, admin, etc.)
        case "$icon" in
            admin|status|enabled|disabled|draft|published|null|now) continue ;;
        esac

        # 檢查是否為已知 icon
        if icon_exists "$icon"; then
            FOUND_COUNT=$((FOUND_COUNT + 1))
        else
            # 可能不是 icon 欄位的值，做額外過濾:
            # 只報告長度 <= 20 且含有 '-' 或全英文短字的 (典型 icon name)
            # 或者是已知 icon 命名模式
            if echo "$icon" | grep -qE '^(mall-|[a-z]+-[a-z]+|[a-z]{2,15})$'; then
                if [ "$file_has_output" = false ]; then
                    echo -e "${YELLOW}📄 $filename${NC}"
                    file_has_output=true
                fi
                echo -e "  ${RED}✗ 缺少圖示: $icon${NC}  → 請建立 $SVG_DIR/$icon.svg"
                MISSING_COUNT=$((MISSING_COUNT + 1))
                MISSING_LIST="$MISSING_LIST $icon"
            fi
        fi
    done <<< "$icons"
done

echo ""
echo "====================================="
echo " 檢查結果"
echo "====================================="

if [ $MISSING_COUNT -eq 0 ]; then
    echo -e "${GREEN}✓ 所有選單 icon 都有對應的 SVG 檔案${NC}"
    exit 0
else
    echo -e "${RED}✗ 發現 $MISSING_COUNT 個缺失的 icon:${MISSING_LIST}${NC}"
    echo ""
    echo "提示: 可用的 icon 列表請參考 $SVG_DIR/"
    exit 1
fi
