-- ============================
-- 在借出記錄表新增物品資訊冗餘欄位
-- 版本：V22
-- 說明：解決刪除物品後借出記錄無法顯示物品名稱的問題
-- ============================

-- 新增物品名稱和物品編碼欄位（冗餘設計，保留歷史記錄）
ALTER TABLE inv_borrow
    ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名稱（冗餘欄位，保留歷史記錄）' AFTER item_id,
    ADD COLUMN item_code VARCHAR(50)  DEFAULT NULL COMMENT '物品編碼（冗餘欄位，保留歷史記錄）' AFTER item_name;

-- 為現有記錄補齊物品資訊（從 inv_item 表更新）
UPDATE inv_borrow b
    INNER JOIN inv_item i
    ON b.item_id = i.item_id
SET b.item_name = i.item_name,
    b.item_code = i.item_code
WHERE b.item_name IS NULL
   OR b.item_code IS NULL;

-- 為已刪除物品的借出記錄標記特殊說明
UPDATE inv_borrow b
    LEFT JOIN inv_item i
    ON b.item_id = i.item_id
SET b.item_name = CONCAT('[已刪除物品 ID:', b.item_id, ']'),
    b.item_code = ''
WHERE i.item_id IS NULL
  AND (b.item_name IS NULL OR b.item_name = '');
