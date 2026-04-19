# 圖片路徑顯示修正總結

## 問題描述
前端在 Zeabur 部署後，圖片顯示為 `src="undefined/profile/avatar/..."` 的問題。

## 根本原因
1. **環境變數未正確處理**：`process.env.VUE_APP_BASE_API` 在某些情況下為 `undefined`，導致路徑拼接錯誤
2. **後端路徑格式不一致**：後端返回的圖片路徑有多種格式（`/profile/xxx`, 絕對路徑等）
3. **缺乏統一處理邏輯**：各個組件各自處理圖片路徑，沒有統一的處理函數

## 解決方案

### 1. 建立統一的圖片路徑處理工具 ✅
**檔案**：`cheng-ui/src/utils/image.js`

提供 `getImageUrl(imagePath)` 函數，統一處理：
- HTTP/HTTPS 絕對 URL
- `/profile` 開頭的路徑
- 本機檔案系統絕對路徑（如 `/Users/cheng/uploadPath/...`）
- 相對路徑
- **防禦性處理 `undefined`**：使用 `process.env.VUE_APP_BASE_API || ''`

### 2. 修正核心模組 ✅

#### a. 使用者頭像處理（`cheng-ui/src/store/modules/user.js`）
```javascript
// 修正前
avatar = (isEmpty(avatar)) ? defAva : process.env.VUE_APP_BASE_API + avatar

// 修正後
if (avatar && avatar.startsWith('/profile')) {
  // 已經是完整路徑，不需要加前綴
  avatar = avatar
} else {
  avatar = (isEmpty(avatar)) ? defAva : (process.env.VUE_APP_BASE_API || '') + avatar
}
```

#### b. 圖片上傳組件（`cheng-ui/src/components/ImageUpload/index.vue`）
```javascript
// 修正前
baseUrl: process.env.VUE_APP_BASE_API,
uploadImgUrl: process.env.VUE_APP_BASE_API + this.action,

// 修正後
baseUrl: process.env.VUE_APP_BASE_API || '',
uploadImgUrl: (process.env.VUE_APP_BASE_API || '') + this.action,
```

### 3. 修正業務頁面 ✅

所有使用圖片的頁面統一改用 `getImageUrl` 函數：

- ✅ **庫存管理**（`views/inventory/management/index.vue`）
  - 匯入：`import { getImageUrl } from '@/utils/image'`
  - 使用：`methods: { getImageUrl, ... }`
  - 移除原有的自定義 `getImageUrl` 方法

- ✅ **掃描頁面**（`views/inventory/scan/index.vue`）
  - 匯入：`import { getImageUrl } from '@/utils/image'`
  - 使用：`methods: { getImageUrl, ... }`
  - 圖片顯示：`:src="getImageUrl(scanResult.imageUrl) || require('@/assets/images/profile.jpg')"`

- ✅ **使用者頭像**（`layout/components/Navbar.vue`）
  - 無需修改，使用 Vuex store 中已處理過的 avatar

### 4. 前端建置配置優化 ✅

**檔案**：`build-frontend.sh`
- 加入 `--progress=plain` 參數，顯示詳細建置日誌
- 便於除錯環境變數載入問題

## 修改檔案清單

### 新增檔案
- ✅ `cheng-ui/src/utils/image.js` - 圖片路徑處理工具

### 修改檔案
- ✅ `cheng-ui/src/store/modules/user.js` - 使用者頭像處理邏輯
- ✅ `cheng-ui/src/components/ImageUpload/index.vue` - 圖片上傳組件
- ✅ `cheng-ui/src/views/inventory/management/index.vue` - 庫存管理頁面
- ✅ `cheng-ui/src/views/inventory/scan/index.vue` - 掃描頁面
- ✅ `build-frontend.sh` - 建置腳本優化

## 測試要點

### 本地測試
1. 確認環境變數：`.env.production` 中 `VUE_APP_BASE_API = '/prod-api'`
2. 建置前端：`npm run build:prod`
3. 檢查建置輸出，確認沒有 `undefined` 字串

### Zeabur 部署測試
1. 使用 `AUTO_CONFIRM=true ./build-frontend.sh` 建置並推送
2. 部署後檢查以下頁面的圖片顯示：
   - ✅ 右上角使用者頭像
   - ✅ 個人中心頭像上傳
   - ✅ 庫存管理物品圖片
   - ✅ 掃描頁面物品圖片

### 預期結果
```html
<!-- 正確的圖片路徑 -->
<img src="/profile/avatar/2025/10/13/xxx.jpg">

<!-- 透過 Nginx 代理到後端 -->
/profile/* → http://coolapps-api.zeabur.internal:8080/profile/*
```

## Nginx 配置確認

**檔案**：`cheng-ui/nginx.conf`

```nginx
# 靜態資源代理（必須在 /prod-api/ 之前）
location /profile/ {
  proxy_pass http://coolapps-api.zeabur.internal:8080/profile/;
  proxy_http_version 1.1;
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  
  expires 30d;
  add_header Cache-Control "public, immutable";
}
```

## 後續改進建議

1. **統一後端返回格式**：建議後端統一返回 `/profile/xxx` 格式的相對路徑
2. **環境變數驗證**：在應用啟動時驗證必要的環境變數
3. **錯誤處理**：為圖片載入失敗提供更友好的 fallback
4. **效能優化**：考慮使用 CDN 或圖片壓縮服務

## 完成時間
2025-10-13 12:40

## 版本資訊
- 前端版本：v1.2.3
- 修正類型：fix - 修正圖片路徑顯示問題
