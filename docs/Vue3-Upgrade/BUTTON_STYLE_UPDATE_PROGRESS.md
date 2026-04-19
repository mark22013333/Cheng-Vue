# 按鈕和輸入框樣式統一進度

## 目標
統一所有頁面的按鈕和輸入框樣式，參考 `system/user/index.vue`

## 已完成 ✅

### 1. 角色管理 (`/src/views/system/role/index.vue`)
- ✅ 搜尋表單按鈕：改用 `<el-icon>` 元件包裹圖標
- ✅ 操作按鈕組：新增、修改、刪除、匯出
- ✅ 表格操作列按鈕：`type="primary" link`
- ✅ 下拉選單按鈕和選項圖標
- ✅ 分頁元件：`.sync` → `v-model:`
- ✅ 對話框：`:visible.sync` → `v-model`
- ✅ Slot 語法：`slot="label"` → `template #label`
- ✅ 日期選擇器：`value-format="yyyy-MM-dd"` → `"YYYY-MM-DD"`
- ✅ 事件修飾符：`@keyup.enter.native` → `@keyup.enter`
- ✅ 匯入並註冊所有 Element Plus 圖標元件
- ✅ **修復樹狀選單滾動條問題**：新增最大高度和滾動條樣式

## 待處理頁面（準備批量更新）

### 系統管理（9個頁面）
- [x] **選單管理** (`/system/menu/index.vue`) ✅
- [x] **部門管理** (`/system/dept/index.vue`) ✅
- [x] **崗位管理** (`/system/post/index.vue`) ✅
- [ ] **字典管理** (`/system/dict/index.vue`)
- [ ] **字典資料** (`/system/dict/data.vue`)
- [ ] **參數設定** (`/system/config/index.vue`)
- [ ] **通知公告** (`/system/notice/index.vue`)
- [ ] **角色授權使用者** (`/system/role/authUser.vue`)
- [ ] **選擇使用者** (`/system/role/selectUser.vue`)

### 需要統一的樣式
1. **搜尋按鈕**：`icon="el-icon-search"` → `<el-icon><Search/></el-icon>`
2. **操作按鈕**：`type="text"` → `type="primary" link`
3. **事件修飾符**：`@keyup.enter.native` → `@keyup.enter`
4. **對話框綁定**：`:visible.sync` → `v-model`
5. **雙向綁定**：`:showSearch.sync` → `v-model:showSearch`
6. **插槽語法**：`slot="footer"` → `<template #footer>`
7. **表格行高**：已全域統一（padding: 8px 0）

## 全域樣式已完成 ✅

### `/src/assets/styles/cheng.scss`
- ✅ 統一按鈕樣式（小尺寸、預設尺寸、plain）
- ✅ **按鈕圓角：`border-radius: 20px`**（更圓的按鈕）
- ✅ 按鈕 icon 間距：4px
- ✅ icon 垂直對齊：`vertical-align: middle`
- ✅ 兼容 Vue 2 的 `[class*="el-icon-"]` 寫法
- ✅ **Link 按鈕樣式優化**：
  - `height: auto` 和 `line-height: 1`
  - 表格內 link 按鈕間距：10px
  - 垂直對齊：`vertical-align: middle`
- ✅ 統一輸入框樣式：`border-radius: 4px`
- ✅ 統一 `el-select`、`el-date-editor` 樣式
- ✅ **樹狀選單滾動條樣式**：
  - 最大高度：`300px`
  - 自動滾動：`overflow-y: auto`
  - 美化滾動條：寬度 6px，圓角，hover 效果

### 表格欄位寬度優化

**角色管理** (`/src/views/system/role/index.vue`):
- 選擇欄：`50px`
- 角色編號：`100px`
- 角色名稱：`min-width: 120px`
- 權限字串：`min-width: 140px`
- 顯示順序：`90px` + `align="center"`
- 狀態：`80px`
- 建立時間：`160px`
- **操作：`180px`**

**使用者管理** (`/src/views/system/user/index.vue`):
- 狀態：`80px`
- **操作：`180px`**

### RightToolbar 和操作欄按鈕對齊優化

**RightToolbar** (`/src/components/RightToolbar/index.vue`):
- ✅ 增加上方 padding（8px），避免 tooltip 被切到頁面邊緣
- ✅ 圓形按鈕使用 flexbox 確保 icon 置中
- ✅ 按鈕容器使用 flex 版面和 gap 間距
- ✅ 強制設定按鈕尺寸 32px × 32px，確保 icon 完美置中
- ✅ 移除 icon 的 margin，設定 line-height: 1

**表格操作欄對齊**:
- ✅ 操作欄使用 flexbox 確保按鈕垂直對齊
- ✅ dropdown 按鈕與 link 按鈕對齊在同一水平線
- ✅ 統一使用 `gap: 10px` 控制按鈕間距
- ✅ 增加表格行高（padding: 8px 0），避免按鈕被切到
- ✅ 操作欄最小高度 44px，確保圓形按鈕不被裁切

## 批量更新計劃

### 階段一：核心樣式已完成 ✅
- ✅ 角色管理頁面
- ✅ 使用者管理頁面
- ✅ 全域 CSS 樣式（cheng.scss）
  - 表格行高：5px（更緊湊）
  - 操作欄高度：38px
  - 表格內容：置中顯示
  - 輸入框寬度：統一 240px
- ✅ RightToolbar 元件樣式
- ✅ 表格行高和操作欄對齊

### 階段二：批量更新其他系統頁面（✅ 已完成）
**更新順序**：
1. ✅ 選單管理
2. ✅ 部門管理
3. ✅ 崗位管理
4. ✅ 字典管理
5. ✅ 字典資料
6. ✅ 參數設定
7. ✅ 通知公告
8. ✅ 角色授權使用者
9. ✅ 選擇使用者

### 階段三：監控管理頁面（進行中）
**優先級 1 - 核心監控頁面（✅ 已完成）**：
1. ✅ 操作日誌
2. ✅ 登入日誌
3. ✅ 線上使用者

**優先級 2 - 系統管理補完（✅ 已完成）**：
4. ✅ 角色管理主頁
5. ✅ 使用者管理主頁
6. ✅ 個人資料頁

**優先級 3 - 其他監控工具（進行中）**：
7. ✅ 定時任務
8. ✅ 任務日誌
9. 緩存管理

**每個頁面的修改**：
- ✅ 移除所有 `size="small"` 屬性（使用預設尺寸）
- ✅ 更新按鈕 icon 語法（`el-icon-*` → `<el-icon><ComponentName/></el-icon>`）
- ✅ 更新表格操作按鈕（`type="text"` → `type="primary" link`）
- ✅ 移除 `.native` 修飾符
- ✅ 更新對話框綁定（`:visible.sync` → `v-model`）
- ✅ 更新雙向綁定（`:showSearch.sync` → `v-model:showSearch`）
- ✅ 更新插槽語法（`slot="footer"` → `<template #footer>`）
- ✅ 匯入並註冊 Element Plus 圖標元件

## 測試步驟

### 1. 清除緩存（必須！）
```bash
# 硬重新整理：Mac 按 Cmd+Shift+R，Windows 按 Ctrl+Shift+R
# 或使用無痕模式測試
```

### 2. 檢查角色管理頁面
1. 前往 `/system/role`
2. 檢查搜尋按鈕樣式（應有圖標和文字，間距正確）
3. 檢查操作按鈕組（新增、修改、刪除、匯出）
4. 檢查表格內的操作按鈕（修改、刪除、更多）
5. 檢查輸入框和下拉選單的圓角
6. **檢查樹狀選單滾動條**：
   - 點擊「新增」或「修改」按鈕開啟對話框
   - 查看「選單權限」的樹狀選單區域
   - 確認最大高度約 300px，超過時顯示滾動條
   - 滾動條應該美觀（細滾動條，圓角，hover 有變色）
7. **檢查 RightToolbar 按鈕**：
   - 查看右上角三個圓形按鈕（搜尋、重新整理、選單）
   - 按鈕應該是完整圓形，不會被切到
   - Hover 時 tooltip 不會被頁面邊緣切到
   - Icon 應該在按鈕中心，完美置中
8. **檢查表格操作欄對齊**：
   - 查看表格「操作」欄位的按鈕
   - 「修改」、「刪除」、「更多」按鈕應該在同一水平線
   - 按鈕之間間距一致（10px）

### 3. 比對參考頁面
- 開啟 `/system/user` 頁面
- 開啟 `/system/role` 頁面
- 比對兩頁面的按鈕和輸入框樣式是否一致

### 4. 功能測試
- 點擊搜尋、重置按鈕
- 點擊新增、修改、刪除按鈕
- 點擊表格內的操作按鈕
- 檢查對話框是否正常開啟

## 更新模式（後續頁面參考）

每個頁面需要更新的項目：

### 1. Template 部分
```vue
<!-- ❌ 舊寫法 -->
<el-button icon="el-icon-search" size="small" type="primary">搜尋</el-button>

<!-- ✅ 新寫法 -->
<el-button type="primary">
  <el-icon class="el-icon--left">
    <Search/>
  </el-icon>
  搜尋
</el-button>
```

### 2. 表格操作列
```vue
<!-- ❌ 舊寫法 -->
<el-button size="small" type="text" icon="el-icon-edit">修改</el-button>

<!-- ✅ 新寫法 -->
<el-button size="small" type="primary" link icon="Edit">修改</el-button>
```

### 3. 事件修飾符
```vue
<!-- ❌ 舊寫法 -->
@keyup.enter.native="handleQuery"

<!-- ✅ 新寫法 -->
@keyup.enter="handleQuery"
```

### 4. 雙向綁定
```vue
<!-- ❌ 舊寫法 -->
:visible.sync="open"
:showSearch.sync="showSearch"

<!-- ✅ 新寫法 -->
v-model="open"
v-model:showSearch="showSearch"
```

### 5. Slot 語法
```vue
<!-- ❌ 舊寫法 -->
<span slot="label">標籤</span>
<div slot="footer">按鈕</div>

<!-- ✅ 新寫法 -->
<template #label>標籤</template>
<template #footer>按鈕</template>
```

### 6. Script 部分
```javascript
// 匯入圖標
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  Download
  // ... 其他需要的圖標
} from '@element-plus/icons-vue'

export default {
  name: "PageName",
  components: {
    Search,
    Refresh,
    Plus,
    Edit,
    Delete,
    Download
  },
  // ...
}
```

## 常用圖標對照表

| 舊語法 | 新元件 | 用途 |
|--------|--------|------|
| `el-icon-search` | `<Search/>` | 搜尋 |
| `el-icon-refresh` | `<Refresh/>` | 重置、重新整理 |
| `el-icon-plus` | `<Plus/>` | 新增 |
| `el-icon-edit` | `<Edit/>` | 修改、編輯 |
| `el-icon-delete` | `<Delete/>` | 刪除 |
| `el-icon-download` | `<Download/>` | 下載、匯出 |
| `el-icon-upload` | `<Upload/>` | 上傳、匯入 |
| `el-icon-circle-check` | `<CircleCheck/>` | 選中、確認 |
| `el-icon-user` | `<User/>` | 使用者 |
| `el-icon-d-arrow-right` | `<DArrowRight/>` | 更多、下拉箭頭 |
| `el-icon-question` | `<QuestionFilled/>` | 問號、提示 |

## 預期效果

更新後，所有頁面應該：
- ✅ 按鈕大小一致
- ✅ 圖標與文字間距一致（4px）
- ✅ 圖標垂直居中對齊
- ✅ 輸入框圓角一致（4px）
- ✅ 沒有 Vue 3 兼容性警告
- ✅ 所有功能正常運作

## 下一步

1. 測試角色管理頁面
2. 依序更新其他頁面
3. 每次更新一個頁面後進行測試
4. 確認無誤後再更新下一個

## 注意事項

- 每次修改後必須清除瀏覽器緩存
- 使用無痕模式測試可避免緩存問題
- 如果遇到圖標不顯示，檢查是否已匯入和註冊元件
- 如果遇到功能異常，檢查事件綁定是否正確
