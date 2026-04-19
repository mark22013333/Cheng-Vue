# 定時任務範本功能部署檢查清單

## 📋 部署前檢查

### 後端檢查

#### 1. 檔案確認

- [ ] `ScheduledTaskType.java` 已建立
  - 路徑: `/cheng-quartz/src/main/java/com/cheng/quartz/enums/ScheduledTaskType.java`
  - 包含 5 種爬蟲任務類型

- [ ] `ScheduledTaskTypeController.java` 已建立
  - 路徑: `/cheng-quartz/src/main/java/com/cheng/quartz/controller/ScheduledTaskTypeController.java`
  - 提供 4 個 REST API 端點

- [ ] `CrawlerTask.java` 已更新
  - 路徑: `/cheng-quartz/src/main/java/com/cheng/quartz/task/CrawlerTask.java`
  - 支援動態爬蟲類型

#### 2. 編譯檢查

```bash
cd cheng-quartz
mvn clean compile
```

- [ ] 編譯成功，無錯誤
- [ ] 無警告訊息

#### 3. API 端點測試

啟動專案後測試：

```bash
# 測試 1: 取得所有任務類型
curl http://localhost:8080/monitor/job/types

# 測試 2: 取得分類列表
curl http://localhost:8080/monitor/job/types/categories

# 測試 3: 取得特定任務詳情
curl http://localhost:8080/monitor/job/types/crawler_run
```

- [ ] API 可正常存取
- [ ] 回應格式正確
- [ ] 資料內容完整

### 前端檢查

#### 1. 檔案確認

- [ ] `jobType.js` 已建立
  - 路徑: `/cheng-ui/src/api/monitor/jobType.js`
  - 包含 4 個 API 方法

- [ ] `index.vue` 已更新
  - 路徑: `/cheng-ui/src/views/monitor/job/index.vue`
  - 已整合任務類型選擇器

#### 2. 編譯檢查

```bash
cd cheng-ui
npm run build:prod
```

- [ ] 編譯成功，無錯誤
- [ ] 打包檔案正常產生

#### 3. 開發環境測試

```bash
cd cheng-ui
npm run dev
```

- [ ] 專案啟動成功
- [ ] 無 console 錯誤

---

## 🧪 功能測試

### 測試 1: 基本顯示

1. 進入頁面
   - [ ] 進入 `/monitor/job` 頁面
   - [ ] 點選「新增」按鈕
   - [ ] 對話框正常彈出

2. 檢查介面元素
   - [ ] 可看到「設定方式」選項（範本 / 手動）
   - [ ] 預設為「範本模式」
   - [ ] 顯示「任務類型」下拉選單

### 測試 2: 範本模式

1. 選擇任務類型
   - [ ] 下拉選單可正常展開
   - [ ] 顯示「爬蟲任務」分類
   - [ ] 顯示 5 種爬蟲任務選項

2. 選擇「執行爬蟲（無參數）」
   - [ ] 系統顯示成功訊息
   - [ ] 自動填入 Bean 名稱: `crawlerTask`
   - [ ] 自動填入方法名稱: `run`
   - [ ] 顯示「任務參數」區域
   - [ ] 顯示「爬蟲類型」參數欄位
   - [ ] 參數預設值為 `CA102`
   - [ ] 顯示必填標記 ⚠️
   - [ ] Cron 表達式自動填入 `0 0 1 * * ?`
   - [ ] 顯示 Cron 建議值提示
   - [ ] 顯示「使用建議值」按鈕
   - [ ] 呼叫目標顯示: `crawlerTask.run('CA102')`

3. 測試參數修改
   - [ ] 修改爬蟲類型為 `CA103`
   - [ ] 呼叫目標即時更新為 `crawlerTask.run('CA103')`

4. 測試 Cron 建議
   - [ ] 點選「使用建議值」按鈕
   - [ ] Cron 表達式正確填入
   - [ ] 顯示成功訊息

### 測試 3: 手動模式

1. 切換模式
   - [ ] 點選「手動輸入」
   - [ ] 任務類型選單隱藏
   - [ ] 參數表單隱藏
   - [ ] 顯示傳統的「呼叫方法」輸入框

2. 手動輸入
   - [ ] 可正常輸入呼叫目標
   - [ ] 可正常輸入 Cron 表達式

3. 切換回範本模式
   - [ ] 點選「從範本選擇」
   - [ ] 介面正常切換
   - [ ] 之前的資料已清空

### 測試 4: 提交功能

1. 範本模式提交
   - [ ] 填寫任務名稱: `測試爬蟲任務`
   - [ ] 選擇任務類型: `執行爬蟲（無參數）`
   - [ ] 爬蟲類型: `CA102`
   - [ ] 點選「確定」
   - [ ] 顯示成功訊息
   - [ ] 任務列表中出現新任務
   - [ ] 呼叫目標為: `crawlerTask.run('CA102')`

2. 必填參數驗證
   - [ ] 清空爬蟲類型參數
   - [ ] 點選「確定」
   - [ ] 顯示錯誤訊息：「爬蟲類型 為必填參數」

### 測試 5: 其他任務類型

測試「執行爬蟲（帶模式）」：
- [ ] 選擇該任務類型
- [ ] 顯示 2 個參數：爬蟲類型、執行模式
- [ ] 參數預設值正確
- [ ] Cron 為 `0 0 9,12,15 * * ?`
- [ ] 呼叫目標正確產生

測試「執行爬蟲（完整參數）」：
- [ ] 選擇該任務類型
- [ ] 顯示 4 個參數
- [ ] INTEGER 類型顯示為數字輸入框
- [ ] 選填參數無必填標記
- [ ] 呼叫目標正確產生

### 測試 6: 任務執行

1. 建立任務後
   - [ ] 任務狀態為「暫停」
   - [ ] 可切換為「正常」
   - [ ] 可點選「執行」立即執行

2. 執行測試
   - [ ] 點選「執行」
   - [ ] 顯示確認對話框
   - [ ] 確認後顯示執行成功
   - [ ] 可在日誌中查看執行記錄

---

## 🔍 瀏覽器相容性測試

測試以下瀏覽器：

- [ ] Chrome (最新版)
- [ ] Firefox (最新版)
- [ ] Safari (最新版)
- [ ] Edge (最新版)

確認功能：
- [ ] 下拉選單正常顯示
- [ ] 參數表單正常顯示
- [ ] 動態更新正常運作
- [ ] 無 console 錯誤

---

## 📱 響應式測試

測試不同螢幕尺寸：

- [ ] 桌面 (1920x1080)
- [ ] 筆電 (1366x768)
- [ ] 平板 (768x1024)

確認：
- [ ] 對話框大小適中
- [ ] 表單排版正常
- [ ] 按鈕可正常點選

---

## 🚨 錯誤處理測試

### 1. 網路錯誤

- [ ] 停止後端服務
- [ ] 嘗試載入任務類型
- [ ] 顯示錯誤訊息
- [ ] 不影響頁面其他功能

### 2. 資料錯誤

- [ ] 修改 API 回傳格式（模擬錯誤）
- [ ] 前端正確捕捉錯誤
- [ ] 顯示友善的錯誤訊息

### 3. 使用者輸入錯誤

- [ ] 輸入無效的爬蟲類型
- [ ] 輸入無效的 Cron 表達式
- [ ] 系統正確驗證並提示

---

## 📊 效能測試

### 1. 載入速度

- [ ] 頁面載入時間 < 2 秒
- [ ] API 回應時間 < 500ms
- [ ] 下拉選單展開流暢

### 2. 記憶體測試

- [ ] 開啟/關閉對話框 10 次
- [ ] 記憶體無明顯增長
- [ ] 無記憶體洩漏

---

## 🔐 安全性檢查

- [ ] API 需要登入才能存取
- [ ] 角色權限正確控制
- [ ] 輸入參數有適當驗證
- [ ] 無 XSS 風險
- [ ] 無 SQL 注入風險

---

## 📝 文件檢查

確認以下文件已建立：

- [ ] `QUARTZ_JOB_TYPE_ENUM.md` - Enum 設計文件
- [ ] `QUARTZ_FRONTEND_INTEGRATION.md` - 前端整合指南
- [ ] `QUARTZ_QUICK_START.md` - 快速使用指南
- [ ] `QUARTZ_TEMPLATE_FEATURE.md` - 功能總結
- [ ] `DEPLOYMENT_CHECKLIST_QUARTZ.md` - 本檢查清單

---

## ✅ 部署步驟

### 1. 備份

```bash
# 備份資料庫
mysqldump -u root -p cheng_db > backup_$(date +%Y%m%d).sql

# 備份現有程式碼（如有）
tar -czf backup_code_$(date +%Y%m%d).tar.gz /path/to/project
```

- [ ] 資料庫已備份
- [ ] 程式碼已備份

### 2. 後端部署

```bash
# 編譯
cd cheng-quartz
mvn clean package -DskipTests

# 停止服務
systemctl stop cheng-backend

# 更新 JAR 檔
cp target/cheng-quartz.jar /opt/cheng/

# 啟動服務
systemctl start cheng-backend

# 檢查日誌
tail -f /var/log/cheng/application.log
```

- [ ] 編譯成功
- [ ] 服務正常啟動
- [ ] 無錯誤日誌

### 3. 前端部署

```bash
# 編譯
cd cheng-ui
npm run build:prod

# 停止服務
systemctl stop nginx

# 更新檔案
rm -rf /var/www/cheng-ui/*
cp -r dist/* /var/www/cheng-ui/

# 啟動服務
systemctl start nginx
```

- [ ] 編譯成功
- [ ] 檔案正常更新
- [ ] Nginx 正常啟動

### 4. 驗證

- [ ] 訪問前端頁面正常
- [ ] API 可正常呼叫
- [ ] 功能測試通過

---

## 🎉 部署完成

### 上線通知

通知相關人員：
- [ ] 系統管理員
- [ ] 測試人員
- [ ] 使用者

### 監控

部署後持續監控：
- [ ] 應用程式日誌
- [ ] 錯誤日誌
- [ ] 效能指標
- [ ] 使用者回饋

---

## 📞 問題處理

如遇到問題：

1. 檢查日誌
2. 查看瀏覽器 console
3. 檢查網路請求
4. 回滾到備份版本（如需要）

**緊急聯絡**: [您的聯絡方式]

---

**檢查日期**: _________________  
**檢查人員**: _________________  
**部署日期**: _________________  
**部署人員**: _________________
