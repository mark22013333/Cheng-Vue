-- =========================================
-- 標籤模組初始化 SQL
-- 版本：V35
-- 建立時間：2026-01-11
-- 說明：建立跨平台標籤模組（方案 C：混合方案）
--       - sys_tag：共用標籤定義表
--       - line_user_tag_relation：LINE 使用者標籤關聯（保留原表，修改 FK）
--       - inv_item_tag_relation：庫存物品標籤關聯（新建）
-- =========================================

-- =========================================
-- 1. 建立共用標籤定義表
-- =========================================
CREATE TABLE IF NOT EXISTS sys_tag
(
    tag_id          BIGINT AUTO_INCREMENT COMMENT '標籤ID',
    tag_name        VARCHAR(100) NOT NULL COMMENT '標籤名稱',
    tag_code        VARCHAR(100) NOT NULL COMMENT '標籤代碼（唯一）',
    tag_color       VARCHAR(20)  DEFAULT NULL COMMENT '標籤顏色（前端顯示用，如 #FF5733）',
    tag_description VARCHAR(500) DEFAULT NULL COMMENT '標籤描述',
    platform_scope  VARCHAR(100) DEFAULT 'ALL' COMMENT '適用平台範圍：ALL=全平台/LINE=僅LINE/INVENTORY=僅庫存，多值以逗號分隔如 LINE,INVENTORY',
    user_count      INT          DEFAULT 0 COMMENT 'LINE 使用者數量（冗餘欄位，定時重算）',
    item_count      INT          DEFAULT 0 COMMENT '庫存物品數量（冗餘欄位，定時重算）',
    status          TINYINT      DEFAULT 1 COMMENT '狀態：0=停用, 1=啟用',
    sort_order      INT          DEFAULT 0 COMMENT '排序順序（數字越小越前）',
    create_by       VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (tag_id),
    UNIQUE KEY uk_tag_code (tag_code),
    INDEX idx_status (status),
    INDEX idx_platform_scope (platform_scope),
    INDEX idx_sort_order (sort_order)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系統標籤定義表（跨平台共用）';

-- =========================================
-- 2. 從現有 line_user_tag 遷移資料到 sys_tag
-- =========================================
INSERT INTO sys_tag (tag_id, tag_name, tag_code, tag_color, tag_description, platform_scope, user_count, item_count,
                     status, sort_order, create_by, create_time, update_by, update_time, remark)
SELECT tag_id,
       tag_name,
       tag_code,
       tag_color,
       tag_description,
       'LINE',
       user_count,
       0,
       status,
       sort_order,
       create_by,
       create_time,
       update_by,
       update_time,
       NULL
FROM line_user_tag
ON DUPLICATE KEY UPDATE tag_name = VALUES(tag_name);

-- =========================================
-- 3. 建立庫存物品標籤關聯表
-- =========================================
CREATE TABLE IF NOT EXISTS inv_item_tag_relation
(
    id          BIGINT AUTO_INCREMENT COMMENT '主鍵ID',
    item_id     BIGINT NOT NULL COMMENT '物品ID（對應 inv_item.item_id）',
    tag_id      BIGINT NOT NULL COMMENT '標籤ID（對應 sys_tag.tag_id）',
    create_by   VARCHAR(64) DEFAULT '' COMMENT '建立者',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_item_tag (item_id, tag_id),
    INDEX idx_item_id (item_id),
    INDEX idx_tag_id (tag_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='庫存物品標籤關聯表';

-- =========================================
-- 4. 刪除舊的 line_user_tag 表（資料已遷移）
-- =========================================
DROP TABLE IF EXISTS line_user_tag;

-- =========================================
-- 5. 新增選單項目
-- =========================================

-- 取得 menu_id 最大值
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 3000)
                    FROM sys_menu);

-- 5.1 新增「標籤管理」一級選單
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 1, '標籤管理', 0, 7, 'tag', NULL, 1, 0, 'M', '0', '0', '', 'dict', 'admin', NOW(), '', NULL,
        '標籤管理模組');

SET @tag_menu_id = @max_menu_id + 1;

-- 5.2 新增「LINE 標籤」二級選單（目錄）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 2, 'LINE 標籤', @tag_menu_id, 1, 'line', NULL, 1, 0, 'M', '0', '0', '', 'peoples', 'admin',
        NOW(), '', NULL, 'LINE 標籤管理');

SET @line_tag_menu_id = @max_menu_id + 2;

-- 5.3 新增「標籤列表」頁面（LINE）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 3, 'LINE標籤列表', @line_tag_menu_id, 1, 'list', 'tag/line/list/index', 1, 0, 'C', '0', '0',
        'tag:line:list', 'list', 'admin', NOW(), '', NULL, 'LINE 標籤列表');

-- 5.4 新增「使用者貼標」頁面（LINE）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 4, '使用者貼標', @line_tag_menu_id, 2, 'bindUser', 'tag/line/bindUser/index', 1, 0, 'C', '0',
        '0', 'tag:line:bindUser', 'user', 'admin', NOW(), '', NULL, 'LINE 使用者貼標');

-- 5.5 新增「庫存標籤」二級選單（目錄）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 5, '庫存標籤', @tag_menu_id, 2, 'inventory', NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin',
        NOW(), '', NULL, '庫存標籤管理');

SET @inv_tag_menu_id = @max_menu_id + 5;

-- 5.6 新增「標籤列表」頁面（庫存）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 6, '庫存標籤列表', @inv_tag_menu_id, 1, 'list', 'tag/inventory/list/index', 1, 0, 'C', '0', '0',
        'tag:inventory:list', 'list', 'admin', NOW(), '', NULL, '庫存標籤列表');

-- 5.7 新增「物品貼標」頁面（庫存）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 7, '物品貼標', @inv_tag_menu_id, 2, 'bindItem', 'tag/inventory/bindItem/index', 1, 0, 'C', '0',
        '0', 'tag:inventory:bindItem', 'component', 'admin', NOW(), '', NULL, '庫存物品貼標');

-- =========================================
-- 6. 新增按鈕權限
-- =========================================

-- 6.1 LINE 標籤列表按鈕權限
SET @line_list_menu_id = @max_menu_id + 3;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 8, '標籤查詢', @line_list_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'tag:line:query', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 9, '標籤新增', @line_list_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'tag:line:add', '#', 'admin',
        NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 10, '標籤修改', @line_list_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'tag:line:edit', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 11, '標籤刪除', @line_list_menu_id, 4, '', '', 1, 0, 'F', '0', '0', 'tag:line:remove', '#',
        'admin', NOW(), '', NULL, '');

-- 6.2 LINE 使用者貼標按鈕權限
SET @line_bind_menu_id = @max_menu_id + 4;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 12, '貼標查詢', @line_bind_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'tag:line:bindQuery', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 13, '批次貼標', @line_bind_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'tag:line:batchBind', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 14, '移除標籤', @line_bind_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'tag:line:unbind', '#',
        'admin', NOW(), '', NULL, '');

-- 6.3 庫存標籤列表按鈕權限
SET @inv_list_menu_id = @max_menu_id + 6;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 15, '標籤查詢', @inv_list_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'tag:inventory:query', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 16, '標籤新增', @inv_list_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'tag:inventory:add', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 17, '標籤修改', @inv_list_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'tag:inventory:edit', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 18, '標籤刪除', @inv_list_menu_id, 4, '', '', 1, 0, 'F', '0', '0', 'tag:inventory:remove', '#',
        'admin', NOW(), '', NULL, '');

-- 6.4 庫存物品貼標按鈕權限
SET @inv_bind_menu_id = @max_menu_id + 7;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 19, '貼標查詢', @inv_bind_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'tag:inventory:bindQuery',
        '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 20, '批次貼標', @inv_bind_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'tag:inventory:batchBind',
        '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 21, '移除標籤', @inv_bind_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'tag:inventory:unbind', '#',
        'admin', NOW(), '', NULL, '');

-- =========================================
-- 7. 為管理員角色分配標籤管理權限
-- =========================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE perms LIKE 'tag:%'
   OR menu_name = '標籤管理'
   OR (parent_id IN (SELECT m2.menu_id FROM sys_menu m2 WHERE m2.menu_name = '標籤管理'));
