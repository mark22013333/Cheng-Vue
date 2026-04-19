# 🚀 部署指南

## 📋 目錄

- [環境變數配置](#環境變數配置)
- [資料庫初始化](#資料庫初始化)
- [Docker 部署](#docker-部署)
- [傳統部署](#傳統部署)
- [Nginx 配置](#nginx-配置)
- [效能優化](#效能優化)
- [監控與日誌](#監控與日誌)

## 🔐 環境變數配置

### 建立 `.env` 檔案

在專案根目錄建立 `.env` 檔案（**不要提交到 Git**）：

```bash
# Jasypt 加密密碼（請聯繫管理員取得實際值）
JASYPT_PASSWORD=

# 應用程式環境（local/dev/prod）
SPRING_PROFILES_ACTIVE=prod

# 伺服器埠號
PORT=8080

# 時區設定
TZ=Asia/Taipei

# 檔案上傳路徑
UPLOAD_PATH=/opt/cool-apps/upload

# LINE Bot 設定（如有使用，請聯繫管理員取得）
LINE_CHANNEL_SECRET=
LINE_CHANNEL_ACCESS_TOKEN=
```

### Jasypt 加密配置說明

#### 什麼是 Jasypt？

Jasypt 用於加密敏感配置（如資料庫密碼、API Token），加密後的值格式為：`ENC(...)`

#### 如何加密新的配置值？

使用專案提供的加密工具：

```bash
# 加密明文
mvn jasypt:encrypt-value \
  -Djasypt.encryptor.password=${JASYPT_PASSWORD} \
  -Djasypt.plugin.value="your-plain-text-value"

# 輸出範例：
# ENC(X5Q6Z8B2...)
```

#### 在配置檔中使用加密值

```yaml
# application-prod.yml
spring:
  datasource:
    url: ENC(X5Q6Z8B2...)      # 使用加密後的值
    username: ENC(A3F7G9H1...)
    password: ENC(K8L2M4N6...)
```

## 🗄️ 資料庫初始化

### 使用 Flyway 自動遷移（推薦）

**專案已整合 Flyway**，啟動時會自動執行資料庫遷移：

```bash
# 1. 建立資料庫
CREATE DATABASE `cool-apps` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 啟動應用程式（自動執行 Migration）
java -Djasypt.encryptor.password=${JASYPT_PASSWORD} -jar apps.war
```

**Migration 檔案位置**：
```
cheng-admin/src/main/resources/db/migration/
├── V0.1__init_system_core.sql
├── V0.2__init_quartz.sql
├── V1__init_inventory_table.sql
└── ...
```

### 手動執行 Migration（不建議）

如需手動執行：

```bash
# 檢查 Migration 狀態
mvn flyway:info

# 執行 Migration
mvn flyway:migrate

# 清理資料庫（危險！僅用於開發環境）
mvn flyway:clean
```

## 🐳 Docker 部署

### 部署架構

```
┌─────────────┐
│   Nginx     │ ← 前端靜態檔案 + 反向代理
│   (1.27)    │
└──────┬──────┘
       │
┌──────▼──────┐
│   Tomcat    │ ← 後端 Spring Boot
│ (10.1-jdk17)│
└──────┬──────┘
       │
┌──────▼──────┬──────────┐
│   MySQL     │  Redis   │ ← 雲端託管
│   (8.0+)    │  (6.0+)  │
└─────────────┴──────────┘
```

### 一鍵建置與部署

專案提供自動化建置腳本：

```bash
# 建置所有 Docker 映像（前端 + 後端）
./build-all.sh

# 或分別建置
./build-frontend.sh   # 建置前端
./build-backend.sh    # 建置後端
```

### 手動建置 Docker 映像

#### 前端 Docker 映像

```bash
cd cheng-ui

# 建置前端靜態檔案
npm run build:prod

# 建置 Docker 映像
docker build -t cheng-ui:latest .
```

#### 後端 Docker 映像

```bash
# 打包 WAR 檔案
mvn clean package -DskipTests

# 建置 Docker 映像
docker build \
  -f Dockerfile.backend-tomcat \
  -t cheng-backend:latest .
```

### Docker Compose 部署

建立 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # 前端服務
  frontend:
    image: cheng-ui:latest
    container_name: cool-apps-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

  # 後端服務
  backend:
    image: cheng-backend:latest
    container_name: cool-apps-backend
    environment:
      - JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_PASSWORD}
      - SPRING_PROFILES_ACTIVE=prod
      - TZ=Asia/Taipei
    ports:
      - "8080:8080"
    restart: unless-stopped
```

啟動服務：

```bash
# 設定環境變數
export JASYPT_PASSWORD="your-jasypt-password"

# 啟動服務
docker-compose up -d

# 查看日誌
docker-compose logs -f backend

# 停止服務
docker-compose down
```

### 多平台建置（支援 ARM64）

使用 Docker Buildx：

```bash
# 建立 builder
docker buildx create --name multiarch --use

# 建置多平台映像
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t cheng-backend:latest \
  -f Dockerfile.backend-tomcat \
  --push .
```

## 📦 傳統部署

### 後端部署

#### 1. 打包應用程式

```bash
mvn clean package -DskipTests
```

產出檔案：`cheng-admin/target/apps.war`

#### 2. 部署到 Tomcat

```bash
# 複製 WAR 到 Tomcat webapps
cp cheng-admin/target/apps.war /opt/tomcat/webapps/

# 設定環境變數
export JASYPT_ENCRYPTOR_PASSWORD="your-password"
export SPRING_PROFILES_ACTIVE=prod

# 啟動 Tomcat
/opt/tomcat/bin/startup.sh
```

#### 3. 獨立運行（內嵌 Tomcat）

```bash
java -Djasypt.encryptor.password=${JASYPT_PASSWORD} \
     -Dspring.profiles.active=prod \
     -jar cheng-admin/target/apps.war
```

### 前端部署

#### 1. 建置靜態檔案

```bash
cd cheng-ui

# 安裝依賴
npm install

# 建置生產版本
npm run build:prod
```

產出目錄：`cheng-ui/dist/`

#### 2. 使用部署腳本

```bash
# 建置前端
./cheng.deploy/build-frontend.sh

# 驗證建置結果
./cheng.deploy/verify-build.sh

# 部署到伺服器（需配置 SSH）
./cheng.deploy/deploy-to-server.sh
```

## 🌐 Nginx 配置

### 完整配置範例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # Gzip 壓縮
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    gzip_min_length 1000;
    
    # 前端靜態檔案
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
        
        # 快取策略
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # 後端 API 反向代理
    location /prod-api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超時設定
        proxy_connect_timeout 600;
        proxy_send_timeout 600;
        proxy_read_timeout 600;
        send_timeout 600;
    }
    
    # 檔案上傳大小限制
    client_max_body_size 50m;
}
```

### HTTPS 配置（使用 Let's Encrypt）

```bash
# 安裝 certbot
sudo apt-get install certbot python3-certbot-nginx

# 取得 SSL 憑證
sudo certbot --nginx -d your-domain.com

# 自動更新憑證
sudo certbot renew --dry-run
```

## ⚡ 效能優化

### 後端優化

#### 1. JVM 參數調優

```bash
java -Xms2g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Djasypt.encryptor.password=${JASYPT_PASSWORD} \
     -jar apps.war
```

#### 2. Druid 連線池優化

已在 `application-prod.yml` 中設定：

```yaml
spring:
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 50
      max-wait: 60000
```

#### 3. Redis 暫存優化

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 10
```

### 前端優化

已在建置時自動啟用：

- ✅ Gzip 壓縮
- ✅ 程式碼分割
- ✅ Tree Shaking
- ✅ 靜態資源快取

## 📊 監控與日誌

### 應用程式日誌

**日誌目錄**（根據環境自動切換）：

```
# 本地環境
/Users/cheng/cool-logs/

# 生產環境
/opt/cool-apps/logs/
├── current/           # 當日日誌
│   ├── sys-info.log
│   ├── sys-error.log
│   └── sys-user.log
└── archive/           # 歷史日誌（保留 60 天）
    └── sys-info.2025-11-01.log.gz
```

**查詢日誌**：

```bash
# 根據 TraceId 追蹤請求
grep "a1B2c3D4" /opt/cool-apps/logs/current/*.log

# 即時查看錯誤日誌
tail -f /opt/cool-apps/logs/current/sys-error.log

# 解壓歷史日誌
gunzip /opt/cool-apps/logs/archive/sys-info.2025-11-01.log.gz
```

### 系統監控

**內建監控端點**（需要權限）：

- `/actuator/health` - 健康檢查
- `/actuator/metrics` - 應用程式指標
- `/actuator/info` - 應用程式資訊

**Druid 監控介面**：

訪問 `http://your-domain/druid/index.html`

帳號密碼在 `application-prod.yml` 中（已加密）

## 🔒 安全檢查清單

部署前請確認：

- [ ] `.env` 檔案已建立且**不在 Git 版控中**
- [ ] `.gitignore` 包含 `.env`、`*.local.yml`
- [ ] Jasypt 密碼已設定且安全保管
- [ ] 資料庫連線使用加密配置
- [ ] Redis 設定密碼
- [ ] Nginx 配置 HTTPS
- [ ] 檔案上傳路徑權限正確
- [ ] 防火牆規則已設定
- [ ] 定期備份資料庫

## 🆘 故障排查

### 問題：應用程式無法啟動

檢查項目：
1. JASYPT_PASSWORD 是否正確設定
2. 資料庫連線是否正常
3. Redis 連線是否正常
4. 檢查日誌：`tail -f /opt/cool-apps/logs/current/sys-error.log`

### 問題：前端無法連接後端

檢查項目：
1. Nginx 反向代理配置是否正確
2. 後端服務是否正常運行
3. 防火牆規則是否正確

### 問題：資料庫連線失敗

檢查項目：
1. 資料庫是否啟動
2. 連線字串是否正確
3. 帳號密碼是否正確（檢查加密配置）
4. 防火牆是否開放 3306 埠

## 📞 技術支援

遇到部署問題？

1. 查看 [故障排查](#故障排查)
2. 檢查應用程式日誌
3. 聯繫專案負責人

## 🔗 相關文檔

- [開發指南](./DEVELOPMENT.md)
- [專案架構文檔](./docs/Architecture/)
- [LOG 系統配置說明](./docs/Architecture/LOG系統配置說明.md)
