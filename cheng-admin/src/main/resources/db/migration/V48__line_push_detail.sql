-- LINE 逐人推播明細表
CREATE TABLE line_push_detail (
    detail_id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明細 ID',
    message_id        BIGINT        NOT NULL                COMMENT '訊息日誌 ID (FK line_message_log)',
    line_user_id      VARCHAR(50)   NOT NULL                COMMENT 'LINE 使用者 ID',
    line_display_name VARCHAR(100)                          COMMENT 'LINE 顯示名稱（快照）',
    status            VARCHAR(10)   NOT NULL DEFAULT 'PENDING' COMMENT '狀態：SUCCESS/FAILED/BLOCKED/PENDING',
    retry_count       INT           NOT NULL DEFAULT 0      COMMENT '重試次數',
    error_message     VARCHAR(500)                          COMMENT '錯誤訊息',
    send_time         DATETIME                              COMMENT '發送時間',
    PRIMARY KEY (detail_id),
    INDEX idx_push_detail_message_id (message_id),
    INDEX idx_push_detail_user_id (line_user_id),
    INDEX idx_push_detail_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LINE 逐人推播明細';
