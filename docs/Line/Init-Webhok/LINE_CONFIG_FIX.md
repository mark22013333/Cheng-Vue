# LINE 頻道設定頁面修正說明

## 修正日期
2025-10-29

## 問題描述

根據截圖發現以下問題：

1. **已存在頻道無法修改** - 顯示錯誤訊息「新增 LINE 頻道設定失敗，頻道類型已存在」
2. **Webhook 基礎 URL 沒有顯示** - 前端欄位存在但資料未正確綁定
3. **缺少自動設定 Webhook 功能** - 需要新增「設定 LINE Webhook URL」按鈕，呼叫 LINE API 自動設定

## 修正內容

### 1. 修正「頻道類型已存在」問題

**問題分析**：
- Controller 層在新增時會檢查 `checkChannelTypeUnique`
- 當編輯已存在的頻道時，前端會重新提交，但後端檢查邏輯將其視為新增而報錯

**修正方式**：
移除 Controller 層的重複檢查，因為 Service 層已經有完整的檢查邏輯。

**檔案**：`LineConfigController.java`

```java
// 修正前
@PostMapping
public AjaxResult add(@Validated @RequestBody LineConfig lineConfig) {
    if (!lineConfigService.checkChannelTypeUnique(lineConfig)) {
        return error("新增 LINE 頻道設定失敗，頻道類型已存在");
    }
    lineConfig.setCreateBy(getUsername());
    return toAjax(lineConfigService.insertLineConfig(lineConfig));
}

// 修正後
@PostMapping
public AjaxResult add(@Validated @RequestBody LineConfig lineConfig) {
    lineConfig.setCreateBy(getUsername());
    return toAjax(lineConfigService.insertLineConfig(lineConfig));
}
```

**說明**：
- Service 層的 `insertLineConfig` 方法已有完整的檢查邏輯
- Controller 不需要重複檢查，避免邏輯衝突

### 2. 確保 Webhook 基礎 URL 顯示

**問題分析**：
- 前端 `ConfigForm.vue` 已有 `webhookBaseUrl` 欄位（第84-96行）
- 後端 `LineConfig` 實體已有對應欄位
- 資料綁定正確，應該會自動顯示

**驗證方式**：
- 檢查資料庫 `sys_line_config` 表的 `webhook_base_url` 欄位
- 確認前端 form 綁定：`v-model="form.webhookBaseUrl"`

**檔案**：已存在，無需修改
- 前端：`ConfigForm.vue` (第84-96行)
- 後端：`LineConfig.java` (第79-85行)

### 3. 新增「設定 LINE Webhook URL」功能

#### 3.1 後端實作

**新增 Controller 端點**

**檔案**：`LineConfigController.java`

```java
/**
 * 設定 LINE Webhook URL
 * 呼叫 LINE Messaging API 設定 Webhook 端點
 *
 * @param configId 頻道設定ID
 * @return 結果
 */
@PreAuthorize("@ss.hasPermi('line:config:edit')")
@Log(title = "設定LINE Webhook", businessType = BusinessType.UPDATE)
@PostMapping("/setLineWebhook/{configId}")
public AjaxResult setLineWebhook(@PathVariable Integer configId) {
    try {
        String result = lineConfigService.setLineWebhookEndpoint(configId);
        return success(result);
    } catch (Exception e) {
        log.error("設定 LINE Webhook URL 失敗", e);
        return error("設定失敗：" + e.getMessage());
    }
}
```

**新增 Service 介面**

**檔案**：`ILineConfigService.java`

```java
/**
 * 設定 LINE Webhook 端點
 * 呼叫 LINE Messaging API 將 Webhook URL 設定到 LINE Platform
 *
 * @param configId 設定ID
 * @return 設定結果訊息
 */
String setLineWebhookEndpoint(Integer configId);
```

**實作 Service 方法**

**檔案**：`LineConfigServiceImpl.java`

```java
@Override
public String setLineWebhookEndpoint(Integer configId) {
    log.info("開始設定 LINE Webhook 端點，設定ID：{}", configId);

    // 1. 查詢頻道設定
    LineConfig config = selectLineConfigById(configId);
    if (config == null) {
        throw new ServiceException("找不到對應的頻道設定");
    }

    // 2. 檢查頻道是否啟用
    if (config.getStatus() != Status.ENABLE) {
        throw new ServiceException("頻道未啟用，無法設定 Webhook");
    }

    // 3. 取得 Webhook URL
    String webhookUrl = config.getWebhookUrl();
    if (StringUtils.isEmpty(webhookUrl)) {
        throw new ServiceException("Webhook URL 為空，請先儲存頻道設定");
    }

    // 4. 建立 MessagingApiClient
    String accessToken = JasyptUtils.decrypt(config.getChannelAccessToken());
    MessagingApiClient client = MessagingApiClient.builder(accessToken).build();

    try {
        // 5. 呼叫 LINE API 設定 Webhook URL
        // PUT https://api.line.me/v2/bot/channel/webhook/endpoint
        client.setWebhookEndpoint(URI.create(webhookUrl))
                .get();  // 同步等待結果

        log.info("設定 LINE Webhook 端點成功，URL：{}", webhookUrl);
        
        // 6. 更新資料庫狀態
        config.setWebhookStatus(Status.ENABLE);
        lineConfigMapper.updateLineConfig(config);

        return "Webhook URL 設定成功：" + webhookUrl;

    } catch (ExecutionException | InterruptedException e) {
        log.error("設定 LINE Webhook 端點失敗", e);
        
        // 更新資料庫狀態為失敗
        config.setWebhookStatus(Status.DISABLE);
        lineConfigMapper.updateLineConfig(config);
        
        String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        throw new ServiceException("設定 Webhook 失敗：" + errorMsg);
    }
}
```

**API 參考**：
- [LINE Messaging API - Set webhook endpoint URL](https://developers.line.biz/en/reference/messaging-api/#set-webhook-endpoint-url)
- Method: `PUT /v2/bot/channel/webhook/endpoint`

#### 3.2 前端實作

**新增 API 呼叫**

**檔案**：`src/api/line/config.js`

```javascript
// 設定 LINE Webhook URL
export function setLineWebhook(configId) {
  return request({
    url: '/line/config/setLineWebhook/' + configId,
    method: 'post'
  })
}
```

**修改表單元件**

**檔案**：`ConfigForm.vue`

1. **新增按鈕**（第106-121行）：

```vue
<el-button
  v-if="!isAdd && form.configId"
  type="success"
  size="small"
  icon="el-icon-link"
  :loading="settingWebhook"
  @click="handleSetLineWebhook"
  style="margin-top: 10px;"
>
  設定 LINE Webhook URL
</el-button>
<div class="form-tip" v-if="!isAdd">
  <i class="el-icon-warning"></i>
  點擊按鈕將自動呼叫 LINE API 設定 Webhook URL，無需手動至 LINE Developer Console 設定
</div>
```

2. **新增狀態變數**（第180行）：

```javascript
data() {
  return {
    // ...
    settingWebhook: false,  // 設定 Webhook 中
    // ...
  }
}
```

3. **新增處理方法**（第416-442行）：

```javascript
/** 設定 LINE Webhook URL */
handleSetLineWebhook() {
  if (!this.form.configId) {
    this.$modal.msgWarning('請先儲存頻道設定')
    return
  }

  this.$confirm(
    '即將呼叫 LINE API 設定 Webhook URL，確定要繼續嗎？',
    '確認',
    {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    this.settingWebhook = true
    setLineWebhook(this.form.configId).then(response => {
      this.$modal.msgSuccess(response.msg || 'Webhook URL 設定成功')
      this.$emit('success')
    }).catch(error => {
      this.$modal.msgError('設定失敗：' + (error.msg || error.message || '未知錯誤'))
    }).finally(() => {
      this.settingWebhook = false
    })
  })
}
```

4. **匯入 API**（第164行）：

```javascript
import { getConfig, addConfig, updateConfig, checkChannelType, setLineWebhook } from '@/api/line/config'
```

## 使用方式

### 1. 編輯頻道設定

1. 進入「LINE 頻道設定」頁面
2. 點擊現有頻道的「設定」按鈕
3. 修改頻道資訊
4. 點擊「確定」儲存

**注意**：現在可以正常編輯已存在的頻道，不會再出現「頻道類型已存在」錯誤。

### 2. 查看 Webhook 基礎 URL

1. 開啟頻道編輯對話框
2. 在「Webhook 設定」區塊可以看到「Webhook 基礎 URL」欄位
3. 可以自訂 Webhook 基礎 URL，留空則使用系統預設值

### 3. 設定 LINE Webhook URL

#### 前置條件：
- 頻道已儲存（有 configId）
- 頻道狀態為「啟用」
- 已設定 Channel Access Token

#### 操作步驟：
1. 開啟頻道編輯對話框
2. 在「Webhook URL」欄位下方，點擊「設定 LINE Webhook URL」按鈕
3. 確認提示訊息
4. 系統將自動呼叫 LINE Messaging API 設定 Webhook
5. 設定成功後會顯示成功訊息

#### API 呼叫流程：
```
前端按鈕點擊
  ↓
POST /line/config/setLineWebhook/{configId}
  ↓
後端 LineConfigService.setLineWebhookEndpoint()
  ↓
呼叫 LINE API: PUT /v2/bot/channel/webhook/endpoint
  ↓
更新資料庫 webhook_status
  ↓
返回結果給前端
```

## 注意事項

1. **權限控制**：
   - 設定 Webhook 需要 `line:config:edit` 權限
   - 確保使用者有對應權限

2. **Channel Access Token**：
   - 必須使用有效的長期 Channel Access Token
   - Token 會使用 Jasypt 解密後再使用

3. **Webhook URL 格式**：
   - 自動產生格式：`{baseUrl}/webhook/line/{botBasicId}`
   - baseUrl 優先使用頻道自訂的 `webhookBaseUrl`
   - 若未設定則使用系統配置 `line.webhook.base-url`

4. **錯誤處理**：
   - 設定失敗會更新 `webhook_status` 為 DISABLE
   - 前端顯示詳細錯誤訊息
   - 後端記錄完整錯誤日誌

## 測試建議

### 1. 測試頻道編輯功能

```bash
# 測試場景：編輯已存在的主頻道
1. 進入頻道設定頁面
2. 點擊主頻道的「設定」按鈕
3. 修改頻道名稱或其他資訊
4. 點擊「確定」
5. 預期結果：儲存成功，不出現「頻道類型已存在」錯誤
```

### 2. 測試 Webhook 基礎 URL 顯示

```bash
# 測試場景：查看和修改 Webhook 基礎 URL
1. 開啟頻道編輯對話框
2. 檢查「Webhook 基礎 URL」欄位是否顯示
3. 輸入自訂值（如：your-domain.com）
4. 儲存後重新開啟，確認值已儲存
5. 檢查「Webhook URL」是否使用自訂的 baseUrl
```

### 3. 測試設定 LINE Webhook

```bash
# 測試場景：成功設定 Webhook
1. 確保頻道已啟用且有有效的 Access Token
2. 開啟頻道編輯對話框
3. 點擊「設定 LINE Webhook URL」按鈕
4. 確認提示訊息
5. 預期結果：
   - 顯示成功訊息
   - 資料庫 webhook_status 更新為 ENABLE
   - LINE Developer Console 的 Webhook URL 已自動設定

# 測試場景：設定失敗處理
1. 使用無效的 Access Token
2. 點擊「設定 LINE Webhook URL」按鈕
3. 預期結果：
   - 顯示錯誤訊息
   - 資料庫 webhook_status 更新為 DISABLE
   - 後端日誌記錄錯誤詳情
```

## 相關檔案

### 後端
- `/cheng-admin/src/main/java/com/cheng/web/controller/line/LineConfigController.java`
- `/cheng-line/src/main/java/com/cheng/line/service/ILineConfigService.java`
- `/cheng-line/src/main/java/com/cheng/line/service/impl/LineConfigServiceImpl.java`
- `/cheng-line/src/main/java/com/cheng/line/domain/LineConfig.java`

### 前端
- `/cheng-ui/src/api/line/config.js`
- `/cheng-ui/src/views/line/config/components/ConfigForm.vue`

## 參考資料

- [LINE Messaging API Reference](https://developers.line.biz/en/reference/messaging-api/)
- [Set webhook endpoint URL](https://developers.line.biz/en/reference/messaging-api/#set-webhook-endpoint-url)
- [LINE Bot SDK for Java](https://github.com/line/line-bot-sdk-java)
