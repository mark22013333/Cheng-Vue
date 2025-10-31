-- =========================================
-- LINE 模組權限更新 - 細化權限控制
-- 版本：V12
-- 建立時間：2025-10-28
-- 說明：將推播訊息權限拆分為查看和發送，避免誤發
-- =========================================

-- 取得當前最大選單 ID
SET @max_menu_id = (SELECT MAX(menu_id) FROM sys_menu);

-- 取得 LINE 管理選單的 ID（假設是最大 ID 的基準）
SET @line_parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '行銷管理' ORDER BY menu_id DESC LIMIT 1);
SET @line_message_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '推播訊息' ORDER BY menu_id DESC LIMIT 1);

-- 1. 刪除舊的推播訊息權限（如果存在）
DELETE FROM sys_menu WHERE perms IN ('line:message:query', 'line:message:send');

-- 2. 新增細化的推播訊息權限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES 
-- 查看相關權限（一般使用者可擁有）
(@max_menu_id + 1, '推播訊息列表', @line_message_id, 1, '#', '', 1, 0, 'F', '0', '0', 'line:message:list', '#',
 'admin', NOW(), '', NULL, '查看推播訊息列表'),
(@max_menu_id + 2, '推播訊息詳情', @line_message_id, 2, '#', '', 1, 0, 'F', '0', '0', 'line:message:query', '#',
 'admin', NOW(), '', NULL, '查看推播訊息詳情'),
(@max_menu_id + 3, '匯出推播訊息', @line_message_id, 3, '#', '', 1, 0, 'F', '0', '0', 'line:message:export', '#',
 'admin', NOW(), '', NULL, '匯出推播訊息記錄'),

-- 發送相關權限（需謹慎授予）
(@max_menu_id + 4, '發送推播訊息', @line_message_id, 4, '#', '', 1, 0, 'F', '0', '0', 'line:message:send', '#',
 'admin', NOW(), '', NULL, '發送 LINE 推播訊息（重要權限）'),

-- 管理相關權限
(@max_menu_id + 5, '刪除推播訊息', @line_message_id, 5, '#', '', 1, 0, 'F', '0', '0', 'line:message:remove', '#',
 'admin', NOW(), '', NULL, '刪除推播訊息記錄');

-- 3. 為管理員角色新增所有權限
SET @admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'admin' LIMIT 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_id BETWEEN @max_menu_id + 1 AND @max_menu_id + 5;

-- 4. 建立「行銷人員」角色範例（可選）
-- 此角色只有查看權限，沒有發送權限
-- 如果不需要可以註解掉此段

-- 檢查是否已存在行銷人員角色
SET @marketing_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'marketing' LIMIT 1);

-- 如果不存在則建立
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
                      status, del_flag, create_by, create_time, remark)
SELECT (SELECT IFNULL(MAX(role_id), 0) + 1 FROM (SELECT role_id FROM sys_role) AS temp),
       '行銷人員', 'marketing', 4, '2', 1, 1, '0', '0', 'admin', NOW(),
       '行銷人員角色，擁有 LINE 訊息查看權限，但無發送權限'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'marketing');

-- 取得行銷人員角色 ID
SET @marketing_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'marketing' LIMIT 1);

-- 為行銷人員角色新增查看權限（不包含發送和刪除）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @marketing_role_id, menu_id
FROM sys_menu
WHERE perms IN ('line:message:list', 'line:message:query', 'line:message:export')
  AND @marketing_role_id IS NOT NULL;

-- 5. 新增權限說明註解
UPDATE sys_menu 
SET remark = '【重要權限】發送 LINE 推播訊息，建議僅授予信任的管理人員'
WHERE perms = 'line:message:send';

UPDATE sys_menu 
SET remark = '查看推播訊息列表，一般使用者可擁有此權限'
WHERE perms = 'line:message:list';

-- 6. 記錄權限更新日誌
INSERT INTO sys_oper_log (title, business_type, method, request_method, operator_type, oper_name, oper_url, oper_ip,
                          oper_location, oper_param, json_result, status, error_msg, oper_time)
VALUES ('LINE模組權限更新', 0, 'V12__update_line_permissions.sql', 'SQL', 0, 'system', '/db/migration', '127.0.0.1',
        '內網IP', '細化 LINE 推播訊息權限，將查看和發送權限分離', '{"success": true}', 0, NULL, NOW());
