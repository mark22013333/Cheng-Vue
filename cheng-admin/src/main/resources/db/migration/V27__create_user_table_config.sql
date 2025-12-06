-- ============================================================
-- 使用者表格欄位配置表
-- 用於儲存每個使用者在不同頁面的表格欄位顯示偏好
-- ============================================================

CREATE TABLE sys_user_table_config
(
    config_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    user_id       BIGINT       NOT NULL COMMENT '使用者ID',
    page_key      VARCHAR(100) NOT NULL COMMENT '頁面標識（如 system_user, inventory_borrow）',
    column_config TEXT         NOT NULL COMMENT '欄位配置（JSON格式）',
    create_by     VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '備註',
    UNIQUE KEY uk_user_page (user_id, page_key),
    INDEX idx_user_id (user_id),
    INDEX idx_page_key (page_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='使用者表格欄位配置表';

-- ============================================================
-- 範例資料說明
-- column_config JSON 格式範例：
-- {
--   "userId": {"label": "使用者編號", "visible": true},
--   "userName": {"label": "使用者名稱", "visible": false},
--   "nickName": {"label": "使用者暱稱", "visible": true}
-- }
-- ============================================================
