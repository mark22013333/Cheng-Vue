# 分類管理功能部署指南

## 📋 功能概述

本次更新為庫存管理系統新增了完整的分類管理功能，包含：

### 核心功能
- ✅ 分類的新增、修改、刪除、查詢
- ✅ 預設分類設定（自動確保唯一性）
- ✅ 刪除前物品關聯檢查
- ✅ 邏輯刪除（非物理刪除）
- ✅ Excel 匯出功能
- ✅ 權限控制
- ✅ 自動記錄建立者/時間

### 移除功能
- ❌ 父子分類功能（簡化為扁平結構）

---

## 🗄️ 資料庫部署

### 步驟 1：執行資料庫修正腳本

```bash
# 連接到 MySQL
mysql -u root -p your_database

# 執行修正腳本
source /Users/cheng/IdeaProjects/R/Cheng-Vue/sql/fix_category_default.sql
```

**此腳本將：**
- 設定「書籍」(ID: 1000) 為預設分類
- 清除其他分類的預設標記
- 清理空白備註

### 步驟 2：配置權限（可選）

```bash
# 執行權限配置腳本
source /Users/cheng/IdeaProjects/R/Cheng-Vue/sql/category_permissions.sql
```

**此腳本將：**
- 建立分類管理選單
- 新增所有相關權限
- 為管理員角色授權

---

## 🔧 後端部署

### 步驟 1：確認修改的檔案

**Mapper XML：**
- `/cheng-system/src/main/resources/mapper/system/InvCategoryMapper.xml`
  - 新增 `sort_order` 欄位
  - 改為邏輯刪除
  - 查詢只顯示未刪除的資料

**Service：**
- `/cheng-system/src/main/java/com/cheng/system/service/impl/InvCategoryServiceImpl.java`
  - 自動設定建立者/時間
  - 預設分類唯一性控制
  - 刪除前物品關聯檢查

**Controller：**
- `/cheng-admin/src/main/java/com/cheng/web/controller/inventory/InvCategoryController.java`
  - 新增匯出功能

**Domain：**
- `/cheng-system/src/main/java/com/cheng/system/domain/InvCategory.java`
  - 調整 Excel 註解

### 步驟 2：重新編譯

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn clean package -DskipTests
```

### 步驟 3：重啟服務

```bash
# 停止舊服務
pkill -f cheng-admin

# 啟動新服務
cd cheng-admin/target
java -jar cheng-admin.jar &
```

---

## 🎨 前端部署

### 步驟 1：確認修改的檔案

**新增組件：**
- `/cheng-ui/src/views/inventory/management/components/CategoryManagement.vue`

**修改組件：**
- `/cheng-ui/src/views/inventory/management/index.vue`
  - 新增頁籤結構
  - 引入 CategoryManagement 組件

### 步驟 2：安裝依賴（如需要）

```bash
cd cheng-ui
npm install
```

### 步驟 3：重新編譯

```bash
# 開發模式
npm run dev

# 或正式模式
npm run build:prod
```

---

## 🧪 測試清單

### 1. 基本功能測試

#### ✅ 新增分類
1. 打開「庫存管理」→「分類管理」頁籤
2. 點擊「新增分類」
3. 填寫資料：
   - 分類名稱：測試分類
   - 分類編碼：TEST_CAT
   - 排序：10
4. 提交
5. **驗證：**
   - 建立者欄位顯示當前使用者
   - 建立時間顯示當前時間
   - 分類出現在列表中

#### ✅ 修改分類
1. 點擊任一分類的「修改」按鈕
2. 修改分類名稱
3. 提交
4. **驗證：**
   - 更新時間已更新
   - 修改成功提示

#### ✅ 預設分類切換
1. 點擊「修改」書籍分類（當前預設）
2. 關閉「設為預設分類」開關
3. **驗證：** 可以正常切換

4. 點擊「修改」其他分類
5. 開啟「設為預設分類」開關
6. **驗證：**
   - 彈出確認對話框
   - 顯示目前預設分類資訊
   - 提示切換後原預設會被取消

7. 確認切換
8. **驗證：**
   - 新分類顯示「預設」標籤
   - 原預設分類的「預設」標籤消失

#### ✅ 刪除檢查
1. 建立一個測試分類
2. 在「物品管理」中新增一個物品，使用該分類
3. 返回「分類管理」，嘗試刪除該分類
4. **驗證：**
   - 顯示錯誤提示
   - 提示「分類正被 X 個物品使用，無法刪除」
   - 分類未被刪除

5. 刪除該物品
6. 再次嘗試刪除分類
7. **驗證：**
   - 刪除成功
   - 分類從列表消失

#### ✅ 邏輯刪除驗證
1. 刪除一個分類
2. 在資料庫中查詢：
   ```sql
   SELECT * FROM inv_category WHERE del_flag = '2';
   ```
3. **驗證：** 分類記錄仍存在，`del_flag = '2'`

#### ✅ Excel 匯出
1. 點擊「匯出Excel」按鈕
2. **驗證：**
   - 成功下載檔案
   - 檔案名稱格式：`分類資料_時間戳.xlsx`
   - Excel 內容包含所有分類資料
   - 欄位包括：分類名稱、分類編碼、排序、狀態、備註

### 2. 權限測試

1. 使用非管理員帳號登入
2. **驗證：** 
   - 有權限的使用者可以看到「分類管理」頁籤
   - 無權限的使用者看不到「分類管理」頁籤
3. 測試各個操作按鈕的權限控制

### 3. 邊界測試

#### 測試長文字
- 分類名稱：輸入 50 個字元
- 分類編碼：輸入 30 個字元
- 備註：輸入長文字

#### 測試特殊字元
- 分類編碼：只接受大寫字母和底線
- 輸入小寫字母或特殊符號應該顯示錯誤

#### 測試排序
- 新增多個分類，設定不同排序值
- **驗證：** 列表按照排序值排列

---

## 🔍 故障排查

### 問題 1：建立者和時間為空
**原因：** 後端服務未重啟  
**解決：** 重新編譯並重啟後端服務

### 問題 2：刪除是物理刪除
**原因：** Mapper XML 未更新  
**解決：** 確認 `InvCategoryMapper.xml` 中的 delete 語句已改為 update

### 問題 3：預設分類有多個
**原因：** Service 邏輯未生效  
**解決：** 
1. 執行 `fix_category_default.sql` 清理資料
2. 重啟服務

### 問題 4：匯出功能報錯
**原因：** 缺少匯出權限  
**解決：** 執行 `category_permissions.sql` 新增權限

### 問題 5：無法刪除分類，但沒有物品使用
**原因：** 可能查詢包含了已刪除的物品  
**解決：** 確認 `InvItemMapper` 的查詢有過濾 `del_flag = '0'`

---

## 📊 資料庫結構說明

```sql
CREATE TABLE inv_category (
    category_id   BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '分類ID',
    parent_id     BIGINT(20)   DEFAULT 0 COMMENT '父分類ID（預設0，不使用）',
    category_name VARCHAR(50)  NOT NULL COMMENT '分類名稱',
    category_code VARCHAR(30)  DEFAULT '' COMMENT '分類編碼',
    sort_order    INT(4)       DEFAULT 0 COMMENT '排序',
    status        CHAR(1)      DEFAULT '0' COMMENT '狀態（0正常 1停用）',
    del_flag      CHAR(1)      DEFAULT '0' COMMENT '刪除標誌（0存在 2刪除）',
    create_by     VARCHAR(64)  DEFAULT '' COMMENT '建立者',
    create_time   DATETIME     COMMENT '建立時間',
    update_by     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time   DATETIME     COMMENT '更新時間',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '備註（預設分類會包含「預設分類」標記）',
    PRIMARY KEY (category_id)
) ENGINE=INNODB COMMENT = '物品分類表';
```

### 重要欄位說明

- **parent_id**: 保留欄位但不使用，所有分類的 parent_id 都是 0
- **del_flag**: 
  - '0' = 正常
  - '2' = 已刪除
- **remark**: 
  - 包含「預設分類」文字的為預設分類
  - 系統確保只有一個預設分類

---

## 🎯 後續優化建議

1. **批量操作**：支援批量修改狀態、批量刪除
2. **匯入功能**：支援 Excel 匯入分類資料
3. **分類圖示**：為每個分類新增圖示設定
4. **使用統計**：顯示每個分類下的物品數量
5. **排序拖拽**：支援拖拽調整分類排序

---

## ✅ 部署檢查清單

- [ ] 執行資料庫修正腳本 (`fix_category_default.sql`)
- [ ] 執行權限配置腳本 (`category_permissions.sql`)
- [ ] 後端程式碼已更新並重新編譯
- [ ] 後端服務已重啟
- [ ] 前端程式碼已更新並重新編譯
- [ ] 前端服務已重啟/部署
- [ ] 管理員可以正常存取分類管理頁面
- [ ] 新增分類功能正常
- [ ] 修改分類功能正常
- [ ] 刪除檢查功能正常
- [ ] 預設分類切換功能正常
- [ ] Excel 匯出功能正常
- [ ] 所有測試案例通過

---

## 📞 支援

如遇到問題，請檢查：
1. 瀏覽器控制台錯誤訊息
2. 後端日誌檔案
3. 資料庫連接狀態
4. 權限配置是否正確

---

**部署完成！** 🎉
