# Cheng-Vue 部署指南

> **版本**：v1.2.0  
> **最後更新**：2025-10-04  
> **新增功能**：ISBN 書籍掃描與資訊爬蟲整合

## 📁 目錄結構說明

```
cheng.deploy/
├── README.md                           # 📖 本指南檔案
├── build-frontend.sh                   # 🔨 前端建置腳本
├── deploy-backend.sh                   # 🚀 後端 WAR 檔案部署腳本
├── deploy-to-server.sh                 # 🚀 前端檔案部署腳本
├── frontend-dist/                      # 📦 建置完成的前端檔案（自動產生）
├── nginx/                              # ⚙️  Nginx 設定檔案
│   └── proxy-ssl-corrected.conf        # 🔧 Nginx 反向代理設定
└── logs/                               # 📝 部署日誌目錄（自動產生）
```

## 🚀 部署方式

### **方式一：GitHub Actions CI/CD（推薦）**

專案已整合自動化 CI/CD，推送程式碼到 GitHub 即可自動部署：

1. **設定 GitHub Secrets**（僅需設定一次）：
   - `SSH_PRIVATE_KEY`：SSH 私鑰
   - `DEPLOY_HOST`：伺服器主機位址
   - `DEPLOY_USER`：SSH 使用者名稱
   - `SSH_PORT`：SSH 連接埠（預設 22）
   - `TOMCAT_WEBAPPS_DIR`：Tomcat webapps 目錄路徑
   - `SSH_KNOWN_HOSTS`：（選用）已知的主機金鑰

2. **推送程式碼觸發部署**：
   ```bash
   git add .
   git commit -m "feat: 新增功能"
   git push origin main
   ```

3. **CI/CD 自動執行流程**：
   - ✅ 建置前端（Vue.js）
   - ✅ 建置後端（Maven 多模組）
   - ✅ 部署 WAR 檔案到 Tomcat
   - ✅ 部署前端靜態檔案到 Nginx

### **方式二：本地手動部署**

如果需要在本地建置和部署：

```bash
# 1. 建置前端（需要 Node.js 18+）
./cheng.deploy/build-frontend.sh

# 2. 部署後端 WAR 檔案（需要設定環境變數）
export DEPLOY_HOST="your-server-host"
export DEPLOY_USER="your-ssh-user"
./cheng.deploy/deploy-backend.sh

# 3. 部署前端到伺服器（需要設定環境變數）
export DEPLOY_HOST="your-server-host"
export DEPLOY_USER="your-ssh-user"
./cheng.deploy/deploy-to-server.sh
```

> ⚠️ **注意**：手動部署需要設定環境變數，避免在腳本中硬編碼敏感資訊

## 📋 核心檔案說明

### 🔨 **build-frontend.sh**
前端建置腳本，將 Vue.js 專案編譯為正式版本。

**功能**：
- 檢查 Node.js 和 pnpm 環境
- 安裝前端依賴套件
- 執行正式版本建置
- 產生建置資訊檔案
- 驗證建置結果

**環境需求**：
- Node.js 18+
- pnpm 8+

### 🚀 **deploy-backend.sh**
後端 WAR 檔案部署腳本。

**功能**：
- 透過 SSH 上傳 WAR 檔案到伺服器
- 備份現有的 WAR 檔案
- 重新啟動 Tomcat 服務
- 執行健康檢查

**環境變數**：
- `DEPLOY_HOST`：伺服器主機位址
- `DEPLOY_USER`：SSH 使用者
- `SSH_PORT`：SSH 連接埠
- `TOMCAT_WEBAPPS_DIR`：Tomcat webapps 目錄
- `WAR_FILE`：WAR 檔案路徑

### 🚀 **deploy-to-server.sh**
前端靜態檔案部署腳本。

**功能**：
- 透過 rsync 同步前端檔案到伺服器
- 更新 Nginx 設定檔案
- 測試並重新載入 Nginx
- 確認服務狀態

**環境變數**：
- `DEPLOY_HOST`：伺服器主機位址
- `DEPLOY_USER`：SSH 使用者
- `SSH_PORT`：SSH 連接埠
- `LOCAL_FRONTEND_DIR`：本地前端檔案目錄
- `FRONTEND_DIR`：伺服器前端檔案目錄

### ⚙️ **nginx/proxy-ssl-corrected.conf**
Nginx 反向代理設定範本。

**功能**：
- 前端靜態檔案服務
- 後端 API 反向代理
- SSL/TLS 設定
- CORS 跨域支援
- 快取策略設定

**使用變數**：
- `${DEPLOY_HOST}`：域名（由部署腳本自動替換）
- `${FRONTEND_DIR}`：前端檔案路徑（由部署腳本自動替換）

## 🏗️ 技術棧

### 前端
- **框架**：Vue.js 2.6
- **UI 框架**：Element UI（繁體中文）
- **建置工具**：Vue CLI
- **特色功能**：
  - QR Code / 條碼掃描（html5-qrcode）
  - ISBN 書籍資訊爬蟲
  - 庫存管理系統
  - 響應式設計

### 後端
- **框架**：Spring Boot 3.5.4
- **Java 版本**：Java 17
- **資料庫**：MySQL 8
- **ORM**：MyBatis
- **安全**：Spring Security + JWT
- **專案結構**：Maven 多模組
  - `cheng-admin`：主應用模組
  - `cheng-framework`：核心框架
  - `cheng-system`：系統模組
  - `cheng-common`：共用工具
  - `cheng-quartz`：定時任務
  - `cheng-generator`：程式碼產生器
  - `cheng-crawler`：爬蟲模組

### 基礎設施
- **Web 伺服器**：Nginx（反向代理 + 靜態檔案）
- **應用伺服器**：Tomcat 10
- **CI/CD**：GitHub Actions
- **版本控制**：Git

## 📝 環境需求

### 開發環境
- **Java**：JDK 17+
- **Maven**：3.8+
- **Node.js**：18+
- **pnpm**：8+

### 伺服器環境
- **作業系統**：Linux（Ubuntu/CentOS/RHEL）
- **Nginx**：1.18+
- **Tomcat**：10+
- **MySQL**：8+
- **SSL 憑證**：Let's Encrypt（推薦）

## 🔒 安全性注意事項

### 1. **敏感資訊管理**
- ✅ 使用環境變數儲存敏感資訊
- ✅ GitHub Secrets 管理 CI/CD 憑證
- ✅ Jasypt 加密配置檔案中的敏感資料
- ❌ 不要在程式碼中硬編碼密碼、金鑰

### 2. **SSH 連線安全**
- ✅ 使用 SSH 金鑰認證（不使用密碼）
- ✅ 設定 `known_hosts` 驗證主機身份
- ✅ 限制 SSH 使用者權限
- ❌ 不要將私鑰提交到版本控制

### 3. **應用安全**
- ✅ JWT Token 認證機制
- ✅ HTTPS/SSL 加密傳輸
- ✅ CORS 跨域存取控制
- ✅ SQL 注入防護（MyBatis 參數化查詢）

### 4. **配置檔案安全**
- 資料庫連線資訊已使用 Jasypt 加密
- Druid 監控介面需要帳號密碼認證
- 正式環境配置獨立於版本控制

## 🧪 測試部署結果

### 健康檢查端點
```bash
# 測試前端
curl -I https://your-domain/

# 測試後端 API
curl -I https://your-domain/prod-api/

# 測試使用者資料端點（需要登入）
curl https://your-domain/prod-api/system/user/profile
```

### 查看日誌
```bash
# 前端建置日誌
cat cheng.deploy/logs/build-*.log

# Nginx 日誌（伺服器上）
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# Tomcat 日誌（伺服器上）
sudo tail -f /opt/tomcat/logs/catalina.out
```

## ❓ 常見問題

### Q1: 前端建置失敗
**解決方案**：
1. 確認 Node.js 版本 >= 18
2. 清除 pnpm 快取：`pnpm store prune`
3. 刪除 `node_modules` 重新安裝：`rm -rf node_modules && pnpm install`

### Q2: Maven 建置失敗
**解決方案**：
1. 確認 Java 版本 = 17
2. 清除 Maven 快取：`mvn clean`
3. 在專案根目錄執行：`mvn clean install`

### Q3: SSH 連線失敗
**解決方案**：
1. 檢查 SSH 金鑰權限：`chmod 600 ~/.ssh/id_rsa`
2. 測試 SSH 連線：`ssh -vvv user@host`
3. 確認防火牆設定

### Q4: 部署後無法存取
**解決方案**：
1. 檢查 Nginx 狀態：`sudo systemctl status nginx`
2. 檢查 Tomcat 狀態：`sudo systemctl status tomcat10`
3. 檢查防火牆規則：`sudo firewall-cmd --list-all`

## 📚 參考文件

- [GitHub Actions 官方文件](https://docs.github.com/en/actions)
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Vue.js 官方文件](https://v2.vuejs.org/)
- [Nginx 官方文件](https://nginx.org/en/docs/)

---

**版本歷史**：
- **v1.2.0**（2025-10-04）：整合 ISBN 書籍掃描與爬蟲功能，優化 CI/CD 流程
- **v1.1.1**（2025-09-23）：新增庫存管理系統模組
- **v1.0.0**（2025-09-01）：初始版本發布
