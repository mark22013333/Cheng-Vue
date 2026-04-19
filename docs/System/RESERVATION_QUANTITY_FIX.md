# 預約數量邏輯修復說明

## 🐛 問題描述

### 原始問題
當物品有多個（例如總數量 = 2）時：
- admin 借出了 1 個
- testuser 再預約 1 個時失敗
- 錯誤訊息：「該物品在選擇的時間段已有 1 個預約，請選擇其他時間」

### 根本原因
時間重疊檢查邏輯錯誤：
- **舊邏輯**：只要發現有重疊時間的預約記錄就拒絕
- **問題**：沒有考慮物品的總數量，無法支援多人同時預約不同數量

---

## ✅ 解決方案

### 核心邏輯調整

**舊邏輯**：
```java
// 統計已確認的預約數量（記錄數）
int confirmedReservations = (int) existingReservations.stream()
    .filter(r -> r.getReserveStatus() == 2)
    .count();  // ❌ 只計算記錄數，不是總數量

if (confirmedReservations > 0) {
    throw new ServiceException("該物品在選擇的時間段已有預約");
}
```

**新邏輯**：
```java
// 1. 取得物品總數量
int totalQuantity = item.getTotalQuantity();

// 2. 計算重疊時間段的總預約數量（SUM of quantity）
Integer overlappingQuantity = invBorrowMapper.sumOverlappingReservationQuantity(
    itemId, startDate, endDate);

// 3. 計算剩餘可預約數量
int availableForReservation = totalQuantity - overlappingQuantity;

// 4. 檢查是否足夠
if (availableForReservation < request.getBorrowQty()) {
    throw new ServiceException(
        String.format("預約失敗：該物品在選擇的時間段剩餘可預約數量為 %d，您想預約 %d，數量不足",
            availableForReservation, request.getBorrowQty()));
}
```

---

## 📝 修改內容

### 1. Mapper 介面新增方法

**檔案**：`InvBorrowMapper.java`

```java
/**
 * 計算重疊時間段的總預約數量
 * 
 * @param itemId 物品ID
 * @param startDate 開始日期
 * @param endDate 結束日期
 * @return 重疊時間段的總預約數量
 */
Integer sumOverlappingReservationQuantity(@Param("itemId") Long itemId, 
                                         @Param("startDate") Date startDate, 
                                         @Param("endDate") Date endDate);
```

### 2. Mapper XML 實作 SQL

**檔案**：`InvBorrowMapper.xml`

```xml
<!-- 計算重疊時間段的總預約數量 -->
<select id="sumOverlappingReservationQuantity" resultType="java.lang.Integer">
    select COALESCE(SUM(b.quantity), 0)
    from inv_borrow b
    where b.item_id = #{itemId}
      and b.reserve_status in (1, 2)  <!-- 待審核(1)和已確認(2)的預約 -->
      and b.status not in ('2', '3')  <!-- 排除審核拒絕(2)和已歸還(3)的記錄 -->
      and (
          <!-- 新預約的開始日期在現有預約期間內 -->
          <![CDATA[ (#{startDate} >= b.reserve_start_date and #{startDate} <= b.reserve_end_date) ]]>
          or
          <!-- 新預約的結束日期在現有預約期間內 -->
          <![CDATA[ (#{endDate} >= b.reserve_start_date and #{endDate} <= b.reserve_end_date) ]]>
          or
          <!-- 新預約完全包含現有預約 -->
          <![CDATA[ (#{startDate} <= b.reserve_start_date and #{endDate} >= b.reserve_end_date) ]]>
      )
</select>
```

**關鍵點**：
- 使用 `SUM(b.quantity)` 而非 `COUNT(*)`
- `COALESCE(..., 0)` 確保沒有記錄時返回 0 而非 NULL

### 3. Service 層邏輯調整

**檔案**：`InvItemServiceImpl.java`

**修改內容**：
1. 先取得物品資訊，包含總數量
2. 計算重疊時間段的總預約數量
3. 計算剩餘可預約數量 = 總數量 - 已預約總數量
4. 檢查剩餘可預約數量是否足夠
5. 新增詳細日誌記錄

---

## 🧪 測試案例

### 測試 1：單一物品多人預約（核心修復）

**前置條件**：
- 物品 A：總數量 = 2
- 預約時間：2025-12-04 ~ 2026-01-03

**步驟**：
1. admin 預約物品 A，數量 = 1
2. admin 審核通過
3. testuser 預約物品 A，數量 = 1（時間重疊）

**預期結果**：
- ✅ testuser 預約成功
- ✅ 日誌顯示：
  ```
  預約檢查 - 物品總數: 2, 已預約數量: 1, 剩餘可預約: 1, 本次預約: 1
  ```

### 測試 2：預約數量超過剩餘可用

**前置條件**：
- 物品 A：總數量 = 2
- admin 已預約 1 個

**步驟**：
1. testuser 預約物品 A，數量 = 2（超過剩餘）

**預期結果**：
- ❌ 預約失敗
- 錯誤訊息：「預約失敗：該物品在選擇的時間段剩餘可預約數量為 1，您想預約 2，數量不足」

### 測試 3：多人預約耗盡庫存

**前置條件**：
- 物品 A：總數量 = 3

**步驟**：
1. admin 預約 1 個 → 成功
2. testuser 預約 1 個 → 成功
3. user3 預約 1 個 → 成功
4. user4 預約 1 個 → 失敗（剩餘 0）

**預期結果**：
- ✅ 前 3 人預約成功
- ❌ user4 預約失敗，提示「剩餘可預約數量為 0」

### 測試 4：部分時間重疊

**前置條件**：
- 物品 A：總數量 = 2

**步驟**：
1. admin 預約：12/04 ~ 12/10，數量 = 1
2. testuser 預約：12/08 ~ 12/15，數量 = 1（部分重疊）

**預期結果**：
- ✅ testuser 預約成功
- 因為在重疊時間段（12/08 ~ 12/10）內，總數量 = 2，已預約 = 1，剩餘 = 1

### 測試 5：完全包含的時間段

**前置條件**：
- 物品 A：總數量 = 2

**步驟**：
1. admin 預約：12/04 ~ 12/20，數量 = 1
2. testuser 預約：12/08 ~ 12/15，數量 = 1（完全包含在 admin 的時間內）

**預期結果**：
- ✅ testuser 預約成功

### 測試 6：審核拒絕後可再預約

**前置條件**：
- 物品 A：總數量 = 1

**步驟**：
1. admin 預約：12/04 ~ 12/20，數量 = 1
2. 管理員審核拒絕
3. testuser 預約：12/04 ~ 12/20，數量 = 1（時間重疊）

**預期結果**：
- ✅ testuser 預約成功
- 因為 admin 的預約被拒絕，status = '2'，不計入重疊檢查

---

## 📊 邏輯流程圖

```
開始預約
    ↓
檢查日期有效性
    ↓
取得物品總數量（totalQuantity）
    ↓
計算重疊時間段的總預約數量（overlappingQuantity）
    ↓
計算剩餘可預約數量
availableForReservation = totalQuantity - overlappingQuantity
    ↓
判斷：availableForReservation >= 請求數量？
    ├─ 是 → 繼續預約流程
    └─ 否 → 拋出異常：數量不足
```

---

## 🔍 關鍵 SQL 說明

### 重疊時間判斷邏輯

三種重疊情況：

1. **新預約的開始日期在現有預約期間內**
   ```
   現有預約：|-----------|
   新預約：       |----------|
                  ↑ 開始日期在期間內
   ```

2. **新預約的結束日期在現有預約期間內**
   ```
   現有預約：    |-----------|
   新預約：  |----------|
                        ↑ 結束日期在期間內
   ```

3. **新預約完全包含現有預約**
   ```
   現有預約：    |-----|
   新預約：  |-------------|
             ↑             ↑
   ```

### 排除條件

- `reserve_status in (1, 2)`：只計算待審核(1)和已確認(2)的預約
- `status not in ('2', '3')`：排除審核拒絕(2)和已歸還(3)的記錄

---

## ⚠️ 注意事項

### 1. 併發安全

雖然使用樂觀鎖，但在計算剩餘可預約數量時存在時間窗口：
- **時刻 1**：查詢剩餘可預約數量 = 1
- **時刻 2**：另一個使用者也查詢到剩餘 = 1
- **時刻 3**：兩人同時預約，可能導致超額

**緩解措施**：
- 使用樂觀鎖更新庫存
- 如果庫存更新失敗，拋出異常回滾交易

### 2. 待審核預約的處理

- 待審核預約（reserve_status = 1）也計入重疊數量
- 這確保在審核前不會超額預約
- 如果預約被拒絕，數量會自動釋放

### 3. 已歸還記錄的處理

- 已歸還的記錄（status = '3'）不計入重疊
- 確保已歸還的物品可以再次被預約

---

## 🎯 效益

### 功能改進
- ✅ 支援多人同時預約不同數量
- ✅ 正確計算剩餘可預約數量
- ✅ 提供清晰的錯誤訊息

### 使用者體驗
- ✅ 避免誤報「已有預約」錯誤
- ✅ 明確告知剩餘可預約數量
- ✅ 提高系統可用性

### 維護性
- ✅ 邏輯清晰，易於理解
- ✅ 詳細日誌便於追蹤問題
- ✅ SQL 可複用於其他查詢

---

## 📋 修改檔案清單

- ✅ `InvBorrowMapper.java` - 新增計算總數量方法
- ✅ `InvBorrowMapper.xml` - 實作 SQL 查詢
- ✅ `InvItemServiceImpl.java` - 調整預約邏輯

---

## 🚀 部署建議

1. **備份資料庫**：修改前先備份 `inv_borrow` 表
2. **編譯測試**：執行 `mvn clean compile -DskipTests`
3. **重啟服務**：更新後重啟後端服務
4. **驗證功能**：按照測試案例逐一驗證
5. **監控日誌**：觀察「預約檢查」日誌確認邏輯正確

---

**修復完成！請重啟後端服務並按照測試案例進行驗證。** 🎉
