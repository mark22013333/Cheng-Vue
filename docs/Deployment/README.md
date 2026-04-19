# 部署文件

本目錄包含 CoolApps 系統的部署流程、環境配置和檢查清單。

## 📚 文件列表

### 主要部署指南
- **00_DEPLOYMENT_README.md** - 部署主要說明文件（從這裡開始）
- **DEPLOYMENT.md** - 詳細部署步驟說明

### 檢查清單
- **DEPLOYMENT_CHECKLIST.md** - 通用部署檢查清單
- **DEPLOYMENT_CHECKLIST_QUARTZ.md** - Quartz 排程系統部署檢查清單
- **CATEGORY_DEPLOYMENT.md** - 分類功能部署說明

## 🚀 部署流程

### 1. 前置準備
- Java 17
- Maven 3.8+
- Node.js 16+
- MySQL 8.0+
- Redis（選用）

### 2. 環境配置
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://localhost:3306/cool_apps
  flyway:
    enabled: true
```

### 3. 建置專案
```bash
# 建置後端
mvn clean package -DskipTests

# 建置前端
cd cheng-ui
npm run build:prod
```

### 4. 部署執行
```bash
# 使用部署腳本
./cheng.deploy/deploy-all.sh

# 或手動部署
java -jar -Djasypt.encryptor.password=*** cheng-admin.war
```

## ⚙️ 環境變數

### 必要參數
- `JASYPT_ENCRYPTOR_PASSWORD` - Jasypt 加密密碼
- `SPRING_PROFILES_ACTIVE` - 環境設定檔（local/prod）

### 選用參數
- `PORT` - 應用程式埠號（預設 8080）
- `JAVA_OPTS` - JVM 參數

## 🔍 驗證步驟

1. ✅ 檢查應用程式啟動日誌
2. ✅ 訪問健康檢查端點
3. ✅ 測試資料庫連線
4. ✅ 驗證 Flyway 遷移狀態
5. ✅ 測試排程任務執行

## 🔗 相關連結
- [快速開始](../Guides/QUICK_START.md)
- [Flyway 遷移指南](../Flyway/FLYWAY_MIGRATION_GUIDE.md)
- [部署腳本](../Scripts/)
