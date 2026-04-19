-- 測試資料庫連線和 Flyway 狀態
-- 使用方式: mysql -u root -p -P 23506 -h localhost

-- 1. 列出所有資料庫
SHOW DATABASES;

-- 2. 使用目標資料庫
USE cool-test;

-- 3. 列出所有表
SHOW TABLES;

-- 4. 檢查 Flyway 歷史表是否存在
SELECT COUNT(*) AS 'Flyway 歷史表是否存在' 
FROM information_schema.tables 
WHERE table_schema = 'cool-test' 
AND table_name = 'flyway_schema_history';

-- 5. 如果存在，查看遷移記錄
-- SELECT * FROM flyway_schema_history ORDER BY installed_rank;
