-- ============================
-- 庫存管理系統選單資料
-- ============================

-- 庫存管理主選單
INSERT INTO sys_menu
VALUES ('2000', '庫存管理', '0', '5', 'inventory', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin',
        sysdate(), '', NULL, '庫存管理系統');

-- 物品管理
INSERT INTO sys_menu
VALUES ('2001', '物品管理', '2000', '1', 'item', 'inventory/item/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:item:list', 'goods', 'admin', sysdate(), '', NULL, '物品管理選單');

-- 庫存管理
INSERT INTO sys_menu
VALUES ('2002', '庫存查詢', '2000', '2', 'stock', 'inventory/stock/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:stock:list', 'chart', 'admin', sysdate(), '', NULL, '庫存查詢選單');

-- 借出管理
INSERT INTO sys_menu
VALUES ('2003', '借出管理', '2000', '3', 'borrow', 'inventory/borrow/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:borrow:list', 'log', 'admin', sysdate(), '', NULL, '借出管理選單');

-- 掃描功能
INSERT INTO sys_menu
VALUES ('2004', '掃描功能', '2000', '4', 'scan', 'inventory/scan/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:scan:use', 'search', 'admin', sysdate(), '', NULL, '掃描功能選單');

-- 庫存報表
INSERT INTO sys_menu
VALUES ('2005', '庫存報表', '2000', '5', 'report', 'inventory/report/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:report:view', 'documentation', 'admin', sysdate(), '', NULL, '庫存報表選單');

-- 物品管理按鈕
INSERT INTO sys_menu
VALUES ('2011', '物品查詢', '2001', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:query', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2012', '物品新增', '2001', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:add', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2013', '物品修改', '2001', '3', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:edit', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2014', '物品刪除', '2001', '4', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:remove', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2015', '物品匯出', '2001', '5', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:export', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2016', '物品匯入', '2001', '6', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:import', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2017', '物品掃描', '2001', '7', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:scan', '#', 'admin',
        sysdate(), '', NULL, '');

-- 庫存管理按鈕
INSERT INTO sys_menu
VALUES ('2021', '庫存查詢', '2002', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:query', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2022', '庫存新增', '2002', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:add', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2023', '庫存修改', '2002', '3', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:edit', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2024', '庫存刪除', '2002', '4', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:remove', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2025', '庫存匯出', '2002', '5', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:export', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2026', '入庫操作', '2002', '6', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:in', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2027', '出庫操作', '2002', '7', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:out', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2028', '盤點操作', '2002', '8', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:check', '#', 'admin',
        sysdate(), '', NULL, '');

-- 借出管理按鈕
INSERT INTO sys_menu
VALUES ('2031', '借出查詢', '2003', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:query', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2032', '借出新增', '2003', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:add', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2033', '借出修改', '2003', '3', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:edit', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2034', '借出刪除', '2003', '4', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:remove', '#',
        'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2035', '借出匯出', '2003', '5', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:export', '#',
        'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2036', '借出物品', '2003', '6', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:borrow', '#',
        'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2037', '歸還物品', '2003', '7', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:return', '#',
        'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2038', '審核借出', '2003', '8', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:approve', '#',
        'admin', sysdate(), '', NULL, '');

-- 掃描功能按鈕
INSERT INTO sys_menu
VALUES ('2041', '掃描使用', '2004', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:scan:use', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2042', '掃描記錄', '2004', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:scan:log', '#', 'admin',
        sysdate(), '', NULL, '');

-- 庫存報表按鈕
INSERT INTO sys_menu
VALUES ('2051', '報表查看', '2005', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:report:view', '#', 'admin',
        sysdate(), '', NULL, '');
INSERT INTO sys_menu
VALUES ('2052', '報表匯出', '2005', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:report:export', '#',
        'admin', sysdate(), '', NULL, '');

-- 為管理員角色分配庫存管理權限
INSERT INTO sys_role_menu
VALUES ('1', '2000');
INSERT INTO sys_role_menu
VALUES ('1', '2001');
INSERT INTO sys_role_menu
VALUES ('1', '2002');
INSERT INTO sys_role_menu
VALUES ('1', '2003');
INSERT INTO sys_role_menu
VALUES ('1', '2004');
INSERT INTO sys_role_menu
VALUES ('1', '2005');
INSERT INTO sys_role_menu
VALUES ('1', '2011');
INSERT INTO sys_role_menu
VALUES ('1', '2012');
INSERT INTO sys_role_menu
VALUES ('1', '2013');
INSERT INTO sys_role_menu
VALUES ('1', '2014');
INSERT INTO sys_role_menu
VALUES ('1', '2015');
INSERT INTO sys_role_menu
VALUES ('1', '2016');
INSERT INTO sys_role_menu
VALUES ('1', '2017');
INSERT INTO sys_role_menu
VALUES ('1', '2021');
INSERT INTO sys_role_menu
VALUES ('1', '2022');
INSERT INTO sys_role_menu
VALUES ('1', '2023');
INSERT INTO sys_role_menu
VALUES ('1', '2024');
INSERT INTO sys_role_menu
VALUES ('1', '2025');
INSERT INTO sys_role_menu
VALUES ('1', '2026');
INSERT INTO sys_role_menu
VALUES ('1', '2027');
INSERT INTO sys_role_menu
VALUES ('1', '2028');
INSERT INTO sys_role_menu
VALUES ('1', '2031');
INSERT INTO sys_role_menu
VALUES ('1', '2032');
INSERT INTO sys_role_menu
VALUES ('1', '2033');
INSERT INTO sys_role_menu
VALUES ('1', '2034');
INSERT INTO sys_role_menu
VALUES ('1', '2035');
INSERT INTO sys_role_menu
VALUES ('1', '2036');
INSERT INTO sys_role_menu
VALUES ('1', '2037');
INSERT INTO sys_role_menu
VALUES ('1', '2038');
INSERT INTO sys_role_menu
VALUES ('1', '2041');
INSERT INTO sys_role_menu
VALUES ('1', '2042');
INSERT INTO sys_role_menu
VALUES ('1', '2051');
INSERT INTO sys_role_menu
VALUES ('1', '2052');
