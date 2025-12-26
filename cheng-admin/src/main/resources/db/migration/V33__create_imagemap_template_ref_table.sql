-- =========================================
-- 圖文訊息範本關聯表
-- 版本：V33
-- 建立時間：2025-12-27
-- 說明：建立訊息範本與圖文範本的關聯表，支援圖文範本異動時同步更新
-- =========================================

-- 1. 建立訊息範本與圖文範本關聯表
CREATE TABLE IF NOT EXISTS line_template_imagemap_ref
(
    ref_id        BIGINT AUTO_INCREMENT COMMENT '關聯ID',
    template_id   BIGINT NOT NULL COMMENT '訊息範本ID（引用 line_message_template）',
    imagemap_id   BIGINT NOT NULL COMMENT '圖文範本ID（引用 line_message_template，類型為 IMAGEMAP）',
    message_index INT         DEFAULT 0 COMMENT '訊息索引（多訊息組合時的位置）',
    create_by     VARCHAR(64) DEFAULT '' COMMENT '建立者',
    create_time   DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (ref_id),
    UNIQUE KEY uk_template_imagemap_index (template_id, imagemap_id, message_index),
    INDEX idx_template_id (template_id),
    INDEX idx_imagemap_id (imagemap_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='訊息範本與圖文範本關聯表';

-- 2. 在 line_message_template 表新增 imagemap_source_id 欄位
-- 用於標記此訊息範本中的 IMAGEMAP 訊息來源於哪個圖文範本
ALTER TABLE line_message_template
    ADD COLUMN imagemap_source_id BIGINT DEFAULT NULL COMMENT '圖文範本來源ID（當 msg_type=IMAGEMAP 時，標記來源範本）' AFTER preview_img;

-- 新增索引
ALTER TABLE line_message_template
    ADD INDEX idx_imagemap_source_id (imagemap_source_id);
