# 📦 部署 SQL 執行指南

## 📋 本次更新內容

### 1. 書籍資料髒資料修復
- 清理書籍資訊與物品不一致的問題
- 移除孤立的庫存記錄
- 避免 ISBN 和 item_id 重複錯誤

### 2. 分類管理功能優化
- 移除父子分類功能（簡化為扁平結構）
- 新增刪除前物品關聯檢查
- 新增 Excel 匯出功能
- 新增完整權限控制
- 確保預設分類唯一性

### 3. 選單導航修復
- 修正左側選單「分類管理」點擊無反應問題
- 自動切換到分類管理頁籤

---

## 🚀 SQL 執行順序

### ⚠️ 執行前準備

1. **備份資料庫（重要！）**
```bash
mysqldump -u root -p your_database > backup_$(date +%Y%m%d_%H%M%S).sql
```

2. **確認資料庫連線**
```bash
mysql -u root -p your_database
```

---

### 📝 依序執行以下 SQL 檔案

#### 1️⃣ 清理書籍髒資料
```bash
mysql -u root -p your_database < 01_fix_dirty_book_data.sql
```

**功能：**
- ✅ 清理書籍資訊的錯誤關聯
- ✅ 刪除孤立的庫存記錄
- ✅ 驗證清理結果

**預期結果：**
- 書籍資訊中指向不存在物品的記錄被清空
- 孤立的庫存記錄被刪除
- 查詢結果顯示 0 筆問題資料

---

#### 2️⃣ 修正分類預設值
```bash
mysql -u root -p your_database < 02_fix_category_default.sql
```

**功能：**
- ✅ 設定「書籍」(ID: 1000) 為預設分類
- ✅ 清除其他分類的預設標記
- ✅ 確保只有一個預設分類

**預期結果：**
- 只有「書籍」分類的 is_default 顯示為「是」
- 其他分類的備註中沒有「預設分類」標記

---

#### 3️⃣ 配置分類管理權限
```bash
mysql -u root -p your_database < 03_category_permissions.sql
```

**功能：**
- ✅ 新增分類查詢權限
- ✅ 新增分類新增權限
- ✅ 新增分類修改權限
- ✅ 新增分類刪除權限
- ✅ 新增分類匯出權限
- ✅ 為管理員角色授權

**預期結果：**
- 新增 5 個權限記錄
- 管理員角色的 admin_auth 顯示為「已授權」

---

#### 4️⃣ 修正選單路由
```bash
mysql -u root -p your_database < 04_fix_category_menu.sql
```

**功能：**
- ✅ 更新分類管理選單的 path 為 'category'
- ✅ 更新 component 為 'inventory/management/index'

**預期結果：**
- 分類管理選單的 path 為 'category'
- component 為 'inventory/management/index'

---

## 🔍 執行後驗證

### 1. 檢查書籍資料
```sql
-- 應該返回 0 筆記錄
SELECT COUNT(*) FROM inv_book_info b
LEFT JOIN inv_item i ON b.item_id = i.item_id AND i.del_flag = '0'
WHERE b.item_id IS NOT NULL AND (i.item_id IS NULL OR i.del_flag = '2');
```

### 2. 檢查孤立庫存記錄
```sql
-- 應該返回 0 筆記錄
SELECT COUNT(*) FROM inv_stock s
LEFT JOIN inv_item i ON s.item_id = i.item_id AND i.del_flag = '0'
WHERE i.item_id IS NULL OR i.del_flag = '2';
```

### 3. 檢查預設分類
```sql
-- 應該只返回 1 筆「書籍」分類
SELECT category_name FROM inv_category 
WHERE remark LIKE '%預設分類%' AND del_flag = '0';
```

### 4. 檢查權限
```sql
-- 應該返回 5 個權限
SELECT COUNT(*) FROM sys_menu 
WHERE perms LIKE 'inventory:category:%' 
  AND perms != 'inventory:category:list';
```

### 5. 檢查選單路由
```sql
-- path 應該是 'category'
SELECT path, component FROM sys_menu 
WHERE perms = 'inventory:category:list';
```

---

## 🎯 後端部署

### 1. 重新編譯
```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn clean package -DskipTests
```

### 2. 停止服務
```bash
pkill -f cheng-admin
```

### 3. 啟動服務
```bash
cd cheng-admin/target
nohup java -jar cheng-admin.jar > /dev/null 2>&1 &
```

### 4. 檢查日誌
```bash
tail -f logs/sys-info.log
```

---

## 🎨 前端部署

### 1. 重新編譯
```bash
cd cheng-ui
npm run build:prod
```

### 2. 部署到 Nginx（如適用）
```bash
# 複製建置檔案到 Nginx 目錄
cp -r dist/* /usr/share/nginx/html/
```

### 3. 清除瀏覽器快取
- 按 `Ctrl + Shift + Delete`（或 `Cmd + Shift + Delete`）
- 清除快取和 Cookies
- 重新登入

---

## 🧪 功能測試清單

### ✅ 測試 1：ISBN 掃描
1. 掃描 ISBN: `9789863877363`
2. 確認能正常建立或取得書籍
3. 檢查日誌無 `Duplicate entry` 錯誤

### ✅ 測試 2：分類管理
1. 點擊左側選單「分類管理」
2. 確認能正常導航到分類管理頁籤
3. 測試新增、修改、刪除分類
4. 測試預設分類切換
5. 測試刪除前物品關聯檢查
6. 測試 Excel 匯出

### ✅ 測試 3：物品管理
1. 新增物品，選擇不同分類
2. 刪除有物品的分類
3. 確認顯示錯誤訊息：「分類正被 X 個物品使用，無法刪除」

---

## 📊 修改檔案清單

### 後端檔案
1. `/cheng-system/src/main/resources/mapper/system/InvCategoryMapper.xml`
2. `/cheng-system/src/main/java/com/cheng/system/service/impl/InvCategoryServiceImpl.java`
3. `/cheng-system/src/main/java/com/cheng/system/service/impl/BookItemServiceImpl.java`
4. `/cheng-admin/src/main/java/com/cheng/web/controller/inventory/InvCategoryController.java`
5. `/cheng-admin/src/main/java/com/cheng/web/controller/inventory/InvScanController.java`
6. `/cheng-system/src/main/java/com/cheng/system/domain/InvCategory.java`

### 前端檔案
1. `/cheng-ui/src/views/inventory/management/components/CategoryManagement.vue`
2. `/cheng-ui/src/views/inventory/management/index.vue`
3. `/cheng-ui/src/router/index.js`

---

## ❌ 故障排查

### 問題 1：SQL 執行失敗
**解決方案：**
- 檢查資料庫連線
- 確認使用正確的資料庫名稱
- 檢查是否有足夠的權限

### 問題 2：選單點擊還是無反應
**解決方案：**
1. 清除瀏覽器快取
2. 重新登入系統
3. 檢查前端是否正確編譯和部署

### 問題 3：掃描 ISBN 還是報錯
**解決方案：**
1. 確認 SQL 已執行成功
2. 確認後端服務已重啟
3. 檢查日誌確認是否還有其他錯誤

---

## 📞 支援

如遇到問題：
1. 檢查 SQL 執行結果
2. 檢查後端日誌：`tail -f logs/sys-error.log`
3. 檢查瀏覽器控制台錯誤
4. 確認所有步驟都已正確執行

---

## ✅ 部署完成確認

- [ ] 資料庫已備份
- [ ] 4 個 SQL 檔案已按順序執行
- [ ] 所有驗證查詢都返回預期結果
- [ ] 後端已重新編譯並重啟
- [ ] 前端已重新編譯並部署
- [ ] 瀏覽器快取已清除
- [ ] ISBN 掃描功能正常
- [ ] 分類管理選單點擊正常
- [ ] 分類 CRUD 功能正常
- [ ] Excel 匯出功能正常

---

**部署完成後請執行完整的功能測試！** 🎉
