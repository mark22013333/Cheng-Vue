# 庫存狀態查詢修復說明

## 問題描述
選擇「無庫存」狀態進行搜尋時，結果中出現了「正常」狀態的資料（例如可用數量為 104 的 ITEM3D111），這是不正確的行為。

## 根本原因
1. **判斷邏輯不一致**：原本「無庫存」使用 `totalQuantity == 0` 判斷，但實際應該以 `availableQty <= 0` 為準
2. **NULL 值處理缺陷**：當 `min_stock` 為 NULL 時，SQL 條件 `available_qty > min_stock` 返回 NULL（不是 TRUE 或 FALSE），導致這些資料無法被正確分類
3. **低庫存/正常狀態邏輯不完整**：
   - 低庫存條件缺少 `available_qty > 0` 的前置檢查
   - 正常狀態沒有處理 `min_stock IS NULL` 的情況（無最低庫存設定時，所有 > 0 都應為正常）
4. **後端未重新編譯**：修改 XML 和 DTO 後，需要重新啟動後端服務才能生效
5. **前端參數混亂**：從「低庫存」切換到「無庫存」時，`lowStockThreshold` 參數未清除

## 已修改檔案

### 1. InvItemMapper.xml
**檔案位置**：`cheng-system/src/main/resources/mapper/system/InvItemMapper.xml`

**修改內容**：完整重寫了庫存狀態的查詢條件，確保處理 NULL 值並保證邏輯互斥

```xml
<if test="stockStatus != null and stockStatus != ''">
    <choose>
        <!-- 無庫存：可用數量為 null 或 <= 0 -->
        <when test="stockStatus == '2'">
            and (s.available_qty is null or s.available_qty &lt;= 0)
        </when>
        <!-- 低庫存：可用數量 > 0 且低於閾值 -->
        <when test="stockStatus == '1'">
            and s.available_qty > 0
            <choose>
                <!-- 使用自訂閾值 -->
                <when test="lowStockThreshold != null">
                    and s.available_qty &lt;= #{lowStockThreshold}
                </when>
                <!-- 使用物品的最低庫存設定 -->
                <otherwise>
                    and i.min_stock is not null
                    and s.available_qty &lt;= i.min_stock
                </otherwise>
            </choose>
        </when>
        <!-- 正常：可用數量 > 0 且高於閾值 -->
        <when test="stockStatus == '0'">
            and s.available_qty > 0
            <choose>
                <!-- 使用自訂閾值 -->
                <when test="lowStockThreshold != null">
                    and s.available_qty > #{lowStockThreshold}
                </when>
                <!-- 使用物品的最低庫存設定，或無設定時所有 > 0 都算正常 -->
                <otherwise>
                    and (i.min_stock is null or s.available_qty > i.min_stock)
                </otherwise>
            </choose>
        </when>
    </choose>
</if>
```

**關鍵修正點**：
1. 所有狀態都明確檢查 `available_qty > 0`（除了無庫存）
2. 低庫存狀態增加 `min_stock is not null` 檢查
3. 正常狀態使用 `(min_stock is null OR available_qty > min_stock)` 處理無設定的情況

### 2. InvItemWithStockDTO.java
**檔案位置**：`cheng-system/src/main/java/com/cheng/system/dto/InvItemWithStockDTO.java`

**修改內容**：重寫計算邏輯，確保與 SQL 查詢條件完全一致

```java
/**
 * 計算庫存狀態
 * 邏輯與 SQL 查詢條件完全一致
 */
public void calculateStockStatus() {
    // 無庫存：可用數量為 null 或 <= 0
    if (availableQty == null || availableQty <= 0) {
        this.stockStatus = "2";
        this.stockStatusText = "無庫存";
        this.isBelowMinStock = true;
        return;
    }

    // 低庫存：可用數量 > 0 且有設定最低庫存，且低於最低庫存
    if (minStock != null && availableQty > 0 && availableQty <= minStock) {
        this.stockStatus = "1";
        this.stockStatusText = "低庫存";
        this.isBelowMinStock = true;
        return;
    }

    // 正常：可用數量 > 0 且（無最低庫存設定 或 高於最低庫存）
    this.stockStatus = "0";
    this.stockStatusText = "正常";
    this.isBelowMinStock = false;
}
```

**關鍵修正點**：
1. 明確檢查 `availableQty > 0` 再判斷低庫存
2. 使用 early return 避免邏輯混亂
3. 預設為「正常」，覆蓋 `min_stock == null` 的情況

### 3. index.vue (前端)
**檔案位置**：`cheng-ui/src/views/inventory/management/index.vue`

**修改內容**：
- 將庫存狀態下拉選單的 `@change` 事件改為 `handleStockStatusChange`
- 新增 `handleStockStatusChange()` 方法，當切換到非低庫存狀態時，自動清除 `lowStockThreshold` 參數

```javascript
/** 庫存狀態變化處理 */
handleStockStatusChange() {
  // 當切換到非低庫存狀態時，清除低庫存閾值參數
  if (this.queryParams.stockStatus !== '1') {
    this.queryParams.lowStockThreshold = null;
  }
  this.handleQuery();
}
```

## 修復步驟

### 第一步：重新啟動後端服務（必須）
1. 在 IntelliJ IDEA 中停止目前執行的後端服務
2. 清理並重新編譯專案：
   ```bash
   mvn clean compile
   ```
3. 重新啟動後端服務（記得加上 Jasypt 參數）：
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local \
     -Djasypt.encryptor.password=diDsd]3FsGO@4dido
   ```

### 第二步：清除瀏覽器快取並重新整理前端
1. 在瀏覽器中按 `Cmd + Shift + R`（Mac）或 `Ctrl + Shift + R`（Windows）強制重新整理
2. 或者開啟無痕視窗進行測試

### 第三步：驗證修復結果

#### 測試案例 1：無庫存查詢
```bash
curl 'http://localhost:1024/dev-api/inventory/management/list?pageNum=1&pageSize=10&stockStatus=2' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**預期結果**：
- 所有回傳的資料中，`availableQty` 應該都是 `null` 或 `<= 0`
- `stockStatusText` 應該都是「無庫存」
- 不應出現「正常」或「低庫存」狀態的資料

#### 測試案例 2：低庫存查詢（使用閾值）
```bash
curl 'http://localhost:1024/dev-api/inventory/management/list?pageNum=1&pageSize=10&stockStatus=1&lowStockThreshold=10' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**預期結果**：
- 所有回傳的資料中，`availableQty` 應該 `> 0` 且 `<= 10`
- `stockStatusText` 應該都是「低庫存」

#### 測試案例 3：正常庫存查詢
```bash
curl 'http://localhost:1024/dev-api/inventory/management/list?pageNum=1&pageSize=10&stockStatus=0' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**預期結果**：
- 所有回傳的資料中，`availableQty` 應該大於設定的 `minStock`（或大於 0 若無設定）
- `stockStatusText` 應該都是「正常」

## 庫存狀態定義（修改後）

### 無庫存 (stockStatus = 2)
- **條件**：`availableQty is null OR availableQty <= 0`
- **顯示**：紅色標籤「無庫存」
- **說明**：可用數量為 0 或不存在，無法出庫或借出

### 低庫存 (stockStatus = 1)
- **條件**：`availableQty > 0 AND availableQty <= minStock`（或自訂閾值）
- **顯示**：黃色標籤「低庫存」
- **說明**：可用數量已低於最低庫存設定，需要補貨

### 正常 (stockStatus = 0)
- **條件**：`availableQty > minStock`（或自訂閾值）
- **顯示**：綠色標籤「正常」
- **說明**：庫存充足

## 關鍵設計原則

1. **統一以 `availableQty` 為判斷基準**
   - `totalQuantity` 包含借出、預留、損壞等不可用數量
   - `availableQty` 才是真正可以使用的數量
   - 所有狀態判斷都應以 `availableQty` 為準

2. **低庫存閾值的優先級**
   - 若有傳入 `lowStockThreshold` 參數，使用自訂閾值
   - 否則使用物品的 `minStock` 設定
   - 若兩者都沒有，則 `availableQty > 0` 即為正常

3. **前端與後端邏輯一致性**
   - 後端 SQL 查詢條件與 DTO 計算邏輯必須完全一致
   - 前端顯示邏輯依賴後端計算結果，不再自行判斷

## 常見問題排查

### Q1: 修改後仍看到錯誤結果？
**A**: 確認是否已重新啟動後端服務。XML 檔案的修改必須重新啟動後端才能生效。

### Q2: 切換狀態時，低庫存閾值輸入框消失但參數仍在 URL 中？
**A**: 這是正常的，前端會在下次查詢時自動清除該參數（已修改 `handleStockStatusChange` 方法）。

### Q3: 如何確認後端已套用新的查詢邏輯？
**A**: 可以在後端日誌中查看實際執行的 SQL，或直接查詢資料庫：
```sql
-- 無庫存查詢
SELECT * FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
  AND (s.available_qty IS NULL OR s.available_qty <= 0);

-- 低庫存查詢（閾值=10）
SELECT * FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
  AND s.available_qty > 0
  AND s.available_qty <= 10;
```

## 後續優化建議

1. **資料庫索引**：在 `inv_stock.available_qty` 欄位建立索引以提升查詢效能
2. **單元測試**：為 `InvItemWithStockDTO.calculateStockStatus()` 方法新增單元測試
3. **API 文件**：在 Swagger 或 API 文件中明確說明 `stockStatus` 和 `lowStockThreshold` 參數的使用方式
4. **前端提示**：當選擇「低庫存」時，可以在閾值輸入框旁顯示提示文字「留空則使用物品設定的最低庫存」

## 修復時間
- 修復日期：2025-10-05
- 修復人員：Cascade AI
- 影響範圍：庫存管理查詢功能
