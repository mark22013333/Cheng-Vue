-- ===================================================================
-- 更新 LINE 使用者關注狀態：將 BLOCKED 改為 BLACKLISTED（黑名單）
-- 執行時間：2025-11-03
-- 說明：
--   1. 將原本的 BLOCKED 狀態統一改為 UNFOLLOWED（使用者取消關注）
--   2. BLACKLISTED 將作為管理者設定的黑名單狀態
--   3. 黑名單使用者的訊息和互動將被系統忽略
-- ===================================================================

-- 備份現有資料（可選，建議執行）
CREATE TABLE IF NOT EXISTS line_user_backup_20251103 AS SELECT * FROM line_user;

-- 更新所有 BLOCKED 狀態為 UNFOLLOWED
-- 因為 BLOCKED 實際上代表使用者取消關注，與 UNFOLLOWED 意義相同
UPDATE line_user 
SET follow_status = 'UNFOLLOWED',
    update_time = NOW()
WHERE follow_status = 'BLOCKED';

-- 顯示更新結果
SELECT 
    '更新完成' AS status,
    COUNT(*) AS affected_rows 
FROM line_user 
WHERE follow_status = 'UNFOLLOWED' 
  AND update_time >= DATE_SUB(NOW(), INTERVAL 1 MINUTE);

-- 查看各狀態的數量分佈
SELECT 
    follow_status,
    COUNT(*) AS count,
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM line_user), 2), '%') AS percentage
FROM line_user
GROUP BY follow_status
ORDER BY count DESC;

-- ===================================================================
-- 注意事項：
-- 1. BLOCKED 已不再使用，改為 UNFOLLOWED（使用者主動取消關注）
-- 2. BLACKLISTED 是新增的黑名單狀態（管理者設定）
-- 3. 執行此腳本後，需要重新啟動後端服務
-- 4. 前端已經更新，會自動顯示正確的狀態
-- ===================================================================

-- 驗證資料正確性
SELECT 
    id,
    line_user_id,
    line_display_name,
    follow_status,
    block_time,
    unfollow_time,
    update_time
FROM line_user
WHERE follow_status IN ('UNFOLLOWED', 'BLACKLISTED')
ORDER BY update_time DESC
LIMIT 10;
