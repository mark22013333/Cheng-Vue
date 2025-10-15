-- ============================
-- 分類管理權限配置
-- ============================

-- 取得分類管理選單ID（已存在，menu_id = 2062）
SET @categoryMenuId = 2062;

-- 方法1：逐條插入（推薦）
-- 1. 分類查詢權限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '分類查詢', @categoryMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'inventory:category:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'inventory:category:query');

-- 2. 分類新增權限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '分類新增', @categoryMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'inventory:category:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'inventory:category:add');

-- 3. 分類修改權限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '分類修改', @categoryMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'inventory:category:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'inventory:category:edit');

-- 4. 分類刪除權限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '分類刪除', @categoryMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'inventory:category:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'inventory:category:remove');

-- 5. 分類匯出權限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '分類匯出', @categoryMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'inventory:category:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'inventory:category:export');

-- 為管理員角色分配權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu 
WHERE perms LIKE 'inventory:category:%'
  AND perms != 'inventory:category:list'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu 
    WHERE role_id = 1 AND menu_id = sys_menu.menu_id
);

-- 驗證結果
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.order_num,
    m.perms,
    m.menu_type,
    CASE WHEN rm.role_id IS NOT NULL THEN '已授權' ELSE '未授權' END AS admin_auth
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 1
WHERE m.perms LIKE 'inventory:category:%'
   OR m.menu_id = 2062
ORDER BY m.parent_id, m.order_num;
