# 02. 資料庫設計

## 2.1 ER 關係圖

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  shop_category  │     │  shop_product   │     │ shop_product_sku│
│─────────────────│     │─────────────────│     │─────────────────│
│ category_id PK  │◄────│ category_id FK  │     │ sku_id PK       │
│ parent_id       │     │ product_id PK   │◄────│ product_id FK   │
│ name            │     │ title           │     │ sku_name        │
│ sort_order      │     │ description     │     │ price           │
│ status          │     │ status          │     │ inv_item_id FK  │──┐
└─────────────────┘     └─────────────────┘     └─────────────────┘  │
                                                                      │
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐  │
│   shop_member   │     │ shop_member_    │     │    inv_item     │  │
│─────────────────│     │    social       │     │─────────────────│  │
│ member_id PK    │◄────│ member_id FK    │     │ item_id PK      │◄─┘
│ nickname        │     │ provider        │     │ (現有庫存系統)   │
│ mobile          │     │ provider_id     │     └─────────────────┘
│ points          │     │ access_token    │
└────────┬────────┘     └─────────────────┘
         │
         │         ┌─────────────────┐     ┌─────────────────┐
         │         │   shop_order    │     │ shop_order_item │
         └────────►│─────────────────│     │─────────────────│
                   │ order_id PK     │◄────│ order_id FK     │
                   │ member_id FK    │     │ sku_id (冗餘)   │
                   │ order_no        │     │ product_name    │
                   │ status          │     │ sku_name        │
                   │ total_amount    │     │ unit_price      │
                   └─────────────────┘     └─────────────────┘

┌─────────────────┐     ┌─────────────────┐
│  shop_banner    │     │ shop_page_block │
│─────────────────│     │─────────────────│
│ banner_id PK    │     │ block_id PK     │
│ title           │     │ page_key        │
│ image_url       │     │ block_type      │
│ link_url        │     │ content         │
│ sort_order      │     │ sort_order      │
└─────────────────┘     └─────────────────┘
```

---

## 2.2 資料表定義

### 2.2.1 商品分類表 (shop_category)

```sql
CREATE TABLE shop_category (
    category_id     BIGINT          NOT NULL AUTO_INCREMENT COMMENT '分類ID',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父分類ID（0表示一級分類）',
    name            VARCHAR(50)     NOT NULL COMMENT '分類名稱',
    icon            VARCHAR(200)    DEFAULT NULL COMMENT '分類圖示',
    banner_image    VARCHAR(500)    DEFAULT NULL COMMENT '分類橫幅圖片',
    sort_order      INT             DEFAULT 0 COMMENT '排序（數字越小越前）',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (category_id),
    KEY idx_parent_id (parent_id),
    KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB COMMENT='商品分類表';
```

### 2.2.2 商品主表 (shop_product)

```sql
CREATE TABLE shop_product (
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
    is_hot          TINYINT(1)      DEFAULT 0 COMMENT '是否熱門',
    is_new          TINYINT(1)      DEFAULT 0 COMMENT '是否新品',
    is_recommend    TINYINT(1)      DEFAULT 0 COMMENT '是否推薦',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '狀態',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (product_id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB COMMENT='商品主表';
```

### 2.2.3 商品 SKU 表 (shop_product_sku)

```sql
CREATE TABLE shop_product_sku (
    sku_id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    product_id      BIGINT          NOT NULL COMMENT '商品ID',
    sku_code        VARCHAR(50)     DEFAULT NULL COMMENT 'SKU 編碼',
    sku_name        VARCHAR(100)    NOT NULL COMMENT '規格名稱',
    sku_image       VARCHAR(500)    DEFAULT NULL COMMENT 'SKU 專屬圖片',
    price           DECIMAL(10,2)   NOT NULL COMMENT '銷售價',
    original_price  DECIMAL(10,2)   DEFAULT NULL COMMENT '原價（劃線價）',
    cost_price      DECIMAL(10,2)   DEFAULT NULL COMMENT '成本價',
    inv_item_id     BIGINT          DEFAULT NULL COMMENT '關聯庫存物品ID',
    stock_quantity  INT             DEFAULT 0 COMMENT '獨立庫存數量',
    sales_count     INT             DEFAULT 0 COMMENT 'SKU銷量',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (sku_id),
    KEY idx_product_id (product_id),
    KEY idx_inv_item_id (inv_item_id)
) ENGINE=InnoDB COMMENT='商品SKU表';
```

### 2.2.4 會員表 (shop_member)

```sql
CREATE TABLE shop_member (
    member_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '會員ID',
    member_no       VARCHAR(32)     NOT NULL COMMENT '會員編號',
    nickname        VARCHAR(50)     DEFAULT NULL COMMENT '暱稱',
    avatar          VARCHAR(500)    DEFAULT NULL COMMENT '頭像URL',
    mobile          VARCHAR(20)     DEFAULT NULL COMMENT '手機號碼',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '電子信箱',
    password        VARCHAR(200)    DEFAULT NULL COMMENT '密碼（加密）',
    gender          VARCHAR(10)     DEFAULT 'UNKNOWN' COMMENT '性別',
    birthday        DATE            DEFAULT NULL COMMENT '生日',
    points          INT             DEFAULT 0 COMMENT '會員點數',
    level           VARCHAR(20)     DEFAULT 'NORMAL' COMMENT '會員等級',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態',
    last_login_time DATETIME        DEFAULT NULL COMMENT '最後登入時間',
    last_login_ip   VARCHAR(50)     DEFAULT NULL COMMENT '最後登入IP',
    create_time     DATETIME        DEFAULT NULL COMMENT '註冊時間',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_no (member_no),
    UNIQUE KEY uk_mobile (mobile),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB COMMENT='前台會員表';
```

### 2.2.5 會員第三方登入表 (shop_member_social)

```sql
CREATE TABLE shop_member_social (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'ID',
    member_id       BIGINT          NOT NULL COMMENT '會員ID',
    provider        VARCHAR(20)     NOT NULL COMMENT '第三方平台',
    provider_id     VARCHAR(100)    NOT NULL COMMENT '第三方用戶唯一ID',
    union_id        VARCHAR(100)    DEFAULT NULL COMMENT 'UnionID',
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
) ENGINE=InnoDB COMMENT='會員第三方登入綁定表';
```

### 2.2.6 訂單主表 (shop_order)

```sql
CREATE TABLE shop_order (
    order_id            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '訂單ID',
    order_no            VARCHAR(32)     NOT NULL COMMENT '訂單編號',
    member_id           BIGINT          NOT NULL COMMENT '會員ID',
    member_nickname     VARCHAR(50)     DEFAULT NULL COMMENT '會員暱稱（冗餘）',
    product_amount      DECIMAL(10,2)   NOT NULL COMMENT '商品金額',
    shipping_amount     DECIMAL(10,2)   DEFAULT 0 COMMENT '運費',
    discount_amount     DECIMAL(10,2)   DEFAULT 0 COMMENT '折扣金額',
    total_amount        DECIMAL(10,2)   NOT NULL COMMENT '訂單總金額',
    paid_amount         DECIMAL(10,2)   DEFAULT 0 COMMENT '已付金額',
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
    KEY idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='訂單主表';
```

### 2.2.7 訂單明細表 (shop_order_item)

```sql
CREATE TABLE shop_order_item (
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
    KEY idx_order_id (order_id)
) ENGINE=InnoDB COMMENT='訂單明細表';
```

### 2.2.8 首頁輪播表 (shop_banner)

```sql
CREATE TABLE shop_banner (
    banner_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '輪播ID',
    title           VARCHAR(100)    NOT NULL COMMENT '標題',
    image_url       VARCHAR(500)    NOT NULL COMMENT '圖片URL',
    mobile_image    VARCHAR(500)    DEFAULT NULL COMMENT '手機版圖片URL',
    link_type       VARCHAR(20)     DEFAULT 'NONE' COMMENT '連結類型',
    link_value      VARCHAR(200)    DEFAULT NULL COMMENT '連結值',
    position        VARCHAR(20)     NOT NULL DEFAULT 'HOME_TOP' COMMENT '展示位置',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    start_time      DATETIME        DEFAULT NULL COMMENT '開始時間',
    end_time        DATETIME        DEFAULT NULL COMMENT '結束時間',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (banner_id),
    KEY idx_position_status (position, status)
) ENGINE=InnoDB COMMENT='首頁輪播表';
```

### 2.2.9 頁面區塊表 (shop_page_block)

```sql
CREATE TABLE shop_page_block (
    block_id        BIGINT          NOT NULL AUTO_INCREMENT COMMENT '區塊ID',
    page_key        VARCHAR(50)     NOT NULL COMMENT '頁面識別',
    block_key       VARCHAR(50)     NOT NULL COMMENT '區塊識別',
    block_type      VARCHAR(20)     NOT NULL COMMENT '區塊類型',
    title           VARCHAR(100)    DEFAULT NULL COMMENT '區塊標題',
    sub_title       VARCHAR(200)    DEFAULT NULL COMMENT '區塊副標題',
    content         TEXT            DEFAULT NULL COMMENT '區塊內容',
    image_url       VARCHAR(500)    DEFAULT NULL COMMENT '區塊圖片',
    extra_config    JSON            DEFAULT NULL COMMENT '額外配置',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '狀態',
    create_by       VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time     DATETIME        DEFAULT NULL COMMENT '建立時間',
    update_by       VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (block_id),
    UNIQUE KEY uk_page_block (page_key, block_key)
) ENGINE=InnoDB COMMENT='頁面區塊表';
```

### 2.2.10 購物車表 (shop_cart)

```sql
CREATE TABLE shop_cart (
    cart_id         BIGINT          NOT NULL AUTO_INCREMENT COMMENT '購物車ID',
    member_id       BIGINT          NOT NULL COMMENT '會員ID',
    sku_id          BIGINT          NOT NULL COMMENT 'SKU ID',
    quantity        INT             NOT NULL DEFAULT 1 COMMENT '數量',
    is_selected     TINYINT(1)      DEFAULT 1 COMMENT '是否選中',
    create_time     DATETIME        DEFAULT NULL COMMENT '加入時間',
    update_time     DATETIME        DEFAULT NULL COMMENT '更新時間',
    PRIMARY KEY (cart_id),
    UNIQUE KEY uk_member_sku (member_id, sku_id),
    KEY idx_member_id (member_id)
) ENGINE=InnoDB COMMENT='購物車表';
```

---

## 2.3 冗餘欄位設計說明

根據專案規範，**歷史記錄不可變更和刪除，物品刪除後仍需顯示**。

### 訂單表冗餘欄位

| 欄位 | 說明 |
|-----|-----|
| `member_nickname` | 會員暱稱快照 |
| `product_name` | 商品名稱快照 |
| `sku_name` | SKU 名稱快照 |
| `sku_image` | SKU 圖片快照 |
| `unit_price` | 下單時單價 |
| `inv_item_id` | 庫存物品 ID |
| `inv_item_name` | 庫存物品名稱快照 |

**原因**：訂單是歷史記錄，即使商品下架或刪除，訂單仍需顯示當時的商品資訊。
