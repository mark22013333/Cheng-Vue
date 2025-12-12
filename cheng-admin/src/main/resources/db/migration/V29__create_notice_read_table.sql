-- ============================
-- CoolApps 管理系統 - 通知公告已讀記錄表
-- 版本：V29
-- 說明：建立通知公告已讀記錄表，用於追蹤使用者的已讀狀態
-- ============================

-- ----------------------------
-- 通知公告已讀記錄表
-- ----------------------------
DROP TABLE IF EXISTS sys_notice_read;
CREATE TABLE sys_notice_read
(
    read_id     BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '已讀記錄ID',
    notice_id   INT(4)     NOT NULL COMMENT '通知公告ID',
    user_id     BIGINT(20) NOT NULL COMMENT '使用者ID',
    read_time   DATETIME   NOT NULL COMMENT '已讀時間',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (read_id),
    UNIQUE KEY uk_notice_user (notice_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_notice_id (notice_id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT = '通知公告已讀記錄表';
