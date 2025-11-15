-- Rich Menu Alias 管理表
-- 用於管理 LINE Rich Menu 的別名，方便 Rich Menu Switch 功能使用
-- 參考: https://developers.line.biz/en/reference/messaging-api/#create-rich-menu-alias

CREATE TABLE IF NOT EXISTS `sys_line_rich_menu_alias`
(
    `id`           BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
    `rich_menu_id` BIGINT(20)  NOT NULL COMMENT 'Rich Menu ID（關聯 sys_line_rich_menu.id）',
    `alias_id`     VARCHAR(32) NOT NULL COMMENT 'Rich Menu Alias ID（LINE API 識別碼，最多 32 字元）',
    `description`  VARCHAR(200) DEFAULT NULL COMMENT '別名描述',
    `create_by`    VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `update_by`    VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    `remark`       VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_alias_id` (`alias_id`),
    KEY `idx_rich_menu_id` (`rich_menu_id`),
    CONSTRAINT `fk_alias_rich_menu` FOREIGN KEY (`rich_menu_id`) REFERENCES `sys_line_rich_menu` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LINE Rich Menu Alias 管理表';

-- 插入範例資料（選用）
-- INSERT INTO `sys_line_rich_menu_alias` (`rich_menu_id`, `alias_id`, `description`, `create_by`, `remark`)
-- VALUES (1, 'richmenu-alias-home', '主選單別名', 'admin', '用於 Rich Menu Switch');
