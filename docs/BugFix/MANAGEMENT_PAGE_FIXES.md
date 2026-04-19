# 📝 物品與庫存管理頁面問題修正

## 🎯 修正日期
**2025-10-04**

---

## 🐛 問題描述

使用者在使用 `/inventory/management` 頁面時發現了以下三個問題：

### 1. 點擊「修改」按鈕會跳到 404 頁面
**現象**：點擊列表中的「修改」按鈕時，頁面跳轉到 404 錯誤頁面

**原因**：路由配置中缺少物品編輯頁面的路由定義

### 2. 庫存狀態篩選無效
**現象**：
- 選擇「正常」、「低庫存」或「無庫存」後，篩選結果不正確
- 無法自訂低庫存的閾值，只能使用物品設定的最低庫存值

**原因**：
- 選擇器缺少 `@change` 事件綁定
- 後端查詢邏輯未支援自訂低庫存閾值參數

### 3. 缺少排序功能
**現象**：無法對「物品編號」、「物品名稱」、「存放位置」進行排序

**原因**：
- 前端表格欄位未設定 `sortable="custom"`
- 前端缺少排序事件處理
- 後端查詢未支援動態排序

---

## ✅ 修正方案

### 1. 修正路由配置（404 問題）

**檔案**：`cheng-ui/src/router/index.js`

**修正內容**：在 `/inventory` 路由下新增物品新增和編輯的子路由

```javascript
{
  path: 'item/add',
  component: () => import('@/views/inventory/item/index'),
  name: 'ItemAdd',
  meta: { title: '新增物品', icon: 'plus', activeMenu: '/inventory/management' },
  hidden: true
},
{
  path: 'item/edit/:itemId(\\d+)',
  component: () => import('@/views/inventory/item/index'),
  name: 'ItemEdit',
  meta: { title: '修改物品', icon: 'edit', activeMenu: '/inventory/management' },
  hidden: true
}
```

**說明**：
- `hidden: true`：不在側邊欄顯示
- `activeMenu: '/inventory/management'`：確保側邊欄高亮正確的選單項目
- 路由參數 `:itemId(\\d+)`：限制參數必須為數字

---

### 2. 修正庫存狀態篩選

#### 2.1 前端修正

**檔案**：`cheng-ui/src/views/inventory/management/index.vue`

**修正內容 A**：為庫存狀態選擇器新增 `@change` 事件

```vue
<el-form-item label="庫存狀態" prop="stockStatus">
  <el-select 
    v-model="queryParams.stockStatus" 
    placeholder="請選擇" 
    clearable 
    style="width: 150px" 
    @change="handleQuery"
  >
    <el-option label="全部" value="" />
    <el-option label="正常" value="0" />
    <el-option label="低庫存" value="1" />
    <el-option label="無庫存" value="2" />
  </el-select>
</el-form-item>
```

**修正內容 B**：新增低庫存閾值輸入框（當選擇「低庫存」時顯示）

```vue
<el-form-item 
  label="低庫存閾值" 
  prop="lowStockThreshold" 
  v-if="queryParams.stockStatus === '1'"
>
  <el-input-number 
    v-model="queryParams.lowStockThreshold" 
    :min="0" 
    :max="1000" 
    placeholder="預設為物品最低庫存" 
    style="width: 150px" 
  />
</el-form-item>
```

**修正內容 C**：新增全域低庫存閾值設定（在操作按鈕區）

```vue
<el-col :span="1.5">
  <el-tooltip content="點擊可設定全域低庫存閾值" placement="top">
    <el-input-number 
      v-model="globalLowStockThreshold" 
      :min="0" 
      :max="1000" 
      size="mini"
      placeholder="低庫存閾值"
      style="width: 130px"
      @change="handleThresholdChange"
    />
  </el-tooltip>
</el-col>
```

**修正內容 D**：更新查詢參數和新增處理方法

```javascript
data() {
  return {
    queryParams: {
      // ... 現有參數
      lowStockThreshold: null,  // 新增
    },
    globalLowStockThreshold: null,  // 新增
  };
},
methods: {
  /** 全域低庫存閾值變化 */
  handleThresholdChange(value) {
    if (this.queryParams.stockStatus === '1') {
      this.queryParams.lowStockThreshold = value;
      this.handleQuery();
    }
  }
}
```

#### 2.2 後端修正

**檔案 A**：`cheng-system/src/main/java/com/cheng/system/dto/InvItemWithStockDTO.java`

新增自訂低庫存閾值欄位：

```java
// ========== 查詢參數 ==========
/**
 * 自訂低庫存閾值（用於查詢條件）
 */
private Integer lowStockThreshold;
```

**檔案 B**：`cheng-system/src/main/resources/mapper/system/InvItemMapper.xml`

修正庫存狀態篩選邏輯，支援自訂閾值：

```xml
<if test="stockStatus != null and stockStatus != ''">
    <choose>
        <when test="stockStatus == '2'">
            and (s.total_quantity is null or s.total_quantity = 0)
        </when>
        <when test="stockStatus == '1'">
            <choose>
                <when test="lowStockThreshold != null">
                    and s.available_qty &lt;= #{lowStockThreshold} and s.total_quantity > 0
                </when>
                <otherwise>
                    and s.available_qty &lt;= i.min_stock and s.total_quantity > 0
                </otherwise>
            </choose>
        </when>
        <when test="stockStatus == '0'">
            <choose>
                <when test="lowStockThreshold != null">
                    and s.available_qty > #{lowStockThreshold}
                </when>
                <otherwise>
                    and s.available_qty > i.min_stock
                </otherwise>
            </choose>
        </when>
    </choose>
</if>
```

**邏輯說明**：
- **無庫存（2）**：總數量為 null 或 0
- **低庫存（1）**：
  - 若有自訂閾值：可用數量 ≤ 自訂閾值 且 總數量 > 0
  - 若無自訂閾值：可用數量 ≤ 物品最低庫存 且 總數量 > 0
- **正常（0）**：
  - 若有自訂閾值：可用數量 > 自訂閾值
  - 若無自訂閾值：可用數量 > 物品最低庫存

---

### 3. 新增排序功能

#### 3.1 前端修正

**檔案**：`cheng-ui/src/views/inventory/management/index.vue`

**修正內容 A**：為表格新增排序事件監聽

```vue
<el-table 
  v-loading="loading" 
  :data="managementList" 
  @selection-change="handleSelectionChange" 
  @sort-change="handleSortChange"
>
```

**修正內容 B**：為需要排序的欄位新增 `sortable="custom"`

```vue
<el-table-column 
  label="物品編碼" 
  align="center" 
  prop="itemCode" 
  width="140" 
  sortable="custom" 
  :show-overflow-tooltip="true" 
/>
<el-table-column 
  label="物品名稱" 
  align="center" 
  prop="itemName" 
  min-width="150" 
  sortable="custom" 
  :show-overflow-tooltip="true" 
/>
<el-table-column 
  label="存放位置" 
  align="center" 
  prop="location" 
  width="140" 
  sortable="custom" 
  :show-overflow-tooltip="true" 
/>
```

**修正內容 C**：新增查詢參數和排序處理方法

```javascript
data() {
  return {
    queryParams: {
      // ... 現有參數
      orderByColumn: null,  // 新增：排序欄位
      isAsc: null,          // 新增：排序方向
    },
  };
},
methods: {
  /** 排序變化處理 */
  handleSortChange({ column, prop, order }) {
    if (order === 'ascending') {
      this.queryParams.orderByColumn = prop;
      this.queryParams.isAsc = 'asc';
    } else if (order === 'descending') {
      this.queryParams.orderByColumn = prop;
      this.queryParams.isAsc = 'desc';
    } else {
      this.queryParams.orderByColumn = null;
      this.queryParams.isAsc = null;
    }
    this.handleQuery();
  }
}
```

#### 3.2 後端修正

**檔案**：`cheng-system/src/main/resources/mapper/system/InvItemMapper.xml`

新增動態排序 SQL：

```xml
<choose>
    <when test="orderByColumn != null and orderByColumn != ''">
        order by 
        <choose>
            <when test="orderByColumn == 'itemCode'">i.item_code</when>
            <when test="orderByColumn == 'itemName'">i.item_name</when>
            <when test="orderByColumn == 'location'">i.location</when>
            <otherwise>i.create_time</otherwise>
        </choose>
        <choose>
            <when test="isAsc == 'asc'">asc</when>
            <otherwise>desc</otherwise>
        </choose>
    </when>
    <otherwise>
        order by i.create_time desc
    </otherwise>
</choose>
```

**排序規則**：
- **有排序條件**：依據前端指定的欄位和方向排序
- **無排序條件**：預設按建立時間降序排列
- **支援欄位**：
  - `itemCode` → `i.item_code`
  - `itemName` → `i.item_name`
  - `location` → `i.location`

---

## 📊 修正檔案清單

| 類別 | 檔案路徑 | 修正內容 |
|------|----------|----------|
| **前端路由** | `cheng-ui/src/router/index.js` | 新增物品新增/編輯路由 |
| **前端頁面** | `cheng-ui/src/views/inventory/management/index.vue` | 新增排序、低庫存閾值、修正篩選 |
| **後端 DTO** | `cheng-system/src/main/java/com/cheng/system/dto/InvItemWithStockDTO.java` | 新增 lowStockThreshold 欄位 |
| **後端 Mapper** | `cheng-system/src/main/resources/mapper/system/InvItemMapper.xml` | 新增排序和閾值邏輯 |
| **文件** | `docs/MANAGEMENT_PAGE_FIXES.md` | 本修正說明文件 |

---

## 🧪 測試驗證

### 測試案例 1：修改功能（404 修正）

✅ **測試步驟**：
1. 進入 `/inventory/management` 頁面
2. 點擊任意物品的「修改」按鈕
3. 確認跳轉到物品編輯頁面（例如：`/inventory/item/edit/1`）
4. 確認側邊欄「物品與庫存管理」選單保持高亮

✅ **預期結果**：
- 正確跳轉到編輯頁面，無 404 錯誤
- 側邊欄選單保持正確高亮

---

### 測試案例 2：庫存狀態篩選

✅ **測試 2.1：篩選「無庫存」**
1. 選擇庫存狀態 = 「無庫存」
2. 確認只顯示總數量為 0 或 null 的物品

✅ **測試 2.2：篩選「低庫存」（使用預設閾值）**
1. 選擇庫存狀態 = 「低庫存」
2. 不設定低庫存閾值
3. 確認顯示可用數量 ≤ 物品最低庫存的物品

✅ **測試 2.3：篩選「低庫存」（使用自訂閾值）**
1. 選擇庫存狀態 = 「低庫存」
2. 設定低庫存閾值 = 10
3. 確認顯示可用數量 ≤ 10 的物品

✅ **測試 2.4：篩選「正常」（使用自訂閾值）**
1. 設定全域低庫存閾值 = 20
2. 選擇庫存狀態 = 「正常」
3. 確認顯示可用數量 > 20 的物品

✅ **測試 2.5：即時更新**
1. 選擇庫存狀態後，結果應立即更新
2. 修改閾值後，結果應立即更新

---

### 測試案例 3：排序功能

✅ **測試 3.1：物品編碼排序**
1. 點擊「物品編碼」欄位標題
2. 第一次點擊：升序排列（A→Z 或 1→9）
3. 第二次點擊：降序排列（Z→A 或 9→1）
4. 第三次點擊：取消排序（恢復預設）

✅ **測試 3.2：物品名稱排序**
1. 點擊「物品名稱」欄位標題
2. 確認按照名稱正確排序

✅ **測試 3.3：存放位置排序**
1. 點擊「存放位置」欄位標題
2. 確認按照位置正確排序

✅ **測試 3.4：排序與篩選組合**
1. 設定篩選條件（例如：庫存狀態 = 低庫存）
2. 再進行排序（例如：按物品編碼升序）
3. 確認結果同時滿足篩選和排序條件

---

## 💡 使用說明

### 自訂低庫存閾值的兩種方式

#### 方式一：臨時篩選（僅影響當次查詢）
1. 在搜尋表單中選擇「庫存狀態」= 「低庫存」
2. 在「低庫存閾值」輸入框中輸入數值（例如：15）
3. 點擊「搜尋」按鈕
4. 系統會篩選可用數量 ≤ 15 的物品

#### 方式二：全域設定（影響後續查詢）
1. 在操作按鈕區找到「低庫存閾值」輸入框
2. 輸入數值（例如：20）
3. 選擇「庫存狀態」= 「低庫存」
4. 系統會自動套用該閾值進行篩選

**差異**：
- **臨時篩選**：每次需要手動輸入
- **全域設定**：輸入一次後，切換到「低庫存」狀態時自動套用

---

### 排序操作說明

1. **升序排列**：點擊欄位標題一次，欄位旁顯示 ↑
2. **降序排列**：再點擊一次，欄位旁顯示 ↓
3. **取消排序**：再點擊一次，恢復預設排序（按建立時間降序）

**可排序欄位**：
- 物品編碼
- 物品名稱
- 存放位置

---

## 🚀 部署步驟

### 開發環境測試

```bash
# 1. 重新啟動前端服務
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui
npm run dev

# 2. 重新啟動後端服務
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn spring-boot:run -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido

# 3. 清除瀏覽器快取
# 按 Ctrl+Shift+Delete（Windows/Linux）或 Cmd+Shift+Delete（Mac）

# 4. 重新登入系統測試
```

### 正式環境部署

```bash
# 1. 編譯前端
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui
npm run build:prod

# 2. 編譯後端
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn clean install -DskipTests

# 3. 部署到伺服器
./cheng.deploy/deploy-to-server.sh
```

---

## 📝 技術要點

### 1. Vue Router 路由參數驗證
```javascript
path: 'item/edit/:itemId(\\d+)'
```
- `\\d+`：確保 itemId 必須是數字
- 防止非法路由參數

### 2. Element UI 自訂排序
```vue
sortable="custom"
@sort-change="handleSortChange"
```
- `custom`：不使用前端排序，由後端處理
- 適合大量資料的分頁排序場景

### 3. MyBatis 動態 SQL
```xml
<choose>
    <when test="condition">SQL1</when>
    <otherwise>SQL2</otherwise>
</choose>
```
- 根據條件動態產生 SQL
- 提高 SQL 複用性和靈活性

### 4. 條件式渲染
```vue
v-if="queryParams.stockStatus === '1'"
```
- 只有在選擇「低庫存」時才顯示閾值輸入框
- 改善使用者體驗

---

## 🎯 預期效果

### 修正前 vs 修正後

| 功能 | 修正前 | 修正後 |
|------|--------|--------|
| **點擊修改** | 跳轉到 404 頁面 | 正常進入編輯頁面 |
| **庫存篩選** | 選擇後無反應 | 即時篩選正確資料 |
| **低庫存定義** | 只能用物品的最低庫存 | 可自訂閾值（例如：統一 20） |
| **排序功能** | 無法排序 | 可排序 3 個欄位 |
| **使用體驗** | 功能受限，操作困難 | 功能完善，操作便利 |

---

## 🔧 故障排除

### Q1: 點擊修改還是出現 404
**解決方案**：
1. 確認前端服務已重新啟動
2. 清除瀏覽器快取
3. 檢查路由配置是否正確儲存

### Q2: 篩選後沒有資料
**可能原因**：
1. 資料庫中確實沒有符合條件的資料
2. 檢查資料表中是否有庫存資料

**解決方案**：
```sql
-- 檢查物品和庫存資料
SELECT i.item_code, i.item_name, i.min_stock, s.available_qty, s.total_quantity
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0';
```

### Q3: 排序沒有作用
**解決方案**：
1. 確認後端 Mapper XML 修正已部署
2. 重新啟動後端服務
3. 檢查瀏覽器 Network 請求，確認參數正確傳送

---

## 📞 相關文件

- **側邊欄調整功能**：`docs/sidebar_resize_feature.md`
- **整合總結**：`docs/INTEGRATION_SUMMARY.md`
- **整合指南**：`docs/inventory_integration_guide.md`

---

**🎉 修正完成！現在您可以正常使用修改、篩選和排序功能了！**
