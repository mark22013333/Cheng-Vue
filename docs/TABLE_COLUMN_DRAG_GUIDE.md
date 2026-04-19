# 表格欄位拖曳排序功能開發指南

> 本指南說明如何在列表頁面實現「表格欄位可拖曳調整順序並持久化儲存」功能。

---

## 功能說明

- 使用者可在右上角「選單」按鈕中拖曳調整欄位顯示順序
- 欄位順序自動儲存到資料庫（2 秒防抖）
- 重新整理頁面後順序自動還原
- 支援欄位顯示/隱藏功能

---

## 前置條件

確保以下元件已存在：
- `@/components/RightToolbar/index.vue` - 已支援拖曳排序
- `@/api/system/tableConfig.js` - 提供 `getTableConfig`、`saveTableConfig` API
- `@/composables/useTableConfig.js` - 提供 `loadConfig`、`saveConfig` 方法

---

## 實作步驟

### 步驟 1：定義欄位配置（data）

```javascript
data() {
  return {
    // 預設欄位配置
    defaultColumns: {
      fieldA: { label: '欄位A', visible: true, order: 0 },
      fieldB: { label: '欄位B', visible: true, order: 1 },
      fieldC: { label: '欄位C', visible: true, order: 2 },
      // ... 依序定義所有欄位
    },
    // 實際使用的欄位配置（會被資料庫配置覆蓋）
    columns: {
      fieldA: { label: '欄位A', visible: true, order: 0 },
      fieldB: { label: '欄位B', visible: true, order: 1 },
      fieldC: { label: '欄位C', visible: true, order: 2 },
      // ... 與 defaultColumns 相同
    },
    // ... 其他 data
  }
}
```

**重點**：
- 每個欄位必須有 `label`、`visible`、`order` 三個屬性
- `order` 從 0 開始遞增

---

### 步驟 2：新增計算屬性（computed）

```javascript
computed: {
  // 按 order 排序後的欄位 key 陣列
  sortedColumnKeys() {
    return Object.entries(this.columns)
      .sort((a, b) => (a[1].order ?? 999) - (b[1].order ?? 999))
      .map(([key]) => key)
  }
}
```

---

### 步驟 3：引入 API 並載入配置

```javascript
import { getTableConfig, saveTableConfig } from "@/api/system/tableConfig"

// 在 created 或 mounted 中載入配置
async created() {
  await this.loadTableConfig();
  this.getList();
},

methods: {
  /** 載入表格欄位配置 */
  async loadTableConfig() {
    try {
      const response = await getTableConfig('your_page_key');
      if (response.code === 200 && response.data) {
        const savedConfig = JSON.parse(response.data);
        // 合併儲存的配置與預設配置
        const merged = {};
        for (const key in this.defaultColumns) {
          if (savedConfig.hasOwnProperty(key)) {
            merged[key] = {
              label: this.defaultColumns[key].label,
              visible: savedConfig[key].visible,
              order: savedConfig[key].order ?? this.defaultColumns[key].order
            };
          } else {
            merged[key] = { ...this.defaultColumns[key] };
          }
        }
        this.columns = merged;
      }
    } catch (error) {
      console.error('載入表格配置失敗：', error);
    }
  },
}
```

**重點**：
- `your_page_key` 替換為頁面唯一標識，例如 `inventory_management`、`system_user`

---

### 步驟 4：改造 Template

```vue
<template>
  <!-- RightToolbar 必須傳入 columns 和 pageKey -->
  <right-toolbar 
    v-model:showSearch="showSearch" 
    @queryTable="getList" 
    :columns="columns" 
    pageKey="your_page_key"
  />

  <!-- 表格 -->
  <el-table :data="dataList">
    <!-- 固定欄位：勾選框（如需要） -->
    <el-table-column type="selection" width="55" align="center"/>
    
    <!-- 動態欄位（按 order 排序） -->
    <template v-for="key in sortedColumnKeys" :key="key">
      <!-- 簡單欄位範例 -->
      <el-table-column 
        v-if="key === 'fieldA' && columns.fieldA.visible" 
        label="欄位A" 
        prop="fieldA" 
      />
      
      <!-- 自定義模板範例 -->
      <el-table-column 
        v-if="key === 'fieldB' && columns.fieldB.visible" 
        label="欄位B"
      >
        <template #default="scope">
          <el-tag>{{ scope.row.fieldB }}</el-tag>
        </template>
      </el-table-column>
      
      <!-- 條件顯示欄位範例 -->
      <el-table-column 
        v-if="key === 'fieldC' && columns.fieldC.visible && someCondition" 
        label="欄位C" 
        prop="fieldC" 
      />
    </template>
    
    <!-- 固定欄位：操作（如需要） -->
    <el-table-column label="操作" fixed="right" width="180">
      <template #default="scope">
        <!-- 操作按鈕 -->
      </template>
    </el-table-column>
  </el-table>
</template>
```

**重點**：
- 使用 `<template v-for="key in sortedColumnKeys">` 包裹所有動態欄位
- 每個 `el-table-column` 的 `v-if` 需同時判斷 `key === 'xxx'` 和 `columns.xxx.visible`
- 固定欄位（selection、操作）放在 `v-for` 外面

---

## 完整範例

以下是一個最小化的完整範例：

```vue
<template>
  <div class="app-container">
    <!-- 工具列 -->
    <el-row :gutter="10" class="mb8">
      <right-toolbar 
        v-model:showSearch="showSearch" 
        @queryTable="getList" 
        :columns="columns" 
        pageKey="example_list"
      />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="dataList">
      <el-table-column type="selection" width="55" align="center"/>
      
      <template v-for="key in sortedColumnKeys" :key="key">
        <el-table-column v-if="key === 'name' && columns.name.visible" 
          label="名稱" prop="name"/>
        <el-table-column v-if="key === 'status' && columns.status.visible" 
          label="狀態" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
              {{ scope.row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="key === 'createTime' && columns.createTime.visible" 
          label="建立時間" prop="createTime" width="180"/>
      </template>
      
      <el-table-column label="操作" fixed="right" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">編輯</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getTableConfig } from "@/api/system/tableConfig"

export default {
  name: "ExampleList",
  data() {
    return {
      loading: false,
      showSearch: true,
      dataList: [],
      defaultColumns: {
        name: { label: '名稱', visible: true, order: 0 },
        status: { label: '狀態', visible: true, order: 1 },
        createTime: { label: '建立時間', visible: true, order: 2 }
      },
      columns: {
        name: { label: '名稱', visible: true, order: 0 },
        status: { label: '狀態', visible: true, order: 1 },
        createTime: { label: '建立時間', visible: true, order: 2 }
      }
    }
  },
  computed: {
    sortedColumnKeys() {
      return Object.entries(this.columns)
        .sort((a, b) => (a[1].order ?? 999) - (b[1].order ?? 999))
        .map(([key]) => key)
    }
  },
  async created() {
    await this.loadTableConfig();
    this.getList();
  },
  methods: {
    async loadTableConfig() {
      try {
        const response = await getTableConfig('example_list');
        if (response.code === 200 && response.data) {
          const savedConfig = JSON.parse(response.data);
          const merged = {};
          for (const key in this.defaultColumns) {
            if (savedConfig.hasOwnProperty(key)) {
              merged[key] = {
                label: this.defaultColumns[key].label,
                visible: savedConfig[key].visible,
                order: savedConfig[key].order ?? this.defaultColumns[key].order
              };
            } else {
              merged[key] = { ...this.defaultColumns[key] };
            }
          }
          this.columns = merged;
        }
      } catch (error) {
        console.error('載入表格配置失敗：', error);
      }
    },
    getList() {
      // 載入資料
    },
    handleEdit(row) { },
    handleDelete(row) { }
  }
}
</script>
```

---

## pageKey 命名規範

| 格式 | 範例 |
|------|------|
| `模組_功能` | `inventory_management` |
| | `inventory_borrow` |
| | `system_user` |
| | `system_role` |

---

## 檢查清單

實作前請確認：

- [ ] 定義 `defaultColumns` 和 `columns`，每個欄位包含 `label`、`visible`、`order`
- [ ] 新增 `sortedColumnKeys` 計算屬性
- [ ] 引入 `getTableConfig` 並實作 `loadTableConfig` 方法
- [ ] 在 `created` 中呼叫 `loadTableConfig`
- [ ] `RightToolbar` 傳入 `columns` 和 `pageKey`
- [ ] 表格使用 `v-for="key in sortedColumnKeys"` 動態渲染欄位
- [ ] 每個欄位的 `v-if` 同時判斷 `key` 和 `visible`
- [ ] 固定欄位（selection、操作）放在 `v-for` 外面

---

## 常見問題

### Q: 欄位順序沒有變化？
確認 `v-for` 是否正確包裹在 `<template>` 中，且使用 `sortedColumnKeys`。

### Q: 顯示/隱藏沒有效果？
確認 checkbox 綁定的是原始 `columns` 物件，而非複製的物件。

### Q: 新增欄位後舊配置出錯？
`loadTableConfig` 中的合併邏輯會自動處理：新欄位使用預設值，舊欄位使用儲存的配置。

---

## 相關檔案

- `@/components/RightToolbar/index.vue` - 欄位管理組件
- `@/api/system/tableConfig.js` - API 定義
- `@/composables/useTableConfig.js` - Composable（可選）
- `@/views/inventory/management/index.vue` - 參考實作
