#!/bin/bash

# Git Hooks 安裝腳本
# 此腳本會自動安裝專案的 Git Hooks

set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Git Hooks 安裝程式"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 取得專案根目錄
REPO_ROOT=$(git rev-parse --show-toplevel)
HOOKS_SRC_DIR="$REPO_ROOT/.githooks"
HOOKS_DEST_DIR="$REPO_ROOT/.git/hooks"

# 檢查來源目錄是否存在
if [ ! -d "$HOOKS_SRC_DIR" ]; then
    echo "❌ 找不到 Git Hooks 目錄: $HOOKS_SRC_DIR"
    exit 1
fi

# 建立目標目錄
mkdir -p "$HOOKS_DEST_DIR"

# 計數器
INSTALLED_COUNT=0

# 複製所有 hooks
echo "📦 開始安裝 Git Hooks..."
echo ""

for hook in "$HOOKS_SRC_DIR"/*; do
    if [ -f "$hook" ]; then
        hook_name=$(basename "$hook")
        
        # 跳過 README 或其他非 hook 檔案
        if [[ "$hook_name" == "README"* ]] || [[ "$hook_name" == "."* ]]; then
            continue
        fi
        
        dest_file="$HOOKS_DEST_DIR/$hook_name"
        
        # 檢查是否已存在
        if [ -f "$dest_file" ]; then
            echo "⚠️  $hook_name 已存在"
            read -p "   是否覆蓋? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                echo "   ⏭️  跳過 $hook_name"
                continue
            fi
        fi
        
        # 複製並設定執行權限
        cp "$hook" "$dest_file"
        chmod +x "$dest_file"
        echo "✅ 安裝 $hook_name"
        ((INSTALLED_COUNT++))
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $INSTALLED_COUNT -eq 0 ]; then
    echo "ℹ️  沒有安裝任何 Git Hooks"
else
    echo "✅ 成功安裝 $INSTALLED_COUNT 個 Git Hook(s)"
    echo ""
    echo "已安裝的 Hooks："
    ls -1 "$HOOKS_DEST_DIR" | grep -v "\.sample$" | sed 's/^/  - /'
fi

echo ""
echo "📝 注意事項："
echo "  - pre-commit:  在提交前驗證 Mapper"
echo "  - commit-msg:  驗證 commit message 格式"
echo "  - 如需跳過驗證，可使用: git commit --no-verify"
echo "  - 重新安裝 hooks，再次執行此腳本即可"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
