# 環境配置規範

> **核心原則**: 區分環境、保護敏感資訊、統一配置管理

---

## 環境區分

### Local (本地開發環境)

**Profile**: `local`

```yaml
# application-local.yml
spring:
  profiles:
    active: local
  
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/cool-apps?...
        username: root
        password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password:

logging:
  file:
    path: /Users/cheng/cool-logs

cheng:
  profile: /Users/cheng/uploadPath

line:
  webhook:
    base-url: ${LINE_WEBHOOK_BASE_URL:https://your-ngrok-url.ngrok.io}
```

**啟動命令**:
```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

**特點**:
- Port: `8080`
- Context-path: `/`
- LOG 路徑: `/Users/cheng/cool-logs`
- 上傳路徑: `/Users/cheng/uploadPath`
- LINE Webhook: 使用 Ngrok 測試
- 資料庫: 本地 MySQL 或遠端
- Redis: 本地 Redis 或遠端

---

### VM (虛擬機 Tomcat 環境)

**Profile**: `vm`

```yaml
# application-vm.yml
spring:
  profiles:
    active: vm
  
  datasource:
    druid:
      master:
        url: ENC(...)  # Jasypt 加密
        username: ENC(...)
        password: ENC(...)
  
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: ENC(...)

logging:
  file:
    path: /opt/cool-apps/logs

cheng:
  profile: /opt/cool-apps/uploadFile

line:
  webhook:
    base-url: ${LINE_WEBHOOK_BASE_URL:https://cheng.tplinkdns.com}

crawler:
  selenium:
    mode: remote
    remote-url: http://localhost:9515
```

**部署方式**:
- Tomcat 10 + Nginx
- Domain: `cheng.tplinkdns.com`
- Context-path: `/`
- LOG 路徑: `/opt/cool-apps/logs`
- 上傳路徑: `/opt/cool-apps/uploadFile`
- Selenium: Docker remote mode (port 9515)

**Nginx 配置**: 使用 `cheng.deploy/nginx/proxy-ssl.conf`

---

### PROD (Docker/Zeabur 正式環境)

**Profile**: `prod`

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  
  datasource:
    druid:
      master:
        # 優先使用 DB_URL；否則組合 Zeabur 變數
        url: ${DB_URL:jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:cool-apps}?...}
        username: ${DB_USERNAME:${MYSQL_USERNAME:root}}
        password: ${DB_PASSWORD:${MYSQL_PASSWORD:${MYSQL_ROOT_PASSWORD:}}}
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

logging:
  file:
    path: /opt/cool-apps/logs

cheng:
  profile: /opt/cool-apps/uploadFile

line:
  webhook:
    base-url: ${LINE_WEBHOOK_BASE_URL:https://cool-apps.zeabur.app}

crawler:
  selenium:
    mode: local
    chrome-driver-path: /usr/bin/chromedriver
```

**部署方式**:
- Docker + Nginx
- Domain: `cool-apps.zeabur.app`
- Context-path: `/`
- LOG 路徑: `/opt/cool-apps/logs`
- 上傳路徑: `/opt/cool-apps/uploadFile`
- Selenium: 容器內建 Chrome

**環境變數** (在 Zeabur 設定):
```bash
SPRING_PROFILES_ACTIVE=prod
JASYPT_ENCRYPTOR_PASSWORD=diDsd]3FsGO@4dido
LINE_WEBHOOK_BASE_URL=https://cool-apps.zeabur.app
```

---

## Jasypt 加密規範

### 必須加密的資訊

- ✅ 資料庫連線資訊 (URL、username、password)
- ✅ Redis 密碼
- ✅ JWT Secret
- ✅ LINE Channel Secret/Token
- ✅ Druid 監控帳號密碼
- ✅ 所有第三方 API Key
- ❌ 不要加密：埠號、網域名稱、檔案路徑

### 加密方式

```bash
# 使用 Jasypt CLI 加密
java -cp jasypt-1.9.3.jar \
  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="your_secret" \
  password="diDsd]3FsGO@4dido" \
  algorithm=PBEWithMD5AndDES

# 輸出
----ENVIRONMENT-----------------
Runtime: Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 17.0.2+8-LTS-86

----ARGUMENTS-------------------
input: your_secret
password: diDsd]3FsGO@4dido
algorithm: PBEWithMD5AndDES

----OUTPUT----------------------
ENC(KocpHJuKrmFEMlhiiSVggp5zrskhm6n2enL2w+SkHJtB+e4TrBuVU5dRksIpyFJA)
```

### 配置使用

```yaml
spring:
  datasource:
    password: ENC(KocpHJuKrmFEMlhiiSVggp5zrskhm6n2enL2w+SkHJtB+e4TrBuVU5dRksIpyFJA)
```

### 解密密碼管理

**本地開發**:
```bash
# 方式 1: 命令列參數
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido

# 方式 2: IDE 設定
# Run > Edit Configurations > VM options:
-Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

**正式環境**:
```bash
# 環境變數
export JASYPT_ENCRYPTOR_PASSWORD=diDsd]3FsGO@4dido

# 或在 Zeabur/Docker 設定環境變數
JASYPT_ENCRYPTOR_PASSWORD=diDsd]3FsGO@4dido
```

**⚠️ 嚴禁將解密密碼寫入程式碼或 Git！**

---

## Context-path 一致性

### 所有環境統一使用根路徑 `/`

```yaml
# application.yml（所有環境共用）
server:
  servlet:
    context-path: /    # 統一使用根路徑
```

**為什麼？**
- ✅ 簡化 Nginx 配置
- ✅ 避免前後端路徑不一致
- ✅ LINE Webhook URL 更簡潔

### 前端 API 路徑前綴

```javascript
// vue.config.js
devServer: {
  proxy: {
    '/dev-api': {
      target: 'http://localhost:8080',
      pathRewrite: {
        '^/dev-api': ''  // 移除 /dev-api 前綴
      }
    }
  }
}

// .env.production
VUE_APP_BASE_API='/prod-api'
```

**Nginx 配置**:
```nginx
location /prod-api/ {
    proxy_pass http://127.0.0.1:8080/;  # 移除 /prod-api 前綴
}
```

---

## LINE Webhook 配置

### Webhook URL 格式

```
https://{domain}/webhook/line/{botBasicId}
```

**範例**:
- Local: `https://abc123.ngrok.io/webhook/line/322okyxf`
- VM: `https://cheng.tplinkdns.com/webhook/line/322okyxf`
- PROD: `https://cool-apps.zeabur.app/webhook/line/322okyxf`

### Base URL 設定

```yaml
line:
  webhook:
    base-url: ${LINE_WEBHOOK_BASE_URL:https://cool-apps.zeabur.app}
```

**注意**: 只需填寫網域名稱，系統會自動處理路徑。

### Nginx 轉發配置

```nginx
# LINE Webhook 處理（優先順序最高）
location /webhook/line/ {
    proxy_pass http://127.0.0.1:8080/webhook/line/;
    
    # 重要：傳遞 X-Line-Signature 標頭
    proxy_set_header X-Line-Signature $http_x_line_signature;
    
    # 其他標頭
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

---

## 日誌與上傳路徑配置

### 路徑對應表

| 環境 | LOG 路徑 | 上傳路徑 |
|------|---------|---------|
| Local | `/Users/cheng/cool-logs` | `/Users/cheng/uploadPath` |
| VM | `/opt/cool-apps/logs` | `/opt/cool-apps/uploadFile` |
| PROD | `/opt/cool-apps/logs` | `/opt/cool-apps/uploadFile` |

### logback.xml 配置

```xml
<configuration>
    <!-- 從 Spring 配置取得 log.path -->
    <springProperty scope="context" name="log.path" 
                    source="logging.file.path" 
                    defaultValue="/tmp/logs"/>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${log.path}/cheng.log</file>
        <!-- ... -->
    </appender>
</configuration>
```

---

## Druid 監控配置

### 繁體中文設定

```yaml
spring:
  datasource:
    druid:
      statViewServlet:
        enabled: true
        url-pattern: /druid/*
        login-username: ENC(...)  # cheng（加密）
        login-password: ENC(...)  # 123456（加密）
        # 設定繁體中文介面
        init-parameters:
          language: zh_TW
```

### 訪問方式

- **URL**: `http://localhost:8080/druid/`
- **帳號**: `cheng`
- **密碼**: `123456`

---

## 環境切換

### 本地開發

```bash
# 啟動 Local 環境
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido

# 或在 application.yml 設定
spring:
  profiles:
    active: local
```

### VM 部署

```bash
# WAR 部署到 Tomcat
# 在 catalina.sh 或 setenv.sh 加入
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=vm"
JAVA_OPTS="$JAVA_OPTS -Djasypt.encryptor.password=diDsd]3FsGO@4dido"
```

### Docker 部署

```bash
# Dockerfile
ENV SPRING_PROFILES_ACTIVE=prod
ENV JASYPT_ENCRYPTOR_PASSWORD=diDsd]3FsGO@4dido

# 或 docker run
docker run -e SPRING_PROFILES_ACTIVE=prod \
           -e JASYPT_ENCRYPTOR_PASSWORD=diDsd]3FsGO@4dido \
           cool-apps:latest
```

---

## 檢查清單

部署前必須檢查：

- [ ] Profile 是否正確設定？
- [ ] Jasypt 解密密碼是否設定？
- [ ] 敏感資訊是否已加密？
- [ ] Context-path 是否為 `/`？
- [ ] Nginx Webhook 轉發是否正確？
- [ ] LOG 和上傳路徑是否存在？
- [ ] 環境變數是否設定完整？
- [ ] Druid 監控語言是否為繁體中文？

---

**下一步**: 閱讀 [RULE_11_CODE_REVIEW.md](./RULE_11_CODE_REVIEW.md) 學習程式碼審查清單
