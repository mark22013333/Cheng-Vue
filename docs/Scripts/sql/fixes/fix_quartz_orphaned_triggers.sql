-- ===================================================================
-- 修復 Quartz 排程器的孤立 Trigger 問題
-- 問題：QRTZ_TRIGGERS 中有記錄，但 QRTZ_CRON_TRIGGERS 中沒有對應記錄
-- 執行時間：2025-11-04
-- ===================================================================

-- 1. 檢查孤立的 Trigger（有 TRIGGER 但沒有 CRON_TRIGGER）
SELECT 
    '孤立的 Trigger (需要清理)' AS issue_type,
    t.TRIGGER_NAME,
    t.TRIGGER_GROUP,
    t.TRIGGER_TYPE,
    t.TRIGGER_STATE,
    t.JOB_NAME,
    t.JOB_GROUP
FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_CRON_TRIGGERS ct 
    ON t.SCHED_NAME = ct.SCHED_NAME 
    AND t.TRIGGER_NAME = ct.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = ct.TRIGGER_GROUP
WHERE t.TRIGGER_TYPE = 'CRON'
  AND ct.TRIGGER_NAME IS NULL;

-- 2. 檢查孤立的 Job（有 JOB_DETAIL 但沒有對應的 TRIGGER）
SELECT 
    '孤立的 Job (可能需要清理)' AS issue_type,
    jd.JOB_NAME,
    jd.JOB_GROUP,
    jd.JOB_CLASS_NAME,
    jd.IS_DURABLE
FROM QRTZ_JOB_DETAILS jd
LEFT JOIN QRTZ_TRIGGERS t 
    ON jd.SCHED_NAME = t.SCHED_NAME 
    AND jd.JOB_NAME = t.JOB_NAME 
    AND jd.JOB_GROUP = t.JOB_GROUP
WHERE t.TRIGGER_NAME IS NULL
  AND jd.IS_DURABLE = '0';  -- 非持久化的 Job

-- 3. 顯示目前所有的 Trigger 狀態
SELECT 
    '目前所有 Trigger' AS info,
    t.TRIGGER_NAME,
    t.TRIGGER_GROUP,
    t.TRIGGER_TYPE,
    t.TRIGGER_STATE,
    t.JOB_NAME,
    CASE 
        WHEN ct.TRIGGER_NAME IS NOT NULL THEN '有 CRON'
        WHEN st.TRIGGER_NAME IS NOT NULL THEN '有 SIMPLE'
        ELSE '✗ 缺少詳情'
    END AS has_detail
FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_CRON_TRIGGERS ct 
    ON t.SCHED_NAME = ct.SCHED_NAME 
    AND t.TRIGGER_NAME = ct.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = ct.TRIGGER_GROUP
LEFT JOIN QRTZ_SIMPLE_TRIGGERS st 
    ON t.SCHED_NAME = st.SCHED_NAME 
    AND t.TRIGGER_NAME = st.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = st.TRIGGER_GROUP
ORDER BY t.TRIGGER_STATE, t.TRIGGER_NAME;

-- ===================================================================
-- 修復方案一：清理孤立的 CRON Trigger
-- ===================================================================

-- 備份 (可選)
CREATE TABLE IF NOT EXISTS qrtz_triggers_backup_20251104 AS 
SELECT * FROM QRTZ_TRIGGERS;

-- 刪除孤立的 CRON Trigger
DELETE t FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_CRON_TRIGGERS ct 
    ON t.SCHED_NAME = ct.SCHED_NAME 
    AND t.TRIGGER_NAME = ct.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = ct.TRIGGER_GROUP
WHERE t.TRIGGER_TYPE = 'CRON'
  AND ct.TRIGGER_NAME IS NULL;

-- 顯示刪除結果
SELECT 
    '已刪除孤立 Trigger' AS result,
    ROW_COUNT() AS deleted_count;

-- ===================================================================
-- 修復方案二：清理孤立的 SIMPLE Trigger
-- ===================================================================

DELETE t FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_SIMPLE_TRIGGERS st 
    ON t.SCHED_NAME = st.SCHED_NAME 
    AND t.TRIGGER_NAME = st.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = st.TRIGGER_GROUP
WHERE t.TRIGGER_TYPE = 'SIMPLE'
  AND st.TRIGGER_NAME IS NULL;

-- ===================================================================
-- 修復方案三：清理孤立的 Job (非持久化且沒有 Trigger)
-- ===================================================================

DELETE jd FROM QRTZ_JOB_DETAILS jd
LEFT JOIN QRTZ_TRIGGERS t 
    ON jd.SCHED_NAME = t.SCHED_NAME 
    AND jd.JOB_NAME = t.JOB_NAME 
    AND jd.JOB_GROUP = t.JOB_GROUP
WHERE t.TRIGGER_NAME IS NULL
  AND jd.IS_DURABLE = '0';

-- ===================================================================
-- 驗證修復結果
-- ===================================================================

-- 1. 確認沒有孤立的 Trigger
SELECT 
    'CRON Trigger 檢查' AS check_type,
    COUNT(*) AS orphaned_count,
    CASE WHEN COUNT(*) = 0 THEN '正常' ELSE '✗ 還有問題' END AS status
FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_CRON_TRIGGERS ct 
    ON t.SCHED_NAME = ct.SCHED_NAME 
    AND t.TRIGGER_NAME = ct.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = ct.TRIGGER_GROUP
WHERE t.TRIGGER_TYPE = 'CRON'
  AND ct.TRIGGER_NAME IS NULL

UNION ALL

SELECT 
    'SIMPLE Trigger 檢查' AS check_type,
    COUNT(*) AS orphaned_count,
    CASE WHEN COUNT(*) = 0 THEN '正常' ELSE '✗ 還有問題' END AS status
FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_SIMPLE_TRIGGERS st 
    ON t.SCHED_NAME = st.SCHED_NAME 
    AND t.TRIGGER_NAME = st.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = st.TRIGGER_GROUP
WHERE t.TRIGGER_TYPE = 'SIMPLE'
  AND st.TRIGGER_NAME IS NULL;

-- 2. 顯示修復後的 Trigger 列表
SELECT 
    '修復後的 Trigger' AS info,
    t.TRIGGER_NAME,
    t.TRIGGER_GROUP,
    t.TRIGGER_STATE,
    t.JOB_NAME,
    FROM_UNIXTIME(t.NEXT_FIRE_TIME/1000) AS next_fire_time
FROM QRTZ_TRIGGERS t
ORDER BY t.NEXT_FIRE_TIME;

-- ===================================================================
-- 預防措施：檢查 sys_job 表與 Quartz 表的一致性
-- ===================================================================

-- 檢查 sys_job 中有，但 Quartz 中沒有的任務
SELECT 
    'sys_job 有但 Quartz 沒有' AS issue_type,
    sj.job_id,
    sj.job_name,
    sj.job_group,
    sj.status,
    sj.invoke_target
FROM sys_job sj
LEFT JOIN QRTZ_JOB_DETAILS qj 
    ON CONCAT('TASK_CLASS_NAME', sj.job_id) = qj.JOB_NAME
WHERE qj.JOB_NAME IS NULL
  AND sj.status = '0';  -- 狀態為正常

-- 檢查 Quartz 中有，但 sys_job 中沒有的任務
SELECT 
    'Quartz 有但 sys_job 沒有' AS issue_type,
    qj.JOB_NAME,
    qj.JOB_GROUP,
    qj.JOB_CLASS_NAME
FROM QRTZ_JOB_DETAILS qj
LEFT JOIN sys_job sj 
    ON CONCAT('TASK_CLASS_NAME', sj.job_id) = qj.JOB_NAME
WHERE sj.job_id IS NULL
  AND qj.JOB_NAME LIKE 'TASK_CLASS_NAME%';

-- ===================================================================
-- 說明：
-- 1. 執行此腳本會自動清理孤立的 Trigger 和 Job
-- 2. 建議在執行前先備份資料庫
-- 3. 執行後需要重新啟動應用程式
-- 4. 如果有重要的排程任務，建議在前端重新建立
-- ===================================================================
