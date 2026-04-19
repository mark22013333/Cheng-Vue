# Rich Menu Vue 3 轉換說明

## 已完成

### 1. index.vue ✅
- 已轉換為 Vue 3 Composition API (script setup)
- 移除了 SSE 依賴（不存在的 `@/utils/sse/SseClient`）
- 使用簡單的 `ElLoading` 替代進度對話框
- 所有 Vue 2 語法已更新為 Vue 3

### 2. RichMenuEditor.vue ⚠️ 
- 仍需手動轉換為 Vue 3
- 檔案位置: `/cheng-ui/src/views/line/richMenu/components/RichMenuEditor.vue`

## RichMenuEditor.vue 轉換檢查清單

### 需要修改的 Vue 2 → Vue 3 語法：

1. **Props**
   - ❌ `value` → ✅ `modelValue`
   - ❌ `$emit('input')` → ✅ `$emit('update:modelValue')`

2. **生命週期鉤子**
   - ❌ `mounted()` → ✅ `onMounted()`
   - ❌ `beforeDestroy()` → ✅ `onBeforeUnmount()`

3. **響應式數據**
   - ❌ `data()` → ✅ `ref()` 或 `reactive()`
   - ❌ `this.areas` → ✅ `areas.value`

4. **方法**
   - ❌ `methods: {}` → ✅ 直接定義 `function`

5. **組件選項**
   - ❌ `this.$set()` → ✅ 直接賦值（Vue 3 響應式系統自動追蹤）
   - ❌ `this.$message` → ✅ `ElMessage`

## 快速修復方案

由於 RichMenuEditor.vue 很大（1105行），建議採用以下策略：

### 選項 A：保留 Vue 2 語法（臨時）
1. 在 `RichMenuEditor.vue` 中修改 props:
   ```vue
   <script>
   export default {
     name: 'RichMenuEditor',
     props: {
       modelValue: {  // 改為 modelValue
         type: Array,
         default: () => []
       },
       // ... 其他 props
     },
     // ... 其他選項
   }
   </script>
   ```

2. 修改 emit:
   ```javascript
   emitChange() {
     this.$emit('update:modelValue', this.areas)
     this.$emit('change', this.areas)
   }
   ```

3. 修改 watch:
   ```javascript
   watch: {
     modelValue: {  // 改為 modelValue
       handler(newVal) {
         // ...
       }
     }
   }
   ```

### 選項 B：完全轉換為 Vue 3（推薦）

建立新文件 `RichMenuEditor.vue.vue3`：

```vue
<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
// ... 其他 imports

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  templateType: { type: String, default: null },
  imageSize: { type: String, default: '2500x1686' },
  imageUrl: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change', 'manual-edit'])

// 響應式數據
const areas = ref([])
const selectedAreaIndex = ref(null)
// ... 其他 ref

// 計算屬性
const selectedArea = computed(() => {
  if (selectedAreaIndex.value !== null && areas.value[selectedAreaIndex.value]) {
    return areas.value[selectedAreaIndex.value]
  }
  return null
})

// 監聽
watch(() => props.modelValue, (newVal) => {
  // ...處理邏輯
}, { immediate: true, deep: true })

// 方法
function emitChange() {
  emit('update:modelValue', areas.value)
  emit('change', areas.value)
}

// 生命週期
onMounted(() => {
  // ...
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
})
</script>
```

## 當前錯誤修復

如果看到錯誤：`Failed to resolve import "@/utils/sse/SseClient"`

**解決方案**：
- 這個錯誤已在 index.vue 中修復
- SSE 功能已移除並使用簡單的 Loading 提示替代

## 測試步驟

1. **啟動開發伺服器**
   ```bash
   cd cheng-ui
   npm run dev
   ```

2. **訪問 Rich Menu 管理頁面**
   - 登入系統
   - 進入「行銷管理」→「LINE 管理」→「圖文選單管理」

3. **測試功能**
   - [  ] 頁面能正常載入
   - [  ] 列表顯示正常
   - [  ] 點擊「新增選單」按鈕
   - [  ] 對話框打開，編輯器顯示
   - [  ] 可以選擇版型類型
   - [  ] 可以上傳圖片
   - [  ] 可以編輯區塊

## 如果遇到其他錯誤

查看瀏覽器控制台和終端輸出，常見問題：

1. **圖示錯誤**
   - Vue 2: `icon="el-icon-plus"`
   - Vue 3: `<el-icon><Plus /></el-icon>`

2. **Slot 語法**
   - Vue 2: `slot="footer"`
   - Vue 3: `<template #footer>`

3. **v-model 同步修飾符**
   - Vue 2: `:visible.sync="open"`
   - Vue 3: `v-model="open"`

4. **事件修飾符**
   - Vue 2: `@click.native`
   - Vue 3: 直接 `@click`（不需要 .native）

## 下一步

1. 先測試 index.vue 能否正常顯示
2. 如果 index.vue 正常，再轉換 RichMenuEditor.vue
3. 建議使用選項 A（最小修改）先讓功能跑起來
4. 再逐步轉換為完整的 Vue 3 Composition API

## 備份檔案

- `index.vue.backup` - Vue 2 原始版本
- `RichMenuEditor.vue.backup` - Vue 2 原始版本

如需恢復:
```bash
cp index.vue.backup index.vue
cp RichMenuEditor.vue.backup RichMenuEditor.vue
```
