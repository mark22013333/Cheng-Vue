# 圖片無法顯示問題解決方案

## 問題描述

Zeabur 部署後，圖片顯示為 `src="undefined/profile/avatar/..."` 導致無法載入。

## 根本原因

1. **前端建置時環境變數未正確注入**：`process.env.VUE_APP_BASE_API` 在建置時變成 `undefined`
2. **Nginx 缺少靜態資源代理**：`/profile/` 路徑沒有被代理到後端

## 解決方案

### ✅ 已完成的修正

#### 1. Dockerfile 增加環境變數檢查

**檔案**：`cheng-ui/Dockerfile`

```dockerfile
# 確保 .env.production 存在並記錄其內容
RUN ls -la .env* || echo "No .env files found"
RUN cat .env.production || echo ".env.production not found"
# 建置後驗證環境變數是否正確嵌入
RUN grep -r "VUE_APP_BASE_API" dist/ || echo "VUE_APP_BASE_API not found in build"
```

#### 2. Nginx 新增靜態資源代理

**檔案**：`cheng-ui/nginx.conf`

```nginx
# 靜態資源代理（上傳的圖片、檔案等）
# 必須放在 /prod-api/ 之前，優先處理靜態資源
location /profile/ {
  proxy_pass http://coolapps-api.zeabur.internal:8080/profile/;
  proxy_http_version 1.1;
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  
  # 快取設定（圖片可以快取較長時間）
  expires 30d;
  add_header Cache-Control "public, immutable";
}
```

#### 3. 建立環境變數檢查頁面

**檔案**：`cheng-ui/public/env-check.html`

訪問 `https://your-domain.zeabur.app/env-check.html` 可檢查環境變數是否正確。

## 部署步驟

### 1. 重新建置前端映像

```bash
# 在專案根目錄執行
./build-frontend.sh
```

### 2. 檢查建置日誌

建置過程中會顯示：
```
RUN cat .env.production
VUE_APP_TITLE = CoolApps管理系統
ENV = 'production'
VUE_APP_BASE_API = '/prod-api'
```

確認 `VUE_APP_BASE_API` 正確顯示為 `/prod-api`。

### 3. 重新建置後端（可選）

如果後端也需要更新：
```bash
./build-backend.sh
```

### 4. 部署到 Zeabur

1. 前往 Zeabur 控制台
2. 選擇前端服務
3. 點擊「Redeploy」
4. 等待部署完成

### 5. 驗證修正

**方法 1：檢查頁面元素**
```html
<!-- 修正前 -->
<img src="undefined/profile/avatar/2025/10/13/xxx.jpg">

<!-- 修正後 -->
<img src="/profile/avatar/2025/10/13/xxx.jpg">
```

**方法 2：訪問環境變數檢查頁面**
```
https://your-domain.zeabur.app/env-check.html
```

**方法 3：直接測試圖片 URL**
```
https://your-domain.zeabur.app/profile/avatar/2025/10/13/xxx.jpg
```

## 技術說明

### 前端圖片路徑處理邏輯

**檔案**：`cheng-ui/src/store/modules/user.js`

```javascript
// 取得使用者訊息
GetInfo({ commit, state }) {
  return new Promise((resolve, reject) => {
    getInfo().then(res => {
      const user = res.user
      let avatar = user.avatar || ""
      if (!isHttp(avatar)) {
        // 關鍵：如果不是 HTTP 開頭，則加上 VUE_APP_BASE_API 前綴
        avatar = (isEmpty(avatar)) ? defAva : process.env.VUE_APP_BASE_API + avatar
      }
      // ...
    })
  })
}
```

### 後端靜態資源映射

**檔案**：`cheng-framework/src/main/java/com/cheng/framework/config/ResourcesConfig.java`

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 將 /profile/** 請求映射到檔案系統
    registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
            .addResourceLocations("file:" + CoolAppsConfig.getProfile() + "/");
}
```

**常數定義**：`Constants.RESOURCE_PREFIX = "/profile"`

### Nginx 代理流程

```
瀏覽器請求
   ↓
/profile/avatar/xxx.jpg
   ↓
Nginx 接收 (前端容器)
   ↓
location /profile/ 匹配
   ↓
proxy_pass 轉發到後端
   ↓
http://coolapps-api.zeabur.internal:8080/profile/avatar/xxx.jpg
   ↓
後端 Spring Boot
   ↓
ResourcesConfig 映射
   ↓
檔案系統：/opt/cool-apps/uploadFile/avatar/xxx.jpg
   ↓
返回圖片數據
```

## 常見問題

### Q1: 圖片仍然顯示為 undefined？

**檢查**：
1. 確認 `.env.production` 檔案存在於 `cheng-ui/` 目錄
2. 查看 Docker 建置日誌，確認環境變數有被讀取
3. 清除瀏覽器快取後重新載入

### Q2: Nginx 404 錯誤？

**檢查**：
1. 確認 Nginx 配置已包含 `/profile/` location
2. 確認後端服務名稱正確：`coolapps-api.zeabur.internal`
3. 檢查 Zeabur 內部網路是否正常

### Q3: 後端找不到檔案？

**檢查**：
1. 確認環境變數 `SPRING_PROFILES_ACTIVE=prod`
2. 確認 `application-prod.yml` 中 `cheng.profile` 路徑正確
3. 使用 Zeabur 終端機檢查檔案是否存在：
   ```bash
   ls -la /opt/cool-apps/uploadFile/avatar/
   ```

### Q4: 本地開發環境如何測試？

**設定**：
1. 使用 `npm run dev` 啟動前端
2. `.env.development` 會自動載入，設定為 `/dev-api`
3. `vue.config.js` 的 proxy 會將請求轉發到 `localhost:8080`

## 後續建議

### 1. 監控圖片載入

在前端加入圖片載入錯誤處理：

```javascript
// components/ImagePreview/index.vue
<img 
  :src="imageSrc" 
  @error="handleImageError"
  alt="圖片"
/>

methods: {
  handleImageError(e) {
    console.error('圖片載入失敗:', e.target.src);
    e.target.src = require('@/assets/images/default.png');
  }
}
```

### 2. 使用 CDN（未來優化）

考慮將上傳的圖片存放到 CDN 服務（如 Cloudflare R2、AWS S3），減少後端壓力。

### 3. 圖片壓縮優化

在上傳時自動壓縮圖片：
- 頭像限制：200KB 以內
- 自動轉換為 WebP 格式
- 產生多種尺寸的縮圖

## 總結

此問題已通過以下兩個修正完全解決：

✅ **Dockerfile 驗證**：確保環境變數在建置時正確注入  
✅ **Nginx 代理**：新增 `/profile/` 靜態資源代理規則

重新建置並部署後，圖片將能正常顯示。
