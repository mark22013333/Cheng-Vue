# 庫存管理系統 - 部署與使用說明

## 系統概述

本庫存管理系統是一個基於Spring Boot + Vue.js的全端應用程式，提供完整的庫存管理功能，包括：
- QR Code和條碼掃描
- 物品管理和庫存追蹤
- 借出歸還管理
- 庫存異動記錄
- 報表統計和匯出

## 技術架構

### 後端技術棧
- **框架**: Spring Boot 2.x
- **資料庫**: MySQL 8.0+
- **ORM**: MyBatis
- **安全**: Spring Security
- **建構工具**: Maven

### 前端技術棧
- **框架**: Vue.js 2.6
- **UI庫**: Element UI
- **掃描庫**: html5-qrcode
- **建構工具**: Vue CLI

## 系統部署

### 1. 環境準備

#### 系統要求
- Java 8+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

#### 資料庫設定
```sql
-- 建立資料庫
CREATE DATABASE cool_apps DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 建立使用者（可選）
CREATE USER 'cool_apps'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON cool_apps.* TO 'cool_apps'@'%';
FLUSH PRIVILEGES;
```

### 2. 資料庫初始化

按順序執行以下SQL腳本：

```bash
# 1. 基礎系統資料
mysql -u root -p cool_apps < sql/cool-apps_20250922-tw.sql

# 2. 庫存管理資料表
mysql -u root -p cool_apps < sql/inventory_management.sql

# 3. 庫存管理選單
mysql -u root -p cool_apps < sql/inventory_menu.sql

# 4. 定時任務設定（可選）
mysql -u root -p cool_apps < sql/quartz-tw.sql
```

### 3. 後端部署

#### 設定檔案設定

**application-prod.yml**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cool_apps?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: cool_apps
    password: your_password
    
logging:
  file:
    path: /opt/cool-apps/logs
    
cheng:
  profile: /opt/cool-apps/uploadFile
```

#### 建構和部署
```bash
# 建構後端
mvn clean package -Dmaven.test.skip=true

# 部署到伺服器
cp cheng-admin/target/cheng-admin.jar /opt/cool-apps/
cp -r sql /opt/cool-apps/

# 建立啟動腳本
cat > /opt/cool-apps/start.sh << 'EOF'
#!/bin/bash
cd /opt/cool-apps
nohup java -jar cheng-admin.jar --spring.profiles.active=prod > logs/app.log 2>&1 &
echo $! > app.pid
EOF

chmod +x /opt/cool-apps/start.sh
```

### 4. 前端部署

#### 安裝依賴和建構
```bash
cd cheng-ui

# 安裝依賴（包含html5-qrcode）
npm install

# 建構正式版本
npm run build:prod
```

#### Nginx設定
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /opt/cool-apps/frontend-dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /prod-api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 5. 系統啟動

```bash
# 啟動後端
/opt/cool-apps/start.sh

# 重新載入Nginx
nginx -s reload

# 檢查狀態
tail -f /opt/cool-apps/logs/app.log
```

## 功能使用說明

### 1. 系統登入

- 預設管理員帳號：`admin`
- 預設密碼：`admin123`
- 登入後請立即修改密碼

### 2. 庫存管理功能

#### 2.1 物品管理
- **路徑**: 庫存管理 → 物品管理
- **功能**: 新增、修改、刪除、查詢物品資訊
- **權限**: `inventory:item:*`

#### 2.2 庫存查詢
- **路徑**: 庫存管理 → 庫存查詢
- **功能**: 查看庫存狀況、入庫、出庫、盤點
- **權限**: `inventory:stock:*`

#### 2.3 借出管理
- **路徑**: 庫存管理 → 借出管理
- **功能**: 借出申請、審核、歸還管理
- **權限**: `inventory:borrow:*`

#### 2.4 掃描功能
- **路徑**: 庫存管理 → 掃描功能
- **功能**: QR Code和條碼掃描、手動輸入
- **權限**: `inventory:scan:*`
- **注意**: 需要攝影機權限

#### 2.5 庫存報表
- **路徑**: 庫存管理 → 庫存報表
- **功能**: 各類報表查看和匯出
- **權限**: `inventory:report:*`

### 3. 掃描功能使用

#### 3.1 攝影機掃描
1. 點擊「開始掃描」按鈕
2. 允許瀏覽器使用攝影機權限
3. 將條碼或QR Code對準攝影機
4. 系統自動識別並處理

#### 3.2 手動輸入
1. 在輸入框中輸入條碼或QR Code內容
2. 選擇掃描類型（條碼/QR碼）
3. 點擊「掃描」按鈕

#### 3.3 掃描後操作
- 查看物品詳情
- 借出物品
- 庫存入庫/出庫
- 查看掃描歷史

### 4. 報表功能

#### 4.1 庫存狀況報表
- 顯示所有物品的庫存狀況
- 包含庫存不足提醒
- 支援Excel匯出

#### 4.2 借出歸還報表
- 顯示借出記錄和狀態
- 逾期提醒功能
- 統計分析圖表

#### 4.3 庫存異動報表
- 記錄所有庫存變動
- 入庫、出庫、盤點記錄
- 異動原因追蹤

#### 4.4 掃描記錄報表
- 掃描操作記錄
- 成功率統計
- 使用者行為分析

## 系統維護

### 1. 日誌管理

```bash
# 查看應用日誌
tail -f /opt/cool-apps/logs/app.log

# 查看錯誤日誌
tail -f /opt/cool-apps/logs/error.log

# 日誌輪轉（建議設定crontab）
find /opt/cool-apps/logs -name "*.log" -mtime +30 -delete
```

### 2. 資料庫備份

```bash
# 每日備份腳本
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u cool_apps -p cool_apps > /opt/cool-apps/backup/cool_apps_$DATE.sql
find /opt/cool-apps/backup -name "*.sql" -mtime +7 -delete
```

### 3. 系統監控

#### 3.1 Druid監控
- 訪問地址：`http://your-domain/druid/`
- 帳號：`cheng`
- 密碼：`123456`

#### 3.2 應用監控
```bash
# 檢查應用狀態
ps aux | grep cheng-admin

# 檢查埠佔用
netstat -tlnp | grep 8080

# 記憶體使用情況
free -h
```

## 常見問題

### 1. 掃描功能無法使用
- 檢查瀏覽器攝影機權限
- 確認html5-qrcode依賴已安裝
- 檢查HTTPS設定（部分瀏覽器要求）

### 2. 選單不顯示
- 確認資料庫選單腳本已執行
- 檢查使用者角色權限設定
- 清除瀏覽器快取

### 3. 檔案上傳失敗
- 檢查上傳目錄權限
- 確認磁碟空間充足
- 檢查檔案大小限制

### 4. 資料庫連線失敗
- 檢查資料庫服務狀態
- 確認連線參數正確
- 檢查防火牆設定

## 系統升級

### 1. 備份資料
```bash
# 備份資料庫
mysqldump -u cool_apps -p cool_apps > backup_before_upgrade.sql

# 備份應用檔案
cp -r /opt/cool-apps /opt/cool-apps-backup
```

### 2. 停止服務
```bash
# 停止應用
kill $(cat /opt/cool-apps/app.pid)

# 停止Nginx（如需要）
nginx -s stop
```

### 3. 更新程式
```bash
# 更新後端
cp new-version/cheng-admin.jar /opt/cool-apps/

# 更新前端
cp -r new-version/frontend-dist/* /opt/cool-apps/frontend-dist/

# 執行資料庫升級腳本（如有）
mysql -u cool_apps -p cool_apps < upgrade.sql
```

### 4. 重新啟動
```bash
# 啟動應用
/opt/cool-apps/start.sh

# 啟動Nginx
nginx

# 檢查狀態
tail -f /opt/cool-apps/logs/app.log
```

## 聯絡資訊

如有問題或建議，請聯絡：
- 開發團隊：CoolApps Team
- 技術支援：support@coolapps.com
---

**最後更新**: 2025-09-23
**版本**: v1.1.0
