# CoolApps 部署指南

> 版本：v1.2.2  
> 更新日期：2025-10-13  
> 作者：Cheng

## 📋 目錄

- [快速開始](#快速開始)
- [建置腳本說明](#建置腳本說明)
- [手動建置](#手動建置)
- [環境變數配置](#環境變數配置)
- [常見問題](#常見問題)

---

## 🚀 快速開始

### 一鍵建置全部

```bash
# 同時建置前端和後端
./build-all.sh
```

### 單獨建置

```bash
# 只建置前端
./build-frontend.sh

# 只建置後端
./build-backend.sh
```

### 自動確認模式

```bash
# 跳過確認提示，直接建置
AUTO_CONFIRM=true ./build-frontend.sh
AUTO_CONFIRM=true ./build-backend.sh
```

### 自訂版本號

```bash
# 使用自訂版本號
BASE_VERSION=v1.3.0 ./build-frontend.sh
BASE_VERSION=v1.3.0 ./build-backend.sh
```

---

## 📜 建置腳本說明

### 1. `build-frontend.sh` - 前端建置腳本

**功能**：
- 建置 Vue.js 前端應用
- 產生帶時間戳記的版本號
- 推送到 Docker Hub
- 同時標記 `latest` 和版本標籤

**配置**：
```bash
REGISTRY="android106"
IMAGE_NAME="coolapps-frontend"
BASE_VERSION="v1.2.2"
DOCKERFILE="cheng-ui/Dockerfile"
```

**輸出映像**：
- `android106/coolapps-frontend:v1.2.2-20251013-0008`
- `android106/coolapps-frontend:latest`

### 2. `build-backend.sh` - 後端建置腳本

**功能**：
- 建置 Spring Boot 後端應用
- 包含 Maven 編譯過程
- 推送到 Docker Hub
- 建置時間約 3-5 分鐘

**配置**：
```bash
REGISTRY="android106"
IMAGE_NAME="coolapps-backend"
BASE_VERSION="v1.2.2"
DOCKERFILE="Dockerfile.backend-tomcat"
```

**輸出映像**：
- `android106/coolapps-backend:v1.2.2-20251013-0008`
- `android106/coolapps-backend:latest`

### 3. `build-all.sh` - 完整建置腳本

**功能**：
- 依序建置前端和後端
- 顯示總耗時
- 自動化整個建置流程

**執行順序**：
1. 建置前端映像
2. 建置後端映像
3. 顯示建置總結

---

## 🔧 手動建置

### 前端手動建置指令

```bash
docker buildx build \
  -f cheng-ui/Dockerfile \
  --platform linux/amd64 \
  -t android106/coolapps-frontend:v1.2.2-20251013-10 \
  -t android106/coolapps-frontend:latest \
  cheng-ui --push
```

### 後端手動建置指令

```bash
docker buildx build \
  -f Dockerfile.backend-tomcat \
  --platform linux/amd64 \
  -t android106/coolapps-backend:v1.2.2-20251013-5 \
  -t android106/coolapps-backend:latest \
  . --push
```

---

## ⚙️ 環境變數配置

### 後端必要環境變數

| 變數名稱 | 說明 | 範例值 |
|---------|------|--------|
| `JASYPT_ENCRYPTOR_PASSWORD` | Jasypt 加密密碼 | `diDsd]3FsGO@4dido` |
| `SPRING_PROFILES_ACTIVE` | 環境設定 | `prod` 或 `local` |
| `PORT` | 服務埠號 | `8080` |
| `TZ` | 時區設定 | `Asia/Taipei` |

### Zeabur 環境變數設定

在 Zeabur 控制台設定：

```env
# 必要設定
JASYPT_ENCRYPTOR_PASSWORD=diDsd]3FsGO@4dido
SPRING_PROFILES_ACTIVE=prod

# 選填設定
PORT=8080
TZ=Asia/Taipei
```

---

## 📦 部署到 Zeabur

### 步驟

1. **建置映像**
   ```bash
   ./build-all.sh
   ```

2. **確認映像已推送**
   ```bash
   docker images | grep coolapps
   ```

3. **更新 Zeabur 服務**
   - 前往 Zeabur 控制台
   - 選擇對應服務
   - 點擊「Redeploy」
   - 等待部署完成

4. **驗證部署**
   ```bash
   # 測試前端
   curl https://cool-apps.zeabur.app
   
   # 測試後端健康檢查
   curl https://cool-apps.zeabur.app/prod-api/system/config/list
   ```

---

## 🐛 常見問題

### Q1: 建置失敗顯示 "permission denied"

**解決方案**：
```bash
chmod +x build-*.sh
```

### Q2: 後端建置很慢

**原因**：後端包含 Maven 依賴下載和編譯，首次建置需要較長時間。

**解決方案**：
- 使用穩定的網路連線
- 等待 Maven 快取建立完成

### Q3: 映像推送失敗

**原因**：未登入 Docker Hub。

**解決方案**：
```bash
docker login
```

### Q4: Zeabur 部署後 405 錯誤

**原因**：Nginx 配置問題。

**解決方案**：
- 確認 `cheng-ui/nginx.conf` 已包含最新的反向代理配置
- 重新建置前端映像
- 重新部署

### Q5: 後端無法連接資料庫

**原因**：環境變數未正確設定。

**檢查項目**：
- `JASYPT_ENCRYPTOR_PASSWORD` 是否正確
- `SPRING_PROFILES_ACTIVE` 是否為 `prod`
- 資料庫連線資訊是否正確（在 `application-prod.yml` 中）

---

## 📊 版本號規則

### 格式

```
v{主版本}.{次版本}.{修訂版}-{日期}-{建置編號}
```

### 範例

```
v1.2.2-20251013-0008
│││││   │││││││  ││││
│││││   │││││││  │││└─ 建置編號（時分）
│││││   │││││││  │└─── 日
│││││   │││││││  └──── 月
│││││   │└┴┴┴┴──────── 年
│││││   └──────────── 日期分隔符
│││└┴──────────────── 修訂版
││└────────────────── 次版本
│└─────────────────── 主版本
└──────────────────── 版本前綴
```

---

## 🔍 查看建置資訊

### 查看本地映像

```bash
docker images | grep coolapps
```

### 查看映像詳細資訊

```bash
docker inspect android106/coolapps-frontend:latest
docker inspect android106/coolapps-backend:latest
```

### 查看建置歷史

```bash
docker history android106/coolapps-frontend:latest --no-trunc
```

---

## 📝 更新日誌

### v1.2.2 (2025-10-13)

- ✅ 修正 Nginx 反向代理配置，解決 405 錯誤
- ✅ 新增自動化建置腳本
- ✅ 優化版本號管理
- ✅ 完善部署文件

---

## 📞 支援

如有問題，請聯絡：
- 開發者：Cheng
- 專案：CoolApps 管理系統
- 版本：v1.2.2
