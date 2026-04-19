## 1. 合併 Flyway 遷移

- [x] 1.1 重寫 `V47__shop_marketing_tracking.sql`：將 V48 的 `guest_id` 欄位、`member_id` 可 NULL、guest 索引直接整合進 CREATE TABLE
- [x] 1.2 刪除 `V48__shop_tracking_guest_support.sql`

## 2. 更新規格書引用

- [x] 2.1 更新 `guest-tracking-merge/tasks.md`：將 task 1.1 中的 V48 引用改為「已合併至 V47」

## 3. 驗證

- [x] 3.1 確認 db/migration 目錄中不存在 V48 檔案
- [x] 3.2 確認 V47 包含 guest_id 欄位和 guest 索引
