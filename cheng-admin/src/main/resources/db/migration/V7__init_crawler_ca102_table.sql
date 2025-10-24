-- ============================
-- 爬蟲系統 - CA102 公司重大訊息資料表
-- 版本：V7
-- 說明：建立臺灣證券交易所公司重大訊息爬蟲資料表
-- 資料來源：公開資訊觀測站 (https://mops.twse.com.tw)
-- ============================

-- ----------------------------
-- CA102 公司重大訊息資料表
-- ----------------------------
CREATE TABLE IF NOT EXISTS CAT102
(
    id           BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
    company_name VARCHAR(100) NOT NULL COMMENT '公司名稱',
    company_no   VARCHAR(20)  NOT NULL COMMENT '公司代碼',
    title        VARCHAR(500) NOT NULL COMMENT '訊息標題',
    content      TEXT COMMENT '訊息內容',
    publish_date VARCHAR(20)  NOT NULL COMMENT '發布日期 (格式: yyyy-MM-dd)',
    extract_date DATETIME     NOT NULL COMMENT '擷取時間',
    PRIMARY KEY (id),
    INDEX idx_company_no (company_no),
    INDEX idx_publish_date (publish_date),
    INDEX idx_extract_date (extract_date),
    INDEX idx_company_publish (company_no, publish_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='CA102-公司重大訊息資料表';
