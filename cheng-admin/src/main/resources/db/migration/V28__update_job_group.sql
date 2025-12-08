-- ========================================
-- V28 更新任務分組（jobGroup）為更實用的業務分類
-- ========================================

-- 刪除舊的任務分組選項
DELETE FROM sys_dict_data WHERE dict_type = 'sys_job_group';

-- 新增實用的任務分組選項
INSERT INTO sys_dict_data VALUES (50, 1, '資料處理', 'DATA', 'sys_job_group', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '資料同步、匯入匯出、格式轉換等');
INSERT INTO sys_dict_data VALUES (51, 2, '系統維護', 'MAINTENANCE', 'sys_job_group', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '備份、清理、健康檢查、日誌歸檔等');
INSERT INTO sys_dict_data VALUES (52, 3, '通知推播', 'NOTIFICATION', 'sys_job_group', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'LINE推播、郵件通知、簡訊發送等');
INSERT INTO sys_dict_data VALUES (53, 4, '報表統計', 'REPORT', 'sys_job_group', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '日報、週報、月報、統計分析等');
INSERT INTO sys_dict_data VALUES (54, 5, '業務處理', 'BUSINESS', 'sys_job_group', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '訂單處理、庫存盤點、資料對帳等');
INSERT INTO sys_dict_data VALUES (55, 6, '爬蟲抓取', 'CRAWLER', 'sys_job_group', '', '', 'N', '0', 'admin', sysdate(), '', null, '網頁抓取、API呼叫、資料蒐集等');

-- 更新現有任務的分組（根據實際情況調整）
-- 範例：將現有的 DEFAULT 和 SYSTEM 分組更新為新的分類
-- UPDATE sys_job SET job_group = 'DATA' WHERE job_group = 'DEFAULT';
-- UPDATE sys_job SET job_group = 'MAINTENANCE' WHERE job_group = 'SYSTEM';
