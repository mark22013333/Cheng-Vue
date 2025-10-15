-- ============================
-- 修正分類資料腳本
-- ============================

-- 1. 確保「書籍」分類為預設分類
UPDATE inv_category 
SET remark = CASE 
    WHEN remark IS NULL OR remark = '' THEN '書籍類，掃碼自動分類；預設分類'
    WHEN remark NOT LIKE '%預設分類%' THEN CONCAT(remark, '；預設分類')
    ELSE remark
END
WHERE category_id = 1000;

-- 2. 清除其他分類的預設標記（如果有的話）
UPDATE inv_category 
SET remark = TRIM(REPLACE(REPLACE(remark, '；預設分類', ''), '預設分類', ''))
WHERE category_id != 1000 
  AND remark LIKE '%預設分類%';

-- 3. 清理空白備註
UPDATE inv_category 
SET remark = NULL 
WHERE remark = '' OR remark IS NULL;

-- 4. 查詢驗證
SELECT 
    category_id,
    category_name,
    category_code,
    parent_id,
    sort_order,
    CASE WHEN remark LIKE '%預設分類%' THEN '是' ELSE '否' END AS is_default,
    status,
    del_flag,
    remark
FROM inv_category 
WHERE del_flag = '0'
ORDER BY parent_id, sort_order;
