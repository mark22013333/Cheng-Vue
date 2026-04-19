# 📏 側邊欄可調整寬度功能說明

## 功能概述

為解決側邊欄選單文字過長被截斷（顯示為 `...`）的問題，新增了側邊欄寬度可調整功能。使用者可以透過拖曳側邊欄右側邊緣來調整寬度，調整後的寬度會自動儲存到瀏覽器 Cookie，下次登入時會記住設定。

---

## 🎯 功能特性

### ✅ 核心功能
- **拖曳調整**：在側邊欄右側邊緣拖曳即可調整寬度
- **視覺提示**：滑鼠懸停時顯示藍色分隔條
- **寬度限制**：最小 180px，最大 400px
- **自動儲存**：調整後的寬度自動儲存到 Cookie
- **記憶功能**：下次登入自動套用上次設定的寬度
- **平滑過渡**：寬度調整時有流暢的過渡動畫

### 🎨 視覺效果
- 預設狀態：分隔條透明，不干擾視覺
- 懸停狀態：顯示半透明藍色分隔條（rgba(64, 158, 255, 0.5)）
- 拖曳狀態：顯示實心藍色分隔條（rgba(64, 158, 255, 0.8)）
- 滑鼠游標：自動變更為左右調整游標（ew-resize）

---

## 🛠️ 技術實現

### 修改的檔案

#### 1. Vuex Store
**檔案**：`cheng-ui/src/store/modules/app.js`

**新增內容**：
- `sidebar.width` 狀態（預設 200px）
- `SET_SIDEBAR_WIDTH` mutation
- `setSidebarWidth` action

```javascript
// 新增狀態
sidebar: {
  opened: ...,
  withoutAnimation: false,
  hide: false,
  width: parseInt(Cookies.get('sidebarWidth')) || 200 // 新增
}

// 新增 mutation
SET_SIDEBAR_WIDTH: (state, width) => {
  state.sidebar.width = width
  Cookies.set('sidebarWidth', width)
}

// 新增 action
setSidebarWidth({ commit }, width) {
  commit('SET_SIDEBAR_WIDTH', width)
}
```

#### 2. Sidebar 組件
**檔案**：`cheng-ui/src/layout/components/Sidebar/index.vue`

**新增內容**：
- 可拖曳的分隔條元素
- 拖曳邏輯（startResize 方法）
- 分隔條樣式

```vue
<!-- 新增分隔條 -->
<div 
  v-if="!isCollapse" 
  class="sidebar-resizer" 
  @mousedown="startResize"
  :title="'拖曳調整選單寬度 (目前: ' + sidebar.width + 'px)'"
></div>

<!-- 新增拖曳邏輯 -->
methods: {
  startResize(e) {
    // 處理滑鼠拖曳事件
    // 計算新寬度並更新 Vuex
  }
}
```

#### 3. CSS 樣式
**檔案**：`cheng-ui/src/assets/styles/sidebar.scss`

**修改內容**：
- 將固定寬度 `$base-sidebar-width` 改為 CSS 變數 `var(--sidebar-width)`
- 支援動態寬度調整

```scss
// 修改前
width: $base-sidebar-width !important;

// 修改後
width: var(--sidebar-width, $base-sidebar-width) !important;
```

#### 4. Layout 主組件
**檔案**：`cheng-ui/src/layout/index.vue`

**修改內容**：
- 在根元素設定 CSS 變數 `--sidebar-width`

```vue
<div :style="{'--sidebar-width': sidebar.width + 'px'}">
```

---

## 📐 寬度限制說明

| 參數 | 數值 | 說明 |
|------|------|------|
| **預設寬度** | 200px | 系統預設的側邊欄寬度 |
| **最小寬度** | 180px | 防止選單過窄影響可讀性 |
| **最大寬度** | 400px | 防止選單過寬佔用過多螢幕空間 |
| **摺疊寬度** | 54px | 選單摺疊時的固定寬度（不可調整） |

---

## 🎮 使用方式

### 調整側邊欄寬度

1. **找到調整區域**
   - 將滑鼠移動到側邊欄最右側邊緣
   - 滑鼠游標會變成左右箭頭（↔）

2. **開始調整**
   - 按住滑鼠左鍵
   - 向左拖曳：縮小寬度
   - 向右拖曳：增加寬度

3. **完成調整**
   - 鬆開滑鼠左鍵
   - 寬度自動儲存

4. **重置寬度**
   - 方法一：清除瀏覽器 Cookie 中的 `sidebarWidth`
   - 方法二：拖曳至 200px 附近即為預設寬度

### 查看目前寬度

- 將滑鼠懸停在分隔條上
- Tooltip 會顯示：「拖曳調整選單寬度 (目前: XXXpx)」

---

## 💡 使用情境

### 適用場景
1. **選單文字過長**：選單項目名稱較長時（如「物品與庫存管理」）
2. **多層級選單**：有多層子選單時，增加寬度可以更好地顯示層級關係
3. **個人偏好**：依據個人使用習慣調整舒適的寬度

### 建議設定
- **一般使用**：200-250px（預設範圍）
- **長文字選單**：250-300px
- **精簡模式**：180-200px
- **大螢幕**：可以設定到 350-400px

---

## 🔧 技術細節

### Cookie 儲存
```javascript
// 儲存格式
Cookies.set('sidebarWidth', width)

// 讀取格式
parseInt(Cookies.get('sidebarWidth')) || 200
```

### CSS 變數傳遞
```vue
<!-- 從 Vue 傳遞到 CSS -->
:style="{'--sidebar-width': sidebar.width + 'px'}"

<!-- CSS 中使用 -->
width: var(--sidebar-width, 200px) !important;
```

### 拖曳事件流程
1. `mousedown` - 開始拖曳，記錄初始位置和寬度
2. `mousemove` - 計算位移量，更新寬度（限制範圍）
3. `mouseup` - 結束拖曳，移除事件監聽

---

## 📱 響應式支援

### 桌面模式（Desktop）
- ✅ 完整支援拖曳調整功能
- ✅ 寬度記憶功能正常運作

### 行動裝置模式（Mobile）
- ⚠️ 選單摺疊時不顯示分隔條
- ⚠️ 使用固定寬度，不可調整
- ℹ️ 這是為了確保行動裝置上的最佳體驗

---

## 🐛 已知限制

1. **選單摺疊時不可調整**
   - 選單摺疊（只顯示圖示）時，分隔條會隱藏
   - 需要展開選單後才能調整

2. **行動裝置不支援**
   - 行動裝置模式下使用固定寬度
   - 這是設計上的考量，避免影響使用體驗

3. **寬度同步**
   - 不同瀏覽器間的寬度設定不會同步
   - 每個瀏覽器需要單獨調整（儲存在各自的 Cookie）

---

## 🎯 未來優化方向

### 短期優化
- [ ] 新增「重置為預設寬度」按鈕
- [ ] 雙擊分隔條自動調整為最適寬度
- [ ] 新增寬度調整的音效回饋

### 中期優化
- [ ] 記住不同頁面的寬度設定
- [ ] 支援鍵盤快捷鍵調整（Ctrl + [ / ]）
- [ ] 新增寬度預設選項（小/中/大）

### 長期優化
- [ ] 將寬度設定同步到使用者帳戶
- [ ] 支援不同裝置間的設定同步
- [ ] 新增視覺化的寬度調整預覽

---

## 📞 技術支援

如有問題或建議，請參考：
- **開發者**：cheng
- **完成日期**：2025-10-04
- **相關文件**：`docs/inventory_integration_guide.md`

---

**🎉 現在您可以自由調整側邊欄寬度，讓選單顯示更完整！**
