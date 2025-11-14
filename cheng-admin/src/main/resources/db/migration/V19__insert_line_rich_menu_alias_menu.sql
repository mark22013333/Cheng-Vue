-- 插入 Rich Menu Alias 管理選單
-- 需要先找到 LINE Rich Menu 管理的 menu_id

-- 步驟 1: 插入 Rich Menu Alias 管理選單（作為 LINE 模組的子選單）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Rich Menu Alias',          -- 選單名稱
       menu_id,                    -- 父選單ID（LINE 模組）
       4,                          -- 排序（在 Rich Menu 管理之後）
       'richMenuAlias',            -- 路由地址
       'line/richMenuAlias/index', -- 組件路徑
       1,                          -- 是否為外鏈（1=否）
       0,                          -- 是否快取（0=快取）
       'C',                        -- 選單類型（C=選單）
       '0',                        -- 顯示狀態（0=顯示）
       '0',                        -- 選單狀態（0=正常）
       'line:richMenuAlias:list',  -- 權限標識
       'link',                     -- 選單圖示
       'admin',                    -- 建立者
       sysdate(),                  -- 建立時間
       '',                         -- 更新者
       NULL,                       -- 更新時間
       'Rich Menu Alias 管理選單'  -- 備註
FROM sys_menu
WHERE menu_name = 'LINE 管理'
LIMIT 1;

-- 步驟 2: 取得剛插入的選單ID
SET
    @rich_menu_alias_menu_id = LAST_INSERT_ID();

-- 步驟 3: 插入 Rich Menu Alias 管理的按鈕權限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('Rich Menu Alias 查詢', @rich_menu_alias_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'line:richMenuAlias:query',
        '#', 'admin', sysdate(), '', NULL, ''),
       ('Rich Menu Alias 新增', @rich_menu_alias_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'line:richMenuAlias:add',
        '#', 'admin', sysdate(), '', NULL, ''),
       ('Rich Menu Alias 修改', @rich_menu_alias_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'line:richMenuAlias:edit',
        '#', 'admin', sysdate(), '', NULL, ''),
       ('Rich Menu Alias 刪除', @rich_menu_alias_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'line:richMenuAlias:remove',
        '#', 'admin', sysdate(), '', NULL, ''),
       ('Rich Menu Alias 匯出', @rich_menu_alias_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'line:richMenuAlias:export',
        '#', 'admin', sysdate(), '', NULL, ''),
       ('Rich Menu Alias 同步', @rich_menu_alias_menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'line:richMenuAlias:sync',
        '#', 'admin', sysdate(), '', NULL, '');

-- 步驟 4: 為 admin 角色授予權限（假設 admin 角色 ID 為 1）
-- 注意：如果您的系統中 admin 角色 ID 不是 1，請修改下面的 SQL
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE menu_name IN ('Rich Menu Alias', 'Rich Menu Alias 查詢', 'Rich Menu Alias 新增', 'Rich Menu Alias 修改',
                    'Rich Menu Alias 刪除', 'Rich Menu Alias 匯出', 'Rich Menu Alias 同步')
    AND parent_id = @rich_menu_alias_menu_id
   OR menu_id = @rich_menu_alias_menu_id;
