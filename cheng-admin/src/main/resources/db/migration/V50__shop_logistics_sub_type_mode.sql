-- =====================================================
-- V50: 新增綠界物流型態（B2C/C2C）系統參數
-- =====================================================
-- 背景：原本 ShippingMethod.getEcpayLogisticsSubType() 硬寫 B2C 代碼（UNIMART/FAMI/HILIFE），
-- 但使用者實際綠界帳號多為 C2C 合約（出貨量小、自行至超商寄件），送 B2C 代碼會遭綠界
-- 以「找不到加密金鑰，請確認是否有申請開通此物流方式!」錯誤拒絕。
--
-- 本次新增 sys_config 參數 shop.ecpay.logistics.sub_type_mode 以切換：
--   B2C → 送 UNIMART/FAMI/HILIFE/TCAT 代碼（支援宅配）
--   C2C → 送 UNIMARTC2C/FAMIC2C/HILIFEC2C 代碼（不支援宅配）
--
-- 預設 C2C，對齊目前綠界實際開通之合約型態；未來出貨量成長改簽 B2C 時，僅需修改此
-- 欄位值即可切換，毋須重新部署程式。
-- =====================================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('綠界物流型態', 'shop.ecpay.logistics.sub_type_mode', 'C2C', 'Y', 'admin', NOW(),
        'B2C=大宗寄貨（支援宅配） / C2C=門市寄取件（無宅配），預設 C2C。值不區分大小寫，非法值自動 fallback 為 C2C')
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    remark       = VALUES(remark);
