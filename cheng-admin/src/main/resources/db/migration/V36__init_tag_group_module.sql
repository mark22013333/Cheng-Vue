-- =========================================
-- 標籤群組模組初始化 SQL
-- 版本：V36
-- 建立時間：2026-01-12
-- 說明：建立標籤群組功能
--       - sys_tag_group：群組主表
--       - sys_tag_group_detail：群組明細表（最多 5 個標籤）
--       - 支援兩種運算模式：LEFT_TO_RIGHT、OR_OF_AND
--       - 設計考量 500 萬資料量級的效能
-- =========================================

-- =========================================
-- 1. 建立標籤群組主表
-- =========================================
CREATE TABLE IF NOT EXISTS sys_tag_group
(
    group_id        BIGINT AUTO_INCREMENT COMMENT '群組ID',
    group_name      VARCHAR(100) NOT NULL COMMENT '群組名稱',
    group_code      VARCHAR(100) NOT NULL COMMENT '群組代碼（唯一）',
    platform_scope  VARCHAR(50)  NOT NULL DEFAULT 'LINE' COMMENT '適用平台：LINE/INVENTORY',
    calc_mode       VARCHAR(20)  DEFAULT NULL COMMENT '運算模式：LEFT_TO_RIGHT（預設）/OR_OF_AND',
    count_result    INT          DEFAULT 0 COMMENT '符合群組的結果數量（批次運算後回寫）',
    last_calc_time  DATETIME     DEFAULT NULL COMMENT '最後運算時間',
    description     VARCHAR(500) DEFAULT NULL COMMENT '群組描述',
    status          TINYINT      DEFAULT 1 COMMENT '狀態：0=停用, 1=啟用',
    create_by       VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (group_id),
    UNIQUE KEY uk_group_code (group_code),
    INDEX idx_platform_scope (platform_scope),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='標籤群組主表';

-- =========================================
-- 2. 建立標籤群組明細表
-- =========================================
CREATE TABLE IF NOT EXISTS sys_tag_group_detail
(
    id           BIGINT AUTO_INCREMENT COMMENT '主鍵ID',
    group_id     BIGINT      NOT NULL COMMENT '群組ID（對應 sys_tag_group.group_id）',
    group_index  INT         NOT NULL COMMENT '規則順序（1-5，用於 LEFT_TO_RIGHT 模式）',
    tag_id       BIGINT      NOT NULL COMMENT '標籤ID（對應 sys_tag.tag_id）',
    operator     VARCHAR(10) DEFAULT NULL COMMENT '運算子：AND/OR（第一筆可為空）',
    create_by    VARCHAR(64) DEFAULT '' COMMENT '建立者',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_index (group_id, group_index),
    INDEX idx_group_id (group_id),
    INDEX idx_tag_id (tag_id),
    CONSTRAINT fk_tag_group_detail_group FOREIGN KEY (group_id) REFERENCES sys_tag_group (group_id) ON DELETE CASCADE,
    CONSTRAINT fk_tag_group_detail_tag FOREIGN KEY (tag_id) REFERENCES sys_tag (tag_id) ON DELETE RESTRICT,
    CONSTRAINT chk_group_index CHECK (group_index >= 1 AND group_index <= 5),
    CONSTRAINT chk_operator CHECK (operator IS NULL OR operator IN ('AND', 'OR'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='標籤群組明細表';

-- =========================================
-- 3. 建立群組運算結果暫存表（用於大量資料批次運算）
-- =========================================
CREATE TABLE IF NOT EXISTS sys_tag_group_staging
(
    id           BIGINT AUTO_INCREMENT COMMENT '主鍵ID',
    batch_id     VARCHAR(64)  NOT NULL COMMENT '批次ID（用於隔離不同批次）',
    group_id     BIGINT       NOT NULL COMMENT '群組ID',
    target_id    VARCHAR(255) NOT NULL COMMENT '目標ID（LINE 使用者ID 或物品ID）',
    target_type  VARCHAR(20)  NOT NULL COMMENT '目標類型：LINE_USER/INV_ITEM',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_group_id (group_id),
    INDEX idx_target (target_type, target_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='標籤群組運算暫存表';

-- =========================================
-- 4. 新增選單項目
-- =========================================

-- 取得標籤管理的 menu_id
SET @tag_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '標籤管理' AND parent_id = 0 LIMIT 1);

-- 取得 menu_id 最大值
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 3100) FROM sys_menu);

-- 4.1 新增「標籤群組」二級選單（目錄）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 1, '標籤群組', @tag_menu_id, 3, 'group', NULL, 1, 0, 'M', '0', '0', '', 'tree-table', 'admin',
        NOW(), '', NULL, '標籤群組管理');

SET @group_menu_id = @max_menu_id + 1;

-- 4.2 新增「LINE 群組」頁面
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 2, 'LINE 標籤群組', @group_menu_id, 1, 'line-tag-group', 'tag/group/line/index', 1, 0, 'C', '0', '0',
        'tag:group:line:list', 'peoples', 'admin', NOW(), '', NULL, 'LINE 標籤群組管理');

-- 4.3 新增「庫存群組」頁面
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 3, '庫存標籤群組', @group_menu_id, 2, 'inventory-tag-group', 'tag/group/inventory/index', 1, 0, 'C', '0', '0',
        'tag:group:inventory:list', 'shopping', 'admin', NOW(), '', NULL, '庫存標籤群組管理');

-- =========================================
-- 5. 新增按鈕權限
-- =========================================

-- 5.1 LINE 標籤群組按鈕權限
SET @line_group_menu_id = @max_menu_id + 2;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 4, '群組查詢', @line_group_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'tag:group:line:query', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 5, '群組新增', @line_group_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'tag:group:line:add', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 6, '群組修改', @line_group_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'tag:group:line:edit', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 7, '群組刪除', @line_group_menu_id, 4, '', '', 1, 0, 'F', '0', '0', 'tag:group:line:remove', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 8, '執行運算', @line_group_menu_id, 5, '', '', 1, 0, 'F', '0', '0', 'tag:group:line:calc', '#',
        'admin', NOW(), '', NULL, '');

-- 5.2 庫存標籤群組按鈕權限
SET @inv_group_menu_id = @max_menu_id + 3;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 9, '群組查詢', @inv_group_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'tag:group:inventory:query', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 10, '群組新增', @inv_group_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'tag:group:inventory:add', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 11, '群組修改', @inv_group_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'tag:group:inventory:edit', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 12, '群組刪除', @inv_group_menu_id, 4, '', '', 1, 0, 'F', '0', '0', 'tag:group:inventory:remove', '#',
        'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 13, '執行運算', @inv_group_menu_id, 5, '', '', 1, 0, 'F', '0', '0', 'tag:group:inventory:calc', '#',
        'admin', NOW(), '', NULL, '');

-- =========================================
-- 6. 為管理員角色分配標籤群組權限
-- =========================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE perms LIKE 'tag:group:%'
   OR menu_name = '標籤群組'
   OR menu_name = 'LINE 標籤群組'
   OR menu_name = '庫存標籤群組';
