DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

-- ----------------------------
-- 1、儲存每一個已設定的 jobDetail 的詳細資訊
-- ----------------------------
create table QRTZ_JOB_DETAILS
(
    sched_name        varchar(120) not null comment '調動名稱',
    job_name          varchar(200) not null comment '任務名稱',
    job_group         varchar(200) not null comment '任務組名',
    description       varchar(250) null                comment '相關介紹',
    job_class_name    varchar(250) not null comment '執行任務類名稱',
    is_durable        varchar(1)   not null comment '是否持久化',
    is_nonconcurrent  varchar(1)   not null comment '是否併發',
    is_update_data    varchar(1)   not null comment '是否更新資料',
    requests_recovery varchar(1)   not null comment '是否接受恢復執行',
    job_data          blob null                comment '存放持久化job物件',
    primary key (sched_name, job_name, job_group)
) engine=innodb comment = '任務詳細資訊表';

-- ----------------------------
-- 2、 儲存已設定的 Trigger 的資訊
-- ----------------------------
create table QRTZ_TRIGGERS
(
    sched_name     varchar(120) not null comment '調動名稱',
    trigger_name   varchar(200) not null comment '觸發器的名字',
    trigger_group  varchar(200) not null comment '觸發器所屬組的名字',
    job_name       varchar(200) not null comment 'qrtz_job_details表job_name的外鍵',
    job_group      varchar(200) not null comment 'qrtz_job_details表job_group的外鍵',
    description    varchar(250) null                comment '相關介紹',
    next_fire_time bigint(13)      null                comment '上一次觸發時間（毫秒）',
    prev_fire_time bigint(13)      null                comment '下一次觸發時間（預設為-1表示不觸發）',
    priority       integer null                comment '優先順序',
    trigger_state  varchar(16)  not null comment '觸發器狀態',
    trigger_type   varchar(8)   not null comment '觸發器的類型',
    start_time     bigint(13)      not null            comment '開始時間',
    end_time       bigint(13)      null                comment '結束時間',
    calendar_name  varchar(200) null                comment '日程表名稱',
    misfire_instr  smallint(2)     null                comment '補償執行的策略',
    job_data       blob null                comment '存放持久化job物件',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS (sched_name, job_name, job_group)
) engine=innodb comment = '觸發器詳細資訊表';

-- ----------------------------
-- 3、 儲存簡單的 Trigger，包括重複次數，間隔，以及已觸發的次數
-- ----------------------------
create table QRTZ_SIMPLE_TRIGGERS
(
    sched_name      varchar(120) not null comment '調動名稱',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外鍵',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外鍵',
    repeat_count    bigint(7)       not null            comment '重複的次數統計',
    repeat_interval bigint(12)      not null            comment '重複的間隔時間',
    times_triggered bigint(10)      not null            comment '已經觸發的次數',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
) engine=innodb comment = '簡單觸發器的資訊表';

-- ----------------------------
-- 4、 儲存 Cron Trigger，包括 Cron 運算式和時區資訊
-- ----------------------------
create table QRTZ_CRON_TRIGGERS
(
    sched_name      varchar(120) not null comment '調動名稱',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外鍵',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外鍵',
    cron_expression varchar(200) not null comment 'cron運算式',
    time_zone_id    varchar(80) comment '時區',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Cron類型的觸發器表';

-- ----------------------------
-- 5、 Trigger 作為 Blob 類型儲存(用於 Quartz 使用者用 JDBC 建立他們自己定製的 Trigger 類型，JobStore 並不知道如何儲存實體的時候)
-- ----------------------------
create table QRTZ_BLOB_TRIGGERS
(
    sched_name    varchar(120) not null comment '調動名稱',
    trigger_name  varchar(200) not null comment 'qrtz_triggers表trigger_name的外鍵',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外鍵',
    blob_data     blob null                comment '存放持久化Trigger物件',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Blob類型的觸發器表';

-- ----------------------------
-- 6、 以 Blob 類型儲存存放日曆資訊， quartz可設定一個日歷來指定一個時間範圍
-- ----------------------------
create table QRTZ_CALENDARS
(
    sched_name    varchar(120) not null comment '調動名稱',
    calendar_name varchar(200) not null comment '日曆名稱',
    calendar      blob         not null comment '存放持久化calendar物件',
    primary key (sched_name, calendar_name)
) engine=innodb comment = '日曆訊息表';

-- ----------------------------
-- 7、 儲存已暫停的 Trigger 組的資訊
-- ----------------------------
create table QRTZ_PAUSED_TRIGGER_GRPS
(
    sched_name    varchar(120) not null comment '調動名稱',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外鍵',
    primary key (sched_name, trigger_group)
) engine=innodb comment = '暫停的觸發器表';

-- ----------------------------
-- 8、 儲存與已觸發的 Trigger 相關的狀態資訊，以及相聯 Job 的執行資訊
-- ----------------------------
create table QRTZ_FIRED_TRIGGERS
(
    sched_name        varchar(120) not null comment '調動名稱',
    entry_id          varchar(95)  not null comment '調動器實體id',
    trigger_name      varchar(200) not null comment 'qrtz_triggers表trigger_name的外鍵',
    trigger_group     varchar(200) not null comment 'qrtz_triggers表trigger_group的外鍵',
    instance_name     varchar(200) not null comment '調動器實體名',
    fired_time        bigint(13)      not null            comment '觸發的時間',
    sched_time bigint(13)      not null            comment '計時器製定的時間',
    priority          integer      not null comment '優先順序',
    state             varchar(16)  not null comment '狀態',
    job_name          varchar(200) null                comment '任務名稱',
    job_group         varchar(200) null                comment '任務組名',
    is_nonconcurrent  varchar(1) null                comment '是否併發',
    requests_recovery varchar(1) null                comment '是否接受恢復執行',
    primary key (sched_name, entry_id)
) engine=innodb comment = '已觸發的觸發器表';

-- ----------------------------
-- 9、 儲存少量的有關 Scheduler 的狀態資訊，假如是用於集群中，可以看到其他的 Scheduler 實體
-- ----------------------------
create table QRTZ_SCHEDULER_STATE
(
    sched_name        varchar(120) not null comment '調動名稱',
    instance_name     varchar(200) not null comment '實體名稱',
    last_checkin_time bigint(13)      not null            comment '上次檢查時間',
    checkin_interval  bigint(13)      not null            comment '檢查間隔時間',
    primary key (sched_name, instance_name)
) engine=innodb comment = '調動器狀態表';

-- ----------------------------
-- 10、 儲存程式的悲觀鎖的資訊(假如使用了悲觀鎖)
-- ----------------------------
create table QRTZ_LOCKS
(
    sched_name varchar(120) not null comment '調動名稱',
    lock_name  varchar(40)  not null comment '悲觀鎖名稱',
    primary key (sched_name, lock_name)
) engine=innodb comment = '儲存的悲觀鎖資訊表';

-- ----------------------------
-- 11、 Quartz集群實現同步機製的行鎖表
-- ----------------------------
create table QRTZ_SIMPROP_TRIGGERS
(
    sched_name    varchar(120) not null comment '調動名稱',
    trigger_name  varchar(200) not null comment 'qrtz_triggers表trigger_name的外鍵',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外鍵',
    str_prop_1    varchar(512) null                comment 'String類型的trigger的第一個參數',
    str_prop_2    varchar(512) null                comment 'String類型的trigger的第二個參數',
    str_prop_3    varchar(512) null                comment 'String類型的trigger的第三個參數',
    int_prop_1    int null                comment 'int類型的trigger的第一個參數',
    int_prop_2    int null                comment 'int類型的trigger的第二個參數',
    long_prop_1   bigint null                comment 'long類型的trigger的第一個參數',
    long_prop_2   bigint null                comment 'long類型的trigger的第二個參數',
    dec_prop_1    numeric(13, 4) null                comment 'decimal類型的trigger的第一個參數',
    dec_prop_2    numeric(13, 4) null                comment 'decimal類型的trigger的第二個參數',
    bool_prop_1   varchar(1) null                comment 'Boolean類型的trigger的第一個參數',
    bool_prop_2   varchar(1) null                comment 'Boolean類型的trigger的第二個參數',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
) engine=innodb comment = '同步機製的行鎖表';

commit;

