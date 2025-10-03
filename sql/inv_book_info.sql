-- ============================
-- 書籍資訊表（擴充庫存系統）
-- ============================

-- ----------------------------
-- 1、新增書籍分類
-- ----------------------------
INSERT INTO inv_category (category_id, parent_id, category_name, category_code, sort_order, status, del_flag, create_by, create_time, remark)
VALUES (2000, 0, '圖書', 'BOOK', 10, '0', '0', 'system', NOW(), '圖書類別'),
       (2001, 2000, '文學小說', 'BOOK_FICTION', 1, '0', '0', 'system', NOW(), '文學與小說類'),
       (2002, 2000, '商業理財', 'BOOK_BUSINESS', 2, '0', '0', 'system', NOW(), '商業理財類'),
       (2003, 2000, '電腦資訊', 'BOOK_IT', 3, '0', '0', 'system', NOW(), '電腦與資訊科技'),
       (2004, 2000, '語言學習', 'BOOK_LANGUAGE', 4, '0', '0', 'system', NOW(), '語言學習類'),
       (2005, 2000, '藝術設計', 'BOOK_ART', 5, '0', '0', 'system', NOW(), '藝術與設計類'),
       (2006, 2000, '其他圖書', 'BOOK_OTHER', 99, '0', '0', 'system', NOW(), '其他圖書類別');

-- ----------------------------
-- 2、書籍詳細資訊表
-- ----------------------------
DROP TABLE IF EXISTS inv_book_info;
CREATE TABLE inv_book_info
(
    book_info_id       BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '書籍資訊ID',
    item_id            BIGINT(20)            DEFAULT NULL COMMENT '關聯物品ID',
    isbn               VARCHAR(20)  NOT NULL COMMENT 'ISBN',
    title              VARCHAR(200) NOT NULL COMMENT '書名',
    author             VARCHAR(200)          DEFAULT '' COMMENT '作者',
    publisher          VARCHAR(100)          DEFAULT '' COMMENT '出版社',
    publish_date       VARCHAR(50)           DEFAULT '' COMMENT '出版日期',
    publish_location   VARCHAR(50)           DEFAULT '' COMMENT '出版地',
    language           VARCHAR(50)           DEFAULT '' COMMENT '語言',
    edition            VARCHAR(50)           DEFAULT '' COMMENT '版本',
    binding            VARCHAR(50)           DEFAULT '' COMMENT '裝訂',
    classification     VARCHAR(50)           DEFAULT '' COMMENT '分級',
    cover_image_path   VARCHAR(300)          DEFAULT '' COMMENT '封面圖片路徑',
    introduction       TEXT COMMENT '簡介',
    source_url         VARCHAR(300)          DEFAULT '' COMMENT '來源網址',
    crawl_time         DATETIME COMMENT '爬取時間',
    status             CHAR(1)               DEFAULT '0' COMMENT '狀態（0正常 1停用）',
    create_by          VARCHAR(64)           DEFAULT '' COMMENT '建立者',
    create_time        DATETIME COMMENT '建立時間',
    update_by          VARCHAR(64)           DEFAULT '' COMMENT '更新者',
    update_time        DATETIME COMMENT '更新時間',
    remark             VARCHAR(500)          DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (book_info_id),
    UNIQUE KEY uk_isbn (isbn),
    KEY idx_item_id (item_id),
    KEY idx_title (title)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '書籍詳細資訊表';

-- ----------------------------
-- 初始化範例資料
-- ----------------------------
INSERT INTO inv_book_info
VALUES (1, NULL, '9789865406417', '地表最強告解室',
        'Cherng, 地表最強敗犬', '大塊文化', '2020年01月17日',
        '台灣', '繁體中文', '初版', '平裝', '普通級',
        '', '2020地表最強壞話小本本...', 'https://isbn.tw/9789865406417',
        NOW(), '0', 'system', NOW(), '', NULL, '範例書籍資料');
