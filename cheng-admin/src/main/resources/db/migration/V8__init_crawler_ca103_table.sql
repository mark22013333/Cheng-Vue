-- ============================
-- 爬蟲系統 - CA103 上市公司每日收盤價資料表
-- 版本：V8
-- 說明：建立臺灣證券交易所上市公司每日收盤價爬蟲資料表
-- 資料來源：臺灣證券交易所 (https://www.twse.com.tw)
-- ============================

-- ----------------------------
-- CA103 上市公司每日收盤價資料表
-- ----------------------------
CREATE TABLE IF NOT EXISTS CAT103
(
    id           BIGINT(20)     NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
    company_name VARCHAR(100)   NOT NULL COMMENT '公司名稱/證券名稱',
    company_no   VARCHAR(20)    NOT NULL COMMENT '公司代碼/證券代號',
    price        DECIMAL(10, 2) NOT NULL COMMENT '收盤價',
    publish_date VARCHAR(20)    NOT NULL COMMENT '交易日期 (格式: yyyy-MM-dd)',
    extract_date DATETIME       NOT NULL COMMENT '擷取時間',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_company_date (company_no, publish_date),
    INDEX idx_company_no (company_no),
    INDEX idx_publish_date (publish_date),
    INDEX idx_extract_date (extract_date),
    INDEX idx_price (price)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='CA103-上市公司每日收盤價資料表';
