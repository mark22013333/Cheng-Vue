# 🚀 快速部署指南

## 方法一：使用自動化腳本（推薦）

### 1. 設定執行權限
```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/sql
chmod +x deploy_all.sh
```

### 2. 執行部署腳本
```bash
./deploy_all.sh your_database_name
```

**腳本會自動：**
- ✅ 依序執行所有 SQL 檔案
- ✅ 顯示執行進度
- ✅ 檢查執行結果
- ✅ 如有錯誤立即停止

---

## 方法二：手動逐一執行

### 步驟 1：備份資料庫
```bash
mysqldump -u root -p your_database > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 步驟 2：執行 SQL（依序執行）
```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/sql

# 1. 清理書籍髒資料
mysql -u root -p your_database < 01_fix_dirty_book_data.sql

# 2. 修正分類預設值
mysql -u root -p your_database < 02_fix_category_default.sql

# 3. 配置權限
mysql -u root -p your_database < 03_category_permissions.sql

# 4. 修正選單路由
mysql -u root -p your_database < 04_fix_category_menu.sql
```

---

## 部署後端

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue

# 1. 編譯
mvn clean package -DskipTests

# 2. 停止舊服務
pkill -f cheng-admin

# 3. 啟動新服務
cd cheng-admin/target
nohup java -jar cheng-admin.jar > /dev/null 2>&1 &

# 4. 檢查日誌
tail -f ../logs/sys-info.log
```

---

## 部署前端

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui

# 1. 編譯
npm run build:prod

# 2. 部署（如使用 Nginx）
# cp -r dist/* /usr/share/nginx/html/
```

---

## 快速驗證

### 測試 1：掃描 ISBN
```bash
# 掃描任意 ISBN，確認無 Duplicate entry 錯誤
# 例如：9789863877363
```

### 測試 2：分類管理
```bash
# 1. 點擊左側選單「分類管理」
# 2. 確認能正常顯示分類列表
# 3. 測試新增、修改、刪除、匯出功能
```

---

## 檔案說明

| 檔案名稱 | 說明 | 是否必需 |
|---------|------|---------|
| `00_DEPLOYMENT_README.md` | 完整部署說明 | 📖 參考 |
| `QUICK_START.md` | 快速部署指南（本檔案） | 📖 參考 |
| `01_fix_dirty_book_data.sql` | 清理書籍髒資料 | ✅ 必需 |
| `02_fix_category_default.sql` | 修正分類預設值 | ✅ 必需 |
| `03_category_permissions.sql` | 配置權限 | ✅ 必需 |
| `04_fix_category_menu.sql` | 修正選單路由 | ✅ 必需 |
| `deploy_all.sh` | 自動化部署腳本 | 🔧 工具 |

---

## 常見問題

### Q: 執行 SQL 時出現權限錯誤？
**A:** 確認使用的資料庫使用者有足夠的權限（CREATE、INSERT、UPDATE、DELETE）

### Q: 選單點擊還是沒反應？
**A:** 
1. 清除瀏覽器快取（Ctrl/Cmd + Shift + Delete）
2. 重新登入系統
3. 確認前端已重新編譯並部署

### Q: ISBN 掃描還是報錯？
**A:**
1. 確認所有 SQL 都已執行成功
2. 確認後端服務已重啟
3. 檢查後端日誌：`tail -f logs/sys-error.log`

---

## 需要幫助？

請參考 `00_DEPLOYMENT_README.md` 查看詳細說明和故障排查指南。

---

**祝部署順利！** 🎉
