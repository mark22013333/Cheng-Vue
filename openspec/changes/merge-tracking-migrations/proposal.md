## Why

V47 和 V48 兩個 Flyway 遷移都尚未在任何環境執行過，且 V48 是對 V47 建立的表做即時修改（新增 `guest_id`、`member_id` 改 NULL）。將兩者合併為單一 V47 可避免不必要的 ALTER，直接建出最終結構。同時需更新相關的 openspec 規格書以反映合併後的狀態。

## What Changes

- 將 `V48__shop_tracking_guest_support.sql` 的欄位變更（`guest_id`、`member_id` 可 NULL、索引）整合進 `V47__shop_marketing_tracking.sql` 的 CREATE TABLE
- 刪除 `V48__shop_tracking_guest_support.sql`
- 更新 `guest-tracking-merge` 規格書中對 V48 的引用

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- `cheng-admin/src/main/resources/db/migration/V47__shop_marketing_tracking.sql` — 重寫
- `cheng-admin/src/main/resources/db/migration/V48__shop_tracking_guest_support.sql` — 刪除
- `openspec/changes/guest-tracking-merge/tasks.md` — 更新 V48 引用為 V47
