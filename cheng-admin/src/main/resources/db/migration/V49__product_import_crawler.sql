-- shop_product_sku 新增 barcode 欄位
ALTER TABLE shop_product_sku
    ADD COLUMN barcode VARCHAR(50) DEFAULT NULL COMMENT '國際條碼（EAN）' AFTER sku_code;

CREATE INDEX idx_sku_barcode ON shop_product_sku (barcode);

-- 爬蟲匯入記錄表
CREATE TABLE IF NOT EXISTS crawl_import_log
(
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '記錄ID',
    batch_id      VARCHAR(50)  NOT NULL COMMENT '批次ID（同一次匯入共用）',
    barcode       VARCHAR(50)           DEFAULT NULL COMMENT '條碼編號',
    product_name  VARCHAR(200)          DEFAULT NULL COMMENT '品名（來自CSV）',
    status        VARCHAR(20)  NOT NULL COMMENT '狀態：SUCCESS/NOT_FOUND/MULTIPLE/IMG_FAIL/ERROR/SKIPPED',
    product_id    BIGINT                DEFAULT NULL COMMENT '成功時關聯的商品ID',
    detail_url    VARCHAR(500)          DEFAULT NULL COMMENT '爬取的詳情頁URL',
    error_message VARCHAR(500)          DEFAULT NULL COMMENT '錯誤訊息',
    create_time   DATETIME              DEFAULT NULL COMMENT '記錄時間',
    PRIMARY KEY (log_id),
    KEY idx_import_batch (batch_id),
    KEY idx_import_barcode (barcode),
    KEY idx_import_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='爬蟲匯入記錄';

-- Product_price_auto_sync
ALTER TABLE shop_product
    ADD COLUMN price_auto_sync TINYINT(1) DEFAULT 1
        COMMENT '價格是否自動同步SKU最低價（1=自動, 0=手動覆寫）';

-- =============================================
-- SKU 級特惠價語意重構：新增 sale_end_date + 資料下推
-- =============================================

-- 1. shop_product_sku 新增 sale_end_date 欄位
ALTER TABLE shop_product_sku
    ADD COLUMN sale_end_date DATETIME DEFAULT NULL
        COMMENT '特價結束時間（NULL 表示長期）' AFTER sale_price;

-- 2. 資料下推：product 有特惠價且 SKU 尚未設定 → 複製到所有 ENABLED SKU
UPDATE shop_product_sku sku
    INNER JOIN shop_product p ON sku.product_id = p.product_id
SET sku.sale_price    = p.sale_price,
    sku.sale_end_date = p.sale_end_date
WHERE p.sale_price IS NOT NULL
  AND p.sale_price > 0
  AND sku.sale_price IS NULL
  AND sku.status IN ('ENABLED', '1');

-- 3. 為 SKU saleEndDate 加索引（加速過期批次查詢）
ALTER TABLE shop_product_sku
    ADD INDEX idx_sku_sale_end_date (sale_end_date);
