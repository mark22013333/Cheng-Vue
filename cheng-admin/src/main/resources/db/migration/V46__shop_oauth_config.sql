-- =============================================
-- 商城第三方登入 (OAuth) 系統設定
-- =============================================

-- LINE Login 設定
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('商城 LINE 登入啟用', 'shop.oauth.line.enabled', '0', 'Y', '1=啟用, 0=停用', 'admin', NOW());

INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('商城 LINE Channel ID', 'shop.oauth.line.channel_id', '', 'Y', 'LINE Login Channel ID', 'admin', NOW());

INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('商城 LINE Channel Secret', 'shop.oauth.line.channel_secret', '', 'Y',
        'LINE Login Channel Secret（建議使用 ENC() 加密）', 'admin', NOW());

-- Google Login 設定（預留）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('商城 Google 登入啟用', 'shop.oauth.google.enabled', '0', 'Y', '1=啟用, 0=停用', 'admin', NOW());

INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('商城 Google Client ID', 'shop.oauth.google.client_id', '', 'Y', 'Google OAuth Client ID', 'admin', NOW());

INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('商城 Google Client Secret', 'shop.oauth.google.client_secret', '', 'Y',
        'Google OAuth Client Secret（建議使用 ENC() 加密）', 'admin', NOW());

-- LINE 登入設定統一至 LINE 頻道設定（sys_line_config.login_channel_id / login_channel_secret）
-- 移除 sys_config 中重複的 LINE OAuth 設定
DELETE FROM sys_config WHERE config_key = 'shop.oauth.line.enabled';
DELETE FROM sys_config WHERE config_key = 'shop.oauth.line.channel_id';
DELETE FROM sys_config WHERE config_key = 'shop.oauth.line.channel_secret';
