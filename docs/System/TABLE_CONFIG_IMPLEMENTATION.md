# 表格欄位配置功能實施總結

## ✅ 已完成頁面（3/22）

| 頁面 | pageKey | 狀態 | 備註 |
|------|---------|------|------|
| 系統管理 > 使用者管理 | `system_user` | ✅ 完成 | |
| 庫存管理 > 借出管理 | `inventory_borrow` | ✅ 完成 | |
| 庫存管理 > 物品與庫存管理 | `inventory_management` | ✅ 完成 | |

## 📋 待實施頁面（19/22）

### 系統管理（8個）
- [ ] `system/config/index.vue` - `system_config` - 參數設定
- [ ] `system/dept/index.vue` - `system_dept` - 部門管理
- [ ] `system/dict/data.vue` - `system_dict_data` - 字典資料
- [ ] `system/dict/index.vue` - `system_dict` - 字典類型
- [ ] `system/menu/index.vue` - `system_menu` - 選單管理
- [ ] `system/notice/index.vue` - `system_notice` - 通知公告
- [ ] `system/post/index.vue` - `system_post` - 崗位管理
- [ ] `system/role/index.vue` - `system_role` - 角色管理
- [ ] `system/role/authUser.vue` - `system_role_user` - 角色授權使用者

### 庫存管理（2個）
- [ ] `inventory/item/index.vue` - `inventory_item` - 物品管理
- [ ] `inventory/stock/index.vue` - `inventory_stock` - 庫存管理

### LINE 管理（3個）
- [ ] `line/richMenu/index.vue` - `line_rich_menu` - 圖文選單
- [ ] `line/richMenuAlias/index.vue` - `line_rich_menu_alias` - 選單別名
- [ ] `line/user/index.vue` - `line_user` - LINE 使用者

### 監控管理（4個）
- [ ] `monitor/job/index.vue` - `monitor_job` - 定時任務
- [ ] `monitor/job/log.vue` - `monitor_job_log` - 任務日誌
- [ ] `monitor/logininfor/index.vue` - `monitor_login_log` - 登入日誌
- [ ] `monitor/operlog/index.vue` - `monitor_oper_log` - 操作日誌

### 工具（1個）
- [ ] `tool/gen/index.vue` - `tool_gen` - 程式碼產生

### 其他（1個）
- [ ] `inventory/category/index.vue` - `inventory_category` - 分類管理

---

## 🚀 快速實施模板

### Options API（適用於大部分頁面）

#### 步驟 1：Import API
```javascript
import {getTableConfig, saveTableConfig} from "@/api/system/tableConfig"
```

#### 步驟 2：定義 data
```javascript
data() {
  return {
    // 預設列訊息
    defaultColumns: {
      columnKey1: {label: '欄位1', visible: true},
      columnKey2: {label: '欄位2', visible: true},
      columnKey3: {label: '欄位3', visible: false}
    },
    // 列訊息
    columns: {
      columnKey1: {label: '欄位1', visible: true},
      columnKey2: {label: '欄位2', visible: true},
      columnKey3: {label: '欄位3', visible: false}
    }
  }
}
```

#### 步驟 3：修改 created 為 async
```javascript
async created() {
  await this.loadTableConfig()
  this.getList()
  // 其他初始化...
}
```

#### 步驟 4：加入 loadTableConfig 方法
```javascript
methods: {
  /** 載入表格欄位配置 */
  async loadTableConfig() {
    try {
      const response = await getTableConfig('pageKey_name')
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
        
        // 使用 Object.assign 來觸發響應式更新
        Object.assign(this.columns, merged)
      }
    } catch (error) {
      console.error('載入表格欄位配置失敗：', error)
    }
  },
  // 其他方法...
}
```

#### 步驟 5：更新 right-toolbar
```vue
<right-toolbar 
  v-model:showSearch="showSearch" 
  @queryTable="getList"
  :columns="columns"
  pageKey="pageKey_name"
/>
```

#### 步驟 6：為表格欄位加入 v-if
```vue
<el-table-column v-if="columns.columnKey1.visible" label="欄位1" prop="prop1" />
<el-table-column v-if="columns.columnKey2.visible" label="欄位2" prop="prop2" />
<el-table-column v-if="columns.columnKey3.visible" label="欄位3" prop="prop3" />
```

---

## 🔧 注意事項

### 1. 欄位命名規範
- 使用駝峰式命名：`userName`, `createTime`, `status`
- 與表格 `prop` 屬性保持一致
- 避免使用保留字：`data`, `value`, `name` 等

### 2. pageKey 命名規範
- 格式：`模組_功能`
- 全小寫，使用底線分隔
- 範例：`system_user`, `inventory_borrow`, `monitor_job_log`

### 3. 特殊欄位處理
- 操作欄位：不加入 columns（永遠顯示）
- 選擇框：不加入 columns（永遠顯示）
- 展開按鈕：不加入 columns（永遠顯示）

### 4. 響應式更新
- ⚠️ **必須使用 `Object.assign(this.columns, merged)`**
- ❌ **不能直接修改 `this.columns[key].visible`**

### 5. 測試檢查清單
- [ ] 首次訪問使用預設配置
- [ ] 修改後 2 秒自動儲存
- [ ] 重新整理頁面配置保留
- [ ] 隱藏欄位不顯示
- [ ] 顯示欄位正常顯示

---

## 📝 實施記錄模板

### [頁面名稱] - [pageKey]

**實施日期**：YYYY-MM-DD  
**實施人員**：  
**檔案路徑**：`cheng-ui/src/views/xxx/xxx/index.vue`

**欄位配置**：
```javascript
{
  field1: {label: '欄位1', visible: true},
  field2: {label: '欄位2', visible: true},
  field3: {label: '欄位3', visible: false}
}
```

**測試狀態**：
- [ ] 儲存功能正常
- [ ] 載入功能正常
- [ ] 重新整理保留配置
- [ ] 欄位顯示/隱藏正常

**備註**：

---

## 🐛 常見問題排查

### 問題 1：配置無法儲存
**檢查**：
- 是否設定了 `pageKey`？
- Console 是否有錯誤訊息？
- 後端 API 是否正常？

### 問題 2：配置無法載入（重新整理後欄位全部顯示）
**原因**：後端 API 返回格式錯誤或前端解析錯誤

**解決**：
1. 檢查後端 Controller 是否使用 `AjaxResult.success().put("data", columnConfig)`
2. 檢查前端是否使用 `Object.assign(this.columns, merged)`

### 問題 3：新增欄位後無法顯示
**原因**：`defaultColumns` 和 `columns` 不同步

**解決**：
1. 確保 `columns` 初始值與 `defaultColumns` 完全相同
2. 新增欄位時同時修改兩個物件

---

## 📚 相關文件

- **使用指南**：`cheng-ui/TABLE_CONFIG_USAGE_GUIDE.md`
- **後端設計**：`docs/Architecture/TABLE_CONFIG_ARCHITECTURE.md`
- **API 文件**：參見 `cheng-admin/src/main/java/com/cheng/web/controller/system/SysUserTableConfigController.java`

---

## 📊 進度追蹤

**完成進度**：3/22 (13.6%)  
**預計完成時間**：根據需求決定  
**優先級排序**：
1. 高頻使用頁面（使用者、角色、物品、借出）✅ 部分完成
2. 中頻使用頁面（字典、選單、部門、通知）
3. 低頻使用頁面（監控、工具、日誌）

---

**最後更新**：2025-12-07  
**文件維護**：Cascade AI
