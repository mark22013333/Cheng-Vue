# LINE 頻道設定頁面重構

## 修改日期
2025-10-29

## 修改目的
重新設計 LINE 頻道設定頁面，提供更直觀的三頻道卡片式介面，並修正測試連線功能的實作問題。

---

## 主要修改內容

### 1. 後端修正 - 真正調用 Webhook Test API

**檔案**：`cheng-line/src/main/java/com/cheng/line/service/impl/LineConfigServiceImpl.java`

**問題**：
- 原本的 `testLineConnection` 方法在 Webhook 檢查步驟中，只檢查 URL 是否存在
- 並沒有真正調用 LINE 的 `testWebhookEndpoint` API 進行測試

**修正**：
```java
// 2. Webhook 設定檢查（真正測試 Webhook 端點）
if (StringUtils.isEmpty(config.getWebhookUrl())) {
    webhookResult = com.cheng.line.vo.ConnectionTestVO.TestItemVO.builder()
            .success(false)
            .message("Webhook URL 未設定")
            .build();
} else {
    try {
        // 呼叫 LINE API 測試 Webhook 端點
        TestWebhookEndpointRequest webhookRequest = new TestWebhookEndpointRequest(
                URI.create(config.getWebhookUrl())
        );
        TestWebhookEndpointResponse testResult = client.testWebhookEndpoint(webhookRequest).get().body();
        
        if (testResult.success()) {
            webhookResult = com.cheng.line.vo.ConnectionTestVO.TestItemVO.builder()
                    .success(true)
                    .message(String.format("Webhook URL 已設定：%s", config.getWebhookUrl()))
                    .build();
            
            log.info("Webhook 測試成功：url={}, statusCode={}", 
                    config.getWebhookUrl(), testResult.statusCode());
        } else {
            webhookResult = com.cheng.line.vo.ConnectionTestVO.TestItemVO.builder()
                    .success(false)
                    .message(String.format("Webhook 測試失敗：%s - %s", 
                            testResult.reason(), testResult.detail()))
                    .build();
            
            log.warn("Webhook 測試失敗：reason={}, detail={}", 
                    testResult.reason(), testResult.detail());
        }
    } catch (Exception e) {
        webhookResult = com.cheng.line.vo.ConnectionTestVO.TestItemVO.builder()
                .success(false)
                .message("Webhook 測試失敗：" + e.getMessage())
                .build();
        
        log.error("Webhook 測試發生錯誤", e);
    }
}
```

**效果**：
- 現在會真正呼叫 LINE Messaging API 測試 Webhook 端點
- 可以在 LOG 中看到完整的測試結果（成功/失敗、狀態碼、錯誤原因等）
- 測試結果更準確，能真實反映 Webhook 設定狀態

---

### 2. ConfigForm.vue - 移除頻道類型選擇

**檔案**：`cheng-ui/src/views/line/config/components/ConfigForm.vue`

**修改內容**：

1. **移除頻道類型下拉選單**：
   - 原本的頻道類型選擇下拉選單已移除
   - 頻道類型改為從外部傳入（由卡片決定）

2. **修改 `open` 方法參數**：
   ```javascript
   open(configId, channelType = 'SUB') {
     this.reset()
     
     // 取得系統預設的 Webhook 基礎 URL
     getDefaultWebhookBaseUrl().then(response => {
       this.defaultWebhookBaseUrl = response.data || ''
     })
     
     if (configId) {
       this.isAdd = false
       this.title = '修改頻道設定'
       getConfig(configId).then(response => {
         this.form = {
           ...response.data,
           isDefault: response.data.isDefault === 'YES'
         }
         this.webhookUrl = response.data.webhookUrl || ''
       })
     } else {
       this.isAdd = true
       this.title = '新增頻道設定'
       // 設定頻道類型（從外部傳入）
       this.form.channelType = channelType
     }
     this.dialogVisible = true
   }
   ```

3. **移除頻道類型切換監聽**：
   - 移除 `watch` 中的 `form.channelType` 監聽
   - 不再需要 `checkExistingChannel` 檢查邏輯

---

### 3. index.vue - 重新設計卡片介面

**檔案**：`cheng-ui/src/views/line/config/index.vue`

#### 3.1 移除不必要的元件

- **移除搜尋區域**：因為只有三張固定卡片，不需要搜尋功能
- **移除「新增頻道」按鈕**：改為每張卡片上有「立即設定」按鈕

#### 3.2 固定三張頻道卡片

**資料結構**：
```javascript
data() {
  return {
    loading: false,
    channels: [
      {
        type: 'MAIN',
        label: '主頻道',
        icon: 'el-icon-star-on',
        color: '#E6A23C',
        description: '系統預設使用的主要 LINE 頻道',
        config: null
      },
      {
        type: 'SUB',
        label: '副頻道',
        icon: 'el-icon-chat-line-square',
        color: '#409EFF',
        description: '備用頻道，可用於分流或測試',
        config: null
      },
      {
        type: 'TEST',
        label: '測試頻道',
        icon: 'el-icon-data-analysis',
        color: '#909399',
        description: '開發測試專用頻道，不影響正式環境',
        config: null
      }
    ]
  }
}
```

#### 3.3 資料填充邏輯

```javascript
getList() {
  this.loading = true
  listConfig({ pageNum: 1, pageSize: 100 }).then(response => {
    const configs = response.rows || []
    
    // 重置所有頻道的 config
    this.channels.forEach(channel => {
      channel.config = null
    })
    
    // 將查詢到的配置填充到對應的頻道
    configs.forEach(config => {
      const channel = this.channels.find(ch => ch.type === config.channelType)
      if (channel) {
        channel.config = config
      }
    })
    
    this.loading = false
  }).catch(() => {
    this.loading = false
  })
}
```

#### 3.4 卡片設計

**已設定的頻道卡片**：
- 顯示頻道名稱、Bot ID、Channel ID
- 顯示 Webhook 驗證狀態
- 提供「測試連線」和「設定」按鈕
- 下拉選單提供「編輯」、「測試連線」、「刪除」功能

**未設定的頻道卡片（空狀態）**：
- 顯示「尚未設定此頻道」提示
- 顯示頻道用途說明
- 提供「立即設定」按鈕

#### 3.5 顏色區分

**主頻道（MAIN）**：
- 邊框顏色：金黃色 `#E6A23C`
- 標題背景：金黃色漸層 `#FDF6EC → #FFF`
- 圖示：星星圖示

**副頻道（SUB）**：
- 邊框顏色：藍色 `#409EFF`
- 標題背景：藍色漸層 `#ECF5FF → #FFF`
- 圖示：對話框圖示

**測試頻道（TEST）**：
- 邊框顏色：灰色 `#909399`
- 標題背景：灰色漸層 `#F4F4F5 → #FFF`
- 圖示：數據分析圖示

#### 3.6 卡片互動效果

- **Hover 效果**：卡片上移 4px，增加陰影
- **空狀態動畫**：圖示呼吸脈衝動畫
- **過渡動畫**：所有狀態變化都有平滑過渡

---

## 功能改進總結

### 1. 更直觀的介面
- 固定顯示三張卡片，一目了然
- 用顏色區分不同頻道類型
- 空狀態卡片引導用戶設定

### 2. 更準確的測試
- 真正調用 LINE API 測試 Webhook
- 測試結果更可靠
- LOG 記錄更詳細

### 3. 更流暢的操作
- 移除不必要的頻道類型選擇
- 直接從卡片點擊設定對應頻道
- 減少操作步驟

### 4. 更美觀的設計
- 現代化的卡片佈局
- 清晰的視覺層次
- 平滑的動畫效果

---

## 使用方式

### 設定頻道
1. 點擊對應頻道卡片的「立即設定」或「設定」按鈕
2. 填寫頻道資訊（頻道類型已自動設定）
3. 儲存設定

### 測試連線
1. 點擊「測試連線」按鈕
2. 系統會執行三項測試：
   - API 連線測試（Channel Access Token 驗證）
   - Webhook 設定測試（真實呼叫 LINE API）
   - Bot 資訊取得
3. 查看測試結果

### 編輯/刪除頻道
1. 點擊卡片右上角的「⋮」按鈕
2. 選擇「編輯」或「刪除」

---

## 技術細節

### 前端技術
- Vue.js 2.x
- Element UI
- SCSS

### 後端技術
- Spring Boot
- LINE Messaging API SDK
- MyBatis

### 關鍵 API
- `GET /line/config/list` - 查詢頻道列表
- `POST /line/config/testConnection/{configId}` - 測試連線（含 Webhook 測試）
- LINE Messaging API - `testWebhookEndpoint` - 測試 Webhook 端點

---

## 注意事項

1. **Webhook 測試需求**：
   - Webhook URL 必須是公開可存取的 HTTPS 網址
   - LINE 會實際發送請求到 Webhook URL 進行測試
   - 如果本地開發環境無法接收外部請求，Webhook 測試會失敗

2. **頻道類型限制**：
   - 每種類型只能有一個頻道
   - 刪除頻道後可以重新設定相同類型

3. **敏感資料保護**：
   - Channel ID、Channel Secret、Access Token 都會加密儲存
   - 列表顯示時會脫敏處理

---

## 測試建議

1. **測試三種頻道類型的設定**：
   - 分別設定主頻道、副頻道、測試頻道
   - 驗證每個頻道的顏色和樣式是否正確

2. **測試空狀態**：
   - 確認未設定的頻道顯示空狀態卡片
   - 點擊「立即設定」能正確開啟表單

3. **測試連線功能**：
   - 檢查 LOG 是否有 Webhook 測試的記錄
   - 驗證測試結果是否準確

4. **測試編輯功能**：
   - 編輯已設定的頻道，確認資料正確載入
   - 頻道類型不應該可以修改

---

## 後續修正（2025-10-30）

### 修正問題

1. **Webhook 狀態未更新**：
   - 測試連線完成後，新增事件通知父組件重新整理列表
   - `ConnectionTest.vue` 新增 `@test-complete` 事件
   - `index.vue` 監聽事件並重新載入資料

2. **狀態欄位載入問題**：
   - 修正前後端 status 值的轉換邏輯
   - 後端：`ENABLE=1`, `DISABLE=0`
   - 前端：`'0'=啟用`, `'1'=停用`
   - 新增雙向轉換：載入時和提交時都正確轉換

3. **Webhook 基礎 URL 顯示**：
   - 改為在提示訊息中顯示系統預設值
   - 避免 placeholder 動態載入的延遲問題

4. **Webhook URL 雙重斜線**：
   - 修正 URL 組裝邏輯，確保沒有雙重斜線
   - 使用固定的 webhook 路徑：`webhook/line`
   - 確保 baseUrl 結尾沒有斜線

5. **卡片狀態顯示**：
   - 新增 `isEnabled()` 方法正確判斷 Status 枚舉
   - 統一處理執行狀態和 Webhook 狀態的顯示

### 關鍵程式碼

#### Status 轉換邏輯

```javascript
// 載入時：後端 -> 前端
let statusValue = '0' // 預設啟用
if (response.data.status) {
  const statusCode = response.data.status.code !== undefined 
    ? response.data.status.code 
    : response.data.status
  // 後端 1(ENABLE) -> 前端 '0'(啟用)
  // 後端 0(DISABLE) -> 前端 '1'(停用)
  statusValue = statusCode === 1 ? '0' : '1'
}

// 提交時：前端 -> 後端
const statusCode = this.form.status === '0' ? 1 : 0
```

#### Webhook URL 組裝

```javascript
// 移除結尾的斜線
if (baseUrl.endsWith('/')) {
  baseUrl = baseUrl.substring(0, baseUrl.length - 1)
}

// 組裝 Webhook URL，確保沒有雙重斜線
const webhookPath = 'webhook/line'
this.webhookUrl = `${baseUrl}/${webhookPath}/${pathParam}`
```

---

## 加密/解密邏輯修正（2025-10-30）

### 問題：資料庫明碼導致解密失敗

**現象**：
- 資料庫中存在明碼資料（未加密）
- 程式讀取時嘗試解密明碼，拋出 `EncryptionOperationNotPossibleException`
- 系統無法正常執行

**原因分析**：
1. 資料可能是直接用 SQL 新增的測試資料
2. 或在沒有正確設定 Jasypt 密碼時新增的
3. 原有的解密邏輯沒有判斷資料是否已加密，直接解密明碼會失敗

**修正方案**：

1. **`decryptSensitiveData` 修正**：
   - 嘗試解密每個欄位
   - 如果解密失敗（拋出異常），記錄警告但**不拋出異常**
   - 保留明碼資料，讓系統可以繼續執行
   - 提示用戶重新儲存以加密

2. **`encryptSensitiveData` 修正**：
   - 新增 `isEncrypted()` 輔助方法
   - 透過嘗試解密來判斷資料是否已加密
   - 只加密未加密的資料，避免重複加密

3. **自動加密明碼資料**：
   - 用戶透過前端重新儲存頻道設定時
   - 系統會自動加密明碼資料並更新到資料庫

**關鍵程式碼**：

```java
// 判斷是否已加密
private boolean isEncrypted(String key, String value) {
    try {
        JasyptUtils.decryptVal(key, value);
        return true;  // 解密成功，說明是加密資料
    } catch (Exception e) {
        return false;  // 解密失敗，說明是明碼
    }
}

// 解密時容錯處理
if (StringUtils.isNotEmpty(config.getChannelId())) {
    try {
        config.setChannelId(JasyptUtils.decryptVal(key, config.getChannelId()));
    } catch (Exception e) {
        log.warn("Channel ID 解密失敗，可能是明碼資料（configId={}），建議重新儲存以加密", config.getConfigId());
        // 不拋出異常，保留明碼資料
    }
}
```

**使用建議**：
1. 確保啟動時設定 Jasypt 密碼：`-Djasypt.encryptor.password=diDsd]3FsGO@4dido`
2. 對於資料庫中的明碼資料，透過前端重新儲存即可自動加密
3. 檢查 LOG 中的警告訊息，找出哪些資料是明碼

---

## 主頻道啟動邏輯修正（2025-10-30）

### 問題：啟動時強制覆蓋主頻道

**現象**：
- 每次服務啟動，都會用配置檔案的設定覆蓋資料庫的主頻道
- 後台管理者修改主頻道後，重啟服務會被重置
- 造成管理混亂

**原有邏輯**：
```java
if (existingConfig != null) {
    // 存在就更新 - 這會覆蓋後台的設定 ❌
    updateMainChannel(existingConfig);
} else {
    // 不存在就建立
    createMainChannel();
}
```

**修正邏輯**：
```java
// 檢查資料庫是否已存在主頻道
LineConfig mainChannel = lineConfigService.selectLineConfigByChannelType(ChannelType.MAIN);

if (mainChannel != null) {
    // 已存在，跳過自動配置 ✅
    log.info("資料庫已存在主頻道設定，跳過自動配置以避免覆蓋後台管理者的設定");
    log.info("如需使用配置檔案的設定，請先在後台刪除現有的主頻道，再重新啟動服務");
} else {
    // 不存在，從配置檔案建立 ✅
    createMainChannel();
}
```

**改進效果**：
1. ✅ **首次啟動**：資料庫沒有主頻道 → 從配置檔案建立
2. ✅ **後續啟動**：資料庫已有主頻道 → 跳過，保留後台設定
3. ✅ **重新初始化**：刪除主頻道 → 重啟服務 → 從配置檔案重建

**使用建議**：
- 初次部署時，配置檔案設定主頻道的基本資訊
- 服務啟動後，透過後台管理主頻道設定
- 如需重新使用配置檔案的設定，先在後台刪除主頻道再重啟

**移除的方法**：
- `updateMainChannel()` - 不再需要自動更新主頻道

---

## Webhook 狀態更新與保存修正（2025-10-30）

### 問題 1：測試連線成功後，Webhook 狀態未更新

**現象**：
- 點擊「測試連線」成功
- 卡片上的 Webhook 狀態仍顯示「未驗證」

**原因**：
- `testLineConnection()` 方法只返回測試結果
- 沒有更新資料庫的 `webhook_status` 欄位

**修正**：
```java
// 測試完成後，根據 Webhook 測試結果更新資料庫狀態
if (webhookResult.isSuccess()) {
    lineConfigMapper.updateWebhookStatus(configId, Status.ENABLE.getCode());
    log.info("Webhook 測試成功，已更新資料庫狀態為已驗證（configId={}）", configId);
} else {
    lineConfigMapper.updateWebhookStatus(configId, Status.DISABLE.getCode());
    log.warn("Webhook 測試失敗，已更新資料庫狀態為未驗證（configId={}）", configId);
}
```

**新增方法**：
- `LineConfigMapper.updateWebhookStatus()` - 單獨更新 Webhook 狀態
- SQL：直接更新 `webhook_status` 欄位

---

### 問題 2：修改設定後，Webhook 狀態被重置

**現象**：
- 測試連線成功後，Webhook 狀態為「已驗證」
- 修改其他欄位（如啟用狀態）並儲存
- Webhook 狀態被重置為「未驗證」

**原因**：
- `updateLineConfig()` 方法每次都會重置 `webhookStatus` 為 `DISABLE`
- 不管 Webhook URL 是否真的改變

**修正**：
```java
// 重新產生 Webhook URL
String webhookUrl = generateWebhookUrl(lineConfig);

// 只有在 Webhook URL 改變時，才重置驗證狀態
if (!webhookUrl.equals(oldConfig.getWebhookUrl())) {
    lineConfig.setWebhookStatus(Status.DISABLE);
    log.info("Webhook URL 已變更，重置驗證狀態為未驗證（configId={}）", lineConfig.getConfigId());
}
// 否則保留原有的驗證狀態（不覆蓋）
```

**邏輯改進**：
1. ✅ **Webhook URL 未變**：保留原有驗證狀態
2. ✅ **Webhook URL 改變**：重置為未驗證（需要重新測試）
3. ✅ **測試成功**：立即更新狀態為已驗證

---

## 相關檔案

### 前端
- `/cheng-ui/src/views/line/config/index.vue`
- `/cheng-ui/src/views/line/config/components/ConfigForm.vue`
- `/cheng-ui/src/views/line/config/components/ConnectionTest.vue`
- `/cheng-ui/src/api/line/config.js`

### 後端
- `/cheng-line/src/main/java/com/cheng/line/service/impl/LineConfigServiceImpl.java`
- `/cheng-line/src/main/java/com/cheng/line/service/ILineConfigService.java`
- `/cheng-admin/src/main/java/com/cheng/web/controller/line/LineConfigController.java`
- `/cheng-line/src/main/java/com/cheng/line/config/LineMainChannelInitializer.java`
