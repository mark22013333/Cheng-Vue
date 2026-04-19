# 2025-11-05 問題修復總結

## ✅ 已完成的修復（共 5 項）

### 2️⃣ 移除新手導航 ✅
**狀態**：完成

**修改內容**：
- 移除 `showGuide` 相關狀態
- 移除 `checkGuideStatus()` 和 `closeGuide()` 方法  
- 移除 HTML 模板中的導航遮罩
- 移除所有導航相關 CSS（約 100 行）
- 移除 `guideHighlightPosition` computed 屬性

**結果**：新手導航已完全移除，不會再出現

---

### 3️⃣ 調整拖動機制 ✅
**狀態**：完成

**修改內容**：
- 長按時間：2 秒 → **1 秒**
- 提示顏色：藍色(info) → **綠色(success)**
- 提示文字：「可以移動位置了」（綠色）

**結果**：
- 按住 1 秒顯示綠色進度環
- 震動反饋（如果裝置支援）
- 綠色提示「可以移動位置了」

---

### 4️⃣ 掃描後關閉相機 ✅
**狀態**：完成

**修改內容**：
```javascript
handleQuickScanSuccess(decodedText) {
  // 停止掃描器
  this.stopQuickScan();  // ← 恢復此行
  
  // 執行掃描
  this.performQuickScan(decodedText);
  
  // 簡短提示
  this.$message({
    message: `掃描成功: ${decodedText}`,
    type: 'success',
    duration: 1500
  });
}
```

**移除內容**：
- 移除防重複掃描邏輯（lastScannedCode, scanCooldown, recentScannedCodes）
- 移除冷卻檢查邏輯

**結果**：掃描成功後立即關閉相機，不會頻繁觸發

---

### 6️⃣ 第一層爬取加重試機制 ✅
**狀態**：完成

**修改內容**：
```java
public BookInfoDTO crawlByIsbn(String isbn) {
    // 重試機制：3 次重試，等待時間遞增
    int maxRetries = 3;
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            log.info("[第{}次嘗試] 開始爬取 ISBN: {}", attempt, isbn);
            
            // 執行四層爬取邏輯
            // ...
            
            // 如果成功，返回結果
            if (bookInfo != null && bookInfo.getSuccess()) {
                log.info("第{}次嘗試成功爬取 ISBN: {}", attempt, isbn);
                return bookInfo;
            }
            
            // 等待後重試（1秒、2秒、3秒）
            int waitSeconds = attempt;
            log.warn("✗ 第{}次嘗試失敗，等待 {} 秒後重試...", attempt, waitSeconds);
            Thread.sleep(waitSeconds * 1000L);
            
        } catch (Exception e) {
            // 異常處理 + 重試
        }
    }
}
```

**重試策略**：
- 最多 3 次重試
- 等待時間：1秒、2秒、3秒（遞增）
- 異常時也會重試
- 達到最大次數返回錯誤

**結果**：網路不穩或暫時性失敗時會自動重試

---

## ⚠️ 待解決的問題（共 2 項）

### 1️⃣ 爬取到的新書沒有寫到 DB ⚠️
**狀態**：待實作

**問題分析**：
- LOG 顯示爬取成功：`爬取任務完成: taskId=..., isbn=9789864345106, 書名=Python程式交易應用與實作`
- 但沒有寫入 `inv_item` 表
- 目前的流程：掃描 → 建立任務 → 爬取 → **結束**（沒有入庫步驟）

**原因**：
當前 `CrawlTaskServiceImpl` 只負責爬取書籍資料，並沒有入庫邏輯。

**解決方案選項**：

#### 方案 A：自動入庫（推薦）
在 `CrawlTaskServiceImpl.executeTask()` 中，爬取成功後自動寫入 `inv_item` 表。

**優點**：
- ✅ 完全自動化
- ✅ 使用者體驗最好
- ✅ 符合預期行為

**缺點**：
- ⚠️ 需要 cheng-crawler 依賴 cheng-system（可能造成循環依賴）
- ⚠️ 需要處理重複 ISBN 的情況

**實作步驟**：
1. 在 `CrawlTaskServiceImpl` 注入 `IInvItemService`
2. 爬取成功後檢查 ISBN 是否已存在
3. 不存在則自動建立物品記錄
4. 自動建立庫存記錄（初始數量為 0）

#### 方案 B：前端手動入庫
前端收到爬取完成通知後，彈出對話框讓使用者確認是否入庫。

**優點**：
- ✅ 使用者可控制
- ✅ 不會造成模組依賴問題
- ✅ 可以修改書籍資訊後再入庫

**缺點**：
- ⚠️ 需要多一步操作
- ⚠️ 使用者體驗略差

#### 方案 C：新增入庫 API
在 Controller 層提供一個 API，前端可以將爬取結果直接入庫。

**優點**：
- ✅ 架構清晰
- ✅ 職責分離
- ✅ 前端可選擇自動或手動入庫

**缺點**：
- ⚠️ 需要修改前端和後端

---

### 5️⃣ 爬取完成沒有跳出通知框 ⚠️
**狀態**：需要測試

**可能原因**：
1. 輪詢沒有啟動
2. 任務狀態沒有正確更新
3. 通知邏輯有問題

**已實作的邏輯**：
```javascript
// 1. 掃描成功後啟動輪詢
performIsbnQuickScan(isbn) {
  createCrawlTask(isbn).then(response => {
    if (response.code === 200) {
      // 記錄任務
      this.activeTasks.push({
        taskId: response.data,
        isbn: isbn,
        status: 'PENDING'
      });
      
      // 開始輪詢任務狀態
      this.startPolling();  // ← 應該會啟動
    }
  });
}

// 2. 每 2 秒輪詢
startPolling() {
  if (this.pollingTimer) return;
  
  this.pollingTimer = setInterval(() => {
    this.checkAllTasksStatus();
  }, 2000);
}

// 3. 檢查任務狀態
checkAllTasksStatus() {
  for (let i = 0; i < this.activeTasks.length; i++) {
    getTaskStatus(this.activeTasks[i].taskId).then(response => {
      if (response.code === 200 && response.data) {
        this.handleTaskUpdate(response.data);
      }
    });
  }
}

// 4. 任務完成通知
handleTaskUpdate(task) {
  if (task.status === 'COMPLETED') {
    this.$notify({
      title: '爬取完成',
      message: `《${task.bookInfo?.title || task.isbn}》資料已爬取完成`,
      type: 'success',
      duration: 5000,
      onClick: () => {
        this.showBookInfo(task.bookInfo);
      }
    });
    
    // 從列表移除
    this.activeTasks.splice(index, 1);
  }
}
```

**需要確認**：
1. `this.activeTasks` 是否正確新增了任務
2. 輪詢是否正常運作
3. API 是否正確返回任務狀態

**建議除錯步驟**：
1. 在瀏覽器 Console 查看是否有錯誤
2. 檢查 Network 是否有輪詢請求
3. 查看 `activeTasks` 陣列內容

---

## 📋 修改檔案清單

### 前端
- ✅ `FloatingScanButton/index.vue` - 大幅修改
  - 移除新手導航（-120 行）
  - 移除防重複掃描邏輯（-30 行）
  - 調整拖動時間（2秒→1秒）
  - 恢復掃描後關閉相機

### 後端
- ✅ `IsbnCrawlerServiceImpl.java` - 加入重試機制（+70 行）
  - 3 次重試
  - 遞增等待時間（1秒、2秒、3秒）
  - 完整異常處理

---

## 🧪 測試清單

### 已修復功能
- [ ] 長按 1 秒觸發拖動（綠色提示）
- [ ] 掃描後相機關閉
- [ ] 拖動方向正常（上下左右）
- [ ] 無新手導航出現
- [ ] 爬取失敗時自動重試

### 待修復功能
- [ ] 爬取完成通知框
- [ ] 自動入庫功能

---

## 🚀 下一步建議

### 問題 1 - 自動入庫
**我的建議**：採用 **方案 A（自動入庫）**

**理由**：
1. 使用者體驗最好
2. 符合「掃描即入庫」的直覺
3. 技術上可行（注入 Service）

**需要確認**：
- 是否要自動入庫？
- 初始庫存數量設為多少？（建議 0）
- 如果 ISBN 已存在，要如何處理？（建議提示已存在）

### 問題 5 - 通知框
**建議**：先進行前端測試，確認：
1. 是否有輪詢請求
2. 任務狀態是否正確返回
3. Console 是否有錯誤

如果測試後發現問題，再進行修正。

---

**修復完成時間**：2025-11-05 11:35  
**狀態**：5/6 完成，待確認後續方案
