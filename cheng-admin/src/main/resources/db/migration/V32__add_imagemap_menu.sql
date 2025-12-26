-- =========================================
-- LINE 圖文訊息管理選單
-- 版本：V32
-- 建立時間：2025-12-25
-- 說明：新增「訊息範本 > 圖文訊息」子選單
-- =========================================

-- 取得「訊息範本」選單的 menu_id
SET @template_menu_id = (SELECT menu_id
                         FROM sys_menu
                         WHERE menu_name = '訊息範本'
                         LIMIT 1);

-- 取得 menu_id 最大值
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 3000)
                    FROM sys_menu);

-- 如果「訊息範本」選單存在，將其改為目錄類型，並設定 icon
UPDATE sys_menu
SET menu_type = 'M',
    component = NULL,
    path      = 'template',
    icon      = 'message'
WHERE menu_id = @template_menu_id;

-- 新增「圖文訊息」子選單（icon: image）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 1, '圖文訊息', @template_menu_id, 1, 'imagemap', 'line/imagemap/index', 1, 0, 'C', '0', '0',
        'line:imagemap:list', 'image', 'admin', NOW(), '', NULL, 'LINE 圖文訊息管理');

-- 新增「範本列表」子選單（原本的訊息範本頁面，icon: documentation）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 2, '範本列表', @template_menu_id, 2, 'list', 'line/template/index', 1, 0, 'C', '0', '0',
        'line:template:list', 'documentation', 'admin', NOW(), '', NULL, 'LINE 訊息範本列表');

-- 新增圖文訊息相關按鈕權限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 3, '圖文訊息查詢', @max_menu_id + 1, 1, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:query', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 4, '圖文訊息新增', @max_menu_id + 1, 2, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:add', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 5, '圖文訊息修改', @max_menu_id + 1, 3, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:edit', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 6, '圖文訊息刪除', @max_menu_id + 1, 4, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:remove',
        '#',
        'admin', NOW(), '', NULL, '');

-- =========================================
-- 為管理員角色新增權限
-- =========================================

-- 取得管理員角色ID
SET @admin_role_id = (SELECT role_id
                      FROM sys_role
                      WHERE role_key = 'admin'
                      LIMIT 1);

-- 為管理員角色新增圖文訊息相關權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_id BETWEEN @max_menu_id + 1 AND @max_menu_id + 6;

-- =========================================
-- 重命名 sys_line_ 前綴表為 line_ 前綴
-- 建立時間：2025-12-25
-- 說明：重命名資料表前綴 sys_line_ -> line_
-- =========================================

-- 重命名表
RENAME TABLE sys_line_config TO line_config;
RENAME TABLE sys_line_conversation_log TO line_conversation_log;
RENAME TABLE sys_line_message_log TO line_message_log;
RENAME TABLE sys_line_rich_menu TO line_rich_menu;
RENAME TABLE sys_line_rich_menu_alias TO line_rich_menu_alias;
RENAME TABLE sys_line_rich_menu_area TO line_rich_menu_area;
RENAME TABLE sys_line_user TO line_user;
RENAME TABLE sys_line_user_tag TO line_user_tag;
RENAME TABLE sys_line_user_tag_relation TO line_user_tag_relation;

-- 更新表註解
ALTER TABLE line_config
    COMMENT = 'LINE 頻道設定表';
ALTER TABLE line_conversation_log
    COMMENT = 'LINE 對話紀錄表';
ALTER TABLE line_message_log
    COMMENT = 'LINE 訊息紀錄表';
ALTER TABLE line_rich_menu
    COMMENT = 'LINE Rich Menu 表';
ALTER TABLE line_rich_menu_alias
    COMMENT = 'LINE Rich Menu 別名表';
ALTER TABLE line_rich_menu_area
    COMMENT = 'LINE Rich Menu 區域表';
ALTER TABLE line_user
    COMMENT = 'LINE 使用者表';
ALTER TABLE line_user_tag
    COMMENT = 'LINE 使用者標籤表';
ALTER TABLE line_user_tag_relation
    COMMENT = 'LINE 使用者標籤關聯表';
