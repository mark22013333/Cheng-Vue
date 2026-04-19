# 借出管理審核流程修改摘要

## 修改日期
2025-10-07

## 修改概述
將借出管理功能從「新增時立即扣庫存」改為「提交審核，審核通過才扣庫存」的流程。

---

## 一、業務流程變更

### 修改前的流程
1. 使用者新增借出申請
2. **立即扣減庫存**（可用數量減少，借出數量增加）
3. 記錄狀態為「待審核」
4. 審核時只更新狀態，不處理庫存

### 修改後的流程
1. 使用者新增借出申請
2. **不扣減庫存**，只建立記錄
3. 記錄狀態為「待審核」
4. **審核通過時才扣減庫存**（可用數量減少，借出數量增加）
5. 審核拒絕時不扣減庫存

---

## 二、狀態定義變更

### 修改前（混亂）
- Domain 註解：`0=借出中, 1=已歸還, 2=逾期, 3=部分歸還`
- 實際使用：`0=待審核, 1=已借出, 2=已歸還, 3=已拒絕`

### 修改後（統一使用 enum）
建立 `BorrowStatus` enum，明確定義：
- `PENDING("0")` - 待審核
- `BORROWED("1")` - 已借出（審核通過）
- `REJECTED("2")` - 審核拒絕
- `RETURNED("3")` - 已歸還
- `PARTIAL_RETURNED("4")` - 部分歸還
- `OVERDUE("5")` - 逾期

---

## 三、程式碼修改清單

### 1. 新增檔案

#### `BorrowStatus.java`
**路徑**: `cheng-system/src/main/java/com/cheng/system/domain/enums/BorrowStatus.java`

**功能**: 借出狀態列舉類別
- 定義所有借出狀態的代碼和描述
- 提供狀態轉換和檢查的輔助方法
- 提高程式碼可讀性，避免魔術數字

**關鍵方法**:
```java
public boolean isPending()      // 檢查是否為待審核狀態
public boolean isBorrowed()     // 檢查是否為已借出狀態
public boolean needsReturn()    // 檢查是否需要歸還（已借出/逾期/部分歸還）
```

---

### 2. 修改 Domain 層

#### `InvBorrow.java`
**路徑**: `cheng-system/src/main/java/com/cheng/system/domain/InvBorrow.java`

**修改內容**:
1. 匯入 `BorrowStatus` enum
2. 更新狀態欄位註解，明確定義各狀態代碼
3. 新增 enum 輔助方法：
   ```java
   public BorrowStatus getStatusEnum()
   public void setStatusEnum(BorrowStatus statusEnum)
   public boolean isPending()
   public boolean isBorrowed()
   public boolean needsReturn()
   ```

---

### 3. 修改 Service 層

#### `InvBorrowServiceImpl.java`
**路徑**: `cheng-system/src/main/java/com/cheng/system/service/impl/InvBorrowServiceImpl.java`

**核心修改**:

##### a) `borrowItem()` 方法
**修改前**: 新增記錄 + **立即扣減庫存**
```java
// 新增借出記錄
int result = invBorrowMapper.insertInvBorrow(invBorrow);

// 更新庫存（立即扣減）
InvStock stock = invStockMapper.selectInvStockByItemId(invBorrow.getItemId());
stock.setAvailableQty(stock.getAvailableQty() - invBorrow.getQuantity());
stock.setBorrowedQty(stock.getBorrowedQty() + invBorrow.getQuantity());
invStockMapper.updateInvStock(stock);
```

**修改後**: 只建立記錄，**不扣減庫存**
```java
// 設定為待審核狀態
invBorrow.setStatusEnum(BorrowStatus.PENDING);

// 新增借出記錄（不扣減庫存，等待審核）
int result = invBorrowMapper.insertInvBorrow(invBorrow);
return result;
```

##### b) `approveBorrow()` 方法
**修改前**: 只更新狀態，**不處理庫存**
```java
if (isApproved) {
    borrow.setStatus("1"); // 已審核通過
} else {
    borrow.setStatus("2"); // 審核拒絕
}
return invBorrowMapper.updateInvBorrow(borrow);
```

**修改後**: 審核通過時**才扣減庫存**
```java
if (isApproved) {
    // 檢查庫存是否足夠
    if (!checkItemAvailable(borrow.getItemId(), borrow.getQuantity())) {
        throw new RuntimeException("物品庫存不足，無法審核通過");
    }
    
    // 扣減庫存
    InvStock stock = invStockMapper.selectInvStockByItemId(borrow.getItemId());
    stock.setAvailableQty(stock.getAvailableQty() - borrow.getQuantity());
    stock.setBorrowedQty(stock.getBorrowedQty() + borrow.getQuantity());
    invStockMapper.updateInvStock(stock);
    
    borrow.setStatusEnum(BorrowStatus.BORROWED);
} else {
    // 審核拒絕不扣減庫存
    borrow.setStatusEnum(BorrowStatus.REJECTED);
}
```

##### c) `returnItem()` 方法
- 使用 enum 設定狀態
- `BorrowStatus.RETURNED` - 已歸還
- `BorrowStatus.PARTIAL_RETURNED` - 部分歸還

##### d) `updateInvBorrow()` 方法
- 只有 `needsReturn()` 狀態才更新庫存
- 待審核和審核拒絕狀態不更新庫存（因為沒有扣減過）

##### e) `deleteInvBorrowByBorrowIds()` 方法
- 使用 `needsReturn()` 判斷是否需要恢復庫存
- 待審核和審核拒絕記錄刪除時不恢復庫存

---

### 4. 修改 Controller 層

#### `InvBorrowController.java`
**路徑**: `cheng-admin/src/main/java/com/cheng/web/controller/inventory/InvBorrowController.java`

**修改內容**:
1. 匯入 `BorrowStatus` enum
2. `add()` 方法：
   - 註解改為「新增借出申請（提交待審核）」
   - 使用 `invBorrow.setStatusEnum(BorrowStatus.PENDING)`
   - 移除硬編碼的 `"0"`
3. `getBorrowStats()` 方法：
   - 使用 enum 設定查詢條件
   - `pendingQuery.setStatusEnum(BorrowStatus.PENDING)`
   - `borrowedQuery.setStatusEnum(BorrowStatus.BORROWED)`

---

### 5. 修改前端

#### `index.vue`
**路徑**: `cheng-ui/src/views/inventory/borrow/index.vue`

**修改內容**:

##### a) 狀態選項更新
```vue
<!-- 修改前 -->
<el-option label="待審核" value="0"/>
<el-option label="已借出" value="1"/>
<el-option label="已歸還" value="2"/>
<el-option label="已拒絕" value="3"/>
<el-option label="逾期" value="4"/>

<!-- 修改後 -->
<el-option label="待審核" value="0"/>
<el-option label="已借出" value="1"/>
<el-option label="審核拒絕" value="2"/>
<el-option label="已歸還" value="3"/>
<el-option label="部分歸還" value="4"/>
<el-option label="逾期" value="5"/>
```

##### b) 審核表單修改
```vue
<!-- 修改前 -->
<el-radio-group v-model="approveForm.result">
  <el-radio label="1">通過</el-radio>
  <el-radio label="3">拒絕</el-radio>
</el-radio-group>

<!-- 修改後 -->
<el-radio-group v-model="approveForm.approved">
  <el-radio :label="true">通過</el-radio>
  <el-radio :label="false">拒絕</el-radio>
</el-radio-group>
```

##### c) 審核方法修改
```javascript
// 修改前
handleApprove(row) {
  this.approveForm = {
    borrowId: row.borrowId,
    result: '1',  // 字串類型
    remark: ''
  };
}

// 修改後
handleApprove(row) {
  this.approveForm = {
    borrowId: row.borrowId,
    approved: true,  // 布林類型
    remark: ''
  };
}
```

##### d) 狀態對應表更新
```javascript
// 狀態文字對應
getStatusText(status) {
  const statusMap = {
    '0': '待審核',
    '1': '已借出',
    '2': '審核拒絕',  // 新增
    '3': '已歸還',
    '4': '部分歸還',  // 新增
    '5': '逾期'       // 代碼改變
  };
  return statusMap[status] || '未知';
}

// 狀態顏色對應
getStatusType(status) {
  const statusMap = {
    '0': 'info',     // 待審核
    '1': 'primary',  // 已借出
    '2': 'danger',   // 審核拒絕
    '3': 'success',  // 已歸還
    '4': 'warning',  // 部分歸還
    '5': 'danger'    // 逾期
  };
  return statusMap[status] || 'info';
}
```

---

## 四、權限確認

### 選單權限
**檔案**: `sql/inventory_menu.sql`

審核權限已存在於系統選單中：
```sql
-- 第 107-108 行
INSERT INTO sys_menu
VALUES ('2038', '審核借出', '2003', '8', '#', '', NULL, '', 1, 0, 'F', '0', '0', 
        'inventory:borrow:approve', '#', 'admin', sysdate(), '', NULL, '');

-- 第 183-184 行：管理員角色已分配此權限
INSERT INTO sys_role_menu
VALUES ('1', '2038');
```

**權限標識**: `inventory:borrow:approve`

**使用方式**:
- 在「系統管理 > 選單管理」中可以看到「審核借出」按鈕
- 管理者可以分配此權限給特定角色
- 前端按鈕使用 `v-hasPermi="['inventory:borrow:approve']"` 控制顯示

---

## 五、影響範圍分析

### 1. 資料庫影響
- **無需修改資料表結構**
- 狀態欄位 `status` 仍為 VARCHAR 類型
- 只是狀態代碼的語意有變化

### 2. 現有資料影響
如果資料庫中已有舊資料，需要注意：
- 狀態 `0` 的舊記錄：原為「借出中」，現為「待審核」
- 狀態 `1` 的舊記錄：原為「已歸還」，現為「已借出」
- 狀態 `2` 的舊記錄：原為「逾期」，現為「審核拒絕」

**建議**: 如果有正式環境的舊資料，需要執行資料遷移腳本。

### 3. API 影響
- API 端點不變
- 請求/回應格式不變
- 只是業務邏輯變化

---

## 六、測試建議

### 1. 功能測試
- [ ] 新增借出申請，確認不扣減庫存
- [ ] 審核通過，確認庫存正確扣減
- [ ] 審核拒絕，確認庫存不變
- [ ] 修改待審核記錄，確認不影響庫存
- [ ] 刪除待審核記錄，確認不恢復庫存
- [ ] 刪除已借出記錄，確認正確恢復庫存
- [ ] 歸還功能正常運作

### 2. 權限測試
- [ ] 無審核權限的使用者看不到審核按鈕
- [ ] 有審核權限的使用者可以正常審核
- [ ] 在選單管理中可以分配審核權限

### 3. 邊界測試
- [ ] 庫存不足時新增申請（應該允許，但顯示庫存不足）
- [ ] 庫存不足時審核通過（應該拒絕，提示庫存不足）
- [ ] 重複審核（應該拒絕，只能審核待審核狀態）
- [ ] 審核後修改數量（已借出記錄修改應更新庫存）

### 4. 前端顯示測試
- [ ] 狀態標籤顯示正確
- [ ] 狀態顏色對應正確
- [ ] 審核對話框顯示正常
- [ ] 統計卡片數字正確

---

## 七、部署注意事項

### 1. 部署順序
1. 先部署後端程式碼
2. 重啟後端服務
3. 部署前端程式碼
4. 清除瀏覽器快取

### 2. 回滾方案
如果需要回滾，保留舊版本的以下檔案：
- `InvBorrowServiceImpl.java`
- `InvBorrowController.java`
- `InvBorrow.java`
- `index.vue`

### 3. 資料遷移（如有舊資料）
建議建立資料遷移腳本：
```sql
-- 將舊狀態遷移到新狀態
-- 根據實際情況調整
UPDATE inv_borrow 
SET status = '3'  -- 已歸還
WHERE status = '1' AND actual_return IS NOT NULL;

UPDATE inv_borrow 
SET status = '5'  -- 逾期
WHERE status = '2';
```

---

## 八、優點總結

### 1. 業務邏輯更合理
- 審核制度更嚴謹
- 庫存扣減時機正確
- 避免未審核就佔用庫存

### 2. 程式碼品質提升
- 使用 enum 提高可讀性
- 避免魔術數字
- 狀態定義統一明確

### 3. 可維護性提升
- 業務邏輯清晰
- 狀態流轉有跡可循
- 易於擴充和修改

---

## 九、相關文件

- 原始討論記錄：見對話歷史
- 庫存管理系統文件：`docs/`
- 選單 SQL：`sql/inventory_menu.sql`
- 資料表結構：`sql/inventory_tables.sql`

---

**修改完成日期**: 2025-10-07  
**修改人員**: Cascade AI Assistant  
**審核狀態**: 待測試驗證
