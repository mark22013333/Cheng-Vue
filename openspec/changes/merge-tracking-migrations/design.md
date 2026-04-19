## Overview

將 V47（CREATE TABLE）和 V48（ALTER TABLE 加 guest_id）合併為單一 V47 遷移，直接建出包含 `guest_id` 和可 NULL `member_id` 的最終表結構。

## Approach

### 合併策略

將 V48 的三項變更直接內嵌至 V47 的 CREATE TABLE：

1. `member_id BIGINT NULL`（原為 NOT NULL）
2. `guest_id VARCHAR(36) NULL`（新增欄位，位於 member_id 之後）
3. `INDEX idx_browsing_guest_time` / `idx_search_guest_time`（新增索引）

### V47 最終結構

```sql
CREATE TABLE shop_browsing_logs (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT,
    member_id     BIGINT       NULL,                      -- 改為 NULL
    guest_id      VARCHAR(36)  NULL,                      -- 從 V48 合併
    product_id    BIGINT       NOT NULL,
    product_name  VARCHAR(200) NULL,
    category_id   BIGINT       NULL,
    source        VARCHAR(20)  NOT NULL DEFAULT 'DIRECT',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    INDEX idx_browsing_member_time (member_id, create_time DESC),
    INDEX idx_browsing_product (product_id),
    INDEX idx_browsing_guest_time (guest_id, create_time DESC)  -- 從 V48 合併
);

CREATE TABLE shop_search_logs (
    -- 同理
);
```

## Risks

- 若 V47 已在某環境執行過，需改用新版本號。但根據確認，V47 尚未在任何環境執行。
