-- =====================================================
-- LINE 對話記錄表建立
-- 建立日期：2025-11-02
-- 說明：新增 LINE 使用者對話記錄表，用於追蹤使用者與系統的互動記錄
-- =====================================================

-- 1. 建立 LINE 對話記錄表
CREATE TABLE IF NOT EXISTS sys_line_conversation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵ID',
    line_user_id VARCHAR(255) NOT NULL COMMENT 'LINE 使用者 ID',
    direction VARCHAR(20) NOT NULL COMMENT '方向：SENT(系統發送給使用者)/RECEIVED(使用者發送給系統)',
    message_content TEXT COMMENT '訊息內容',
    message_format VARCHAR(20) COMMENT '訊息格式：TEXT/IMAGE/VIDEO/AUDIO/LOCATION/STICKER/FILE/FLEX',
    message_id VARCHAR(255) COMMENT 'LINE 訊息 ID',
    config_id INT COMMENT '頻道設定ID',
    message_time DATETIME NOT NULL COMMENT '訊息時間',
    is_success TINYINT DEFAULT 1 COMMENT '是否成功：0-失敗，1-成功',
    error_message VARCHAR(500) COMMENT '錯誤訊息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    remark VARCHAR(500) COMMENT '備註',
    INDEX idx_line_user_id (line_user_id),
    INDEX idx_message_time (message_time),
    INDEX idx_direction (direction),
    INDEX idx_config_id (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LINE 對話記錄表';

-- 2. 新增 LINE 使用者管理選單權限（如果尚未存在）

-- 取得 LINE 父選單 ID（如果不存在則跳過）
SET @lineParentId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'LINE' AND parent_id = 0 LIMIT 1);

-- 只有在 LINE 父選單存在時才新增子選單
-- 新增 LINE 使用者管理選單（如果不存在）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 'LINE 使用者', @lineParentId, 2, 'user', 'line/user/index', 1, 0, 'C', '0', '0', 'line:user:list', 'user', 'admin', NOW(), 'LINE 使用者管理'
WHERE @lineParentId IS NOT NULL 
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'line:user:list' AND parent_id = @lineParentId
);

-- 取得 LINE 使用者選單 ID
SET @lineUserMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'line:user:list' LIMIT 1);

-- 新增 LINE 使用者管理按鈕權限（只有在 LINE 使用者選單存在時才新增）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 'LINE使用者查詢', @lineUserMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'line:user:query', '#', 'admin', NOW(), ''
WHERE @lineUserMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:user:query')
UNION ALL
SELECT 'LINE使用者綁定', @lineUserMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'line:user:bind', '#', 'admin', NOW(), ''
WHERE @lineUserMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:user:bind')
UNION ALL
SELECT 'LINE使用者編輯', @lineUserMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'line:user:edit', '#', 'admin', NOW(), ''
WHERE @lineUserMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:user:edit')
UNION ALL
SELECT 'LINE使用者刪除', @lineUserMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'line:user:remove', '#', 'admin', NOW(), ''
WHERE @lineUserMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:user:remove')
UNION ALL
SELECT 'LINE使用者匯出', @lineUserMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'line:user:export', '#', 'admin', NOW(), ''
WHERE @lineUserMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:user:export')
UNION ALL
SELECT 'LINE使用者匯入', @lineUserMenuId, 6, '#', '', 1, 0, 'F', '0', '0', 'line:user:import', '#', 'admin', NOW(), ''
WHERE @lineUserMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'line:user:import');

-- =====================================================
-- 完成
-- =====================================================
