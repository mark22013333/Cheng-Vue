-- ========================================
-- 庫存管理選單整合更新 SQL
-- 將「物品管理」和「庫存查詢」整合為「物品與庫存管理」
-- 執行日期：2025-10-04
-- ========================================

-- 1. 新增「物品與庫存管理」選單
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '物品與庫存管理', menu_id, 1, 'management', 'inventory/management/index', 1, 0, 'C', '0', '0', 'inventory:management:list', 'table', 'admin', NOW(), '', NULL, '物品資訊與庫存狀態整合管理'
FROM sys_menu
WHERE menu_name = '庫存管理' AND menu_type = 'M';

-- 2. 為「物品與庫存管理」新增子權限按鈕
-- 查詢
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物品庫存查詢', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:query', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 新增
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物品新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:add', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 修改
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物品修改', menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:edit', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 刪除
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物品刪除', menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:remove', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 匯出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物品庫存匯出', menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:export', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 入庫
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '入庫操作', menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:stockIn', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 出庫
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '出庫操作', menu_id, 7, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:stockOut', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 盤點
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '盤點操作', menu_id, 8, '#', '', 1, 0, 'F', '0', '0', 'inventory:management:stockCheck', '#', 'admin', NOW(), ''
FROM sys_menu
WHERE menu_name = '物品與庫存管理' AND menu_type = 'C';

-- 3. 停用原有的「物品管理」和「庫存查詢」選單（不刪除，保留作為備份）
UPDATE sys_menu SET visible = '1', order_num = 99, remark = CONCAT(IFNULL(remark, ''), ' [已整合至物品與庫存管理]')
WHERE menu_name IN ('物品管理', '庫存查詢') 
  AND parent_id = (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '庫存管理' AND menu_type = 'M') tmp);

-- 4. 查詢更新結果
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    p.menu_name AS parent_menu_name,
    m.order_num,
    m.path,
    m.component,
    m.menu_type,
    m.visible,
    m.status,
    m.perms,
    m.remark
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE p.menu_name = '庫存管理' OR m.menu_name = '庫存管理'
ORDER BY m.parent_id, m.order_num;

-- ========================================
-- 執行完成後，請重新登入系統以載入新選單
-- ========================================

-- 如需復原，執行以下SQL：
-- UPDATE sys_menu SET visible = '0', order_num = 1, remark = REPLACE(remark, ' [已整合至物品與庫存管理]', '')
-- WHERE menu_name IN ('物品管理', '庫存查詢');
-- DELETE FROM sys_menu WHERE menu_name = '物品與庫存管理';
