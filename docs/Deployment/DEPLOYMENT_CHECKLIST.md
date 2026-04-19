# Zeabur 部署檢查清單

## ✅ 已完成的修正

### 1. 圖片路徑顯示問題修正
- [x] 建立統一的圖片處理工具 (`src/utils/image.js`)
- [x] 修正使用者頭像處理邏輯 (`store/modules/user.js`)
- [x] 修正圖片上傳組件 (`components/ImageUpload/index.vue`)
- [x] 修正庫存管理頁面 (`views/inventory/management/index.vue`)
- [x] 修正掃描頁面 (`views/inventory/scan/index.vue`)
- [x] 加入環境變數防禦性處理（防止 `undefined`）

### 2. 建置優化
- [x] 加入 `--progress=plain` 顯示詳細建置日誌
- [x] 加入 `--no-cache` 強制重新建置（確保新程式碼生效）
- [x] 版本號更新至 `v1.2.3`

### 3. Nginx 配置
- [x] 設定 `/profile/` 靜態資源代理
- [x] 優先於 `/prod-api/` 處理
- [x] 加入快取設定（30天）

## 🚀 部署步驟

### 步驟 1：確認前端建置完成
```bash
# 檢查建置日誌
tail -f /tmp/build-no-cache.log

# 等待看到以下訊息：
# ✅ 映像建置成功！
# 📦 映像資訊：
#   - android106/coolapps-frontend:v1.2.3-20251013-xxxx
```

### 步驟 2：在 Zeabur 部署前端
1. 登入 Zeabur 控制台
2. 找到 `coolapps-frontend` 服務
3. 觸發重新部署或等待自動部署
4. 確認使用最新的映像版本

### 步驟 3：驗證部署結果

#### 3.1 檢查頁面元素
開啟瀏覽器開發者工具（F12），檢查圖片路徑：

**正確範例**：
```html
<img src="/profile/avatar/2025/10/13/xxx.jpg">
```

**錯誤範例**（已修正）：
```html
<img src="undefined/profile/avatar/xxx.jpg">
```

#### 3.2 測試功能頁面
- [ ] **右上角使用者頭像**：應正常顯示
- [ ] **個人中心 > 基本資料**：頭像上傳功能
- [ ] **庫存管理**：物品圖片列表顯示
- [ ] **庫存管理 > 詳情**：物品圖片預覽
- [ ] **掃描頁面**：掃描結果圖片顯示

#### 3.3 檢查網路請求
在瀏覽器 Network 面板中：

**圖片請求應該是**：
```
Request URL: https://your-domain.zeabur.app/profile/avatar/xxx.jpg
Status: 200 OK
```

**Nginx 代理應該轉發到**：
```
http://coolapps-api.zeabur.internal:8080/profile/avatar/xxx.jpg
```

### 步驟 4：測試頭像上傳功能

1. 登入系統
2. 進入「個人中心」
3. 點擊「基本資料」頁籤
4. 點擊頭像進行上傳
5. 選擇圖片並確認上傳
6. **檢查點**：
   - 上傳成功提示
   - 頭像立即顯示新圖片
   - 重新整理頁面後頭像仍正常顯示

### 步驟 5：測試庫存管理圖片

1. 進入「庫存管理 > 物品管理」
2. **檢查點**：
   - 列表中的物品圖片正常顯示
   - 點擊「詳情」查看大圖
   - 修改物品時可以上傳/更換圖片

## 🔍 故障排除

### 問題 1：圖片仍顯示 404
**可能原因**：
- 後端 `/profile/` 路徑配置問題
- 檔案實際不存在

**檢查方法**：
```bash
# 在後端容器中檢查檔案是否存在
docker exec -it <backend-container> ls -la /opt/cool-apps/uploadFile/avatar/
```

### 問題 2：圖片路徑仍有 `undefined`
**可能原因**：
- 前端快取未清除
- 使用了舊的映像版本

**解決方法**：
```bash
# 1. 清除瀏覽器快取（Ctrl+Shift+Delete）
# 2. 強制重新整理（Ctrl+F5）
# 3. 確認 Zeabur 使用的映像版本
```

### 問題 3：上傳圖片後無法顯示
**可能原因**：
- 後端儲存路徑與配置不一致
- Nginx 代理配置錯誤

**檢查後端配置**：
```yaml
# application-prod.yml
cheng:
  profile: /opt/cool-apps/uploadFile
```

**檢查 Nginx 配置**：
```nginx
location /profile/ {
  proxy_pass http://coolapps-api.zeabur.internal:8080/profile/;
}
```

## 📝 環境變數確認

### 前端環境變數
**檔案**：`cheng-ui/.env.production`
```bash
VUE_APP_BASE_API = '/prod-api'
```

### 後端環境變數（Zeabur）
需要在 Zeabur 設定：
```bash
JASYPT_ENCRYPTOR_PASSWORD=diDsd]3FsGO@4dido
SPRING_PROFILES_ACTIVE=prod
```

## 🎯 成功標準

所有以下項目都正常運作：

- ✅ 使用者頭像正常顯示和上傳
- ✅ 庫存管理物品圖片正常顯示
- ✅ 掃描頁面物品圖片正常顯示
- ✅ 圖片路徑不包含 `undefined`
- ✅ 圖片請求返回 200 狀態碼
- ✅ 重新整理頁面後圖片仍正常顯示

## 📞 如需協助

如果遇到問題，請檢查：
1. 瀏覽器控制台錯誤訊息
2. 網路請求詳情（Network 面板）
3. Zeabur 部署日誌
4. 後端容器日誌

---

**最後更新**：2025-10-13 13:10
**版本**：v1.2.3
**狀態**：前端建置中...
