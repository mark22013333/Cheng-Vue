-- =============================================
-- 商城忘記密碼流程強化
-- 1) 補齊索引
-- 2) 新增重發冷卻設定
-- 3) 新增預設安全清理排程任務
-- =============================================

-- 1. 補齊 shop_password_reset.member_id 索引（若不存在）
SET @idx_exists := (SELECT COUNT(1)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'shop_password_reset'
                      AND index_name = 'idx_member_id');
SET @idx_sql := IF(
        @idx_exists = 0,
        'ALTER TABLE shop_password_reset ADD INDEX idx_member_id (member_id)',
        'SELECT 1'
                );
PREPARE stmt_idx FROM @idx_sql;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;

-- 2. 新增重發冷卻秒數設定（預設 60 秒）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
SELECT '密碼重設重發冷卻秒數',
       'shop.pwd_reset.resend_cooldown_seconds',
       '60',
       'Y',
       '同一 Email 重發密碼重設連結的冷卻秒數',
       'admin',
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM sys_config
                  WHERE config_key = 'shop.pwd_reset.resend_cooldown_seconds');

-- 3. 新增預設 Quartz 安全清理任務（每日 03:30，禁止併發）
INSERT INTO sys_job (job_name,
                     job_group,
                     invoke_target,
                     task_type_code,
                     cron_expression,
                     misfire_policy,
                     concurrent,
                     status,
                     create_by,
                     create_time,
                     remark)
SELECT '商城安全清理任務',
       'DEFAULT',
       'shopSecurityTask.cleanupExpiredTokens',
       'SHOP_SECURITY_CLEANUP',
       '0 30 3 * * ?',
       '3',
       '1',
       '0',
       'admin',
       NOW(),
       '每日清理過期的密碼重設與 Email 驗證 token'
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM sys_job
                  WHERE invoke_target = 'shopSecurityTask.cleanupExpiredTokens'
                    AND job_group = 'DEFAULT');

-- =============================================
-- 商城郵件設定遷移至 sys_config
-- 將 SMTP 設定從 application.yml 移至管理後台
-- 使用者可在「系統管理 > 參數設定」中修改
-- =============================================

-- SMTP 主機
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
SELECT '商城郵件 SMTP 主機',
       'shop.mail.host',
       'smtp.gmail.com',
       'Y',
       'SMTP 伺服器主機位址（如 smtp.gmail.com）',
       'admin',
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM sys_config
                  WHERE config_key = 'shop.mail.host');

-- SMTP 埠號
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
SELECT '商城郵件 SMTP 埠號',
       'shop.mail.port',
       '587',
       'Y',
       'SMTP 伺服器埠號（Gmail 使用 587）',
       'admin',
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM sys_config
                  WHERE config_key = 'shop.mail.port');

-- 寄件人帳號（留空，由使用者在管理後台設定）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
SELECT '商城郵件寄件人帳號',
       'shop.mail.username',
       '',
       'Y',
       '寄件人 Email 帳號（如 Gmail 帳號）',
       'admin',
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM sys_config
                  WHERE config_key = 'shop.mail.username');

-- 寄件人密碼（留空，由使用者在管理後台設定）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
SELECT '商城郵件寄件人密碼',
       'shop.mail.password',
       '',
       'Y',
       '寄件人密碼（Gmail 請使用應用程式密碼，非帳號密碼）',
       'admin',
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM sys_config
                  WHERE config_key = 'shop.mail.password');
