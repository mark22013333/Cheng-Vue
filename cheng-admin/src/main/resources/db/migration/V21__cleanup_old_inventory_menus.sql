-- ============================
-- 清理舊的庫存管理選單
-- 版本：V20
-- 說明：移除已整合的「物品管理」和「庫存查詢」選單，只保留「物品與庫存管理」
-- ============================

-- 1. 刪除舊選單的角色權限關聯（sys_role_menu）
-- 刪除「物品管理」(2001) 及其子選單的權限關聯
DELETE FROM sys_role_menu 
WHERE menu_id IN (
    2001,  -- 物品管理
    2011, 2012, 2013, 2014, 2015, 2016, 2017,  -- 物品管理子權限
    2002,  -- 庫存查詢
    2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028  -- 庫存查詢子權限
);

-- 2. 刪除舊選單項目（sys_menu）
-- 先刪除子選單（按鈕權限）
DELETE FROM sys_menu WHERE parent_id IN (2001, 2002);

-- 再刪除父選單
DELETE FROM sys_menu WHERE menu_id IN (2001, 2002);

-- 3. 驗證清理結果
-- 執行以下查詢確認已清理乾淨：
-- SELECT menu_id, menu_name, parent_id, visible, perms 
-- FROM sys_menu 
-- WHERE menu_name LIKE '%物品%' OR menu_name LIKE '%庫存%'
-- ORDER BY parent_id, order_num;

-- 預期結果：
-- - 應該看到「物品與庫存管理」及其子權限
-- - 不應該看到「物品管理」和「庫存查詢」

-- 4. 確保「物品與庫存管理」的權限已分配給管理員
-- 如果尚未分配，執行以下語句：
-- INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
-- SELECT '1', menu_id FROM sys_menu WHERE menu_name = '物品與庫存管理' OR parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '物品與庫存管理' LIMIT 1);
