-- =====================================================
-- LINE Rich Menu (圖文選單) 管理模組 - 資料庫初始化
-- Version: V17
-- Author: Cheng
-- Date: 2025-11-13
-- Description: 建立 LINE Rich Menu 相關表，支援視覺化編輯和多版型管理
-- =====================================================

-- 1. 建立 LINE Rich Menu 主表
CREATE TABLE IF NOT EXISTS sys_line_rich_menu
(
    id            BIGINT AUTO_INCREMENT COMMENT '主鍵ID',
    config_id     INT          NOT NULL COMMENT '關聯的 LINE 頻道設定ID（外鍵：sys_line_config.config_id）',
    name          VARCHAR(255) NOT NULL COMMENT '選單名稱（系統內部使用）',
    description   VARCHAR(500) COMMENT '選單說明',
    template_type VARCHAR(50) COMMENT '版型類型：TWO_COLS(左右兩格)/THREE_ROWS(上下三格)/SIX_GRID(六格)/CUSTOM(自訂)',
    chat_bar_text VARCHAR(50) COMMENT '聊天欄位顯示文字（最多14字）',
    image_url     VARCHAR(1000) COMMENT '選單圖片 URL（本機路徑或 CDN）',
    image_size    VARCHAR(20)  NOT NULL COMMENT '圖片尺寸：2500x1686/2500x843/1200x810/1200x405/800x540/800x270',
    is_default    TINYINT      DEFAULT 0 COMMENT '是否為預設選單（0=否，1=是）',
    selected      TINYINT      DEFAULT 0 COMMENT '是否為當前使用中的選單（0=否，1=是）',
    status        VARCHAR(20)  DEFAULT 'DRAFT' COMMENT '狀態：DRAFT(草稿)/ACTIVE(啟用)/INACTIVE(停用)',
    rich_menu_id  VARCHAR(255) COMMENT 'LINE 平台返回的 richMenuId（發布後才有值）',
    areas_json    TEXT COMMENT 'Rich Menu 區塊設定（JSON 格式，符合 LINE API 規範）',
    version       INT          DEFAULT 1 COMMENT '版本號',
    create_by     VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time   DATETIME COMMENT '建立時間',
    update_by     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time   DATETIME COMMENT '更新時間',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (id),
    INDEX idx_config_id (config_id),
    INDEX idx_rich_menu_id (rich_menu_id),
    INDEX idx_status (status),
    INDEX idx_config_selected (config_id, selected),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE Rich Menu (圖文選單) 主表';

-- 2. 建立 LINE Rich Menu 區塊設定表（選擇性使用，目前主要用 areas_json）
CREATE TABLE IF NOT EXISTS sys_line_rich_menu_area
(
    id            BIGINT AUTO_INCREMENT COMMENT '主鍵ID',
    menu_id       BIGINT      NOT NULL COMMENT '關聯的 Rich Menu ID（外鍵：sys_line_rich_menu.id）',
    bounds_x      INT         NOT NULL COMMENT '區塊左上角 X 座標',
    bounds_y      INT         NOT NULL COMMENT '區塊左上角 Y 座標',
    bounds_width  INT         NOT NULL COMMENT '區塊寬度',
    bounds_height INT         NOT NULL COMMENT '區塊高度',
    action_type   VARCHAR(20) NOT NULL COMMENT '動作類型：URI(開啟網址)/MESSAGE(傳送訊息)/POSTBACK(回傳資料)/RICHMENU_SWITCH(切換選單)',
    action_data   TEXT COMMENT '動作資料（JSON 格式）',
    sort_order    INT DEFAULT 0 COMMENT '排序（顯示順序）',
    create_time   DATETIME COMMENT '建立時間',
    update_time   DATETIME COMMENT '更新時間',
    PRIMARY KEY (id),
    INDEX idx_menu_id (menu_id),
    INDEX idx_sort_order (sort_order)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE Rich Menu 區塊設定表（預留，目前主要使用 areas_json）';

-- 3. 插入系統選單配置（Rich Menu 管理入口）
-- 查詢 LINE 管理的父選單 ID
SET @line_menu_id = (SELECT menu_id
                     FROM sys_menu
                     WHERE menu_name = 'LINE 管理'
                     LIMIT 1);

-- 插入 Rich Menu 管理選單（如果 LINE 管理選單存在）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT 'Rich Menu 管理',
       @line_menu_id,
       5,
       'richMenu',
       'line/richMenu/index',
       1,
       0,
       'C',
       '0',
       '0',
       'line:richMenu:list',
       'build',
       'admin',
       NOW(),
       'LINE Rich Menu (圖文選單) 管理'
WHERE @line_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:list');

-- 4. 插入 Rich Menu 管理的操作權限按鈕
SET @rich_menu_id = (SELECT menu_id
                     FROM sys_menu
                     WHERE perms = 'line:richMenu:list'
                     LIMIT 1);

-- 新增按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '新增 Rich Menu',
       @rich_menu_id,
       1,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'line:richMenu:add',
       '#',
       'admin',
       NOW(),
       ''
WHERE @rich_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:add');

-- 編輯按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '編輯 Rich Menu',
       @rich_menu_id,
       2,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'line:richMenu:edit',
       '#',
       'admin',
       NOW(),
       ''
WHERE @rich_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:edit');

-- 刪除按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '刪除 Rich Menu',
       @rich_menu_id,
       3,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'line:richMenu:remove',
       '#',
       'admin',
       NOW(),
       ''
WHERE @rich_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:remove');

-- 發布按鈕（發布到 LINE 平台）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '發布 Rich Menu',
       @rich_menu_id,
       4,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'line:richMenu:publish',
       '#',
       'admin',
       NOW(),
       ''
WHERE @rich_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:publish');

-- 設為預設按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '設為預設選單',
       @rich_menu_id,
       5,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'line:richMenu:setDefault',
       '#',
       'admin',
       NOW(),
       ''
WHERE @rich_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:setDefault');

-- 查看詳情按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '查看 Rich Menu',
       @rich_menu_id,
       6,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'line:richMenu:query',
       '#',
       'admin',
       NOW(),
       ''
WHERE @rich_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:query');

-- 匯出按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '匯出 Rich Menu',
       @rich_menu_id,
       7,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'line:richMenu:export',
       '#',
       'admin',
       NOW(),
       ''
WHERE @rich_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:richMenu:export');

-- 5. 為 admin 角色自動分配 Rich Menu 權限
-- 查詢 admin 角色 ID
SET @admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'admin' LIMIT 1);

-- 為 admin 角色分配 Rich Menu 相關權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE (perms LIKE 'line:richMenu:%' OR menu_name = 'Rich Menu 管理')
  AND @admin_role_id IS NOT NULL
  AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = @admin_role_id);

-- 6. 修正 LINE 設定選單的 icon
UPDATE sys_menu SET icon = 'edit' WHERE menu_name = 'LINE 設定';