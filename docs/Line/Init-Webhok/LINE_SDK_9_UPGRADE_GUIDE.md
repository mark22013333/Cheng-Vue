# LINE Bot SDK 升級指南：6.0.0 → 9.12.0

> **升級時間：** 2025-10-28  
> **SDK 版本：** 6.0.0 → 9.12.0

## 概述

LINE Bot SDK 7.x+ 是基於 OpenAPI 規範自動產生的版本，與 6.x 完全不相容，但能更快速地跟進 API 變更。

## 主要變更

### 1. 模組重新組織

| SDK 6.x | SDK 9.x | 說明 |
|---------|---------|------|
| `line-bot-api-client` | `line-bot-messaging-api-client` | Messaging API 客戶端 |
| `line-bot-model` | `line-bot-webhook` | Webhook 事件模型 |
| `line-bot-spring-boot-starter` | `line-bot-spring-boot-webmvc` | Spring Boot 整合 |
| - | `line-channel-access-token-client` | Access Token 管理 |

### 2. 包路徑變更

#### 事件模型
- **6.x**: `com.linecorp.bot.model.event.*`
- **9.x**: `com.linecorp.bot.webhook.model.*`

#### Spring 註解
- **6.x**: `com.linecorp.bot.spring.boot.annotation.*`
- **9.x**: `com.linecorp.bot.spring.boot.handler.annotation.*`

#### 訊息模型
- **6.x**: `com.linecorp.bot.model.message.*`
- **9.x**: `com.linecorp.bot.messaging.model.*`

#### 客戶端
- **6.x**: `com.linecorp.bot.client.LineMessagingClient`
- **9.x**: `com.linecorp.bot.messaging.client.MessagingApiClient`

### 3. API 調用方式變更

#### 客戶端建立

**6.x:**
```java
LineMessagingClient client = LineMessagingClient.builder(channelAccessToken).build();
```

**9.x:**
```java
MessagingApiClient client = MessagingApiClient.builder(channelAccessToken).build();
```

#### 非同步 vs 同步

**6.x (非同步):**
```java
BotApiResponse response = client.pushMessage(pushMessage).get();  // CompletableFuture
```

**9.x (同步):**
```java
PushMessageResponse response = client.pushMessage(request);  // 直接同步調用
```

#### 方法名稱變更 (Record 風格)

**6.x (JavaBean):**
```java
event.getSource().getUserId()
testResult.isSuccess()
testResult.getStatusCode()
```

**9.x (Record):**
```java
event.source().userId()
testResult.success()
testResult.statusCode()
```

### 4. 訊息發送 API 變更

#### Push Message

**6.x:**
```java
PushMessage pushMessage = new PushMessage(userId, messages);
BotApiResponse response = client.pushMessage(pushMessage).get();
```

**9.x:**
```java
PushMessageRequest request = PushMessageRequest.builder()
        .to(userId)
        .messages(messages)
        .build();
PushMessageResponse response = client.pushMessage(request);
```

#### Multicast

**6.x:**
```java
Multicast multicast = new Multicast(userIds, messages);
BotApiResponse response = client.multicast(multicast).get();
```

**9.x:**
```java
MulticastRequest request = MulticastRequest.builder()
        .to(userIds)
        .messages(messages)
        .build();
client.multicast(request);
```

#### Broadcast

**6.x:**
```java
Broadcast broadcast = new Broadcast(messages);
BotApiResponse response = client.broadcast(broadcast).get();
```

**9.x:**
```java
BroadcastRequest request = BroadcastRequest.builder()
        .messages(messages)
        .build();
client.broadcast(request);
```

#### Reply Message

**6.x:**
```java
ReplyMessage replyMessage = new ReplyMessage(replyToken, messages);
BotApiResponse response = client.replyMessage(replyMessage).get();
```

**9.x:**
```java
ReplyMessageRequest request = ReplyMessageRequest.builder()
        .replyToken(replyToken)
        .messages(messages)
        .build();
ReplyMessageResponse response = client.replyMessage(request);
```

### 5. 訊息建立方式變更

#### Text Message

**6.x:**
```java
Message message = new TextMessage("Hello");
```

**9.x:**
```java
Message message = TextMessage.builder()
        .text("Hello")
        .build();
```

#### Image Message

**6.x:**
```java
Message message = new ImageMessage(originalUrl, previewUrl);
```

**9.x:**
```java
Message message = ImageMessage.builder()
        .originalContentUrl(originalUrl)
        .previewImageUrl(previewUrl)
        .build();
```

### 6. 使用者資料取得

**6.x:**
```java
UserProfileResponse profile = client.getProfile(userId).get();
String displayName = profile.getDisplayName();
String pictureUrl = profile.getPictureUrl();
```

**9.x:**
```java
UserProfileResponse profile = client.getProfile(userId);
String displayName = profile.displayName();
String pictureUrl = profile.pictureUrl();
```

### 7. Webhook 測試

**6.x:**
```java
TestWebhookEndpointRequest request = TestWebhookEndpointRequest.builder()
        .endpoint(new URI(webhookUrl))
        .build();
var result = client.testWebhookEndpoint(request).get();
boolean success = result.isSuccess();
```

**9.x:**
```java
TestWebhookEndpointRequest request = TestWebhookEndpointRequest.builder()
        .endpoint(webhookUrl)  // String 而非 URI
        .build();
TestWebhookEndpointResponse result = client.testWebhookEndpoint(request);
boolean success = result.success();
```

## 升級檢查清單

- [x] 更新父 POM 的 SDK 版本至 9.12.0
- [x] 更新依賴：
  - [x] `line-bot-messaging-api-client`
  - [x] `line-bot-webhook`
  - [x] `line-bot-parser`
  - [x] `line-bot-spring-boot-webmvc`
  - [x] `line-channel-access-token-client`
- [x] 更新 Import 語句
  - [x] `LineWebhookController.java`
  - [x] `LineConfigServiceImpl.java`
  - [x] `LineMessageServiceImpl.java`
  - [x] `LineUserServiceImpl.java`
- [x] 更新 API 調用方式
  - [x] Push Message
  - [x] Multicast
  - [x] Broadcast
  - [x] Reply Message
  - [x] Get Profile
  - [x] Test Webhook
  - [x] Test Connection
  - [x] Build Text Message
  - [x] Build Image Message
- [ ] 測試所有功能
  - [ ] Webhook 事件接收
  - [ ] 訊息發送
  - [ ] 使用者資料取得
  - [ ] 連線測試

## 注意事項

1. **沒有向下相容性**：SDK 9.x 與 6.x 完全不相容，需要全面修改程式碼
2. **同步 API**：9.x 預設使用同步 API，不需要 `.get()` 或處理 `CompletableFuture`
3. **Record 風格**：多數回應物件使用 Java Record，使用 `field()` 而非 `getField()`
4. **Builder 模式**：所有請求和訊息物件都使用 Builder 模式建立
5. **異常處理**：不再需要處理 `InterruptedException` 和 `ExecutionException`

## 已完成的升級

### ✅ 完成檔案清單

1. **POM 檔案**
   - `/pom.xml` - 父 POM，版本升級至 9.12.0
   - `/cheng-line/pom.xml` - LINE 模組依賴更新
   - `/cheng-admin/pom.xml` - Admin 模組依賴更新

2. **Controller 層**
   - `LineWebhookController.java` - Webhook 事件處理，使用 SDK 9.x 事件模型

3. **Service 層**
   - `LineConfigServiceImpl.java` - 完整更新
     - `testLineConnection()` - 使用新的 `getBotInfo()` API
     - `testWebhook()` - 使用新的 `testWebhookEndpoint()` API
   - `LineMessageServiceImpl.java` - 完整更新
     - `sendPushMessage()` - 使用 `PushMessageRequest`
     - `sendMulticastMessage()` - 使用 `MulticastRequest`
     - `sendBroadcastMessage()` - 使用 `BroadcastRequest`
     - `sendReplyMessage()` - 使用 `ReplyMessageRequest`
     - `buildTextMessage()` - 使用 Builder 模式
     - `buildImageMessage()` - 使用 Builder 模式
   - `LineUserServiceImpl.java` - 完整更新
     - `updateUserProfile()` - 使用新的 `getProfile()` API

### 🔧 主要變更摘要

- **所有非同步調用改為同步**：移除所有 `.get()` 和 `CompletableFuture` 處理
- **所有 Request 使用 Builder**：改用 `XxxRequest.builder()....build()` 模式
- **所有 Response 使用 Record**：改用 `response.field()` 而非 `response.getField()`
- **訊息建立使用 Builder**：`TextMessage.builder().text(...).build()`
- **移除異常處理**：不再需要 `InterruptedException` 和 `ExecutionException`

## 參考資源

- [LINE Bot SDK Java GitHub](https://github.com/line/line-bot-sdk-java)
- [LINE Messaging API 文檔](https://developers.line.biz/en/docs/messaging-api/)
- [Maven Repository](https://central.sonatype.com/search?q=com.linecorp.bot)
