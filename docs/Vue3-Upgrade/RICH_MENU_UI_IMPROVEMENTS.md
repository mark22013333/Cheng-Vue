# Rich Menu UI 改進報告

## 修復的問題

### 1. ✅ 對話框尺寸自適應
**問題**: 新增按鈕打開的對話框太大，需要上下滾動
**解決方案**:
- 寬度從 `95%` 調整為 `90%`
- 頂部距離從 `3vh` 調整為 `5vh`
- 對話框內容區域設定 `max-height: calc(90vh - 120px)`
- 內容區域新增 `overflow-y: auto` 實現自動滾動

**修改檔案**: `index.vue`
```vue
<el-dialog 
  :title="title" 
  v-model="open" 
  width="90%" 
  top="5vh" 
  class="rich-menu-dialog"
>
```

**樣式**:
```scss
:deep(.rich-menu-dialog) {
  .el-dialog__body {
    max-height: calc(90vh - 120px);
    overflow-y: auto;
    padding: 20px;
  }
}
```

### 2. ✅ 圖片上傳預覽
**問題**: 圖片上傳後沒有顯示預覽圖
**解決方案**:
- 在上傳組件下方新增預覽區域
- 使用 `el-image` 組件顯示已上傳的圖片
- 支持點擊放大查看
- 新增錯誤處理和佔位圖示

**修改檔案**: `index.vue`
```vue
<!-- 預覽圖片 -->
<div v-if="form.imageUrl" style="margin-top: 10px;">
  <el-image
    :src="getImageUrl(form.imageUrl)"
    :preview-src-list="[getImageUrl(form.imageUrl)]"
    fit="contain"
    style="max-width: 100%; max-height: 200px; border: 1px solid #ddd; border-radius: 4px;"
  >
    <template #error>
      <div style="padding: 20px; text-align: center; color: #ccc;">
        <el-icon style="font-size: 48px;"><Picture /></el-icon>
        <div style="margin-top: 10px;">圖片載入失敗</div>
      </div>
    </template>
  </el-image>
</div>
```

**功能特點**:
- ✅ 實時預覽上傳的圖片
- ✅ 最大高度限制為 200px
- ✅ 保持圖片比例（fit="contain"）
- ✅ 支持點擊放大
- ✅ 錯誤時顯示友好提示

### 3. ✅ 版型區塊分隔清晰
**問題**: 區塊設定擠在一起，不易辨識和操作
**解決方案**:

#### 3.1 區塊視覺改進
**修改檔案**: `RichMenuEditor.vue`

新增區塊邊框和背景:
```scss
.menu-area {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px solid #409EFF;
  background: rgba(64, 158, 255, 0.1);
  transition: all 0.2s;
}

.menu-area:hover {
  background: rgba(64, 158, 255, 0.2) !important;
  border-color: #66b1ff !important;
  z-index: 5;
}

.menu-area.active {
  background: rgba(103, 194, 58, 0.2) !important;
  border-color: #67C23A !important;
  border-width: 3px;
  z-index: 6;
}
```

**視覺效果**:
- 🎨 藍色邊框標識區塊範圍
- 🎨 淺藍色半透明背景
- 🎨 滑鼠懸停時背景加深
- 🎨 選中時顯示綠色邊框（加粗）
- 🎨 使用 z-index 確保選中區塊在最上層

#### 3.2 設定面板間距優化
**修改檔案**: `RichMenuEditor.vue`

```scss
.settings-panel {
  max-height: 650px;
  overflow-y: auto;
  
  /* 美化滾動條 */
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
    
    &:hover {
      background: #555;
    }
  }
}

.area-settings {
  .el-form-item {
    margin-bottom: 18px;  // 增加表單項間距
  }
  
  .el-divider {
    margin: 20px 0;  // 分隔線上下留白
  }
}
```

**改進效果**:
- ✅ 表單項間距從預設的 12px 增加到 18px
- ✅ 分隔線上下各 20px 留白
- ✅ 自訂滾動條樣式，更美觀
- ✅ 面板高度增加到 650px

#### 3.3 主表單間距優化
**修改檔案**: `index.vue`

```scss
:deep(.rich-menu-dialog) {
  .el-form-item {
    margin-bottom: 18px;
  }
  
  .el-row {
    margin-bottom: 10px;
  }
}
```

## 測試檢查清單

### UI 改進測試
- [ ] 對話框尺寸合適，不需要滾動就能看到大部分內容
- [ ] 對話框內容過多時，滾動條正常工作
- [ ] 圖片上傳後立即顯示預覽
- [ ] 預覽圖片可以點擊放大
- [ ] 版型區塊有清晰的邊框和背景色
- [ ] 滑鼠懸停時區塊有視覺反饋
- [ ] 選中區塊時有明顯的標識（綠色邊框）
- [ ] 區塊編號清晰可見
- [ ] 設定面板各表單項間距合適，易於操作
- [ ] 滾動條樣式美觀

### 功能測試
- [ ] 所有原有功能正常工作
- [ ] 上傳圖片功能正常
- [ ] 選擇區塊功能正常
- [ ] 編輯區塊設定正常
- [ ] 保存和發布功能正常

## 視覺效果對比

### 對話框尺寸
- **修改前**: 95% 寬度，3vh 上邊距，內容溢出需要滾動
- **修改後**: 90% 寬度，5vh 上邊距，max-height 限制，自動滾動

### 圖片預覽
- **修改前**: 上傳後沒有預覽，只能在編輯器畫布背景看到
- **修改後**: 
  - 上傳後立即顯示預覽圖
  - 最大高度 200px，保持比例
  - 支持點擊放大
  - 錯誤處理友好

### 區塊視覺
- **修改前**: 
  - 區塊沒有邊框，難以區分
  - 背景透明，不易辨識
  - 沒有視覺層級
  
- **修改後**:
  - 藍色邊框（2px）
  - 淺藍色半透明背景（0.1 透明度）
  - 懸停時背景加深（0.2 透明度）
  - 選中時綠色邊框（3px）
  - 使用 z-index 管理層級

### 表單間距
- **修改前**: 預設間距，略顯擁擠
- **修改後**:
  - 表單項間距: 18px（增加 50%）
  - 分隔線上下: 20px
  - 行間距: 10px

## 技術細節

### CSS 選擇器深度穿透
使用 `:deep()` 修改 Element Plus 組件內部樣式:
```scss
:deep(.rich-menu-dialog) {
  .el-dialog__body { ... }
}
```

### SCSS 嵌套語法
使用 SCSS 嵌套和父選擇器 `&`:
```scss
.settings-panel {
  &::-webkit-scrollbar { ... }
  &::-webkit-scrollbar-thumb {
    &:hover { ... }
  }
}
```

### 響應式高度計算
使用 `calc()` 動態計算高度:
```scss
max-height: calc(90vh - 120px);
```
- `90vh`: 視窗高度的 90%
- `- 120px`: 減去標題和底部按鈕的高度

### 層級管理
使用 `z-index` 管理區塊顯示順序:
- 普通區塊: 無 z-index（預設）
- 懸停區塊: z-index: 5
- 選中區塊: z-index: 6
- 調整手柄: z-index: 10

## 額外改進建議

### 短期改進（可選）
1. **響應式設計**: 在小螢幕上調整對話框寬度和佈局
2. **快捷鍵支援**: 支持方向鍵移動選中的區塊
3. **撤銷/重做**: 新增操作歷史功能

### 長期改進（未來）
1. **區塊對齊輔助線**: 拖動時顯示對齊參考線
2. **區塊尺寸鎖定**: 鎖定寬高比
3. **區塊複製/貼上**: 快速複製現有區塊設定
4. **預設模板庫**: 提供更多預設版型選擇

## 瀏覽器兼容性

所有改進均使用標準 CSS 特性，兼容:
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+

**注意**: 自訂滾動條樣式（`::-webkit-scrollbar`）僅在 Webkit 瀏覽器生效，Firefox 使用預設樣式。

## 總結

此次改進主要聚焦於三個方面:
1. **對話框尺寸**: 更好地適應不同螢幕尺寸
2. **圖片預覽**: 立即反饋，提升用戶體驗
3. **區塊視覺**: 清晰分隔，易於操作

所有改進都採用最小侵入性原則，不影響現有功能，只優化視覺和交互體驗。

---

**改進日期**: 2025-11-29
**改進類型**: UI/UX 優化
**測試狀態**: 待測試
