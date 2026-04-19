## ADDED Requirements

### Requirement: 成本價不得高於售價（硬性規則）
系統 SHALL 在 SKU 儲存時拒絕任何 `costPrice > price` 的資料；`costPrice === price` 允許（0 毛利視為合法情境）。前端驗證於 validateSkuList 回傳錯誤，後端 `ShopProductServiceImpl` 在 save 路徑擲出 `ServiceException`。

#### Scenario: 前端驗證 cost > price
- **WHEN** 使用者在 SkuCard 輸入 `price = 100`、`costPrice = 150` 並按儲存
- **THEN** validateSkuList 回傳 errors 包含 `{ field: 'costPrice', message: '成本價不可高於售價' }`，表單不送出，欄位紅框警示

#### Scenario: 前端驗證允許等值
- **WHEN** 使用者輸入 `price = 100`、`costPrice = 100`
- **THEN** validateSkuList 通過，不回報錯誤

#### Scenario: 後端攔截 API 直呼
- **WHEN** 呼叫端（爬蟲、API 直呼）傳入 `sku.price = 100, sku.costPrice = 150`
- **THEN** `ShopProductServiceImpl.validateAndEnrichSkuPricing` 擲出 `ServiceException`，訊息包含 SKU 名稱與金額，資料不寫入 DB

---

### Requirement: 0 元商品需使用者明確確認（軟性規則）
系統 SHALL 在本次編輯中被修改的 SKU 若 `price <= 0`，於儲存前以確認對話列出 SKU 名稱，使用者按「確定」才送出 API；按「取消」中止儲存並保留表單。既有未被本次編輯的 0 元 SKU 不觸發確認。

#### Scenario: 新增 0 元 SKU
- **WHEN** 使用者新增一筆 SKU，`price = 0`，`skuName = '試用包'`，按儲存
- **THEN** 系統跳出 `ElMessageBox.confirm`，內容列出「試用包」，使用者按確定後資料送出後端

#### Scenario: 取消 0 元商品儲存
- **WHEN** 確認對話出現時使用者按「取消」
- **THEN** 儲存中止，表單內容完整保留，使用者可繼續編輯

#### Scenario: 既有 0 元商品未被編輯
- **WHEN** 使用者打開一個已存在 `price = 0` 的商品，只改了 `title`，按儲存
- **THEN** 不觸發確認對話，直接儲存成功（dirty 比對通過）

#### Scenario: 既有 0 元商品被重新編輯價格
- **WHEN** 使用者打開已存在 `price = 0` 的商品，把 `price` 改成 `100`、又改回 `0`
- **THEN** 觸發確認對話（dirty 比對為 true），使用者需再次確認

#### Scenario: 多個 0 元 SKU 批次確認
- **WHEN** 儲存時有 3 個本次新增的 0 元 SKU
- **THEN** 確認對話一次列出全部 3 個 SKU 名稱，使用者一次決定全部

---

### Requirement: 既有 0 元商品相容顯示
系統 SHALL 載入商品時若偵測到任一 SKU `price === 0`，於 SkuCard 與 SkuListItem 顯示橘色警告圖示與提示「此規格為 0 元商品」，但不阻擋表單提交。

#### Scenario: 載入包含 0 元 SKU 的商品
- **WHEN** 使用者打開商品編輯頁，該商品包含 `price = 0` 的 SKU
- **THEN** 該 SKU 列表項顯示 WarningFilled icon 與 tooltip，提示文字為「此規格為 0 元商品」

#### Scenario: 不阻擋儲存
- **WHEN** 使用者未編輯該 0 元 SKU 的 price / costPrice，調整其他欄位後儲存
- **THEN** 儲存成功，不跳出確認對話

---

### Requirement: 系統推薦售價
系統 SHALL 在 SKU `price` 為空或 0、且 `costPrice > 0` 時提供「建議售價 $X」功能，前端 UI 顯示按鈕可一鍵填入；後端在 `price == null` 時自動以 `round(costPrice × 1.2)` 填補（`price === 0` 不覆寫，尊重前端確認流程）。

#### Scenario: 前端建議售價按鈕顯示
- **WHEN** 使用者在 SkuCard 將 `costPrice` 設為 100、`price` 留空
- **THEN** 售價輸入欄旁顯示「建議售價 $120」按鈕

#### Scenario: 點擊建議售價
- **WHEN** 使用者點擊「建議售價 $120」按鈕
- **THEN** `sku.price` 填入 120，按鈕消失，零元警告消失

#### Scenario: 後端自動填補推薦售價
- **WHEN** API 傳入 `sku.price = null, sku.costPrice = 100`
- **THEN** `ShopProductServiceImpl` 於 save 前自動把 `price` 設為 120（`round(100 × 1.2)`），資料寫入 DB

#### Scenario: 後端不覆寫明確 0
- **WHEN** API 傳入 `sku.price = 0, sku.costPrice = 100`（前端已經過確認對話）
- **THEN** 後端不覆寫 price，保持 0 寫入 DB

#### Scenario: 成本為 0 不提供建議
- **WHEN** SKU `costPrice = 0`、`price` 留空
- **THEN** 前端不顯示建議售價按鈕；後端不自動填入 price

---

### Requirement: 匯入對話框的 0 元提示與成本上限
系統 SHALL 在 `InventoryImportDialog` 的 Preview 步驟移除「$0 匯入後手動設定」類型的暗示性提示；`editCostPrice` 欄位以 `editPrice` 作為動態上限；任何 `editCostPrice > editPrice` 的項目會使「確認匯入」按鈕 disabled，並顯示紅色錯誤提示。

#### Scenario: 0 元匯入警告文字
- **WHEN** 使用者進入 Preview 步驟，有 2 個物品的 `editPrice = 0`
- **THEN** 勾選項區塊顯示「2 個物品的售價為 $0，匯入後仍需於儲存時確認才能建立為 0 元商品」

#### Scenario: 成本超過售價擋匯入
- **WHEN** 使用者在 Preview 步驟將 `editCostPrice` 設為 200、`editPrice = 150`
- **THEN** 該 SKU 的成本欄顯示紅框與「超過售價」文字，「確認匯入」按鈕 disabled

#### Scenario: 調整後可匯入
- **WHEN** 使用者將 `editCostPrice` 降為 100（小於 150）
- **THEN** 紅框消失，「確認匯入」按鈕恢復 enabled

---

### Requirement: validateSkuList 回傳格式擴充
系統 SHALL 將 `useProductForm.validateSkuList()` 的回傳擴充為 `{ valid, errors, needsConfirm }`，其中 `needsConfirm` 為陣列，元素格式 `{ index, skuName, reason }`，僅包含「本次編輯過且 price <= 0」的 SKU。`valid` 僅依 `errors` 判定，`needsConfirm` 不影響 valid。

#### Scenario: 僅有需確認項
- **WHEN** 所有 SKU 通過硬性規則，但有 1 個本次新增的 0 元 SKU
- **THEN** 回傳 `{ valid: true, errors: [], needsConfirm: [{ index: 0, skuName: '試用包', reason: 'zero-price' }] }`

#### Scenario: 同時有錯誤與需確認
- **WHEN** 有 1 個 SKU `cost > price`，另有 1 個新增的 0 元 SKU
- **THEN** 回傳 `{ valid: false, errors: [...], needsConfirm: [...] }`，saveProduct 因 `!valid` 而不進入確認對話
