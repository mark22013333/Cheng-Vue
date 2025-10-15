-- ============================
-- 庫存管理系統資料庫結構設計
-- ============================

-- ----------------------------
-- 1、物品分類表
-- ----------------------------
DROP TABLE IF EXISTS inv_category;
CREATE TABLE inv_category
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
DROP TABLE IF EXISTS inv_item;
CREATE TABLE inv_item
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
DROP TABLE IF EXISTS inv_stock;
CREATE TABLE inv_stock
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
DROP TABLE IF EXISTS inv_stock_record;
CREATE TABLE inv_stock_record
(
    record_id     BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '記錄ID',
    item_id       BIGINT(20)      NOT NULL                   COMMENT '物品ID',
    record_type   CHAR(1)  NOT NULL COMMENT '異動類型（1入庫 2出庫 3借出 4歸還 5盤點 6損壞）',
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
DROP TABLE IF EXISTS inv_borrow;
CREATE TABLE inv_borrow
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
    status          CHAR(1)      DEFAULT '0' COMMENT '狀態（0借出中 1已歸還 2逾期 3部分歸還）',
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
DROP TABLE IF EXISTS inv_return;
CREATE TABLE inv_return
(
    return_id      BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '歸還ID',
    borrow_id      BIGINT(20)      NOT NULL                   COMMENT '借出記錄ID',
    return_no      VARCHAR(50) NOT NULL COMMENT '歸還單號',
    item_id        BIGINT(20)      NOT NULL                   COMMENT '物品ID',
    returner_id    BIGINT(20)      NOT NULL                   COMMENT '歸還人ID',
    returner_name  VARCHAR(50) NOT NULL COMMENT '歸還人姓名',
    quantity       INT(11)         NOT NULL                   COMMENT '歸還數量',
    return_time    DATETIME    NOT NULL COMMENT '歸還時間',
    condition_desc VARCHAR(200) DEFAULT '' COMMENT '物品狀況描述',
    is_damaged     CHAR(1)      DEFAULT '0' COMMENT '是否損壞（0正常 1損壞）',
    damage_desc    VARCHAR(200) DEFAULT '' COMMENT '損壞描述',
    receiver_id    BIGINT(20)                                 COMMENT '接收人ID',
    receiver_name  VARCHAR(50)  DEFAULT '' COMMENT '接收人姓名',
    create_by      VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time    DATETIME COMMENT '建立時間',
    remark         VARCHAR(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (return_id),
    UNIQUE KEY uk_return_no (return_no),
    KEY            idx_borrow_id (borrow_id),
    KEY            idx_item_id (item_id),
    KEY            idx_returner_id (returner_id),
    KEY            idx_return_time (return_time)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '歸還記錄表';

-- ----------------------------
-- 7、掃描記錄表
-- ----------------------------
DROP TABLE IF EXISTS inv_scan_log;
CREATE TABLE inv_scan_log
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

-- ----------------------------
-- 初始化物品分類資料
-- ----------------------------
INSERT INTO inv_category
VALUES (1000, 0, '書籍', 'BOOK', 1, '0', '0', 'admin', NOW(), '', NULL, '書籍類，掃碼自動分類'),
       (1001, 0, '辦公用品', 'OFFICE', 2, '0', '0', 'admin', NOW(), '', NULL, '辦公室日常用品'),
       (1002, 1001, '文具用品', 'STATIONERY', 1, '0', '0', 'admin', NOW(), '', NULL, '筆、紙張等文具'),
       (1003, 1001, '辦公設備', 'EQUIPMENT', 2, '0', '0', 'admin', NOW(), '', NULL, '印表機、掃描器等設備'),
       (2000, 0, '電子產品', 'ELECTRONICS', 3, '0', '0', 'admin', NOW(), '', NULL, '電子設備和配件'),
       (2001, 2000, '電腦設備', 'COMPUTER', 1, '0', '0', 'admin', NOW(), '', NULL, '電腦、筆電、平板等'),
       (2002, 2000, '網路設備', 'NETWORK', 2, '0', '0', 'admin', NOW(), '', NULL, '路由器、交換器等'),
       (3000, 0, '家具用品', 'FURNITURE', 4, '0', '0', 'admin', NOW(), '', NULL, '辦公家具'),
       (3001, 3000, '桌椅', 'DESK_CHAIR', 1, '0', '0', 'admin', NOW(), '', NULL, '辦公桌椅'),
       (3002, 3000, '收納用品', 'STORAGE', 2, '0', '0', 'admin', NOW(), '', NULL, '櫃子、收納盒等');

-- ----------------------------
-- 初始化範例物品資料
-- ----------------------------
INSERT INTO inv_item
VALUES (10001, 'ITEM001', '原子筆-黑色', 1002, '4710123456789', '', '0.7mm', '支', 'Pilot', 'BPS-GP', 15.00, 15.00,
        'ABC文具行', 50, 200, 'A區-1層-1號', '黑色原子筆，書寫流暢', '', '0', '0', 'admin', NOW(), '', NULL,
        '常用辦公文具'),
       (10002, 'ITEM002', 'A4影印紙', 1002, '4710123456790', '', '80磅', '包', 'Double A', 'A4-80', 120.00, 120.00,
        'XYZ紙業', 10, 50, 'A區-2層-1號', 'A4白色影印紙，500張/包', '', '0', '0', 'admin', NOW(), '', NULL,
        '辦公室必需品'),
       (10003, 'ITEM003', '雷射印表機', 1003, '4710123456791', '', 'A4黑白雷射', '台', 'HP', 'LaserJet Pro M404n',
        8500.00, 8500.00, '科技通路商', 1, 5, 'B區設備室', 'A4黑白雷射印表機，網路列印', '', '0', '0', 'admin', NOW(),
        '', NULL, '辦公設備'),
       (10004, 'ITEM004', '筆記型電腦', 2001, '4710123456792', '', '15.6吋', '台', 'ASUS', 'VivoBook 15', 25000.00,
        25000.00, '電腦專賣店', 2, 10, 'C區設備室', 'Intel i5處理器，8GB記憶體，256GB SSD', '', '0', '0', 'admin', NOW(),
        '', NULL, '員工使用設備'),
       (10005, 'ITEM005', '辦公椅', 3001, '4710123456793', '', '人體工學', '張', 'Herman Miller', 'Aeron', 35000.00,
        35000.00, '辦公家具行', 5, 20, 'D區倉庫', '人體工學辦公椅，可調節高度', '', '0', '0', 'admin', NOW(), '', NULL,
        '高級辦公椅');

-- ----------------------------
-- 初始化庫存資料
-- ----------------------------
INSERT INTO inv_stock
VALUES (1, 10001, 150, 150, 0, 0, 0, NOW(), NULL, NOW()),
       (2, 10002, 30, 30, 0, 0, 0, NOW(), NULL, NOW()),
       (3, 10003, 3, 2, 1, 0, 0, NOW(), NOW(), NOW()),
       (4, 10004, 8, 6, 2, 0, 0, NOW(), NOW(), NOW()),
       (5, 10005, 15, 12, 3, 0, 0, NOW(), NOW(), NOW());
