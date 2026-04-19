# LINE Webhook 架構說明

## 架構概述

本專案採用**前後端分離架構**，前端使用 Vue.js，後端使用 Spring Boot。LINE Webhook 請求需要透過 Nginx 反向代理轉發到後端處理。

## Webhook 路徑設計

### 完整 Webhook URL 格式
```
https://{domain}/webhook/line/{botBasicId}
```

### 路徑組成說明
1. **網域 (domain)**: 前端網域，例如 `ap-domain.com`
2. **Webhook 路徑**: `/webhook/line/`
3. **Bot ID**: LINE Bot 的 Basic ID，例如 `322okyxf`

**注意**: 本專案所有環境的 `context-path` 都是 `/`（根路徑），因此 URL 中不包含額外的應用程式路徑。

### 實際範例
- **本地環境**: `https://xxxx.ngrok-free.app/webhook/line/322okyxf`
- **VM/正式環境**: `https://ap-domain.com/webhook/line/322okyxf`

## 配置檔案說明

### 1. 後端配置 (application.yml / application-prod.yml)

```yaml
# 伺服器設定（所有環境統一使用根路徑）
server:
  servlet:
    # 應用程式的 context-path（所有環境都是根路徑）
    context-path: /

# LINE 設定
line:
  webhook:
    # Webhook Base URL（網域名稱）
    # 因為 context-path 是 /，所以直接使用網域即可
    base-url: https://ap-domain.com
```

**重要**：
- 所有環境的 `context-path` 都是 `/`（根路徑）
- `base-url` 只需填寫網域名稱
- 系統會自動組成完整路徑：`{base-url}/webhook/line/{botId}`

### 2. Nginx 配置 (proxy-ssl-corrected.conf / proxy-ssl.conf)

```nginx
# LINE Webhook 處理 - 轉發到後端（優先順序最高）
location /webhook/line/ {
    proxy_pass http://127.0.0.1:8080/webhook/line/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # LINE Webhook 重要設定
    proxy_pass_request_headers on;
    proxy_set_header X-Line-Signature $http_x_line_signature;
    
    # Webhook 逾時設定
    proxy_connect_timeout 10s;
    proxy_send_timeout 10s;
    proxy_read_timeout 10s;
    
    # 記錄 Webhook 請求
    access_log /var/log/nginx/line_webhook_access.log;
    error_log /var/log/nginx/line_webhook_error.log;
}
```

**關鍵點**：
- `location /webhook/line/` 攔截所有 webhook 請求
- `proxy_pass http://127.0.0.1:8080/webhook/line/` 轉發到後端（因為 context-path 是 `/`，所以直接轉發）
- `X-Line-Signature` 標頭必須傳遞，用於驗證請求來源

### 3. 後端 LineProperties.java

```java
@Value("${server.servlet.context-path:/}")
private String contextPath;

public String getWebhookBaseUrl() {
    String baseUrl = webhook.getBaseUrl();
    
    // 移除結尾的斜線
    if (baseUrl.endsWith("/")) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    
    // 如果 context-path 不是根路徑，則加上 context-path
    if (contextPath != null && !contextPath.equals("/")) {
        String path = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return baseUrl + path;
    }
    
    return baseUrl;
}
```

**功能**：
- 自動讀取 `server.servlet.context-path` 配置
- 將 context-path 附加到 base URL 後方
- 前端會從 API 取得完整的 webhook base URL

## 請求流程

```
LINE 平台
  ↓
https://ap-domain.com/webhook/line/322okyxf
  ↓
Nginx (443)
  ↓ location /webhook/line/
  ↓ proxy_pass http://127.0.0.1:8080/webhook/line/
  ↓
Spring Boot (8080)
  ↓ /webhook/line/322okyxf (context-path: /)
  ↓
LineWebhookController
  ↓
處理訊息
```

## 環境差異

### 所有環境統一配置

**本地環境 (application-local.yml)**:
- **Context Path**: `/` (根路徑)
- **Webhook Base URL**: `https://xxxx.ngrok-free.app`
- **完整 Webhook**: `https://xxxx.ngrok-free.app/webhook/line/{botId}`

**VM/PROD 環境 (application-prod.yml)**:
- **Context Path**: `/` (根路徑)
- **Webhook Base URL**: `https://ap-domain.com`
- **完整 Webhook**: `https://ap-domain.com/webhook/line/{botId}`

**請求流程**：
- **填入 LINE Console**: `https://ap-domain.com/webhook/line/{botId}`
- **Nginx 接收**: `https://ap-domain.com/webhook/line/{botId}`
- **Nginx 轉發**: `http://localhost:8080/webhook/line/{botId}`
- **Spring Boot 處理**: `/webhook/line/{botId}` (context-path 是 `/`)

## 部署檢查清單

### 1. 後端配置檢查
- [ ] `application.yml` 中 `server.servlet.context-path` 設定為 `/`（根路徑）
- [ ] `line.webhook.base-url` 設定為正確的網域
- [ ] `LineProperties.java` 正確讀取 context-path（會自動處理根路徑的情況）

### 2. Nginx 配置檢查
- [ ] `/webhook/line/` location 配置已加入
- [ ] `proxy_pass` 正確指向 `http://127.0.0.1:8080/webhook/line/`（因為 context-path 是 `/`）
- [ ] `X-Line-Signature` 標頭已傳遞
- [ ] Webhook 日誌檔案已配置

### 3. 防火牆/安全群組檢查
- [ ] HTTPS (443) 埠已開放
- [ ] SSL 憑證已正確配置
- [ ] LINE 平台的 IP 白名單（如有需要）

### 4. LINE Developer Console 設定
- [ ] Webhook URL 設定為 `https://ap-domain.com/webhook/line/{botBasicId}`
- [ ] Webhook 已啟用
- [ ] 使用系統內建的「設定 LINE Webhook URL」功能自動設定

## 測試方法

### 1. 測試 Nginx 轉發
```bash
# 測試 webhook 路徑是否正確轉發
curl -X POST https://ap-domain.com/webhook/line/322okyxf \
  -H "Content-Type: application/json" \
  -H "X-Line-Signature: test" \
  -d '{"events":[]}'
```

### 2. 檢查 Nginx 日誌
```bash
# 查看 webhook 請求日誌
tail -f /var/log/nginx/line_webhook_access.log
tail -f /var/log/nginx/line_webhook_error.log
```

### 3. 檢查後端日誌
```bash
# 查看 Spring Boot 日誌
tail -f /opt/cool-apps/logs/cheng-admin.log
```

## 常見問題

### Q1: Webhook URL 設定後，LINE 平台回報錯誤
**A**: 檢查：
1. Nginx 配置是否正確重新載入 (`nginx -s reload`)
2. 後端服務是否正常執行
3. SSL 憑證是否有效
4. 防火牆是否開放 443 埠

### Q2: Nginx 顯示 502 Bad Gateway
**A**: 檢查：
1. 後端服務是否在 8080 埠執行
2. Context-path 配置是否一致
3. 後端服務是否正常啟動

### Q3: LINE 訊息無法接收
**A**: 檢查：
1. Webhook URL 是否正確設定在 LINE Console
2. X-Line-Signature 是否正確傳遞
3. 後端日誌是否有錯誤訊息
4. Channel Secret 是否正確配置

## 相關檔案

- 後端配置：`cheng-admin/src/main/resources/application-prod.yml`
- LINE 屬性：`cheng-line/src/main/java/com/cheng/line/config/LineProperties.java`
- Nginx 配置（PROD）：`cheng.deploy/nginx/proxy-ssl-corrected.conf`
- Nginx 配置（VM）：`cheng.deploy/nginx/proxy-ssl.conf`
- Webhook Controller：`cheng-line/src/main/java/com/cheng/line/controller/LineWebhookController.java`
