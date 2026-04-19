# LINE Bot 配置整合說明

## 整合目的

原先的 LINE Bot 配置分為兩個區塊：
- `line.bot.*` - LINE SDK 標準配置（必須存在）
- `line.main.*` - 主頻道擴充配置

由於 LINE SDK 的 Spring Boot 整合套件會自動讀取 `line.bot.*` 配置，因此將兩者整合可以：
1. **避免重複配置**：`channel-token` 和 `access-token` 實際上是同一個值
2. **簡化維護**：只需在一個地方配置，減少錯誤
3. **符合標準**：遵循 LINE SDK 的配置慣例

## 整合後的配置結構

### 配置檔案（application-*.yml）

```yaml
line:
  webhook:
    base-url: https://your-domain.com
  bot:
    # === LINE SDK 必要參數 ===
    channel-token: YOUR_ACCESS_TOKEN      # LINE SDK 必要（Access Token）
    channel-secret: YOUR_CHANNEL_SECRET   # LINE SDK 必要（Channel Secret）
    handler:
      path: /line/callback                # Webhook 處理路徑
    
    # === 擴充參數（主頻道設定）===
    enabled: true                         # 是否啟用主頻道自動載入
    channel-name: 主頻道名稱              # 頻道名稱
    channel-id: 1234567890                # Channel ID
    bot-basic-id: "@yourbot"              # Bot Basic ID（例如：@322okyxf）
```

### 各環境配置

#### 本地環境（application-local.yml）
```yaml
line:
  webhook:
    base-url: https://fbf91fadb09f.ngrok-free.app
  bot:
    channel-token: ENC(...)
    channel-secret: ENC(...)
    # 注意：專案使用自定義多頻道架構，不使用 SDK 自動配置的 handler
    enabled: true
    channel-name: Ava-OA
    channel-id: ENC(...)
    bot-basic-id: "@322okyxf"
```

#### 正式環境（application-prod.yml）
```yaml
line:
  webhook:
    base-url: ${LINE_WEBHOOK_BASE_URL:https://ap-domain.com}
  bot:
    channel-token: ${LINE_BOT_CHANNEL_TOKEN}
    channel-secret: ${LINE_BOT_CHANNEL_SECRET}
    # 注意：專案使用自定義多頻道架構，不使用 SDK 自動配置的 handler
    enabled: ${LINE_MAIN_ENABLED:false}
    channel-name: ${LINE_MAIN_CHANNEL_NAME:主頻道}
    channel-id: ${LINE_MAIN_CHANNEL_ID:}
    bot-basic-id: ${LINE_MAIN_BOT_BASIC_ID:}
```

#### VM 環境（application-vm.yml）
```yaml
line:
  webhook:
    base-url: ${LINE_WEBHOOK_BASE_URL:https://ap-domain.com}
  bot:
    channel-token: ${LINE_BOT_CHANNEL_TOKEN}
    channel-secret: ${LINE_BOT_CHANNEL_SECRET}
    # 注意：專案使用自定義多頻道架構，不使用 SDK 自動配置的 handler
    enabled: ${LINE_MAIN_ENABLED:false}
    channel-name: ${LINE_MAIN_CHANNEL_NAME:主頻道}
    channel-id: ${LINE_MAIN_CHANNEL_ID:}
    bot-basic-id: ${LINE_MAIN_BOT_BASIC_ID:}
```

## 程式碼修改

### LineMainChannelProperties.java

**修改前**：
```java
@ConfigurationProperties(prefix = "line.main")
public class LineMainChannelProperties {
    private String accessToken;
    // ...
}
```

**修改後**：
```java
@ConfigurationProperties(prefix = "line.bot")
public class LineMainChannelProperties {
    private String channelToken;  // 對應 line.bot.channel-token
    
    // 提供相容方法
    public String getAccessToken() {
        return channelToken;
    }
    // ...
}
```

### 配置參數對應表

| 整合前 | 整合後 | 說明 |
|--------|--------|------|
| `line.bot.channel-token` | `line.bot.channel-token` | LINE SDK 必要參數（Access Token） |
| `line.bot.channel-secret` | `line.bot.channel-secret` | LINE SDK 必要參數（Channel Secret） |
| `line.main.access-token` | ~~移除~~ | 與 `channel-token` 重複 |
| `line.main.channel-secret` | ~~移除~~ | 與 `channel-secret` 重複 |
| `line.main.enabled` | `line.bot.enabled` | 擴充參數 |
| `line.main.channel-name` | `line.bot.channel-name` | 擴充參數 |
| `line.main.channel-id` | `line.bot.channel-id` | 擴充參數 |
| `line.main.bot-basic-id` | `line.bot.bot-basic-id` | 擴充參數 |

## 相容性說明

- **向後相容**：`LineMainChannelProperties` 提供了 `getAccessToken()` 方法，內部調用 `channelToken`，確保舊有程式碼正常運作
- **LINE SDK 相容**：`line.bot.channel-token` 和 `line.bot.channel-secret` 會被 LINE SDK 自動讀取
- **環境變數支援**：正式環境和 VM 環境支援使用環境變數覆蓋配置

## 使用方式

### 本地開發

1. 在 `application-local.yml` 中配置完整的 `line.bot.*` 參數
2. 使用 Jasypt 加密敏感資訊
3. 啟動時加上解密密碼：
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password=diDsd]3FsGO@4dido
   ```

### 正式部署

設定以下環境變數：
```bash
LINE_BOT_CHANNEL_TOKEN=your_access_token
LINE_BOT_CHANNEL_SECRET=your_channel_secret
LINE_WEBHOOK_BASE_URL=https://your-domain.com
LINE_MAIN_ENABLED=true
LINE_MAIN_CHANNEL_NAME=主頻道
LINE_MAIN_CHANNEL_ID=1234567890
LINE_MAIN_BOT_BASIC_ID=@yourbot
```

## Webhook 路徑說明

### 專案使用自定義多頻道架構

本專案**不使用** LINE SDK 自動配置的 Webhook Handler (`line.bot.handler.path`)，而是實作了自定義的多頻道路由：

**實際 Webhook URL 格式**：
```
{base-url}/webhook/line/{botBasicId}
```

**範例**：
- 本地環境：`https://fbf91fadb09f.ngrok-free.app/webhook/line/@322okyxf`
- 正式環境：`https://ap-domain.com/webhook/line/@322okyxf`

**對應的 Controller**：
```java
@RequestMapping("/webhook/line")
public class LineWebhookController {
    @PostMapping("/{botBasicId}")
    public ResponseEntity<String> handleWebhook(
        @PathVariable("botBasicId") String botBasicId,
        @RequestHeader("X-Line-Signature") String signature,
        @RequestBody String payload) {
        // 自定義多頻道處理邏輯
    }
}
```

### 為什麼使用自定義架構？

1. **多頻道支援**：支援動態路由，一個應用可以處理多個 LINE Bot 頻道
2. **靈活性**：可以根據 `botBasicId` 動態載入對應的頻道配置
3. **擴充性**：便於新增自定義的驗證、記錄、錯誤處理邏輯

## 注意事項

1. **必要參數**：
   - `line.bot.channel-token` 和 `line.bot.channel-secret` 為 LINE SDK 必要參數，必須設定
   - 若要啟用主頻道自動載入，還需設定所有擴充參數

2. **加密配置**：
   - 本地環境使用 `ENC(...)` 包裹加密內容
   - 正式環境使用環境變數傳遞敏感資訊

3. **Webhook 設定**：
   - 在 LINE Developers Console 設定 Webhook URL 時，請使用：`{base-url}/webhook/line/{botBasicId}`
   - 範例：`https://your-domain.com/webhook/line/@yourbot`
   - 注意：不是 `/line/callback`，該路徑僅供 LINE SDK 自動配置使用（本專案未啟用）

## 修改日期

2025-10-29

## 相關檔案

- `/cheng-admin/src/main/resources/application-local.yml`
- `/cheng-admin/src/main/resources/application-prod.yml`
- `/cheng-admin/src/main/resources/application-vm.yml`
- `/cheng-line/src/main/java/com/cheng/line/config/LineMainChannelProperties.java`
- `/cheng-line/src/main/java/com/cheng/line/config/LineMainChannelInitializer.java`
