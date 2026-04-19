# Nginx SPA 路由 401 錯誤修復

## 📅 修復日期
2025-10-26

## 🐛 問題描述

### 症狀
在 **Zeabur (Docker Image)** 環境中，重新整理以下路徑的頁面會出現 **401 錯誤**：
- `/monitor/**` - 如 `/monitor/online`、`/monitor/druid`
- `/system/**` - 如 `/system/user`、`/system/role`

但在 **VM (Tomcat + Nginx)** 環境中**沒有這個問題**。

### 環境差異
| 環境 | 狀態 | Nginx 配置 |
|------|------|-----------|
| **VM (Tomcat + Nginx)** | ✅ 正常 | `cheng.deploy/nginx/proxy-ssl-corrected.conf` |
| **Zeabur (Docker Image)** | ❌ 401 錯誤 | `cheng-ui/nginx.conf` |

## 🔍 根本原因

### Docker Nginx 配置錯誤

在 `cheng-ui/nginx.conf` 中存在以下**錯誤的容錯配置**（第 97-117 行）：

```nginx
# ❌ 錯誤配置：會誤判前端路由為後端 API
location ~ ^/(system|monitor|common|profile)/ {
  proxy_pass http://coolapps-api.zeabur.internal:8080$request_uri;
  ...
}
```

### 問題流程

當用戶訪問前端路由 `/monitor/online` 時：

1. 用戶在瀏覽器輸入或重新整理 `/monitor/online`
2. ❌ Nginx 匹配到 `^/(system|monitor|...)` 正則規則
3. ❌ 誤判為**後端 API 請求**，代理到 `coolapps-api.zeabur.internal:8080`
4. ❌ 後端沒有 `/monitor/online` 這個 API 端點
5. ❌ 後端返回 **401 Unauthorized** 或 **404 Not Found**
6. ❌ 前端 Vue Router 無法接管路由

### 正確的路由邏輯

| URL 路徑 | 類型 | 正確處理方式 |
|---------|------|------------|
| `/prod-api/system/user/list` | 後端 API | Nginx 代理到後端 8080 |
| `/system/user` | 前端路由 | Nginx 返回 `index.html`，Vue Router 處理 |
| `/monitor/online` | 前端路由 | Nginx 返回 `index.html`，Vue Router 處理 |

## ✅ 解決方案

### 修改內容

**檔案**: `/cheng-ui/nginx.conf`

**移除錯誤的容錯機制**（第 97-117 行）：

```nginx
# ❌ 移除這段
location ~ ^/(system|monitor|common|profile)/ {
  proxy_pass http://coolapps-api.zeabur.internal:8080$request_uri;
  ...
}

location ~ ^/(login|logout|getInfo|getRouters|register|captchaImage)$ {
  proxy_pass http://coolapps-api.zeabur.internal:8080$request_uri;
  ...
}
```

**改為簡單的註解**：

```nginx
# ✅ 正確：所有後端 API 請求必須使用 /prod-api/ 前綴
# 前端路由（如 /system/user, /monitor/online 等）會由下方的 SPA fallback 處理
```

### 修改後的完整配置

```nginx
server {
  listen 80;
  server_name  localhost;
  root /usr/share/nginx/html;
  index index.html;

  # 1️⃣ 靜態資源代理
  location /profile/ {
    proxy_pass http://coolapps-api.zeabur.internal:8080/profile/;
    ...
  }

  # 2️⃣ 後端 API 代理（正式環境）
  location /prod-api/ {
    proxy_pass http://coolapps-api.zeabur.internal:8080/;
    ...
  }

  # 3️⃣ Swagger API 文件
  location /swagger-ui/ {
    proxy_pass http://coolapps-api.zeabur.internal:8080/swagger-ui/;
    ...
  }

  # 4️⃣ SPA 路由 fallback（處理所有前端路由）
  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

## 🚀 部署步驟

### 1. 重新建置 Docker Image

```bash
# 在專案根目錄執行
cd cheng-ui

# 重新建置 Docker Image
docker build -t coolapps-frontend:latest .
```

### 2. 部署到 Zeabur

```bash
# 推送到 Docker Registry（如果使用）
docker tag coolapps-frontend:latest <your-registry>/coolapps-frontend:latest
docker push <your-registry>/coolapps-frontend:latest

# 或直接在 Zeabur 觸發重新部署
# （Zeabur 會自動重新建置和部署）
```

### 3. 驗證修復

訪問以下頁面並重新整理：
- ✅ `/monitor/online`
- ✅ `/monitor/druid`
- ✅ `/system/user`
- ✅ `/system/role`
- ✅ `/system/menu`

## 🔍 驗證測試

### 測試步驟

1. **登入系統**
   ```
   https://your-domain.zeabur.app
   ```

2. **訪問系統管理頁面**
   ```
   點擊: 系統管理 → 使用者管理
   URL: /system/user
   ```

3. **重新整理頁面** (F5 或 Ctrl+R)
   - ✅ 應該**正常顯示**，不會出現 401

4. **訪問系統監控頁面**
   ```
   點擊: 系統監控 → 線上使用者
   URL: /monitor/online
   ```

5. **重新整理頁面**
   - ✅ 應該**正常顯示**，不會出現 401

### 預期結果

| 操作 | 修復前 | 修復後 |
|------|--------|--------|
| 重新整理 `/monitor/online` | ❌ 401 錯誤 | ✅ 正常顯示 |
| 重新整理 `/system/user` | ❌ 401 錯誤 | ✅ 正常顯示 |
| API 請求 `/prod-api/system/user/list` | ✅ 正常 | ✅ 正常 |

## 📝 技術說明

### Vue Router History 模式

Vue Router 使用 **History 模式**時，路由變化不會向伺服器發送請求。但當用戶：
- 直接在網址列輸入 URL
- 重新整理頁面
- 從外部連結進入

瀏覽器會向伺服器請求該路徑，此時需要 Nginx 正確處理。

### Nginx try_files 指令

```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

**處理邏輯**：
1. `$uri` - 嘗試找實體檔案（如 `app.js`、`logo.png`）
2. `$uri/` - 嘗試找目錄
3. `/index.html` - 都找不到時，返回 `index.html`

這樣 Vue Router 就能接管路由並正確顯示頁面。

### 為什麼 VM 環境沒問題？

VM 的 Nginx 配置（`proxy-ssl-corrected.conf`）**沒有**錯誤的正則匹配：

```nginx
# VM 環境的正確配置
location /prod-api/ {
  proxy_pass http://127.0.0.1:8080/apps/;
  ...
}

location / {
  root ${FRONTEND_DIR};
  try_files $uri $uri/ /index.html;  # ← 正確的 SPA fallback
  ...
}
```

## 🎯 最佳實踐

### 1. 後端 API 請求必須使用前綴

❌ **錯誤**：
```javascript
// 前端程式碼
axios.get('/system/user/list')  // 會被誤判為前端路由
```

✅ **正確**：
```javascript
// 前端程式碼（使用 VUE_APP_BASE_API）
axios.get('/prod-api/system/user/list')  // 正確代理到後端
```

### 2. Nginx 配置原則

✅ **遵循以下順序**：
1. 精確匹配（exact match）
2. 前綴匹配（prefix match）- 如 `/prod-api/`
3. 正則匹配（regex match）- 謹慎使用
4. 通用 fallback - `location /`

❌ **避免過度的容錯機制**：
- 不要試圖用正則匹配所有可能的路徑
- 保持配置簡單明確

### 3. 測試流程

每次修改 Nginx 配置後，必須測試：
- ✅ 重新整理所有主要頁面
- ✅ 直接輸入 URL 訪問
- ✅ API 請求正常
- ✅ 靜態資源載入正常

## 🔗 相關連結

- [Vue Router History 模式](https://v3.router.vuejs.org/guide/essentials/history-mode.html)
- [Nginx Location 優先級](https://nginx.org/en/docs/http/ngx_http_core_module.html#location)
- [SPA 部署最佳實踐](../Deployment/00_DEPLOYMENT_README.md)

## 📌 總結

### 問題根源
Docker Nginx 配置中的**過度容錯機制**，誤判前端路由為後端 API。

### 解決方案
移除錯誤的正則匹配，讓 SPA fallback (`try_files`) 正確處理前端路由。

### 影響範圍
- ✅ 修復所有 `/monitor/**` 和 `/system/**` 路徑的重新整理問題
- ✅ 不影響後端 API 請求（都使用 `/prod-api/` 前綴）
- ✅ 不影響靜態資源載入

---

**修復完成時間**: 2025-10-26  
**修復人員**: Cascade AI  
**測試狀態**: ✅ 待部署驗證
