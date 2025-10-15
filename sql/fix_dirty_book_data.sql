-- ============================
-- 清理書籍資料髒資料
-- ============================

-- 1. 查詢有問題的資料：書籍資訊存在但物品不存在或已被刪除
SELECT 
    b.book_info_id,
    b.item_id AS book_item_id,
    b.isbn,
    b.title,
    i.item_id AS actual_item_id,
    i.item_name,
    i.del_flag AS item_del_flag,
    CASE 
        WHEN i.item_id IS NULL THEN '物品不存在'
        WHEN i.del_flag = '2' THEN '物品已刪除'
        ELSE '正常'
    END AS status
FROM inv_book_info b
LEFT JOIN inv_item i ON b.item_id = i.item_id AND i.del_flag = '0'
WHERE i.item_id IS NULL OR i.del_flag = '2';

-- 1.5. 查詢孤立的庫存記錄（物品已刪除但庫存記錄還在）
SELECT 
    s.stock_id,
    s.item_id,
    s.total_quantity,
    i.item_id AS actual_item_id,
    i.item_name,
    i.del_flag,
    CASE 
        WHEN i.item_id IS NULL THEN '物品不存在'
        WHEN i.del_flag = '2' THEN '物品已刪除'
        ELSE '正常'
    END AS status
FROM inv_stock s
LEFT JOIN inv_item i ON s.item_id = i.item_id AND i.del_flag = '0'
WHERE i.item_id IS NULL OR i.del_flag = '2';

-- 2. 方案一：將書籍資訊的 item_id 設為 NULL（建議）
-- 這樣下次掃描時會自動重新建立物品並關聯
UPDATE inv_book_info b
SET b.item_id = NULL,
    b.update_time = NOW(),
    b.update_by = 'system'
WHERE b.item_id IN (
    SELECT item_id FROM (
        SELECT b2.item_id
        FROM inv_book_info b2
        LEFT JOIN inv_item i ON b2.item_id = i.item_id AND i.del_flag = '0'
        WHERE b2.item_id IS NOT NULL 
          AND (i.item_id IS NULL OR i.del_flag = '2')
    ) AS tmp
);

-- 2.5. 清理孤立的庫存記錄（物品已被刪除但庫存記錄還在）
-- 這會導致重新建立物品時，庫存記錄已存在而無法插入
DELETE FROM inv_stock
WHERE item_id IN (
    SELECT item_id FROM (
        SELECT s.item_id
        FROM inv_stock s
        LEFT JOIN inv_item i ON s.item_id = i.item_id AND i.del_flag = '0'
        WHERE i.item_id IS NULL OR i.del_flag = '2'
    ) AS tmp
);

-- 3. 驗證清理結果
SELECT 
    COUNT(*) AS total_books,
    SUM(CASE WHEN item_id IS NULL THEN 1 ELSE 0 END) AS books_without_item,
    SUM(CASE WHEN item_id IS NOT NULL THEN 1 ELSE 0 END) AS books_with_item
FROM inv_book_info;

-- 4. 檢查是否還有問題資料
SELECT 
    b.book_info_id,
    b.isbn,
    b.title,
    b.item_id,
    '書籍資訊指向的物品不存在' AS issue
FROM inv_book_info b
LEFT JOIN inv_item i ON b.item_id = i.item_id AND i.del_flag = '0'
WHERE b.item_id IS NOT NULL 
  AND (i.item_id IS NULL OR i.del_flag = '2');

-- 5. 檢查是否還有孤立的庫存記錄
SELECT 
    s.stock_id,
    s.item_id,
    '孤立的庫存記錄' AS issue
FROM inv_stock s
LEFT JOIN inv_item i ON s.item_id = i.item_id AND i.del_flag = '0'
WHERE i.item_id IS NULL OR i.del_flag = '2';

-- 5. （可選）方案二：完全刪除有問題的書籍資訊
-- 警告：這會永久刪除資料，建議先備份
-- BACKUP FIRST:
-- CREATE TABLE inv_book_info_backup AS SELECT * FROM inv_book_info;
-- 
-- DELETE FROM inv_book_info
-- WHERE item_id IN (
--     SELECT item_id FROM (
--         SELECT b2.item_id
--         FROM inv_book_info b2
--         LEFT JOIN inv_item i ON b2.item_id = i.item_id AND i.del_flag = '0'
--         WHERE b2.item_id IS NOT NULL 
--           AND (i.item_id IS NULL OR i.del_flag = '2')
--     ) AS tmp
-- );
