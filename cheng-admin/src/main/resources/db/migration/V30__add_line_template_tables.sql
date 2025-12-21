-- =========================================
-- LINE 訊息範本模組 SQL
-- 版本：V30
-- 建立時間：2025-12-18
-- 說明：建立 LINE 訊息範本相關資料表
-- =========================================

-- 1. 建立 LINE 訊息範本表
CREATE TABLE IF NOT EXISTS line_message_template
(
    template_id   BIGINT AUTO_INCREMENT COMMENT '範本ID',
    template_name VARCHAR(100) NOT NULL COMMENT '範本名稱',
    template_code VARCHAR(50)   DEFAULT NULL COMMENT '範本代碼（用於程式內快速引用）',
    msg_type      VARCHAR(20)  NOT NULL COMMENT '訊息類型：TEXT/IMAGE/VIDEO/AUDIO/LOCATION/STICKER/IMAGEMAP/FLEX',
    content       LONGTEXT     NOT NULL COMMENT '訊息內容（JSON 格式，儲存完整的 LINE Message Object）',
    alt_text      VARCHAR(400)  DEFAULT NULL COMMENT '替代文字（Flex/Imagemap 必填，裝置不支援時顯示）',
    preview_img   VARCHAR(500)  DEFAULT NULL COMMENT '預覽圖 URL（供後台列表顯示）',
    category_id   BIGINT        DEFAULT NULL COMMENT '範本分類ID（預留，可用於範本分類管理）',
    variables     VARCHAR(1000) DEFAULT NULL COMMENT '使用的變數列表（JSON Array，如 ["nickname","email"]）',
    status        TINYINT       DEFAULT 1 COMMENT '狀態：0=停用, 1=啟用',
    sort_order    INT           DEFAULT 0 COMMENT '排序順序',
    use_count     INT           DEFAULT 0 COMMENT '使用次數統計',
    last_used_at  DATETIME      DEFAULT NULL COMMENT '最後使用時間',
    del_flag      CHAR(1)       DEFAULT '0' COMMENT '刪除標誌：0=正常, 2=刪除',
    create_by     VARCHAR(64)   DEFAULT '' COMMENT '建立者',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by     VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark        VARCHAR(500)  DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (template_id),
    UNIQUE KEY uk_template_code (template_code),
    INDEX idx_msg_type (msg_type),
    INDEX idx_status (status),
    INDEX idx_category_id (category_id),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE 訊息範本表';

-- 2. 建立範本分類表（預留擴充）
CREATE TABLE IF NOT EXISTS line_message_template_category
(
    category_id   BIGINT AUTO_INCREMENT COMMENT '分類ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分類名稱',
    parent_id     BIGINT      DEFAULT 0 COMMENT '父分類ID（0=頂層）',
    sort_order    INT         DEFAULT 0 COMMENT '排序順序',
    status        TINYINT     DEFAULT 1 COMMENT '狀態：0=停用, 1=啟用',
    create_by     VARCHAR(64) DEFAULT '' COMMENT '建立者',
    create_time   DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by     VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (category_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE 訊息範本分類表';

-- =========================================
-- 新增選單項目
-- =========================================

-- 取得 LINE 管理選單的 menu_id
SET @line_menu_id = (SELECT menu_id
                     FROM sys_menu
                     WHERE menu_name = 'LINE 管理'
                       AND menu_type = 'M'
                     LIMIT 1);

-- 取得 menu_id 最大值
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 3000)
                    FROM sys_menu);

-- 新增「訊息範本」選單
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 1, '訊息範本', @line_menu_id, 4, 'template', 'line/template/index', 1, 0, 'C', '0', '0',
        'line:template:list', 'documentation', 'admin', NOW(), '', NULL, 'LINE 訊息範本管理');

-- 新增訊息範本相關按鈕權限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 2, '範本查詢', @max_menu_id + 1, 1, '#', '', 1, 0, 'F', '0', '0', 'line:template:query', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 3, '範本新增', @max_menu_id + 1, 2, '#', '', 1, 0, 'F', '0', '0', 'line:template:add', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 4, '範本修改', @max_menu_id + 1, 3, '#', '', 1, 0, 'F', '0', '0', 'line:template:edit', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 5, '範本刪除', @max_menu_id + 1, 4, '#', '', 1, 0, 'F', '0', '0', 'line:template:remove', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 6, '範本匯出', @max_menu_id + 1, 5, '#', '', 1, 0, 'F', '0', '0', 'line:template:export', '#',
        'admin', NOW(), '', NULL, '');

-- =========================================
-- 為管理員角色新增權限
-- =========================================

-- 取得管理員角色ID
SET @admin_role_id = (SELECT role_id
                      FROM sys_role
                      WHERE role_key = 'admin'
                      LIMIT 1);

-- 為管理員角色新增訊息範本相關權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_id BETWEEN @max_menu_id + 1 AND @max_menu_id + 6;

-- =========================================
-- 新增資料字典
-- =========================================

-- 新增 LINE 訊息範本類型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES ((SELECT IFNULL(MAX(dict_id), 100) + 1 FROM (SELECT dict_id FROM sys_dict_type) AS temp), 'LINE訊息範本類型',
        'line_template_type', '0', 'admin', NOW(), '', NULL, 'LINE 訊息範本類型');

-- 取得 dict_code 最大值
SET @max_dict_code = (SELECT IFNULL(MAX(dict_code), 100)
                      FROM sys_dict_data);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (@max_dict_code + 1, 1, '純文字', 'TEXT', 'line_template_type', '', 'primary', 'Y', '0', 'admin', NOW(), '',
        NULL,
        ''),
       (@max_dict_code + 2, 2, '圖片', 'IMAGE', 'line_template_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL,
        ''),
       (@max_dict_code + 3, 3, '影片', 'VIDEO', 'line_template_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL,
        ''),
       (@max_dict_code + 4, 4, '音訊', 'AUDIO', 'line_template_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL,
        ''),
       (@max_dict_code + 5, 5, '位置', 'LOCATION', 'line_template_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL,
        ''),
       (@max_dict_code + 6, 6, '貼圖', 'STICKER', 'line_template_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL,
        ''),
       (@max_dict_code + 7, 7, '圖片地圖', 'IMAGEMAP', 'line_template_type', '', 'warning', 'N', '0', 'admin', NOW(),
        '',
        NULL, ''),
       (@max_dict_code + 8, 8, 'Flex訊息', 'FLEX', 'line_template_type', '', 'danger', 'N', '0', 'admin', NOW(), '',
        NULL, '');

-- 新增訊息數量欄位到訊息範本表
-- 用於記錄範本包含的訊息物件數量（1-5，符合 LINE Push Message API 限制）

ALTER TABLE line_message_template
    ADD COLUMN message_count INT DEFAULT 1 COMMENT '訊息物件數量（1-5）' AFTER sort_order;

-- 更新現有記錄的訊息數量（預設為 1）
UPDATE line_message_template SET message_count = 1 WHERE message_count IS NULL;
