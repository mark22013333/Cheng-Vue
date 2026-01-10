-- =========================================
-- Flex 範本儲存與發送權限
-- 版本：V34
-- 建立時間：2025-01-06
-- 說明：
--   1. 建立 Flex 自訂範本表（供使用者儲存常用 Flex JSON）
--   2. 新增建立者 ID 欄位到訊息範本表
--   3. 新增發送權限控制
-- =========================================

-- =========================================
-- 1. 建立 Flex 自訂範本表
-- =========================================
CREATE TABLE IF NOT EXISTS line_flex_template
(
    flex_template_id BIGINT AUTO_INCREMENT COMMENT 'Flex範本ID',
    template_name    VARCHAR(100) NOT NULL COMMENT '範本名稱',
    flex_json        LONGTEXT     NOT NULL COMMENT 'Flex JSON 內容',
    alt_text         VARCHAR(400) DEFAULT '訊息通知' COMMENT '替代文字',
    preview_url      VARCHAR(500) DEFAULT NULL COMMENT '預覽圖 URL',
    description      VARCHAR(500) DEFAULT NULL COMMENT '範本說明',
    is_public        TINYINT      DEFAULT 0 COMMENT '是否公開：0=私人, 1=公開（所有人可用）',
    use_count        INT          DEFAULT 0 COMMENT '使用次數',
    creator_id       BIGINT       NOT NULL COMMENT '建立者使用者ID',
    creator_name     VARCHAR(64)  DEFAULT '' COMMENT '建立者名稱（冗餘欄位）',
    status           TINYINT      DEFAULT 1 COMMENT '狀態：0=停用, 1=啟用',
    del_flag         CHAR(1)      DEFAULT '0' COMMENT '刪除標誌：0=正常, 2=刪除',
    create_by        VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by        VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (flex_template_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_is_public (is_public),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE Flex 自訂範本表';

-- =========================================
-- 2. 新增建立者 ID 欄位到訊息範本表
-- =========================================
ALTER TABLE line_message_template
    ADD COLUMN creator_id   BIGINT      DEFAULT NULL COMMENT '建立者使用者ID' AFTER create_by,
    ADD COLUMN creator_name VARCHAR(64) DEFAULT '' COMMENT '建立者名稱（冗餘欄位）' AFTER creator_id,
    ADD INDEX idx_creator_id (creator_id);

-- =========================================
-- 3. 新增發送權限
-- =========================================

-- 取得 LINE 管理選單的 menu_id
SET @line_menu_id = (SELECT menu_id
                     FROM sys_menu
                     WHERE menu_name = 'LINE 管理'
                       AND menu_type = 'M'
                     LIMIT 1);

-- 取得推播訊息選單的 menu_id
SET @push_menu_id = (SELECT menu_id
                     FROM sys_menu
                     WHERE menu_name = '推播訊息'
                       AND parent_id = @line_menu_id
                     LIMIT 1);

-- 取得 menu_id 最大值
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 4000)
                    FROM sys_menu);

-- 新增「發送權限」按鈕（掛在推播訊息下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 1, '訊息發送', @push_menu_id, 10, '#', '', 1, 0, 'F', '0', '0', 'line:message:send', '#',
        'admin', NOW(), '', NULL, '允許發送 LINE 推播訊息');

-- 取得範本列表選單的 menu_id
SET @template_menu_id = (SELECT menu_id
                         FROM sys_menu
                         WHERE menu_name = '訊息範本'
                           AND parent_id = @line_menu_id
                         LIMIT 1);

-- 新增「範本發送」按鈕（掛在訊息範本下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 2, '範本發送', @template_menu_id, 10, '#', '', 1, 0, 'F', '0', '0', 'line:template:send', '#',
        'admin', NOW(), '', NULL, '允許使用範本發送測試推播');

-- =========================================
-- 4. 為管理員角色新增權限
-- =========================================

-- 取得管理員角色ID
SET @admin_role_id = (SELECT role_id
                      FROM sys_role
                      WHERE role_key = 'admin'
                      LIMIT 1);

-- 為管理員角色新增發送權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_id BETWEEN @max_menu_id + 1 AND @max_menu_id + 2;
