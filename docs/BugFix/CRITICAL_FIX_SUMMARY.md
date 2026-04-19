# 🔴 關鍵問題修復總結

## 發現的核心問題

### 問題現象
選擇「無庫存」時，出現 `availableQty = 104` 且顯示「正常」的資料（如 ITEM3D111）

### 根本原因：SQL NULL 值處理缺陷

當物品的 `min_stock` 欄位為 `NULL` 時（表示該物品未設定最低庫存），原有的 SQL 條件會失效：

```sql
-- 原邏輯（錯誤）
WHERE s.available_qty > i.min_stock

-- 當 min_stock = NULL 時
-- available_qty > NULL → 返回 NULL（不是 TRUE 也不是 FALSE）
-- 結果：這筆資料不會被任何條件篩選到！
```

**實際案例**：
- ITEM3D111 的 `available_qty = 104`，`min_stock = NULL`
- 查詢「無庫存」時：`104 <= 0` → FALSE ❌
- 查詢「低庫存」時：`104 <= NULL` → NULL（條件失效）❌  
- 查詢「正常」時：`104 > NULL` → NULL（條件失效）❌

結果：這筆資料在任何狀態查詢中都可能出現！

## 已實施的完整修復

### 1. XML 查詢條件重寫
**檔案**：`cheng-system/src/main/resources/mapper/system/InvItemMapper.xml`

#### 修正前後對比

| 狀態 | 修正前 | 修正後 |
|------|--------|--------|
| 無庫存 | `available_qty IS NULL OR available_qty = 0` | ✅ `available_qty IS NULL OR available_qty <= 0` |
| 低庫存 | `available_qty <= min_stock AND total_quantity > 0` | ✅ `available_qty > 0 AND min_stock IS NOT NULL AND available_qty <= min_stock` |
| 正常 | `available_qty > min_stock` | ✅ `available_qty > 0 AND (min_stock IS NULL OR available_qty > min_stock)` |

**關鍵改進**：
1. ✅ 低庫存增加 `min_stock IS NOT NULL` 檢查
2. ✅ 正常狀態使用 `min_stock IS NULL OR ...` 明確處理未設定的情況
3. ✅ 所有狀態都增加 `available_qty > 0` 前置條件（除無庫存外）

### 2. DTO 計算邏輯同步修正
**檔案**：`cheng-system/src/main/java/com/cheng/system/dto/InvItemWithStockDTO.java`

```java
// 修正後的邏輯
public void calculateStockStatus() {
    // 無庫存
    if (availableQty == null || availableQty <= 0) {
        return "無庫存";
    }
    
    // 低庫存（必須有設定 minStock）
    if (minStock != null && availableQty > 0 && availableQty <= minStock) {
        return "低庫存";
    }
    
    // 正常（無設定或高於最低庫存）
    return "正常";
}
```

### 3. 前端參數清理
**檔案**：`cheng-ui/src/views/inventory/management/index.vue`

新增 `handleStockStatusChange()` 方法，切換狀態時自動清除不相關參數。

## 邏輯決策樹

```
物品庫存狀態判斷：
│
├─ availableQty IS NULL 或 <= 0
│  └─ 🔴 無庫存
│
├─ availableQty > 0 且 minStock IS NOT NULL 且 availableQty <= minStock
│  └─ 🟡 低庫存
│
└─ availableQty > 0 且 (minStock IS NULL 或 availableQty > minStock)
   └─ 🟢 正常
```

## 必須執行的操作

### ⚠️ 重要：必須重新啟動後端！

```bash
# 方法 1：在 IDE 中
1. 停止目前執行的 Spring Boot 應用
2. Clean Project (可選)
3. 重新啟動（記得加 Jasypt 參數）

# 方法 2：使用命令列
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 測試驗證

使用提供的測試腳本：
```bash
# 1. 取得你的 Authorization Token（從瀏覽器開發者工具複製）
# 2. 執行測試腳本
./test-stock-status.sh "YOUR_TOKEN_HERE"
```

或手動測試：
```bash
# 無庫存查詢
curl "http://localhost:1024/dev-api/inventory/management/list?pageNum=1&pageSize=10&stockStatus=2" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 預期：所有 availableQty <= 0 或 null，stockStatusText = "無庫存"
```

### SQL 診斷（可選）

執行診斷腳本查看資料庫實際狀態：
```bash
mysql -u root -p < debug-stock-status.sql
```

## 預期結果

修復後，三種狀態的查詢結果應該：

### ✅ 無庫存 (stockStatus=2)
- 所有資料：`availableQty IS NULL 或 <= 0`
- 不會出現任何「正常」或「低庫存」的資料

### ✅ 低庫存 (stockStatus=1)
- 所有資料：`availableQty > 0 且 <= 閾值`
- `minStock` 不為 NULL（有設定最低庫存的物品）
- 不會出現「無庫存」或「正常」的資料

### ✅ 正常 (stockStatus=0)
- 所有資料：`availableQty > 0`
- 若有設定 `minStock`，則 `availableQty > minStock`
- **若無設定 `minStock`（NULL），則所有 > 0 都算正常** ← 這是關鍵！
- 不會出現「無庫存」或「低庫存」的資料

## 資料完整性檢查

如果修復後仍有問題，請檢查以下資料：

```sql
-- 找出可能有問題的資料
SELECT 
    i.item_id,
    i.item_code,
    i.item_name,
    i.min_stock,
    s.total_quantity,
    s.available_qty,
    s.borrowed_qty,
    CASE 
        WHEN s.available_qty IS NULL OR s.available_qty <= 0 THEN '無庫存'
        WHEN i.min_stock IS NOT NULL AND s.available_qty <= i.min_stock THEN '低庫存'
        ELSE '正常'
    END AS expected_status
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
ORDER BY s.available_qty DESC;
```

## 常見問題

### Q: 為什麼有些物品 min_stock 是 NULL？
**A**: 這是正常的。並非所有物品都需要設定最低庫存。對於這些物品：
- 只要 `available_qty > 0`，就是「正常」
- `available_qty <= 0`，就是「無庫存」
- 不會有「低庫存」狀態（因為沒有閾值可比較）

### Q: 修改後重啟了，但還是有問題？
**A**: 
1. 確認 IntelliJ IDEA 確實重新編譯了 XML 檔案（檢查 target 目錄）
2. 檢查瀏覽器是否快取了舊的 API 回應（開無痕視窗測試）
3. 使用 `debug-stock-status.sql` 直接查詢資料庫，確認資料本身沒問題

### Q: 可以強制所有物品都設定 min_stock 嗎？
**A**: 可以，但不建議。更好的做法是：
1. 保持現有邏輯（允許 NULL）
2. 在新增物品時，UI 提供預設值（如 10）
3. 讓使用者決定是否需要最低庫存管理

## 文件與腳本

- 📄 詳細修復說明：`docs/STOCK_STATUS_FIX.md`
- 🧪 測試腳本：`test-stock-status.sh`
- 🔍 SQL 診斷：`debug-stock-status.sql`

## 修復時間
- 發現時間：2025-10-05 23:00
- 修復時間：2025-10-05 23:26
- 影響範圍：庫存管理查詢功能
- 嚴重程度：🔴 高（核心功能錯誤）
- 修復狀態：✅ 已完成，待驗證
