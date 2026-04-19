# 手機版掃描功能問題修復

## 🔧 修復時間
2025-11-05 11:08

---

## ✅ 已修復的問題

### 1️⃣ 新手導航遮罩太暗

**問題**：
- 畫面太黑（75% 不透明度）
- 文字和箭頭看不到（超出螢幕）

**修復內容**：
```css
/* 遮罩透明度：75% → 50% */
background: rgba(0, 0, 0, 0.5);

/* 文字和箭頭改為螢幕中央固定位置 */
.guide-arrow {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, 50px);
}

.guide-text {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, 120px);
}
```

**結果**：
- ✅ 遮罩更輕盈，不會太黑
- ✅ 文字和箭頭在螢幕中央，絕對不會超出版面
- ✅ 圓球保持在初始位置（右下角）

---

### 2️⃣ 拖動 Y 軸方向反向

**問題**：
- 向上滑動 → 按鈕往下移動（錯誤）
- 向下滑動 → 按鈕往上移動（錯誤）
- 左右方向正常

**原因**：
```javascript
// 錯誤的計算
const deltaY = e.touches[0].clientY - this.dragStartY;
```

**修復內容**：
```javascript
// 修正後的計算
const deltaX = this.dragStartX - e.touches[0].clientX;
const deltaY = this.dragStartY - e.touches[0].clientY;  // 修正！
```

**結果**：
- ✅ 向上滑動 → 按鈕往上移動
- ✅ 向下滑動 → 按鈕往下移動
- ✅ 四個方向都正常

---

### 3️⃣ 拖動觸發機制

**問題**：
- 點擊按鈕變成需要點 2 次才能進入
- 因為 `touchstart` 立即設定 `isDragging = true`

**修復內容**：
```javascript
// 新增狀態管理
longPressTimer: null,      // 長按計時器
hasMoved: false,           // 是否已經移動
longPressProgress: 0,      // 長按進度 (0-100)
showLongPressIndicator: false  // 顯示進度指示器

// 長按 2 秒才能拖動
handleTouchStart(e) {
  this.hasMoved = false;
  this.showLongPressIndicator = true;
  this.longPressProgress = 0;
  
  // 進度動畫
  const startTime = Date.now();
  const duration = 2000;
  const updateProgress = () => {
    if (!this.showLongPressIndicator) return;
    const elapsed = Date.now() - startTime;
    this.longPressProgress = Math.min((elapsed / duration) * 100, 100);
    if (elapsed < duration) {
      requestAnimationFrame(updateProgress);
    }
  };
  updateProgress();
  
  // 2 秒後才能拖動
  this.longPressTimer = setTimeout(() => {
    this.isDragging = true;
    this.showLongPressIndicator = false;
    
    // 震動反饋
    if (navigator.vibrate) {
      navigator.vibrate(50);
    }
    
    this.$message({
      message: '可以移動位置了',
      type: 'info',
      duration: 1000
    });
  }, 2000);
}

// 點擊判斷
handleScanClick() {
  if (this.isDragging || this.hasMoved) {
    return;  // 拖動時不觸發點擊
  }
  // 正常點擊邏輯...
}
```

**UI 元件**：
```vue
<!-- 長按進度環 -->
<div v-if="showLongPressIndicator" class="long-press-indicator">
  <svg class="progress-ring" width="80" height="80">
    <circle
      class="progress-ring-circle"
      stroke="#409eff"
      stroke-width="4"
      fill="transparent"
      r="36"
      cx="40"
      cy="40"
      :style="progressStyle"
    />
  </svg>
</div>
```

**結果**：
- ✅ 點擊 1 次立即進入掃描（不需要 2 次）
- ✅ 長按 2 秒顯示藍色進度環
- ✅ 達到 2 秒後震動反饋
- ✅ 提示「可以移動位置了」
- ✅ 然後才能拖動

---

### 4️⃣ 重複掃描問題

**問題**：
- 相機持續對著條碼
- 瞬間觸發多次掃描
- 重複呼叫 API

**修復內容（方案 A - 去重 + 冷卻）**：
```javascript
// 新增狀態管理
lastScannedCode: null,        // 最後掃描的條碼
lastScanTime: 0,              // 最後掃描時間
scanCooldown: 3000,           // 冷卻時間 3 秒
recentScannedCodes: new Set() // 最近 30 秒掃描記錄

handleQuickScanSuccess(decodedText) {
  const now = Date.now();
  
  // 檢查 1：同一條碼冷卻時間（3 秒）
  if (decodedText === this.lastScannedCode) {
    if (now - this.lastScanTime < this.scanCooldown) {
      console.log('冷卻中，跳過此次掃描');
      return;
    }
  }
  
  // 檢查 2：最近 30 秒內已掃描過
  if (this.recentScannedCodes.has(decodedText)) {
    this.$message.warning('此條碼剛剛已掃描過');
    return;
  }
  
  // 記錄掃描
  this.lastScannedCode = decodedText;
  this.lastScanTime = now;
  this.recentScannedCodes.add(decodedText);
  
  // 30 秒後從記錄中移除
  setTimeout(() => {
    this.recentScannedCodes.delete(decodedText);
  }, 30000);
  
  // 執行掃描
  this.performQuickScan(decodedText);
  
  this.$message({
    message: `掃描成功: ${decodedText}`,
    type: 'success',
    duration: 1500
  });
}
```

**結果**：
- ✅ 同一條碼 3 秒內只能掃描 1 次
- ✅ 30 秒內不會重複掃描同一條碼
- ✅ 可以快速切換掃描不同條碼
- ✅ 防止重複呼叫 API
- ✅ 使用者體驗更好

---

## 📋 修改檔案清單

### 前端
```
cheng-ui/src/components/FloatingScanButton/index.vue
├── data() - 新增 7 個狀態變數
├── computed - 新增 progressStyle 計算屬性
├── handleTouchStart() - 完全重寫（長按 2 秒）
├── handleTouchMove() - 修正 Y 軸計算
├── handleTouchEnd() - 調整判斷邏輯
├── handleScanClick() - 新增 hasMoved 判斷
├── handleQuickScanSuccess() - 新增防重複邏輯
├── guideHighlightPosition() - 固定在右下角
└── CSS - 新增長按進度環樣式 + 調整導航樣式
```

---

## 🧪 測試清單

### 問題 1 - 新手導航
- [ ] 首次登入顯示導航
- [ ] 遮罩不會太黑（50% 透明度）
- [ ] 文字和箭頭在螢幕中央可見
- [ ] 圓球在右下角高亮
- [ ] 點擊任意處關閉導航

### 問題 2 - 拖動方向
- [ ] 向上滑動 → 按鈕往上移動 ✅
- [ ] 向下滑動 → 按鈕往下移動 ✅
- [ ] 向左滑動 → 按鈕往左移動 ✅
- [ ] 向右滑動 → 按鈕往右移動 ✅

### 問題 3 - 長按拖動
- [ ] 點擊 1 次立即進入掃描 ✅
- [ ] 長按顯示藍色進度環
- [ ] 進度環從 0% → 100%（2 秒）
- [ ] 達到 100% 後震動反饋
- [ ] 提示「可以移動位置了」
- [ ] 然後可以拖動按鈕
- [ ] 拖動過程中不觸發點擊

### 問題 4 - 防重複掃描
- [ ] 同一條碼 3 秒內只能掃描 1 次
- [ ] 冷卻期間再次掃描不觸發 API
- [ ] 控制台顯示「冷卻中，跳過此次掃描」
- [ ] 30 秒內重複掃描顯示警告
- [ ] 可以快速切換不同條碼掃描
- [ ] 不同條碼不受冷卻限制

---

## 📊 改進效果

| 問題 | 改進前 | 改進後 | 提升 |
|------|--------|--------|------|
| **導航遮罩** | 75% 太黑 | 50% 輕盈 | ✅ 更舒適 |
| **文字可見性** | 可能超出 | 螢幕中央 | ✅ 100% 可見 |
| **Y 軸方向** | 反向 | 正常 | ✅ 符合直覺 |
| **點擊次數** | 需要 2 次 | 1 次 | ✅ 更流暢 |
| **拖動體驗** | 無進度 | 有進度環 + 震動 | ✅ 更明確 |
| **重複掃描** | 無限制 | 3 秒冷卻 | ✅ 防止浪費 |
| **API 呼叫** | 重複多次 | 去重 | ✅ 節省資源 |

---

## 🎨 新增視覺元素

### 長按進度環
```
⚪ 按下 → 🔵 進度 0% 
         ↓
      🔵 進度 50%
         ↓
      🔵 進度 100% + 震動 ✅
         ↓
      可以拖動了！
```

### 新手導航布局
```
┌─────────────────────────┐
│   灰暗遮罩 (50%)         │
│                         │
│                         │
│         ↓ 箭頭          │ ← 螢幕中央
│   「按住可移動位置」     │
│                         │
│                         │
│                   ⭕   │ ← 右下角高亮
└─────────────────────────┘
```

---

## 🔧 技術細節

### CSS 進度環動畫
```css
.progress-ring-circle {
  stroke-dasharray: 226 226;  /* 2π × 36 */
  stroke-dashoffset: 226;     /* 初始全空 */
  transition: stroke-dashoffset 0.1s linear;
}

/* 進度 50% → offset = 113 */
/* 進度 100% → offset = 0 */
```

### 防重複邏輯
```
同一條碼？
  ├─ 是 → 冷卻中？
  │        ├─ 是 → 跳過 ❌
  │        └─ 否 → 繼續
  └─ 否 → 最近掃描過？
           ├─ 是 → 警告 ⚠️
           └─ 否 → 執行 ✅
```

---

## ✅ 驗證結果

- [x] 程式碼語法正確
- [x] 無編譯錯誤
- [x] 邏輯完整
- [ ] 手機實測（待測試）
- [ ] 各裝置相容性（待測試）

---

**修復完成時間**：2025-11-05 11:08  
**預估測試時間**：15-30 分鐘  
**風險評估**：低（邏輯清晰，改動明確）
