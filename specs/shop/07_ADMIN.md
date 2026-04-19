# 07. 後台管理功能

> 整合到現有 cheng-ui 後台系統

---

## 7.1 選單結構

```
商城管理
├── 首頁管理
│   ├── 輪播管理
│   └── 區塊管理
├── 商品管理
│   ├── 分類管理
│   └── 商品列表
├── 訂單管理
│   └── 訂單列表
└── 會員管理
    └── 會員列表
```

---

## 7.2 頁面規格

### 7.2.1 輪播管理 (shop/banner/index.vue)

**列表欄位**

| 欄位 | 說明 | 寬度 |
|-----|-----|-----|
| 縮圖 | 輪播圖片預覽 | 120px |
| 標題 | 輪播標題 | auto |
| 位置 | 展示位置 | 100px |
| 連結類型 | 連結目標類型 | 100px |
| 排序 | 數字越小越前 | 80px |
| 狀態 | 啟用/停用 | 80px |
| 時間區間 | 開始~結束時間 | 200px |
| 操作 | 編輯、刪除 | 120px |

**新增/編輯表單**

```vue
<el-form :model="form" :rules="rules" label-width="100px">
  <el-form-item label="標題" prop="title">
    <el-input v-model="form.title" placeholder="請輸入標題" />
  </el-form-item>
  
  <el-form-item label="圖片" prop="imageUrl">
    <image-upload v-model="form.imageUrl" :limit="1" />
  </el-form-item>
  
  <el-form-item label="手機版圖片">
    <image-upload v-model="form.mobileImage" :limit="1" />
  </el-form-item>
  
  <el-form-item label="展示位置" prop="position">
    <el-select v-model="form.position">
      <el-option label="首頁頂部" value="HOME_TOP" />
      <el-option label="首頁中部" value="HOME_MIDDLE" />
    </el-select>
  </el-form-item>
  
  <el-form-item label="連結類型" prop="linkType">
    <el-select v-model="form.linkType">
      <el-option label="無連結" value="NONE" />
      <el-option label="商品詳情" value="PRODUCT" />
      <el-option label="商品分類" value="CATEGORY" />
      <el-option label="外部連結" value="URL" />
    </el-select>
  </el-form-item>
  
  <el-form-item label="連結值" v-if="form.linkType !== 'NONE'">
    <el-input v-model="form.linkValue" placeholder="請輸入連結" />
  </el-form-item>
  
  <el-form-item label="排序" prop="sortOrder">
    <el-input-number v-model="form.sortOrder" :min="0" />
  </el-form-item>
  
  <el-form-item label="展示時間">
    <el-date-picker
      v-model="form.timeRange"
      type="datetimerange"
      range-separator="至"
      start-placeholder="開始時間"
      end-placeholder="結束時間"
    />
  </el-form-item>
  
  <el-form-item label="狀態" prop="status">
    <el-radio-group v-model="form.status">
      <el-radio label="ENABLED">啟用</el-radio>
      <el-radio label="DISABLED">停用</el-radio>
    </el-radio-group>
  </el-form-item>
</el-form>
```

---

### 7.2.2 區塊管理 (shop/block/index.vue)

**頁面選擇器**

```vue
<el-tabs v-model="activePageKey">
  <el-tab-pane label="首頁" name="HOME" />
  <el-tab-pane label="關於我們" name="ABOUT" />
  <el-tab-pane label="聯絡我們" name="CONTACT" />
</el-tabs>
```

**區塊編輯器**

```vue
<div class="block-list">
  <draggable v-model="blocks" item-key="blockId">
    <template #item="{ element }">
      <el-card class="block-item">
        <template #header>
          <span>{{ element.title || '區塊' }}</span>
          <el-button type="danger" size="small" @click="removeBlock(element)">刪除</el-button>
        </template>
        
        <el-form label-width="80px">
          <el-form-item label="區塊類型">
            <el-select v-model="element.blockType">
              <el-option label="純文字" value="TEXT" />
              <el-option label="圖片" value="IMAGE" />
              <el-option label="富文本" value="HTML" />
              <el-option label="商品列表" value="PRODUCT_LIST" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="標題">
            <el-input v-model="element.title" />
          </el-form-item>
          
          <!-- 根據 blockType 動態顯示不同編輯器 -->
          <el-form-item label="內容" v-if="element.blockType === 'TEXT'">
            <el-input v-model="element.content" type="textarea" :rows="4" />
          </el-form-item>
          
          <el-form-item label="圖片" v-if="element.blockType === 'IMAGE'">
            <image-upload v-model="element.imageUrl" />
          </el-form-item>
          
          <el-form-item label="內容" v-if="element.blockType === 'HTML'">
            <editor v-model="element.content" />
          </el-form-item>
        </el-form>
      </el-card>
    </template>
  </draggable>
  
  <el-button type="primary" @click="addBlock">新增區塊</el-button>
  <el-button type="success" @click="saveBlocks">儲存</el-button>
</div>
```

---

### 7.2.3 分類管理 (shop/category/index.vue)

**樹形列表**

```vue
<el-table :data="categoryTree" row-key="categoryId" :tree-props="{ children: 'children' }">
  <el-table-column prop="name" label="分類名稱" />
  <el-table-column prop="icon" label="圖示" width="80">
    <template #default="{ row }">
      <el-image v-if="row.icon" :src="row.icon" style="width: 40px; height: 40px" />
    </template>
  </el-table-column>
  <el-table-column prop="sortOrder" label="排序" width="80" />
  <el-table-column prop="status" label="狀態" width="80">
    <template #default="{ row }">
      <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
        {{ row.status === 'ENABLED' ? '啟用' : '停用' }}
      </el-tag>
    </template>
  </el-table-column>
  <el-table-column label="操作" width="200">
    <template #default="{ row }">
      <el-button size="small" @click="handleAdd(row)">新增子分類</el-button>
      <el-button size="small" @click="handleEdit(row)">編輯</el-button>
      <el-button size="small" type="danger" @click="handleDelete(row)">刪除</el-button>
    </template>
  </el-table-column>
</el-table>
```

---

### 7.2.4 商品管理 (shop/product/index.vue)

**搜尋條件**

```vue
<el-form :model="queryParams" :inline="true">
  <el-form-item label="商品名稱">
    <el-input v-model="queryParams.title" placeholder="請輸入商品名稱" />
  </el-form-item>
  <el-form-item label="分類">
    <el-cascader v-model="queryParams.categoryId" :options="categoryTree" />
  </el-form-item>
  <el-form-item label="狀態">
    <el-select v-model="queryParams.status" clearable>
      <el-option label="草稿" value="DRAFT" />
      <el-option label="預覽" value="PREVIEW" />
      <el-option label="上架中" value="ON_SALE" />
      <el-option label="已下架" value="OFF_SALE" />
    </el-select>
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="handleQuery">搜尋</el-button>
    <el-button @click="resetQuery">重置</el-button>
  </el-form-item>
</el-form>
```

**列表欄位**

| 欄位 | 說明 | 寬度 |
|-----|-----|-----|
| 主圖 | 商品主圖 | 80px |
| 商品名稱 | 標題 | auto |
| 分類 | 所屬分類 | 120px |
| 價格區間 | SKU 最低~最高價 | 150px |
| 銷量 | 累計銷量 | 80px |
| 狀態 | 上架狀態 | 100px |
| 操作 | 編輯、上/下架、刪除 | 200px |

**商品編輯頁 (shop/product/edit.vue)**

採用 Tab 分頁：

| Tab | 內容 |
|-----|-----|
| 基本資訊 | 標題、副標題、分類、標籤 |
| 商品圖片 | 主圖、輪播圖、影片 |
| SKU 管理 | 規格設定、價格、庫存關聯 |
| 商品詳情 | 富文本編輯器 |
| SEO 設定 | 預留擴展 |

---

### 7.2.5 SKU 管理

**SKU 表格（嵌入商品編輯頁）**

```vue
<el-table :data="skuList" border>
  <el-table-column prop="skuName" label="規格名稱" />
  <el-table-column label="SKU 圖片" width="100">
    <template #default="{ row }">
      <image-upload v-model="row.skuImage" :limit="1" />
    </template>
  </el-table-column>
  <el-table-column label="銷售價" width="120">
    <template #default="{ row }">
      <el-input-number v-model="row.price" :min="0" :precision="2" />
    </template>
  </el-table-column>
  <el-table-column label="原價" width="120">
    <template #default="{ row }">
      <el-input-number v-model="row.originalPrice" :min="0" :precision="2" />
    </template>
  </el-table-column>
  <el-table-column label="關聯庫存" width="200">
    <template #default="{ row }">
      <el-select v-model="row.invItemId" filterable clearable placeholder="選擇庫存物品">
        <el-option 
          v-for="item in invItemList" 
          :key="item.itemId" 
          :label="item.itemName" 
          :value="item.itemId"
        />
      </el-select>
    </template>
  </el-table-column>
  <el-table-column label="獨立庫存" width="100">
    <template #default="{ row }">
      <el-input-number v-model="row.stockQuantity" :min="0" :disabled="!!row.invItemId" />
    </template>
  </el-table-column>
  <el-table-column label="操作" width="80">
    <template #default="{ row, $index }">
      <el-button type="danger" size="small" @click="removeSku($index)">刪除</el-button>
    </template>
  </el-table-column>
</el-table>

<el-button @click="addSku">新增規格</el-button>
```

---

### 7.2.6 訂單管理 (shop/order/index.vue)

**搜尋條件**

```vue
<el-form :model="queryParams" :inline="true">
  <el-form-item label="訂單編號">
    <el-input v-model="queryParams.orderNo" placeholder="請輸入訂單編號" />
  </el-form-item>
  <el-form-item label="會員">
    <el-input v-model="queryParams.memberNickname" placeholder="請輸入會員暱稱" />
  </el-form-item>
  <el-form-item label="訂單狀態">
    <el-select v-model="queryParams.status" clearable>
      <el-option label="待付款" value="PENDING" />
      <el-option label="已付款" value="PAID" />
      <el-option label="處理中" value="PROCESSING" />
      <el-option label="已出貨" value="SHIPPED" />
      <el-option label="已完成" value="COMPLETED" />
      <el-option label="已取消" value="CANCELLED" />
    </el-select>
  </el-form-item>
  <el-form-item label="下單時間">
    <el-date-picker v-model="queryParams.dateRange" type="daterange" />
  </el-form-item>
</el-form>
```

**列表欄位**

| 欄位 | 說明 |
|-----|-----|
| 訂單編號 | 訂單唯一編號 |
| 會員 | 會員暱稱 |
| 商品數 | 訂單商品件數 |
| 訂單金額 | 總金額 |
| 付款狀態 | 付款狀態標籤 |
| 物流狀態 | 物流狀態標籤 |
| 訂單狀態 | 訂單狀態標籤 |
| 下單時間 | 建立時間 |
| 操作 | 詳情、出貨、退款 |

**訂單詳情頁 (shop/order/detail.vue)**

| 區塊 | 內容 |
|-----|-----|
| 基本資訊 | 訂單編號、狀態、時間 |
| 收件資訊 | 收件人、電話、地址 |
| 商品明細 | 商品列表表格 |
| 金額明細 | 商品金額、運費、折扣、總計 |
| 物流資訊 | 物流單號、軌跡 |
| 操作紀錄 | 訂單操作日誌 |

---

### 7.2.7 會員管理 (shop/member/index.vue)

**搜尋條件**

```vue
<el-form :model="queryParams" :inline="true">
  <el-form-item label="會員編號">
    <el-input v-model="queryParams.memberNo" />
  </el-form-item>
  <el-form-item label="暱稱">
    <el-input v-model="queryParams.nickname" />
  </el-form-item>
  <el-form-item label="手機">
    <el-input v-model="queryParams.mobile" />
  </el-form-item>
  <el-form-item label="狀態">
    <el-select v-model="queryParams.status" clearable>
      <el-option label="正常" value="ACTIVE" />
      <el-option label="停用" value="DISABLED" />
      <el-option label="凍結" value="FROZEN" />
    </el-select>
  </el-form-item>
</el-form>
```

**列表欄位**

| 欄位 | 說明 |
|-----|-----|
| 頭像 | 會員頭像 |
| 會員編號 | 唯一編號 |
| 暱稱 | 會員暱稱 |
| 手機 | 手機號碼 |
| Email | 電子信箱 |
| 等級 | 會員等級 |
| 點數 | 會員點數 |
| 狀態 | 帳號狀態 |
| 註冊時間 | 註冊時間 |
| 操作 | 詳情、停用 |

---

## 7.3 權限配置

在 `sys_menu` 新增選單：

```sql
-- 商城管理（一級選單）
INSERT INTO sys_menu VALUES (2000, '商城管理', 0, 1, 'shop', NULL, NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', NOW(), '', NULL, '商城管理目錄');

-- 首頁管理（二級選單）
INSERT INTO sys_menu VALUES (2001, '首頁管理', 2000, 1, 'home', NULL, NULL, 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', NOW(), '', NULL, '首頁管理目錄');
INSERT INTO sys_menu VALUES (2002, '輪播管理', 2001, 1, 'banner', 'shop/banner/index', NULL, 1, 0, 'C', '0', '0', 'shop:banner:list', 'list', 'admin', NOW(), '', NULL, '輪播管理選單');
INSERT INTO sys_menu VALUES (2003, '區塊管理', 2001, 2, 'block', 'shop/block/index', NULL, 1, 0, 'C', '0', '0', 'shop:block:list', 'component', 'admin', NOW(), '', NULL, '區塊管理選單');

-- 商品管理（二級選單）
INSERT INTO sys_menu VALUES (2010, '商品管理', 2000, 2, 'product', NULL, NULL, 1, 0, 'M', '0', '0', '', 'goods', 'admin', NOW(), '', NULL, '商品管理目錄');
INSERT INTO sys_menu VALUES (2011, '分類管理', 2010, 1, 'category', 'shop/category/index', NULL, 1, 0, 'C', '0', '0', 'shop:category:list', 'tree', 'admin', NOW(), '', NULL, '分類管理選單');
INSERT INTO sys_menu VALUES (2012, '商品列表', 2010, 2, 'list', 'shop/product/index', NULL, 1, 0, 'C', '0', '0', 'shop:product:list', 'list', 'admin', NOW(), '', NULL, '商品列表選單');

-- 訂單管理（二級選單）
INSERT INTO sys_menu VALUES (2020, '訂單管理', 2000, 3, 'order', 'shop/order/index', NULL, 1, 0, 'C', '0', '0', 'shop:order:list', 'documentation', 'admin', NOW(), '', NULL, '訂單管理選單');

-- 會員管理（二級選單）
INSERT INTO sys_menu VALUES (2030, '會員管理', 2000, 4, 'member', 'shop/member/index', NULL, 1, 0, 'C', '0', '0', 'shop:member:list', 'peoples', 'admin', NOW(), '', NULL, '會員管理選單');
```
