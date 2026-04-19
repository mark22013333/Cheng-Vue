# 依賴升級分析報告

> **分析日期**：2025-11-22  
> **專案路徑**：/cheng-ui  
> **當前分支**：feature/vue3-migration

---

## 📊 升級總覽

### 依賴分類統計

| 分類 | 數量 | 處理策略 |
|------|------|----------|
| 核心框架（需替換） | 5 | 完全替換為 Vue 3 生態 |
| UI 框架（需替換） | 1 | Element UI → Element Plus |
| 建構工具（需移除） | 2 | Vue CLI → Vite |
| 可直接升級 | 18 | 升級到最新版本 |
| 需特別處理 | 4 | 檢查兼容性後升級 |
| **總計** | **30** | - |

---

## 🔴 核心框架升級（必須處理）

### 1. Vue 生態系統

| 套件 | 當前版本 | Vue 2 最新 | Vue 3 目標 | 處理方式 |
|------|----------|------------|------------|----------|
| **vue** | 2.6.12 | 2.7.16 | **3.5.24** | ⚠️ 完全替換 |
| **vue-router** | 3.4.9 | 3.6.5 | **4.6.3** | ⚠️ 完全替換 |
| **vuex** | 3.6.0 | 3.6.2 | **4.1.0 或 Pinia 2.x** | ⚠️ 建議用 Pinia |
| **vue-template-compiler** | 2.6.12 | 2.7.16 | **（移除）** | ⚠️ Vue 3 不需要 |

**重要說明**：
- `vue-template-compiler` 在 Vue 3 中已整合到核心，直接移除
- Vuex 4 與 Vue 3 兼容，但建議遷移到 Pinia（官方推薦）

---

### 2. UI 框架

| 套件 | 當前版本 | 目標版本 | 處理方式 |
|------|----------|----------|----------|
| **element-ui** | 2.15.14 | **移除** | ⚠️ 完全替換 |
| **@element-plus/icons-vue** | - | **2.3.x** | ✅ 新安裝 |
| **element-plus** | - | **2.5.x** | ✅ 新安裝 |

**影響範圍**：
- 所有 Element UI 元件需更新語法
- 圖標系統需完全重寫
- 樣式變數需重新配置

---

### 3. 建構工具

| 套件 | 當前版本 | 處理方式 |
|------|----------|----------|
| **@vue/cli-plugin-babel** | 4.4.6 | ⚠️ 移除（改用 Vite） |
| **@vue/cli-service** | 4.4.6 | ⚠️ 移除（改用 Vite） |
| **sass-loader** | 10.1.1 | ⚠️ 移除（Vite 自動處理） |
| **compression-webpack-plugin** | 6.1.2 | ⚠️ 移除（改用 vite-plugin-compression） |
| **svg-sprite-loader** | 5.1.1 | ⚠️ 移除（改用 vite-plugin-svg-icons） |

**新增依賴**：
```json
{
  "vite": "^5.0.0",
  "@vitejs/plugin-vue": "^5.0.0",
  "vite-plugin-svg-icons": "^2.0.0",
  "vite-plugin-compression": "^0.5.0"
}
```

---

## 🟡 需特別處理的依賴

### 1. Vue 插件

| 套件 | 當前版本 | 目標版本 | Vue 3 替代品 | 狀態 |
|------|----------|----------|--------------|------|
| **vue-clipboard2** | 0.3.3 | - | **vue-clipboard3** ^2.0.0 | ⚠️ 替換 |
| **@riophae/vue-treeselect** | 0.4.0 | - | **@riophae/vue-treeselect** @next | ⚠️ 升級 |
| **vuedraggable** | 2.24.3 | - | **vuedraggable** @next | ⚠️ 升級 |
| **vue-cropper** | 0.5.5 | 0.6.5 | 檢查兼容性 | 🔍 需測試 |
| **vue-count-to** | 1.0.13 | - | 檢查兼容性 | 🔍 需測試 |

**處理建議**：
- `vue-clipboard2` → `vue-clipboard3`（API 幾乎相同）
- `vue-treeselect` → 使用 @next 版本
- `vuedraggable` → 使用 @next 版本
- `vue-cropper` 和 `vue-count-to` 需檢查是否支援 Vue 3

---

### 2. 第三方工具庫（可能影響）

| 套件 | 當前版本 | 最新版本 | 破壞性變更 | 建議 |
|------|----------|----------|------------|------|
| **axios** | 0.28.1 | 1.13.2 | ⚠️ 有 | 升級並測試 |
| **echarts** | 5.4.0 | 6.0.0 | ⚠️ 有 | 謹慎升級 |
| **highlight.js** | 9.18.5 | 11.11.1 | ⚠️ 有 | 升級並測試 |
| **fuse.js** | 6.4.3 | 7.1.0 | ⚠️ 有 | 升級並測試 |

**重要說明**：
- **axios 1.x**：移除了一些舊 API，需檢查攔截器用法
- **echarts 6.x**：API 有變更，需檢查圖表配置
- **highlight.js 11.x**：語法顯亮規則更新
- **fuse.js 7.x**：搜尋演算法優化

---

## 🟢 可安全升級的依賴

### 1. 工具類庫

| 套件 | 當前版本 | 目標版本 | 備註 |
|------|----------|----------|------|
| **core-js** | 3.37.1 | 3.47.0 | polyfill 更新 |
| **js-cookie** | 3.0.1 | 3.0.5 | 小版本更新 |
| **jsencrypt** | 3.0.0-rc.1 | 3.5.4 | 加密庫更新 |
| **sortablejs** | 1.10.2 | 1.15.6 | 拖拽庫更新 |
| **clipboard** | 2.0.8 | 2.0.11 | 剪貼簿工具 |
| **js-beautify** | 1.13.0 | 1.15.4 | 程式碼格式化 |
| **screenfull** | 5.0.2 | 6.0.2 | 全螢幕 API |
| **connect** | 3.6.6 | 3.7.0 | 中介軟體 |

### 2. 建構相關

| 套件 | 當前版本 | 目標版本 | 備註 |
|------|----------|----------|------|
| **sass** | 1.32.13 | 1.94.2 | CSS 預處理器 |
| **quill** | 2.0.2 | 2.0.3 | 豐富文字編輯器 |
| **chalk** | 4.1.0 | 5.6.2 | 終端機顏色（ESM） |

### 3. UI 元件庫

| 套件 | 當前版本 | 目標版本 | 備註 |
|------|----------|----------|------|
| **splitpanes** | 2.4.1 | 4.0.4 | 分割面板 |

**注意**：
- **chalk 5.x** 已改為 ESM，在 Vite 中使用需調整
- **splitpanes 4.x** 可能有 API 變更，需測試

---

## 📋 升級執行計劃

### Phase 2.1: 移除舊依賴

```bash
npm uninstall \
  vue@2 \
  vue-template-compiler \
  vue-router@3 \
  vuex@3 \
  element-ui \
  @vue/cli-plugin-babel \
  @vue/cli-service \
  sass-loader \
  compression-webpack-plugin \
  svg-sprite-loader \
  vue-clipboard2 \
  @riophae/vue-treeselect \
  vuedraggable
```

### Phase 2.2: 安裝 Vue 3 核心

```bash
npm install \
  vue@^3.5.24 \
  vue-router@^4.6.3 \
  pinia@^2.1.0
```

### Phase 2.3: 安裝 Element Plus

```bash
npm install \
  element-plus@^2.5.0 \
  @element-plus/icons-vue@^2.3.0
```

### Phase 2.4: 安裝 Vite

```bash
npm install -D \
  vite@^5.0.0 \
  @vitejs/plugin-vue@^5.0.0 \
  @vitejs/plugin-vue-jsx@^4.0.0 \
  vite-plugin-svg-icons@^2.0.0 \
  vite-plugin-compression@^0.5.0
```

### Phase 2.5: 升級 Vue 3 兼容插件

```bash
npm install \
  vue-clipboard3@^2.0.0 \
  @riophae/vue-treeselect@next \
  vuedraggable@next
```

### Phase 2.6: 升級其他依賴

```bash
npm install \
  axios@^1.13.2 \
  core-js@^3.47.0 \
  sass@^1.94.2 \
  js-cookie@^3.0.5 \
  jsencrypt@^3.5.4 \
  sortablejs@^1.15.6 \
  clipboard@^2.0.11 \
  screenfull@^6.0.2 \
  quill@^2.0.3 \
  js-beautify@^1.15.4 \
  connect@^3.7.0
```

### Phase 2.7: 謹慎升級（需測試）

```bash
# 先不升級，等核心遷移完成後再評估
# echarts@^6.0.0
# highlight.js@^11.11.1
# fuse.js@^7.1.0
# splitpanes@^4.0.4
# vue-cropper@^0.6.5
```

---

## ⚠️ 風險評估

### 高風險依賴

1. **axios 1.x**
   - 風險：攔截器 API 可能變更
   - 建議：升級後測試所有 HTTP 請求

2. **echarts 6.x**
   - 風險：圖表配置可能不兼容
   - 建議：暫時保持 5.x，等測試階段再升級

3. **vue-cropper / vue-count-to**
   - 風險：可能不支援 Vue 3
   - 建議：先查閱文件，必要時尋找替代品

### 中風險依賴

1. **splitpanes 4.x**
   - 風險：API 可能有破壞性變更
   - 建議：升級後測試分割面板功能

2. **chalk 5.x**
   - 風險：改為 ESM，可能影響建構腳本
   - 建議：Vite 環境下應無問題

---

## 📌 注意事項

1. **不要一次性升級所有依賴**
   - 按階段進行，每個階段測試
   - 核心框架優先，工具庫其次

2. **保留當前 package-lock.json**
   ```bash
   cp package-lock.json package-lock.json.vue2.backup
   ```

3. **建立依賴快照**
   ```bash
   npm list --depth=0 > dependency-snapshot-vue2.txt
   ```

4. **測試重點**
   - HTTP 請求（axios）
   - 圖表顯示（echarts）
   - 剪貼簿功能（clipboard）
   - 拖拽功能（vuedraggable）
   - 檔案上傳（可能受影響）

---

## 🔄 回滾策略

如果升級失敗：

```bash
# 方案 1：恢復 package.json
git checkout package.json package-lock.json
npm install

# 方案 2：從備份恢復
cp package-lock.json.vue2.backup package-lock.json
npm ci

# 方案 3：回到 Vue 2 分支
git checkout main
```

---

**分析完成時間**：2025-11-22 23:45  
**下一步**：建立測試檢查點清單
