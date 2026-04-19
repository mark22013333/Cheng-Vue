# LINE 頻道設定頁面測試指南

## 測試環境準備

### 1. 啟動後端服務

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin
mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 2. 啟動前端服務

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui
npm run dev
```

### 3. 訪問頁面

```
http://localhost:80/marketing/line/config
```

## 測試場景

### 場景 1：修正「頻道類型已存在」錯誤

**目的**：驗證已存在的頻道可以正常編輯

**步驟**：
1. 登入系統
2. 進入「營銷管理 > LINE 頻道設定」頁面
3. 找到已存在的頻道（例如：Ava-OA）
4. 點擊「設定」按鈕
5. 修改頻道名稱（例如：改為 "Ava-OA 測試"）
6. 點擊「確定」按鈕

**預期結果**：
- ✅ 儲存成功，顯示「修改成功」訊息
- ✅ 不出現「新增 LINE 頻道設定失敗，頻道類型已存在」錯誤
- ✅ 頻道列表中顯示更新後的名稱

**失敗情況**：
- ❌ 出現「頻道類型已存在」錯誤
- ❌ 儲存失敗

---

### 場景 2：驗證 Webhook 基礎 URL 顯示

**目的**：確認 Webhook 基礎 URL 欄位正確顯示和儲存

**步驟**：
1. 點擊任一頻道的「設定」按鈕
2. 檢查「Webhook 設定」區塊
3. 查看「Webhook 基礎 URL」欄位

**預期結果**：
- ✅ 欄位存在且可見
- ✅ 如果資料庫有值，應顯示該值
- ✅ 如果為空，顯示 placeholder：「選填，留空則使用系統預設值」
- ✅ 欄位前綴顯示 "https://"

**測試自訂 Webhook 基礎 URL**：
1. 在「Webhook 基礎 URL」欄位輸入：`your-test-domain.com`
2. 點擊「確定」儲存
3. 重新開啟該頻道設定
4. 檢查「Webhook 基礎 URL」是否顯示 `your-test-domain.com`
5. 檢查「Webhook URL」是否變為：`https://your-test-domain.com/webhook/line/322okyxf`

**預期結果**：
- ✅ 自訂 URL 正確儲存
- ✅ Webhook URL 使用自訂的 baseUrl
- ✅ 清空自訂 URL 後，使用系統預設值

---

### 場景 3：測試「設定 LINE Webhook URL」功能

#### 3.1 成功情況測試

**前置條件**：
- 頻道狀態為「啟用」
- Channel Access Token 有效
- Bot Basic ID 正確

**步驟**：
1. 開啟頻道編輯對話框（必須是已存在的頻道）
2. 檢查「Webhook URL」欄位下方是否有「設定 LINE Webhook URL」按鈕
3. 點擊「設定 LINE Webhook URL」按鈕
4. 確認提示對話框：「即將呼叫 LINE API 設定 Webhook URL，確定要繼續嗎？」
5. 點擊「確定」

**預期結果**：
- ✅ 按鈕顯示 Loading 狀態
- ✅ 呼叫 API：`POST /line/config/setLineWebhook/{configId}`
- ✅ 顯示成功訊息：「Webhook URL 設定成功：https://...」
- ✅ 資料庫 `webhook_status` 更新為 `0`（ENABLE）
- ✅ LINE Developer Console 的 Webhook URL 已自動設定

**驗證方式**：
1. 查看瀏覽器 Network 面板，確認 API 呼叫成功（200 OK）
2. 查看後端日誌：
   ```
   設定 LINE Webhook 端點成功，URL：https://...
   ```
3. 登入 [LINE Developers Console](https://developers.line.biz/console/)
4. 選擇對應的頻道
5. 進入「Messaging API」頁籤
6. 檢查「Webhook URL」是否已自動設定

#### 3.2 失敗情況測試

**測試 1：頻道未儲存**

**步驟**：
1. 點擊「新增頻道」
2. 填寫必要資訊但不儲存
3. 嘗試點擊「設定 LINE Webhook URL」按鈕

**預期結果**：
- ✅ 按鈕不顯示（因為是新增模式）
- ✅ 或顯示警告：「請先儲存頻道設定」

---

**測試 2：Access Token 無效**

**前置條件**：
- 使用無效或過期的 Channel Access Token

**步驟**：
1. 修改頻道的 Channel Access Token 為無效值
2. 儲存頻道
3. 重新開啟編輯對話框
4. 點擊「設定 LINE Webhook URL」按鈕
5. 確認提示對話框

**預期結果**：
- ✅ 顯示錯誤訊息：「設定失敗：...」
- ✅ 資料庫 `webhook_status` 更新為 `1`（DISABLE）
- ✅ 後端日誌記錄錯誤詳情

**後端日誌範例**：
```
設定 LINE Webhook 端點失敗
com.linecorp.bot.client.exception.LineBotAPIException: ...
```

---

**測試 3：網路錯誤**

**步驟**：
1. 暫時中斷網路連線
2. 點擊「設定 LINE Webhook URL」按鈕
3. 確認提示對話框

**預期結果**：
- ✅ 顯示錯誤訊息
- ✅ 按鈕恢復可用狀態
- ✅ 可以重新嘗試

---

### 場景 4：整合測試流程

**完整流程測試**：

1. **新增頻道**
   - 填寫所有必要欄位
   - 自訂 Webhook 基礎 URL（選填）
   - 儲存頻道

2. **編輯頻道**
   - 修改頻道名稱
   - 確認 Webhook 基礎 URL 正確顯示
   - 確認 Webhook URL 自動更新
   - 儲存修改

3. **設定 Webhook**
   - 點擊「設定 LINE Webhook URL」按鈕
   - 確認設定成功
   - 驗證 LINE Developer Console

4. **測試 Webhook**
   - 使用 LINE 發送訊息給 Bot
   - 檢查後端是否收到 Webhook 請求
   - 確認 Webhook 正常運作

---

## 檢查清單

### 功能檢查

- [ ] 可以新增頻道
- [ ] 可以編輯已存在的頻道（不出現「頻道類型已存在」錯誤）
- [ ] Webhook 基礎 URL 欄位正確顯示
- [ ] 可以自訂 Webhook 基礎 URL
- [ ] Webhook URL 自動產生正確
- [ ] 「設定 LINE Webhook URL」按鈕在編輯模式顯示
- [ ] 「設定 LINE Webhook URL」按鈕在新增模式隱藏
- [ ] 點擊按鈕顯示確認對話框
- [ ] 設定成功顯示成功訊息
- [ ] 設定失敗顯示錯誤訊息
- [ ] Loading 狀態正確顯示

### 資料庫檢查

```sql
-- 檢查頻道資料
SELECT 
  config_id,
  channel_name,
  channel_type,
  bot_basic_id,
  webhook_base_url,
  webhook_url,
  webhook_status,
  status
FROM sys_line_config;

-- 檢查 webhook_status
-- 0 = ENABLE (設定成功)
-- 1 = DISABLE (未設定或設定失敗)
```

### 日誌檢查

**後端日誌**：
```bash
# 成功日誌
開始設定 LINE Webhook 端點，設定ID：1
設定 LINE Webhook 端點成功，URL：https://...

# 失敗日誌
開始設定 LINE Webhook 端點，設定ID：1
設定 LINE Webhook 端點失敗
```

**前端 Network**：
```
POST /dev-api/line/config/setLineWebhook/1
Status: 200 OK
Response: {
  "code": 200,
  "msg": "Webhook URL 設定成功：https://...",
  "data": "..."
}
```

---

## 常見問題排查

### 問題 1：頻道編輯失敗，提示「頻道類型已存在」

**原因**：Controller 層檢查邏輯未移除

**解決方式**：
- 確認 `LineConfigController.java` 的 `add` 方法已移除 `checkChannelTypeUnique` 檢查
- 重新編譯並重啟後端服務

---

### 問題 2：Webhook 基礎 URL 不顯示

**原因**：前端未正確綁定或資料庫欄位為 NULL

**檢查步驟**：
1. 檢查 `ConfigForm.vue` 第84-96行是否存在欄位
2. 檢查 `v-model="form.webhookBaseUrl"` 綁定是否正確
3. 檢查資料庫 `webhook_base_url` 欄位

---

### 問題 3：「設定 LINE Webhook URL」按鈕不顯示

**原因**：條件判斷不符合

**檢查條件**：
- `!isAdd` - 必須是編輯模式
- `form.configId` - 必須有頻道 ID
- `webhookUrl` - 必須有產生的 Webhook URL

---

### 問題 4：設定 Webhook 失敗

**可能原因**：
1. Channel Access Token 無效或過期
2. Bot Basic ID 不正確
3. 網路連線問題
4. LINE API 服務異常

**排查步驟**：
1. 檢查後端日誌錯誤訊息
2. 驗證 Channel Access Token 是否有效
3. 確認 Bot Basic ID 格式正確（有或沒有 @ 前綴都可以）
4. 嘗試直接呼叫 LINE API 測試

---

## LINE Developer Console 驗證

### 查看 Webhook URL

1. 登入 [LINE Developers Console](https://developers.line.biz/console/)
2. 選擇 Provider
3. 選擇對應的 Channel
4. 點擊「Messaging API」頁籤
5. 找到「Webhook settings」區塊
6. 確認「Webhook URL」是否正確設定

### 測試 Webhook

1. 在 LINE Developer Console 點擊「Verify」按鈕
2. 查看驗證結果
3. 如果成功，表示 Webhook 設定正確

---

## 效能與安全性

### API 回應時間

- 正常情況：1-3 秒
- 包含 LINE API 呼叫時間

### 安全性檢查

- [ ] Channel Access Token 加密儲存
- [ ] API 需要權限驗證
- [ ] 錯誤訊息不洩露敏感資訊
- [ ] 日誌記錄完整但不包含 Token

---

## 回報問題

如果測試中發現問題，請提供以下資訊：

1. **問題描述**：詳細說明發生的問題
2. **重現步驟**：如何重現該問題
3. **預期結果**：應該出現什麼結果
4. **實際結果**：實際出現什麼結果
5. **截圖**：提供相關截圖
6. **日誌**：提供後端日誌和前端 Console 錯誤
7. **環境資訊**：瀏覽器版本、後端版本等

---

## 測試完成確認

測試完成後，請確認：

- [ ] 所有測試場景都已執行
- [ ] 功能檢查清單全部通過
- [ ] 沒有發現新的 Bug
- [ ] 效能表現符合預期
- [ ] 使用者體驗良好

**測試日期**：2025-10-29

**測試人員**：_____________

**測試結果**：□ 通過 □ 失敗（需要修正）
