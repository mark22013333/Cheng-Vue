-- =============================================================================
-- 商城付款方式設定
-- =============================================================================

-- 新增付款方式設定到 sys_config
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('商城啟用付款方式',
        'shop.payment.methods',
        'ECPAY',
        'Y',
        '可用付款方式代碼，多個用逗號分隔。可選值：COD:貨到付款, ECPAY:綠界金流, LINE_PAY:LINE Pay, CREDIT_CARD:信用卡, ATM:ATM轉帳, CVS:超商付款',
        'admin',
        NOW());
