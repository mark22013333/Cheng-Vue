# 表格欄位配置功能使用指南

## 功能說明

此功能允許每個登入使用者儲存自己在不同頁面的表格欄位顯示偏好，並在下次訪問時自動載入。

## 特性

✅ **自動儲存**：修改欄位顯示/隱藏後，2 秒後自動儲存  
✅ **自動載入**：頁面載入時自動從資料庫取得配置  
✅ **獨立配置**：每個使用者、每個頁面都有獨立配置  
✅ **向後相容**：未儲存配置時使用頁面預設配置  
✅ **智慧合併**：新增欄位自動顯示，刪除欄位自動忽略  

---

## 使用方式

### 1. 在頁面中使用（三步驟）

#### 步驟 1：定義預設欄位配置

```javascript
import { reactive } from 'vue'

const defaultColumns = {
  userId: { label: '使用者編號', visible: true },
  userName: { label: '使用者名稱', visible: true },
  nickName: { label: '使用者暱稱', visible: true },
  deptName: { label: '部門', visible: false },  // 預設隱藏
  phonenumber: { label: '手機號碼', visible: true },
  status: { label: '狀態', visible: true },
  createTime: { label: '建立時間', visible: true }
}

const columns = reactive({ ...defaultColumns })
```

#### 步驟 2：在 mounted 時載入配置

```javascript
import { onMounted } from 'vue'
import { useTableConfig } from '@/composables/useTableConfig'

const { loadConfig } = useTableConfig()

onMounted(async () => {
  // 載入使用者儲存的欄位配置
  const savedConfig = await loadConfig('system_user', defaultColumns)
  
  // 合併到 columns（保持響應式）
  Object.assign(columns, savedConfig)
  
  // 載入資料
  getList()
})
```

#### 步驟 3：在 RightToolbar 加入 pageKey

```vue
<right-toolbar 
  v-model:showSearch="showSearch" 
  @queryTable="getList"
  :columns="columns"
  pageKey="system_user"
/>
```

**重要**：`pageKey` 必須唯一，建議使用 `模組_功能` 格式。

---

### 2. 在表格欄位中使用 columns

```vue
<el-table-column 
  v-if="columns.userId.visible" 
  key="userId" 
  label="使用者編號" 
  prop="userId"
/>

<el-table-column 
  v-if="columns.userName.visible" 
  key="userName" 
  label="使用者名稱" 
  prop="userName"
/>

<!-- 其他欄位... -->
```

---

## 完整範例

### 範例 1：使用者管理頁面

```vue
<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form v-show="showSearch">
      <!-- ... -->
    </el-form>

    <!-- 工具列 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </el-col>
      <right-toolbar 
        v-model:showSearch="showSearch" 
        @queryTable="getList"
        :columns="columns"
        pageKey="system_user"
      />
    </el-row>

    <!-- 表格 -->
    <el-table :data="userList">
      <el-table-column type="selection" width="50" />
      <el-table-column v-if="columns.userId.visible" label="使用者編號" prop="userId" />
      <el-table-column v-if="columns.userName.visible" label="使用者名稱" prop="userName" />
      <el-table-column v-if="columns.nickName.visible" label="使用者暱稱" prop="nickName" />
      <!-- 其他欄位... -->
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useTableConfig } from '@/composables/useTableConfig'
import { listUser } from '@/api/system/user'

// 預設欄位配置
const defaultColumns = {
  userId: { label: '使用者編號', visible: true },
  userName: { label: '使用者名稱', visible: true },
  nickName: { label: '使用者暱稱', visible: true },
  deptName: { label: '部門', visible: true },
  phonenumber: { label: '手機號碼', visible: true },
  status: { label: '狀態', visible: true },
  createTime: { label: '建立時間', visible: true }
}

// 響應式欄位配置
const columns = reactive({ ...defaultColumns })

// 其他狀態
const showSearch = ref(true)
const userList = ref([])

// 載入配置
const { loadConfig } = useTableConfig()

onMounted(async () => {
  // 載入使用者儲存的欄位配置
  const savedConfig = await loadConfig('system_user', defaultColumns)
  Object.assign(columns, savedConfig)
  
  // 載入資料
  getList()
})

function getList() {
  // 查詢使用者列表
  listUser().then(response => {
    userList.value = response.rows
  })
}
</script>
```

---

### 範例 2：借出管理頁面

```vue
<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <right-toolbar 
        v-model:showSearch="showSearch" 
        @queryTable="getList"
        :columns="columns"
        pageKey="inventory_borrow"
      />
    </el-row>

    <el-table :data="borrowList">
      <el-table-column v-if="columns.borrowNo.visible" label="借出單號" prop="borrowNo" />
      <el-table-column v-if="columns.itemName.visible" label="物品名稱" prop="itemName" />
      <el-table-column v-if="columns.borrowerName.visible" label="借用人" prop="borrowerName" />
      <el-table-column v-if="columns.quantity.visible" label="數量" prop="quantity" />
      <!-- 其他欄位... -->
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useTableConfig } from '@/composables/useTableConfig'

const defaultColumns = {
  borrowNo: { label: '借出單號', visible: true },
  itemName: { label: '物品名稱', visible: true },
  borrowerName: { label: '借用人', visible: true },
  quantity: { label: '數量', visible: true },
  status: { label: '狀態', visible: true },
  borrowTime: { label: '借出時間', visible: true }
}

const columns = reactive({ ...defaultColumns })
const { loadConfig } = useTableConfig()

onMounted(async () => {
  const savedConfig = await loadConfig('inventory_borrow', defaultColumns)
  Object.assign(columns, savedConfig)
  getList()
})
</script>
```

---

## pageKey 命名規範

**格式：`模組_功能`**

| 頁面 | pageKey | 說明 |
|------|---------|------|
| 系統管理 > 使用者管理 | `system_user` | 使用者列表 |
| 系統管理 > 角色管理 | `system_role` | 角色列表 |
| 系統管理 > 選單管理 | `system_menu` | 選單列表 |
| 庫存管理 > 借出管理 | `inventory_borrow` | 借出記錄 |
| 庫存管理 > 物品管理 | `inventory_management` | 物品列表 |
| 行銷管理 > LINE 使用者 | `line_user` | LINE 使用者列表 |

---

## 進階功能

### 1. 停用自動儲存

```vue
<right-toolbar 
  :columns="columns"
  pageKey="system_user"
  :autoSave="false"
/>
```

### 2. 手動儲存

```javascript
import { useTableConfig } from '@/composables/useTableConfig'

const { saveConfig } = useTableConfig()

async function handleManualSave() {
  try {
    await saveConfig('system_user', columns)
    ElMessage.success('配置已儲存')
  } catch (error) {
    ElMessage.error('儲存失敗')
  }
}
```

### 3. 重置為預設配置

```javascript
function resetColumns() {
  Object.assign(columns, defaultColumns)
  // 自動儲存會在 2 秒後執行
}
```

---

## Vue Options API 實施方式

對於使用 Options API 的頁面（如 `system/user/index.vue`），需要手動實現 `loadTableConfig` 方法：

```javascript
export default {
  data() {
    return {
      // 預設列訊息
      defaultColumns: {
        userId: {label: '使用者編號', visible: true},
        userName: {label: '使用者名稱', visible: true},
        nickName: {label: '使用者暱稱', visible: true}
      },
      // 列訊息
      columns: {
        userId: {label: '使用者編號', visible: true},
        userName: {label: '使用者名稱', visible: true},
        nickName: {label: '使用者暱稱', visible: true}
      }
    }
  },
  async created() {
    await this.loadTableConfig()
    this.getList()
  },
  methods: {
    /** 載入表格欄位配置 */
    async loadTableConfig() {
      try {
        const response = await getTableConfig('system_user')
        if (response.data) {
          const savedConfig = JSON.parse(response.data)
          const merged = {}
          
          // 合併配置：優先使用儲存的配置，但包含新增的欄位
          for (const key in this.defaultColumns) {
            if (savedConfig.hasOwnProperty(key)) {
              merged[key] = {
                label: this.defaultColumns[key].label,
                visible: savedConfig[key].visible
              }
            } else {
              merged[key] = { ...this.defaultColumns[key] }
            }
          }
          
          // ⚠️ 重要：必須使用 Object.assign 來觸發 Vue 響應式更新
          Object.assign(this.columns, merged)
        }
      } catch (error) {
        console.error('載入表格欄位配置失敗：', error)
      }
    }
  }
}
```

**⚠️ 重要提醒**：
- **不能直接修改 `this.columns[key].visible`**，這樣不會觸發 Vue 的響應式更新
- **必須使用 `Object.assign(this.columns, merged)`** 來整體替換物件，確保響應式系統能追蹤變更
- 在 `created` 鉤子中必須使用 `async/await` 確保配置載入完成後才執行 `getList()`

---

## 注意事項

1. **pageKey 必須唯一**：不同頁面不能使用相同的 pageKey
2. **defaultColumns 必須定義**：作為首次訪問或儲存失敗時的備用配置
3. **使用 reactive**：`columns` 必須是響應式物件，才能觸發自動儲存
4. **防抖機制**：修改後 2 秒才儲存，避免頻繁請求
5. **向後相容**：未啟用此功能的頁面不受影響
6. **⚠️ 響應式更新**：Options API 必須使用 `Object.assign` 更新整個物件，不能直接修改深層屬性

---

## 故障排除

### 問題 1：配置未儲存

**檢查**：
- 是否設定了 `pageKey`？
- `columns` 是否為響應式物件（使用 `reactive`）？
- 瀏覽器 Console 是否有錯誤訊息？

### 問題 2：配置未載入（重新整理後欄位又全部顯示）

**可能原因 1**：直接修改 `this.columns[key].visible` 不會觸發 Vue 響應式更新。

**可能原因 2**：後端 API 返回格式錯誤，數據在 `msg` 而不是 `data` 欄位。

**解決方案**：
```javascript
// ❌ 錯誤做法
for (const key in this.defaultColumns) {
  this.columns[key].visible = savedConfig[key].visible  // 不會觸發響應式
}

// ✅ 正確做法
const merged = {}
for (const key in this.defaultColumns) {
  merged[key] = {
    label: this.defaultColumns[key].label,
    visible: savedConfig[key].visible
  }
}
Object.assign(this.columns, merged)  // 觸發響應式更新
```

**後端修正**（如果是原因 2）：
```java
// ❌ 錯誤做法
@GetMapping("/{pageKey}")
public AjaxResult getConfig(@PathVariable String pageKey) {
    String columnConfig = service.getColumnConfig(pageKey);
    return success(columnConfig);  // 數據會在 msg 欄位
}

// ✅ 正確做法
@GetMapping("/{pageKey}")
public AjaxResult getConfig(@PathVariable String pageKey) {
    String columnConfig = service.getColumnConfig(pageKey);
    return AjaxResult.success().put("data", columnConfig);  // 數據在 data 欄位
}
```

**檢查清單**：
- [ ] `onMounted` 或 `created` 中是否呼叫了 `loadConfig`？
- [ ] 是否使用 `Object.assign(columns, merged)` 更新？
- [ ] 是否在載入配置後才呼叫 `getList()`？
- [ ] 後端 API 是否正確返回 `{ data: "..." }` 格式？

### 問題 3：新增欄位未顯示

**原因**：新增欄位不在儲存的配置中。

**解決**：`mergeConfig` 會自動處理，新增欄位預設顯示（`visible: true`）。

---

## API 文件

### useTableConfig()

```javascript
const { loadConfig, saveConfig, mergeConfig } = useTableConfig()
```

#### loadConfig(pageKey, defaultColumns)

載入使用者儲存的配置，並與預設配置合併。

- **參數**：
  - `pageKey` (string)：頁面標識
  - `defaultColumns` (object)：預設欄位配置
- **返回**：Promise<object> - 合併後的配置

#### saveConfig(pageKey, columns)

儲存欄位配置到資料庫。

- **參數**：
  - `pageKey` (string)：頁面標識
  - `columns` (object)：欄位配置
- **返回**：Promise<void>

#### mergeConfig(defaultColumns, savedConfig)

合併預設配置和儲存的配置。

- **參數**：
  - `defaultColumns` (object)：預設配置
  - `savedConfig` (object)：儲存的配置
- **返回**：object - 合併後的配置

---

## 總結

使用此功能只需三步驟：
1. 定義 `defaultColumns` 和 `columns`
2. 在 `onMounted` 呼叫 `loadConfig`
3. 在 `<right-toolbar>` 加入 `pageKey`

所有儲存和載入都自動完成，無需手動處理！
