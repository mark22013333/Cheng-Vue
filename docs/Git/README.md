# Git Hooks

這個目錄包含專案的 Git Hooks。

## 快速安裝

### 自動安裝（推薦）⭐

**啟動後端服務時自動安裝**：

```bash
# 啟動服務，會自動檢查並安裝 Git Hooks
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 或使用 IDE 啟動服務
```

服務啟動時會顯示：
```
════════════════════════════════════════════════════
  Git Hooks 自動安裝
════════════════════════════════════════════════════
✅ 成功安裝 2 個 Git Hook(s)
已安裝的 Hooks：
  - pre-commit
  - commit-msg
════════════════════════════════════════════════════
```

### 手動安裝

在專案根目錄執行：

```bash
./cheng.deploy/install-git-hooks.sh
```

## 已提供的 Hooks

### pre-commit

在 `git commit` 之前自動執行 Mapper 驗證。

**功能**：
- ✅ 驗證全專案所有 Mapper 的 XML 實作
- ✅ 發現問題會阻止提交
- ✅ 可以使用 `--no-verify` 跳過

### commit-msg

在提交訊息確認後驗證格式。

**功能**：
- ✅ 檢查 commit message 必須有有效的 prefix（feat、fix、docs 等）
- ✅ 檢查標題長度（建議 < 72 字元）
- ✅ 自動跳過 merge 和 revert commit

**允許的 prefix**：
- `feat:` - 新增功能
- `fix:` - 修正 Bug
- `docs:` - 文件變更
- `style:` - 程式碼格式調整
- `refactor:` - 重構程式碼
- `perf:` - 效能優化
- `test:` - 測試相關
- `chore:` - 雜項
- `build:` - 建置系統變更
- `ci:` - CI/CD 配置變更

**跳過驗證**：
```bash
git commit --no-verify -m "your message"
```

## 手動安裝

如果不想使用安裝腳本，也可以手動複製：

```bash
cp .githooks/pre-commit .git/hooks/pre-commit
cp .githooks/commit-msg .git/hooks/commit-msg
chmod +x .git/hooks/pre-commit
chmod +x .git/hooks/commit-msg
```

## 移除

刪除對應的 hook 檔案即可：

```bash
# 移除 Mapper 驗證
rm .git/hooks/pre-commit

# 移除 Commit 格式驗證
rm .git/hooks/commit-msg

# 移除全部
rm .git/hooks/pre-commit .git/hooks/commit-msg
```

## 團隊協作

**首次使用**：
1. Clone 專案後執行：`./cheng.deploy/install-git-hooks.sh`
2. Hooks 會自動在每次提交前驗證

**更新 Hooks**：
- 當 `.githooks/` 目錄更新時，重新執行安裝腳本即可

**不想使用 Hooks**：
- 完全是可選的，不安裝也不影響開發
- 但建議安裝以確保程式碼品質

## 注意事項

- Git Hooks 是本地的，不會被提交到 repository
- 每個開發者需要自行安裝
- 安裝腳本會詢問是否覆蓋已存在的 hooks
