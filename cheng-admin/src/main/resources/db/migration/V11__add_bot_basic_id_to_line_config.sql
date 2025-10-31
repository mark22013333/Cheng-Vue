-- =========================================
-- LINE 模組更新 - 新增 Bot Basic ID 欄位
-- 版本：V11
-- 建立時間：2025-10-28
-- 說明：新增 bot_basic_id 欄位，用於 Webhook URL 參數
-- =========================================

-- 1. 新增 bot_basic_id 欄位到 sys_line_config 表
ALTER TABLE sys_line_config
    ADD COLUMN bot_basic_id VARCHAR(100) NULL COMMENT 'Bot Basic ID（例如：@322okyxf）' AFTER channel_id,
    ADD UNIQUE KEY uk_bot_basic_id (bot_basic_id);

-- 2. 移除舊的 channel_type 唯一鍵約束（允許多個相同類型的頻道）
ALTER TABLE sys_line_config DROP INDEX uk_channel_type;

-- 3. 新增索引以提升查詢效能
ALTER TABLE sys_line_config ADD INDEX idx_bot_basic_id (bot_basic_id);

-- 4. 更新備註說明
ALTER TABLE sys_line_config MODIFY COLUMN webhook_url VARCHAR(500) DEFAULT NULL COMMENT 'Webhook URL（格式：https://domain.com/webhook/line/@botid）';
