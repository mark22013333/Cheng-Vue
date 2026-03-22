-- =============================================
-- 行銷追蹤資料表：瀏覽足跡 + 搜尋記錄（含訪客追蹤支援）
-- =============================================

-- 瀏覽足跡
CREATE TABLE shop_browsing_logs (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '瀏覽記錄 ID',
    member_id     BIGINT       NULL                    COMMENT '會員 ID（訪客時為 NULL）',
    guest_id      VARCHAR(36)  NULL                    COMMENT '訪客追蹤 ID (UUID)',
    product_id    BIGINT       NOT NULL                COMMENT '商品 ID',
    product_name  VARCHAR(200) NULL                    COMMENT '商品名稱（冗餘快照）',
    category_id   BIGINT       NULL                    COMMENT '商品分類 ID',
    source        VARCHAR(20)  NOT NULL DEFAULT 'DIRECT' COMMENT '來源：DIRECT/SEARCH/RECOMMEND/CATEGORY',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '瀏覽時間',
    PRIMARY KEY (log_id),
    INDEX idx_browsing_member_time (member_id, create_time DESC),
    INDEX idx_browsing_product (product_id),
    INDEX idx_browsing_guest_time (guest_id, create_time DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '會員瀏覽足跡';

-- 搜尋記錄
CREATE TABLE shop_search_logs (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '搜尋記錄 ID',
    member_id     BIGINT       NULL                    COMMENT '會員 ID（訪客時為 NULL）',
    guest_id      VARCHAR(36)  NULL                    COMMENT '訪客追蹤 ID (UUID)',
    keyword       VARCHAR(100) NOT NULL                COMMENT '搜尋關鍵字',
    result_count  INT          NULL                    COMMENT '搜尋結果數量',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜尋時間',
    PRIMARY KEY (log_id),
    INDEX idx_search_member_time (member_id, create_time DESC),
    INDEX idx_search_keyword (keyword),
    INDEX idx_search_guest_time (guest_id, create_time DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '會員搜尋記錄';
