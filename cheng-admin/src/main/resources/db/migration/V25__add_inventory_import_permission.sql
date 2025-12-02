-- =============================================
-- V25: 新增「物品匯入」權限按鈕
-- 作者: Cheng
-- 日期: 2025-12-03
-- 說明: 為物品與庫存管理頁面新增「匯入」按鈕權限
-- =============================================

-- 新增「物品匯入」權限按鈕
INSERT INTO sys_menu (
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,
    status,
    perms,
    icon,
    create_by,
    create_time,
    remark
)
SELECT
    '物品匯入',
    menu_id,
    9,  -- 排在盤點操作（8）之後
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'inventory:management:import',
    '#',
    'admin',
    NOW(),
    '批次匯入物品資料'
FROM sys_menu
WHERE menu_name = '物品與庫存管理'
  AND menu_type = 'C';

-- 驗證插入結果
-- SELECT menu_id, menu_name, perms, order_num
-- FROM sys_menu
-- WHERE parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '物品與庫存管理' AND menu_type = 'C')
-- ORDER BY order_num;

-- 將新權限自動分配給管理員角色（role_id = 1）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT
    '1',
    menu_id
FROM sys_menu
WHERE perms = 'inventory:management:import'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = '1'
      AND menu_id = sys_menu.menu_id
  );

-- 驗證角色權限分配
-- SELECT rm.role_id, r.role_name, m.menu_name, m.perms
-- FROM sys_role_menu rm
-- JOIN sys_role r ON rm.role_id = r.role_id
-- JOIN sys_menu m ON rm.menu_id = m.menu_id
-- WHERE m.perms = 'inventory:management:import';
