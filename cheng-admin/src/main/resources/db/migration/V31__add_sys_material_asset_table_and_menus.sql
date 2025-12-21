-- =========================================
-- 素材管理 - 建立素材資料表與選單權限
-- 版本：V31
-- 建立時間：2025-12-20
-- 說明：新增素材管理（音檔/影片/圖片）共用素材庫
-- =========================================

-- 1) 建立素材主檔資料表
CREATE TABLE IF NOT EXISTS sys_material_asset (
    asset_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '素材ID',
    asset_type    VARCHAR(20)  NOT NULL COMMENT '素材類型：AUDIO/VIDEO/IMAGE',
    original_name VARCHAR(255)          DEFAULT NULL COMMENT '原始檔名',
    file_name     VARCHAR(255) NOT NULL COMMENT '儲存檔名',
    file_ext      VARCHAR(20)           DEFAULT NULL COMMENT '副檔名（不含點）',
    mime_type     VARCHAR(100)          DEFAULT NULL COMMENT 'MIME Type',
    file_size     BIGINT               DEFAULT NULL COMMENT '檔案大小（bytes）',
    duration_ms   BIGINT               DEFAULT NULL COMMENT '長度（毫秒，音檔/影片）',
    relative_path VARCHAR(500) NOT NULL COMMENT '相對路徑（material/**）',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態：ACTIVE/DISABLED',
    remark        VARCHAR(500)          DEFAULT NULL COMMENT '備註',
    create_by     VARCHAR(64)           DEFAULT '' COMMENT '建立者',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by     VARCHAR(64)           DEFAULT '' COMMENT '更新者',
    update_time   DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (asset_id),
    UNIQUE KEY uk_asset_type_file_name (asset_type, file_name),
    INDEX idx_asset_type (asset_type),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='素材管理 - 素材主檔';

-- 2. 新增選單（行銷管理 -> 素材管理 -> 音檔管理/影片管理/圖片管理）
SET @marketing_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '行銷管理' ORDER BY menu_id DESC LIMIT 1);
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 2000) FROM sys_menu);

-- 2.1 素材管理（目錄）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark, route_name)
SELECT @max_menu_id + 1,
       '素材管理',
       @marketing_menu_id,
       4,
       'material',
       NULL,
       1,
       0,
       'M',
       '0',
       '0',
       '',
       'nested',
       'admin',
       NOW(),
       '',
       NULL,
       '共用素材管理（音檔/影片/圖片）',
       'Material'
WHERE @marketing_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @marketing_menu_id AND path = 'material');

SET @material_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @marketing_menu_id AND path = 'material' ORDER BY menu_id DESC LIMIT 1);
SET @max_menu_id2 = (SELECT IFNULL(MAX(menu_id), 2000) FROM sys_menu);

-- 2.2 音檔管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark, route_name)
SELECT @max_menu_id2 + 1,
       '音檔管理',
       @material_menu_id,
       1,
       'audio',
       'material/audio/index',
       1,
       0,
       'C',
       '0',
       '0',
       'system:material:audio:list',
       'phone',
       'admin',
       NOW(),
       '',
       NULL,
       '音檔素材管理',
       'MaterialAudio'
WHERE @material_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:audio:list');

-- 2.3 影片管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark, route_name)
SELECT @max_menu_id2 + 2,
       '影片管理',
       @material_menu_id,
       2,
       'video',
       'material/video/index',
       1,
       0,
       'C',
       '0',
       '0',
       'system:material:video:list',
       'monitor',
       'admin',
       NOW(),
       '',
       NULL,
       '影片素材管理',
       'MaterialVideo'
WHERE @material_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:video:list');

-- 2.4 圖片管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark, route_name)
SELECT @max_menu_id2 + 3,
       '圖片管理',
       @material_menu_id,
       3,
       'image',
       'material/image/index',
       1,
       0,
       'C',
       '0',
       '0',
       'system:material:image:list',
       'eye-open',
       'admin',
       NOW(),
       '',
       NULL,
       '圖片素材管理',
       'MaterialImage'
WHERE @material_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:image:list');

-- 2.5 按鈕權限（查詢/上傳/刪除）
SET @audio_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'system:material:audio:list' ORDER BY menu_id DESC LIMIT 1);
SET @video_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'system:material:video:list' ORDER BY menu_id DESC LIMIT 1);
SET @image_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'system:material:image:list' ORDER BY menu_id DESC LIMIT 1);

SET @max_menu_id3 = (SELECT IFNULL(MAX(menu_id), 2000) FROM sys_menu);

-- 音檔按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 1, '音檔查詢', @audio_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'system:material:audio:query', '#', 'admin', NOW(), '', NULL, ''
WHERE @audio_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:audio:query');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 2, '音檔上傳', @audio_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'system:material:audio:upload', '#', 'admin', NOW(), '', NULL, ''
WHERE @audio_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:audio:upload');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 3, '音檔刪除', @audio_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'system:material:audio:remove', '#', 'admin', NOW(), '', NULL, ''
WHERE @audio_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:audio:remove');

-- 影片按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 4, '影片查詢', @video_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'system:material:video:query', '#', 'admin', NOW(), '', NULL, ''
WHERE @video_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:video:query');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 5, '影片上傳', @video_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'system:material:video:upload', '#', 'admin', NOW(), '', NULL, ''
WHERE @video_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:video:upload');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 6, '影片刪除', @video_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'system:material:video:remove', '#', 'admin', NOW(), '', NULL, ''
WHERE @video_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:video:remove');

-- 圖片按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 7, '圖片查詢', @image_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'system:material:image:query', '#', 'admin', NOW(), '', NULL, ''
WHERE @image_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:image:query');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 8, '圖片上傳', @image_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'system:material:image:upload', '#', 'admin', NOW(), '', NULL, ''
WHERE @image_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:image:upload');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @max_menu_id3 + 9, '圖片刪除', @image_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'system:material:image:remove', '#', 'admin', NOW(), '', NULL, ''
WHERE @image_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:material:image:remove');

-- 3. 為 admin 角色自動分配素材管理權限
SET @admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'admin' LIMIT 1);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE perms IN (
    'system:material:audio:list', 'system:material:audio:query', 'system:material:audio:upload', 'system:material:audio:remove',
    'system:material:video:list', 'system:material:video:query', 'system:material:video:upload', 'system:material:video:remove',
    'system:material:image:list', 'system:material:image:query', 'system:material:image:upload', 'system:material:image:remove'
) AND @admin_role_id IS NOT NULL;

-- 4. 修正已存在選單的 icon（避免因 NOT EXISTS 跳過插入導致 icon 空白）
UPDATE sys_menu
SET icon = 'nested'
WHERE parent_id = @marketing_menu_id
  AND path = 'material'
  AND (icon IS NULL OR icon = '' OR icon = '#');

UPDATE sys_menu
SET icon = 'phone'
WHERE perms = 'system:material:audio:list'
  AND (icon IS NULL OR icon = '' OR icon = '#');

UPDATE sys_menu
SET icon = 'monitor'
WHERE perms = 'system:material:video:list'
  AND (icon IS NULL OR icon = '' OR icon = '#');

UPDATE sys_menu
SET icon = 'eye-open'
WHERE perms = 'system:material:image:list'
  AND (icon IS NULL OR icon = '' OR icon = '#');


-- 新增 Bot 顯示名稱和圖片 URL 欄位到 sys_line_config 表
-- 這些欄位用於訊息預覽時顯示 Bot 頭像和名稱
ALTER TABLE sys_line_config
    ADD COLUMN bot_display_name VARCHAR(200) NULL COMMENT 'Bot 顯示名稱（從 LINE API 取得）' AFTER sort_order,
    ADD COLUMN bot_picture_url VARCHAR(500) NULL COMMENT 'Bot 圖片 URL（從 LINE API 取得）' AFTER bot_display_name;
