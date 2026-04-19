-- 診斷庫存狀態查詢問題
-- 執行時間：2025-10-05

-- 1. 查看所有物品的庫存狀態原始資料
SELECT 
    i.item_id,
    i.item_code,
    i.item_name,
    i.min_stock,
    s.total_quantity,
    s.available_qty,
    s.borrowed_qty,
    -- 根據修正後的邏輯計算狀態
    CASE 
        WHEN s.available_qty IS NULL OR s.available_qty <= 0 THEN '無庫存'
        WHEN i.min_stock IS NOT NULL AND s.available_qty <= i.min_stock THEN '低庫存'
        WHEN i.min_stock IS NULL AND s.available_qty > 0 THEN '正常(無最低庫存設定)'
        ELSE '正常'
    END AS calculated_status
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
ORDER BY i.item_code;

-- 2. 模擬「無庫存」查詢 (stockStatus=2)
SELECT 
    i.item_id,
    i.item_code,
    i.item_name,
    s.available_qty,
    '應該都是無庫存' AS expected_result
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
  AND (s.available_qty IS NULL OR s.available_qty <= 0);

-- 3. 模擬「低庫存」查詢 (stockStatus=1, 無閾值)
SELECT 
    i.item_id,
    i.item_code,
    i.item_name,
    i.min_stock,
    s.available_qty,
    s.total_quantity,
    '應該都是低庫存' AS expected_result
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
  AND s.available_qty > 0
  AND i.min_stock IS NOT NULL
  AND s.available_qty <= i.min_stock;

-- 4. 模擬「正常」查詢 (stockStatus=0, 無閾值)
SELECT 
    i.item_id,
    i.item_code,
    i.item_name,
    i.min_stock,
    s.available_qty,
    '應該都是正常' AS expected_result
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
  AND s.available_qty > 0
  AND (
    (i.min_stock IS NOT NULL AND s.available_qty > i.min_stock)
    OR i.min_stock IS NULL
  );

-- 5. 檢查問題資料：有庫存但 min_stock 為 NULL 的情況
SELECT 
    i.item_id,
    i.item_code,
    i.item_name,
    i.min_stock,
    s.available_qty,
    '這些資料可能造成分類問題' AS note
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
  AND i.min_stock IS NULL
  AND s.available_qty > 0;

-- 6. 找出截圖中的 ITEM3D111 (或類似的異常資料)
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
    END AS calculated_status
FROM inv_item i
LEFT JOIN inv_stock s ON i.item_id = s.item_id
WHERE i.del_flag = '0'
  AND i.item_code LIKE '%ITEM%'
ORDER BY s.available_qty DESC;
