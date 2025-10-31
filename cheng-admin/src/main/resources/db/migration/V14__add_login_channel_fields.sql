-- =========================================
-- LINE 模組擴充：新增 LOGIN 頻道欄位
-- 版本：V14
-- 建立時間：2025-10-30
-- 說明：新增 login_channel_id 和 login_channel_secret 欄位
--       用於支援 LINE Login 功能
-- =========================================

-- 在 sys_line_config 表中新增 LOGIN 頻道相關欄位
ALTER TABLE sys_line_config
    ADD COLUMN login_channel_id     VARCHAR(255) DEFAULT NULL COMMENT 'LINE Login Channel ID' AFTER channel_secret,
    ADD COLUMN login_channel_secret VARCHAR(500) DEFAULT NULL COMMENT 'LINE Login Channel Secret' AFTER login_channel_id;
