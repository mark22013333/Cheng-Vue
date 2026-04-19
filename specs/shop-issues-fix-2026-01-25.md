# 商城功能問題修復清單

**建立日期**：2026-01-25
**問題來源**：使用者回報
**優先順序**：依嚴重程度排列（P0 最高）

---

## 問題總覽

| # | 問題描述 | 優先級 | 影響範圍 | 狀態 |
|---|---------|--------|---------|------|
| 1 | 新商品 SKU 首次儲存失敗 | P0 | 後台商品管理 | ✅ 已修復 |
| 2 | 購物車/結帳頁面商品圖片不顯示 | P0 | 前台購物流程 | ✅ 已修復 |
| 3 | 結帳頁面無法新增地址 | P0 | 前台購物流程 | ✅ 已修復 |
| 4 | 價格符號應為 `$` 而非 `¥` | P1 | 前台所有頁面 | ✅ 已修復 |
| 5 | 搜尋按鈕需要調整為圓形 | P2 | 前台 Header | ✅ 已修復 |

---

## 問題詳細分析

### 問題 1：新商品 SKU 首次儲存失敗（P0）

**現象描述**：
- 新商品上架後，前端商城頁面看不到 SKU 選項
- 後台編輯時，原本設定的 SKU 和商品圖片都消失
- 第二次編輯新增 SKU 後才能在商城頁面看到

**根本原因**：
後端 `ShopProductController.add()` 沒有返回 `productId`，導致前端無法呼叫 `batchSaveSku`。

**程式碼分析**：

```java
// ShopProductController.java - 目前程式碼
@PostMapping
public AjaxResult add(@Validated @RequestBody ShopProduct product) {
    product.setCreateBy(SecurityUtils.getUsername());
    int result = productService.insertProduct(product);
    AjaxResult ajaxResult = toAjax(result);
    ajaxResult.put("productTitle", product.getTitle());
    return ajaxResult;  // ❌ 沒有返回 productId
}
```

```javascript
// edit.vue - 前端程式碼
const savedProductId = response.data?.productId || form.value.productId || productId.value

if (skuList.value.length > 0 && savedProductId) {  // ❌ savedProductId 為 undefined
    return batchSaveSku(savedProductId, skuList.value)
}
```

**解決方案**：
修改 `ShopProductController.add()` 方法，在回傳結果中加入 `productId`。

**修復檔案**：
- `cheng-shop/src/main/java/com/cheng/shop/controller/ShopProductController.java`

---

### 問題 2：購物車/結帳頁面商品圖片不顯示（P0）

**現象描述**：
- 購物車頁面商品圖片顯示為 `/placeholder-image.png`
- 結帳頁面商品圖片同樣無法顯示

**根本原因**：
`ShopCartMapper.xml` 的 `selectCartVo` SQL 查詢缺少商品主圖欄位 `p.main_image`。

**程式碼分析**：

```xml
<!-- ShopCartMapper.xml - 目前程式碼 -->
<sql id="selectCartVo">
    select c.cart_id, c.member_id, c.sku_id, c.quantity, c.is_selected, c.create_time, c.update_time,
           s.product_id, p.title as product_name, s.sku_name, s.sku_image, s.price, s.stock_quantity
           <!-- ❌ 缺少 p.main_image as product_image -->
    from shop_cart c
    left join shop_product_sku s on c.sku_id = s.sku_id
    left join shop_product p on s.product_id = p.product_id
</sql>
```

**解決方案**：
在 `selectCartVo` 中加入 `p.main_image as product_image`，並在 `resultMap` 中加入對應的映射。

**修復檔案**：
- `cheng-shop/src/main/resources/mapper/shop/ShopCartMapper.xml`

---

### 問題 3：結帳頁面無法新增地址（P0）

**現象描述**：
- 點擊「新增地址」按鈕後跳轉到會員地址頁面
- 沒有返回結帳頁面的機制，造成結帳流程中斷
- 因為沒有地址，結帳按鈕無法點擊

**根本原因**：
`goAddAddress()` 函數直接導向 `/mall/member/address`，沒有處理新增完成後返回結帳頁面的流程。

**程式碼分析**：

```javascript
// checkout/index.vue
function goAddAddress() {
  showAddressDialog.value = false
  router.push('/mall/member/address')  // ❌ 直接跳轉，沒有返回機制
}
```

**解決方案**：
1. 方案 A：在結帳頁面內嵌地址新增對話框
2. 方案 B：跳轉時帶參數 `?redirect=/mall/checkout`，完成後返回

**修復檔案**：
- `cheng-ui/src/views/shop-front/checkout/index.vue`
- `cheng-ui/src/api/shop/address.js`（如有需要）

---

### 問題 4：價格符號應為 `$` 而非 `¥`（P1）

**現象描述**：
- 購物車頁面價格顯示為 `¥100.00`
- 結帳頁面價格顯示為 `¥100.00`

**根本原因**：
前端程式碼硬編碼使用 `¥` 符號。

**程式碼位置**：
```html
<!-- cart/index.vue -->
<span class="price">¥{{ formatPrice(item.price) }}</span>

<!-- checkout/index.vue -->
<div class="product-price">¥{{ formatPrice(item.price) }}</div>
```

**解決方案**：
將所有 `¥` 替換為 `$`。

**修復檔案**：
- `cheng-ui/src/views/shop-front/cart/index.vue`
- `cheng-ui/src/views/shop-front/checkout/index.vue`

---

### 問題 5：搜尋按鈕需要調整為圓形（P2）

**現象描述**：
- 搜尋按鈕目前為方形，需要調整為圓形以符合 UI 設計

**根本原因**：
使用 Element Plus 的 `el-input` 的 `append` slot，預設樣式不是獨立圓形按鈕。

**程式碼分析**：

```html
<!-- ShopLayout.vue -->
<el-input v-model="searchKeyword" placeholder="搜尋商品...">
  <template #append>
    <el-button icon="Search" @click="handleSearch" />
  </template>
</el-input>
```

**解決方案**：
將搜尋按鈕改為獨立的圓形按鈕，或調整 CSS 樣式。

**修復檔案**：
- `cheng-ui/src/layout/ShopLayout.vue`

---

## 修復順序

1. **P0 問題（必須立即修復）** ✅ 全部完成
   - [x] 問題 1：SKU 儲存問題 - 後端返回 productId
   - [x] 問題 2：圖片顯示問題 - Mapper 加入 main_image 欄位
   - [x] 問題 3：地址新增問題 - 結帳頁面內嵌地址表單

2. **P1 問題（重要但可稍後）** ✅ 全部完成
   - [x] 問題 4：價格符號問題 - 將 ¥ 替換為 $

3. **P2 問題（優化項目）** ✅ 全部完成
   - [x] 問題 5：搜尋按鈕樣式 - 改為獨立圓形按鈕

---

## 驗證清單

修復完成後，需驗證以下流程：

- [ ] 新增商品並設定 SKU → 前台能正確顯示 SKU 選項
- [ ] 加入購物車 → 圖片正確顯示、價格符號為 `$`
- [ ] 結帳頁面 → 能新增地址、圖片正確顯示、能成功提交訂單
- [ ] 搜尋功能 → 按鈕為圓形樣式
