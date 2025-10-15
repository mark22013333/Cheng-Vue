-- ============================
-- 修正分類管理選單路由
-- ============================

-- 1. 查詢當前分類管理選單配置
SELECT 
    menu_id,
    menu_name,
    parent_id,
    path,
    component,
    perms,
    visible,
    status
FROM sys_menu 
WHERE menu_name = '分類管理' OR perms = 'inventory:category:list';

-- 2. 更新分類管理選單的路徑
UPDATE sys_menu 
SET path = 'category',
    component = 'inventory/management/index',
    update_time = NOW()
WHERE perms = 'inventory:category:list';

-- 3. 驗證更新結果
SELECT 
    menu_id,
    menu_name,
    parent_id,
    path,
    component,
    perms,
    icon,
    visible,
    status
FROM sys_menu 
WHERE perms = 'inventory:category:list';

-- 4. 查詢完整的庫存管理選單樹
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.path,
    m.component,
    m.order_num,
    m.perms,
    m.menu_type,
    m.visible,
    m.status
FROM sys_menu m
WHERE m.menu_id IN (
    SELECT menu_id FROM sys_menu WHERE parent_id = (
        SELECT menu_id FROM sys_menu WHERE menu_name = '庫存管理' AND parent_id = 0
    )
)
OR m.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '庫存管理' AND parent_id = 0)
ORDER BY m.parent_id, m.order_num;
