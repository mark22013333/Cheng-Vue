# Mapper 驗證工具使用指南

## 🚀 快速開始

### 新成員加入專案

```bash
# 1. Clone 專案
git clone <repository-url>
cd Cheng-Vue

# 2. 啟動服務（會自動安裝 Git Hooks）⭐
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 或使用 IDE 啟動服務，Git Hooks 會自動安裝

# 3. 開始開發
# Git Hooks 會在每次 commit 前自動驗證
```

**優勢**：
- ✅ 自動化 - 啟動服務時自動檢查並安裝
- ✅ 零學習成本 - 新成員無需記憶安裝指令
- ✅ 保持最新 - 每次啟動都會更新到最新版本

## 📦 驗證工具位置

所有驗證工具都在 `cheng.deploy/` 目錄下：

```
cheng.deploy/
├── verify-all-mappers.py      # 全專案 Mapper 驗證（主要工具）
├── verify-mapper.py            # 單一模組驗證
├── install-git-hooks.sh        # Git Hooks 安裝腳本
└── ... 其他部署腳本
```

## 🔍 使用方式

### 手動驗證

```bash
# 驗證全專案（推薦）
python3 cheng.deploy/verify-all-mappers.py

# 驗證單一模組
cd cheng-line
python3 ../cheng.deploy/verify-mapper.py
```

### 自動驗證（Git Hooks）

**方法 1：自動安裝（推薦）⭐**

啟動後端服務時自動安裝：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
# 或使用 IDE 啟動
```

**方法 2：手動安裝**

```bash
./cheng.deploy/install-git-hooks.sh
```

安裝後，每次 `git commit` 都會自動驗證。

**跳過驗證**（不建議）：
```bash
git commit --no-verify -m "your message"
```

### CI/CD 自動驗證

已整合到 GitHub Actions，每次 Push/PR 自動執行。

## 📊 驗證結果說明

### ✅ 驗證通過

```
======================================================================
驗證總結
======================================================================

模組統計：

模組名稱                  Mapper數    方法數    缺失數    狀態
----------------------------------------------------------------------
cheng-system              23         190       -         ✅ 通過
cheng-line                4          49        -         ✅ 通過
cheng-quartz              2          14        -         ✅ 通過
cheng-generator           2          16        -         ✅ 通過
----------------------------------------------------------------------
總計                      31         269       0         ✅ 全部通過

✅ 驗證通過！所有 4 個模組的 31 個 Mapper 都有完整的 XML 實作
```

### ❌ 驗證失敗

```
檢查 LineMessageLogMapper...
  Java: .../LineMessageLogMapper.java
  ❌ 缺少 3 個 XML 實作：
     - selectMessageLogByUserId
     - selectMessageLogByTagId
     - countFailedMessagesByConfigId
```

**處理方式**：
1. 在對應的 XML 檔案中補完缺失的實作
2. 重新執行驗證確認通過
3. 提交程式碼

## 🎯 最佳實踐

### 開發流程

```
1. 在 Mapper 介面定義方法
   ↓
2. 在 XML 中實作 SQL（或使用 @Select 等註解）
   ↓
3. 執行驗證：python3 cheng.deploy/verify-all-mappers.py
   ↓
4. 驗證通過後提交程式碼
   ↓
5. Git Hook 再次驗證（雙重保險）
   ↓
6. CI/CD 最終驗證（三重保險）
```

### 團隊協作

- ✅ **建議**：每位成員安裝 Git Hooks
- ✅ **Code Review**：檢查驗證結果
- ✅ **提交前**：確保本地驗證通過
- ✅ **合併前**：確保 CI/CD 驗證通過

## 💡 常見問題

### Q1: Git Hook 會影響提交速度嗎？

**A**: 不會。驗證速度非常快（< 1 秒），幾乎感覺不到延遲。

### Q2: 我不想用 Git Hook 可以嗎？

**A**: 完全可以。Git Hook 是可選的，不安裝也不影響開發。但建議安裝以確保程式碼品質。

### Q3: 如何卸載 Git Hook？

**A**: 刪除檔案即可：
```bash
rm .git/hooks/pre-commit
```

### Q4: Git Hook 檔案會被提交嗎？

**A**: 不會。`.git/hooks/` 目錄的內容不會被 Git 追蹤。每位開發者需要自行安裝。

### Q5: 驗證工具支援註解實作嗎？

**A**: 是的！工具能正確識別 `@Select`、`@Insert`、`@Update`、`@Delete` 等註解實作的方法，不會誤報。

### Q6: 為什麼只顯示 4 個模組？

**A**: 因為專案中只有 4 個模組包含 Mapper：
- cheng-system（系統 + 庫存管理）
- cheng-line（LINE 功能）
- cheng-quartz（定時任務）
- cheng-generator（程式碼產生器）

其他模組（admin、framework、common、crawler）不包含資料存取層，所以不需要驗證。

## 🔗 相關文件

- 詳細使用指南：`docs/Line/MAPPER_VERIFICATION_GUIDE.md`
- 完成總結：`docs/Line/MAPPER_VERIFICATION_COMPLETE.md`
- Git Hooks 說明：`.githooks/README.md`

## 📞 需要協助？

如果遇到問題：
1. 查看詳細文件
2. 檢查錯誤訊息
3. 聯絡專案維護者

---

**最後更新**：2025-11-02  
**維護者**：開發團隊
