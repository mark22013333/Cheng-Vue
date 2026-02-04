-- =============================================
-- V39: 金流回調紀錄表
-- =============================================

CREATE TABLE IF NOT EXISTS shop_payment_callback_log
(
    log_id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '紀錄ID',
    order_no       VARCHAR(32)          DEFAULT NULL COMMENT '訂單編號',
    payment_method VARCHAR(20) NOT NULL COMMENT '付款方式代碼',
    callback_type  VARCHAR(20) NOT NULL COMMENT '回調來源: SERVER/BROWSER',
    trade_no       VARCHAR(64)          DEFAULT NULL COMMENT '第三方交易編號',
    rtn_code       VARCHAR(32)          DEFAULT NULL COMMENT '第三方回傳碼',
    verify_status  TINYINT     NOT NULL DEFAULT 0 COMMENT '驗簽結果:1成功0失敗',
    verify_message VARCHAR(200)         DEFAULT NULL COMMENT '驗簽訊息',
    raw_info       JSON                 DEFAULT NULL COMMENT '回傳原始資訊(JSON)',
    create_time    DATETIME             DEFAULT NULL COMMENT '建立時間',
    PRIMARY KEY (log_id),
    KEY idx_payment_log_order_no (order_no),
    KEY idx_payment_log_method (payment_method),
    KEY idx_payment_log_trade_no (trade_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='金流回調紀錄表';

-- =============================================
-- 金流回調紀錄選單
-- =============================================

-- 取得 商城管理 父選單 ID
SET @shopMenuId = (SELECT menu_id
                   FROM sys_menu
                   WHERE menu_name = '商城管理'
                     AND parent_id = 0
                   LIMIT 1);

-- 新增 金流回調紀錄 選單
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金流回調紀錄',
       @shopMenuId,
       5,
       'payment-callback',
       'shop/payment-callback/index',
       'ShopPaymentCallback',
       1,
       0,
       'C',
       '0',
       '0',
       'shop:payment:callback:list',
       'documentation',
       'admin',
       NOW(),
       '金流回調紀錄選單'
WHERE @shopMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'shop:payment:callback:list' AND parent_id = @shopMenuId);

-- 取得 金流回調紀錄 選單 ID
SET @callbackMenuId = (SELECT menu_id
                       FROM sys_menu
                       WHERE perms = 'shop:payment:callback:list'
                       LIMIT 1);

-- 新增按鈕權限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金流回調查詢',
       @callbackMenuId,
       1,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'shop:payment:callback:query',
       '#',
       'admin',
       NOW(),
       ''
WHERE @callbackMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'shop:payment:callback:query');

