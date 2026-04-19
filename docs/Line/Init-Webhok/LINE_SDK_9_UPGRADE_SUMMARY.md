# LINE Bot SDK 升級到 9.12.0 完整總結

## 版本資訊
- **舊版本**: LINE Bot SDK 6.x
- **新版本**: LINE Bot SDK 9.12.0
- **升級日期**: 2025-10-28
- **編譯狀態**: ✅ BUILD SUCCESS

## 關鍵發現

### @LineMessageHandler 和 @EventMapping 註解的位置

**重要**: 這些註解在 `line-bot-spring-boot-handler` 模組中，而非 `line-bot-spring-boot-webmvc`！

```xml
<!-- 必須新增此依賴才能使用 @LineMessageHandler 和 @EventMapping -->
<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-spring-boot-handler</artifactId>
    <version>9.12.0</version>
</dependency>
```

## Maven 依賴完整配置

### 根 pom.xml (dependency management)
```xml
<line-bot-sdk.version>9.12.0</line-bot-sdk.version>

<dependencyManagement>
    <dependencies>
        <!-- LINE Bot SDK 9.12.0 - Messaging API Client -->
        <dependency>
            <groupId>com.linecorp.bot</groupId>
            <artifactId>line-bot-messaging-api-client</artifactId>
            <version>${line-bot-sdk.version}</version>
        </dependency>

        <!-- LINE Bot SDK 9.12.0 - Webhook -->
        <dependency>
            <groupId>com.linecorp.bot</groupId>
            <artifactId>line-bot-webhook</artifactId>
            <version>${line-bot-sdk.version}</version>
        </dependency>

        <!-- LINE Bot SDK 9.12.0 - Parser -->
        <dependency>
            <groupId>com.linecorp.bot</groupId>
            <artifactId>line-bot-parser</artifactId>
            <version>${line-bot-sdk.version}</version>
        </dependency>

        <!-- LINE Bot Spring Boot Handler (Webhook 事件處理註解) -->
        <dependency>
            <groupId>com.linecorp.bot</groupId>
            <artifactId>line-bot-spring-boot-handler</artifactId>
            <version>${line-bot-sdk.version}</version>
        </dependency>

        <!-- LINE Bot Spring Boot WebMVC (Spring MVC 整合) -->
        <dependency>
            <groupId>com.linecorp.bot</groupId>
            <artifactId>line-bot-spring-boot-webmvc</artifactId>
            <version>${line-bot-sdk.version}</version>
        </dependency>

        <!-- LINE Channel Access Token Client -->
        <dependency>
            <groupId>com.linecorp.bot</groupId>
            <artifactId>line-channel-access-token-client</artifactId>
            <version>${line-bot-sdk.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### cheng-line 模組 pom.xml
```xml
<dependencies>
    <!-- LINE Bot SDK 9.12.0 - Messaging API Client -->
    <dependency>
        <groupId>com.linecorp.bot</groupId>
        <artifactId>line-bot-messaging-api-client</artifactId>
    </dependency>

    <!-- LINE Bot SDK 9.12.0 - Webhook -->
    <dependency>
        <groupId>com.linecorp.bot</groupId>
        <artifactId>line-bot-webhook</artifactId>
    </dependency>
</dependencies>
```

### cheng-admin 模組 pom.xml
```xml
<dependencies>
    <!-- LINE Bot Spring Boot Handler (Webhook 事件處理註解) -->
    <dependency>
        <groupId>com.linecorp.bot</groupId>
        <artifactId>line-bot-spring-boot-handler</artifactId>
    </dependency>

    <!-- LINE Bot Spring Boot WebMVC (Spring MVC 整合) -->
    <dependency>
        <groupId>com.linecorp.bot</groupId>
        <artifactId>line-bot-spring-boot-webmvc</artifactId>
    </dependency>
</dependencies>
```

## 程式碼修改總結

### 1. LineUserServiceImpl.java

**修改重點**:
- `LineMessagingClient` → `MessagingApiClient`
- API 呼叫改為 `.get().body()`
- `pictureUrl()` 返回 `URI`，需轉換為 `String`
- 異常改為 `InterruptedException | ExecutionException`

```java
// 建立 Client
MessagingApiClient client = MessagingApiClient.builder(config.getChannelAccessToken()).build();

// 取得使用者資料
UserProfileResponse profile = client.getProfile(lineUserId).get().body();
user.setLineDisplayName(profile.displayName());
user.setLinePictureUrl(profile.pictureUrl() != null ? profile.pictureUrl().toString() : null);
user.setLineStatusMessage(profile.statusMessage());
```

### 2. LineConfigServiceImpl.java

**修改重點**:
- `getBotInfo()` 改為同步呼叫
- `TestWebhookEndpointRequest` 使用新建構子

```java
// 測試連線
var botInfo = client.getBotInfo().get().body();

// 測試 Webhook
TestWebhookEndpointRequest request = new TestWebhookEndpointRequest(
    URI.create(config.getWebhookUrl())
);
TestWebhookEndpointResponse testResult = client.testWebhookEndpoint(request).get().body();
```

### 3. LineMessageServiceImpl.java

**修改重點**:
- 所有 Request 使用建構子而非 Builder
- 訊息物件使用建構子
- `ImageMessage` 參數改為 `URI`
- API 呼叫需要 `UUID` 作為請求 ID

```java
// Push Message
PushMessageRequest request = new PushMessageRequest(
    pushMessageDTO.getTargetLineUserId(),
    Collections.singletonList(message),
    pushMessageDTO.getNotificationDisabled(),
    null  // customAggregationUnits
);
PushMessageResponse response = client.pushMessage(UUID.randomUUID(), request).get().body();

// Multicast
MulticastRequest request = new MulticastRequest(
    Collections.singletonList(message),
    new ArrayList<>(multicastMessageDTO.getTargetLineUserIds()),
    multicastMessageDTO.getNotificationDisabled(),
    null  // customAggregationUnits
);
client.multicast(UUID.randomUUID(), request).get();

// Broadcast
BroadcastRequest request = new BroadcastRequest(
    Collections.singletonList(message),
    broadcastMessageDTO.getNotificationDisabled()
);
client.broadcast(UUID.randomUUID(), request).get();

// Reply
ReplyMessageRequest request = new ReplyMessageRequest(
    replyMessageDTO.getReplyToken(),
    Collections.singletonList(message),
    replyMessageDTO.getNotificationDisabled()
);
ReplyMessageResponse response = client.replyMessage(request).get().body();

// 建立訊息
TextMessage textMessage = new TextMessage(text);
ImageMessage imageMessage = new ImageMessage(URI.create(imageUrl), URI.create(previewUrl));
```

### 4. LineWebhookController.java

**修改重點**:
- 需要匯入 `line-bot-spring-boot-handler` 的註解
- 保持使用 `@LineMessageHandler` 和 `@EventMapping`

```java
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.*;

@Slf4j
@LineMessageHandler
public class LineWebhookController {

    @Resource
    private ILineUserService lineUserService;

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String userId = event.source().userId();
        lineUserService.handleFollowEvent(userId);
        lineUserService.updateUserProfile(userId, null);
    }

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        String userId = event.source().userId();
        lineUserService.handleUnfollowEvent(userId);
    }

    @EventMapping
    public void handleMessageEvent(MessageEvent event) {
        String userId = event.source().userId();
        if (event.message() instanceof TextMessageContent) {
            String messageText = ((TextMessageContent) event.message()).text();
            // 處理文字訊息
        }
        lineUserService.incrementMessageCount(userId, false);
    }

    @EventMapping
    public void handleDefaultEvent(Event event) {
        log.info("收到事件: {}", event.getClass().getSimpleName());
    }
}
```

## API 變更對照表

| 舊版 API (6.x) | 新版 API (9.x) | 說明 |
|----------------|----------------|------|
| `LineMessagingClient` | `MessagingApiClient` | 核心 Client 類別 |
| `.getProfile(userId).get()` | `.getProfile(userId).get().body()` | 同步呼叫需要 `.body()` |
| `profile.getDisplayName()` | `profile.displayName()` | Record 風格的 getter |
| `profile.getPictureUrl()` | `profile.pictureUrl()` | 返回 `URI` 型別 |
| `PushMessageRequest.builder()` | `new PushMessageRequest()` | 使用建構子 |
| `MulticastRequest.builder()` | `new MulticastRequest()` | 使用建構子 |
| `BroadcastRequest.builder()` | `new BroadcastRequest()` | 使用建構子 |
| `ReplyMessageRequest.builder()` | `new ReplyMessageRequest()` | 使用建構子 |
| `TextMessage.builder()` | `new TextMessage(text)` | 使用建構子 |
| `ImageMessage.builder()` | `new ImageMessage(URI, URI)` | 參數改為 `URI` |
| `.pushMessage(request).get()` | `.pushMessage(UUID, request).get().body()` | 需要 UUID 和 .body() |
| `.multicast(request).get()` | `.multicast(UUID, request).get()` | 需要 UUID |
| `.broadcast(request).get()` | `.broadcast(UUID, request).get()` | 需要 UUID |
| `.replyMessage(request).get()` | `.replyMessage(request).get().body()` | 需要 .body() |
| `Exception` | `InterruptedException \| ExecutionException` | 異常類型 |

## Import 變更對照

### 移除的 Imports
```java
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.*;
import com.linecorp.bot.model.message.*;
```

### 新增的 Imports
```java
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.webhook.model.*;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
```

## 編譯結果

```
[INFO] Reactor Summary for cheng 3.9.0:
[INFO] 
[INFO] cheng .............................................. SUCCESS [  0.263 s]
[INFO] cheng-common ....................................... SUCCESS [  4.060 s]
[INFO] cheng-crawler ...................................... SUCCESS [  1.855 s]
[INFO] cheng-system ....................................... SUCCESS [  1.512 s]
[INFO] cheng-framework .................................... SUCCESS [  0.928 s]
[INFO] cheng-quartz ....................................... SUCCESS [  0.536 s]
[INFO] cheng-generator .................................... SUCCESS [  0.344 s]
[INFO] cheng-line ......................................... SUCCESS [  0.484 s]
[INFO] cheng-admin ........................................ SUCCESS [  0.924 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.078 s
[INFO] Finished at: 2025-10-28T00:36:25+08:00
```

## 下一步：測試計畫

1. **單元測試**
   - 測試使用者資料更新
   - 測試訊息發送功能
   - 測試 Webhook 事件處理

2. **整合測試**
   - 測試連線功能
   - 測試 Webhook 測試功能
   - 測試完整的訊息流程

3. **手動測試**
   - 在 LINE Developer Console 測試 Webhook
   - 測試實際的 LINE Bot 功能
   - 驗證推播、廣播、回覆功能

## 參考資源

- [LINE Bot SDK Java GitHub](https://github.com/line/line-bot-sdk-java)
- [Official Sample - Spring Boot Echo](https://github.com/line/line-bot-sdk-java/tree/master/samples/sample-spring-boot-echo)
- [LINE Messaging API Reference](https://developers.line.biz/en/reference/messaging-api/)

## 總結

✅ **升級成功完成！**

關鍵要點：
1. ✅ 新增了正確的依賴（特別是 `line-bot-spring-boot-handler`）
2. ✅ 修改了所有 API 呼叫方式
3. ✅ 處理了型別轉換（URI、Record getters）
4. ✅ 更新了異常處理
5. ✅ 保持了 `@LineMessageHandler` 和 `@EventMapping` 的使用方式
6. ✅ 編譯成功通過

現在可以開始進行功能測試！
