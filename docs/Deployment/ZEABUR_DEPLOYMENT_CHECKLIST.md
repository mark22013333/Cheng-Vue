# Zeabur Docker 部署檢查清單

## 📋 修復內容總結

### 問題
重新整理 `/monitor/**` 和 `/system/**` 頁面出現 **401 錯誤**

### 根本原因
Docker Nginx 配置中存在錯誤的正則匹配，誤判前端路由為後端 API

### 修復檔案
- ✅ `/cheng-ui/nginx.conf` - 移除錯誤的容錯機制

## 🚀 部署步驟

### 1. 確認修改內容

```bash
# 檢查 nginx.conf 是否已修復
git diff cheng-ui/nginx.conf
```

應該看到移除了以下內容：
```nginx
- location ~ ^/(system|monitor|common|profile)/ {
-   proxy_pass http://coolapps-api.zeabur.internal:8080$request_uri;
-   ...
- }
```

### 2. 提交變更

```bash
git add cheng-ui/nginx.conf
git commit -m "fix: 修正 Docker Nginx 配置導致的 SPA 路由 401 錯誤

移除錯誤的正則匹配規則，避免前端路由被誤判為後端 API。

影響檔案:
- cheng-ui/nginx.conf - 移除容錯機制，使用正確的 SPA fallback

修復問題:
- 重新整理 /monitor/** 頁面不再出現 401
- 重新整理 /system/** 頁面不再出現 401
"
git push
```

### 3. Zeabur 自動部署

Zeabur 會自動偵測到 Git 變更並觸發重新部署：

1. 前往 [Zeabur Dashboard](https://zeabur.com/dashboard)
2. 選擇你的專案
3. 等待自動部署完成（約 3-5 分鐘）
4. 查看部署日誌確認成功

### 4. 驗證修復

#### 4.1 登入系統

訪問你的 Zeabur 部署網址：
```
https://your-app.zeabur.app
```

#### 4.2 測試重新整理功能

| 測試項目 | 操作 | 預期結果 |
|---------|------|---------|
| **系統管理** | 訪問 `/system/user` 並重新整理 | ✅ 正常顯示 |
| **角色管理** | 訪問 `/system/role` 並重新整理 | ✅ 正常顯示 |
| **選單管理** | 訪問 `/system/menu` 並重新整理 | ✅ 正常顯示 |
| **線上使用者** | 訪問 `/monitor/online` 並重新整理 | ✅ 正常顯示 |
| **資料監控** | 訪問 `/monitor/druid` 並重新整理 | ✅ 正常顯示 |
| **定時任務** | 訪問 `/monitor/job` 並重新整理 | ✅ 正常顯示 |

#### 4.3 測試 API 功能

| 測試項目 | 操作 | 預期結果 |
|---------|------|---------|
| **使用者列表** | 系統管理 → 使用者管理 → 查詢 | ✅ 資料正常載入 |
| **新增使用者** | 點擊「新增」按鈕 | ✅ 表單正常顯示 |
| **線上使用者** | 系統監控 → 線上使用者 | ✅ 列表正常顯示 |
| **定時任務** | 系統監控 → 定時任務 | ✅ 任務列表正常 |

## 🔍 問題排查

### 如果仍然出現 401

#### 檢查 1：確認 Nginx 配置生效

```bash
# SSH 進入 Zeabur 容器（如果可能）
docker exec -it <container-id> sh

# 檢查 Nginx 配置
cat /etc/nginx/conf.d/default.conf

# 應該看到正確的配置，沒有錯誤的 location 規則
```

#### 檢查 2：清除瀏覽器快取

```
1. 開啟瀏覽器開發者工具（F12）
2. 右鍵點擊重新整理按鈕
3. 選擇「清除快取並強制重新整理」
```

#### 檢查 3：查看網路請求

```
1. 開啟開發者工具 → Network 標籤
2. 重新整理頁面
3. 檢查失敗的請求：
   - 如果是 /monitor/online 直接 401 → Nginx 配置問題
   - 如果是 /prod-api/... 401 → 後端認證問題
```

### 常見問題

#### Q1: 部署後仍然 401

**可能原因**：Docker Image 快取未更新

**解決方案**：
```bash
# 強制重新建置
zeabur deploy --force-rebuild
```

#### Q2: 某些頁面正常，某些頁面 401

**可能原因**：權限問題，非 Nginx 配置問題

**解決方案**：檢查使用者角色權限設定

#### Q3: API 請求正常，但頁面重新整理 401

**可能原因**：Nginx 配置未正確更新

**解決方案**：確認 `nginx.conf` 已推送到 Git，Zeabur 已重新部署

## 📊 驗證檢查表

完成以下檢查項目：

### 部署前
- [ ] 確認 `cheng-ui/nginx.conf` 已修改
- [ ] 移除了錯誤的 `location ~ ^/(system|monitor|...)` 規則
- [ ] Git commit 並 push 到遠端

### 部署中
- [ ] Zeabur 自動觸發部署
- [ ] 部署日誌無錯誤
- [ ] 前端服務啟動成功

### 部署後
- [ ] 訪問首頁正常
- [ ] 登入功能正常
- [ ] `/system/user` 重新整理 ✅
- [ ] `/system/role` 重新整理 ✅
- [ ] `/monitor/online` 重新整理 ✅
- [ ] `/monitor/druid` 重新整理 ✅
- [ ] API 請求功能正常
- [ ] 無 Console 錯誤

## 🎯 技術說明

### 為什麼 VM 環境沒問題？

VM 環境的 Nginx 配置（`proxy-ssl-corrected.conf`）**沒有**錯誤的正則匹配：

```nginx
# VM 配置（正確）
location /prod-api/ {
  proxy_pass http://127.0.0.1:8080/apps/;
}

location / {
  try_files $uri $uri/ /index.html;  # ← SPA fallback
}
```

### Docker 配置錯誤的原因

早期為了「容錯」而加入的規則，結果造成反效果：

```nginx
# Docker 配置（錯誤）
location ~ ^/(system|monitor|...)/ {
  proxy_pass http://coolapps-api.zeabur.internal:8080$request_uri;
  # ← 誤判前端路由為後端 API
}
```

### 正確的架構

```
瀏覽器請求
    ↓
┌─────────────────────────────┐
│  是否為靜態資源？             │
│  (js, css, png, etc.)       │
└────┬─YES──────────NO─────┬──┘
     ↓                      ↓
  返回檔案         ┌──────────────────┐
                   │ 是否為 API 請求？  │
                   │ (/prod-api/...)   │
                   └─┬─YES────NO──┬────┘
                     ↓            ↓
              代理到後端    返回 index.html
              8080 port    (Vue Router 處理)
```

## 📝 記錄

### 修復日期
2025-10-26

### 修復內容
- 移除 `cheng-ui/nginx.conf` 中的錯誤容錯規則
- 保留正確的 SPA fallback 機制

### 影響範圍
- ✅ 所有 `/monitor/**` 路徑
- ✅ 所有 `/system/**` 路徑
- ✅ 不影響 API 請求（`/prod-api/**`）
- ✅ 不影響靜態資源

### 參考文件
- [NGINX_SPA_ROUTING_FIX.md](../BugFix/NGINX_SPA_ROUTING_FIX.md)
- [Vue Router History 模式](https://v3.router.vuejs.org/guide/essentials/history-mode.html)

---

**檢查清單版本**: 1.0  
**最後更新**: 2025-10-26
