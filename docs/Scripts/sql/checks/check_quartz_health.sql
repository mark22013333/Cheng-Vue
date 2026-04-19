-- ===================================================================
-- Quartz 排程器健康檢查腳本
-- 用途：定期檢查 Quartz 資料表的一致性
-- 建議：每週執行一次，或在出現錯誤時執行
-- ===================================================================

-- 1. 檢查孤立的 CRON Trigger
SELECT 
    '⚠️ 孤立的 CRON Trigger' AS issue,
    COUNT(*) AS count,
    GROUP_CONCAT(t.TRIGGER_NAME SEPARATOR ', ') AS trigger_names
FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_CRON_TRIGGERS ct 
    ON t.SCHED_NAME = ct.SCHED_NAME 
    AND t.TRIGGER_NAME = ct.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = ct.TRIGGER_GROUP
WHERE t.TRIGGER_TYPE = 'CRON'
  AND ct.TRIGGER_NAME IS NULL;

-- 2. 檢查孤立的 SIMPLE Trigger
SELECT 
    '⚠️ 孤立的 SIMPLE Trigger' AS issue,
    COUNT(*) AS count,
    GROUP_CONCAT(t.TRIGGER_NAME SEPARATOR ', ') AS trigger_names
FROM QRTZ_TRIGGERS t
LEFT JOIN QRTZ_SIMPLE_TRIGGERS st 
    ON t.SCHED_NAME = st.SCHED_NAME 
    AND t.TRIGGER_NAME = st.TRIGGER_NAME 
    AND t.TRIGGER_GROUP = st.TRIGGER_GROUP
WHERE t.TRIGGER_TYPE = 'SIMPLE'
  AND st.TRIGGER_NAME IS NULL;

-- 3. 檢查孤立的 Job（沒有 Trigger）
SELECT 
    '⚠️ 孤立的 Job (無 Trigger)' AS issue,
    COUNT(*) AS count,
    GROUP_CONCAT(jd.JOB_NAME SEPARATOR ', ') AS job_names
FROM QRTZ_JOB_DETAILS jd
LEFT JOIN QRTZ_TRIGGERS t 
    ON jd.SCHED_NAME = t.SCHED_NAME 
    AND jd.JOB_NAME = t.JOB_NAME 
    AND jd.JOB_GROUP = t.JOB_GROUP
WHERE t.TRIGGER_NAME IS NULL
  AND jd.IS_DURABLE = '0';

-- 4. 檢查 sys_job 與 Quartz 的不一致
SELECT 
    '⚠️ sys_job 有但 Quartz 沒有' AS issue,
    COUNT(*) AS count,
    GROUP_CONCAT(CONCAT(sj.job_id, ':', sj.job_name) SEPARATOR ', ') AS jobs
FROM sys_job sj
LEFT JOIN QRTZ_JOB_DETAILS qj 
    ON CONCAT('TASK_CLASS_NAME', sj.job_id) = qj.JOB_NAME
WHERE qj.JOB_NAME IS NULL
  AND sj.status = '0';

-- 5. 檢查阻塞的 Trigger
SELECT 
    '⚠️ 阻塞的 Trigger' AS issue,
    COUNT(*) AS count,
    GROUP_CONCAT(t.TRIGGER_NAME SEPARATOR ', ') AS trigger_names
FROM QRTZ_TRIGGERS t
WHERE t.TRIGGER_STATE = 'BLOCKED';

-- 6. 檢查錯誤的 Trigger
SELECT 
    '⚠️ 錯誤的 Trigger' AS issue,
    COUNT(*) AS count,
    GROUP_CONCAT(t.TRIGGER_NAME SEPARATOR ', ') AS trigger_names
FROM QRTZ_TRIGGERS t
WHERE t.TRIGGER_STATE = 'ERROR';

-- 7. 顯示所有正常的 Trigger
SELECT 
    '正常的 Trigger' AS status,
    COUNT(*) AS count
FROM QRTZ_TRIGGERS t
WHERE t.TRIGGER_STATE IN ('WAITING', 'PAUSED');

-- 8. 顯示即將執行的任務
SELECT 
    '📅 即將執行的任務' AS info,
    t.TRIGGER_NAME,
    t.JOB_NAME,
    t.TRIGGER_STATE,
    FROM_UNIXTIME(t.NEXT_FIRE_TIME/1000) AS next_run_time,
    TIMESTAMPDIFF(MINUTE, NOW(), FROM_UNIXTIME(t.NEXT_FIRE_TIME/1000)) AS minutes_until_next_run
FROM QRTZ_TRIGGERS t
WHERE t.TRIGGER_STATE = 'WAITING'
  AND t.NEXT_FIRE_TIME IS NOT NULL
ORDER BY t.NEXT_FIRE_TIME
LIMIT 10;

-- 9. 總結
SELECT 
    '=== 健康檢查總結 ===' AS summary,
    (SELECT COUNT(*) FROM QRTZ_JOB_DETAILS) AS total_jobs,
    (SELECT COUNT(*) FROM QRTZ_TRIGGERS) AS total_triggers,
    (SELECT COUNT(*) FROM QRTZ_TRIGGERS WHERE TRIGGER_STATE = 'WAITING') AS waiting_triggers,
    (SELECT COUNT(*) FROM QRTZ_TRIGGERS WHERE TRIGGER_STATE = 'PAUSED') AS paused_triggers,
    (SELECT COUNT(*) FROM QRTZ_TRIGGERS WHERE TRIGGER_STATE = 'BLOCKED') AS blocked_triggers,
    (SELECT COUNT(*) FROM QRTZ_TRIGGERS WHERE TRIGGER_STATE = 'ERROR') AS error_triggers;

-- ===================================================================
-- 判斷標準：
-- 健康：所有孤立/阻塞/錯誤的計數都是 0
-- ⚠️ 警告：有少量孤立記錄（1-5 個）
-- ❌ 嚴重：有大量孤立記錄或阻塞/錯誤狀態
-- ===================================================================
