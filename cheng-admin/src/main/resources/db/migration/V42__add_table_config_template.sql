-- ============================
-- 表格欄位配置模版 + 權限
-- 版本：V42
-- 說明：新增全域表格欄位模版表，並為三個頁面新增自訂欄位/管理模版權限
-- ============================

-- ----------------------------
-- 1. 建立模版表
-- ----------------------------
CREATE TABLE sys_table_config_template
(
    template_id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模版ID',
    page_key      VARCHAR(100) NOT NULL COMMENT '頁面標識',
    column_config TEXT         NOT NULL COMMENT '欄位配置（JSON格式）',
    create_by     VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '備註',
    UNIQUE KEY uk_page_key (page_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '表格欄位配置模版表';

-- ----------------------------
-- 2. 新增權限按鈕（F 類型）
-- ----------------------------

-- 物品與庫存管理 → 自訂表格欄位
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '自訂表格欄位',
       menu_id,
       20,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'system:tableConfig:customize',
       '#',
       'admin',
       NOW(),
       '允許使用者自行調整表格欄位顯示'
FROM sys_menu
WHERE menu_name = '物品與庫存管理'
  AND menu_type = 'C'
LIMIT 1;

-- 物品與庫存管理 → 管理欄位模版
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '管理欄位模版',
       menu_id,
       21,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'system:tableConfig:template',
       '#',
       'admin',
       NOW(),
       '允許設定全域表格欄位模版'
FROM sys_menu
WHERE menu_name = '物品與庫存管理'
  AND menu_type = 'C'
LIMIT 1;

-- 借出管理 → 自訂表格欄位
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '自訂表格欄位',
       menu_id,
       20,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'system:tableConfig:customize',
       '#',
       'admin',
       NOW(),
       '允許使用者自行調整表格欄位顯示'
FROM sys_menu
WHERE menu_name = '借出管理'
  AND menu_type = 'C'
LIMIT 1;

-- 借出管理 → 管理欄位模版
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '管理欄位模版',
       menu_id,
       21,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'system:tableConfig:template',
       '#',
       'admin',
       NOW(),
       '允許設定全域表格欄位模版'
FROM sys_menu
WHERE menu_name = '借出管理'
  AND menu_type = 'C'
LIMIT 1;

-- 使用者管理 → 自訂表格欄位
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '自訂表格欄位',
       menu_id,
       20,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'system:tableConfig:customize',
       '#',
       'admin',
       NOW(),
       '允許使用者自行調整表格欄位顯示'
FROM sys_menu
WHERE menu_name = '使用者管理'
  AND menu_type = 'C'
  AND perms = 'system:user:list'
LIMIT 1;

-- 使用者管理 → 管理欄位模版
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '管理欄位模版',
       menu_id,
       21,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'system:tableConfig:template',
       '#',
       'admin',
       NOW(),
       '允許設定全域表格欄位模版'
FROM sys_menu
WHERE menu_name = '使用者管理'
  AND menu_type = 'C'
  AND perms = 'system:user:list'
LIMIT 1;

-- ----------------------------
-- 3. 為 admin 角色分配新權限
-- ----------------------------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE perms IN ('system:tableConfig:customize', 'system:tableConfig:template');
