# LINE 使用者管理系統部署與測試檢查清單

## ✅ 部署前檢查

### 資料庫準備
- [ ] 確認 `sys_line_user` 表已存在（由 V10 建立）
- [ ] 確認 Flyway 已啟用（檢查 `application.yml`）
- [ ] 備份正式資料庫（如果是正式環境）
  ```bash
  mysqldump -u root -p cool_apps > backup_$(date +%Y%m%d_%H%M%S).sql
  ```
- [ ] V15 遷移會自動建立 `sys_line_conversation_log` 表和選單權限

### 後端驗證
- [ ] 檢查所有新建立的 Java 類別是否編譯成功
  - `ConversationDirection.java`
  - `LineConversationLog.java`
  - `LineConversationLogMapper.java`
  - `LineConversationLogServiceImpl.java`
  - `LineUserStatsDTO.java`
  - `LineUserImportResultDTO.java`
- [ ] 檢查更新的類別是否無編譯錯誤
  - `LineUserMapper.java`
  - `LineUserMapper.xml`
  - `LineUserServiceImpl.java`
  - `LineUserController.java`
- [ ] 確認 MyBatis Mapper XML 檔案語法正確
- [ ] 檢查所有 import 語句是否完整

### 前端驗證
- [ ] 檢查前端檔案是否建立完成
  - `/cheng-ui/src/views/line/user/index.vue`
  - `/cheng-ui/src/views/line/user/components/StatsCard.vue`
  - `/cheng-ui/src/views/line/user/components/UserDetail.vue`
  - `/cheng-ui/src/views/line/user/components/BindDialog.vue`
  - `/cheng-ui/src/views/line/user/components/ImportDialog.vue`
- [ ] 確認 API 檔案已更新
  - `/cheng-ui/src/api/line/user.js`
- [ ] 檢查前端程式碼語法是否正確
- [ ] 確認所有 import 路徑正確

## 🚀 啟動步驟

### 1. 啟動後端
```bash
cd cheng-admin
mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 2. 啟動前端
```bash
cd cheng-ui
npm run dev
```

### 3. 登入系統
- 訪問：http://localhost:80
- 使用管理員帳號登入

## 🧪 功能測試清單

### 基本功能測試

#### 1. 頁面訪問
- [ ] 能否正常訪問 LINE 使用者管理頁面
- [ ] 選單是否正確顯示
- [ ] 頁面載入是否無錯誤

#### 2. 統計卡片
- [ ] 統計卡片是否正確顯示
- [ ] 統計數字是否準確
- [ ] 卡片 hover 動畫是否正常
- [ ] RWD 響應式是否正常

#### 3. 查詢功能
- [ ] LINE 使用者 ID 模糊搜尋是否正常
- [ ] 顯示名稱模糊搜尋是否正常
- [ ] 綁定狀態篩選是否正常
- [ ] 關注狀態篩選是否正常
- [ ] 日期範圍篩選是否正常
- [ ] 重置按鈕是否正常

#### 4. 資料表格
- [ ] 表格資料是否正確載入
- [ ] 分頁功能是否正常
- [ ] 頭像是否正確顯示
- [ ] 狀態標籤顏色是否正確
- [ ] 互動統計 Tooltip 是否正常
- [ ] 複製 LINE ID 功能是否正常

#### 5. 使用者詳情
- [ ] 點擊詳情按鈕是否開啟抽屜
- [ ] 使用者資料是否完整顯示
- [ ] 關注歷程時間軸是否正確
- [ ] 綁定歷程時間軸是否正確
- [ ] 互動統計是否正確
- [ ] 關閉抽屜是否正常

#### 6. 綁定功能
- [ ] 綁定對話框是否正常開啟
- [ ] 系統使用者列表是否正確載入
- [ ] 搜尋系統使用者是否正常
- [ ] 綁定操作是否成功
- [ ] 綁定後狀態是否更新
- [ ] 錯誤處理是否正確

#### 7. 解綁功能
- [ ] 解綁確認對話框是否顯示
- [ ] 解綁操作是否成功
- [ ] 解綁後狀態是否更新

#### 8. 同步資料
- [ ] 同步確認對話框是否顯示
- [ ] 同步操作是否成功
- [ ] 同步後資料是否更新

#### 9. 刪除功能
- [ ] 單筆刪除是否正常
- [ ] 批次刪除是否正常
- [ ] 刪除確認對話框是否顯示
- [ ] 刪除後資料是否更新
- [ ] 統計數字是否更新

### 匯入功能測試

#### Excel 匯入
- [ ] 選擇 LINE 頻道下拉是否正常
- [ ] 上傳 .xlsx 檔案是否正常
- [ ] 上傳 .xls 檔案是否正常
- [ ] 匯入進度提示是否顯示
- [ ] 匯入成功訊息是否正確
- [ ] 匯入結果統計是否準確
- [ ] 失敗詳情是否顯示
- [ ] 匯入後列表是否更新
- [ ] 匯入後統計是否更新

#### CSV 匯入
- [ ] 上傳 .csv 檔案是否正常
- [ ] CSV 解析是否正確
- [ ] 匯入結果是否正確

#### TXT 匯入
- [ ] 上傳 .txt 檔案是否正常
- [ ] TXT 解析是否正確（換行分隔）
- [ ] 空白和重複項是否正確處理
- [ ] 匯入結果是否正確

#### 錯誤處理
- [ ] 未選擇頻道時是否提示錯誤
- [ ] 未選擇檔案時是否提示錯誤
- [ ] 不支援的檔案格式是否提示錯誤
- [ ] LINE API 錯誤是否正確記錄
- [ ] 失敗的 User ID 是否顯示詳細原因

### 匯出功能測試
- [ ] 匯出按鈕是否正常
- [ ] 匯出的 Excel 檔案是否正確
- [ ] 匯出資料是否完整
- [ ] 查詢條件匯出是否正常

### 權限測試
- [ ] 無權限使用者是否無法訪問頁面
- [ ] 無權限使用者是否無法看到操作按鈕
- [ ] line:user:list 權限是否正常
- [ ] line:user:query 權限是否正常
- [ ] line:user:bind 權限是否正常
- [ ] line:user:edit 權限是否正常
- [ ] line:user:remove 權限是否正常
- [ ] line:user:export 權限是否正常
- [ ] line:user:import 權限是否正常

### 效能測試
- [ ] 大量資料（>1000筆）載入速度
- [ ] 匯入大量使用者（>100筆）是否正常
- [ ] 統計查詢效能是否可接受
- [ ] 頁面操作是否流暢

### 相容性測試
- [ ] Chrome 瀏覽器是否正常
- [ ] Firefox 瀏覽器是否正常
- [ ] Safari 瀏覽器是否正常
- [ ] 行動裝置瀏覽是否正常
- [ ] 不同螢幕尺寸是否正常顯示

## 🐛 已知問題與注意事項

### 需要注意的地方
1. **LINE API 限流**
   - 匯入大量使用者時，LINE API 可能會限流
   - 建議分批匯入，每次不超過 100 筆

2. **檔案格式**
   - Excel 第一列必須是標題
   - CSV 必須使用 UTF-8 編碼
   - TXT 檔案每行一個 User ID

3. **頻道設定**
   - 匯入前必須確保選擇的頻道狀態為「啟用」
   - 頻道的 Access Token 必須有效

4. **系統使用者**
   - 綁定時只顯示狀態為「正常」的系統使用者
   - 綁定前會檢查是否已綁定其他帳號

5. **統計資料**
   - 統計資料為即時計算
   - 資料量大時可能略慢

## 📋 上線檢查

### 正式環境部署前
- [ ] 所有測試項目均已通過
- [ ] 資料庫備份已完成
- [ ] 設定檔已更新為正式環境設定
- [ ] 權限配置已正確設定
- [ ] 日誌等級已調整
- [ ] 效能測試已通過
- [ ] 安全性檢查已完成

### 上線後驗證
- [ ] 選單是否正確顯示
- [ ] 基本功能是否正常
- [ ] 統計資料是否正確
- [ ] 匯入功能是否正常
- [ ] 錯誤日誌是否正常記錄

## 📞 問題回報

如發現任何問題，請記錄以下資訊：
1. 問題描述
2. 重現步驟
3. 錯誤訊息
4. 瀏覽器和版本
5. 後端日誌（如有）

## 📚 參考文件
- [LINE_USER_MANAGEMENT.md](./LINE_USER_MANAGEMENT.md) - 完整功能文件
- [LINE_MESSAGE_LOG_TABLE.sql](./LINE_MESSAGE_LOG_TABLE.sql) - 資料庫初始化腳本

---

**檢查清單版本：** 1.0  
**最後更新：** 2025-11-02
