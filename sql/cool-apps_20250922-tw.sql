-- ----------------------------
-- 1、部門表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept
(
    dept_id     bigint(20)      not null auto_increment    comment '部門id',
    parent_id   bigint(20)      default 0                  comment '父部門id',
    ancestors   varchar(50) default '' comment '祖級列表',
    dept_name   varchar(30) default '' comment '部門名稱',
    order_num   int(4)          default 0                  comment '顯示順序',
    leader      varchar(20) default null comment '負責人',
    phone       varchar(11) default null comment '聯絡電話',
    email       varchar(50) default null comment '信箱',
    status      char(1)     default '0' comment '部門狀態（0正常 1停用）',
    del_flag    char(1)     default '0' comment '刪除標誌（0代表存在 2代表刪除）',
    create_by   varchar(64) default '' comment '建立者',
    create_time datetime comment '建立時間',
    update_by   varchar(64) default '' comment '更新者',
    update_time datetime comment '更新時間',
    primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部門表';

-- ----------------------------
-- 初始化-部門表資料
-- ----------------------------
insert into sys_dept
values (100, 0, '0', 'CoolApps科技', 0, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(), '',
        null);
insert into sys_dept
values (101, 100, '0,100', '台中總公司', 1, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(), '',
        null);
insert into sys_dept
values (102, 100, '0,100', '台南分公司', 2, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(), '',
        null);
insert into sys_dept
values (103, 101, '0,100,101', '研發部門', 1, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(),
        '',
        null);
insert into sys_dept
values (104, 101, '0,100,101', '市場部門', 2, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(),
        '',
        null);
insert into sys_dept
values (105, 101, '0,100,101', '測試部門', 3, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(),
        '',
        null);
insert into sys_dept
values (106, 101, '0,100,101', '財務部門', 4, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(),
        '',
        null);
insert into sys_dept
values (107, 101, '0,100,101', '維運部門', 5, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(),
        '',
        null);
insert into sys_dept
values (108, 102, '0,100,102', '市場部門', 1, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(),
        '',
        null);
insert into sys_dept
values (109, 102, '0,100,102', '財務部門', 2, 'CoolApps', '0932313520', 'ry@yahoo.com', '0', '0', 'admin', sysdate(),
        '',
        null);


-- ----------------------------
-- 2、使用者資訊表
-- ----------------------------
drop table if exists sys_user;
create table sys_user
(
    user_id         bigint(20)      not null auto_increment    comment '使用者ID',
    dept_id         bigint(20)      default null               comment '部門ID',
    user_name       varchar(30) not null comment '使用者帳號',
    nick_name       varchar(30) not null comment '使用者暱稱',
    user_type       varchar(2)   default '00' comment '使用者類型（00系統使用者）',
    email           varchar(50)  default '' comment '使用者信箱',
    phonenumber     varchar(11)  default '' comment '手機號碼',
    sex             char(1)      default '0' comment '使用者性別（0男 1女 2未知）',
    avatar          varchar(100) default '' comment '頭像地址',
    password        varchar(100) default '' comment '密碼',
    status          char(1)      default '0' comment '帳號狀態（0正常 1停用）',
    del_flag        char(1)      default '0' comment '刪除標誌（0代表存在 2代表刪除）',
    login_ip        varchar(128) default '' comment '最後登入IP',
    login_date      datetime comment '最後登入時間',
    pwd_update_date datetime comment '密碼最後更新時間',
    create_by       varchar(64)  default '' comment '建立者',
    create_time     datetime comment '建立時間',
    update_by       varchar(64)  default '' comment '更新者',
    update_time     datetime comment '更新時間',
    remark          varchar(500) default null comment '備註',
    primary key (user_id)
) engine=innodb auto_increment=100 comment = '使用者資訊表';

-- ----------------------------
-- 初始化-使用者資訊表資料
-- ----------------------------
insert into sys_user
values (1, 103, 'admin', 'CoolApps', '00', 'ry@gmail.com', '0932313520', '1', '',
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(),
        'admin', sysdate(), '', null, '管理員');
insert into sys_user
values (2, 105, 'ry', 'CoolApps', '00', 'ry@yahoo.com', '0912345678', '1', '',
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(),
        'admin', sysdate(), '', null, '測試員');


-- ----------------------------
-- 3、職位資訊表
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
    post_id     bigint(20)      not null auto_increment    comment '職位ID',
    post_code   varchar(64) not null comment '職位編碼',
    post_name   varchar(50) not null comment '職位名稱',
    post_sort   int(4)          not null                   comment '顯示順序',
    status      char(1)     not null comment '狀態（0正常 1停用）',
    create_by   varchar(64)  default '' comment '建立者',
    create_time datetime comment '建立時間',
    update_by   varchar(64)  default '' comment '更新者',
    update_time datetime comment '更新時間',
    remark      varchar(500) default null comment '備註',
    primary key (post_id)
) engine=innodb comment = '職位資訊表';

-- ----------------------------
-- 初始化-職位資訊表資料
-- ----------------------------
insert into sys_post
values (1, 'ceo', '董事長', 1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post
values (2, 'se', '專案經理', 2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post
values (3, 'hr', '人力資源', 3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post
values (4, 'user', '普通員工', 4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4、角色訊息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role
(
    role_id             bigint(20)      not null auto_increment    comment '角色ID',
    role_name           varchar(30)  not null comment '角色名稱',
    role_key            varchar(100) not null comment '角色許可權字串',
    role_sort           int(4)          not null                   comment '顯示順序',
    data_scope          char(1)      default '1' comment '資料範圍（1：全部資料許可權 2：自訂數據許可權 3：本部門資料許可權 4：本部門及以下資料許可權）',
    menu_check_strictly tinyint(1)      default 1                  comment '功能表樹選擇項是否關聯顯示',
    dept_check_strictly tinyint(1)      default 1                  comment '部門樹選擇項是否關聯顯示',
    status              char(1)      not null comment '角色狀態（0正常 1停用）',
    del_flag            char(1)      default '0' comment '刪除標誌（0代表存在 2代表刪除）',
    create_by           varchar(64)  default '' comment '建立者',
    create_time         datetime comment '建立時間',
    update_by           varchar(64)  default '' comment '更新者',
    update_time         datetime comment '更新時間',
    remark              varchar(500) default null comment '備註',
    primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色訊息表';

-- ----------------------------
-- 初始化-角色資訊表資料
-- ----------------------------
insert into sys_role
values ('1', '超級管理員', 'admin', 1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超級管理員');
insert into sys_role
values ('2', '普通角色', 'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');


-- ----------------------------
-- 5、選單許可權表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu
(
    menu_id     bigint(20)      not null auto_increment    comment '選單ID',
    menu_name   varchar(50) not null comment '選單名稱',
    parent_id   bigint(20)      default 0                  comment '父選單ID',
    order_num   int(4)          default 0                  comment '顯示順序',
    path        varchar(200) default '' comment '路由地址',
    component   varchar(255) default null comment '元件路徑',
    query       varchar(255) default null comment '路由參數',
    route_name  varchar(50)  default '' comment '路由名稱',
    is_frame    int(1)          default 1                  comment '是否為外部連結（0是 1否）',
    is_cache    int(1)          default 0                  comment '是否暫存（0暫存 1不暫存）',
    menu_type   char(1)      default '' comment '功能表類型（M目錄 C功能表 F按鈕）',
    visible     char(1)      default 0 comment '功能表狀態（0顯示 1隱藏）',
    status      char(1)      default 0 comment '功能表狀態（0正常 1停用）',
    perms       varchar(100) default null comment '許可權標識',
    icon        varchar(100) default '#' comment '功能表圖示',
    create_by   varchar(64)  default '' comment '建立者',
    create_time datetime comment '建立時間',
    update_by   varchar(64)  default '' comment '更新者',
    update_time datetime comment '更新時間',
    remark      varchar(500) default '' comment '備註',
    primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '選單許可權表';

-- ----------------------------
-- 初始化-功能表資訊表資料
-- ----------------------------
-- 一級選單
insert into sys_menu
values ('1', '系統管理', '0', '1', 'system', null, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', sysdate(), '',
        null, '系統管理目錄');
insert into sys_menu
values ('2', '系統監控', '0', '2', 'monitor', null, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', sysdate(), '',
        null, '系統監控目錄');
insert into sys_menu
values ('3', '系統工具', '0', '3', 'tool', null, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate(), '', null,
        '系統工具目錄');
insert into sys_menu
values ('4', 'CoolApps官網', '0', '4', 'http://cheng.vip', null, '', '', 0, 0, 'M', '0', '0', '', 'guide', 'admin',
        sysdate(), '', null, 'CoolApps官網地址');
-- 二級選單
insert into sys_menu
values ('100', '使用者管理', '1', '1', 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list',
        'user', 'admin', sysdate(), '', null, '使用者管理功能表');
insert into sys_menu
values ('101', '角色管理', '1', '2', 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list',
        'peoples', 'admin', sysdate(), '', null, '角色管理功能表');
insert into sys_menu
values ('102', '功能表管理', '1', '3', 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list',
        'tree-table', 'admin', sysdate(), '', null, '功能表管理功能表');
insert into sys_menu
values ('103', '部門管理', '1', '4', 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list',
        'tree', 'admin', sysdate(), '', null, '部門管理功能表');
insert into sys_menu
values ('104', '職位管理', '1', '5', 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list',
        'post', 'admin', sysdate(), '', null, '職位管理功能表');
insert into sys_menu
values ('105', '字典管理', '1', '6', 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list',
        'dict', 'admin', sysdate(), '', null, '字典管理功能表');
insert into sys_menu
values ('106', '參數設定', '1', '7', 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list',
        'edit', 'admin', sysdate(), '', null, '參數設定選單');
insert into sys_menu
values ('107', '通知公告', '1', '8', 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list',
        'message', 'admin', sysdate(), '', null, '通知公告選單');
insert into sys_menu
values ('108', '日誌管理', '1', '9', 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', sysdate(), '', null,
        '日誌管理功能表');
insert into sys_menu
values ('109', '線上使用者', '2', '1', 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0',
        'monitor:online:list', 'online', 'admin', sysdate(), '', null, '線上使用者功能表');
insert into sys_menu
values ('110', '定時任務', '2', '2', 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job',
        'admin', sysdate(), '', null, '定時任務功能表');
insert into sys_menu
values ('111', '資料監控', '2', '3', 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',
        'druid', 'admin', sysdate(), '', null, '資料監控功能表');
insert into sys_menu
values ('112', '服務監控', '2', '4', 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0',
        'monitor:server:list', 'server', 'admin', sysdate(), '', null, '服務監控功能表');
insert into sys_menu
values ('113', '暫存監控', '2', '5', 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',
        'redis', 'admin', sysdate(), '', null, '暫存監控選單');
insert into sys_menu
values ('114', '暫存列表', '2', '6', 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0',
        'monitor:cache:list', 'redis-list', 'admin', sysdate(), '', null, '暫存清單選單');
insert into sys_menu
values ('115', '表單建構', '3', '1', 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list',
        'build', 'admin', sysdate(), '', null, '表單建構選單');
insert into sys_menu
values ('116', '程式碼產生', '3', '2', 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code',
        'admin', sysdate(), '', null, '程式碼產生功能表');
insert into sys_menu
values ('117', '系統介面', '3', '3', 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',
        'swagger', 'admin', sysdate(), '', null, '系統介面功能表');
-- 三級選單
insert into sys_menu
values ('500', '操作日誌', '108', '1', 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0',
        'monitor:operlog:list', 'form', 'admin', sysdate(), '', null, '操作日誌功能表');
insert into sys_menu
values ('501', '登入日誌', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0',
        'monitor:logininfor:list', 'logininfor', 'admin', sysdate(), '', null, '登入日誌功能表');
-- 使用者管理按鈕
insert into sys_menu
values ('1000', '使用者查詢', '100', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1001', '使用者新增', '100', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin',
        sysdate(),
        '', null, '');
insert into sys_menu
values ('1002', '使用者修改', '100', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1003', '使用者刪除', '100', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1004', '使用者匯出', '100', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1005', '使用者匯入', '100', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1006', '重置密碼', '100', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin',
        sysdate(), '', null, '');
-- 角色管理按鈕
insert into sys_menu
values ('1007', '角色查詢', '101', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1008', '角色新增', '101', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', sysdate(),
        '', null, '');
insert into sys_menu
values ('1009', '角色修改', '101', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1010', '角色刪除', '101', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1011', '角色匯出', '101', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin',
        sysdate(), '', null, '');
-- 功能表管理按鈕
insert into sys_menu
values ('1012', '選單查詢', '102', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1013', '選單新增', '102', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', sysdate(),
        '', null, '');
insert into sys_menu
values ('1014', '選單修改', '102', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1015', '選單刪除', '102', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin',
        sysdate(), '', null, '');
-- 部門管理按鈕
insert into sys_menu
values ('1016', '部門查詢', '103', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1017', '部門新增', '103', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', sysdate(),
        '', null, '');
insert into sys_menu
values ('1018', '部門修改', '103', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1019', '部門刪除', '103', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin',
        sysdate(), '', null, '');
-- 職位管理按鈕
insert into sys_menu
values ('1020', '職位查詢', '104', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1021', '職位新增', '104', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', sysdate(),
        '', null, '');
insert into sys_menu
values ('1022', '職位修改', '104', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1023', '職位刪除', '104', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1024', '職位匯出', '104', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin',
        sysdate(), '', null, '');
-- 字典管理按鈕
insert into sys_menu
values ('1025', '字典查詢', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1026', '字典新增', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1027', '字典修改', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1028', '字典刪除', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1029', '字典匯出', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin',
        sysdate(), '', null, '');
-- 參數設定按鈕
insert into sys_menu
values ('1030', '參數查詢', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1031', '參數新增', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1032', '參數修改', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1033', '參數刪除', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1034', '參數匯出', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin',
        sysdate(), '', null, '');
-- 通知公告按鈕
insert into sys_menu
values ('1035', '公告查詢', '107', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1036', '公告新增', '107', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1037', '公告修改', '107', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1038', '公告刪除', '107', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin',
        sysdate(), '', null, '');
-- 操作日誌按鈕
insert into sys_menu
values ('1039', '操作查詢', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1040', '操作刪除', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1041', '日誌匯出', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin',
        sysdate(), '', null, '');
-- 登入日誌按鈕
insert into sys_menu
values ('1042', '登入查詢', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1043', '登入刪除', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1044', '日誌匯出', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1045', '帳戶解鎖', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin',
        sysdate(), '', null, '');
-- 線上使用者按鈕
insert into sys_menu
values ('1046', '線上查詢', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1047', '批次強退', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#',
        'admin', sysdate(), '', null, '');
insert into sys_menu
values ('1048', '單條強退', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#',
        'admin', sysdate(), '', null, '');
-- 定時任務按鈕
insert into sys_menu
values ('1049', '任務查詢', '110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1050', '任務新增', '110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1051', '任務修改', '110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1052', '任務刪除', '110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1053', '狀態修改', '110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1054', '任務匯出', '110', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin',
        sysdate(), '', null, '');
-- 程式碼產生按鈕
insert into sys_menu
values ('1055', '產生查詢', '116', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', sysdate(),
        '', null, '');
insert into sys_menu
values ('1056', '產生修改', '116', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', sysdate(),
        '', null, '');
insert into sys_menu
values ('1057', '產生刪除', '116', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1058', '匯入程式碼', '116', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1059', '預覽程式碼', '116', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin',
        sysdate(), '', null, '');
insert into sys_menu
values ('1060', '產生程式碼', '116', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin',
        sysdate(),
        '', null, '');


-- ----------------------------
-- 6、使用者和角色關聯表  使用者N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role
(
    user_id bigint(20) not null comment '使用者ID',
    role_id bigint(20) not null comment '角色ID',
    primary key (user_id, role_id)
) engine=innodb comment = '使用者和角色關聯表';

-- ----------------------------
-- 初始化-使用者和角色關聯表資料
-- ----------------------------
insert into sys_user_role
values ('1', '1');
insert into sys_user_role
values ('2', '2');


-- ----------------------------
-- 7、角色和選單關聯表  角色1-N選單
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu
(
    role_id bigint(20) not null comment '角色ID',
    menu_id bigint(20) not null comment '選單ID',
    primary key (role_id, menu_id)
) engine=innodb comment = '角色和選單關聯表';

-- ----------------------------
-- 初始化-角色和功能表關聯表資料
-- ----------------------------
insert into sys_role_menu
values ('2', '1');
insert into sys_role_menu
values ('2', '2');
insert into sys_role_menu
values ('2', '3');
insert into sys_role_menu
values ('2', '4');
insert into sys_role_menu
values ('2', '100');
insert into sys_role_menu
values ('2', '101');
insert into sys_role_menu
values ('2', '102');
insert into sys_role_menu
values ('2', '103');
insert into sys_role_menu
values ('2', '104');
insert into sys_role_menu
values ('2', '105');
insert into sys_role_menu
values ('2', '106');
insert into sys_role_menu
values ('2', '107');
insert into sys_role_menu
values ('2', '108');
insert into sys_role_menu
values ('2', '109');
insert into sys_role_menu
values ('2', '110');
insert into sys_role_menu
values ('2', '111');
insert into sys_role_menu
values ('2', '112');
insert into sys_role_menu
values ('2', '113');
insert into sys_role_menu
values ('2', '114');
insert into sys_role_menu
values ('2', '115');
insert into sys_role_menu
values ('2', '116');
insert into sys_role_menu
values ('2', '117');
insert into sys_role_menu
values ('2', '500');
insert into sys_role_menu
values ('2', '501');
insert into sys_role_menu
values ('2', '1000');
insert into sys_role_menu
values ('2', '1001');
insert into sys_role_menu
values ('2', '1002');
insert into sys_role_menu
values ('2', '1003');
insert into sys_role_menu
values ('2', '1004');
insert into sys_role_menu
values ('2', '1005');
insert into sys_role_menu
values ('2', '1006');
insert into sys_role_menu
values ('2', '1007');
insert into sys_role_menu
values ('2', '1008');
insert into sys_role_menu
values ('2', '1009');
insert into sys_role_menu
values ('2', '1010');
insert into sys_role_menu
values ('2', '1011');
insert into sys_role_menu
values ('2', '1012');
insert into sys_role_menu
values ('2', '1013');
insert into sys_role_menu
values ('2', '1014');
insert into sys_role_menu
values ('2', '1015');
insert into sys_role_menu
values ('2', '1016');
insert into sys_role_menu
values ('2', '1017');
insert into sys_role_menu
values ('2', '1018');
insert into sys_role_menu
values ('2', '1019');
insert into sys_role_menu
values ('2', '1020');
insert into sys_role_menu
values ('2', '1021');
insert into sys_role_menu
values ('2', '1022');
insert into sys_role_menu
values ('2', '1023');
insert into sys_role_menu
values ('2', '1024');
insert into sys_role_menu
values ('2', '1025');
insert into sys_role_menu
values ('2', '1026');
insert into sys_role_menu
values ('2', '1027');
insert into sys_role_menu
values ('2', '1028');
insert into sys_role_menu
values ('2', '1029');
insert into sys_role_menu
values ('2', '1030');
insert into sys_role_menu
values ('2', '1031');
insert into sys_role_menu
values ('2', '1032');
insert into sys_role_menu
values ('2', '1033');
insert into sys_role_menu
values ('2', '1034');
insert into sys_role_menu
values ('2', '1035');
insert into sys_role_menu
values ('2', '1036');
insert into sys_role_menu
values ('2', '1037');
insert into sys_role_menu
values ('2', '1038');
insert into sys_role_menu
values ('2', '1039');
insert into sys_role_menu
values ('2', '1040');
insert into sys_role_menu
values ('2', '1041');
insert into sys_role_menu
values ('2', '1042');
insert into sys_role_menu
values ('2', '1043');
insert into sys_role_menu
values ('2', '1044');
insert into sys_role_menu
values ('2', '1045');
insert into sys_role_menu
values ('2', '1046');
insert into sys_role_menu
values ('2', '1047');
insert into sys_role_menu
values ('2', '1048');
insert into sys_role_menu
values ('2', '1049');
insert into sys_role_menu
values ('2', '1050');
insert into sys_role_menu
values ('2', '1051');
insert into sys_role_menu
values ('2', '1052');
insert into sys_role_menu
values ('2', '1053');
insert into sys_role_menu
values ('2', '1054');
insert into sys_role_menu
values ('2', '1055');
insert into sys_role_menu
values ('2', '1056');
insert into sys_role_menu
values ('2', '1057');
insert into sys_role_menu
values ('2', '1058');
insert into sys_role_menu
values ('2', '1059');
insert into sys_role_menu
values ('2', '1060');

-- ----------------------------
-- 8、角色和部門關聯表  角色1-N部門
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept
(
    role_id bigint(20) not null comment '角色ID',
    dept_id bigint(20) not null comment '部門ID',
    primary key (role_id, dept_id)
) engine=innodb comment = '角色和部門關聯表';

-- ----------------------------
-- 初始化-角色和部門關聯表資料
-- ----------------------------
insert into sys_role_dept
values ('2', '100');
insert into sys_role_dept
values ('2', '101');
insert into sys_role_dept
values ('2', '105');


-- ----------------------------
-- 9、使用者與職位關聯表  使用者1-N職位
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
    user_id bigint(20) not null comment '使用者ID',
    post_id bigint(20) not null comment '職位ID',
    primary key (user_id, post_id)
) engine=innodb comment = '使用者與職位關聯表';

-- ----------------------------
-- 初始化-使用者與職位關聯表資料
-- ----------------------------
insert into sys_user_post
values ('1', '1');
insert into sys_user_post
values ('2', '2');


-- ----------------------------
-- 10、操作日誌記錄
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log
(
    oper_id        bigint(20)      not null auto_increment    comment '日誌主鍵',
    title          varchar(50)   default '' comment '模組標題',
    business_type  int(2)          default 0                  comment '業務類型（0其它 1新增 2修改 3刪除）',
    method         varchar(200)  default '' comment '方法名稱',
    request_method varchar(10)   default '' comment '請求方式',
    operator_type  int(1)          default 0                  comment '操作類別（0其它 1後台使用者 2手機端使用者）',
    oper_name      varchar(50)   default '' comment '操作人員',
    dept_name      varchar(50)   default '' comment '部門名稱',
    oper_url       varchar(255)  default '' comment '請求URL',
    oper_ip        varchar(128)  default '' comment '主機位址',
    oper_location  varchar(255)  default '' comment '操作地點',
    oper_param     varchar(2000) default '' comment '請求參數',
    json_result    varchar(2000) default '' comment '返回參數',
    status         int(1)          default 0                  comment '操作狀態（0正常 1異常）',
    error_msg      varchar(2000) default '' comment '錯誤訊息',
    oper_time      datetime comment '操作時間',
    cost_time      bigint(20)      default 0                  comment '消耗時間',
    primary key (oper_id),
    key            idx_sys_oper_log_bt (business_type),
    key            idx_sys_oper_log_s (status),
    key            idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日誌記錄';


-- ----------------------------
-- 11、字典類型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
    dict_id     bigint(20)      not null auto_increment    comment '字典主鍵',
    dict_name   varchar(100) default '' comment '字典名稱',
    dict_type   varchar(100) default '' comment '字典類型',
    status      char(1)      default '0' comment '狀態（0正常 1停用）',
    create_by   varchar(64)  default '' comment '建立者',
    create_time datetime comment '建立時間',
    update_by   varchar(64)  default '' comment '更新者',
    update_time datetime comment '更新時間',
    remark      varchar(500) default null comment '備註',
    primary key (dict_id),
    unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典類型表';

insert into sys_dict_type
values (1, '使用者性別', 'sys_user_sex', '0', 'admin', sysdate(), '', null, '使用者性別列表');
insert into sys_dict_type
values (2, '功能表狀態', 'sys_show_hide', '0', 'admin', sysdate(), '', null, '功能表狀態清單');
insert into sys_dict_type
values (3, '系統開關', 'sys_normal_disable', '0', 'admin', sysdate(), '', null, '系統開關清單');
insert into sys_dict_type
values (4, '任務狀態', 'sys_job_status', '0', 'admin', sysdate(), '', null, '任務狀態清單');
insert into sys_dict_type
values (5, '任務分組', 'sys_job_group', '0', 'admin', sysdate(), '', null, '任務分組列表');
insert into sys_dict_type
values (6, '系統是否', 'sys_yes_no', '0', 'admin', sysdate(), '', null, '系統是否清單');
insert into sys_dict_type
values (7, '通知類型', 'sys_notice_type', '0', 'admin', sysdate(), '', null, '通知類型列表');
insert into sys_dict_type
values (8, '通知狀態', 'sys_notice_status', '0', 'admin', sysdate(), '', null, '通知狀態清單');
insert into sys_dict_type
values (9, '操作類型', 'sys_oper_type', '0', 'admin', sysdate(), '', null, '操作類型列表');
insert into sys_dict_type
values (10, '系統狀態', 'sys_common_status', '0', 'admin', sysdate(), '', null, '登入狀態清單');


-- ----------------------------
-- 12、字典資料表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
    dict_code   bigint(20)      not null auto_increment    comment '字典編碼',
    dict_sort   int(4)          default 0                  comment '字典排序',
    dict_label  varchar(100) default '' comment '字典標籤',
    dict_value  varchar(100) default '' comment '字典鍵值',
    dict_type   varchar(100) default '' comment '字典類型',
    css_class   varchar(100) default null comment '樣式屬性（其他樣式擴充）',
    list_class  varchar(100) default null comment '表格顯示樣式',
    is_default  char(1)      default 'N' comment '是否預設（Y是 N否）',
    status      char(1)      default '0' comment '狀態（0正常 1停用）',
    create_by   varchar(64)  default '' comment '建立者',
    create_time datetime comment '建立時間',
    update_by   varchar(64)  default '' comment '更新者',
    update_time datetime comment '更新時間',
    remark      varchar(500) default null comment '備註',
    primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典資料表';

insert into sys_dict_data
values (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', sysdate(), '', null, '性別男');
insert into sys_dict_data
values (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', sysdate(), '', null, '性別女');
insert into sys_dict_data
values (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', sysdate(), '', null, '性別未知');
insert into sys_dict_data
values (4, 1, '顯示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '顯示功能表');
insert into sys_dict_data
values (5, 2, '隱藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '隱藏選單');
insert into sys_dict_data
values (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常狀態');
insert into sys_dict_data
values (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '停用狀態');
insert into sys_dict_data
values (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常狀態');
insert into sys_dict_data
values (9, 2, '暫停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '停用狀態');
insert into sys_dict_data
values (10, 1, '預設', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', sysdate(), '', null, '預設分組');
insert into sys_dict_data
values (11, 2, '系統', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', sysdate(), '', null, '系統分組');
insert into sys_dict_data
values (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '系統預設是');
insert into sys_dict_data
values (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '系統預設否');
insert into sys_dict_data
values (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', sysdate(), '', null, '通知');
insert into sys_dict_data
values (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '公告');
insert into sys_dict_data
values (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常狀態');
insert into sys_dict_data
values (17, 2, '關閉', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '關閉狀態');
insert into sys_dict_data
values (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '其他操作');
insert into sys_dict_data
values (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data
values (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data
values (21, 3, '刪除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '刪除操作');
insert into sys_dict_data
values (22, 4, '授權', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '授權操作');
insert into sys_dict_data
values (23, 5, '匯出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '匯出操作');
insert into sys_dict_data
values (24, 6, '匯入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '匯入操作');
insert into sys_dict_data
values (25, 7, '強退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '強退操作');
insert into sys_dict_data
values (26, 8, '產生程式碼', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '產生操作');
insert into sys_dict_data
values (27, 9, '清除數據', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '清除操作');
insert into sys_dict_data
values (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '正常狀態');
insert into sys_dict_data
values (29, 2, '失敗', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '停用狀態');


-- ----------------------------
-- 13、參數設定表
-- ----------------------------
drop table if exists sys_config;
create table sys_config
(
    config_id    int(5)          not null auto_increment    comment '參數主鍵',
    config_name  varchar(100) default '' comment '參數名稱',
    config_key   varchar(100) default '' comment '參數鍵名',
    config_value varchar(500) default '' comment '參數鍵值',
    config_type  char(1)      default 'N' comment '系統內置（Y是 N否）',
    create_by    varchar(64)  default '' comment '建立者',
    create_time  datetime comment '建立時間',
    update_by    varchar(64)  default '' comment '更新者',
    update_time  datetime comment '更新時間',
    remark       varchar(500) default null comment '備註',
    primary key (config_id)
) engine=innodb auto_increment=100 comment = '參數設定表';

insert into sys_config
values (1, '主框架頁-預設介面樣式名稱', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', sysdate(), '', null,
        '藍色 skin-blue、綠色 skin-green、紫色 skin-purple、紅色 skin-red、黃色 skin-yellow');
insert into sys_config
values (2, '使用者管理-帳號初始密碼', 'sys.user.initPassword', '123456', 'Y', 'admin', sysdate(), '', null,
        '初始化密碼 123456');
insert into sys_config
values (3, '主框架頁-側邊欄主題', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', sysdate(), '', null,
        '深色主題theme-dark，淺色主題theme-light');
insert into sys_config
values (4, '帳號自助-驗證碼開關', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', sysdate(), '', null,
        '是否開啟驗證碼功能（true開啟，false關閉）');
insert into sys_config
values (5, '帳號自助-是否開啟使用者註冊功能', 'sys.account.registerUser', 'false', 'Y', 'admin', sysdate(), '', null,
        '是否開啟註冊使用者功能（true開啟，false關閉）');
insert into sys_config
values (6, '使用者登入-黑名單列表', 'sys.login.blackIPList', '', 'Y', 'admin', sysdate(), '', null,
        '設定登入IP黑名單限制，多個匹配項以;分隔，支援匹配（*通配、網段）');
insert into sys_config
values (7, '使用者管理-初始密碼修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', sysdate(), '', null,
        '0：初始密碼修改策略關閉，沒有任何提示，1：提醒使用者，如果未修改初始密碼，則在登入時就會提醒修改密碼對話方塊');
insert into sys_config
values (8, '使用者管理-帳號密碼更新週期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', sysdate(), '', null,
        '密碼更新週期（填寫數位，資料初始化值為0不限制，若修改必須為大於0小於365的正整數），如果超過這個週期登入系統時，則在登入時就會提醒修改密碼對話方塊');


-- ----------------------------
-- 14、系統訪問記錄
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor
(
    info_id        bigint(20)     not null auto_increment   comment '訪問ID',
    user_name      varchar(50)  default '' comment '使用者帳號',
    ipaddr         varchar(128) default '' comment '登入IP位置',
    login_location varchar(255) default '' comment '登入地點',
    browser        varchar(50)  default '' comment '瀏覽器類型',
    os             varchar(50)  default '' comment '作業系統',
    status         char(1)      default '0' comment '登入狀態（0成功 1失敗）',
    msg            varchar(255) default '' comment '提示訊息',
    login_time     datetime comment '登入時間',
    primary key (info_id),
    key            idx_sys_logininfor_s (status),
    key            idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系統訪問記錄';


-- ----------------------------
-- 15、定時任務呼叫表
-- ----------------------------
drop table if exists sys_job;
create table sys_job
(
    job_id          bigint(20)    not null auto_increment    comment '任務ID',
    job_name        varchar(64)  default '' comment '任務名稱',
    job_group       varchar(64)  default 'DEFAULT' comment '任務組名',
    invoke_target   varchar(500) not null comment '呼叫目標字串',
    cron_expression varchar(255) default '' comment 'Cron執行運算式',
    misfire_policy  varchar(20)  default '3' comment '計畫執行錯誤策略（1立即執行 2執行一次 3放棄執行）',
    concurrent      char(1)      default '1' comment '是否併發執行（0允許 1禁止）',
    status          char(1)      default '0' comment '狀態（0正常 1暫停）',
    create_by       varchar(64)  default '' comment '建立者',
    create_time     datetime comment '建立時間',
    update_by       varchar(64)  default '' comment '更新者',
    update_time     datetime comment '更新時間',
    remark          varchar(500) default '' comment '備註訊息',
    primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定時任務呼叫表';

insert into sys_job
values (1, '系統預設（無參數）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '',
        null, '');
insert into sys_job
values (2, '系統預設（有參數）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin',
        sysdate(),
        '', null, '');
insert into sys_job
values (3, '系統預設（多參數）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?',
        '3', '1', '1', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 16、定時任務呼叫日誌表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log
(
    job_log_id     bigint(20)     not null auto_increment    comment '任務日誌ID',
    job_name       varchar(64)  not null comment '任務名稱',
    job_group      varchar(64)  not null comment '任務組名',
    invoke_target  varchar(500) not null comment '呼叫目標字串',
    job_message    varchar(500) comment '日誌資訊',
    status         char(1)       default '0' comment '執行狀態（0正常 1失敗）',
    exception_info varchar(2000) default '' comment '異常資訊',
    create_time    datetime comment '建立時間',
    primary key (job_log_id)
) engine=innodb comment = '定時任務呼叫日誌表';


-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice
(
    notice_id      int(4)          not null auto_increment    comment '公告ID',
    notice_title   varchar(50) not null comment '公告標題',
    notice_type    char(1)     not null comment '公告類型（1通知 2公告）',
    notice_content longblob     default null comment '公告內容',
    status         char(1)      default '0' comment '公告狀態（0正常 1關閉）',
    create_by      varchar(64)  default '' comment '建立者',
    create_time    datetime comment '建立時間',
    update_by      varchar(64)  default '' comment '更新者',
    update_time    datetime comment '更新時間',
    remark         varchar(255) default null comment '備註',
    primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 初始化-公告資訊表資料
-- ----------------------------
insert into sys_notice
values ('1', '溫馨提醒：2018-07-01 CoolApps新版本發佈啦', '2', '新版本內容', '0', 'admin', sysdate(), '', null,
        '管理員');
insert into sys_notice
values ('2', '維護通知：2018-07-01 CoolApps系統淩晨維護', '1', '維護內容', '0', 'admin', sysdate(), '', null, '管理員');


-- ----------------------------
-- 18、程式碼產生業務表
-- ----------------------------
drop table if exists gen_table;
create table gen_table
(
    table_id          bigint(20)      not null auto_increment    comment '編號',
    table_name        varchar(200) default '' comment '表名稱',
    table_comment     varchar(500) default '' comment '表描述',
    sub_table_name    varchar(64)  default null comment '關聯子表的表名',
    sub_table_fk_name varchar(64)  default null comment '子表關聯的外鍵名',
    class_name        varchar(100) default '' comment '實體類名稱',
    tpl_category      varchar(200) default 'crud' comment '使用的範本（crud單表操作 tree樹表操作）',
    tpl_web_type      varchar(30)  default '' comment '前端範本類型（element-ui模版 element-plus模版）',
    package_name      varchar(100) comment '產生包路徑',
    module_name       varchar(30) comment '產生模組名',
    business_name     varchar(30) comment '產生業務名',
    function_name     varchar(50) comment '產生功能名',
    function_author   varchar(50) comment '產生功能作者',
    gen_type          char(1)      default '0' comment '產生程式碼方式（0zip壓縮包 1自訂路徑）',
    gen_path          varchar(200) default '/' comment '產生路徑（不填預設專案路徑）',
    options           varchar(1000) comment '其它產生選項',
    create_by         varchar(64)  default '' comment '建立者',
    create_time       datetime comment '建立時間',
    update_by         varchar(64)  default '' comment '更新者',
    update_time       datetime comment '更新時間',
    remark            varchar(500) default null comment '備註',
    primary key (table_id)
) engine=innodb auto_increment=1 comment = '程式碼產生業務表';


-- ----------------------------
-- 19、程式碼產生業務表欄位
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column
(
    column_id      bigint(20)      not null auto_increment    comment '編號',
    table_id       bigint(20)                                 comment '歸屬表編號',
    column_name    varchar(200) comment '欄位名稱',
    column_comment varchar(500) comment '欄位描述',
    column_type    varchar(100) comment '欄位類型',
    java_type      varchar(500) comment 'JAVA類型',
    java_field     varchar(200) comment 'JAVA欄位名',
    is_pk          char(1) comment '是否主鍵（1是）',
    is_increment   char(1) comment '是否自增（1是）',
    is_required    char(1) comment '是否必填（1是）',
    is_insert      char(1) comment '是否為新增欄位（1是）',
    is_edit        char(1) comment '是否編輯欄位（1是）',
    is_list        char(1) comment '是否列表欄位（1是）',
    is_query       char(1) comment '是否查詢欄位（1是）',
    query_type     varchar(200) default 'EQ' comment '查詢方式（等於、不等於、大於、小於、範圍）',
    html_type      varchar(200) comment '顯示類型（文字方塊、文字域、下拉清單、核取方塊、單選框、日期控制項）',
    dict_type      varchar(200) default '' comment '字典類型',
    sort           int comment '排序',
    create_by      varchar(64)  default '' comment '建立者',
    create_time    datetime comment '建立時間',
    update_by      varchar(64)  default '' comment '更新者',
    update_time    datetime comment '更新時間',
    primary key (column_id)
) engine=innodb auto_increment=1 comment = '程式碼產生業務表欄位';

