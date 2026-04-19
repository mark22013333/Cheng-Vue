-- =========================================
-- 本地環境手動修復 SQL
-- 說明：修復 V32 錯誤建立的選單
-- 使用：在本地資料庫手動執行此腳本
-- 注意：此檔案不要提交到 UAT/PROD
-- =========================================

-- 1. 刪除 V32 錯誤建立的選單權限
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu 
    WHERE menu_name IN ('圖文訊息', '範本列表', '圖文訊息查詢', '圖文訊息新增', '圖文訊息修改', '圖文訊息刪除')
);

-- 2. 刪除 V32 錯誤建立的選單
DELETE FROM sys_menu 
WHERE menu_name IN ('圖文訊息', '範本列表', '圖文訊息查詢', '圖文訊息新增', '圖文訊息修改', '圖文訊息刪除');

-- 3. 取得「訊息範本」選單的 menu_id
SET @template_menu_id = (SELECT menu_id
                         FROM sys_menu
                         WHERE menu_name = '訊息範本'
                         LIMIT 1);

-- 4. 修復「訊息範本」選單（設定為目錄類型 + icon）
UPDATE sys_menu
SET menu_type = 'M',
    component  = NULL,
    path       = 'template',
    icon       = 'message'
WHERE menu_id = @template_menu_id;

-- 5. 取得 menu_id 最大值
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 3000)
                    FROM sys_menu);

-- 6. 重新建立「圖文訊息」子選單（icon: image）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 1, '圖文訊息', @template_menu_id, 1, 'imagemap', 'line/imagemap/index', 1, 0, 'C', '0', '0',
        'line:imagemap:list', 'image', 'admin', NOW(), '', NULL, 'LINE 圖文訊息管理');

-- 7. 重新建立「範本列表」子選單（icon: documentation）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 2, '範本列表', @template_menu_id, 2, 'list', 'line/template/index', 1, 0, 'C', '0', '0',
        'line:template:list', 'documentation', 'admin', NOW(), '', NULL, 'LINE 訊息範本列表');

-- 8. 重新建立圖文訊息相關按鈕權限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (@max_menu_id + 3, '圖文訊息查詢', @max_menu_id + 1, 1, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:query', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 4, '圖文訊息新增', @max_menu_id + 1, 2, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:add', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 5, '圖文訊息修改', @max_menu_id + 1, 3, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:edit', '#',
        'admin', NOW(), '', NULL, ''),
       (@max_menu_id + 6, '圖文訊息刪除', @max_menu_id + 1, 4, '#', '', 1, 0, 'F', '0', '0', 'line:imagemap:remove', '#',
        'admin', NOW(), '', NULL, '');

-- 9. 取得管理員角色ID
SET @admin_role_id = (SELECT role_id
                      FROM sys_role
                      WHERE role_key = 'admin'
                      LIMIT 1);

-- 10. 為管理員角色新增圖文訊息相關權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_id BETWEEN @max_menu_id + 1 AND @max_menu_id + 6;

-- 完成！請重新登入系統以刷新選單
