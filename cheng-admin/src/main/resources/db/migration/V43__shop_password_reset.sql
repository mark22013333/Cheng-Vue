-- =============================================
-- 密碼重設功能重構：驗證碼 → 時效性重設連結
-- =============================================

-- A. 建立密碼重設 Token 表（selector + hashed token 設計）
CREATE TABLE IF NOT EXISTS shop_password_reset
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主鍵 ID',
    selector     VARCHAR(32)  NOT NULL COMMENT '選擇器（URL-safe 隨機字串，用於查詢）',
    hashed_token VARCHAR(64)  NOT NULL COMMENT '驗證器 SHA-256 雜湊（不存原始值）',
    email        VARCHAR(100) NOT NULL COMMENT '綁定的 Email',
    member_id    BIGINT       NOT NULL COMMENT '會員 ID',
    expires_at   DATETIME     NOT NULL COMMENT '過期時間',
    used         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已使用（0=未使用，1=已使用）',
    ip_address   VARCHAR(50)           DEFAULT NULL COMMENT '請求時 IP 地址',
    user_agent   VARCHAR(500)          DEFAULT NULL COMMENT '請求時瀏覽器 User-Agent',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    used_at      DATETIME              DEFAULT NULL COMMENT '使用時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_selector (selector),
    KEY idx_email (email),
    KEY idx_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '密碼重設 Token 表';

-- B. 為 shop_member 新增密碼變更時間欄位（用於 JWT 強制失效判斷）
ALTER TABLE shop_member
    ADD COLUMN password_changed_at DATETIME DEFAULT NULL COMMENT '最近密碼變更時間' AFTER password;

-- C. 新增密碼重設相關系統設定
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_time)
VALUES ('密碼重設連結有效分鐘數', 'shop.pwd_reset.token_expire_minutes', '30', 'Y', '密碼重設 URL token 有效期（分鐘）',
        NOW()),
       ('密碼重設 Email 每小時上限', 'shop.pwd_reset.email_hourly_limit', '3', 'Y', '同一 Email 每小時最多請求次數',
        NOW()),
       ('密碼重設 Email 每日上限', 'shop.pwd_reset.email_daily_limit', '5', 'Y', '同一 Email 每天最多請求次數', NOW()),
       ('密碼最小長度', 'shop.pwd_reset.min_password_length', '12', 'Y', '重設密碼時的最小長度要求', NOW()),
       ('敏感操作保護時數', 'shop.pwd_reset.sensitive_lock_hours', '24', 'Y', '密碼重設後敏感操作鎖定時數', NOW());


-- =============================================
-- Email 驗證功能
-- 1. shop_member 新增 email_verified 欄位
-- 2. 建立 shop_email_verification 表
-- 3. 新增 Email 驗證相關 sys_config
-- =============================================

-- A. shop_member 新增 email_verified 欄位
ALTER TABLE shop_member
    ADD COLUMN email_verified TINYINT(1) NOT NULL DEFAULT 0
        COMMENT 'Email 是否已驗證' AFTER email;

-- 既有會員若有 email 則視為已驗證
UPDATE shop_member
SET email_verified = 1
WHERE email IS NOT NULL
  AND email != '';

-- B. 建立 shop_email_verification 表（結構仿 shop_password_reset）
CREATE TABLE shop_email_verification
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主鍵 ID',
    selector     VARCHAR(32)  NOT NULL COMMENT '選擇器（URL-safe 隨機字串，用於查詢）',
    hashed_token VARCHAR(64)  NOT NULL COMMENT '驗證器 SHA-256 雜湊',
    email        VARCHAR(100) NOT NULL COMMENT '綁定的 Email',
    member_id    BIGINT       NOT NULL COMMENT '會員 ID',
    expires_at   DATETIME     NOT NULL COMMENT '過期時間',
    used         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已使用',
    ip_address   VARCHAR(50)           DEFAULT NULL COMMENT '請求時 IP 地址',
    user_agent   VARCHAR(500)          DEFAULT NULL COMMENT '請求時瀏覽器 User-Agent',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    used_at      DATETIME              DEFAULT NULL COMMENT '使用時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_selector (selector),
    KEY idx_email (email),
    KEY idx_member_id (member_id),
    KEY idx_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Email 驗證 Token';

-- C. 新增 Email 驗證相關 sys_config
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_by, create_time)
VALUES ('Email 驗證連結有效分鐘數', 'shop.email_verify.token_expire_minutes', '1440', 'Y',
        'Email 驗證連結有效期，預設 24 小時', 'admin', NOW()),
       ('Email 驗證每小時上限', 'shop.email_verify.email_hourly_limit', '3', 'Y', '同一 Email 每小時最多發送驗證信次數',
        'admin', NOW()),
       ('Email 驗證每日上限', 'shop.email_verify.email_daily_limit', '5', 'Y', '同一 Email 每日最多發送驗證信次數',
        'admin', NOW());
