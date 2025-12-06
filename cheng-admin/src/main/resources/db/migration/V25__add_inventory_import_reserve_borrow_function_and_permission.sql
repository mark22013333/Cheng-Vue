-- =============================================
-- V25: 新增「物品匯入」權限按鈕
-- 作者: Cheng
-- 日期: 2025-12-03
-- 說明: 為物品與庫存管理頁面新增「匯入」按鈕權限
-- =============================================

-- 新增「物品匯入」權限按鈕
INSERT INTO sys_menu (menu_name,
                      parent_id,
                      order_num,
                      path,
                      component,
                      is_frame,
                      is_cache,
                      menu_type,
                      visible,
                      status,
                      perms,
                      icon,
                      create_by,
                      create_time,
                      remark)
SELECT '物品匯入',
       menu_id,
       9, -- 排在盤點操作（8）之後
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'inventory:management:import',
       '#',
       'admin',
       NOW(),
       '批次匯入物品資料'
FROM sys_menu
WHERE menu_name = '物品與庫存管理'
  AND menu_type = 'C';

-- 驗證插入結果
-- SELECT menu_id, menu_name, perms, order_num
-- FROM sys_menu
-- WHERE parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '物品與庫存管理' AND menu_type = 'C')
-- ORDER BY order_num;

-- 將新權限自動分配給管理員角色（role_id = 1）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT '1',
       menu_id
FROM sys_menu
WHERE perms = 'inventory:management:import'
  AND NOT EXISTS (SELECT 1
                  FROM sys_role_menu
                  WHERE role_id = '1'
                    AND menu_id = sys_menu.menu_id);

-- 驗證角色權限分配
-- SELECT rm.role_id, r.role_name, m.menu_name, m.perms
-- FROM sys_role_menu rm
-- JOIN sys_role r ON rm.role_id = r.role_id
-- JOIN sys_menu m ON rm.menu_id = m.menu_id
-- WHERE m.perms = 'inventory:management:import';

-- 新增預約功能相關欄位
-- 時間戳記: 2025-12-03

-- 1. 為 inv_item 表新增預約數量（如果不存在）
SET @column_exists = (SELECT COUNT(*)
                      FROM INFORMATION_SCHEMA.COLUMNS
                      WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = 'inv_item'
                        AND COLUMN_NAME = 'reserved_qty');
SET @sql = IF(@column_exists = 0,
              'ALTER TABLE inv_item ADD COLUMN reserved_qty INT DEFAULT 0 COMMENT ''預約數量''',
              'SELECT ''Column reserved_qty already exists in inv_item''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 為 inv_item 表新增樂觀鎖版本號（如果不存在）
SET @column_exists = (SELECT COUNT(*)
                      FROM INFORMATION_SCHEMA.COLUMNS
                      WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = 'inv_item'
                        AND COLUMN_NAME = 'version');
SET @sql = IF(@column_exists = 0,
              'ALTER TABLE inv_item ADD COLUMN version INT DEFAULT 0 COMMENT ''樂觀鎖版本號，防止並發更新''',
              'SELECT ''Column version already exists in inv_item''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 為 inv_item 表的 version 欄位建立索引（如果不存在）
SET @index_exists = (SELECT COUNT(*)
                     FROM INFORMATION_SCHEMA.STATISTICS
                     WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'inv_item'
                       AND INDEX_NAME = 'idx_inv_item_version');
SET @sql = IF(@index_exists = 0,
              'CREATE INDEX idx_inv_item_version ON inv_item(item_id, version)',
              'SELECT ''Index idx_inv_item_version already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 為 inv_borrow 表新增預約狀態（如果不存在）
SET @column_exists = (SELECT COUNT(*)
                      FROM INFORMATION_SCHEMA.COLUMNS
                      WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = 'inv_borrow'
                        AND COLUMN_NAME = 'reserve_status');
SET @sql = IF(@column_exists = 0,
              'ALTER TABLE inv_borrow ADD COLUMN reserve_status TINYINT DEFAULT 0 COMMENT ''預約狀態: 0=正常借出, 1=待審核預約, 2=預約通過, 3=預約拒絕, 4=預約取消''',
              'SELECT ''Column reserve_status already exists in inv_borrow''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 為 inv_borrow 表新增預約開始日期（如果不存在）
SET @column_exists = (SELECT COUNT(*)
                      FROM INFORMATION_SCHEMA.COLUMNS
                      WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = 'inv_borrow'
                        AND COLUMN_NAME = 'reserve_start_date');
SET @sql = IF(@column_exists = 0,
              'ALTER TABLE inv_borrow ADD COLUMN reserve_start_date DATE COMMENT ''預約開始日期''',
              'SELECT ''Column reserve_start_date already exists in inv_borrow''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 為 inv_borrow 表新增預約結束日期（如果不存在）
SET @column_exists = (SELECT COUNT(*)
                      FROM INFORMATION_SCHEMA.COLUMNS
                      WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = 'inv_borrow'
                        AND COLUMN_NAME = 'reserve_end_date');
SET @sql = IF(@column_exists = 0,
              'ALTER TABLE inv_borrow ADD COLUMN reserve_end_date DATE COMMENT ''預約結束日期''',
              'SELECT ''Column reserve_end_date already exists in inv_borrow''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 為 inv_borrow 表的預約狀態建立索引（如果不存在）
SET @index_exists = (SELECT COUNT(*)
                     FROM INFORMATION_SCHEMA.STATISTICS
                     WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'inv_borrow'
                       AND INDEX_NAME = 'idx_inv_borrow_reserve_status');
SET @sql = IF(@index_exists = 0,
              'CREATE INDEX idx_inv_borrow_reserve_status ON inv_borrow(reserve_status)',
              'SELECT ''Index idx_inv_borrow_reserve_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 初始化所有現有物品的版本號為 1（保持與新記錄一致）
UPDATE inv_item
SET version = 1
WHERE version = 0
   OR version IS NULL;

-- 9. 新增備註說明
ALTER TABLE inv_item
    COMMENT = '物品資訊表 - 支援預約功能';
ALTER TABLE inv_borrow
    COMMENT = '借出記錄表 - 支援預約狀態';

-- 新增預約功能權限
-- 時間戳記: 2025-12-04

-- 1. 為「物品與庫存管理」新增「預約」權限按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '物品預約',
       menu_id,
       9,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'inventory:management:reserve',
       '#',
       'admin',
       NOW(),
       '預約物品功能'
FROM sys_menu
WHERE menu_name = '物品與庫存管理'
  AND menu_type = 'C'
  AND NOT EXISTS (SELECT 1
                  FROM sys_menu
                  WHERE perms = 'inventory:management:reserve');

-- 2. 為管理員角色（role_id=1）自動分配預約權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT '1', menu_id
FROM sys_menu
WHERE perms = 'inventory:management:reserve'
  AND NOT EXISTS (SELECT 1
                  FROM sys_role_menu
                  WHERE role_id = '1'
                    AND menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'inventory:management:reserve' LIMIT 1));

-- 3. 驗證插入結果
SELECT menu_id, menu_name, perms, order_num, create_time
FROM sys_menu
WHERE perms = 'inventory:management:reserve';

-- 新增查看所有借出記錄權限
-- 時間戳記: 2025-12-04

-- 1. 為「借出管理」新增「查看所有記錄」權限按鈕
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, remark)
SELECT '查看所有記錄',
       menu_id,
       10,
       '#',
       '',
       1,
       0,
       'F',
       '0',
       '0',
       'inventory:borrow:all',
       '#',
       'admin',
       NOW(),
       '查看所有借出記錄（不受借用人限制）'
FROM sys_menu
WHERE menu_name = '借出管理'
  AND menu_type = 'C'
  AND NOT EXISTS (SELECT 1
                  FROM sys_menu
                  WHERE perms = 'inventory:borrow:all');

-- 2. 為管理員角色（role_id=1）自動分配此權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT '1', menu_id
FROM sys_menu
WHERE perms = 'inventory:borrow:all'
  AND NOT EXISTS (SELECT 1
                  FROM sys_role_menu
                  WHERE role_id = '1'
                    AND menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'inventory:borrow:all' LIMIT 1));

-- 3. 驗證插入結果
SELECT menu_id, menu_name, perms, order_num, create_time
FROM sys_menu
WHERE perms = 'inventory:borrow:all';
