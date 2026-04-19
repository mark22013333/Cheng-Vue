# LINE 模組快速測試指南

> **文件版本：** 1.0  
> **更新時間：** 2025-10-28  
> **預計測試時間：** 15-20 分鐘

---

## 🎯 測試目標

驗證 LINE 頻道設定功能是否正常運作，包括：
- ✅ 後端 API 正常編譯和啟動
- ✅ 前端頁面正常顯示
- ✅ 資料庫遷移正常執行
- ✅ 權限控制正常
- ✅ CRUD 功能正常

---

## 📋 測試前準備

### 1. 確認資料庫狀態

```sql
-- 檢查資料表是否建立成功
SHOW TABLES LIKE 'line_%';

-- 應該看到以下表：
-- line_config
-- line_user
-- line_message_log

-- 檢查選單是否建立
SELECT menu_id, menu_name, parent_id, perms 
FROM sys_menu 
WHERE menu_name LIKE '%LINE%' OR menu_name LIKE '%行銷%'
ORDER BY menu_id;
```

### 2. 檢查後端編譯狀態

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn clean compile -pl cheng-line -am -DskipTests
```

**預期結果：** `BUILD SUCCESS`

### 3. 確認前端檔案存在

```bash
# 檢查頁面檔案
ls -la /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui/src/views/line/config/

# 應該看到：
# index.vue
# components/ConfigForm.vue
# components/ConnectionTest.vue

# 檢查 API 檔案
ls -la /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui/src/api/line/

# 應該看到：
# config.js
# message.js
# user.js
```

---

## 🚀 Step 1: 啟動後端服務

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin

# 啟動後端（使用 local profile）
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 檢查點

- [ ] 服務啟動成功，無錯誤訊息
- [ ] 看到 "Started ChengApplication" 訊息
- [ ] Druid 監控介面可訪問：http://localhost:8080/druid
- [ ] 埠 8080 正常監聽

### 常見問題

**Q: 埠 8080 已被佔用？**
```bash
# 查看佔用埠的進程
lsof -i :8080

# 停止舊的進程
kill -9 <PID>
```

---

## 🎨 Step 2: 啟動前端服務

**開啟新的終端視窗**

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui

# 啟動前端開發伺服器
npm run dev
```

### 檢查點

- [ ] 前端服務啟動成功
- [ ] 看到 "Compiled successfully" 訊息
- [ ] 瀏覽器自動開啟 http://localhost:80
- [ ] 無編譯錯誤

---

## 🔐 Step 3: 登入並授權

### 3.1 登入系統

```
URL: http://localhost:80
帳號: admin
密碼: admin123
```

### 3.2 授予權限（首次使用）

1. **進入角色管理**
   ```
   系統管理 → 角色管理 → 編輯「管理員」角色
   ```

2. **勾選 LINE 權限**
   ```
   展開選單權限樹
   └─ 行銷管理
      └─ LINE 管理
         └─ LINE 設定 ✓
            ├─ LINE設定查詢 ✓
            ├─ LINE設定新增 ✓
            ├─ LINE設定修改 ✓
            ├─ LINE設定刪除 ✓
            └─ LINE連線測試 ✓
   ```

3. **儲存並重新登入**

---

## ✅ Step 4: 功能測試

### 測試 4.1: 頁面訪問

**訪問路徑：**
```
選單: 行銷管理 → LINE 管理 → LINE 設定
或直接訪問: http://localhost:80/line/config
```

**檢查點：**
- [ ] 選單正常顯示
- [ ] 點擊選單可以正常跳轉
- [ ] 頁面正常載入，無錯誤
- [ ] 頁面顯示「尚未設定任何 LINE 頻道」空狀態

### 測試 4.2: 新增頻道

**步驟：**
1. 點擊「新增頻道」按鈕
2. 填寫以下測試資料：

```
頻道名稱: 測試頻道
頻道類型: 測試頻道
Channel ID: 1234567890
Bot Basic ID: testbot (會自動加上 @)
Channel Secret: test_secret_12345678901234567890
Channel Access Token: test_token_abcdefghijklmnopqrstuvwxyz1234567890
狀態: 啟用
備註: 這是測試用頻道
```

3. 點擊「確定」

**檢查點：**
- [ ] 表單正常開啟
- [ ] 必填欄位驗證正常（空白會提示錯誤）
- [ ] Channel ID 格式驗證正常（只能輸入數字）
- [ ] 提交成功，顯示「新增成功」訊息
- [ ] 頁面自動重新整理，顯示新增的頻道卡片

### 測試 4.3: 卡片顯示

**檢查點：**
- [ ] 卡片顯示頻道名稱
- [ ] 顯示頻道類型標籤（藍色「測試頻道」）
- [ ] 顯示狀態（綠色「正常執行」）
- [ ] Bot Basic ID 顯示為 `@testbot`
- [ ] Channel ID 正常顯示
- [ ] Channel Secret 被遮罩（test****7890）
- [ ] Webhook URL 正常產生
- [ ] 統計數字顯示為 0

### 測試 4.4: 複製功能

**步驟：**
1. 點擊 Bot Basic ID 旁的複製按鈕
2. 在任意文字輸入框貼上（Ctrl+V 或 Cmd+V）

**檢查點：**
- [ ] 點擊複製按鈕後顯示「已複製到剪貼簿」訊息
- [ ] 貼上的內容正確（`@testbot`）

### 測試 4.5: Webhook URL 複製

**步驟：**
1. 點擊 Webhook URL 輸入框右側的「複製」按鈕
2. 在任意文字輸入框貼上

**檢查點：**
- [ ] 複製成功提示
- [ ] 貼上的 URL 格式正確：`http://localhost:8080/webhook/line/@testbot`

### 測試 4.6: 編輯功能

**步驟：**
1. 點擊卡片上的「設定」按鈕（或下拉選單 → 編輯）
2. 修改頻道名稱為「測試頻道（已修改）」
3. 點擊「確定」

**檢查點：**
- [ ] 編輯對話框正常開啟
- [ ] 資料正確載入到表單
- [ ] Channel Secret 和 Access Token 正確顯示（未遮罩）
- [ ] 修改成功提示
- [ ] 卡片顯示更新後的名稱

### 測試 4.7: 測試連線

**步驟：**
1. 點擊「測試連線」按鈕
2. 觀察測試結果

**檢查點：**
- [ ] 測試對話框正常開啟
- [ ] 顯示頻道資訊
- [ ] 顯示三個測試項目（API 連線、Webhook 設定、Bot 資訊）
- [ ] 測試執行中顯示載入動畫
- [ ] 測試完成後顯示結果（預期會失敗，因為是測試資料）

**預期結果（使用測試資料）：**
```
✗ API 連線測試 - 失敗（Token 無效）
✗ Webhook 設定檢查 - 失敗
✗ Bot 資訊取得 - 失敗
```

這是正常的，因為我們使用的是測試資料。

### 測試 4.8: 設為主頻道

**步驟：**
1. 點擊卡片下拉選單（⋮）
2. 選擇「設為主頻道」
3. 確認對話框點擊「確定」

**檢查點：**
- [ ] 顯示確認對話框
- [ ] 設定成功提示
- [ ] 卡片左上角顯示黃色「主頻道」標籤
- [ ] 卡片邊框變為黃色（`border: 2px solid #E6A23C`）

### 測試 4.9: 搜尋功能

**步驟：**
1. 在搜尋區域輸入「測試」
2. 點擊「搜尋」按鈕

**檢查點：**
- [ ] 搜尋結果正確顯示（找到測試頻道）
- [ ] 搜尋無結果時顯示空狀態

### 測試 4.10: 刪除功能

**步驟：**
1. 點擊卡片下拉選單
2. 選擇「刪除」
3. 確認對話框點擊「確定」

**檢查點：**
- [ ] 顯示確認對話框（包含頻道名稱）
- [ ] 刪除成功提示
- [ ] 卡片從列表中移除
- [ ] 顯示空狀態（「尚未設定任何 LINE 頻道」）

---

## 🐛 測試 4.11: 錯誤處理

### 測試空白提交

**步驟：**
1. 點擊「新增頻道」
2. 不填寫任何資料，直接點擊「確定」

**檢查點：**
- [ ] 顯示紅色錯誤提示
- [ ] 必填欄位標示紅色邊框
- [ ] 不會提交表單

### 測試格式錯誤

**步驟：**
1. Channel ID 輸入英文字母（如 `abc123`）
2. 點擊「確定」

**檢查點：**
- [ ] 顯示「Channel ID 格式不正確」錯誤
- [ ] 不會提交表單

---

## 📊 Step 5: 後端 API 測試

### 使用瀏覽器測試

**5.1 查詢頻道列表**
```
URL: http://localhost:8080/line/config/list?pageNum=1&pageSize=10
方法: GET
預期: 回傳 JSON，包含頻道列表
```

**5.2 查詢頻道詳情**
```
URL: http://localhost:8080/line/config/1
方法: GET
預期: 回傳單一頻道的完整資訊
```

### 使用 curl 測試（可選）

```bash
# 取得 Token（先登入）
TOKEN="your_token_here"

# 查詢列表
curl -X GET "http://localhost:8080/line/config/list" \
  -H "Authorization: Bearer $TOKEN"

# 新增頻道
curl -X POST "http://localhost:8080/line/config" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "channelName": "API測試頻道",
    "channelType": "TEST",
    "channelId": "9999999999",
    "botBasicId": "apitest",
    "channelSecret": "api_test_secret",
    "channelAccessToken": "api_test_token",
    "status": "0"
  }'
```

---

## ✅ 測試結果檢查清單

### 後端測試
- [ ] 後端編譯成功
- [ ] 後端啟動成功
- [ ] 無錯誤日誌
- [ ] API 端點正常回應

### 前端測試
- [ ] 前端編譯成功
- [ ] 頁面正常載入
- [ ] 無控制台錯誤
- [ ] 選單正常顯示

### 功能測試
- [ ] 新增頻道成功
- [ ] 卡片顯示正常
- [ ] 編輯功能正常
- [ ] 刪除功能正常
- [ ] 搜尋功能正常
- [ ] 複製功能正常
- [ ] 權限控制正常

### UI/UX 測試
- [ ] 卡片式設計美觀
- [ ] 響應式設計正常（調整瀏覽器大小）
- [ ] Hover 效果正常
- [ ] 動畫流暢
- [ ] 繁體中文顯示正確

---

## 🚨 常見問題排查

### 問題 1: 選單沒有顯示

**可能原因：**
- 權限未授予
- 資料庫選單未建立
- 前端路由配置錯誤

**解決方案：**
```sql
-- 檢查選單是否存在
SELECT * FROM sys_menu WHERE perms = 'line:config:list';

-- 檢查角色權限
SELECT rm.* FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.perms LIKE 'line:%';
```

### 問題 2: API 404 錯誤

**可能原因：**
- Controller 路徑錯誤
- 後端未正常啟動

**解決方案：**
```bash
# 檢查後端日誌
tail -f /Users/cheng/cool-logs/info.log

# 查看所有 Controller 映射
grep "Mapped" /Users/cheng/cool-logs/info.log | grep line
```

### 問題 3: 前端空白頁

**可能原因：**
- 組件載入錯誤
- 語法錯誤

**解決方案：**
```javascript
// 開啟瀏覽器控制台 (F12)
// 查看 Console 頁籤的錯誤訊息
// 查看 Network 頁籤的 API 請求狀態
```

---

## 📝 測試報告範本

```markdown
## LINE 模組測試報告

**測試日期：** 2025-10-28  
**測試人員：** [你的名字]  
**測試環境：** local

### 測試結果

| 測試項目 | 狀態 | 備註 |
|---------|------|------|
| 後端啟動 | ✅ / ❌ |  |
| 前端啟動 | ✅ / ❌ |  |
| 頁面訪問 | ✅ / ❌ |  |
| 新增功能 | ✅ / ❌ |  |
| 編輯功能 | ✅ / ❌ |  |
| 刪除功能 | ✅ / ❌ |  |
| 搜尋功能 | ✅ / ❌ |  |
| 權限控制 | ✅ / ❌ |  |

### 發現的問題

1. [問題描述]
   - 重現步驟：
   - 預期結果：
   - 實際結果：
   - 嚴重程度：

### 建議改進

1. [建議內容]
```

---

## 🎉 測試完成後

如果所有測試都通過：

1. ✅ 目前的 LINE 頻道設定功能可以正常使用
2. 🎯 可以決定是否繼續開發其他功能：
   - 推播訊息管理頁面
   - LINE 使用者管理頁面
3. 📦 或者先將目前功能部署到測試環境
4. 📖 更新使用者操作手冊

如果有測試失敗：

1. 📋 記錄問題詳情
2. 🔍 根據「常見問題排查」章節進行排查
3. 🐛 修正問題後重新測試
4. 💬 需要協助時提供詳細的錯誤訊息

---

**祝測試順利！** 🚀
