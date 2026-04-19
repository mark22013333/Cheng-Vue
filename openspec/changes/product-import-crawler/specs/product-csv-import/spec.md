## ADDED Requirements

### Requirement: 讀取 CSV 檔案並解析商品資料
系統 SHALL 讀取本地 CSV 檔案，解析以下欄位：狀態、條碼編號、品名、規格、品號、定價一、零售價、九折價。僅處理「狀態」為「正常品」的列。

#### Scenario: 成功讀取 CSV
- **WHEN** 提供有效的 CSV 檔案路徑
- **THEN** 系統解析所有「正常品」列，回傳商品資料 List，每筆包含 barcode、productName、spec、productCode、costPrice（定價一）、retailPrice（零售價）、discountPrice（九折價）

#### Scenario: CSV 檔案不存在
- **WHEN** 提供的 CSV 路徑不存在
- **THEN** 系統拋出異常並記錄錯誤訊息

#### Scenario: CSV 欄位缺失
- **WHEN** CSV 某列缺少必要欄位（條碼編號或品名）
- **THEN** 系統跳過該列並記錄警告

### Requirement: 價格解析與轉換
系統 SHALL 正確解析 CSV 中的價格欄位，處理含逗號的數字格式（如 "1,280.00"）。

#### Scenario: 一般價格
- **WHEN** 零售價為 "175"
- **THEN** 解析為 BigDecimal(175)

#### Scenario: 含逗號價格
- **WHEN** 零售價為 "1,280.00"
- **THEN** 解析為 BigDecimal(1280)
