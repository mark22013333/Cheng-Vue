-- ============================
-- 庫存管理系統 - 選單資料
-- 版本：V3
-- 說明：初始化庫存管理系統的選單和權限配置
-- ============================

-- ----------------------------
-- 庫存管理主選單
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2000', '庫存管理', '0', '5', 'inventory', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin',
        sysdate(), '庫存管理系統');

-- ----------------------------
-- 物品管理選單
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2001', '物品管理', '2000', '1', 'item', 'inventory/item/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:item:list', 'goods', 'admin', sysdate(), '物品管理選單');

-- 物品管理按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2011', '物品查詢', '2001', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:query', '#', 'admin',
        sysdate(), ''),
       ('2012', '物品新增', '2001', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:add', '#', 'admin',
        sysdate(), ''),
       ('2013', '物品修改', '2001', '3', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:edit', '#', 'admin',
        sysdate(), ''),
       ('2014', '物品刪除', '2001', '4', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:remove', '#', 'admin',
        sysdate(), ''),
       ('2015', '物品匯出', '2001', '5', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:export', '#', 'admin',
        sysdate(), ''),
       ('2016', '物品匯入', '2001', '6', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:import', '#', 'admin',
        sysdate(), ''),
       ('2017', '物品掃描', '2001', '7', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:item:scan', '#', 'admin',
        sysdate(), '');

-- ----------------------------
-- 庫存查詢選單
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2002', '庫存查詢', '2000', '2', 'stock', 'inventory/stock/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:stock:list', 'chart', 'admin', sysdate(), '庫存查詢選單');

-- 庫存管理按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2021', '庫存查詢', '2002', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:query', '#', 'admin',
        sysdate(), ''),
       ('2022', '庫存新增', '2002', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:add', '#', 'admin',
        sysdate(), ''),
       ('2023', '庫存修改', '2002', '3', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:edit', '#', 'admin',
        sysdate(), ''),
       ('2024', '庫存刪除', '2002', '4', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:remove', '#', 'admin',
        sysdate(), ''),
       ('2025', '庫存匯出', '2002', '5', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:export', '#', 'admin',
        sysdate(), ''),
       ('2026', '入庫操作', '2002', '6', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:in', '#', 'admin',
        sysdate(), ''),
       ('2027', '出庫操作', '2002', '7', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:out', '#', 'admin',
        sysdate(), ''),
       ('2028', '盤點操作', '2002', '8', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:stock:check', '#', 'admin',
        sysdate(), '');

-- ----------------------------
-- 借出管理選單
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2003', '借出管理', '2000', '3', 'borrow', 'inventory/borrow/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:borrow:list', 'log', 'admin', sysdate(), '借出管理選單');

-- 借出管理按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2031', '借出查詢', '2003', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:query', '#', 'admin',
        sysdate(), ''),
       ('2032', '借出新增', '2003', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:add', '#', 'admin',
        sysdate(), ''),
       ('2033', '借出修改', '2003', '3', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:edit', '#', 'admin',
        sysdate(), ''),
       ('2034', '借出刪除', '2003', '4', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:remove', '#',
        'admin', sysdate(), ''),
       ('2035', '借出匯出', '2003', '5', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:export', '#',
        'admin', sysdate(), ''),
       ('2036', '借出物品', '2003', '6', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:borrow', '#',
        'admin', sysdate(), ''),
       ('2037', '歸還物品', '2003', '7', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:return', '#',
        'admin', sysdate(), ''),
       ('2038', '審核借出', '2003', '8', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:borrow:approve', '#',
        'admin', sysdate(), '');

-- ----------------------------
-- 掃描功能選單
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2004', '掃描功能', '2000', '4', 'scan', 'inventory/scan/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:scan:use', 'search', 'admin', sysdate(), '掃描功能選單');

-- 掃描功能按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2041', '掃描使用', '2004', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:scan:use', '#', 'admin',
        sysdate(), ''),
       ('2042', '掃描記錄', '2004', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:scan:log', '#', 'admin',
        sysdate(), '');

-- ----------------------------
-- 庫存報表選單
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2005', '庫存報表', '2000', '5', 'report', 'inventory/report/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:report:view', 'documentation', 'admin', sysdate(), '庫存報表選單');

-- 庫存報表按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2051', '報表查看', '2005', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:report:view', '#', 'admin',
        sysdate(), ''),
       ('2052', '報表匯出', '2005', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:report:export', '#',
        'admin', sysdate(), '');

-- ----------------------------
-- 物品分類選單（新增）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2006', '物品分類', '2000', '6', 'category', 'inventory/category/index', NULL, '', 1, 0, 'C', '0', '0',
        'inventory:category:list', 'tree-table', 'admin', sysdate(), '物品分類管理選單');

-- 物品分類按鈕
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('2061', '分類查詢', '2006', '1', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:category:query', '#', 'admin',
        sysdate(), ''),
       ('2062', '分類新增', '2006', '2', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:category:add', '#', 'admin',
        sysdate(), ''),
       ('2063', '分類修改', '2006', '3', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:category:edit', '#', 'admin',
        sysdate(), ''),
       ('2064', '分類刪除', '2006', '4', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:category:remove', '#', 'admin',
        sysdate(), ''),
       ('2065', '分類匯出', '2006', '5', '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inventory:category:export', '#', 'admin',
        sysdate(), '');

-- ----------------------------
-- 為管理員角色分配庫存管理權限
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) 
VALUES ('1', '2000'), ('1', '2001'), ('1', '2002'), ('1', '2003'), ('1', '2004'), ('1', '2005'), ('1', '2006'),
       ('1', '2011'), ('1', '2012'), ('1', '2013'), ('1', '2014'), ('1', '2015'), ('1', '2016'), ('1', '2017'),
       ('1', '2021'), ('1', '2022'), ('1', '2023'), ('1', '2024'), ('1', '2025'), ('1', '2026'), ('1', '2027'), ('1', '2028'),
       ('1', '2031'), ('1', '2032'), ('1', '2033'), ('1', '2034'), ('1', '2035'), ('1', '2036'), ('1', '2037'), ('1', '2038'),
       ('1', '2041'), ('1', '2042'),
       ('1', '2051'), ('1', '2052'),
       ('1', '2061'), ('1', '2062'), ('1', '2063'), ('1', '2064'), ('1', '2065');
