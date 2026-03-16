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
DELETE
FROM sys_config
WHERE config_key = 'shop.oauth.line.enabled';
DELETE
FROM sys_config
WHERE config_key = 'shop.oauth.line.channel_id';
DELETE
FROM sys_config
WHERE config_key = 'shop.oauth.line.channel_secret';

-- 商城公告欄區塊：前台頂部滾動公告（支援連結跳轉）
-- content 欄位格式: JSON 陣列，每項包含 text / linkType / linkValue
-- linkType: NONE（純文字）| PRODUCT（商品）| ARTICLE（文章）| CATEGORY（分類）| URL（自訂網址）
INSERT INTO shop_page_block (page_key, block_key, block_type, title, sub_title, content, sort_order, status,
                             create_time)
VALUES ('GLOBAL', 'ANNOUNCEMENT_BAR', 'TEXT', '商城公告欄',
        '前台頂部滾動公告文字，支援連結跳轉',
        '[{"text":"全館滿 $999 免運費","linkType":"NONE","linkValue":""},{"text":"新會員首單享 9 折優惠","linkType":"NONE","linkValue":""},{"text":"夏日特賣活動進行中","linkType":"URL","linkValue":"/products?isHot=true"}]',
        0, 'ENABLED', NOW());
