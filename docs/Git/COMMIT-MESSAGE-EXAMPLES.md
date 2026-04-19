# Commit Message 格式範例

## 📝 基本格式

```
<prefix>: <標題>

<詳細說明>（可選）

<檔案變更說明>（可選）
```

## ✅ 正確範例

### 範例 1：簡單格式

```
feat: 新增使用者登入功能
```

### 範例 2：帶詳細說明

```
fix: 修正阿甘頭像可能被覆蓋的問題

LINEBotApi.java - 修正若為阿甘傳送的頭像，不能套用後台設定的頭像。新增判斷 sender 參數是否存在，決定是否使用預設值。
AuthController.java - 補上推播功能所需的 sender 參數，避免後續流程出錯。
ChatHandler.java - 移除冗餘方法，因為 push 用到的共用方法已整合已讀 API 功能，無需重複。
```

### 範例 3：功能開發

```
feat: 實作庫存管理系統

新增以下功能：
- 物品分類管理
- 庫存查詢與統計
- 借出歸還流程
- QR Code 掃描功能

涉及模組：
- cheng-system: 新增 8 個 Mapper 和對應的 XML
- cheng-ui: 新增 5 個前端頁面
```

### 範例 4：Bug 修復

```
fix: 修正 LineMessageLog 缺失的 XML 實作

補完以下方法的 XML 實作：
- selectMessageLogByUserId
- selectMessageLogByTagId  
- countFailedMessagesByConfigId

解決執行時出現 BindingException 的問題。
```

### 範例 5：文件更新

```
docs: 新增 Mapper 驗證工具使用指南

建立以下文件：
- MAPPER_VERIFICATION_GUIDE.md - 完整使用指南
- MAPPER_VERIFICATION_COMPLETE.md - 技術實作總結
- README-MAPPER-VERIFICATION.md - 快速開始指南
```

### 範例 6：重構

```
refactor: 整合部署腳本到 cheng.deploy 目錄

統一管理所有部署和驗證工具：
- 移動 verify-*.py 到 cheng.deploy/
- 更新 CI/CD 配置路徑
- 建立 Git Hooks 安裝器
```

### 範例 7：效能優化

```
perf: 優化 Mapper 驗證速度

改進：
- 使用遞迴搜尋取代多次檔案系統掃描
- 快取正則表達式編譯結果
- 平行處理多個模組

效能提升：31 個 Mapper 從 3 秒降至 < 1 秒
```

## ❌ 錯誤範例

### 錯誤 1：缺少 prefix

```
❌ 新增使用者登入功能
✅ feat: 新增使用者登入功能
```

### 錯誤 2：使用錯誤的 prefix

```
❌ add: 新增使用者登入功能
✅ feat: 新增使用者登入功能
```

### 錯誤 3：prefix 後面缺少空格

```
❌ feat:新增使用者登入功能
✅ feat: 新增使用者登入功能
```

### 錯誤 4：使用簡體中文

```
❌ feat: 创建用户登录功能
✅ feat: 建立使用者登入功能
```

```
❌ fix: 修复数据庫連接问题
✅ fix: 修正資料庫連線問題
```

## 🏷️ Prefix 對照表

| Prefix | 用途 | 範例 |
|--------|------|------|
| `feat` | 新增功能 | feat: 新增使用者權限管理 |
| `fix` | 修正 Bug | fix: 修正登入驗證錯誤 |
| `docs` | 文件變更 | docs: 更新 API 使用說明 |
| `style` | 程式碼格式 | style: 統一程式碼縮排為 4 空格 |
| `refactor` | 重構程式碼 | refactor: 簡化 Mapper 驗證邏輯 |
| `perf` | 效能優化 | perf: 優化資料庫查詢效能 |
| `test` | 測試相關 | test: 新增 Mapper 單元測試 |
| `chore` | 雜項 | chore: 更新依賴套件版本 |
| `build` | 建置系統 | build: 升級 Maven 版本到 3.9.0 |
| `ci` | CI/CD | ci: 新增 Mapper 驗證到 GitHub Actions |

## 🌍 繁體中文用語對照

| 簡體 | 繁體（建議使用） |
|------|------------------|
| 创建 | 建立 |
| 取得 | 取得 |
| 删除 | 移除 / 刪除 |
| 线程 | 執行緒 |
| API | 介面 |
| 数据庫 | 資料庫 |
| 刷新 | 重新整理 |
| 加载 | 載入 |
| 遞归 | 遞迴 |
| 新增 | 新增 |

## 💡 最佳實踐

### 1. 標題簡潔明確

- ✅ `feat: 新增使用者登入功能`
- ❌ `feat: 這個 commit 是要新增一個很棒的使用者登入功能，可以讓使用者輸入帳號密碼登入系統`

### 2. 詳細說明放第二段

```
feat: 新增使用者登入功能

實作功能：
- 帳號密碼驗證
- JWT Token 產生
- 登入狀態記錄
- 失敗次數限制
```

### 3. 逐檔說明變更

```
fix: 修正 Mapper XML 缺失問題

LineMessageLogMapper.xml - 補完 3 個缺失的查詢方法
LineUserMapper.xml - 修正參數類型錯誤
```

### 4. 提及影響範圍

```
refactor: 重構認證模組

影響範圍：
- 後端：cheng-framework/security
- 前端：cheng-ui/views/login
- 資料庫：sys_user 表結構不變
```

## 🚫 跳過驗證（不建議）

如果確實需要跳過格式驗證：

```bash
git commit --no-verify -m "urgent fix"
```

**注意**：只在緊急情況使用，正常開發請遵守規範。

---

**參考**：專案全域規則 `.windsurf/rules/user_global`  
**工具**：`.githooks/commit-msg`  
**安裝**：`./cheng.deploy/install-git-hooks.sh`
