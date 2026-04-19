# 指南與快速開始

本目錄包含快速入門指南、驗證清單和開發規範。

## 📚 文件列表

- **QUICK_START.md** - 快速開始指南（新手必讀）
- **FINAL_VERIFICATION.md** - 最終驗證清單
- **COMMIT_MESSAGE.txt** - Git Commit Message 撰寫規範

## 🚀 快速開始流程

### 1. 環境準備
```bash
# 安裝 Java 17
java -version

# 安裝 Maven
mvn -version

# 安裝 Node.js
node -v
npm -v
```

### 2. 複製專案
```bash
git clone https://github.com/mark22013333/Cheng-Vue.git
cd Cheng-Vue
```

### 3. 設定資料庫
```bash
# 建立資料庫
mysql -u root -p < docs/Scripts/setup-cool-test-db.sh

# Flyway 會自動執行 migration
```

### 4. 啟動後端
```bash
cd cheng-admin
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 5. 啟動前端
```bash
cd cheng-ui
npm install
npm run dev
```

### 6. 訪問應用
- 前端：http://localhost:80
- 後端：http://localhost:8080
- Swagger：http://localhost:8080/swagger-ui/index.html
- Druid：http://localhost:8080/druid/

## 📋 驗證清單

部署完成後，使用 [FINAL_VERIFICATION.md](./FINAL_VERIFICATION.md) 進行完整驗證：

- ✅ 應用程式正常啟動
- ✅ 資料庫連線成功
- ✅ Flyway 遷移完成
- ✅ 登入功能正常
- ✅ API 端點可訪問
- ✅ 排程任務執行正常

## 📝 開發規範

### Git Commit Message
請參考 [COMMIT_MESSAGE.txt](./COMMIT_MESSAGE.txt) 撰寫規範的提交訊息：

```
feat: 新增庫存掃描功能

InventoryController.java - 新增掃描 API 端點
ScanService.java - 實作 QR Code 解析邏輯
scan.vue - 新增前端掃描頁面
```

### 程式碼規範
- 使用繁體中文撰寫註解和文件
- 遵循 Java 命名慣例
- 使用 MyBatis 進行資料存取
- 採用 RESTful API 設計

## 🔗 延伸閱讀

### 功能開發
- [爬蟲開發指南](../Crawler/)
- [排程系統指南](../Schedule/)
- [庫存管理指南](../Inventory/)

### 部署維護
- [部署文件](../Deployment/)
- [Flyway 指南](../Flyway/)
- [問題排除](../BugFix/)

### 架構設計
- [系統架構](../Architecture/)
- [MyBatis 最佳實踐](../Architecture/MYBATIS_BEST_PRACTICES.md)
