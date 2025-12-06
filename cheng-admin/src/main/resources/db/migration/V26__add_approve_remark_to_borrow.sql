-- =============================================
-- V26: 為 inv_borrow 表新增 approve_remark 欄位
-- 作者: Cheng
-- 日期: 2025-12-06
-- 說明: 儲存審核拒絕時的原因
-- =============================================

-- 檢查欄位是否已存在，如不存在則新增
SET
    @column_exists = (SELECT COUNT(*)
                      FROM INFORMATION_SCHEMA.COLUMNS
                      WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = 'inv_borrow'
                        AND COLUMN_NAME = 'approve_remark');

SET
    @sql = IF(@column_exists = 0,
              'ALTER TABLE inv_borrow ADD COLUMN approve_remark VARCHAR(500) DEFAULT NULL COMMENT ''審核備註（拒絕原因）'' AFTER approve_time',
              'SELECT ''Column approve_remark already exists in inv_borrow''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 驗證結果
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'inv_borrow'
  AND COLUMN_NAME = 'approve_remark';
