# LINE 使用者管理功能增強 - 測試指南

> **測試目標**: 驗證 UNFOLLOWED 狀態、自動解綁機制、頻道下拉選單功能

---

## 🔧 測試前準備

### 1. 啟動後端服務
```bash
cd cheng-admin
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 2. 啟動前端服務
```bash
cd cheng-ui
npm run dev
```

### 3. 準備測試資料
- 確保至少有 1 個啟用的 LINE 頻道設定
- 確保 Webhook URL 已正確設定

---

## ✅ 測試項目

### 測試 1: 新 API - 取得啟用頻道列表

#### 方法 1: Swagger UI 測試
1. 開啟 `http://localhost:8080/swagger-ui/index.html`
2. 找到 `LineConfigController` → `GET /line/config/enabled`
3. 點選「Try it out」→「Execute」
4. **預期結果**:
   ```json
   {
     "code": 200,
     "msg": "操作成功",
     "data": [
       {
         "configId": 1,
         "channelName": "主頻道",
         "channelType": "MAIN",
         "status": 1
       }
     ]
   }
   ```

#### 方法 2: cURL 測試
```bash
curl -X GET "http://localhost:8080/line/config/enabled" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**驗證點**:
- [ ] 只回傳 status=1 的頻道
- [ ] 回傳的 configId 欄位存在
- [ ] channelName 正確顯示

---

### 測試 2: 前端 - 匯入對話框頻道下拉選單

#### 操作步驟
1. 登入前端系統 `http://localhost:80`
2. 前往「行銷管理」→「LINE 管理」→「LINE 使用者」
3. 點選「匯入」按鈕
4. 查看「LINE 頻道」下拉選單

**驗證點**:
- [ ] 下拉選單有資料顯示
- [ ] 只顯示啟用狀態的頻道
- [ ] 頻道名稱正確顯示
- [ ] 沒有 console 錯誤

**Debug 提示**:
- 打開瀏覽器開發者工具 (F12)
- 查看 Network 標籤，確認呼叫 `/line/config/enabled`
- 查看 Console 標籤，確認沒有錯誤

---

### 測試 3: Webhook - Follow 事件（加入好友）

#### 準備工作
1. 確保 LINE Bot 已設定好 Webhook URL
2. 使用真實的 LINE 帳號

#### 操作步驟
1. 用手機 LINE 掃描 Bot QR Code
2. 點選「加入好友」
3. 檢查後端 LOG

**預期 LOG**:
```
收到關注事件，使用者ID: U1234567890abcdef
關注事件處理成功，使用者ID: U1234567890abcdef
```

**資料庫驗證**:
```sql
SELECT 
    line_user_id,
    line_display_name,
    follow_status,
    bind_status,
    first_follow_time,
    latest_follow_time
FROM sys_line_user
WHERE line_user_id = 'U1234567890abcdef';
```

**預期結果**:
- [ ] `follow_status` = `'FOLLOWING'`
- [ ] `bind_status` = `'UNBOUND'`
- [ ] `first_follow_time` 有值
- [ ] `latest_follow_time` 有值
- [ ] `line_display_name` 已更新

---

### 測試 4: Webhook - Unfollow 事件（封鎖/刪除好友）

#### 操作步驟
1. 在手機 LINE 中找到該 Bot
2. 點選「封鎖」或「刪除好友」
3. 檢查後端 LOG

**預期 LOG**:
```
收到取消關注事件，使用者ID: U1234567890abcdef
使用者取消關注，已自動解除綁定, lineUserId=U1234567890abcdef
取消關注事件處理成功，使用者ID: U1234567890abcdef
```

**資料庫驗證**:
```sql
SELECT 
    line_user_id,
    follow_status,
    bind_status,
    sys_user_id,
    unfollow_time,
    block_time,
    unbind_time
FROM sys_line_user
WHERE line_user_id = 'U1234567890abcdef';
```

**預期結果**:
- [ ] `follow_status` = `'BLOCKED'`
- [ ] `bind_status` = `'UNBOUND'` ⭐
- [ ] `sys_user_id` = `NULL` ⭐
- [ ] `unfollow_time` 有值
- [ ] `block_time` 有值
- [ ] `unbind_time` 有值

---

### 測試 5: 前端 - 狀態顯示

#### 操作步驟
1. 前往「LINE 使用者」列表頁面
2. 查看「關注狀態」欄位

**驗證點**:
- [ ] 關注中：綠色標籤「關注中」
- [ ] 已封鎖：紅色標籤「已封鎖」
- [ ] 未關注：灰色標籤「已取消」

**使用篩選器測試**:
1. 點選「關注狀態」下拉選單
2. **驗證選項**:
   - [ ] 關注中 (FOLLOWING)
   - [ ] 已取消 (UNFOLLOWED)
   - [ ] 已封鎖 (BLOCKED)

---

### 測試 6: 資料一致性檢查

#### SQL 檢查腳本
```sql
-- 檢查各狀態數量
SELECT 
    follow_status,
    bind_status,
    COUNT(*) as count
FROM sys_line_user
GROUP BY follow_status, bind_status;

-- 檢查異常資料（BLOCKED 但仍綁定）
SELECT 
    line_user_id,
    line_display_name,
    follow_status,
    bind_status,
    sys_user_id
FROM sys_line_user
WHERE follow_status = 'BLOCKED' 
  AND bind_status = 'BOUND';
  
-- 應該為空！如果有資料，表示自動解綁失敗
```

**預期結果**:
- [ ] 沒有「BLOCKED + BOUND」的資料
- [ ] 所有 BLOCKED 狀態的 sys_user_id 都是 NULL

---

## 🔍 除錯指南

### 問題 1: 下拉選單沒有資料

**可能原因**:
1. 沒有啟用的頻道
2. API 路徑錯誤
3. 權限不足

**解決方式**:
```sql
-- 檢查是否有啟用頻道
SELECT * FROM sys_line_config WHERE status = 1;

-- 如果沒有，手動啟用一個
UPDATE sys_line_config SET status = 1 WHERE config_id = 1;
```

### 問題 2: unfollow 事件沒有自動解綁

**檢查步驟**:
1. 查看後端 LOG 是否有錯誤
2. 檢查 `handleUnfollowEvent` 是否被呼叫
3. 確認事務是否成功提交

**Debug SQL**:
```sql
-- 查看最近更新的使用者
SELECT * FROM sys_line_user 
ORDER BY update_time DESC 
LIMIT 10;
```

### 問題 3: 前端顯示 enum name 而非 code

**原因**: 可能是 @JsonValue 註解沒生效

**檢查**:
```bash
# 確認 FollowStatus.java 有 @JsonValue
grep -A 5 "@JsonValue" cheng-line/src/main/java/com/cheng/line/enums/FollowStatus.java
```

**前端驗證**:
```javascript
// 在瀏覽器 Console 查看 API 回應
// 應該看到：
{
  "followStatus": "FOLLOWING",  // ✅ 正確
  // 而非
  "followStatus": {             // ❌ 錯誤
    "code": "FOLLOWING",
    "description": "好友"
  }
}
```

---

## 📊 測試結果記錄表

| 測試項目 | 狀態 | 備註 |
|---------|------|------|
| 1. 新 API - 啟用頻道列表 | ⬜ | |
| 2. 前端下拉選單顯示 | ⬜ | |
| 3. Follow 事件處理 | ⬜ | |
| 4. Unfollow 事件自動解綁 | ⬜ | |
| 5. 前端狀態顯示 | ⬜ | |
| 6. 資料一致性檢查 | ⬜ | |

**測試人員**: ___________  
**測試日期**: ___________  
**測試環境**: Local / VM / PROD

---

## 🎯 驗收標準

### 必須通過
- [x] 新 API `/line/config/enabled` 正常運作
- [x] 前端下拉選單正確顯示啟用頻道
- [x] Follow 事件自動建立使用者並設定 FOLLOWING
- [x] Unfollow 事件自動解除綁定
- [x] 前端三種狀態正確顯示

### 建議通過
- [ ] 無 console 錯誤
- [ ] 無資料庫異常資料
- [ ] LOG 記錄完整清晰

---

**測試完成後請填寫測試結果並回報！**
