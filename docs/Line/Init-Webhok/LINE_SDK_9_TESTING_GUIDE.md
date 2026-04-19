# LINE Bot SDK 9.12.0 測試指南

> **版本：** 9.12.0  
> **更新時間：** 2025-10-28

## 啟動應用程式

### 本地環境啟動

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin

mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 檢查啟動日誌

應該看到以下正常啟動訊息：
```
Started ChengApplication in X.XXX seconds
```

如果出現 LINE SDK 相關錯誤，請檢查：
1. Maven 依賴是否正確下載
2. Import 語句是否正確
3. API 調用方式是否使用新的 SDK 9.x 語法

## 測試項目

### 1. 連線測試

**API 端點：** `POST /line/config/test-connection/{configId}`

**測試步驟：**
1. 登入管理後台：`http://localhost:8080`
2. 進入「LINE 設定」→「頻道設定」
3. 點擊「測試連線」按鈕

**預期結果：**
```json
{
  "code": 200,
  "msg": "連線測試成功！Bot 名稱：XXX"
}
```

**驗證重點：**
- ✅ 使用 `MessagingApiClient.getBotInfo()`
- ✅ 同步調用（無 `.get()`）
- ✅ Record 風格（`botInfo.displayName()`）

### 2. Webhook 測試

**API 端點：** `POST /line/config/test-webhook/{configId}`

**測試步驟：**
1. 在「LINE 設定」頁面
2. 確保 Webhook URL 已設定
3. 點擊「測試 Webhook」按鈕

**預期結果：**
```json
{
  "code": 200,
  "msg": "Webhook 測試成功！端點可正常接收訊息（狀態碼：200）"
}
```

**驗證重點：**
- ✅ 使用 `TestWebhookEndpointRequest.builder()`
- ✅ 同步調用
- ✅ Record 風格（`testResult.success()`）

### 3. 推播訊息（單人）

**API 端點：** `POST /line/message/push`

**請求範例：**
```json
{
  "configId": 1,
  "targetLineUserId": "U1234567890abcdef1234567890abcdef",
  "contentType": "TEXT",
  "textMessage": "測試訊息",
  "notificationDisabled": false
}
```

**預期結果：**
```json
{
  "code": 200,
  "msg": "推播訊息發送成功",
  "data": 1
}
```

**驗證重點：**
- ✅ 使用 `PushMessageRequest.builder()`
- ✅ 使用 `TextMessage.builder().text(...).build()`
- ✅ 同步調用
- ✅ Record 風格（`response.sentMessages().get(0).id()`）

### 4. 多人推播

**API 端點：** `POST /line/message/multicast`

**請求範例：**
```json
{
  "configId": 1,
  "targetLineUserIds": [
    "U1234567890abcdef1234567890abcdef",
    "U9876543210fedcba9876543210fedcba"
  ],
  "contentType": "TEXT",
  "textMessage": "多人測試訊息",
  "notificationDisabled": false
}
```

**預期結果：**
```json
{
  "code": 200,
  "msg": "多人推播訊息發送成功",
  "data": 2
}
```

**驗證重點：**
- ✅ 使用 `MulticastRequest.builder()`
- ✅ 使用 `new HashSet<>(userIds)`
- ✅ 同步調用（無回應，但不會拋出異常）

### 5. 廣播訊息

**API 端點：** `POST /line/message/broadcast`

**請求範例：**
```json
{
  "configId": 1,
  "contentType": "TEXT",
  "textMessage": "廣播測試訊息",
  "notificationDisabled": false
}
```

**預期結果：**
```json
{
  "code": 200,
  "msg": "廣播訊息發送成功",
  "data": 3
}
```

**驗證重點：**
- ✅ 使用 `BroadcastRequest.builder()`
- ✅ 同步調用

### 6. Webhook 事件接收

**API 端點：** `POST /callback`（由 LINE Platform 調用）

**測試步驟：**
1. 使用 ngrok 建立公開 URL：
   ```bash
   ngrok http 8080
   ```

2. 在 LINE Developers Console 設定 Webhook URL：
   ```
   https://xxxx.ngrok.io/callback
   ```

3. 從 LINE 官方帳號發送訊息

4. 在後台日誌中查看：
   ```
   收到文字訊息，使用者ID: Uxxxx, 訊息內容: xxx
   文字訊息事件處理成功，使用者ID: Uxxxx
   ```

**驗證重點：**
- ✅ 使用 `@LineMessageHandler` 和 `@EventMapping`
- ✅ 自動解析 Webhook JSON
- ✅ Record 風格（`event.source().userId()`）
- ✅ 自動簽章驗證

### 7. 使用者資料取得

**自動觸發：** 當使用者關注時自動調用

**驗證日誌：**
```
更新使用者個人資料成功：userId=Uxxxx
```

**驗證重點：**
- ✅ 使用 `MessagingApiClient.getProfile()`
- ✅ 同步調用
- ✅ Record 風格（`profile.displayName()`、`profile.pictureUrl()`）

## 常見問題排除

### 問題 1：找不到類別（Cannot resolve symbol）

**原因：** Maven 依賴未正確下載或 IDE 緩存問題

**解決方案：**
```bash
mvn clean install -DskipTests
```

然後在 IDE 中：
- IntelliJ IDEA: 右鍵專案 → Maven → Reload Project
- Eclipse: 右鍵專案 → Maven → Update Project

### 問題 2：方法不存在（Cannot resolve method）

**原因：** 使用了舊版 SDK 6.x 的 API

**檢查清單：**
- ❌ `event.getSource().getUserId()` → ✅ `event.source().userId()`
- ❌ `response.getRequestId()` → ✅ `response.sentMessages().get(0).id()`
- ❌ `client.pushMessage(pushMessage).get()` → ✅ `client.pushMessage(request)`

### 問題 3：編譯錯誤（Compilation error）

**原因：** 未移除舊的異常處理

**檢查清單：**
- ❌ `catch (InterruptedException | ExecutionException e)`
- ✅ `catch (Exception e)`

### 問題 4：Webhook 無法接收事件

**可能原因：**
1. Webhook URL 未正確設定
2. 簽章驗證失敗（確認 Channel Secret 正確）
3. 防火牆阻擋（本地測試需使用 ngrok）

**檢查步驟：**
1. 確認 Webhook URL 可從外部訪問
2. 查看應用程式日誌是否有錯誤
3. 在 LINE Developers Console 查看 Webhook 測試結果

## 效能測試

### 同步 vs 非同步

SDK 9.x 使用同步 API，簡化了程式碼但可能影響效能。

**建議：**
- 小量訊息（< 100）：同步調用即可
- 大量訊息（> 100）：考慮使用執行緒池非同步處理

**範例：**
```java
@Async
public CompletableFuture<Void> sendMessagesAsync(List<String> userIds) {
    // 使用 @Async 配合同步 API
    MessagingApiClient client = MessagingApiClient.builder(token).build();
    
    for (String userId : userIds) {
        client.pushMessage(buildRequest(userId));
    }
    
    return CompletableFuture.completedFuture(null);
}
```

## 測試結果記錄

| 測試項目 | 狀態 | 備註 |
|---------|------|------|
| 連線測試 | ⏳ 待測試 | |
| Webhook 測試 | ⏳ 待測試 | |
| 推播訊息（單人） | ⏳ 待測試 | |
| 多人推播 | ⏳ 待測試 | |
| 廣播訊息 | ⏳ 待測試 | |
| Webhook 事件接收 | ⏳ 待測試 | |
| 使用者資料取得 | ⏳ 待測試 | |

---

## 下一步

完成所有測試後：
1. ✅ 標記所有測試項目為「通過」
2. 📝 記錄遇到的問題和解決方案
3. 🚀 準備部署到正式環境
4. 📚 更新使用者文檔

## 參考資源

- [LINE Bot SDK 9.x GitHub](https://github.com/line/line-bot-sdk-java)
- [LINE Messaging API 文檔](https://developers.line.biz/en/docs/messaging-api/)
- [升級指南](./LINE_SDK_9_UPGRADE_GUIDE.md)
