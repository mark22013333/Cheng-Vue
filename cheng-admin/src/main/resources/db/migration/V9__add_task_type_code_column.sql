-- ================================================
-- Flyway Migration: V9
-- 描述: 為 sys_job 表新增 task_type_code 欄位
-- 用途: 儲存任務類型代碼，方便編輯時還原表單
-- 日期: 2025-10-26
-- ================================================

-- 1. 新增 task_type_code 欄位
ALTER TABLE sys_job
    ADD COLUMN task_type_code VARCHAR(50) DEFAULT NULL COMMENT '任務類型代碼（用於範本模式）'
AFTER invoke_target;

-- 2. 為現有資料更新 task_type_code（資料遷移）

-- CA103: 臺灣證券交易所 - 上市公司每日收盤價
-- 特徵：invokeTarget 包含 crawlerTask.runWithMode 且參數包含 today-only/full-sync/date-range
UPDATE sys_job
SET task_type_code = 'CA103'
WHERE invoke_target LIKE 'crawlerTask.runWithMode(%'
  AND (invoke_target LIKE '%today-only%'
    OR invoke_target LIKE '%full-sync%'
    OR invoke_target LIKE '%date-range%')
  AND (job_name LIKE '%上市%' OR job_name LIKE '%CA103%');

-- CA102: 臺灣證券交易所 - 即時重大訊息
-- 特徵：invokeTarget 是 crawlerTask.runWithMode() 無參數
UPDATE sys_job
SET task_type_code = 'CA102'
WHERE invoke_target = 'crawlerTask.runWithMode()'
   OR (invoke_target LIKE 'crawlerTask.runWithMode()%'
    AND (job_name LIKE '%重大訊息%' OR job_name LIKE '%CA102%'));

-- CA101: ISBN 書籍查詢 - 書籍庫存
-- 特徵：invokeTarget 包含 crawlerTask.runWithMode 且只有單一參數，工作名稱包含 ISBN
UPDATE sys_job
SET task_type_code = 'CA101'
WHERE invoke_target LIKE 'crawlerTask.runWithMode(%'
  AND invoke_target NOT LIKE '%,%'
  AND (job_name LIKE '%ISBN%' OR job_name LIKE '%CA101%')
  AND task_type_code IS NULL;
-- 避免覆蓋已設定的值

-- LINE_PUSH: LINE Bot 推播
-- 特徵：invokeTarget 包含 lineBotTask
UPDATE sys_job
SET task_type_code = 'LINE_PUSH'
WHERE invoke_target LIKE 'lineBotTask.%'
  AND task_type_code IS NULL;

-- 3. 建立索引以提升查詢效能
CREATE INDEX idx_task_type_code ON sys_job (task_type_code);
