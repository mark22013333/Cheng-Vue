-- ============================
-- 庫存管理系統 - 初始資料
-- 版本：V2
-- 說明：初始化物品分類和範例物品資料
-- ============================

-- ----------------------------
-- 初始化物品分類資料
-- ----------------------------
INSERT INTO inv_category (category_id, parent_id, category_name, category_code, sort_order, status, del_flag, create_by, create_time, remark)
VALUES (1000, 0, '書籍', 'BOOK', 1, '0', '0', 'admin', NOW(), '書籍類，掃碼自動分類'),
       (1001, 0, '辦公用品', 'OFFICE', 2, '0', '0', 'admin', NOW(), '辦公室日常用品'),
       (1002, 1001, '文具用品', 'STATIONERY', 1, '0', '0', 'admin', NOW(), '筆、紙張等文具'),
       (1003, 1001, '辦公設備', 'EQUIPMENT', 2, '0', '0', 'admin', NOW(), '印表機、掃描器等設備'),
       (2000, 0, '電子產品', 'ELECTRONICS', 3, '0', '0', 'admin', NOW(), '電子設備和配件'),
       (2001, 2000, '電腦設備', 'COMPUTER', 1, '0', '0', 'admin', NOW(), '電腦、筆電、平板等'),
       (2002, 2000, '網路設備', 'NETWORK', 2, '0', '0', 'admin', NOW(), '路由器、交換器等'),
       (3000, 0, '家具用品', 'FURNITURE', 4, '0', '0', 'admin', NOW(), '辦公家具'),
       (3001, 3000, '桌椅', 'DESK_CHAIR', 1, '0', '0', 'admin', NOW(), '辦公桌椅'),
       (3002, 3000, '收納用品', 'STORAGE', 2, '0', '0', 'admin', NOW(), '櫃子、收納盒等');

-- ----------------------------
-- 初始化範例物品資料
-- ----------------------------
INSERT INTO inv_item (item_id, item_code, item_name, category_id, barcode, qr_code, specification, unit, brand, model, purchase_price, current_price, supplier, min_stock, max_stock, location, description, image_url, status, del_flag, create_by, create_time, remark)
VALUES (10001, 'ITEM001', '原子筆-黑色', 1002, '4710123456789', '', '0.7mm', '支', 'Pilot', 'BPS-GP', 15.00, 15.00,
        'ABC文具行', 50, 200, 'A區-1層-1號', '黑色原子筆，書寫流暢', '', '0', '0', 'admin', NOW(),
        '常用辦公文具'),
       (10002, 'ITEM002', 'A4影印紙', 1002, '4710123456790', '', '80磅', '包', 'Double A', 'A4-80', 120.00, 120.00,
        'XYZ紙業', 10, 50, 'A區-2層-1號', 'A4白色影印紙，500張/包', '', '0', '0', 'admin', NOW(),
        '辦公室必需品'),
       (10003, 'ITEM003', '雷射印表機', 1003, '4710123456791', '', 'A4黑白雷射', '台', 'HP', 'LaserJet Pro M404n',
        8500.00, 8500.00, '科技通路商', 1, 5, 'B區設備室', 'A4黑白雷射印表機，網路列印', '', '0', '0', 'admin', NOW(),
        '辦公設備'),
       (10004, 'ITEM004', '筆記型電腦', 2001, '4710123456792', '', '15.6吋', '台', 'ASUS', 'VivoBook 15', 25000.00,
        25000.00, '電腦專賣店', 2, 10, 'C區設備室', 'Intel i5處理器，8GB記憶體，256GB SSD', '', '0', '0', 'admin', NOW(),
        '員工使用設備'),
       (10005, 'ITEM005', '辦公椅', 3001, '4710123456793', '', '人體工學', '張', 'Herman Miller', 'Aeron', 35000.00,
        35000.00, '辦公家具行', 5, 20, 'D區倉庫', '人體工學辦公椅，可調節高度', '', '0', '0', 'admin', NOW(),
        '高級辦公椅');

-- ----------------------------
-- 初始化庫存資料
-- ----------------------------
INSERT INTO inv_stock (stock_id, item_id, total_quantity, available_qty, borrowed_qty, reserved_qty, damaged_qty, last_in_time, last_out_time, update_time)
VALUES (1, 10001, 150, 150, 0, 0, 0, NOW(), NULL, NOW()),
       (2, 10002, 30, 30, 0, 0, 0, NOW(), NULL, NOW()),
       (3, 10003, 3, 2, 1, 0, 0, NOW(), NOW(), NOW()),
       (4, 10004, 8, 6, 2, 0, 0, NOW(), NOW(), NOW()),
       (5, 10005, 15, 12, 3, 0, 0, NOW(), NOW(), NOW());
