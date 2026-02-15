-- =====================================================
-- V41: 物流整合 - 超商門市暫存表與訂單欄位擴充
-- =====================================================

-- 1. 建立超商門市暫存表
CREATE TABLE shop_cvs_store_temp
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    store_key     VARCHAR(64)  NOT NULL COMMENT '前端識別 key（UUID），避免 memberId 碰撞',
    member_id     BIGINT       NOT NULL COMMENT '會員ID',
    logistics_sub VARCHAR(20)  NOT NULL COMMENT '物流子類型：FAMI/UNIMART/HILIFE',
    store_id      VARCHAR(20)  NOT NULL COMMENT '門市代號',
    store_name    VARCHAR(100) NOT NULL COMMENT '門市名稱',
    store_address VARCHAR(200) DEFAULT NULL COMMENT '門市地址',
    store_tel     VARCHAR(20)  DEFAULT NULL COMMENT '門市電話',
    cvs_outside   VARCHAR(1)   DEFAULT NULL COMMENT '是否離島（1=是）',
    expire_time   DATETIME     NOT NULL COMMENT '過期時間（建立後 30 分鐘）',
    create_time   DATETIME     DEFAULT NULL COMMENT '建立時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_store_key (store_key),
    KEY idx_member_id (member_id),
    KEY idx_expire_time (expire_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='超商門市選取暫存表';

-- 2. shop_order 表新增超商門市欄位
ALTER TABLE shop_order
    ADD COLUMN cvs_store_id       VARCHAR(20)  DEFAULT NULL COMMENT '超商門市代號' AFTER shipping_method,
    ADD COLUMN cvs_store_name     VARCHAR(100) DEFAULT NULL COMMENT '超商門市名稱' AFTER cvs_store_id,
    ADD COLUMN cvs_store_address  VARCHAR(200) DEFAULT NULL COMMENT '超商門市地址' AFTER cvs_store_name,
    ADD COLUMN logistics_sub_type VARCHAR(20)  DEFAULT NULL COMMENT '物流子類型' AFTER cvs_store_address;

-- 3. 新增物流相關系統設定
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES
    -- 綠界物流 B2C 帳號
    ('綠界物流 MerchantID (B2C)', 'shop.ecpay.logistics.merchant_id', '2000132', 'Y', 'admin', NOW(),
     '綠界物流 B2C 商店代號（測試用 2000132）'),
    ('綠界物流 HashKey (B2C)', 'shop.ecpay.logistics.hash_key', '5294y06JbISpM5x9', 'Y', 'admin', NOW(),
     '綠界物流 B2C HashKey'),
    ('綠界物流 HashIV (B2C)', 'shop.ecpay.logistics.hash_iv', 'v77hoKGq4kWxNNIS', 'Y', 'admin', NOW(),
     '綠界物流 B2C HashIV'),
    ('綠界物流模式', 'shop.ecpay.logistics.mode', 'test', 'Y', 'admin', NOW(),
     'test=測試環境, prod=正式環境'),
    -- 電子地圖回調 URL 設定
    ('物流電子地圖回調BaseURL', 'shop.logistics.callback_base_url', '', 'Y', 'admin', NOW(),
     '超商電子地圖回調用的後端 Base URL。系統會自動附加 /shop/logistics/cvs/store/callback。範例：本地開發 https://xxxx.ngrok-free.dev/dev-api，正式環境 https://your-domain.com/prod-api'),
    -- 宅配設定
    ('宅配寄件人姓名', 'shop.logistics.sender_name', '', 'Y', 'admin', NOW(),
     '宅配寄件人姓名。綠界規定：中文 2-5 字或英文 4-10 字元。範例：王小明、HaoYuCheng'),
    ('宅配寄件人電話', 'shop.logistics.sender_phone', '', 'Y', 'admin', NOW(),
     '宅配寄件人電話。格式：09 開頭的 10 位數字。範例：0912345678'),
    ('宅配寄件人地址', 'shop.logistics.sender_address', '', 'Y', 'admin', NOW(),
     '宅配寄件人完整地址。範例：新北市新莊區中正路100號3樓'),
    ('宅配寄件人郵遞區號', 'shop.logistics.sender_zip', '', 'Y', 'admin', NOW(),
     '宅配寄件人郵遞區號（3 碼）。範例：242（新莊區）、100（中正區）'),
    -- 超商門市暫存過期分鐘數
    ('門市暫存過期分鐘', 'shop.logistics.cvs_store_expire_minutes', '30', 'Y', 'admin', NOW(),
     '超商門市暫存有效分鐘數'),
    -- 物流方式啟用設定
    ('商城啟用物流方式', 'shop.logistics.methods', 'HOME_DELIVERY,CVS_711,CVS_FAMILY,CVS_HILIFE', 'Y', 'admin', NOW(),
     '可用物流方式代碼，多個用逗號分隔。可選值：HOME_DELIVERY:宅配到府, CVS_711:7-11超取, CVS_FAMILY:全家超取, CVS_HILIFE:萊爾富超取'),
    -- 運費設定
    ('宅配運費', 'shop.logistics.home_delivery_fee', '100', 'Y', 'admin', NOW(),
     '宅配運費（元）'),
    ('宅配免運門檻', 'shop.logistics.home_delivery_free_threshold', '1000', 'Y', 'admin', NOW(),
     '宅配免運金額門檻（元），0 表示不免運'),
    ('超商取貨運費', 'shop.logistics.cvs_fee', '60', 'Y', 'admin', NOW(),
     '超商取貨運費（元）'),
    ('超商取貨免運門檻', 'shop.logistics.cvs_free_threshold', '0', 'Y', 'admin', NOW(),
     '超商取貨免運金額門檻（元），0 表示不免運'),
    -- 物流狀態回調 URL（ECPay API 必填）
    ('綠界物流狀態回調URL', 'shop.ecpay.logistics.server_reply_url', '', 'Y', 'admin', NOW(),
     '物流狀態回調完整 URL（ECPay Express/Create API 必填）。綠界會在物流狀態變更時發送通知到此 URL。範例：本地開發 https://xxxx.ngrok-free.dev/dev-api/shop/logistics/status-callback，正式環境 https://your-domain.com/prod-api/shop/logistics/status-callback');
