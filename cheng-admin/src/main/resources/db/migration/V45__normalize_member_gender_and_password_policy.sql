-- 正規化 shop_member.gender 欄位值
-- 原設計使用 MALE/FEMALE/UNKNOWN，統一為 0/1/2（與前端及 sys_user 慣例一致）

UPDATE shop_member SET gender = '0' WHERE gender = 'MALE';
UPDATE shop_member SET gender = '1' WHERE gender = 'FEMALE';
UPDATE shop_member SET gender = '2' WHERE gender = 'UNKNOWN';

ALTER TABLE shop_member
    MODIFY COLUMN gender VARCHAR(10) DEFAULT '2' COMMENT '性別：0=男, 1=女, 2=未知';

-- ==============================================
-- 會員密碼政策設定
-- 將密碼規則統一為 shop.password.* 系列，
-- 註冊與密碼重設共用同一套驗證邏輯
-- ==============================================

-- 1. 遷移舊 key：shop.pwd_reset.min_password_length → shop.password.min_length
UPDATE sys_config
SET config_key  = 'shop.password.min_length',
    config_name = '密碼最小長度（註冊+重設共用）',
    update_time = NOW()
WHERE config_key = 'shop.pwd_reset.min_password_length'
  AND NOT EXISTS (SELECT 1 FROM (SELECT config_id FROM sys_config WHERE config_key = 'shop.password.min_length') t);

-- 2. 若舊 key 不存在（或已遷移），確保新 key 存在
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '密碼最小長度（註冊+重設共用）', 'shop.password.min_length', '8', 'Y', 'system', NOW(), '會員密碼最小長度'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'shop.password.min_length');

-- 3. 新增密碼最大長度
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '密碼最大長度', 'shop.password.max_length', '50', 'Y', 'system', NOW(), '會員密碼最大長度'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'shop.password.max_length');

-- 4. 新增複雜度開關
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '需要大寫字母', 'shop.password.require_uppercase', '0', 'Y', 'system', NOW(), '密碼需包含大寫字母（1=是, 0=否）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'shop.password.require_uppercase');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '需要小寫字母', 'shop.password.require_lowercase', '0', 'Y', 'system', NOW(), '密碼需包含小寫字母（1=是, 0=否）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'shop.password.require_lowercase');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '需要數字', 'shop.password.require_digit', '0', 'Y', 'system', NOW(), '密碼需包含數字（1=是, 0=否）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'shop.password.require_digit');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '需要特殊符號', 'shop.password.require_special', '0', 'Y', 'system', NOW(), '密碼需包含特殊符號（1=是, 0=否）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'shop.password.require_special');


-- ==============================================
-- 商城密碼政策 config_name 加上「商城」前綴
-- 避免與後台管理密碼設定混淆
-- ==============================================

UPDATE sys_config SET config_name = '商城密碼最小長度（註冊+重設共用）', update_time = NOW()
WHERE config_key = 'shop.password.min_length';

UPDATE sys_config SET config_name = '商城密碼最大長度', update_time = NOW()
WHERE config_key = 'shop.password.max_length';

UPDATE sys_config SET config_name = '商城需要大寫字母', update_time = NOW()
WHERE config_key = 'shop.password.require_uppercase';

UPDATE sys_config SET config_name = '商城需要小寫字母', update_time = NOW()
WHERE config_key = 'shop.password.require_lowercase';

UPDATE sys_config SET config_name = '商城需要數字', update_time = NOW()
WHERE config_key = 'shop.password.require_digit';

UPDATE sys_config SET config_name = '商城需要特殊符號', update_time = NOW()
WHERE config_key = 'shop.password.require_special';
