# 歸還功能錯誤修正

## 修正日期
2025-10-07 14:46

---

## 錯誤說明

### 錯誤 1：選擇「損壞」或「完好」時的資料庫錯誤
```
Error: Unknown column 'borrow_code' in 'field list'
```

**原因**：
- 資料表 `inv_return` 使用 `return_no`、`quantity`、`returner_id` 等欄位名稱
- 但 `InvReturnMapper.xml` 使用 `borrow_code`、`return_quantity`、`borrower_id` 等
- Domain 類別 `InvReturn.java` 的屬性與 Mapper.xml 一致，但與資料表不一致

### 錯誤 2：選擇「遺失」時的 NullPointerException
```
Cannot invoke "java.lang.Integer.intValue()" because the return value of 
"com.cheng.system.domain.InvStock.getLostQty()" is null
```

**原因**：
- 執行 `ALTER TABLE inv_stock ADD COLUMN lost_qty` 後
- 現有記錄的 `lost_qty` 欄位值為 NULL
- 程式碼直接對 NULL 值進行算術運算導致錯誤

---

## 解決方案

### 方案概述
1. 修正資料表結構，使其符合 Mapper.xml 的期望
2. 更新現有資料的 NULL 值
3. 修正程式碼處理 NULL 值的邏輯

### 詳細修正

#### 1. 資料庫修正

**執行檔案**：`sql/fix_return_table.sql`

**修正內容**：

##### a) 修正 `inv_stock` 的 `lost_qty`
```sql
UPDATE inv_stock SET lost_qty = 0 WHERE lost_qty IS NULL;
```

##### b) 修正 `inv_return` 表結構
- `return_no` → `borrow_code`
- `quantity` → `return_quantity`
- `returner_id` → `borrower_id`
- `returner_name` → `borrower_name`
- `condition_desc` → `item_condition`
- `damage_desc` → `damage_description`
- 移除 `is_damaged` 欄位（已整合到 `item_condition`）
- 新增缺少的欄位：`expected_return`, `is_overdue`, `overdue_days`, `compensation_amount`, `return_status`, `update_by`, `update_time`

**執行方式**：
```bash
mysql -u root -p your_database < sql/fix_return_table.sql
```

#### 2. 程式碼修正

##### a) 處理 `lostQty` 為 NULL 的情況

**檔案**：`InvBorrowServiceImpl.java`

**修改前**：
```java
if ("lost".equals(conditionDesc) || "遺失".equals(conditionDesc)) {
    stock.setTotalQuantity(stock.getTotalQuantity() - returnQuantity);
    stock.setLostQty(stock.getLostQty() + returnQuantity); // 如果 getLostQty() 返回 null 會報錯
}
```

**修改後**：
```java
if ("lost".equals(conditionDesc) || "遺失".equals(conditionDesc)) {
    stock.setTotalQuantity(stock.getTotalQuantity() - returnQuantity);
    // 處理 lostQty 可能為 null 的情況
    Integer currentLostQty = stock.getLostQty();
    stock.setLostQty((currentLostQty != null ? currentLostQty : 0) + returnQuantity);
}
```

##### b) 移除不存在欄位的設定

**檔案**：`InvBorrowServiceImpl.java`

**修改前**：
```java
invReturn.setItemId(borrow.getItemId());
invReturn.setItemCode(borrow.getItemCode());  // 資料表中不存在，透過 JOIN 取得
invReturn.setItemName(borrow.getItemName());  // 資料表中不存在，透過 JOIN 取得
invReturn.setReturnQuantity(returnQuantity.longValue());
```

**修改後**：
```java
invReturn.setItemId(borrow.getItemId());
// itemCode 和 itemName 透過 JOIN 從 inv_item 表取得，不需要手動設定
invReturn.setReturnQuantity(returnQuantity.longValue());
```

---

## 資料表結構對照

### 修正前（原始 SQL 定義）
```sql
CREATE TABLE inv_return (
    return_id      BIGINT(20)   NOT NULL AUTO_INCREMENT,
    borrow_id      BIGINT(20)   NOT NULL,
    return_no      VARCHAR(50)  NOT NULL,    -- ❌ 與 Mapper 不一致
    item_id        BIGINT(20)   NOT NULL,
    returner_id    BIGINT(20)   NOT NULL,    -- ❌ 與 Mapper 不一致
    returner_name  VARCHAR(50)  NOT NULL,    -- ❌ 與 Mapper 不一致
    quantity       INT(11)      NOT NULL,    -- ❌ 與 Mapper 不一致
    return_time    DATETIME     NOT NULL,
    condition_desc VARCHAR(200),             -- ❌ 與 Mapper 不一致
    is_damaged     CHAR(1),                  -- ❌ 多餘欄位
    damage_desc    VARCHAR(200),             -- ❌ 與 Mapper 不一致
    receiver_id    BIGINT(20),
    receiver_name  VARCHAR(50),
    create_by      VARCHAR(64),
    create_time    DATETIME,
    remark         VARCHAR(500),
    PRIMARY KEY (return_id)
);
```

### 修正後（符合 Mapper.xml）
```sql
CREATE TABLE inv_return (
    return_id            BIGINT(20)    NOT NULL AUTO_INCREMENT,
    borrow_id            BIGINT(20)    NOT NULL,
    borrow_code          VARCHAR(50)   NOT NULL,    -- ✅ 修正
    item_id              BIGINT(20)    NOT NULL,
    borrower_id          BIGINT(20)    NOT NULL,    -- ✅ 修正
    borrower_name        VARCHAR(50)   NOT NULL,    -- ✅ 修正
    return_quantity      INT(11)       NOT NULL,    -- ✅ 修正
    return_time          DATETIME      NOT NULL,
    expected_return      DATETIME,                  -- ✅ 新增
    is_overdue           CHAR(1)       DEFAULT '0', -- ✅ 新增
    overdue_days         BIGINT(20)    DEFAULT 0,   -- ✅ 新增
    item_condition       VARCHAR(50),               -- ✅ 修正
    damage_description   VARCHAR(200),              -- ✅ 修正
    compensation_amount  DECIMAL(10,2),             -- ✅ 新增
    receiver_id          BIGINT(20),
    receiver_name        VARCHAR(50),
    return_status        CHAR(1)       DEFAULT '1', -- ✅ 新增
    create_by            VARCHAR(64),
    create_time          DATETIME,
    update_by            VARCHAR(64),               -- ✅ 新增
    update_time          DATETIME,                  -- ✅ 新增
    remark               VARCHAR(500),
    PRIMARY KEY (return_id),
    KEY idx_borrow_code (borrow_code),              -- ✅ 新增
    KEY idx_borrower_id (borrower_id)               -- ✅ 新增
);
```

---

## 欄位映射說明

| 資料表欄位 (修正前) | 資料表欄位 (修正後) | Java 屬性 | 說明 |
|-------------------|-------------------|----------|------|
| `return_no` | `borrow_code` | `borrowCode` | 借出單號 |
| `quantity` | `return_quantity` | `returnQuantity` | 歸還數量 |
| `returner_id` | `borrower_id` | `borrowerId` | 借用人ID |
| `returner_name` | `borrower_name` | `borrowerName` | 借用人姓名 |
| `condition_desc` | `item_condition` | `itemCondition` | 物品狀態 |
| `damage_desc` | `damage_description` | `damageDescription` | 損壞描述 |
| `is_damaged` | ❌ 刪除 | - | 已整合到 item_condition |
| - | `expected_return` ✅ | `expectedReturn` | 預計歸還時間 |
| - | `is_overdue` ✅ | `isOverdue` | 是否逾期 |
| - | `overdue_days` ✅ | `overdueDays` | 逾期天數 |
| - | `compensation_amount` ✅ | `compensationAmount` | 賠償金額 |
| - | `return_status` ✅ | `returnStatus` | 歸還狀態 |
| - | `update_by` ✅ | `updateBy` | 更新者 |
| - | `update_time` ✅ | `updateTime` | 更新時間 |

---

## 測試驗證

### 測試 1：歸還完好物品
1. 借出 2 個物品
2. 審核通過
3. 歸還 1 個「完好」
4. ✅ 應該成功，沒有錯誤
5. ✅ 庫存：可用 +1
6. ✅ 歸還記錄：item_condition = 'good'

### 測試 2：歸還損壞物品
1. 借出 2 個物品
2. 審核通過
3. 歸還 1 個「損壞」
4. ✅ 應該成功，沒有錯誤
5. ✅ 庫存：損壞 +1，可用不變
6. ✅ 歸還記錄：item_condition = 'damaged'

### 測試 3：歸還遺失物品
1. 借出 2 個物品
2. 審核通過
3. 歸還 1 個「遺失」
4. ✅ 應該成功，沒有錯誤
5. ✅ 庫存：總數 -1，遺失 +1，可用不變
6. ✅ 歸還記錄：item_condition = 'lost'

### 測試 4：查看歸還記錄
1. 完成多次歸還（包含完好、損壞、遺失）
2. 點擊「歸還記錄」按鈕
3. ✅ 應該顯示所有歸還記錄
4. ✅ 每筆記錄顯示正確的物品狀態

---

## 修改的檔案清單

### SQL 腳本
1. ✅ `sql/fix_return_table.sql` - 資料表結構修正腳本（新增）

### 後端
1. ✅ `InvBorrowServiceImpl.java` - 修正 lostQty NULL 處理，移除不存在欄位的設定

### 文件
1. ✅ `docs/BUGFIX_RETURN_ERRORS.md` - 錯誤修正文件（新增）

---

## ⚠️ 重要提醒

### 部署步驟（必須按順序執行）

#### 1. 備份資料庫
```bash
mysqldump -u root -p your_database > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### 2. 執行修正腳本
```bash
mysql -u root -p your_database < sql/add_lost_qty.sql
mysql -u root -p your_database < sql/fix_return_table.sql
```

#### 3. 重新編譯後端
```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
./mvnw clean package
```

#### 4. 重啟服務
```bash
# 停止舊服務
pkill -f cheng-admin

# 啟動新服務
java -jar cheng-admin/target/cheng-admin.jar
```

#### 5. 清除前端快取
在瀏覽器中按 `Ctrl+Shift+R` 強制重新整理

---

## 預期效果

### 修正前
- ❌ 歸還「損壞」：SQLSyntaxErrorException: Unknown column 'borrow_code'
- ❌ 歸還「完好」：SQLSyntaxErrorException: Unknown column 'borrow_code'
- ❌ 歸還「遺失」：NullPointerException: getLostQty() is null

### 修正後
- ✅ 歸還「損壞」：成功，損壞數量 +1
- ✅ 歸還「完好」：成功，可用數量 +1
- ✅ 歸還「遺失」：成功，總數量 -1，遺失數量 +1
- ✅ 可查看歸還記錄明細

---

## 根本原因分析

### 為什麼會出現這個問題？

1. **設計階段的不一致**
   - 資料表 SQL 定義使用一套命名規則（如 `return_no`、`quantity`）
   - Domain 類別和 Mapper.xml 使用另一套命名規則（如 `borrowCode`、`returnQuantity`）
   - 兩者沒有統一

2. **開發流程問題**
   - 先建立了資料表
   - 後來建立 Domain 類別和 Mapper.xml 時沒有檢查資料表實際結構
   - 直接使用了不同的欄位名稱

3. **測試不充分**
   - 歸還功能在本次修改前沒有實際使用
   - 如果有單元測試或整合測試，可以更早發現這個問題

### 預防措施

1. **開發規範**
   - 資料表欄位命名應該與 Java 屬性命名規則一致
   - 使用工具自動產生 Domain 類別和 Mapper.xml（如 MyBatis Generator）

2. **程式碼審查**
   - 新增功能時檢查資料表結構與程式碼的一致性
   - 特別注意 MyBatis Mapper.xml 的欄位映射

3. **測試覆蓋**
   - 為所有 CRUD 操作編寫單元測試
   - 執行整合測試確保資料庫操作正常

---

**修正完成日期**: 2025-10-07  
**修正人員**: Cascade AI Assistant  
**狀態**: 已完成，需執行 SQL 腳本並重啟服務
