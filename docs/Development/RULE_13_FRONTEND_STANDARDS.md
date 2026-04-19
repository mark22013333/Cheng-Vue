# Vue 3 前端開發規範

> **技術棧**: Vue 3 + Element Plus + Pinia + Vite + Composition API / Options API  
> **核心原則**: 前後端資料格式一致、狀態值正確轉換、響應式系統正確使用

---

## 📋 目錄

1. [技術棧概覽](#技術棧概覽)
2. [狀態參數處理](#狀態參數處理)
3. [Vue 3 Composition API](#vue-3-composition-api)
4. [Template Ref 規範](#template-ref-規範)
5. [Element Plus 元件使用](#element-plus-元件使用)
6. [Pinia 狀態管理](#pinia-狀態管理)
7. [Composables 使用](#composables-使用)
8. [常見錯誤與解決](#常見錯誤與解決)
9. [開發檢查清單](#開發檢查清單)

---

## 技術棧概覽

### 當前使用版本

| 技術 | 版本 | 說明 |
|------|------|------|
| Vue | 3.5.24 | 使用 Composition API 和 Options API |
| Element Plus | 2.11.8 | UI 元件庫（替代 Element UI） |
| Pinia | 2.3.1 | 狀態管理（替代 Vuex） |
| Vue Router | 4.6.3 | 路由管理 |
| Vite | 5.4.21 | 建置工具（替代 Vue CLI） |

### 專案特色

- ✅ **混合使用 API 風格**：新功能推薦 Composition API，舊功能保留 Options API
- ✅ **Pinia 狀態管理**：模組化設計，類型推斷友善
- ✅ **Composables 複用**：如 `useTableConfig`、`useDict` 等
- ✅ **環境變數**：使用 `import.meta.env` 取代 `process.env`

---

## ⚠️ 狀態參數處理（最重要！）

### 問題來源

前後端對於狀態值的處理方式不同，容易造成混亂：
- **後端 Enum**: 使用 `code` 欄位（"0"/"1" 或 "MAIN"/"SUB"）
- **前端元件**: 
  - `el-radio-group`: 綁定字串 `"0"`/`"1"`
  - `el-switch`: 綁定布林值 `true`/`false`
  - `el-select`: 綁定字串或數字

### 標準處理流程

#### 1. 後端返回 → 前端顯示

後端可能返回多種格式，前端必須統一處理：

```javascript
// ❌ 錯誤：直接使用後端返回值
this.form.status = response.data.status  // 可能是枚舉物件、數字或字串

// ✅ 正確：統一轉換為前端需要的格式
let statusValue = '1'  // 預設值

if (response.data.status) {
  const status = response.data.status
  
  // 情況 1：枚舉物件 {code: "1", description: "啟用"}
  if (typeof status === 'object' && status.code !== undefined) {
    statusValue = String(status.code)
  } 
  // 情況 2：數字 1 或 0
  else if (typeof status === 'number') {
    statusValue = String(status)
  } 
  // 情況 3：字串 "ENABLE" 或 "DISABLE" 或 "1" 或 "0"
  else if (typeof status === 'string') {
    if (status === 'ENABLE' || status === '1') {
      statusValue = '1'
    } else if (status === 'DISABLE' || status === '0') {
      statusValue = '0'
    }
  }
}

this.form.status = statusValue
```

#### 2. 前端提交 → 後端接收

前端必須轉換為後端期望的格式：

```javascript
// ❌ 錯誤：直接提交前端值
axios.post('/api/config', this.form)  // status 是字串 "1"

// ✅ 正確：轉換為後端期望的格式
const submitData = {
  ...this.form,
  // 使用 statusCode 而非 status（對應後端 setStatusCode() 方法）
  statusCode: parseInt(this.form.status),  // 轉為數字 1 或 0
  status: undefined  // 移除原始欄位避免衝突
}

axios.post('/api/config', submitData)
```

---

## Vue 3 Composition API

### 基本語法

Vue 3 支援兩種 API 風格，專案中**混合使用**：

#### 方式 1：`<script setup>` （推薦新功能使用）

```vue
<template>
  <div>{{ count }}</div>
  <button @click="increment">增加</button>
</template>

<script setup>
import { ref } from 'vue'

const count = ref(0)
const increment = () => {
  count.value++
}
</script>
```

**特點**：
- ✅ 更簡潔，無需 `return` 暴露
- ✅ 更好的類型推斷
- ✅ 更好的效能

#### 方式 2：Options API（專案現有頁面仍在使用）

```vue
<template>
  <div>{{ count }}</div>
  <button @click="increment">增加</button>
</template>

<script>
export default {
  data() {
    return {
      count: 0
    }
  },
  methods: {
    increment() {
      this.count++
    }
  }
}
</script>
```

### 響應式 API

#### `ref()` - 基本類型響應式

```javascript
import { ref } from 'vue'

// ✅ 正確：基本類型用 ref
const count = ref(0)
const name = ref('張三')
const isActive = ref(false)

// 讀取和修改都需要 .value
console.log(count.value)  // 0
count.value++             // 1

// ⚠️ 模板中自動解包，不需要 .value
// <div>{{ count }}</div>
```

#### `reactive()` - 物件響應式

```javascript
import { reactive } from 'vue'

// ✅ 正確：複雜物件用 reactive
const form = reactive({
  userName: '',
  password: '',
  rememberMe: false
})

// 直接存取，無需 .value
console.log(form.userName)
form.userName = 'admin'

// ❌ 錯誤：不能直接替換整個物件
form = { ...newData }  // 失去響應式

// ✅ 正確：使用 Object.assign
Object.assign(form, newData)
```

#### 選擇 `ref` 或 `reactive`？

| 場景 | 推薦使用 | 原因 |
|------|---------|------|
| 基本類型 | `ref` | reactive 無法處理基本類型 |
| 表單物件 | `reactive` | 更符合表單結構 |
| 列表資料 | `ref([])` | 方便整個陣列替換 |
| 配置物件 | `reactive` | 通常不會整個替換 |

### 計算屬性

```javascript
import { computed } from 'vue'

// ✅ 唯讀計算屬性
const fullName = computed(() => {
  return firstName.value + ' ' + lastName.value
})

// ✅ 可寫計算屬性
const fullName = computed({
  get() {
    return firstName.value + ' ' + lastName.value
  },
  set(value) {
    [firstName.value, lastName.value] = value.split(' ')
  }
})
```

### 生命週期鉤子

```javascript
import { onMounted, onUnmounted } from 'vue'

// ✅ Composition API 生命週期
onMounted(() => {
  console.log('組件已掛載')
  getList()
})

onUnmounted(() => {
  console.log('組件已卸載')
  // 清理定時器、事件監聽等
})
```

**對照表**：

| Options API | Composition API |
|-------------|-----------------|
| `beforeCreate` | 不需要（setup 替代） |
| `created` | 不需要（setup 替代） |
| `beforeMount` | `onBeforeMount` |
| `mounted` | `onMounted` |
| `beforeUpdate` | `onBeforeUpdate` |
| `updated` | `onUpdated` |
| `beforeUnmount` | `onBeforeUnmount` |
| `unmounted` | `onUnmounted` |

---

## Template Ref 規範 ⚠️ 重要！

### 核心原則

**Template ref 名稱必須與資料變數名稱不同，否則會衝突！**

### ❌ 錯誤範例（常見Bug）

```vue
<template>
  <el-form ref="form" :model="form">
    <!-- 對話框中資料全部空白！ -->
  </el-form>
</template>

<script setup>
import { ref } from 'vue'

const form = ref({})  // ❌ 衝突！ref="form" 會覆蓋這個變數
</script>
```

**結果**：點擊「修改」按鈕時，對話框中所有欄位都是空的。

### ✅ 正確範例

```vue
<template>
  <!-- 使用 formRef 而非 form -->
  <el-form ref="formRef" :model="form">
    <el-form-item label="使用者名稱" prop="userName">
      <el-input v-model="form.userName" />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'

const formRef = ref(null)  // ✅ Template ref
const form = ref({         // ✅ 資料變數
  userName: ''
})

// 調用表單方法
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 提交資料
    }
  })
}
</script>
```

### 命名規範

| Template Ref | 資料變數 | 說明 |
|--------------|---------|------|
| `ref="formRef"` | `form` | 主表單 |
| `ref="queryFormRef"` | `queryParams` | 查詢表單 |
| `ref="tableRef"` | - | 表格組件 |
| `ref="uploadRef"` | - | 上傳組件 |
| `ref="dialogRef"` | - | 對話框組件 |

### Options API 中的 Ref

```vue
<template>
  <el-form ref="form" :model="form">
    <!-- Options API 中不會衝突 -->
  </el-form>
</template>

<script>
export default {
  data() {
    return {
      form: {}  // ✅ 不會衝突，ref 存在 this.$refs.form
    }
  },
  methods: {
    submitForm() {
      this.$refs.form.validate((valid) => {
        // ...
      })
    }
  }
}
</script>
```

**注意**：雖然 Options API 中不會衝突，但轉換為 Composition API 時必須注意！

---

## Element Plus 元件使用規範

### 1. el-radio-group（單選框組）

✅ **正確使用**:
```vue
<template>
  <el-form-item label="是否啟用">
    <el-radio-group v-model="form.status">
      <el-radio label="1">啟用</el-radio>
      <el-radio label="0">停用</el-radio>
    </el-radio-group>
  </el-form-item>
</template>

<script>
export default {
  data() {
    return {
      form: {
        status: '1'  // 字串類型
      }
    }
  }
}
</script>
```

**注意**:
- `label` 必須是字串 `"1"`/`"0"`，不能是數字 `1`/`0`
- `v-model` 綁定的值也必須是字串
- 提交時要轉換為數字：`parseInt(this.form.status)`

### 2. el-switch（開關）

✅ **正確使用**:
```vue
<template>
  <el-form-item label="設為預設">
    <el-switch
      v-model="form.isDefault"
      :active-value="true"
      :inactive-value="false"
    />
  </el-form-item>
</template>

<script>
export default {
  data() {
    return {
      form: {
        isDefault: false  // 布林類型
      }
    }
  },
  methods: {
    loadData(response) {
      // 後端返回 "YES" 或 "NO"
      this.form.isDefault = response.data.isDefault === 'YES'
    },
    submitForm() {
      const submitData = {
        ...this.form,
        // 轉換為數字：true -> 1, false -> 0
        isDefaultCode: this.form.isDefault ? 1 : 0,
        isDefault: undefined
      }
      axios.post('/api/config', submitData)
    }
  }
}
</script>
```

**注意**:
- `el-switch` 綁定布林值 `true`/`false`
- 後端可能返回 `"YES"`/`"NO"` 或 `1`/`0`，需要轉換
- 提交時轉換為後端期望的格式

### 3. el-select（下拉選單）

✅ **正確使用**:
```vue
<template>
  <el-form-item label="頻道類型">
    <el-select v-model="form.channelType" placeholder="請選擇">
      <el-option label="主頻道" value="MAIN" />
      <el-option label="副頻道" value="SUB" />
      <el-option label="測試頻道" value="TEST" />
    </el-select>
  </el-form-item>
</template>

<script>
export default {
  data() {
    return {
      form: {
        channelType: 'SUB'  // 字串類型，對應 Enum 的 code
      }
    }
  },
  methods: {
    submitForm() {
      const submitData = {
        ...this.form,
        // 使用 channelTypeCode 對應後端 setChannelTypeCode()
        channelTypeCode: this.form.channelType,
        channelType: undefined
      }
      axios.post('/api/config', submitData)
    }
  }
}
</script>
```

**注意**:
- `value` 使用 Enum 的 `code` 值（字串）
- 不要使用 Enum 的名稱（如 `"NORMAL"`）

### 4. el-checkbox（多選框）

✅ **正確使用**:
```vue
<template>
  <el-form-item label="角色">
    <el-checkbox-group v-model="form.roleIds">
      <el-checkbox :label="1">管理員</el-checkbox>
      <el-checkbox :label="2">普通用戶</el-checkbox>
    </el-checkbox-group>
  </el-form-item>
</template>

<script>
export default {
  data() {
    return {
      form: {
        roleIds: [1, 2]  // 數字陣列
      }
    }
  }
}
</script>
```

**注意**:
- `:label` 綁定數字時使用 `:label="1"` 而非 `label="1"`
- `v-model` 綁定陣列
- 直接提交陣列即可，後端會自動接收

---

## 常見錯誤與解決方案

### 錯誤 1: el-radio-group 值不匹配

❌ **錯誤**:
```vue
<el-radio-group v-model="form.status">
  <el-radio :label="1">啟用</el-radio>  <!-- 數字 -->
</el-radio-group>

<script>
data() {
  return {
    form: { status: '1' }  // 字串，不匹配！
  }
}
</script>
```

**結果**: 單選框無法選中

✅ **正確**:
```vue
<el-radio-group v-model="form.status">
  <el-radio label="1">啟用</el-radio>  <!-- 字串 -->
</el-radio-group>

<script>
data() {
  return {
    form: { status: '1' }  // 字串，匹配！
  }
}
</script>
```

### 錯誤 2: 後端返回枚舉物件未處理

❌ **錯誤**:
```javascript
// 後端返回：{ status: { code: "1", description: "啟用" } }
this.form.status = response.data.status  // 物件！
```

**結果**: `v-model` 綁定錯誤，元件無法顯示

✅ **正確**:
```javascript
// 提取 code
this.form.status = response.data.status.code  // "1"
```

### 錯誤 3: 提交時欄位名稱衝突

❌ **錯誤**:
```javascript
// 前端 status 是字串 "1"
// 後端期望 statusCode 是數字 1
axios.post('/api/config', this.form)  // 後端收到 status: "1"，解析失敗
```

✅ **正確**:
```javascript
const submitData = {
  ...this.form,
  statusCode: parseInt(this.form.status),  // 新增 statusCode
  status: undefined  // 移除衝突的 status
}
axios.post('/api/config', submitData)
```

### 錯誤 4: Boolean 和字串混用

❌ **錯誤**:
```javascript
// 後端返回 "YES" 或 "NO"
this.form.isDefault = response.data.isDefault  // "YES"，不是布林值

// el-switch 無法正確顯示
<el-switch v-model="form.isDefault" />
```

✅ **正確**:
```javascript
// 轉換為布林值
this.form.isDefault = response.data.isDefault === 'YES'
```

---

## Pinia 狀態管理

### 基本使用

Pinia 是 Vue 3 推薦的狀態管理工具，替代 Vuex。

#### Store 定義（使用 `defineStore`）

```javascript
// src/store/modules/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    name: '',
    roles: [],
    permissions: []
  }),
  
  getters: {
    // 計算屬性
    isAdmin: (state) => state.roles.includes('admin')
  },
  
  actions: {
    // 登入
    login(userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo).then(res => {
          this.token = res.token
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },
    
    // 取得使用者資訊
    getInfo() {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          this.name = res.user.userName
          this.roles = res.roles
          this.permissions = res.permissions
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
})
```

#### 在組件中使用 Store

**Composition API 方式**：

```vue
<script setup>
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// ✅ 讀取 state
console.log(userStore.name)

// ✅ 讀取 getter
console.log(userStore.isAdmin)

// ✅ 調用 action
userStore.login({ username: 'admin', password: '123456' })

// ✅ 修改 state（建議在 action 中修改）
userStore.$patch({
  name: '新名稱'
})
</script>
```

**Options API 方式**：

```vue
<script>
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/store/modules/user'

export default {
  computed: {
    ...mapState(useUserStore, ['name', 'roles'])
  },
  methods: {
    ...mapActions(useUserStore, ['login', 'getInfo'])
  }
}
</script>
```

### Pinia vs Vuex

| 項目 | Vuex | Pinia |
|------|------|-------|
| **mutations** | ✅ 需要 | ❌ 不需要（直接在 actions 中修改） |
| **modules** | ✅ 需要手動註冊 | ✅ 自動模組化 |
| **TypeScript** | ⚠️ 支援較弱 | ✅ 原生支援 |
| **DevTools** | ✅ 支援 | ✅ 支援 |
| **語法** | 較複雜 | 更簡潔 |

---

## Composables 使用

### 什麼是 Composables？

Composables 是可複用的組合式函數，封裝和複用有狀態的邏輯。

### 專案中的 Composables

#### 1. useTableConfig（表格欄位配置）

```javascript
// src/composables/useTableConfig.js
import { ref } from 'vue'
import { getTableConfig, saveTableConfig } from '@/api/system/tableConfig'

export function useTableConfig() {
  async function loadConfig(pageKey, defaultColumns) {
    try {
      const response = await getTableConfig(pageKey)
      if (!response.data) {
        return { ...defaultColumns }
      }
      const savedConfig = JSON.parse(response.data)
      return mergeConfig(defaultColumns, savedConfig)
    } catch (error) {
      console.error('載入表格欄位配置失敗：', error)
      return { ...defaultColumns }
    }
  }

  async function saveConfig(pageKey, columns) {
    await saveTableConfig(pageKey, columns)
  }

  function mergeConfig(defaultColumns, savedConfig) {
    const merged = {}
    for (const key in defaultColumns) {
      if (savedConfig.hasOwnProperty(key)) {
        merged[key] = {
          ...defaultColumns[key],
          visible: savedConfig[key].visible
        }
      } else {
        merged[key] = {
          ...defaultColumns[key],
          visible: true
        }
      }
    }
    return merged
  }

  return { loadConfig, saveConfig, mergeConfig }
}
```

**使用範例**：

```vue
<script setup>
import { reactive, onMounted } from 'vue'
import { useTableConfig } from '@/composables/useTableConfig'

const { loadConfig } = useTableConfig()

// 預設欄位配置
const defaultColumns = {
  userId: { label: '使用者編號', visible: true },
  userName: { label: '使用者名稱', visible: true },
  nickName: { label: '使用者暱稱', visible: false }
}

const columns = reactive({ ...defaultColumns })

onMounted(async () => {
  const savedConfig = await loadConfig('system_user', defaultColumns)
  Object.assign(columns, savedConfig)
  getList()
})
</script>
```

#### 2. useDict（字典資料）

```javascript
// 使用字典資料
import { useDict } from '@/utils/dict'

const { sys_normal_disable, sys_user_sex } = useDict('sys_normal_disable', 'sys_user_sex')
```

### 自訂 Composables 規範

**命名規則**：使用 `use` 前綴

```javascript
// ✅ 正確
export function useTableConfig() { }
export function useFormValidation() { }
export function usePermission() { }

// ❌ 錯誤
export function tableConfig() { }
export function formValidation() { }
```

**返回值**：返回物件或陣列

```javascript
// ✅ 推薦：返回物件
export function useMouse() {
  const x = ref(0)
  const y = ref(0)
  
  return { x, y }
}

// ✅ 也可以：返回陣列（類似 React Hooks）
export function useToggle(initialValue = false) {
  const value = ref(initialValue)
  const toggle = () => { value.value = !value.value }
  
  return [value, toggle]
}
```

---

## 環境變數

### Vite 環境變數規範

**不再使用 `process.env`，改用 `import.meta.env`**

```javascript
// ❌ Vue 2 / Webpack 方式
const baseURL = process.env.VUE_APP_BASE_API

// ✅ Vue 3 / Vite 方式
const baseURL = import.meta.env.VITE_APP_BASE_API
```

### 環境變數文件

- `.env` - 所有環境共用
- `.env.development` - 開發環境
- `.env.production` - 生產環境

**範例**：

```bash
# .env.development
VITE_APP_BASE_API = '/dev-api'
VITE_APP_TITLE = 'Cheng 管理系統'
```

**使用**：

```javascript
console.log(import.meta.env.VITE_APP_BASE_API)  // '/dev-api'
console.log(import.meta.env.MODE)               // 'development'
console.log(import.meta.env.DEV)                // true
console.log(import.meta.env.PROD)               // false
```

---

## 常見錯誤與解決

### 錯誤 1: Template Ref 衝突

```vue
<!-- ❌ 錯誤 -->
<el-form ref="form" :model="form">
<script setup>
const form = ref({})  // 衝突！
</script>

<!-- ✅ 正確 -->
<el-form ref="formRef" :model="form">
<script setup>
const formRef = ref(null)
const form = ref({})
</script>
```

### 錯誤 2: reactive 整個替換

```javascript
// ❌ 錯誤
const form = reactive({ name: '' })
form = { name: '新值' }  // 失去響應式

// ✅ 正確
Object.assign(form, { name: '新值' })
```

### 錯誤 3: 忘記 .value

```javascript
const count = ref(0)

// ❌ 錯誤
console.log(count)      // RefImpl {...}
count++                 // 不會更新

// ✅ 正確
console.log(count.value)  // 0
count.value++            // 1
```

### 錯誤 4: 使用 process.env

```javascript
// ❌ 錯誤（Vite 不支援）
const api = process.env.VUE_APP_BASE_API

// ✅ 正確
const api = import.meta.env.VITE_APP_BASE_API
```

### 錯誤 5: Pinia action 中的 this

```javascript
// ✅ 正確：Pinia action 中可以直接用 this
actions: {
  login(userInfo) {
    this.token = userInfo.token  // ✅ 正確
  }
}

// ❌ 錯誤：使用箭頭函數會失去 this
actions: {
  login: (userInfo) => {
    this.token = userInfo.token  // ❌ this 為 undefined
  }
}
```

---

## 開發檢查清單

### Vue 3 Composition API
- [ ] 新功能優先使用 `<script setup>`
- [ ] 基本類型用 `ref()`，物件用 `reactive()`
- [ ] 記得在 script 中使用 `.value`
- [ ] Template ref 名稱加上 `Ref` 後綴

### Template Ref
- [ ] ref 名稱不與資料變數同名
- [ ] 多個表單使用不同的 ref 名稱
- [ ] 調用方法時使用 `.value`（Composition API）

### Pinia
- [ ] 使用 `defineStore` 定義 store
- [ ] action 中可直接修改 state，不需要 mutation
- [ ] 使用 `useXxxStore()` 取得 store 實例

### Composables
- [ ] 函數名稱使用 `use` 前綴
- [ ] 返回物件或陣列
- [ ] 可複用的邏輯提取為 composable

### 環境變數
- [ ] 使用 `import.meta.env` 而非 `process.env`
- [ ] 環境變數以 `VITE_` 開頭

### Element Plus
- [ ] 元件名稱使用 `el-` 前綴
- [ ] icon 使用 Element Plus Icons
- [ ] 按需引入或全域註冊

---

## 完整範例

### 範例：使用 Composition API 的表單頁面

```vue
<template>
  <div class="app-container">
    <!-- 查詢表單 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true">
      <el-form-item label="使用者名稱" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="請輸入" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="userList">
      <el-table-column label="使用者編號" prop="userId" />
      <el-table-column label="使用者名稱" prop="userName" />
      <el-table-column label="狀態" prop="status">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleUpdate(row)">修改</el-button>
          <el-button link type="danger" @click="handleDelete(row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/修改對話框 -->
    <el-dialog v-model="open" :title="title" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="使用者名稱" prop="userName">
          <el-input v-model="form.userName" placeholder="請輸入" />
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="submitForm">確定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUser, getUser, addUser, updateUser, delUser } from '@/api/system/user'

// ========== 響應式資料 ==========
const loading = ref(false)
const open = ref(false)
const title = ref('')
const userList = ref([])

const queryFormRef = ref(null)
const formRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: ''
})

const form = ref({
  userId: null,
  userName: '',
  status: '0'
})

const rules = {
  userName: [
    { required: true, message: '使用者名稱不能為空', trigger: 'blur' }
  ]
}

// ========== 查詢列表 ==========
const getList = async () => {
  loading.value = true
  try {
    const response = await listUser(queryParams)
    userList.value = response.rows
  } finally {
    loading.value = false
  }
}

// ========== 搜尋 ==========
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// ========== 重置 ==========
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// ========== 新增 ==========
const handleAdd = () => {
  reset()
  open.value = true
  title.value = '新增使用者'
}

// ========== 修改 ==========
const handleUpdate = async (row) => {
  reset()
  const response = await getUser(row.userId)
  form.value = response.data
  open.value = true
  title.value = '修改使用者'
}

// ========== 刪除 ==========
const handleDelete = (row) => {
  ElMessageBox.confirm(`是否確認刪除使用者「${row.userName}」？`, '警告', {
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await delUser(row.userId)
    ElMessage.success('刪除成功')
    getList()
  }).catch(() => {})
}

// ========== 提交表單 ==========
const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      if (form.value.userId) {
        await updateUser(form.value)
        ElMessage.success('修改成功')
      } else {
        await addUser(form.value)
        ElMessage.success('新增成功')
      }
      open.value = false
      getList()
    }
  })
}

// ========== 取消 ==========
const cancel = () => {
  open.value = false
  reset()
}

// ========== 重置表單 ==========
const reset = () => {
  form.value = {
    userId: null,
    userName: '',
    status: '0'
  }
  formRef.value?.resetFields()
}

// ========== 生命週期 ==========
onMounted(() => {
  getList()
})
</script>
```

---

## 相關文件

- [RULE_02_ENUM_STANDARDS.md](./RULE_02_ENUM_STANDARDS.md) - 後端 Enum 規範
- [Vue 3 官方文件](https://vuejs.org/)
- [Element Plus 官方文件](https://element-plus.org/)
- [Pinia 官方文件](https://pinia.vuejs.org/)
- [Vite 官方文件](https://vitejs.dev/)

---

**文件更新時間**: 2024-12-07  
**適用版本**: Vue 3.5.24 + Element Plus 2.11.8 + Pinia 2.3.1 + Vite 5.4.21  
**狀態**: ✅ Vue 3 遷移完成
