-- 為掃描記錄表新增物品名稱欄位（冗餘欄位，保留歷史記錄）
-- 即使物品被刪除，仍能顯示當時掃描的物品名稱

ALTER TABLE inv_scan_log
ADD COLUMN item_name VARCHAR(200) DEFAULT '' COMMENT '物品名稱（冗餘欄位）' AFTER item_id;

-- 更新現有記錄的物品名稱（從 inv_item 表同步）
UPDATE inv_scan_log s
LEFT JOIN inv_item i ON s.item_id = i.item_id
SET s.item_name = IFNULL(i.item_name, '')
WHERE s.item_id IS NOT NULL;
