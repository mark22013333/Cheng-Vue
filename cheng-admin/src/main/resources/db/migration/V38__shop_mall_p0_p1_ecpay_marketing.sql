-- =============================================
-- P0 商城遷移：折扣系統、運費設定、金流整合
-- =============================================

-- =============================================
-- 1. 折扣系統 - 商品特價欄位
-- =============================================

-- 商品主表增加特價欄位
ALTER TABLE shop_product
    ADD COLUMN sale_price    DECIMAL(10, 2) DEFAULT NULL COMMENT '特惠價（優先於全站折扣）' AFTER original_price,
    ADD COLUMN sale_end_date DATETIME       DEFAULT NULL COMMENT '特價結束時間（NULL 表示長期）' AFTER sale_price;

-- SKU 表增加特價欄位
ALTER TABLE shop_product_sku
    ADD COLUMN sale_price DECIMAL(10, 2) DEFAULT NULL COMMENT '特惠價（優先於全站折扣）' AFTER original_price;

-- 商品特價索引（加速過期檢查）
ALTER TABLE shop_product
    ADD INDEX idx_product_sale_end_date (sale_end_date);

-- =============================================
-- 2. 系統設定：折扣 + 運費 + ECPay
-- =============================================

-- 全站折扣設定
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('商城折扣模式', 'shop.discount.mode', '0', 'Y', 'admin', NOW(), '0=無折扣, 1=加價模式, 2=原價折扣模式'),
       ('商城折扣比率', 'shop.discount.rate', '0', 'Y', 'admin', NOW(), '折扣百分比數值（依模式解讀）');

-- 運費設定
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('免運門檻', 'shop.shipping.free_threshold', '1000', 'Y', 'admin', NOW(), '訂單金額達此值免運費，0 表示不免運'),
       ('國內運費', 'shop.shipping.domestic_fee', '60', 'Y', 'admin', NOW(), '本島基本運費'),
       ('離島運費', 'shop.shipping.island_fee', '150', 'Y', 'admin', NOW(), '離島運費');

-- ECPay 金流設定
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('ECPay 商店代號', 'shop.ecpay.merchant_id', '3002607', 'Y', 'admin', NOW(), 'ECPay MerchantID'),
       ('ECPay HashKey', 'shop.ecpay.hash_key', 'pwFHCqoQZGmho4w6', 'Y', 'admin', NOW(), 'ECPay HashKey'),
       ('ECPay HashIV', 'shop.ecpay.hash_iv', 'EkRm7iFT261dpevs', 'Y', 'admin', NOW(), 'ECPay HashIV'),
       ('ECPay 模式', 'shop.ecpay.mode', 'test', 'Y', 'admin', NOW(), 'test=測試環境, prod=正式環境');

-- =============================================
-- 3. 訂單表增加 ECPay 相關欄位
-- =============================================

ALTER TABLE shop_order
    ADD COLUMN ecpay_trade_no VARCHAR(64) DEFAULT NULL COMMENT 'ECPay 交易編號' AFTER payment_no,
    ADD COLUMN ecpay_info     JSON        DEFAULT NULL COMMENT 'ECPay 回傳完整資訊' AFTER ecpay_trade_no;

ALTER TABLE shop_order
    ADD INDEX idx_order_ecpay_trade_no (ecpay_trade_no);

-- =============================================
-- V39: P1 行銷與促銷 - 滿額禮物 + 文章系統
-- =============================================

-- =============================================
-- 1. 滿額禮物表
-- =============================================
CREATE TABLE shop_gift
(
    gift_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100)   NOT NULL COMMENT '禮物名稱',
    image_url        VARCHAR(500)            DEFAULT NULL COMMENT '圖片 URL',
    threshold_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '消費門檻金額',
    stock_quantity   INT            NOT NULL DEFAULT 0 COMMENT '庫存數量',
    sort_order       INT            NOT NULL DEFAULT 0 COMMENT '排序',
    status           VARCHAR(20)    NOT NULL DEFAULT 'ENABLED' COMMENT '狀態: ENABLED/DISABLED',
    create_by        VARCHAR(64)             DEFAULT NULL,
    create_time      DATETIME                DEFAULT NULL,
    update_by        VARCHAR(64)             DEFAULT NULL,
    update_time      DATETIME                DEFAULT NULL,
    remark           VARCHAR(500)            DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='滿額禮物';

CREATE INDEX idx_gift_status ON shop_gift (status);

-- =============================================
-- 2. 訂單新增禮物欄位
-- =============================================
ALTER TABLE shop_order
    ADD COLUMN gift_id   BIGINT       DEFAULT NULL COMMENT '禮物ID' AFTER ecpay_info,
    ADD COLUMN gift_name VARCHAR(100) DEFAULT NULL COMMENT '禮物名稱' AFTER gift_id;

-- =============================================
-- 3. 禮物啟用設定
-- =============================================
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('商城滿額禮開關', 'shop.gift.enabled', '0', 'Y', 'admin', NOW(), '0=停用, 1=啟用');

-- =============================================
-- 4. 文章表
-- =============================================
CREATE TABLE shop_article
(
    article_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL COMMENT '標題',
    summary     VARCHAR(500)          DEFAULT NULL COMMENT '摘要',
    content     LONGTEXT              DEFAULT NULL COMMENT 'HTML 內容',
    cover_image VARCHAR(500)          DEFAULT NULL COMMENT '封面圖',
    product_id  BIGINT                DEFAULT NULL COMMENT '關聯商品ID',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    view_count  INT          NOT NULL DEFAULT 0 COMMENT '瀏覽數',
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '狀態: DRAFT/PUBLISHED',
    create_by   VARCHAR(64)           DEFAULT NULL,
    create_time DATETIME              DEFAULT NULL,
    update_by   VARCHAR(64)           DEFAULT NULL,
    update_time DATETIME              DEFAULT NULL,
    remark      VARCHAR(500)          DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商城文章';

CREATE INDEX idx_article_status ON shop_article (status);

-- =============================================
-- 5. 後台選單
-- =============================================

-- 取得「商城管理」選單 ID
SELECT @shop_menu_id := menu_id
FROM sys_menu
WHERE menu_name = '商城管理'
  AND parent_id = 0
LIMIT 1;

-- 取得「首頁管理」選單 ID
SELECT @home_menu_id := menu_id
FROM sys_menu
WHERE menu_name = '首頁管理'
  AND parent_id = @shop_menu_id
LIMIT 1;

-- ----- 禮物管理 (掛在首頁管理下) -----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('禮物管理', @home_menu_id, 3, 'gift', 'shop/gift/index', NULL, 'ShopGift', 1, 0, 'C', '0', '0',
        'shop:gift:list', 'mall-gift', 'admin', NOW(), '', NULL, '滿額禮物管理選單');

SET @gift_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
VALUES ('禮物查詢', @gift_menu_id, 1, '', NULL, 1, 0, 'F', '0', '0', 'shop:gift:query', '#', 'admin', NOW(), NULL),
       ('禮物新增', @gift_menu_id, 2, '', NULL, 1, 0, 'F', '0', '0', 'shop:gift:add', '#', 'admin', NOW(), NULL),
       ('禮物修改', @gift_menu_id, 3, '', NULL, 1, 0, 'F', '0', '0', 'shop:gift:edit', '#', 'admin', NOW(), NULL),
       ('禮物刪除', @gift_menu_id, 4, '', NULL, 1, 0, 'F', '0', '0', 'shop:gift:remove', '#', 'admin', NOW(), NULL);

-- ----- 文章管理 (掛在商城管理下) -----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('文章管理', @shop_menu_id, 5, 'article', 'shop/article/index', NULL, 'ShopArticle', 1, 0, 'C', '0', '0',
        'shop:article:list', 'message', 'admin', NOW(), '', NULL, '商城文章管理選單');

SET @article_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
VALUES ('文章查詢', @article_menu_id, 1, '', NULL, 1, 0, 'F', '0', '0', 'shop:article:query', '#', 'admin', NOW(),
        NULL),
       ('文章新增', @article_menu_id, 2, '', NULL, 1, 0, 'F', '0', '0', 'shop:article:add', '#', 'admin', NOW(),
        NULL),
       ('文章修改', @article_menu_id, 3, '', NULL, 1, 0, 'F', '0', '0', 'shop:article:edit', '#', 'admin', NOW(),
        NULL),
       ('文章刪除', @article_menu_id, 4, '', NULL, 1, 0, 'F', '0', '0', 'shop:article:remove', '#', 'admin', NOW(),
        NULL);


-- =============================================
-- 修正 ECPay 測試 MerchantID
-- =============================================
-- 根據 ECPay 官方文件 (https://developers.ecpay.com.tw/?p=35542)：
--   MerchantID 3002607 = 有信用卡3D驗證的特店
--   MerchantID 2000132 = 無信用卡3D驗證的特店（通用測試帳號）
--
-- ECPay FAQ 建議測試環境使用 2000132：
-- https://developers.ecpay.com.tw/2856/

UPDATE sys_config SET config_value = '2000132'          WHERE config_key = 'shop.ecpay.merchant_id';
UPDATE sys_config SET config_value = '5294y06JbISpM5x9' WHERE config_key = 'shop.ecpay.hash_key';
UPDATE sys_config SET config_value = 'v77hoKGq4kWxNNIS' WHERE config_key = 'shop.ecpay.hash_iv';

-- 新增金流回調 Base URL 配置（解決 ngrok 開發環境 localhost 問題）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('金流回調 Base URL', 'shop.payment.base_url', '', 'Y', 'admin', NOW(),
        '金流回調用的伺服器基礎 URL，例如 https://xxxx.ngrok-free.app，留空則自動偵測');

-- 新增前端 Base URL 配置（ECPay 付款後重導向用）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('前端 Base URL', 'shop.payment.frontend_url', '', 'Y', 'admin', NOW(),
        '前端應用基礎 URL，例如 http://localhost:1024，留空則使用相對路徑');

-- 新增瀏覽器端後端 Base URL 設定（ECPay OrderResultURL 用）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('瀏覽器端後端 Base URL', 'shop.payment.browser_base_url', '', 'Y', 'admin', NOW(),
        '付款完成後瀏覽器跳轉用的後端 URL。本地開發設為 http://localhost:1024/dev-api（經 Vite proxy），正式環境留空即用 base_url');

