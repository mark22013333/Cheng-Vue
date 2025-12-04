-- 新增預約功能權限
-- 時間戳記: 2025-12-04

-- 1. 為「物品與庫存管理」新增「預約」權限按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物品預約',
       menu_id,
       9,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'inventory:management:reserve',
       '#',
       'admin',
       NOW(),
       '預約物品功能'
FROM sys_menu
WHERE menu_name = '物品與庫存管理'
  AND menu_type = 'C'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE perms = 'inventory:management:reserve'
);

-- 2. 為管理員角色（role_id=1）自動分配預約權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT '1', menu_id
FROM sys_menu
WHERE perms = 'inventory:management:reserve'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = '1'
      AND menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'inventory:management:reserve' LIMIT 1)
);

-- 3. 驗證插入結果
SELECT menu_id, menu_name, perms, order_num, create_time
FROM sys_menu
WHERE perms = 'inventory:management:reserve';
