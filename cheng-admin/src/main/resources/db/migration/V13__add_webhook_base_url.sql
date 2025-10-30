-- =========================================
-- LINE 模組 Webhook Base URL 功能
-- 版本：V13
-- 建立時間：2025-10-28
-- 說明：新增 webhook_base_url 欄位，支援自訂 API 網域
-- =========================================

-- 新增 webhook_base_url 欄位（可選，用於自訂 API 網域）
ALTER TABLE sys_line_config
    ADD COLUMN webhook_base_url VARCHAR(255) DEFAULT NULL
        COMMENT 'Webhook 基礎 URL（選填，若為空則使用系統預設值）'
        AFTER channel_access_token;

-- 更新表註解
ALTER TABLE sys_line_config
    COMMENT = 'LINE 頻道設定表（支援自訂 Webhook 網域）';
