# LINE 使用者管理系統開發文件

## 📋 功能概述

完整的 LINE 使用者管理系統，包含使用者資料管理、綁定、統計、匯入匯出等功能。

## 🎯 核心功能

### 1. 統計卡片
- **總使用者數** - 系統中所有 LINE 使用者
- **關注中** - 目前關注官方帳號的使用者
- **已綁定** - 已綁定系統帳號的使用者
- **已封鎖** - 已封鎖官方帳號的使用者
- **本週新增** - 最近 7 天新增的使用者

### 2. 查詢與篩選
- **LINE 使用者 ID** - 支援模糊搜尋
- **顯示名稱** - 支援模糊搜尋
- **綁定狀態** - 已綁定/未綁定
- **關注狀態** - 關注中/已取消/已封鎖
- **加入時間** - 日期範圍篩選

### 3. 資料表格
- **頭像** - 顯示 LINE 使用者頭像
- **顯示名稱** - LINE 暱稱
- **LINE ID** - 使用者 ID（支援複製）
- **關注狀態** - 標籤顯示
- **綁定狀態** - 標籤顯示
- **互動統計** - 發送/接收訊息數
- **最後互動** - 最後一次互動時間

### 4. 操作功能

#### 綁定系統使用者
- 選擇系統使用者進行綁定
- 支援搜尋系統使用者
- 顯示系統使用者的暱稱和帳號

#### 解除綁定
- 一鍵解除綁定
- 需要確認操作

#### 同步資料
- 從 LINE API 同步最新使用者資料
- 更新顯示名稱、頭像、狀態訊息

#### 查看詳情
- 抽屜式側邊欄顯示完整資訊
- **基本資料** - 頭像、名稱、ID、狀態訊息、語言
- **關注歷程** - 初次關注、最近關注、取消關注、封鎖時間
- **綁定歷程** - 初次綁定、最近綁定、解除綁定時間
- **互動統計** - 發送/接收訊息數、最後互動時間
- **備註** - 使用者備註資訊

### 5. 匯入功能

#### 支援格式
- **Excel** (.xlsx, .xls) - 第一列為標題，第一欄為 LINE User ID
- **CSV** (.csv) - 第一列為標題，第一欄為 LINE User ID
- **TXT** (.txt) - 每行一個 LINE User ID

#### 匯入流程
1. 選擇 LINE 頻道
2. 上傳檔案
3. 系統讀取檔案中的 LINE User ID
4. 呼叫 LINE API 取得使用者資料
5. 批次新增或更新到資料庫
6. 顯示匯入結果（成功/失敗統計）

#### 錯誤處理
- 成功的記錄會保留
- 失敗的記錄會記錄錯誤原因
- 顯示詳細的失敗資訊（行號、User ID、錯誤原因）

### 6. 匯出功能
- 匯出為 Excel 格式
- 包含所有欄位資料
- 支援查詢條件匯出

### 7. 批次操作
- 批次刪除使用者
- 支援多選

## 🗄️ 資料庫設計

### 資料庫遷移
**遷移檔案**: `/cheng-admin/src/main/resources/db/migration/V15__add_line_conversation_log_table.sql`

⚠️ **重要**：本專案使用 **Flyway** 進行資料庫版本管理，請勿手動執行 SQL 檔案。啟動應用程式時會自動執行遷移。

### sys_line_user (LINE 使用者表)
已存在的表（由 V10 建立），包含以下欄位：
- `id` - 主鍵ID
- `line_user_id` - LINE 使用者 ID（唯一）
- `line_display_name` - LINE 顯示名稱
- `line_picture_url` - LINE 頭像 URL
- `line_status_message` - LINE 狀態訊息
- `line_language` - LINE 語言設定
- `sys_user_id` - 系統使用者 ID
- `bind_status` - 綁定狀態
- `follow_status` - 關注狀態
- `first_follow_time` - 初次關注時間
- `latest_follow_time` - 最近關注時間
- `unfollow_time` - 取消關注時間
- `block_time` - 封鎖時間
- `first_bind_time` - 初次綁定時間
- `latest_bind_time` - 最近綁定時間
- `unbind_time` - 解除綁定時間
- `bind_count` - 綁定次數
- `total_messages_sent` - 累計發送訊息數
- `total_messages_received` - 累計接收訊息數
- `last_interaction_time` - 最後互動時間
- `create_time` - 建立時間
- `update_time` - 更新時間
- `remark` - 備註

### sys_line_conversation_log (LINE 對話記錄表)
由 **V15 遷移**建立，用於記錄使用者互動對話：
- `id` - 主鍵ID
- `line_user_id` - LINE 使用者 ID
- `direction` - 方向（SENT/RECEIVED）
- `message_content` - 訊息內容
- `message_format` - 訊息格式
- `message_id` - LINE 訊息 ID
- `config_id` - 頻道設定ID
- `message_time` - 訊息時間
- `is_success` - 是否成功
- `error_message` - 錯誤訊息
- `create_time` - 建立時間
- `remark` - 備註

## 🔧 技術實作

### 後端 (Spring Boot + MyBatis)

#### 新增的類別
1. **ConversationDirection.java** - 對話方向枚舉
2. **LineConversationLog.java** - 對話記錄實體
3. **LineConversationLogMapper.java** - 對話記錄 Mapper
4. **LineConversationLogMapper.xml** - 對話記錄 MyBatis 對映檔案
5. **ILineConversationLogService.java** - 對話記錄服務介面
6. **LineConversationLogServiceImpl.java** - 對話記錄服務實作
7. **LineUserStatsDTO.java** - 使用者統計 DTO
8. **LineUserImportResultDTO.java** - 匯入結果 DTO

#### 更新的類別
1. **LineUserMapper.java** - 新增統計查詢方法
2. **LineUserMapper.xml** - 新增統計 SQL、更新查詢支援模糊搜尋
3. **ILineUserService.java** - 新增統計和匯入方法
4. **LineUserServiceImpl.java** - 實作統計和匯入功能
5. **LineUserController.java** - 新增統計、匯入、下載範本端點
6. **DateUtils.java** (cheng-common) - 新增 `getBeforeDaysDate(int days)` 方法，用於取得指定天數之前的日期字串

#### 核心功能實作

##### 統計功能
```java
// 取得使用者統計資料
@GetMapping("/stats")
public AjaxResult getStats() {
    return success(lineUserService.getUserStats());
}
```

##### 匯入功能
```java
// 支援 Excel、CSV、TXT 格式
@PostMapping("/import")
public AjaxResult importUsers(@RequestParam("file") MultipartFile file,
                               @RequestParam("configId") Integer configId)
```

處理流程：
1. 讀取檔案內容（根據副檔名選擇解析方式）
2. 取得 LINE User ID 列表
3. 去除空白和重複項
4. 逐個呼叫 LINE API 取得使用者資料
5. 新增或更新到資料庫
6. 記錄成功/失敗結果

##### 對話記錄清理功能
```java
// 刪除指定天數之前的對話記錄
public int deleteLineConversationLogBeforeDays(int days) {
    String beforeDate = DateUtils.getBeforeDaysDate(days);
    return lineConversationLogMapper.deleteLineConversationLogBeforeDate(beforeDate);
}
```

功能說明：
- 自動清理過期的對話記錄
- 可配合定時任務使用，定期清理資料
- 減少資料庫儲存空間，提升查詢效能
- 建議透過 Quartz 定時任務排程執行（例如：每月清理 90 天前的記錄）

### 前端 (Vue.js + Element UI)

#### 檔案結構
```
cheng-ui/src/views/line/user/
├── index.vue                    # 主頁面
└── components/
    ├── StatsCard.vue            # 統計卡片組件
    ├── UserDetail.vue           # 使用者詳情抽屜
    ├── BindDialog.vue           # 綁定對話框
    └── ImportDialog.vue         # 匯入對話框
```

#### 組件說明

##### StatsCard.vue
- 顯示 5 個統計卡片
- 使用漸層色彩設計
- 支援 hover 動畫效果
- RWD 響應式設計

##### UserDetail.vue
- 使用 Drawer 抽屜組件
- 分區顯示：基本資料、關注歷程、綁定歷程、互動統計
- Timeline 時間軸顯示歷程
- 卡片式統計資訊展示

##### BindDialog.vue
- 選擇系統使用者
- 支援搜尋和篩選
- 顯示使用者暱稱和帳號

##### ImportDialog.vue
- 拖曳上傳檔案
- 選擇 LINE 頻道
- 顯示匯入說明
- 上傳進度提示

## 📝 API 端點

### 查詢
- `GET /line/user/list` - 查詢使用者列表（分頁）
- `GET /line/user/{id}` - 查詢使用者詳情
- `GET /line/user/following` - 查詢關注中的使用者
- `GET /line/user/stats` - 取得統計資料

### 操作
- `PUT /line/user/bind/{lineUserId}/{sysUserId}` - 綁定系統使用者
- `PUT /line/user/unbind/{lineUserId}` - 解除綁定
- `PUT /line/user/updateProfile/{lineUserId}` - 同步使用者資料
- `DELETE /line/user/{ids}` - 刪除使用者

### 匯入匯出
- `POST /line/user/import` - 匯入使用者
- `POST /line/user/importTemplate` - 下載匯入範本
- `POST /line/user/export` - 匯出使用者

## 🚀 部署步驟

### 1. 資料庫初始化
```bash
# 執行 SQL 腳本
mysql -u root -p < docs/LINE/LINE_MESSAGE_LOG_TABLE.sql
```

### 2. 後端部署
```bash
# 已整合到現有系統，無需額外部署
```

### 3. 前端部署
```bash
cd cheng-ui
npm run build:prod
```

### 4. 設定權限
- 為角色分配 `LINE 使用者` 選單權限
- 分配對應的按鈕權限（查詢、綁定、編輯、刪除、匯出、匯入）

## 📊 使用說明

### 1. 查看統計資訊
- 進入頁面自動載入統計卡片
- 點擊卡片可查看詳細資料

### 2. 查詢使用者
- 使用搜尋條件篩選
- 支援多條件組合查詢
- 點擊「搜尋」執行查詢

### 3. 綁定使用者
- 點擊「更多」→「綁定」
- 選擇系統使用者
- 確認綁定

### 4. 查看詳情
- 點擊「詳情」按鈕
- 側邊欄顯示完整資訊
- 可查看歷程和統計

### 5. 匯入使用者
- 點擊「匯入」按鈕
- 選擇 LINE 頻道
- 上傳檔案（Excel/CSV/TXT）
- 等待匯入完成
- 查看匯入結果

### 6. 匯出資料
- 設定查詢條件（可選）
- 點擊「匯出」按鈕
- 下載 Excel 檔案

## ⚠️ 注意事項

### 匯入功能
1. 必須先選擇 LINE 頻道
2. 檔案格式必須正確
3. LINE User ID 必須有效
4. 需要 LINE API 存取權限
5. 大量匯入時請耐心等待

### 權限管理
- 綁定功能需要 `line:user:bind` 權限
- 匯入功能需要 `line:user:import` 權限
- 匯出功能需要 `line:user:export` 權限

### 效能考量
- 匯入大量使用者時會逐個呼叫 LINE API
- 建議分批匯入，避免 API 限流
- 統計資料即時計算，資料量大時可考慮快取

## 🔍 故障排除

### 匯入失敗
- 檢查 LINE 頻道設定是否正確
- 確認 LINE API Token 是否有效
- 檢查 LINE User ID 格式是否正確
- 查看錯誤訊息詳情

### 綁定失敗
- 確認系統使用者是否存在
- 檢查是否已綁定其他帳號
- 查看後端日誌

### 統計資料不正確
- 確認資料庫資料是否完整
- 檢查統計 SQL 是否正確執行
- 重新整理頁面

## 📌 後續擴充建議

1. **批次綁定** - 支援多個 LINE 使用者同時綁定
2. **標籤管理** - 為使用者新增標籤分類
3. **訊息推播** - 從使用者列表直接推播訊息
4. **匯出範本** - 提供標準的匯入範本下載
5. **資料視覺化** - 新增圖表顯示統計資料
6. **對話記錄** - 顯示最近的對話內容
7. **自動同步** - 定期自動同步使用者資料

## 📚 相關文件
- [LINE Messaging API 文件](https://developers.line.biz/en/docs/messaging-api/)
- [Element UI 文件](https://element.eleme.io/)
- [Vue.js 文件](https://vuejs.org/)

---

**開發完成日期：** 2025-11-02  
**開發者：** Cascade AI Assistant
