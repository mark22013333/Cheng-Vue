## MODIFIED Requirements

### Requirement: 合併 Flyway 遷移

V47 遷移 SHALL 直接建出包含訪客追蹤支援的最終表結構（`guest_id` 欄位、`member_id` 可 NULL、guest 索引）。V48 遷移 SHALL 被刪除。

#### Scenario: 合併後的 V47
- **WHEN** 執行 V47 遷移
- **THEN** `shop_browsing_logs` 和 `shop_search_logs` 包含 `guest_id VARCHAR(36) NULL` 欄位、`member_id` 為 `BIGINT NULL`、包含 `idx_browsing_guest_time` 和 `idx_search_guest_time` 索引

#### Scenario: V48 不存在
- **WHEN** 查看 db/migration 目錄
- **THEN** 不存在 V48 遷移檔案

### Requirement: 規格書引用更新

`guest-tracking-merge` 的 tasks.md 中對 V48 的引用 SHALL 更新為反映已合併至 V47 的事實。
