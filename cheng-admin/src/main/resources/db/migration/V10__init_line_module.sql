-- =========================================
-- LINE 模組初始化 SQL
-- 版本：V10
-- 建立時間：2025-10-26
-- 說明：建立 LINE 模組所需的資料表、選單、權限
-- =========================================

-- 1. 建立 LINE 頻道設定表
CREATE TABLE IF NOT EXISTS sys_line_config
(
    config_id            INT AUTO_INCREMENT COMMENT '設定ID',
    channel_type         VARCHAR(20)   NOT NULL COMMENT '頻道類型：MAIN(主頻道)/SUB(副頻道)/TEST(測試頻道)',
    channel_name         VARCHAR(100)  NOT NULL COMMENT '頻道名稱',
    channel_id           VARCHAR(255)  NOT NULL COMMENT 'LINE Channel ID (加密)',
    channel_secret       VARCHAR(500)  NOT NULL COMMENT 'LINE Channel Secret (加密)',
    channel_access_token VARCHAR(1000) NOT NULL COMMENT 'Channel Access Token (加密)',
    webhook_url          VARCHAR(500) DEFAULT NULL COMMENT 'Webhook URL',
    webhook_status       TINYINT      DEFAULT 0 COMMENT 'Webhook 狀態：0=未驗證, 1=已驗證',
    status               TINYINT      DEFAULT 1 COMMENT '啟用狀態：0=停用, 1=啟用',
    is_default           TINYINT      DEFAULT 0 COMMENT '是否為預設頻道：0=否, 1=是',
    sort_order           INT          DEFAULT 0 COMMENT '排序順序',
    create_by            VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time          DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by            VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark               VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_channel_type (channel_type),
    INDEX idx_status (status),
    INDEX idx_channel_type (channel_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE 頻道設定表';

-- 2. 建立推播訊息記錄表
CREATE TABLE IF NOT EXISTS sys_line_message_log
(
    message_id          BIGINT AUTO_INCREMENT COMMENT '訊息ID',
    config_id           INT         NOT NULL COMMENT '使用的頻道設定ID',
    message_type        VARCHAR(20) NOT NULL COMMENT '訊息類型：REPLY/PUSH/MULTICAST/BROADCAST',
    content_type        VARCHAR(20) NOT NULL COMMENT '內容類型：TEXT/IMAGE/TEMPLATE/FLEX',
    message_content     TEXT        NOT NULL COMMENT '訊息內容 (JSON 格式)',
    target_type         VARCHAR(20) NOT NULL COMMENT '推播對象類型：SINGLE/MULTIPLE/TAG/ALL',
    target_line_user_id VARCHAR(255) DEFAULT NULL COMMENT '目標 LINE 使用者 ID (單人推播時使用)',
    target_user_ids     TEXT         DEFAULT NULL COMMENT '目標 LINE 使用者 ID 列表 (JSON Array，多人推播時使用)',
    target_tag_id       BIGINT       DEFAULT NULL COMMENT '目標標籤 ID (標籤推播時使用，預留欄位)',
    target_count        INT          DEFAULT 0 COMMENT '目標數量',
    success_count       INT          DEFAULT 0 COMMENT '成功數量',
    fail_count          INT          DEFAULT 0 COMMENT '失敗數量',
    send_status         VARCHAR(20) NOT NULL COMMENT '發送狀態：PENDING/SENDING/SUCCESS/PARTIAL_SUCCESS/FAILED',
    error_message       TEXT         DEFAULT NULL COMMENT '錯誤訊息',
    line_request_id     VARCHAR(100) DEFAULT NULL COMMENT 'LINE API Request ID',
    send_time           DATETIME     DEFAULT NULL COMMENT '實際發送時間',
    create_by           VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (message_id),
    INDEX idx_config_id (config_id),
    INDEX idx_message_type (message_type),
    INDEX idx_target_type (target_type),
    INDEX idx_send_status (send_status),
    INDEX idx_target_line_user_id (target_line_user_id),
    INDEX idx_target_tag_id (target_tag_id),
    INDEX idx_create_time (create_time),
    INDEX idx_send_time (send_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE 推播訊息記錄表';

-- 3. 建立 LINE 使用者表
CREATE TABLE IF NOT EXISTS sys_line_user
(
    id                      BIGINT AUTO_INCREMENT COMMENT '主鍵ID',
    line_user_id            VARCHAR(255) NOT NULL COMMENT 'LINE 使用者 ID',
    line_display_name       VARCHAR(255)          DEFAULT NULL COMMENT 'LINE 顯示名稱',
    line_picture_url        VARCHAR(500)          DEFAULT NULL COMMENT 'LINE 頭像 URL',
    line_status_message     VARCHAR(500)          DEFAULT NULL COMMENT 'LINE 狀態訊息',
    line_language           VARCHAR(10)           DEFAULT NULL COMMENT 'LINE 語言設定',
    sys_user_id             BIGINT                DEFAULT NULL COMMENT '系統使用者 ID',
    bind_status             VARCHAR(20)  NOT NULL DEFAULT 'UNBOUND' COMMENT '綁定狀態：UNBOUND(未綁定)/BOUND(已綁定)',
    follow_status           VARCHAR(20)  NOT NULL DEFAULT 'UNFOLLOWED' COMMENT '關注狀態：UNFOLLOWED(未關注)/FOLLOWING(關注中)/BLOCKED(已封鎖)',
    first_follow_time       DATETIME              DEFAULT NULL COMMENT '初次加入時間（首次關注）',
    latest_follow_time      DATETIME              DEFAULT NULL COMMENT '最近一次關注時間',
    unfollow_time           DATETIME              DEFAULT NULL COMMENT '取消關注時間',
    block_time              DATETIME              DEFAULT NULL COMMENT '封鎖時間',
    first_bind_time         DATETIME              DEFAULT NULL COMMENT '初次綁定時間',
    latest_bind_time        DATETIME              DEFAULT NULL COMMENT '最近一次綁定時間',
    unbind_time             DATETIME              DEFAULT NULL COMMENT '解除綁定時間',
    bind_count              INT                   DEFAULT 0 COMMENT '綁定次數',
    total_messages_sent     INT                   DEFAULT 0 COMMENT '累計發送訊息數',
    total_messages_received INT                   DEFAULT 0 COMMENT '累計接收訊息數',
    last_interaction_time   DATETIME              DEFAULT NULL COMMENT '最後互動時間',
    create_time             DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_time             DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark                  VARCHAR(500)          DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (id),
    UNIQUE KEY uk_line_user_id (line_user_id),
    INDEX idx_sys_user_id (sys_user_id),
    INDEX idx_bind_status (bind_status),
    INDEX idx_follow_status (follow_status),
    INDEX idx_first_follow_time (first_follow_time),
    INDEX idx_last_interaction_time (last_interaction_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE 使用者表';

-- 4. 建立 LINE 使用者標籤表（預留）
CREATE TABLE IF NOT EXISTS sys_line_user_tag
(
    tag_id          BIGINT AUTO_INCREMENT COMMENT '標籤ID',
    tag_name        VARCHAR(100) NOT NULL COMMENT '標籤名稱',
    tag_code        VARCHAR(100) NOT NULL COMMENT '標籤代碼',
    tag_color       VARCHAR(20)  DEFAULT NULL COMMENT '標籤顏色（前端顯示用）',
    tag_description VARCHAR(500) DEFAULT NULL COMMENT '標籤描述',
    user_count      INT          DEFAULT 0 COMMENT '標籤下的使用者數量',
    status          TINYINT      DEFAULT 1 COMMENT '狀態：0=停用, 1=啟用',
    sort_order      INT          DEFAULT 0 COMMENT '排序順序',
    create_by       VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (tag_id),
    UNIQUE KEY uk_tag_code (tag_code),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE 使用者標籤表';

-- 5. 建立 LINE 使用者標籤關聯表（預留）
CREATE TABLE IF NOT EXISTS sys_line_user_tag_relation
(
    id           BIGINT AUTO_INCREMENT COMMENT '主鍵ID',
    line_user_id VARCHAR(255) NOT NULL COMMENT 'LINE 使用者 ID',
    tag_id       BIGINT       NOT NULL COMMENT '標籤ID',
    create_by    VARCHAR(64) DEFAULT '' COMMENT '建立者',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_tag (line_user_id, tag_id),
    INDEX idx_line_user_id (line_user_id),
    INDEX idx_tag_id (tag_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE 使用者標籤關聯表';

-- =========================================
-- 新增選單項目
-- =========================================

-- 取得 menu_id 最大值
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 2000)
                    FROM sys_menu);

-- 新增「行銷管理」一級選單
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 1, '行銷管理', 0, 6, 'marketing', NULL, 1, 0, 'M', '0', '0', '', 'chart', 'admin', NOW(), '',
        NULL, '行銷功能模組');

-- 新增「LINE 管理」二級選單
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 2, 'LINE 管理', @max_menu_id + 1, 1, 'line', NULL, 1, 0, 'M', '0', '0', '', 'message', 'admin',
        NOW(), '', NULL, 'LINE 模組');

-- 新增「LINE 設定」選單
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 3, 'LINE 設定', @max_menu_id + 2, 1, 'config', 'line/config/index', 1, 0, 'C', '0', '0',
        'line:config:list', 'setting', 'admin', NOW(), '', NULL, 'LINE 頻道設定');

-- 新增「推播訊息」選單
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 4, '推播訊息', @max_menu_id + 2, 2, 'message', 'line/message/index', 1, 0, 'C', '0', '0',
        'line:message:list', 'email', 'admin', NOW(), '', NULL, '推播訊息管理');

-- 新增「LINE 使用者」選單
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 5, 'LINE 使用者', @max_menu_id + 2, 3, 'user', 'line/user/index', 1, 0, 'C', '0', '0',
        'line:user:list', 'peoples', 'admin', NOW(), '', NULL, 'LINE 使用者管理');

-- 新增 LINE 設定相關按鈕權限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 11, 'LINE設定查詢', @max_menu_id + 3, 1, '#', '', 1, 0, 'F', '0', '0', 'line:config:query', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 12, 'LINE設定新增', @max_menu_id + 3, 2, '#', '', 1, 0, 'F', '0', '0', 'line:config:add', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 13, 'LINE設定修改', @max_menu_id + 3, 3, '#', '', 1, 0, 'F', '0', '0', 'line:config:edit', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 14, 'LINE設定刪除', @max_menu_id + 3, 4, '#', '', 1, 0, 'F', '0', '0', 'line:config:remove', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 15, 'LINE連線測試', @max_menu_id + 3, 5, '#', '', 1, 0, 'F', '0', '0', 'line:config:test', '#',
        'admin', NOW(), '', NULL, '');

-- 新增推播訊息相關按鈕權限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 21, '推播訊息查詢', @max_menu_id + 4, 1, '#', '', 1, 0, 'F', '0', '0', 'line:message:query', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 22, '發送推播訊息', @max_menu_id + 4, 2, '#', '', 1, 0, 'F', '0', '0', 'line:message:send', '#',
        'admin', NOW(), '', NULL, '');

-- 新增 LINE 使用者相關按鈕權限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 31, 'LINE使用者查詢', @max_menu_id + 5, 1, '#', '', 1, 0, 'F', '0', '0', 'line:user:query', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 32, 'LINE使用者綁定', @max_menu_id + 5, 2, '#', '', 1, 0, 'F', '0', '0', 'line:user:bind', '#',
        'admin', NOW(), '', NULL, '');

-- =========================================
-- 為管理員角色新增 LINE 模組權限
-- =========================================

-- 取得管理員角色ID
SET @admin_role_id = (SELECT role_id
                      FROM sys_role
                      WHERE role_key = 'admin'
                      LIMIT 1);

-- 為管理員角色新增所有 LINE 選單權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_id BETWEEN @max_menu_id + 1 AND @max_menu_id + 50;

-- =========================================
-- 新增資料字典
-- =========================================

-- 新增 LINE 頻道類型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES ((SELECT IFNULL(MAX(dict_id), 100) + 1 FROM (SELECT dict_id FROM sys_dict_type) AS temp), 'LINE頻道類型',
        'line_channel_type', '0', 'admin', NOW(), '', NULL, 'LINE 頻道類型列表');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES ((SELECT IFNULL(MAX(dict_code), 100) + 1 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 1, '主頻道',
        'MAIN', 'line_channel_type', '', 'primary', 'Y', '0', 'admin', NOW(), '', NULL, '正式環境使用的主頻道'),
       ((SELECT IFNULL(MAX(dict_code), 100) + 2 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 2, '副頻道', 'SUB',
        'line_channel_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '備用頻道'),
       ((SELECT IFNULL(MAX(dict_code), 100) + 3 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 3, '測試頻道',
        'TEST', 'line_channel_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '測試環境使用');

-- 新增 LINE 訊息類型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES ((SELECT IFNULL(MAX(dict_id), 100) + 2 FROM (SELECT dict_id FROM sys_dict_type) AS temp), 'LINE訊息類型',
        'line_message_type', '0', 'admin', NOW(), '', NULL, 'LINE 訊息類型列表');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES ((SELECT IFNULL(MAX(dict_code), 100) + 4 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 1, '回覆訊息',
        'REPLY', 'line_message_type', '', 'default', 'N', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 5 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 2,
        '推播訊息(單人)', 'PUSH', 'line_message_type', '', 'primary', 'Y', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 6 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 3,
        '推播訊息(多人)', 'MULTICAST', 'line_message_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 7 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 4, '廣播訊息',
        'BROADCAST', 'line_message_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 新增 LINE 內容類型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES ((SELECT IFNULL(MAX(dict_id), 100) + 3 FROM (SELECT dict_id FROM sys_dict_type) AS temp), 'LINE內容類型',
        'line_content_type', '0', 'admin', NOW(), '', NULL, 'LINE 訊息內容類型');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES ((SELECT IFNULL(MAX(dict_code), 100) + 8 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 1, '純文字',
        'TEXT', 'line_content_type', '', 'primary', 'Y', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 9 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 2, '圖片訊息',
        'IMAGE', 'line_content_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 10 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 3, '模板訊息',
        'TEMPLATE', 'line_content_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 11 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 4, 'Flex訊息',
        'FLEX', 'line_content_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 新增 LINE 發送狀態字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES ((SELECT IFNULL(MAX(dict_id), 100) + 4 FROM (SELECT dict_id FROM sys_dict_type) AS temp), 'LINE發送狀態',
        'line_send_status', '0', 'admin', NOW(), '', NULL, 'LINE 訊息發送狀態');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES ((SELECT IFNULL(MAX(dict_code), 100) + 12 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 1, '待發送',
        'PENDING', 'line_send_status', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 13 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 2, '發送中',
        'SENDING', 'line_send_status', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 14 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 3, '發送成功',
        'SUCCESS', 'line_send_status', '', 'success', 'Y', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 15 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 4, '部分成功',
        'PARTIAL_SUCCESS', 'line_send_status', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, ''),
       ((SELECT IFNULL(MAX(dict_code), 100) + 16 FROM (SELECT dict_code FROM sys_dict_data) AS temp), 5, '發送失敗',
        'FAILED', 'line_send_status', '', 'danger', 'N', '0', 'admin', NOW(), '', NULL, '');
