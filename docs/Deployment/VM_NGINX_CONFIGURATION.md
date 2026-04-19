# VM 環境 Nginx 配置說明

> **重要記錄**：避免未來混淆和錯誤修改

---

## ⚠️ 關鍵配置（2025-11-01 確認）

### VM 環境特殊配置

**問題**：VM 環境無法到達登入頁面

**根本原因**：Nginx 的 `proxy_pass` 路徑錯誤

**解決方案**：只需修改 Nginx 配置，**不需要修改後端配置**

---

## ✅ 正確的 Nginx 配置

### 檔案：`cheng.deploy/nginx/proxy-ssl-corrected.conf`

```nginx
# Cheng-Vue 後端應用程式 - 處理 /prod-api/ 路徑
# ⚠️ 重要：VM 環境後端使用 /apps 路徑，必須轉發到 /apps/
location /prod-api/ {
    proxy_pass http://127.0.0.1:8080/apps/;  # ← 關鍵！必須是 /apps/
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    ...
}
```

**說明**：
- 前端呼叫：`/prod-api/login`
- Nginx 轉發：`http://127.0.0.1:8080/apps/login`
- 後端接收：`/apps/login`

---

## ❌ 常見錯誤

### 錯誤 1：誤改為根路徑

```nginx
location /prod-api/ {
    proxy_pass http://127.0.0.1:8080/;  # ❌ 錯誤！
}
```

**結果**：無法到達登入頁面，後端返回 404

### 錯誤 2：錯誤地修改後端配置

```yaml
# application-vm.yml
server:
  servlet:
    context-path: /apps  # ❌ 不需要在這裡設定！
```

**說明**：VM 環境的後端 context-path 可能是在**啟動時指定**的，不是在配置檔案中設定的。

---

## 🎯 正確的修改方式

### 只需修改一個檔案

**檔案**：`cheng.deploy/nginx/proxy-ssl-corrected.conf`

**修改內容**：
```diff
location /prod-api/ {
-   proxy_pass http://127.0.0.1:8080/;
+   proxy_pass http://127.0.0.1:8080/apps/;
}
```

### 不需要修改

- ❌ `application-vm.yml` - 不需要加上 `context-path`
- ❌ `application.yml` - 不需要修改
- ❌ 後端 Java 程式碼 - 不需要修改
- ❌ 前端程式碼 - 不需要修改

---

## 📋 環境對照表

| 環境 | 後端 Context Path | Nginx proxy_pass | 說明 |
|------|------------------|------------------|------|
| **Local** | `/` | 不使用 Nginx | 本地開發環境 |
| **VM** | `/apps` (啟動時指定) | `/apps/` | **只改 Nginx** |
| **Zeabur** | `/` | `/` | Zeabur 自動配置 |

---

## 🔍 檢查清單

部署 VM 環境時必須確認：

### Nginx 配置
- [ ] `/prod-api/` 的 `proxy_pass` 是 `http://127.0.0.1:8080/apps/`
- [ ] `/webhook/line/` 的 `proxy_pass` 是 `http://127.0.0.1:8080/webhook/line/`（不需要 /apps）

### 後端配置
- [ ] `application-vm.yml` **不需要**設定 `context-path`
- [ ] 後端啟動時可能已經指定了 context-path 參數

### 測試驗證
```bash
# 測試後端 API
curl -I http://localhost:8080/apps/

# 測試 Nginx 代理
curl -I https://your-domain.com/prod-api/getInfo

# 測試登入頁面
curl -I https://your-domain.com/
```

---

## 📝 記錄

### 2025-11-01
- **問題**：VM 環境無法到達登入頁面
- **原因**：Nginx `proxy_pass` 路徑錯誤（應該是 `/apps/` 而非 `/`）
- **解決**：只修改 Nginx 配置，不修改後端配置
- **教訓**：不要在沒有確認的情況下隨意修改配置檔案

---

## ⚠️ 注意事項

1. **VM 環境的 context-path 可能不在配置檔案中**
   - 可能在啟動腳本中指定
   - 可能在 systemd service 檔案中指定
   - 可能在環境變數中指定

2. **只改 Nginx 就好**
   - 前端路徑：由 Nginx 處理
   - 後端路徑：已經在執行時配置好了
   - 不要假設需要修改後端配置

3. **修改前先確認**
   - 檢查後端實際執行的 context-path
   - 查看後端日誌
   - 測試實際的 API 路徑

---

**維護者**：Cheng  
**最後更新**：2025-11-01  
**重要性**：⭐⭐⭐⭐⭐（極高）
