-- 修正歸還記錄表結構和庫存表資料
-- 執行前請先備份資料庫！

-- ====================================
-- 第一部分：修正 inv_stock 的 lost_qty
-- ====================================
-- 更新現有 inv_stock 記錄的 lost_qty，將 NULL 設為 0
UPDATE inv_stock SET lost_qty = 0 WHERE lost_qty IS NULL;

-- ====================================
-- 第二部分：修正 inv_return 表結構
-- ====================================
-- 說明：inv_return 表的原始結構與 Mapper.xml 不一致
-- 需要修改欄位名稱以符合 Mapper 的期望

-- 檢查欄位是否存在，如果存在才進行修改
-- 1. 修改欄位名稱（從原始 SQL 定義改為 Mapper 期望的名稱）
SET @dbname = DATABASE();
SET @tablename = 'inv_return';

-- 修改 return_no -> borrow_code
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'return_no');
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE inv_return CHANGE COLUMN `return_no` `borrow_code` VARCHAR(50) NOT NULL COMMENT ''借出單號''',
    'SELECT ''Column return_no does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改 quantity -> return_quantity
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'quantity');
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE inv_return CHANGE COLUMN `quantity` `return_quantity` INT(11) NOT NULL COMMENT ''歸還數量''',
    'SELECT ''Column quantity does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改 returner_id -> borrower_id
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'returner_id');
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE inv_return CHANGE COLUMN `returner_id` `borrower_id` BIGINT(20) NOT NULL COMMENT ''借用人ID''',
    'SELECT ''Column returner_id does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改 returner_name -> borrower_name
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'returner_name');
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE inv_return CHANGE COLUMN `returner_name` `borrower_name` VARCHAR(50) NOT NULL COMMENT ''借用人姓名''',
    'SELECT ''Column returner_name does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改 condition_desc -> item_condition
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'condition_desc');
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE inv_return CHANGE COLUMN `condition_desc` `item_condition` VARCHAR(50) DEFAULT NULL COMMENT ''物品狀態（good完好 damaged損壞 lost遺失）''',
    'SELECT ''Column condition_desc does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改 damage_desc -> damage_description
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'damage_desc');
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE inv_return CHANGE COLUMN `damage_desc` `damage_description` VARCHAR(200) DEFAULT '''' COMMENT ''損壞描述''',
    'SELECT ''Column damage_desc does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 移除不需要的欄位（檢查後刪除）
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'is_damaged');
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE inv_return DROP COLUMN `is_damaged`',
    'SELECT ''Column is_damaged does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 新增缺少的欄位（檢查後新增）
-- expected_return
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'expected_return');
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE inv_return ADD COLUMN `expected_return` DATETIME DEFAULT NULL COMMENT ''預計歸還時間'' AFTER `return_time`',
    'SELECT ''Column expected_return already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- is_overdue
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'is_overdue');
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE inv_return ADD COLUMN `is_overdue` CHAR(1) DEFAULT ''0'' COMMENT ''是否逾期（0否 1是）'' AFTER `expected_return`',
    'SELECT ''Column is_overdue already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- overdue_days
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'overdue_days');
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE inv_return ADD COLUMN `overdue_days` BIGINT(20) DEFAULT 0 COMMENT ''逾期天數'' AFTER `is_overdue`',
    'SELECT ''Column overdue_days already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- compensation_amount
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'compensation_amount');
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE inv_return ADD COLUMN `compensation_amount` DECIMAL(10,2) DEFAULT NULL COMMENT ''賠償金額'' AFTER `damage_description`',
    'SELECT ''Column compensation_amount already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- return_status
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'return_status');
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE inv_return ADD COLUMN `return_status` CHAR(1) DEFAULT ''1'' COMMENT ''歸還狀態（0待確認 1已確認 2有異議）'' AFTER `receiver_name`',
    'SELECT ''Column return_status already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- update_by
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'update_by');
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE inv_return ADD COLUMN `update_by` VARCHAR(64) DEFAULT '''' COMMENT ''更新者'' AFTER `create_time`',
    'SELECT ''Column update_by already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- update_time
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'update_time');
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE inv_return ADD COLUMN `update_time` DATETIME DEFAULT NULL COMMENT ''更新時間'' AFTER `update_by`',
    'SELECT ''Column update_time already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 修改索引（安全方式）
-- 刪除舊索引（如果存在）
SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = 'uk_return_no');
SET @sql = IF(@index_exists > 0, 
    'ALTER TABLE inv_return DROP INDEX `uk_return_no`',
    'SELECT ''Index uk_return_no does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = 'idx_returner_id');
SET @sql = IF(@index_exists > 0, 
    'ALTER TABLE inv_return DROP INDEX `idx_returner_id`',
    'SELECT ''Index idx_returner_id does not exist'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 新增新索引（如果不存在）
SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = 'idx_borrow_code');
SET @sql = IF(@index_exists = 0, 
    'ALTER TABLE inv_return ADD INDEX `idx_borrow_code` (`borrow_code`)',
    'SELECT ''Index idx_borrow_code already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = 'idx_borrower_id');
SET @sql = IF(@index_exists = 0, 
    'ALTER TABLE inv_return ADD INDEX `idx_borrower_id` (`borrower_id`)',
    'SELECT ''Index idx_borrower_id already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 完成
SELECT '資料表修正完成！' as message;
