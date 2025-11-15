-- 新增 Rich Menu 歷史記錄與本地圖片路徑欄位
-- 用於重新發布時的異常回滾和版本追蹤

ALTER TABLE sys_line_rich_menu
    ADD COLUMN local_image_path VARCHAR(255) NULL COMMENT '本地圖片路徑' AFTER image_url;

ALTER TABLE sys_line_rich_menu
    ADD COLUMN previous_rich_menu_id VARCHAR(100) NULL COMMENT '前一個 Rich Menu ID（用於回滾）' AFTER rich_menu_id;

ALTER TABLE sys_line_rich_menu
    ADD COLUMN previous_config TEXT NULL COMMENT '前一版本配置快照（JSON格式）' AFTER previous_rich_menu_id;

-- 新增 suggested_alias_id 欄位
-- 用於發布時自動建立 Alias

ALTER TABLE sys_line_rich_menu
    ADD COLUMN suggested_alias_id VARCHAR(32) NULL COMMENT '建議的 Alias ID（發布時自動建立）' AFTER rich_menu_id;
