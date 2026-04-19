# 歸還記錄功能與遺失物品處理

## 修改日期
2025-10-07

---

## 需求說明

### 問題 1：遺失物品的庫存處理不正確
**問題描述**：
當歸還時選擇「遺失」，庫存計算錯誤。遺失的物品應該從總數量中扣除，因為物品已經不存在了。

**原邏輯**：
- 完好：可用數量 +1
- 損壞：損壞數量 +1，可用數量不變
- **遺失：可用數量 +1** ❌ 錯誤！

**正確邏輯**：
- 完好：可用數量 +1
- 損壞：損壞數量 +1，可用數量不變
- **遺失：總數量 -1，遺失數量 +1，可用數量不變** ✅ 正確！

### 問題 2：無法查看歸還記錄明細
**問題描述**：
借出 3 個物品後，無法得知每次歸還時物品的狀態（完好/損壞/遺失）。需要一個「歸還記錄」功能來追蹤每次歸還的詳細資訊。

---

## 解決方案

### 1. ✅ 新增遺失數量欄位

#### 資料庫變更
**檔案**：`sql/add_lost_qty.sql`

```sql
ALTER TABLE inv_stock ADD COLUMN lost_qty INT(11) DEFAULT 0 COMMENT '遺失數量' AFTER damaged_qty;
```

**執行方式**：
```bash
mysql -u root -p < sql/add_lost_qty.sql
```

#### 後端 Domain 更新
**修改檔案**：
1. `InvStock.java` - 新增 `lostQty` 欄位
2. `InvItemWithStockDTO.java` - 新增 `lostQty` 欄位
3. `InvItemServiceImpl.java` - 初始化時設定 `lostQty = 0`
4. `BookItemServiceImpl.java` - 初始化時設定 `lostQty = 0`

```java
/**
 * 遺失數量
 */
@Excel(name = "遺失數量")
private Integer lostQty;
```

---

### 2. ✅ 修正歸還遺失物品的邏輯

#### 修改檔案
`InvBorrowServiceImpl.java` - `returnItem()` 方法

#### 原邏輯（錯誤）
```java
// 更新庫存
stock.setAvailableQty(stock.getAvailableQty() + returnQuantity);
stock.setBorrowedQty(stock.getBorrowedQty() - returnQuantity);

// 如果物品損壞，增加損壞數量
if ("1".equals(isDamaged)) {
    stock.setDamagedQty(stock.getDamagedQty() + returnQuantity);
    stock.setAvailableQty(stock.getAvailableQty() - returnQuantity);
}
```

#### 新邏輯（正確）
```java
// 減少借出數量
stock.setBorrowedQty(stock.setBorrowedQty() - returnQuantity);

// 根據物品狀況更新庫存
if ("lost".equals(conditionDesc) || "遺失".equals(conditionDesc)) {
    // 遺失：總數量減少，增加遺失數量，可用數量不變
    stock.setTotalQuantity(stock.getTotalQuantity() - returnQuantity);
    stock.setLostQty(stock.getLostQty() + returnQuantity);
} else if ("1".equals(isDamaged) || "damaged".equals(conditionDesc) || "損壞".equals(conditionDesc)) {
    // 損壞：增加損壞數量，可用數量不變
    stock.setDamagedQty(stock.getDamagedQty() + returnQuantity);
} else {
    // 完好：增加可用數量
    stock.setAvailableQty(stock.getAvailableQty() + returnQuantity);
}
```

#### 邏輯說明

| 物品狀態 | 可用數量 | 總數量 | 損壞數量 | 遺失數量 | 借出數量 |
|---------|---------|--------|---------|---------|---------|
| **完好** | +1 | 不變 | 不變 | 不變 | -1 |
| **損壞** | 不變 | 不變 | +1 | 不變 | -1 |
| **遺失** | 不變 | -1 | 不變 | +1 | -1 |

#### 案例說明

**初始狀態**：
- 總數量：10
- 可用數量：7
- 借出數量：3
- 損壞數量：0
- 遺失數量：0

**情境 1：歸還 1 個完好物品**
- 總數量：10（不變）
- 可用數量：8（+1）
- 借出數量：2（-1）
- 損壞數量：0
- 遺失數量：0

**情境 2：歸還 1 個損壞物品**
- 總數量：10（不變）
- 可用數量：7（不變）
- 借出數量：2（-1）
- 損壞數量：1（+1）
- 遺失數量：0

**情境 3：歸還 1 個遺失物品**
- 總數量：9（-1）⭐ 關鍵！
- 可用數量：7（不變）
- 借出數量：2（-1）
- 損壞數量：0
- 遺失數量：1（+1）

---

### 3. ✅ 建立歸還記錄功能

#### 資料表
`inv_return` 表已存在，包含欄位：
- `return_id` - 歸還記錄 ID
- `borrow_id` - 借出記錄 ID
- `item_id` - 物品 ID
- `return_quantity` - 歸還數量
- `return_time` - 歸還時間
- `item_condition` - 物品狀態（good/damaged/lost）
- `is_overdue` - 是否逾期
- `overdue_days` - 逾期天數
- `damage_description` - 損壞說明

#### 後端實作

##### a) Service 層
**檔案**：`InvBorrowServiceImpl.java`

在 `returnItem()` 方法中新增建立歸還記錄的邏輯：

```java
// 建立歸還記錄
InvReturn invReturn = new InvReturn();
invReturn.setBorrowId(borrowId);
invReturn.setBorrowCode(borrow.getBorrowNo());
invReturn.setItemId(borrow.getItemId());
invReturn.setItemCode(borrow.getItemCode());
invReturn.setItemName(borrow.getItemName());
invReturn.setReturnQuantity(returnQuantity.longValue());
invReturn.setBorrowerId(borrow.getBorrowerId());
invReturn.setBorrowerName(borrow.getBorrowerName());
invReturn.setReturnTime(DateUtils.getNowDate());
invReturn.setExpectedReturn(borrow.getExpectedReturn());

// 判斷是否逾期
if (borrow.getExpectedReturn() != null && DateUtils.getNowDate().after(borrow.getExpectedReturn())) {
    invReturn.setIsOverdue("1");
    long diffInMillies = DateUtils.getNowDate().getTime() - borrow.getExpectedReturn().getTime();
    long overdueDays = diffInMillies / (24 * 60 * 60 * 1000);
    invReturn.setOverdueDays(overdueDays);
} else {
    invReturn.setIsOverdue("0");
    invReturn.setOverdueDays(0L);
}

// 設定物品狀況
if ("lost".equals(conditionDesc) || "遺失".equals(conditionDesc)) {
    invReturn.setItemCondition("lost");
} else if ("1".equals(isDamaged) || "damaged".equals(conditionDesc) || "損壞".equals(conditionDesc)) {
    invReturn.setItemCondition("damaged");
    invReturn.setDamageDescription(damageDesc);
} else {
    invReturn.setItemCondition("good");
}

invReturn.setReceiverId(returnerId);
invReturn.setReturnStatus("1"); // 已確認
invReturn.setCreateTime(DateUtils.getNowDate());

// 儲存歸還記錄
invReturnMapper.insertInvReturn(invReturn);
```

##### b) Controller 層
**檔案**：`InvBorrowController.java`

新增查詢歸還記錄的 API：

```java
/**
 * 查詢借出記錄的歸還記錄
 */
@PreAuthorize("@ss.hasPermi('inventory:borrow:query')")
@GetMapping("/returnRecords/{borrowId}")
public AjaxResult getReturnRecords(@PathVariable Long borrowId) {
    try {
        List<InvReturn> returnRecords = invReturnMapper.selectInvReturnByBorrowId(borrowId);
        return success(returnRecords);
    } catch (Exception e) {
        return error("查詢歸還記錄失敗：" + e.getMessage());
    }
}
```

---

### 4. ✅ 前端顯示歸還記錄

#### 新增「歸還記錄」按鈕
**檔案**：`cheng-ui/src/views/inventory/borrow/index.vue`

**位置**：操作欄

```vue
<!-- 已歸還或部分歸還狀態顯示查看歸還記錄按鈕 -->
<el-button
  size="mini"
  type="text"
  icon="el-icon-document"
  @click="handleViewReturnRecords(scope.row)"
  v-if="scope.row.status === '3' || scope.row.status === '4'"
  v-hasPermi="['inventory:borrow:query']"
>歸還記錄
</el-button>
```

**顯示條件**：
- 狀態為「已歸還」(3) 或「部分歸還」(4)
- 需要 `inventory:borrow:query` 權限

#### 歸還記錄對話框
```vue
<!-- 歸還記錄對話框 -->
<el-dialog title="歸還記錄" :visible.sync="returnRecordsOpen" width="800px" append-to-body>
  <el-table :data="returnRecords" style="width: 100%">
    <el-table-column label="歸還時間" align="center" prop="returnTime" width="160"/>
    <el-table-column label="歸還數量" align="center" prop="returnQuantity" width="100"/>
    <el-table-column label="物品狀態" align="center" width="100">
      <template slot-scope="scope">
        <el-tag v-if="scope.row.itemCondition === 'good'" type="success">完好</el-tag>
        <el-tag v-else-if="scope.row.itemCondition === 'damaged'" type="warning">損壞</el-tag>
        <el-tag v-else-if="scope.row.itemCondition === 'lost'" type="danger">遺失</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="是否逾期" align="center" width="100">
      <template slot-scope="scope">
        <el-tag v-if="scope.row.isOverdue === '1'" type="danger">逾期 {{ scope.row.overdueDays }} 天</el-tag>
        <el-tag v-else type="success">準時</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="說明" align="center" prop="remark" :show-overflow-tooltip="true"/>
  </el-table>
  <div slot="footer" class="dialog-footer">
    <el-button @click="returnRecordsOpen = false">關 閉</el-button>
  </div>
</el-dialog>
```

#### 查詢歸還記錄方法
```javascript
/** 查看歸還記錄 */
handleViewReturnRecords(row) {
  this.$axios.get(`/inventory/borrow/returnRecords/${row.borrowId}`).then(response => {
    if (response.code === 200) {
      this.returnRecords = response.data || [];
      this.returnRecordsOpen = true;
    } else {
      this.$modal.msgError(response.msg || '查詢歸還記錄失敗');
    }
  }).catch(error => {
    console.error('查詢歸還記錄失敗:', error);
    this.$modal.msgError('查詢歸還記錄失敗');
  });
}
```

---

## 完整流程示意圖

### 借出 3 個物品的歸還流程

```
借出記錄：借出 3 個物品
   ↓
第 1 次歸還：1 個完好
   ├─ 借出記錄更新：returnQuantity = 1, status = 部分歸還
   ├─ 庫存更新：可用 +1, 借出 -1
   └─ 建立歸還記錄 #1：數量=1, 狀態=good
   ↓
第 2 次歸還：1 個損壞
   ├─ 借出記錄更新：returnQuantity = 2, status = 部分歸還
   ├─ 庫存更新：損壞 +1, 借出 -1
   └─ 建立歸還記錄 #2：數量=1, 狀態=damaged
   ↓
第 3 次歸還：1 個遺失
   ├─ 借出記錄更新：returnQuantity = 3, status = 已歸還
   ├─ 庫存更新：總數 -1, 遺失 +1, 借出 -1
   └─ 建立歸還記錄 #3：數量=1, 狀態=lost
   ↓
點擊「歸還記錄」按鈕
   ↓
顯示 3 筆歸還記錄：
   - 記錄 #1：1 個完好 ✅
   - 記錄 #2：1 個損壞 ⚠️
   - 記錄 #3：1 個遺失 ❌
```

---

## 修改的檔案清單

### 資料庫
1. ✅ `sql/add_lost_qty.sql` - 新增遺失數量欄位（新增）

### 後端
1. ✅ `InvStock.java` - 新增 lostQty 欄位
2. ✅ `InvItemWithStockDTO.java` - 新增 lostQty 欄位
3. ✅ `InvItemServiceImpl.java` - 初始化 lostQty
4. ✅ `BookItemServiceImpl.java` - 初始化 lostQty
5. ✅ `InvBorrowServiceImpl.java` - 修正遺失物品邏輯 + 建立歸還記錄
6. ✅ `InvBorrowController.java` - 新增查詢歸還記錄 API

### 前端
1. ✅ `cheng-ui/src/views/inventory/borrow/index.vue` - 新增歸還記錄按鈕和對話框

---

## 測試驗證

### 測試 1：遺失物品庫存處理
1. 記錄初始庫存（例如：總數=10, 可用=7, 借出=3）
2. 借出 2 個物品 → 可用=5, 借出=5
3. 審核通過
4. 歸還 1 個「遺失」
5. ✅ 檢查庫存：
   - 總數量：9（-1）
   - 可用數量：5（不變）
   - 借出數量：4（-1）
   - 遺失數量：1（+1）

### 測試 2：歸還記錄顯示
1. 借出 3 個物品
2. 審核通過
3. 分別歸還：
   - 第 1 次：1 個完好
   - 第 2 次：1 個損壞
   - 第 3 次：1 個遺失
4. ✅ 點擊「歸還記錄」按鈕
5. ✅ 確認顯示 3 筆記錄，狀態正確標示

### 測試 3：部分歸還顯示
1. 借出 3 個物品
2. 只歸還 1 個完好
3. ✅ 狀態變為「部分歸還」
4. ✅ 可以看到「歸還記錄」按鈕
5. ✅ 點擊顯示 1 筆記錄
6. ✅ 仍可以繼續歸還剩餘物品

---

## 優化效益

### 1. 庫存準確性提升
- 遺失物品正確從總數量扣除
- 避免庫存數據不一致
- 損壞、遺失數據獨立追蹤

### 2. 可追溯性
- 每次歸還都有完整記錄
- 可以查看物品的狀態歷史
- 逾期資訊自動記錄

### 3. 責任歸屬明確
- 知道哪些物品在誰手中遺失或損壞
- 賠償追蹤有據可查
- 物品管理更嚴謹

---

## 相關文件

- 借出審核流程：`docs/BORROW_APPROVAL_CHANGES.md`
- 借出功能修正：`docs/BORROW_BUGFIX.md`
- 庫存優化：`docs/INVENTORY_OPTIMIZATION.md`

---

**修改完成日期**: 2025-10-07  
**修改人員**: Cascade AI Assistant  
**狀態**: 已完成，需執行資料庫變更並測試
