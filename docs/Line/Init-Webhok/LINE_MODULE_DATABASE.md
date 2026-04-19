# LINE 模組 - 資料庫設計文件

> **建立時間：** 2025-10-26  
> **版本：** 1.0

---

## 資料表清單

1. `sys_line_config` - LINE 頻道設定表
2. `sys_line_message_log` - 推播訊息記錄表
3. `sys_line_user` - LINE 使用者表
4. `sys_line_user_tag` - LINE 使用者標籤表（預留）
5. `sys_line_user_tag_relation` - 標籤關聯表（預留）

---

## 1. LINE 頻道設定表 (`sys_line_config`)

### 用途
儲存多個 LINE 頻道的設定（主頻道、副頻道、測試頻道）

### 表格結構

```sql
CREATE TABLE sys_line_config (
    config_id           INT             AUTO_INCREMENT COMMENT '設定ID',
    channel_type        VARCHAR(20)     NOT NULL COMMENT '頻道類型：MAIN/SUB/TEST',
    channel_name        VARCHAR(100)    NOT NULL COMMENT '頻道名稱',
    channel_id          VARCHAR(255)    NOT NULL COMMENT 'LINE Channel ID (加密)',
    channel_secret      VARCHAR(500)    NOT NULL COMMENT 'LINE Channel Secret (加密)',
    channel_access_token VARCHAR(1000)  NOT NULL COMMENT 'Channel Access Token (加密)',
    webhook_url         VARCHAR(500)    DEFAULT NULL COMMENT 'Webhook URL',
    webhook_status      TINYINT         DEFAULT 0 COMMENT 'Webhook 狀態',
    status              TINYINT         DEFAULT 1 COMMENT '啟用狀態',
    is_default          TINYINT         DEFAULT 0 COMMENT '是否為預設頻道',
    sort_order          INT             DEFAULT 0 COMMENT '排序順序',
    create_by           VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_by           VARCHAR(64)     DEFAULT '' COMMENT '更新者',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark              VARCHAR(500)    DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_channel_type (channel_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LINE 頻道設定表';
```

**重要說明：**
- 使用 INT 作為主鍵（頻道數量不多）
- channel_type 使用 UNIQUE 索引確保每種類型只有一筆
- 敏感欄位使用 Jasypt 加密儲存

---

## 2. 推播訊息記錄表 (`sys_line_message_log`)

```sql
CREATE TABLE sys_line_message_log (
    message_id          BIGINT          AUTO_INCREMENT COMMENT '訊息ID',
    config_id           INT             NOT NULL COMMENT '使用的頻道設定ID',
    message_type        VARCHAR(20)     NOT NULL COMMENT '訊息類型',
    content_type        VARCHAR(20)     NOT NULL COMMENT '內容類型',
    message_content     TEXT            NOT NULL COMMENT '訊息內容(JSON)',
    target_type         VARCHAR(20)     NOT NULL COMMENT '推播對象類型',
    target_line_user_id VARCHAR(255)    DEFAULT NULL COMMENT '目標使用者ID(單人)',
    target_user_ids     TEXT            DEFAULT NULL COMMENT '目標使用者列表(多人)',
    target_tag_id       BIGINT          DEFAULT NULL COMMENT '目標標籤ID(預留)',
    target_count        INT             DEFAULT 0 COMMENT '目標數量',
    success_count       INT             DEFAULT 0 COMMENT '成功數量',
    fail_count          INT             DEFAULT 0 COMMENT '失敗數量',
    send_status         VARCHAR(20)     NOT NULL COMMENT '發送狀態',
    error_message       TEXT            DEFAULT NULL COMMENT '錯誤訊息',
    line_request_id     VARCHAR(100)    DEFAULT NULL COMMENT 'LINE API Request ID',
    send_time           DATETIME        DEFAULT NULL COMMENT '發送時間',
    create_by           VARCHAR(64)     DEFAULT '' COMMENT '建立者',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (message_id),
    INDEX idx_config_id (config_id),
    INDEX idx_target_line_user_id (target_line_user_id),
    INDEX idx_target_tag_id (target_tag_id),
    INDEX idx_send_status (send_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LINE 推播訊息記錄表';
```

**重要欄位說明：**
- target_line_user_id: 單人推播使用
- target_user_ids: 多人推播使用（JSON Array）
- target_tag_id: 標籤推播使用（預留欄位）

---

## 3. LINE 使用者表 (`sys_line_user`)

```sql
CREATE TABLE sys_line_user (
    id                      BIGINT          AUTO_INCREMENT COMMENT '主鍵ID',
    line_user_id            VARCHAR(255)    NOT NULL COMMENT 'LINE使用者ID',
    line_display_name       VARCHAR(255)    DEFAULT NULL COMMENT 'LINE顯示名稱',
    line_picture_url        VARCHAR(500)    DEFAULT NULL COMMENT 'LINE頭像URL',
    line_status_message     VARCHAR(500)    DEFAULT NULL COMMENT 'LINE狀態訊息',
    line_language           VARCHAR(10)     DEFAULT NULL COMMENT 'LINE語言設定',
    sys_user_id             BIGINT          DEFAULT NULL COMMENT '系統使用者ID',
    bind_status             VARCHAR(20)     NOT NULL DEFAULT 'UNBOUND' COMMENT '綁定狀態',
    follow_status           VARCHAR(20)     NOT NULL DEFAULT 'UNFOLLOWED' COMMENT '關注狀態',
    first_follow_time       DATETIME        DEFAULT NULL COMMENT '初次加入時間',
    latest_follow_time      DATETIME        DEFAULT NULL COMMENT '最近關注時間',
    unfollow_time           DATETIME        DEFAULT NULL COMMENT '取消關注時間',
    block_time              DATETIME        DEFAULT NULL COMMENT '封鎖時間',
    first_bind_time         DATETIME        DEFAULT NULL COMMENT '初次綁定時間',
    latest_bind_time        DATETIME        DEFAULT NULL COMMENT '最近綁定時間',
    unbind_time             DATETIME        DEFAULT NULL COMMENT '解除綁定時間',
    bind_count              INT             DEFAULT 0 COMMENT '綁定次數',
    total_messages_sent     INT             DEFAULT 0 COMMENT '累計發送訊息數',
    total_messages_received INT             DEFAULT 0 COMMENT '累計接收訊息數',
    last_interaction_time   DATETIME        DEFAULT NULL COMMENT '最後互動時間',
    create_time             DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_time             DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark                  VARCHAR(500)    DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (id),
    UNIQUE KEY uk_line_user_id (line_user_id),
    INDEX idx_sys_user_id (sys_user_id),
    INDEX idx_bind_status (bind_status),
    INDEX idx_follow_status (follow_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LINE使用者表';
```

**完整時間追蹤：**
- 初次加入、最近關注、取消關注、封鎖
- 初次綁定、最近綁定、解除綁定
- 支援多次綁定/解綁的記錄

**統計資訊：**
- 綁定次數、發送/接收訊息數
- 最後互動時間

---

## 4-5. 標籤相關表（預留設計）

詳細設計請參考完整的資料庫遷移檔案 `V10__init_line_module.sql`
