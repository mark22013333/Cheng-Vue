# Mapper 驗證工具說明

## 📁 工具清單

本目錄包含以下 Mapper 驗證工具：

| 工具 | 說明 | 用途 |
|------|------|------|
| `verify-all-mappers.py` | 全專案驗證 | 掃描所有模組（推薦） |
| `verify-mapper.py` | 單一模組驗證 | 僅驗證 LINE 模組 |
| `install-git-hooks.sh` | Git Hooks 安裝器 | 自動安裝 pre-commit hook |

## 🚀 快速使用

### 1. 驗證全專案（推薦）

```bash
# 在專案根目錄執行
python3 cheng.deploy/verify-all-mappers.py
```

### 2. 安裝 Git Hook（一次性設定）

```bash
# 在專案根目錄執行
./cheng.deploy/install-git-hooks.sh
```

安裝後，每次 `git commit` 都會自動驗證。

## 📊 驗證範圍

### 掃描的模組

- ✅ **cheng-system** - 23 個 Mapper（系統管理 + 庫存管理）
- ✅ **cheng-line** - 4 個 Mapper（LINE 功能）
- ✅ **cheng-quartz** - 2 個 Mapper（定時任務）
- ✅ **cheng-generator** - 2 個 Mapper（程式碼產生器）

### 不包含 Mapper 的模組

- **cheng-admin** - Web 層（Controller）
- **cheng-framework** - 框架配置
- **cheng-common** - 工具類
- **cheng-crawler** - 爬蟲功能

## 🎯 工具特色

### verify-all-mappers.py

- ✅ 自動發現所有包含 Mapper 的模組
- ✅ 遞迴搜尋 XML 檔案（支援子目錄）
- ✅ 識別註解實作（`@Select`、`@Insert` 等）
- ✅ 詳細的統計報表
- ✅ 彩色輸出，清晰易讀

### install-git-hooks.sh

- ✅ 互動式安裝
- ✅ 檢查現有 hooks，詢問是否覆蓋
- ✅ 自動設定執行權限
- ✅ 顯示安裝結果

## 📝 使用範例

### 範例 1：開發新功能

```bash
# 1. 在 Mapper 介面定義方法
vim cheng-line/src/main/java/.../LineUserMapper.java

# 2. 在 XML 中實作 SQL
vim cheng-line/src/main/resources/mapper/LineUserMapper.xml

# 3. 驗證
python3 cheng.deploy/verify-all-mappers.py

# 4. 提交（如果安裝了 Git Hook，會自動驗證）
git add .
git commit -m "feat: 新增使用者查詢方法"
```

### 範例 2：修復驗證錯誤

```bash
# 執行驗證發現問題
python3 cheng.deploy/verify-all-mappers.py

# 輸出顯示：
# ❌ 缺少 3 個 XML 實作：
#    - selectMessageLogByUserId
#    - selectMessageLogByTagId
#    - countFailedMessagesByConfigId

# 在對應的 XML 檔案中補完實作
vim cheng-line/src/main/resources/mapper/LineMessageLogMapper.xml

# 重新驗證
python3 cheng.deploy/verify-all-mappers.py
# ✅ 驗證通過！
```

## 🔧 進階用法

### 跳過 Git Hook 驗證

```bash
# 不建議，但緊急情況可用
git commit --no-verify -m "urgent fix"
```

### 卸載 Git Hook

```bash
rm .git/hooks/pre-commit
```

### 重新安裝 Git Hook

```bash
# 直接再次執行安裝腳本即可
./cheng.deploy/install-git-hooks.sh
```

## 🐛 疑難排解

### Q: 驗證工具找不到模組？

**A**: 確保在專案根目錄執行：
```bash
cd /path/to/Cheng-Vue
python3 cheng.deploy/verify-all-mappers.py
```

### Q: Git Hook 沒有執行？

**A**: 檢查權限：
```bash
ls -la .git/hooks/pre-commit
# 應該要有執行權限 (-rwxr-xr-x)

# 如果沒有，手動設定：
chmod +x .git/hooks/pre-commit
```

### Q: 註解實作被誤報？

**A**: 工具已支援註解實作識別，如果還有問題，請檢查：
- 註解是否在方法定義前
- 註解格式是否正確（`@Select`、`@Insert` 等）

### Q: 為什麼只掃描 4 個模組？

**A**: 這是正常的。專案中只有這 4 個模組包含資料存取層（Mapper）。其他模組屬於不同的分層（Controller、Service、Utils 等），不需要驗證 Mapper。

## 📚 相關文件

- 專案根目錄：`README-MAPPER-VERIFICATION.md` - 團隊使用指南
- Git Hooks：`.githooks/README.md` - Git Hooks 詳細說明
- 詳細文件：`docs/Line/MAPPER_VERIFICATION_GUIDE.md` - 完整使用指南
- 技術總結：`docs/Line/MAPPER_VERIFICATION_COMPLETE.md` - 實作細節

## 🎓 給新成員

歡迎加入團隊！建議你：

1. **閱讀**：`README-MAPPER-VERIFICATION.md`
2. **安裝**：`./cheng.deploy/install-git-hooks.sh`
3. **測試**：`python3 cheng.deploy/verify-all-mappers.py`
4. **開發**：按照團隊規範開發功能

有問題隨時詢問團隊成員！

---

**工具位置**：`/cheng.deploy/`  
**維護者**：開發團隊  
**最後更新**：2025-11-02
