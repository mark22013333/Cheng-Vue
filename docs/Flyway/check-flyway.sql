-- ============================
-- Flyway 狀態檢查 SQL 腳本
-- ============================
-- 使用方式: mysql -u root -p cool_apps < docs/check-flyway.sql

-- 1. 檢查 Flyway 歷史表是否存在
SELECT '1. 檢查 Flyway 歷史表' AS '檢查項目';
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'Flyway 歷史表存在'
        ELSE '✗ Flyway 歷史表不存在'
    END AS '結果'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name = 'flyway_schema_history';

-- 2. 查看所有遷移記錄
SELECT '\n2. 遷移歷史記錄' AS '';
SELECT 
    installed_rank AS '執行順序',
    version AS '版本',
    description AS '描述',
    script AS '腳本名稱',
    DATE_FORMAT(installed_on, '%Y-%m-%d %H:%i:%s') AS '執行時間',
    execution_time AS '執行時間(ms)',
    CASE 
        WHEN success = 1 THEN '成功'
        ELSE '✗ 失敗'
    END AS '狀態'
FROM flyway_schema_history
ORDER BY installed_rank;

-- 3. 統計資訊
SELECT '\n3. 統計資訊' AS '';
SELECT 
    COUNT(*) AS '總遷移數量',
    SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) AS '成功數量',
    SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) AS '失敗數量'
FROM flyway_schema_history;

-- 4. 最新版本資訊
SELECT '\n4. 最新版本' AS '';
SELECT 
    version AS '目前版本',
    description AS '描述',
    DATE_FORMAT(installed_on, '%Y-%m-%d %H:%i:%s') AS '執行時間'
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 1;

-- 5. 檢查失敗的遷移
SELECT '\n5. 失敗的遷移（如有）' AS '';
SELECT 
    installed_rank AS '執行順序',
    version AS '版本',
    description AS '描述',
    script AS '腳本名稱',
    DATE_FORMAT(installed_on, '%Y-%m-%d %H:%i:%s') AS '執行時間'
FROM flyway_schema_history
WHERE success = 0
ORDER BY installed_rank;

-- 6. 檢查資料表統計
SELECT '\n6. 資料表統計' AS '';
SELECT 
    '系統核心表 (sys_*)' AS '類型',
    COUNT(*) AS '數量'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'sys_%'
UNION ALL
SELECT 
    'Quartz 表 (QRTZ_*)' AS '類型',
    COUNT(*) AS '數量'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'QRTZ_%'
UNION ALL
SELECT 
    '庫存管理表 (inv_*)' AS '類型',
    COUNT(*) AS '數量'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'inv_%'
UNION ALL
SELECT 
    '其他表' AS '類型',
    COUNT(*) AS '數量'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name NOT LIKE 'sys_%'
AND table_name NOT LIKE 'QRTZ_%'
AND table_name NOT LIKE 'inv_%';

-- 7. 列出所有庫存管理表
SELECT '\n7. 庫存管理表清單' AS '';
SELECT 
    table_name AS '表名',
    table_comment AS '說明',
    table_rows AS '資料筆數'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'inv_%'
ORDER BY table_name;

-- 8. 檢查關鍵表是否存在
SELECT '\n8. 關鍵表檢查' AS '';
SELECT 
    '存在' AS '狀態',
    table_name AS '表名'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name IN (
    'inv_category',
    'inv_item',
    'inv_stock',
    'inv_stock_record',
    'inv_borrow',
    'inv_return',
    'inv_scan_log',
    'inv_book_info'
);

-- 9. Flyway 健康度評估
SELECT '\n9. Flyway 健康度評估' AS '';
SELECT 
    CASE 
        WHEN failed_count = 0 AND total_count >= 7 THEN '健康 - 所有遷移都成功執行'
        WHEN failed_count > 0 THEN '✗ 異常 - 有失敗的遷移'
        WHEN total_count < 7 THEN '⚠ 警告 - 遷移數量少於預期'
        ELSE '? 未知狀態'
    END AS '健康狀態',
    total_count AS '總遷移數',
    success_count AS '成功數',
    failed_count AS '失敗數'
FROM (
    SELECT 
        COUNT(*) AS total_count,
        SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) AS success_count,
        SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) AS failed_count
    FROM flyway_schema_history
) AS stats;

-- 完成
SELECT '\n檢查完成' AS '';
