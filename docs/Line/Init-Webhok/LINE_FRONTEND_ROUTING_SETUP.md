# LINE 模組前端路由配置說明

> **文件版本：** 1.0  
> **更新時間：** 2025-10-28  
> **狀態：** 已完成

---

## 📋 配置內容

### 1. 路由配置

已在 `/cheng-ui/src/router/index.js` 中新增 LINE 模組路由：

```javascript
{
  path: '/line/config',
  component: Layout,
  hidden: false,
  permissions: ['line:config:list'],
  children: [
    {
      path: '',
      component: () => import('@/views/line/config/index'),
      name: 'LineConfig',
      meta: {
        title: 'LINE 設定',
        icon: 'setting'
      }
    }
  ]
}
```

### 2. 權限控制

**權限代碼：** `line:config:list`

此權限已在資料庫遷移腳本 `V10__init_line_module.sql` 中配置，對應：
- **選單項目：** LINE 設定
- **父選單：** LINE 管理 > 行銷管理
- **權限按鈕：** 
  - `line:config:list` - 查看列表
  - `line:config:query` - 查看詳情
  - `line:config:add` - 新增頻道
  - `line:config:edit` - 編輯頻道
  - `line:config:remove` - 刪除頻道
  - `line:config:test` - 測試連線

---

## 📁 頁面檔案結構

```
cheng-ui/src/views/line/
└── config/
    ├── index.vue                    # 主頁面（卡片式列表）
    └── components/
        ├── ConfigForm.vue           # 新增/編輯表單
        └── ConnectionTest.vue       # 連線測試對話框
```

---

## 🔧 配置步驟

### Step 1: 執行資料庫遷移

確保已執行資料庫遷移腳本：

```bash
# 資料庫遷移會自動執行
# 或手動執行 V10__init_line_module.sql 和 V11__add_bot_basic_id.sql
```

### Step 2: 重新啟動後端服務

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-admin
mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### Step 3: 啟動前端開發伺服器

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui
npm run dev
```

### Step 4: 授予權限

1. 使用管理員帳號登入系統
2. 進入「系統管理」→「角色管理」
3. 編輯需要授權的角色
4. 勾選「行銷管理」→「LINE 管理」→「LINE 設定」
5. 儲存

---

## 🎯 訪問路徑

配置完成後，可透過以下方式訪問：

### 選單訪問

```
系統選單
└── 行銷管理
    └── LINE 管理
        └── LINE 設定
```

### 直接訪問 URL

```
http://localhost:80/line/config
```

---

## ✅ 測試檢查清單

### 權限測試

- [ ] 管理員可以看到「行銷管理」選單
- [ ] 管理員可以看到「LINE 管理」子選單
- [ ] 管理員可以看到「LINE 設定」頁面
- [ ] 沒有權限的使用者看不到選單項目
- [ ] 直接訪問 URL 時，沒有權限會被拒絕（跳轉到 401 頁面）

### 頁面功能測試

- [ ] 頁面正常載入，無錯誤
- [ ] 卡片列表正常顯示
- [ ] 搜尋功能正常
- [ ] 新增頻道對話框可以開啟
- [ ] 表單驗證正常
- [ ] 一鍵複製功能正常
- [ ] 測試連線功能正常
- [ ] 編輯功能正常
- [ ] 刪除功能正常（需確認）
- [ ] 設為主頻道功能正常

### API 測試

- [ ] GET `/line/config/list` - 取得頻道列表
- [ ] GET `/line/config/{id}` - 取得頻道詳情
- [ ] POST `/line/config` - 新增頻道
- [ ] PUT `/line/config` - 更新頻道
- [ ] DELETE `/line/config/{id}` - 刪除頻道
- [ ] POST `/line/config/testConnection/{id}` - 測試連線
- [ ] PUT `/line/config/setDefault/{id}` - 設為預設頻道

---

## 🚨 常見問題

### Q1: 選單沒有顯示「行銷管理」？

**A:** 檢查以下項目：
1. 資料庫遷移是否執行成功
2. 角色是否已授權 `line:config:list` 權限
3. 使用者是否重新登入（權限需要重新載入）
4. 清除瀏覽器快取後重試

### Q2: 頁面顯示 404 Not Found？

**A:** 可能原因：
1. 路由配置未生效，需要重新啟動前端服務
2. 組件檔案路徑錯誤，檢查 `/cheng-ui/src/views/line/config/index.vue` 是否存在
3. 前端編譯錯誤，查看控制台是否有錯誤訊息

### Q3: 頁面顯示 401 Unauthorized？

**A:** 權限不足，需要：
1. 確認當前使用者是否有 `line:config:list` 權限
2. 重新登入後再試
3. 聯絡管理員授予權限

### Q4: API 請求失敗？

**A:** 檢查：
1. 後端服務是否正常執行
2. API 路徑是否正確（`/line/config/...`）
3. 查看後端日誌是否有錯誤
4. 檢查網路連線

---

## 📊 選單層次結構

```
sys_menu 表結構：
┌────────────────────────────────────────────────────┐
│ menu_id  │ menu_name    │ parent_id │ menu_type │
├──────────┼──────────────┼───────────┼───────────┤
│ 2001     │ 行銷管理     │ 0         │ M (目錄)  │
│ 2002     │ LINE 管理    │ 2001      │ M (目錄)  │
│ 2003     │ LINE 設定    │ 2002      │ C (頁面)  │
│ 2011     │ LINE設定查詢 │ 2003      │ F (按鈕)  │
│ 2012     │ LINE設定新增 │ 2003      │ F (按鈕)  │
│ 2013     │ LINE設定修改 │ 2003      │ F (按鈕)  │
│ 2014     │ LINE設定刪除 │ 2003      │ F (按鈕)  │
│ 2015     │ LINE連線測試 │ 2003      │ F (按鈕)  │
└────────────────────────────────────────────────────┘
```

---

## 🔄 後續計畫

### 待新增的路由

1. **推播訊息管理**
   - 路徑：`/line/message`
   - 權限：`line:message:list`
   - 狀態：⏸️ 未實作

2. **LINE 使用者管理**
   - 路徑：`/line/user`
   - 權限：`line:user:list`
   - 狀態：⏸️ 未實作

---

## 📝 變更記錄

| 版本 | 日期 | 說明 |
|-----|------|------|
| 1.0 | 2025-10-28 | 初版，完成 LINE 設定頁面路由配置 |

---

## 相關文件

- [LINE 模組概述](./LINE_MODULE_OVERVIEW.md)
- [LINE 模組開發進度](./LINE_MODULE_PROGRESS.md)
- [LINE 權限設計](./LINE_PERMISSIONS_DESIGN.md)
- [LINE SDK 升級指南](./LINE_SDK_9_UPGRADE_GUIDE.md)
