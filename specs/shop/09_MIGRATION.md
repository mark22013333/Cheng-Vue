# 09. Migration 規劃

---

## 9.1 Migration 檔案清單

> 命名規則：`V{版本號}__shop_{描述}.sql`
> 
> ⚠️ 建立新 Migration 前，先檢查當前最高版本號

### 9.1.1 建立順序

| 順序 | 檔案名稱 | 說明 |
|-----|---------|-----|
| 1 | `V{N}__shop_create_category_table.sql` | 建立分類表 |
| 2 | `V{N+1}__shop_create_product_tables.sql` | 建立商品主表和 SKU 表 |
| 3 | `V{N+2}__shop_create_member_tables.sql` | 建立會員表和第三方登入表 |
| 4 | `V{N+3}__shop_create_order_tables.sql` | 建立訂單主表和明細表 |
| 5 | `V{N+4}__shop_create_cart_table.sql` | 建立購物車表 |
| 6 | `V{N+5}__shop_create_content_tables.sql` | 建立輪播和區塊表 |
| 7 | `V{N+6}__shop_create_menu.sql` | 建立後台選單 |
| 8 | `V{N+7}__shop_init_data.sql` | 初始化基礎資料 |

---

## 9.2 Migration 內容

### 9.2.1 V{N}__shop_create_category_table.sql

```sql
-- 商品分類表
CREATE TABLE IF NOT EXISTS shop_category (
    category_id     BIGINT          NOT NULL AUTO_INCREMENT COMMENT '分類ID',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父分類ID（0表示一級分類）',
    name            VARCHAR(50)     NOT NULL COMMENT '分類名稱',
    icon            VARCHAR(200)    DEFAULT NULL COMMENT '分類圖示',
    banner_image    VARCHAR(500)    DEFAULT NULL COMMENT '分類橫幅圖片',
    sort_order      INT             DEFAULT 0 COMMENT '排序（數字越小越前）',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態：ENABLED/DISABLED',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (category_id),
    KEY idx_parent_id (parent_id),
    KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分類表';
```

### 9.2.2 V{N+1}__shop_create_product_tables.sql

```sql
-- 商品主表
CREATE TABLE IF NOT EXISTS shop_product (
    product_id      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    category_id     BIGINT          NOT NULL COMMENT '分類ID',
    title           VARCHAR(200)    NOT NULL COMMENT '商品標題',
    sub_title       VARCHAR(300)    DEFAULT NULL COMMENT '副標題/促銷語',
    main_image      VARCHAR(500)    NOT NULL COMMENT '主圖',
    slider_images   JSON            DEFAULT NULL COMMENT '輪播圖 ["url1","url2"]',
    video_url       VARCHAR(500)    DEFAULT NULL COMMENT '商品影片URL',
    description     TEXT            DEFAULT NULL COMMENT '商品描述（富文本HTML）',
    sales_count     INT             DEFAULT 0 COMMENT '銷量（可虛擬）',
    view_count      INT             DEFAULT 0 COMMENT '瀏覽量',
    is_hot          TINYINT(1)      DEFAULT 0 COMMENT '是否熱門：0否 1是',
    is_new          TINYINT(1)      DEFAULT 0 COMMENT '是否新品：0否 1是',
    is_recommend    TINYINT(1)      DEFAULT 0 COMMENT '是否推薦：0否 1是',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '狀態：DRAFT/PREVIEW/ON_SALE/OFF_SALE',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (product_id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_is_hot (is_hot),
    KEY idx_is_new (is_new),
    KEY idx_is_recommend (is_recommend)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品主表';

-- 商品SKU表
CREATE TABLE IF NOT EXISTS shop_product_sku (
    sku_id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    product_id      BIGINT          NOT NULL COMMENT '商品ID',
    sku_code        VARCHAR(50)     DEFAULT NULL COMMENT 'SKU 編碼',
    sku_name        VARCHAR(100)    NOT NULL COMMENT '規格名稱',
    sku_image       VARCHAR(500)    DEFAULT NULL COMMENT 'SKU 專屬圖片',
    price           DECIMAL(10,2)   NOT NULL COMMENT '銷售價',
    original_price  DECIMAL(10,2)   DEFAULT NULL COMMENT '原價（劃線價）',
    cost_price      DECIMAL(10,2)   DEFAULT NULL COMMENT '成本價',
    inv_item_id     BIGINT          DEFAULT NULL COMMENT '關聯庫存物品ID（關聯 inv_item.item_id）',
    stock_quantity  INT             DEFAULT 0 COMMENT '獨立庫存數量（未關聯 inv_item 時使用）',
    sales_count     INT             DEFAULT 0 COMMENT 'SKU銷量',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態：ENABLED/DISABLED',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (sku_id),
    KEY idx_product_id (product_id),
    KEY idx_inv_item_id (inv_item_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';
```

### 9.2.3 V{N+2}__shop_create_member_tables.sql

```sql
-- 前台會員表
CREATE TABLE IF NOT EXISTS shop_member (
    member_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '會員ID',
    member_no       VARCHAR(32)     NOT NULL COMMENT '會員編號',
    nickname        VARCHAR(50)     DEFAULT NULL COMMENT '暱稱',
    avatar          VARCHAR(500)    DEFAULT NULL COMMENT '頭像URL',
    mobile          VARCHAR(20)     DEFAULT NULL COMMENT '手機號碼',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '電子信箱',
    password        VARCHAR(200)    DEFAULT NULL COMMENT '密碼（加密）',
    gender          VARCHAR(10)     DEFAULT 'UNKNOWN' COMMENT '性別：MALE/FEMALE/UNKNOWN',
    birthday        DATE            DEFAULT NULL COMMENT '生日',
    points          INT             DEFAULT 0 COMMENT '會員點數',
    level           VARCHAR(20)     DEFAULT 'NORMAL' COMMENT '會員等級：NORMAL/VIP/SVIP',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態：ACTIVE/DISABLED/FROZEN',
    last_login_time DATETIME        DEFAULT NULL COMMENT '最後登入時間',
    last_login_ip   VARCHAR(50)     DEFAULT NULL COMMENT '最後登入IP',
    create_time     DATETIME        DEFAULT NULL COMMENT '註冊時間',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_no (member_no),
    UNIQUE KEY uk_mobile (mobile),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前台會員表';

-- 會員第三方登入綁定表
CREATE TABLE IF NOT EXISTS shop_member_social (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'ID',
    member_id       BIGINT          NOT NULL COMMENT '會員ID',
    provider        VARCHAR(20)     NOT NULL COMMENT '第三方平台：LINE/GOOGLE/FACEBOOK',
    provider_id     VARCHAR(100)    NOT NULL COMMENT '第三方用戶唯一ID',
    union_id        VARCHAR(100)    DEFAULT NULL COMMENT 'UnionID（部分平台有）',
    access_token    VARCHAR(500)    DEFAULT NULL COMMENT 'Access Token',
    refresh_token   VARCHAR(500)    DEFAULT NULL COMMENT 'Refresh Token',
    token_expire    DATETIME        DEFAULT NULL COMMENT 'Token過期時間',
    nickname        VARCHAR(50)     DEFAULT NULL COMMENT '第三方暱稱',
    avatar          VARCHAR(500)    DEFAULT NULL COMMENT '第三方頭像',
    extra_data      JSON            DEFAULT NULL COMMENT '額外資料',
    create_time     DATETIME        DEFAULT NULL COMMENT '綁定時間',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_id (provider, provider_id),
    KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員第三方登入綁定表';
```

### 9.2.4 V{N+3}__shop_create_order_tables.sql

```sql
-- 訂單主表
CREATE TABLE IF NOT EXISTS shop_order (
    order_id            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '訂單ID',
    order_no            VARCHAR(32)     NOT NULL COMMENT '訂單編號',
    member_id           BIGINT          NOT NULL COMMENT '會員ID',
    member_nickname     VARCHAR(50)     DEFAULT NULL COMMENT '會員暱稱（冗餘）',
    product_amount      DECIMAL(10,2)   NOT NULL COMMENT '商品金額',
    shipping_amount     DECIMAL(10,2)   DEFAULT 0.00 COMMENT '運費',
    discount_amount     DECIMAL(10,2)   DEFAULT 0.00 COMMENT '折扣金額',
    total_amount        DECIMAL(10,2)   NOT NULL COMMENT '訂單總金額',
    paid_amount         DECIMAL(10,2)   DEFAULT 0.00 COMMENT '已付金額',
    receiver_name       VARCHAR(50)     NOT NULL COMMENT '收件人姓名',
    receiver_mobile     VARCHAR(20)     NOT NULL COMMENT '收件人電話',
    receiver_address    VARCHAR(300)    NOT NULL COMMENT '收件地址',
    receiver_zip        VARCHAR(10)     DEFAULT NULL COMMENT '郵遞區號',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '訂單狀態',
    pay_status          VARCHAR(20)     NOT NULL DEFAULT 'UNPAID' COMMENT '付款狀態',
    ship_status         VARCHAR(20)     NOT NULL DEFAULT 'UNSHIPPED' COMMENT '物流狀態',
    payment_method      VARCHAR(20)     DEFAULT NULL COMMENT '付款方式',
    payment_no          VARCHAR(64)     DEFAULT NULL COMMENT '第三方支付單號',
    paid_time           DATETIME        DEFAULT NULL COMMENT '付款時間',
    shipping_method     VARCHAR(20)     DEFAULT NULL COMMENT '物流方式',
    shipping_no         VARCHAR(64)     DEFAULT NULL COMMENT '物流單號',
    shipped_time        DATETIME        DEFAULT NULL COMMENT '出貨時間',
    received_time       DATETIME        DEFAULT NULL COMMENT '簽收時間',
    buyer_remark        VARCHAR(500)    DEFAULT NULL COMMENT '買家備註',
    seller_remark       VARCHAR(500)    DEFAULT NULL COMMENT '賣家備註',
    cancel_reason       VARCHAR(200)    DEFAULT NULL COMMENT '取消原因',
    cancel_time         DATETIME        DEFAULT NULL COMMENT '取消時間',
    complete_time       DATETIME        DEFAULT NULL COMMENT '完成時間',
    create_time         DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_time         DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_member_id (member_id),
    KEY idx_status (status),
    KEY idx_pay_status (pay_status),
    KEY idx_ship_status (ship_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單主表';

-- 訂單明細表
CREATE TABLE IF NOT EXISTS shop_order_item (
    item_id         BIGINT          NOT NULL AUTO_INCREMENT COMMENT '明細ID',
    order_id        BIGINT          NOT NULL COMMENT '訂單ID',
    product_id      BIGINT          NOT NULL COMMENT '商品ID',
    sku_id          BIGINT          NOT NULL COMMENT 'SKU ID',
    product_name    VARCHAR(200)    NOT NULL COMMENT '商品名稱（冗餘）',
    sku_name        VARCHAR(100)    NOT NULL COMMENT 'SKU名稱（冗餘）',
    sku_image       VARCHAR(500)    DEFAULT NULL COMMENT 'SKU圖片（冗餘）',
    unit_price      DECIMAL(10,2)   NOT NULL COMMENT '單價（下單時）',
    quantity        INT             NOT NULL COMMENT '數量',
    total_price     DECIMAL(10,2)   NOT NULL COMMENT '小計',
    inv_item_id     BIGINT          DEFAULT NULL COMMENT '庫存物品ID（冗餘）',
    inv_item_name   VARCHAR(100)    DEFAULT NULL COMMENT '庫存物品名稱（冗餘）',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    PRIMARY KEY (item_id),
    KEY idx_order_id (order_id),
    KEY idx_product_id (product_id),
    KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單明細表';
```

### 9.2.5 V{N+4}__shop_create_cart_table.sql

```sql
-- 購物車表
CREATE TABLE IF NOT EXISTS shop_cart (
    cart_id         BIGINT          NOT NULL AUTO_INCREMENT COMMENT '購物車ID',
    member_id       BIGINT          NOT NULL COMMENT '會員ID',
    sku_id          BIGINT          NOT NULL COMMENT 'SKU ID',
    quantity        INT             NOT NULL DEFAULT 1 COMMENT '數量',
    is_selected     TINYINT(1)      DEFAULT 1 COMMENT '是否選中：0否 1是',
    create_time     DATETIME        DEFAULT NULL COMMENT '加入時間',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (cart_id),
    UNIQUE KEY uk_member_sku (member_id, sku_id),
    KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='購物車表';
```

### 9.2.6 V{N+5}__shop_create_content_tables.sql

```sql
-- 首頁輪播表
CREATE TABLE IF NOT EXISTS shop_banner (
    banner_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '輪播ID',
    title           VARCHAR(100)    NOT NULL COMMENT '標題',
    image_url       VARCHAR(500)    NOT NULL COMMENT '圖片URL',
    mobile_image    VARCHAR(500)    DEFAULT NULL COMMENT '手機版圖片URL',
    link_type       VARCHAR(20)     DEFAULT 'NONE' COMMENT '連結類型：NONE/PRODUCT/CATEGORY/URL',
    link_value      VARCHAR(200)    DEFAULT NULL COMMENT '連結值',
    position        VARCHAR(20)     NOT NULL DEFAULT 'HOME_TOP' COMMENT '展示位置',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    start_time      DATETIME        DEFAULT NULL COMMENT '開始時間',
    end_time        DATETIME        DEFAULT NULL COMMENT '結束時間',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態：ENABLED/DISABLED',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (banner_id),
    KEY idx_position_status (position, status),
    KEY idx_start_end_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首頁輪播表';

-- 頁面區塊表
CREATE TABLE IF NOT EXISTS shop_page_block (
    block_id        BIGINT          NOT NULL AUTO_INCREMENT COMMENT '區塊ID',
    page_key        VARCHAR(50)     NOT NULL COMMENT '頁面識別：HOME/ABOUT/CONTACT',
    block_key       VARCHAR(50)     NOT NULL COMMENT '區塊識別',
    block_type      VARCHAR(20)     NOT NULL COMMENT '區塊類型：TEXT/IMAGE/HTML/PRODUCT_LIST',
    title           VARCHAR(100)    DEFAULT NULL COMMENT '區塊標題',
    sub_title       VARCHAR(200)    DEFAULT NULL COMMENT '區塊副標題',
    content         TEXT            DEFAULT NULL COMMENT '區塊內容',
    image_url       VARCHAR(500)    DEFAULT NULL COMMENT '區塊圖片',
    extra_config    JSON            DEFAULT NULL COMMENT '額外配置',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態：ENABLED/DISABLED',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (block_id),
    UNIQUE KEY uk_page_block (page_key, block_key),
    KEY idx_page_key (page_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='頁面區塊表';
```

### 9.2.7 V{N+6}__shop_create_menu.sql

```sql
-- 商城管理選單（需根據實際 menu_id 調整）
-- 先查詢當前最大 menu_id
-- SELECT MAX(menu_id) FROM sys_menu;

-- 商城管理（一級選單）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商城管理', 0, 5, 'shop', NULL, NULL, NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', NOW(), '', NULL, '商城管理目錄');

SET @shop_menu_id = LAST_INSERT_ID();

-- 首頁管理（二級選單）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('首頁管理', @shop_menu_id, 1, 'home', NULL, NULL, NULL, 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', NOW(), '', NULL, '首頁管理目錄');

SET @home_menu_id = LAST_INSERT_ID();

-- 輪播管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('輪播管理', @home_menu_id, 1, 'banner', 'shop/banner/index', NULL, 'ShopBanner', 1, 0, 'C', '0', '0', 'shop:banner:list', 'list', 'admin', NOW(), '', NULL, '輪播管理選單');

-- 區塊管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('區塊管理', @home_menu_id, 2, 'block', 'shop/block/index', NULL, 'ShopBlock', 1, 0, 'C', '0', '0', 'shop:block:list', 'component', 'admin', NOW(), '', NULL, '區塊管理選單');

-- 商品管理（二級選單）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品管理', @shop_menu_id, 2, 'product', NULL, NULL, NULL, 1, 0, 'M', '0', '0', '', 'goods', 'admin', NOW(), '', NULL, '商品管理目錄');

SET @product_menu_id = LAST_INSERT_ID();

-- 分類管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('分類管理', @product_menu_id, 1, 'category', 'shop/category/index', NULL, 'ShopCategory', 1, 0, 'C', '0', '0', 'shop:category:list', 'tree', 'admin', NOW(), '', NULL, '分類管理選單');

-- 商品列表
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品列表', @product_menu_id, 2, 'list', 'shop/product/index', NULL, 'ShopProduct', 1, 0, 'C', '0', '0', 'shop:product:list', 'list', 'admin', NOW(), '', NULL, '商品列表選單');

-- 訂單管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('訂單管理', @shop_menu_id, 3, 'order', 'shop/order/index', NULL, 'ShopOrder', 1, 0, 'C', '0', '0', 'shop:order:list', 'documentation', 'admin', NOW(), '', NULL, '訂單管理選單');

-- 會員管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('會員管理', @shop_menu_id, 4, 'member', 'shop/member/index', NULL, 'ShopMember', 1, 0, 'C', '0', '0', 'shop:member:list', 'peoples', 'admin', NOW(), '', NULL, '會員管理選單');
```

### 9.2.8 V{N+7}__shop_init_data.sql

```sql
-- 初始化商品分類
INSERT INTO shop_category (parent_id, name, sort_order, status, create_by, create_time)
VALUES 
    (0, '全部商品', 0, 'ENABLED', 'system', NOW()),
    (0, '熱銷商品', 1, 'ENABLED', 'system', NOW()),
    (0, '新品上市', 2, 'ENABLED', 'system', NOW());

-- 初始化首頁區塊
INSERT INTO shop_page_block (page_key, block_key, block_type, title, content, sort_order, status, create_by, create_time)
VALUES
    ('HOME', 'BRAND_INTRO', 'HTML', '品牌介紹', '<p>歡迎來到我們的商城！</p>', 1, 'ENABLED', 'system', NOW()),
    ('HOME', 'HOT_PRODUCTS', 'PRODUCT_LIST', '熱門商品', NULL, 2, 'ENABLED', 'system', NOW()),
    ('HOME', 'NEW_PRODUCTS', 'PRODUCT_LIST', '新品上架', NULL, 3, 'ENABLED', 'system', NOW()),
    ('ABOUT', 'ABOUT_US', 'HTML', '關於我們', '<p>關於我們的介紹內容...</p>', 1, 'ENABLED', 'system', NOW()),
    ('CONTACT', 'CONTACT_INFO', 'HTML', '聯絡資訊', '<p>聯絡方式...</p>', 1, 'ENABLED', 'system', NOW());
```

---

## 9.3 注意事項

### 9.3.1 建立 Migration 前

```bash
# 先檢查當前最高版本號
ls -la cheng-admin/src/main/resources/db/migration/ | grep "^-" | tail -5
```

### 9.3.2 Migration 命名規則

- **前綴**：`V{版本號}__`（兩個底線）
- **模組標識**：`shop_`
- **描述**：使用底線分隔的英文描述

### 9.3.3 跨環境注意

- 確保 SQL 語法相容 MySQL 8.0
- 使用 `IF NOT EXISTS` 避免重複建立錯誤
- JSON 類型需要 MySQL 5.7+
