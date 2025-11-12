-- V16: 更新 LINE 使用者表的 follow_status 欄位註解
-- 作者: cheng
-- 日期: 2025-11-12
-- 說明: 更新 follow_status 欄位註解，明確區分三種關注狀態

-- 更新 follow_status 欄位註解
ALTER TABLE sys_line_user
    MODIFY COLUMN follow_status VARCHAR(20) NOT NULL DEFAULT 'UNFOLLOWED'
        COMMENT '關注狀態：UNFOLLOWED(未關注或封鎖)/FOLLOWING(關注中)/BLACKLISTED(管理者黑名單)';

-- 說明：
-- 1. UNFOLLOWED: 使用者主動取消關注或封鎖 LINE 頻道（LINE 官方狀態，兩者在系統中視為相同）
-- 2. FOLLOWING: 使用者正在關注頻道（好友狀態）
-- 3. BLACKLISTED: 管理者設定的黑名單（即使使用者關注頻道，也無法接收訊息，優先級最高）
-- 
-- 優先級：BLACKLISTED > FOLLOWING > UNFOLLOWED
-- 當使用者被加入黑名單時，無論原本是什麼狀態（包括關注中），都無法收到訊息
