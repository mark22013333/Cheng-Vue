# Git Hooks 自動安裝機制

## 📋 功能說明

在後端服務啟動時，自動檢查並安裝專案的 Git Hooks，確保團隊成員都能使用最新的驗證工具。

## 🎯 設計目標

### 問題

傳統方式需要新成員手動執行安裝腳本：
- ❌ 容易忘記安裝
- ❌ 需要記憶指令
- ❌ Hook 更新後需要手動重裝

### 解決方案

**啟動服務時自動安裝**：
- ✅ 零學習成本 - 不需要記憶任何指令
- ✅ 自動更新 - 每次啟動都檢查最新版本
- ✅ 開發友好 - 不影響開發流程

## 🔧 技術實作

### 核心類別

**`GitHooksAutoInstaller.java`**

```java
@Component
@Order(1) // 優先執行
public class GitHooksAutoInstaller implements ApplicationRunner {
    
    @Override
    public void run(ApplicationArguments args) {
        // 1. 尋找專案根目錄
        // 2. 檢查 .githooks 目錄
        // 3. 複製 hooks 到 .git/hooks/
        // 4. 設定執行權限
        // 5. 顯示安裝結果
    }
}
```

### 執行時機

**Spring Boot 啟動完成後**：
- 使用 `ApplicationRunner` 介面
- `@Order(1)` 確保優先執行
- 在應用程式完全啟動前完成安裝

### 安裝邏輯

1. **尋找專案根目錄**
   - 從當前目錄往上找 `.git` 目錄
   - 支援從任何模組啟動

2. **檢查是否需要更新**
   - 比較檔案修改時間
   - 只更新有變動的 hooks

3. **複製檔案**
   - 從 `.githooks/` 複製到 `.git/hooks/`
   - 自動覆蓋舊版本

4. **設定權限**
   - 自動設定為可執行
   - 支援 Unix/Linux/Mac

## 📊 使用方式

### 啟動服務

```bash
# 方式 1：使用 Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 方式 2：使用 IDE
# 直接點擊 Run 按鈕
```

### 安裝訊息

服務啟動時會看到：

```
════════════════════════════════════════════════════
  Git Hooks 自動安裝
════════════════════════════════════════════════════
✅ 成功安裝 2 個 Git Hook(s)
已安裝的 Hooks：
  - pre-commit
  - commit-msg

📝 提示：
  - pre-commit:  在提交前驗證 Mapper
  - commit-msg:  驗證 commit message 格式
  - 如需跳過驗證: git commit --no-verify
════════════════════════════════════════════════════
```

### 已是最新狀態

如果 hooks 已是最新：

```
════════════════════════════════════════════════════
  Git Hooks 自動安裝
════════════════════════════════════════════════════
ℹ️  所有 Git Hooks 已是最新狀態
════════════════════════════════════════════════════
```

## 🎨 特色功能

### 1. 智能更新

- 檢查檔案修改時間
- 只更新有變動的 hooks
- 避免不必要的複製

### 2. 錯誤處理

- 找不到 `.githooks` - 跳過安裝
- 找不到 `.git` - 顯示警告
- 安裝失敗 - 不影響服務啟動

### 3. 日誌輸出

- 使用彩色日誌（開發環境）
- 清晰的安裝狀態顯示
- DEBUG 級別的詳細資訊

## 💡 使用場景

### 場景 1：新成員加入

```bash
# 1. Clone 專案
git clone <repo-url>

# 2. 啟動服務（IDE 或 Maven）
# Git Hooks 自動安裝 ✅

# 3. 開始開發
git commit -m "feat: 新增功能"
# Hooks 自動驗證 ✅
```

### 場景 2：Hook 更新

```bash
# 團隊更新了 Git Hooks

# 開發者只需：
# 1. git pull（取得最新的 .githooks/）
# 2. 重啟服務
# Hooks 自動更新 ✅
```

### 場景 3：多個開發分支

```bash
# 在不同分支開發

git checkout feature-a
# 啟動服務，安裝 feature-a 的 hooks

git checkout feature-b  
# 啟動服務，安裝 feature-b 的 hooks

# 每個分支的 hooks 都能保持最新 ✅
```

## 🔍 技術細節

### 檔案比較邏輯

```java
private boolean needsUpdate(Path source, Path dest) throws IOException {
    if (!Files.exists(dest)) {
        return true; // 檔案不存在，需要安裝
    }

    // 比較修改時間
    long sourceTime = Files.getLastModifiedTime(source).toMillis();
    long destTime = Files.getLastModifiedTime(dest).toMillis();

    return sourceTime > destTime; // 來源較新，需要更新
}
```

### 專案根目錄搜尋

```java
private Path findProjectRoot() {
    Path current = Paths.get("").toAbsolutePath();
    
    while (current != null) {
        Path gitDir = current.resolve(".git");
        if (Files.exists(gitDir) && Files.isDirectory(gitDir)) {
            return current; // 找到 .git 目錄
        }
        current = current.getParent(); // 往上一層找
    }
    
    return null;
}
```

### 執行權限設定

```java
private void setExecutable(Path file) {
    try {
        File f = file.toFile();
        f.setExecutable(true, false); // 設定為可執行
    } catch (Exception e) {
        log.debug("無法設定執行權限: {}", e.getMessage());
    }
}
```

## 🚀 效益分析

### 團隊協作

| 項目 | 手動安裝 | 自動安裝 |
|------|---------|---------|
| 新成員學習成本 | 中等 | 零 |
| 忘記安裝風險 | 高 | 無 |
| Hook 更新同步 | 需通知 | 自動 |
| 維護成本 | 中等 | 低 |

### 開發體驗

- ✅ **透明化** - 開發者無感知，自動完成
- ✅ **零配置** - 不需要額外設定
- ✅ **持續更新** - 始終使用最新版本
- ✅ **錯誤友好** - 安裝失敗不影響開發

## ⚙️ 配置選項

### 關閉自動安裝

如果不想使用自動安裝，可以：

**方式 1：環境變數**
```bash
export SKIP_GIT_HOOKS_INSTALL=true
mvn spring-boot:run
```

**方式 2：修改類別**
```java
// 在 GitHooksAutoInstaller 加入條件判斷
@ConditionalOnProperty(
    name = "git.hooks.auto-install", 
    havingValue = "true", 
    matchIfMissing = true
)
```

### 調整執行順序

```java
@Order(1)  // 數字越小越早執行
public class GitHooksAutoInstaller implements ApplicationRunner {
    // ...
}
```

## 🐛 疑難排解

### Q1: 服務啟動但沒看到安裝訊息？

**A**: 檢查日誌級別：
```yaml
logging:
  level:
    com.cheng.framework.config: INFO
```

### Q2: 安裝失敗怎麼辦？

**A**: 
1. 檢查是否在 Git 儲存庫中
2. 檢查 `.githooks/` 目錄是否存在
3. 手動執行：`./cheng.deploy/install-git-hooks.sh`

### Q3: Windows 環境會自動安裝嗎？

**A**: 會的！但執行權限設定在 Windows 上可能無效，建議使用 Git Bash。

## 📚 相關文件

- Git Hooks 說明：`.githooks/README.md`
- Commit 格式範例：`.githooks/COMMIT-MESSAGE-EXAMPLES.md`
- Mapper 驗證指南：`docs/Line/MAPPER_VERIFICATION_GUIDE.md`
- 手動安裝腳本：`cheng.deploy/install-git-hooks.sh`

## 🎓 最佳實踐

### 團隊規範

1. **保持服務執行** - 開發時保持服務開啟，確保 hooks 最新
2. **定期重啟** - 每天第一次開始開發時重啟服務
3. **檢查訊息** - 注意服務啟動時的 Git Hooks 安裝訊息
4. **遇到問題** - 先重啟服務，讓 hooks 重新安裝

### 維護 Hooks

1. **更新 .githooks/** - 修改 hooks 後提交到 Git
2. **通知團隊** - 告知有重要更新，建議重啟服務
3. **測試驗證** - 更新後自己先測試一次 commit

---

**建立日期**：2025-11-02  
**維護者**：開發團隊  
**狀態**：✅ 已實作並測試完成
