# Windsurf (Cascade AI) 設定指南

> **目的**: 讓團隊成員在本地設定 Windsurf Rules，提升開發效率

---

## 📁 目錄結構

```
Cheng-Vue/
├── .windsurf/                    # ❌ 不進版控（已加入 .gitignore）
│   └── rules/
│       └── cool-apps.md         # 本地規則檔案
│
└── docs/
    └── Development/
        ├── RULE_00_INDEX.md     # ✅ 進版控（團隊共享）
        ├── RULE_01_*.md
        ├── ...
        └── WINDSURF_RULES_TEMPLATE.md  # ✅ 規則範本（進版控）
```

---

## 🎯 為什麼不進版控？

### `.windsurf/rules/` 是個人化配置

| 項目 | `.windsurf/rules/` | `/docs/Development/` |
|------|-------------------|---------------------|
| **作用** | 本地 AI 輔助規則 | 團隊開發規範文件 |
| **讀取者** | Cascade AI（本地） | 團隊成員（人工閱讀） |
| **是否進版控** | ❌ 不需要 | ✅ 必須 |
| **個人化** | ✅ 可自訂 | ❌ 統一標準 |
| **影響範圍** | 只影響您的 AI | 影響整個團隊 |

### 原因說明

1. **本地配置**: Cascade AI 從您本地檔案系統讀取規則
2. **個人偏好**: 每個開發者可能有不同的 AI 輔助需求
3. **避免衝突**: 不同開發者的規則可能不同
4. **隱私保護**: 可能包含個人化的提示詞或敏感資訊

---

## 🚀 快速設定步驟

### 步驟 1: 確認 .gitignore

確認 `.gitignore` 已包含 `.windsurf/` 目錄：

```bash
# 檢查是否已設定
grep ".windsurf" .gitignore
```

✅ 已設定完成（2025-11-01）

### 步驟 2: 建立本地規則檔案

```bash
# 建立目錄
mkdir -p .windsurf/rules

# 建立規則檔案
touch .windsurf/rules/cool-apps.md
```

### 步驟 3: 複製規則內容

開啟檔案：
```bash
# Mac/Linux
open .windsurf/rules/cool-apps.md

# 或使用 IDE
code .windsurf/rules/cool-apps.md
```

填寫內容（參考 `/docs/Development/WINDSURF_RULES_TEMPLATE.md`）：

#### Description 欄位
```
CoolApps 專案開發規範：強制使用 Enum、效能優化、前後端資料格式轉換、異常處理等核心規範
```

#### Glob Pattern 欄位
```
**/*.{java,vue,js,ts,xml,yml,yaml,md}
```

#### Content 欄位
複製 `WINDSURF_RULES_TEMPLATE.md` 中「### 3. Content 欄位」下方的所有內容。

### 步驟 4: 啟用規則

在 Windsurf IDE 中：
1. 開啟任何程式碼檔案
2. 在 Cascade 輸入框中輸入 `@cool-apps` 或使用 `@mention` 功能
3. Cascade 會自動載入並應用規則

---

## 💡 最佳實踐

### 1. 團隊協作方式

#### 進版控的（團隊共享）
```
✅ /docs/Development/RULE_*.md          # 詳細規範文件
✅ /docs/Development/WINDSURF_RULES_TEMPLATE.md  # 規則範本
✅ /docs/Development/WINDSURF_SETUP.md  # 本文件
```

#### 不進版控的（個人配置）
```
❌ .windsurf/rules/*.md                 # 本地 AI 規則
❌ .windsurf/workflows/*.md             # 個人工作流程
```

### 2. 團隊成員加入時

**新成員 Onboarding 流程**:
1. Clone 專案後，閱讀 `/docs/Development/RULE_00_INDEX.md`
2. （可選）依照本文件設定 `.windsurf/rules/cool-apps.md`
3. 使用 Cascade AI 時會自動應用規則

### 3. 規則更新流程

當團隊規範更新時：

```bash
# 1. 更新團隊文件（進版控）
git pull
# 更新 /docs/Development/RULE_*.md

# 2. （可選）更新本地 AI 規則
# 參考最新的 WINDSURF_RULES_TEMPLATE.md 更新 .windsurf/rules/cool-apps.md
```

---

## 🔍 檢查 Git 狀態

確認 `.windsurf/` 不會被追蹤：

```bash
# 查看 Git 狀態
git status

# 應該看不到 .windsurf/ 目錄
# 如果看到，執行：
git rm -r --cached .windsurf/
git commit -m "chore: 移除 .windsurf/ 從版控"
```

---

## ❓ 常見問題

### Q1: 不進版控會影響 AI 功能嗎？

**不會！** Cascade AI 從您本地的檔案系統讀取規則。只要檔案存在於您的本地專案中，AI 就能讀取並應用。

### Q2: 其他團隊成員如何使用相同規則？

**方式 1**: 每個人參考 `WINDSURF_RULES_TEMPLATE.md` 自行設定本地規則

**方式 2**: 直接閱讀 `/docs/Development/` 目錄下的詳細規範文件

**方式 3**: 在團隊內部共享 `.windsurf/rules/cool-apps.md` 內容（例如透過 Slack/Teams）

### Q3: 可以進版控嗎？

**可以，但不建議**。原因：
- 個人化需求不同
- 可能包含個人化的提示詞
- 會造成不必要的 merge conflict

**建議**: 只進版控範本檔案 `WINDSURF_RULES_TEMPLATE.md`

### Q4: 如何同步多台電腦的設定？

**方式 1**: 使用個人的 dotfiles 儲存庫（不要放在專案 repo）

**方式 2**: 手動複製 `.windsurf/rules/` 目錄

**方式 3**: 使用雲端同步（Dropbox/Google Drive）建立符號連結

### Q5: 規則檔案太大怎麼辦？

Content 欄位限制 12000 字元。解決方案：

**方式 1**: 拆分成多個規則檔案
```
.windsurf/rules/
├── enum-rules.md        # Enum 專用規則
├── frontend-rules.md    # 前端專用規則
└── backend-rules.md     # 後端專用規則
```

**方式 2**: 使用精簡版（當前範本已優化）

**方式 3**: 直接查閱 `/docs/Development/` 詳細文件

---

## 📚 相關資源

- **規則範本**: [WINDSURF_RULES_TEMPLATE.md](./WINDSURF_RULES_TEMPLATE.md)
- **開發規範索引**: [RULE_00_INDEX.md](./RULE_00_INDEX.md)
- **Windsurf 官方文檔**: [Windsurf Documentation](https://docs.codeium.com/windsurf)

---

## 🎉 設定完成

設定完成後，您可以：
1. ✅ 在 Cascade 中使用 `@cool-apps` 引用規則
2. ✅ AI 會自動根據規範提供建議
3. ✅ 不用擔心影響 Git 版控
4. ✅ 隨時根據個人需求調整規則

---

**更新時間**: 2025-11-01
**維護者**: CoolApps 開發團隊
