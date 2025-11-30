-- =============================================
-- V24: 新增「重新抓取 ISBN」權限按鈕
-- 作者: Cheng
-- 日期: 2025-11-30
-- 說明: 為物品與庫存管理頁面新增「重新抓取 ISBN」按鈕權限
-- =============================================

-- 新增「重新抓取 ISBN」權限按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '重新抓取 ISBN',
       menu_id,
       9,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'inventory:management:refreshIsbn',
       '#',
       'admin',
       NOW(),
       '重新抓取物品的 ISBN 書籍資料'
FROM sys_menu
WHERE menu_name = '物品與庫存管理'
  AND menu_type = 'C';

-- 驗證插入結果
SELECT menu_id, menu_name, perms, order_num, create_time
FROM sys_menu
WHERE perms = 'inventory:management:refreshIsbn';
