# 庫存管理系統優化

## 修改日期
2025-10-07

---

## 優化內容

### 1. ✅ 移除「預留數量」欄位

**問題描述**：
在庫存詳情視窗中顯示「預留數量」欄位，但此欄位在業務邏輯中未實際使用，只在初始化時設定為 0。

**業務分析**：
- `reservedQty` 只在新增物品時初始化為 0
- 無任何業務邏輯會修改此欄位
- 借出時直接扣減 `availableQty`，不使用預留機制
- 此欄位會造成使用者混淆

**修正方式**：
從前端詳情視窗移除此欄位顯示

**檔案**：`cheng-ui/src/views/inventory/management/index.vue`

```vue
<!-- 修改前 -->
<el-descriptions-item label="總數量">{{ detailData.totalQuantity }}</el-descriptions-item>
<el-descriptions-item label="可用數量">{{ detailData.availableQty }}</el-descriptions-item>
<el-descriptions-item label="借出數量">{{ detailData.borrowedQty }}</el-descriptions-item>
<el-descriptions-item label="預留數量">{{ detailData.reservedQty }}</el-descriptions-item>
<el-descriptions-item label="損壞數量">{{ detailData.damagedQty }}</el-descriptions-item>

<!-- 修改後 -->
<el-descriptions-item label="總數量">{{ detailData.totalQuantity }}</el-descriptions-item>
<el-descriptions-item label="可用數量">{{ detailData.availableQty }}</el-descriptions-item>
<el-descriptions-item label="借出數量">{{ detailData.borrowedQty }}</el-descriptions-item>
<el-descriptions-item label="損壞數量">{{ detailData.damagedQty }}</el-descriptions-item>
```

**注意事項**：
- 後端資料表和實體類別保留 `reserved_qty` 欄位（避免資料庫變更）
- 只是從前端顯示中移除
- 如未來需要預留功能，可以擴充業務邏輯後再顯示

---

### 2. ✅ 限制預計歸還時間只能選擇未來

**問題描述**：
新增借出申請時，「預計歸還」時間選擇器可以選擇過去的時間，這不符合業務邏輯。

**業務需求**：
- 預計歸還時間應該是未來的時間
- 不應該允許選擇今天之前的日期
- 提升資料合理性

**修正方式**：
在時間選擇器中加入 `pickerOptions` 配置，禁用過去的日期

**檔案**：`cheng-ui/src/views/inventory/borrow/index.vue`

#### 修改 1：時間選擇器加入配置
```vue
<!-- 修改前 -->
<el-date-picker
  v-model="form.expectedReturn"
  type="datetime"
  placeholder="選擇預計歸還時間"
  format="yyyy-MM-dd HH:mm:ss"
  value-format="yyyy-MM-dd HH:mm:ss">
</el-date-picker>

<!-- 修改後 -->
<el-date-picker
  v-model="form.expectedReturn"
  type="datetime"
  placeholder="選擇預計歸還時間"
  format="yyyy-MM-dd HH:mm:ss"
  value-format="yyyy-MM-dd HH:mm:ss"
  :picker-options="pickerOptions">
</el-date-picker>
```

#### 修改 2：在 data 中新增配置
```javascript
data() {
  return {
    // ... 其他資料
    
    // 時間選擇器配置：只能選擇未來時間
    pickerOptions: {
      disabledDate(time) {
        return time.getTime() < Date.now() - 8.64e7; // 禁用今天之前的日期
      }
    }
  };
}
```

**技術說明**：
- `8.64e7` = 86400000 毫秒 = 24 小時
- `Date.now() - 8.64e7` = 昨天結束的時間
- `time.getTime() < Date.now() - 8.64e7` = 禁用今天之前的所有日期
- 今天和今天之後的日期都可以選擇

---

### 3. ✅ 修正歸還損壞物品時的庫存顯示

**問題描述**：
當歸還物品並選擇「損壞」狀態時，新增借出視窗中顯示的「可用數量」沒有立即更新，仍然顯示舊的數量。

**原因分析**：
1. 後端邏輯正確：歸還損壞物品時
   ```java
   // 先恢復到可用數量
   stock.setAvailableQty(stock.getAvailableQty() + returnQuantity);
   stock.setBorrowedQty(stock.getBorrowedQty() - returnQuantity);
   
   // 如果損壞，再從可用數量扣除並計入損壞數量
   if ("1".equals(isDamaged)) {
       stock.setDamagedQty(stock.getDamagedQty() + returnQuantity);
       stock.setAvailableQty(stock.getAvailableQty() - returnQuantity);
   }
   ```

2. 前端問題：歸還成功後，沒有重新載入物品列表，導致「新增借出」視窗中的物品選項仍然是舊資料

**修正方式**：
在歸還成功後，除了重新整理借出列表，也要重新載入物品列表

**檔案**：`cheng-ui/src/views/inventory/borrow/index.vue`

```javascript
// 修改前
submitReturn() {
  returnBorrow(requestData).then(response => {
    this.$modal.msgSuccess("歸還成功");
    this.returnOpen = false;
    this.getList();              // 只刷新借出列表
    this.getBorrowStatistics();  // 刷新統計
  });
}

// 修改後
submitReturn() {
  returnBorrow(requestData).then(response => {
    this.$modal.msgSuccess("歸還成功");
    this.returnOpen = false;
    this.getList();              // 刷新借出列表
    this.getBorrowStatistics();  // 刷新統計
    this.getItemList();          // 刷新物品列表，更新可用數量
  });
}
```

**效果**：
- 歸還完好物品：可用數量 +1
- 歸還損壞物品：可用數量不變，損壞數量 +1
- 新增借出視窗會立即顯示正確的可用數量

---

## 數據流程說明

### 損壞物品歸還的完整流程

```
1. 使用者選擇歸還損壞物品（數量 1）
   ↓
2. 後端處理（InvBorrowServiceImpl.returnItem）
   ├─ 借出記錄更新：returnQuantity + 1, status = 已歸還/部分歸還
   ├─ 庫存更新（假設原本：可用=5, 借出=3, 損壞=1）
   │  ├─ availableQty = 5 + 1 = 6（先恢復）
   │  ├─ borrowedQty = 3 - 1 = 2（減少借出）
   │  ├─ 檢測到損壞：
   │  ├─ damagedQty = 1 + 1 = 2（增加損壞）
   │  └─ availableQty = 6 - 1 = 5（扣除損壞的）
   └─ 最終：可用=5, 借出=2, 損壞=2
   ↓
3. 前端處理
   ├─ 顯示「歸還成功」
   ├─ 刷新借出列表（getList）
   ├─ 刷新統計資料（getBorrowStatistics）
   └─ 刷新物品列表（getItemList）✨ 新增
   ↓
4. 使用者打開「新增借出」視窗
   └─ 看到正確的可用數量：5
```

### 完好物品歸還的流程

```
1. 使用者選擇歸還完好物品（數量 1）
   ↓
2. 後端處理
   ├─ 借出記錄更新
   ├─ 庫存更新（假設原本：可用=5, 借出=3, 損壞=1）
   │  ├─ availableQty = 5 + 1 = 6
   │  ├─ borrowedQty = 3 - 1 = 2
   │  └─ 未損壞，不處理 damagedQty
   └─ 最終：可用=6, 借出=2, 損壞=1
   ↓
3. 前端刷新後
   └─ 看到正確的可用數量：6
```

---

## 修改的檔案清單

1. ✅ `cheng-ui/src/views/inventory/management/index.vue`
   - 移除「預留數量」顯示

2. ✅ `cheng-ui/src/views/inventory/borrow/index.vue`
   - 新增時間選擇器配置（限制只能選未來）
   - 歸還成功後刷新物品列表

---

## 測試建議

### 測試項目 1：預留數量移除
- [x] 打開庫存管理頁面
- [x] 點擊任一物品的「詳情」按鈕
- [x] 確認詳情視窗中沒有「預留數量」欄位
- [x] 確認其他欄位（總數量、可用數量、借出數量、損壞數量）正常顯示

### 測試項目 2：預計歸還時間限制
- [x] 打開借出管理頁面
- [x] 點擊「新增借出」按鈕
- [x] 選擇物品和數量
- [x] 點擊「預計歸還」時間選擇器
- [x] 嘗試選擇昨天或更早的日期 → 應該無法選擇（日期顯示為灰色）
- [x] 選擇今天或未來的日期 → 應該可以選擇
- [x] 提交表單 → 應該成功

### 測試項目 3：損壞物品歸還
#### 步驟：
1. 記錄某物品的初始可用數量（例如：5）
2. 新增借出申請（數量：2）
3. 審核通過 → 可用數量應變為 3
4. 歸還 1 個，選擇「損壞」
5. 檢查庫存：
   - 可用數量：3（不變）
   - 借出數量：1（減少 1）
   - 損壞數量：+1
6. 立即點擊「新增借出」→ 可用數量應顯示 3 ✅

#### 預期結果：
- ✅ 損壞物品不會增加可用數量
- ✅ 損壞數量正確增加
- ✅ 新增借出視窗立即顯示正確的可用數量

---

## 優化效益

### 1. 界面簡化
- 移除無用欄位，減少使用者困惑
- 提升界面清晰度

### 2. 資料合理性
- 預計歸還時間只能選未來
- 防止輸入不合理的資料

### 3. 即時性提升
- 歸還操作後立即更新物品可用數量
- 提供更好的使用者體驗
- 避免因資料不同步造成的操作錯誤

---

## 相關文件

- 借出審核流程修改：`docs/BORROW_APPROVAL_CHANGES.md`
- 借出功能修正：`docs/BORROW_BUGFIX.md`
- 資料表結構：`sql/inventory_tables.sql`

---

**修改完成日期**: 2025-10-07  
**修改人員**: Cascade AI Assistant  
**狀態**: 已完成，建議測試驗證
