-- ============================
-- 庫存管理系統 - 資料表結構初始化
-- 版本：V1
-- 說明：建立庫存管理所需的所有資料表
-- ============================

-- ----------------------------
-- 1、物品分類表
-- ----------------------------
CREATE TABLE IF NOT EXISTS inv_category
(
    category_id   BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '分類ID',
    parent_id     BIGINT(20)      DEFAULT 0                  COMMENT '父分類ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分類名稱',
    category_code VARCHAR(30)  DEFAULT '' COMMENT '分類編碼',
    sort_order    INT(4)          DEFAULT 0                  COMMENT '排序',
    status        CHAR(1)      DEFAULT '0' COMMENT '狀態（0正常 1停用）',
    del_flag      CHAR(1)      DEFAULT '0' COMMENT '刪除標誌（0存在 2刪除）',
    create_by     VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time   DATETIME COMMENT '建立時間',
    update_by     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time   DATETIME COMMENT '更新時間',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (category_id)
) ENGINE=INNODB AUTO_INCREMENT=1000 COMMENT = '物品分類表';

-- ----------------------------
-- 2、物品資訊表
-- ----------------------------
CREATE TABLE IF NOT EXISTS inv_item
(
    item_id        BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '物品ID',
    item_code      VARCHAR(50)  NOT NULL COMMENT '物品編碼',
    item_name      VARCHAR(100) NOT NULL COMMENT '物品名稱',
    category_id    BIGINT(20)      NOT NULL                   COMMENT '分類ID',
    barcode        VARCHAR(100)   DEFAULT '' COMMENT '條碼',
    qr_code        VARCHAR(200)   DEFAULT '' COMMENT 'QR碼',
    specification  VARCHAR(200)   DEFAULT '' COMMENT '規格',
    unit           VARCHAR(20)    DEFAULT '個' COMMENT '單位',
    brand          VARCHAR(50)    DEFAULT '' COMMENT '品牌',
    model          VARCHAR(50)    DEFAULT '' COMMENT '型號',
    purchase_price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '採購價格',
    current_price  DECIMAL(10, 2) DEFAULT 0.00 COMMENT '現價',
    supplier       VARCHAR(100)   DEFAULT '' COMMENT '供應商',
    min_stock      INT(11)         DEFAULT 0                  COMMENT '最低庫存',
    max_stock      INT(11)         DEFAULT 0                  COMMENT '最高庫存',
    location       VARCHAR(100)   DEFAULT '' COMMENT '存放位置',
    description    TEXT COMMENT '描述',
    image_url      VARCHAR(200)   DEFAULT '' COMMENT '圖片路徑',
    status         CHAR(1)        DEFAULT '0' COMMENT '狀態（0正常 1停用）',
    del_flag       CHAR(1)        DEFAULT '0' COMMENT '刪除標誌（0存在 2刪除）',
    create_by      VARCHAR(64)    DEFAULT '' COMMENT '建立者',
    create_time    DATETIME COMMENT '建立時間',
    update_by      VARCHAR(64)    DEFAULT '' COMMENT '更新者',
    update_time    DATETIME COMMENT '更新時間',
    remark         VARCHAR(500)   DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (item_id),
    UNIQUE KEY uk_item_code (item_code),
    KEY            idx_category_id (category_id),
    KEY            idx_barcode (barcode),
    KEY            idx_qr_code (qr_code)
) ENGINE=INNODB AUTO_INCREMENT=10000 COMMENT = '物品資訊表';

-- ----------------------------
-- 3、庫存表
-- ----------------------------
CREATE TABLE IF NOT EXISTS inv_stock
(
    stock_id       BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '庫存ID',
    item_id        BIGINT(20)      NOT NULL                   COMMENT '物品ID',
    total_quantity INT(11)         DEFAULT 0                  COMMENT '總數量',
    available_qty  INT(11)         DEFAULT 0                  COMMENT '可用數量',
    borrowed_qty   INT(11)         DEFAULT 0                  COMMENT '借出數量',
    reserved_qty   INT(11)         DEFAULT 0                  COMMENT '預留數量',
    damaged_qty    INT(11)         DEFAULT 0                  COMMENT '損壞數量',
    last_in_time   DATETIME COMMENT '最後入庫時間',
    last_out_time  DATETIME COMMENT '最後出庫時間',
    update_time    DATETIME COMMENT '更新時間',
    PRIMARY KEY (stock_id),
    UNIQUE KEY uk_item_id (item_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '庫存表';

-- ----------------------------
-- 4、庫存異動記錄表
-- ----------------------------
CREATE TABLE IF NOT EXISTS inv_stock_record
(
    record_id     BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '記錄ID',
    item_id       BIGINT(20)      NOT NULL                   COMMENT '物品ID',
    record_type   CHAR(1)  NOT NULL COMMENT '異動類型（1入庫 2出庫 3借出 4歸還 5盤點 6損壞 7遺失）',
    quantity      INT(11)         NOT NULL                   COMMENT '數量（正數入庫，負數出庫）',
    before_qty    INT(11)         DEFAULT 0                  COMMENT '異動前數量',
    after_qty     INT(11)         DEFAULT 0                  COMMENT '異動後數量',
    unit_price    DECIMAL(10, 2) DEFAULT 0.00 COMMENT '單價',
    total_amount  DECIMAL(12, 2) DEFAULT 0.00 COMMENT '總金額',
    operator_id   BIGINT(20)                                 COMMENT '操作人員ID',
    operator_name VARCHAR(50)    DEFAULT '' COMMENT '操作人員姓名',
    dept_id       BIGINT(20)                                 COMMENT '部門ID',
    record_time   DATETIME NOT NULL COMMENT '異動時間',
    reason        VARCHAR(200)   DEFAULT '' COMMENT '異動原因',
    remark        VARCHAR(500)   DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (record_id),
    KEY           idx_item_id (item_id),
    KEY           idx_record_type (record_type),
    KEY           idx_record_time (record_time),
    KEY           idx_operator_id (operator_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '庫存異動記錄表';

-- ----------------------------
-- 5、借出記錄表
-- ----------------------------
CREATE TABLE IF NOT EXISTS inv_borrow
(
    borrow_id       BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '借出ID',
    borrow_no       VARCHAR(50) NOT NULL COMMENT '借出單號',
    item_id         BIGINT(20)      NOT NULL                   COMMENT '物品ID',
    borrower_id     BIGINT(20)      NOT NULL                   COMMENT '借出人ID',
    borrower_name   VARCHAR(50) NOT NULL COMMENT '借出人姓名',
    borrower_dept   BIGINT(20)                                 COMMENT '借出人部門ID',
    quantity        INT(11)         NOT NULL                   COMMENT '借出數量',
    borrow_time     DATETIME    NOT NULL COMMENT '借出時間',
    expected_return DATETIME COMMENT '預計歸還時間',
    actual_return   DATETIME COMMENT '實際歸還時間',
    return_quantity INT(11)         DEFAULT 0                  COMMENT '已歸還數量',
    status          CHAR(1)      DEFAULT '0' COMMENT '狀態（0待審核 1已借出 2審核拒絕 3已歸還 4部分歸還 5逾期）',
    approver_id     BIGINT(20)                                 COMMENT '審核人ID',
    approver_name   VARCHAR(50)  DEFAULT '' COMMENT '審核人姓名',
    approve_time    DATETIME COMMENT '審核時間',
    purpose         VARCHAR(200) DEFAULT '' COMMENT '借用目的',
    create_by       VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time     DATETIME COMMENT '建立時間',
    update_by       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time     DATETIME COMMENT '更新時間',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (borrow_id),
    UNIQUE KEY uk_borrow_no (borrow_no),
    KEY             idx_item_id (item_id),
    KEY             idx_borrower_id (borrower_id),
    KEY             idx_status (status),
    KEY             idx_borrow_time (borrow_time)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '借出記錄表';

-- ----------------------------
-- 6、歸還記錄表
-- ----------------------------
CREATE TABLE IF NOT EXISTS inv_return
(
    return_id            BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '歸還ID',
    borrow_id            BIGINT(20)      NOT NULL                   COMMENT '借出記錄ID',
    borrow_code          VARCHAR(50) NOT NULL COMMENT '借出單號',
    item_id              BIGINT(20)      NOT NULL                   COMMENT '物品ID',
    borrower_id          BIGINT(20)      NOT NULL                   COMMENT '借用人ID',
    borrower_name        VARCHAR(50) NOT NULL COMMENT '借用人姓名',
    return_quantity      INT(11)         NOT NULL                   COMMENT '歸還數量',
    return_time          DATETIME    NOT NULL COMMENT '歸還時間',
    expected_return      DATETIME COMMENT '預計歸還時間',
    is_overdue           CHAR(1)      DEFAULT '0' COMMENT '是否逾期（0否 1是）',
    overdue_days         BIGINT(20)      DEFAULT 0                  COMMENT '逾期天數',
    item_condition       VARCHAR(50)  DEFAULT NULL COMMENT '物品狀態（good完好 damaged損壞 lost遺失）',
    damage_description   VARCHAR(200) DEFAULT '' COMMENT '損壞描述',
    compensation_amount  DECIMAL(10,2) DEFAULT NULL COMMENT '賠償金額',
    receiver_id          BIGINT(20)                                 COMMENT '接收人ID',
    receiver_name        VARCHAR(50)  DEFAULT '' COMMENT '接收人姓名',
    return_status        CHAR(1)      DEFAULT '1' COMMENT '歸還狀態（0待確認 1已確認 2有異議）',
    create_by            VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time          DATETIME COMMENT '建立時間',
    update_by            VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time          DATETIME COMMENT '更新時間',
    remark               VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (return_id),
    KEY                  idx_borrow_id (borrow_id),
    KEY                  idx_borrow_code (borrow_code),
    KEY                  idx_item_id (item_id),
    KEY                  idx_borrower_id (borrower_id),
    KEY                  idx_return_time (return_time)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '歸還記錄表';

-- ----------------------------
-- 7、掃描記錄表
-- ----------------------------
CREATE TABLE IF NOT EXISTS inv_scan_log
(
    scan_id       BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '掃描ID',
    scan_type     CHAR(1)      NOT NULL COMMENT '掃描類型（1條碼 2QR碼）',
    scan_code     VARCHAR(200) NOT NULL COMMENT '掃描內容',
    item_id       BIGINT(20)                                 COMMENT '物品ID（如果識別成功）',
    scan_result   CHAR(1)      DEFAULT '0' COMMENT '掃描結果（0成功 1失敗）',
    operator_id   BIGINT(20)      NOT NULL                   COMMENT '操作人員ID',
    operator_name VARCHAR(50)  DEFAULT '' COMMENT '操作人員姓名',
    scan_time     DATETIME     NOT NULL COMMENT '掃描時間',
    ip_address    VARCHAR(50)  DEFAULT '' COMMENT 'IP地址',
    user_agent    VARCHAR(200) DEFAULT '' COMMENT '用戶代理',
    error_msg     VARCHAR(200) DEFAULT '' COMMENT '錯誤訊息',
    PRIMARY KEY (scan_id),
    KEY           idx_scan_code (scan_code),
    KEY           idx_item_id (item_id),
    KEY           idx_operator_id (operator_id),
    KEY           idx_scan_time (scan_time)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '掃描記錄表';
