# 📘 書籍資料髒資料修復指南

## 🐛 問題描述

### 錯誤訊息
```
Duplicate entry 'xxxxxxxxxxxxx' for key 'uk_isbn'
```

### 根本原因
資料庫中存在**書籍資訊**但對應的**物品已被刪除**的情況，導致：
1. 掃描 ISBN 時，檢查發現書籍資訊存在
2. 但物品記錄不存在（已被刪除）
3. 嘗試重新建立時，物品建立成功
4. 但新增書籍資訊時違反 ISBN 唯一性約束

### 資料不一致情況
```
inv_book_info 表：
- ISBN: 9789863877363
- item_id: 10001 （此物品已被刪除）

inv_item 表：
- item_id: 10001 的記錄不存在或 del_flag = '2'
```

---

## ✅ 解決方案

### 方案 A：自動修復（推薦）⭐

#### 1. 執行清理腳本
```bash
mysql -u root -p your_database < sql/fix_dirty_book_data.sql
```

**此腳本會：**
- 識別所有有問題的書籍資訊
- 將這些書籍資訊的 `item_id` 設為 NULL
- 下次掃描時會自動重新建立物品並關聯

#### 2. 重新編譯並部署程式碼
```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn clean package -DskipTests
# 重啟服務
```

**程式碼修改包含：**
- 檢測到書籍資訊存在但物品不存在時
- 自動建立新物品
- 更新書籍資訊的 `item_id` 關聯
- 避免重複新增導致唯一性約束錯誤

#### 3. 測試驗證
掃描之前出錯的 ISBN，確認能正常處理。

---

### 方案 B：手動清理

#### 1. 查詢有問題的資料
```sql
SELECT 
    b.book_info_id,
    b.isbn,
    b.title,
    b.item_id,
    i.item_id AS actual_item_id,
    CASE 
        WHEN i.item_id IS NULL THEN '物品不存在'
        WHEN i.del_flag = '2' THEN '物品已刪除'
        ELSE '正常'
    END AS status
FROM inv_book_info b
LEFT JOIN inv_item i ON b.item_id = i.item_id AND i.del_flag = '0'
WHERE i.item_id IS NULL OR i.del_flag = '2';
```

#### 2. 清空有問題書籍的 item_id
```sql
UPDATE inv_book_info
SET item_id = NULL,
    update_time = NOW(),
    update_by = 'admin'
WHERE book_info_id IN (1234, 5678, ...);  -- 替換為實際的 ID
```

#### 3. 重新掃描 ISBN
在前端掃描功能中重新掃描這些 ISBN。

---

## 🔍 診斷查詢

### 查詢所有書籍資料狀態
```sql
SELECT 
    COUNT(*) AS total_books,
    SUM(CASE WHEN item_id IS NULL THEN 1 ELSE 0 END) AS no_item_link,
    SUM(CASE WHEN item_id IS NOT NULL THEN 1 ELSE 0 END) AS has_item_link
FROM inv_book_info;
```

### 查詢有問題的書籍
```sql
SELECT 
    b.isbn,
    b.title,
    b.item_id,
    i.item_name,
    i.del_flag
FROM inv_book_info b
LEFT JOIN inv_item i ON b.item_id = i.item_id
WHERE b.item_id IS NOT NULL 
  AND (i.item_id IS NULL OR i.del_flag != '0');
```

### 查詢孤立的物品（有物品但沒有書籍資訊）
```sql
SELECT 
    i.item_id,
    i.item_code,
    i.item_name,
    i.barcode
FROM inv_item i
LEFT JOIN inv_book_info b ON i.item_id = b.item_id
WHERE i.barcode LIKE '978%'  -- ISBN 開頭
  AND b.book_info_id IS NULL
  AND i.del_flag = '0';
```

---

## 🚫 預防措施

### 1. 避免直接刪除物品
如果要刪除書籍物品，應該：
1. 先檢查是否有關聯的書籍資訊
2. 同時刪除或解除關聯
3. 建議使用邏輯刪除而非物理刪除

### 2. 新增級聯刪除約束（可選）
```sql
-- 修改外鍵約束，新增級聯更新
ALTER TABLE inv_book_info 
DROP FOREIGN KEY fk_book_item;

ALTER TABLE inv_book_info 
ADD CONSTRAINT fk_book_item 
FOREIGN KEY (item_id) 
REFERENCES inv_item(item_id) 
ON DELETE SET NULL 
ON UPDATE CASCADE;
```

### 3. 定期資料檢查
建議設定定期任務（每週或每月）執行診斷查詢，及早發現資料不一致問題。

---

## 📊 修復後驗證

### 1. 確認沒有髒資料
```sql
-- 應該返回 0 筆記錄
SELECT COUNT(*) AS dirty_data_count
FROM inv_book_info b
LEFT JOIN inv_item i ON b.item_id = i.item_id AND i.del_flag = '0'
WHERE b.item_id IS NOT NULL 
  AND (i.item_id IS NULL OR i.del_flag = '2');
```

### 2. 測試掃描功能
- 掃描一個新的 ISBN（未在系統中）
- 掃描一個已存在的 ISBN
- 掃描之前出錯的 ISBN

### 3. 檢查日誌
```bash
tail -f /path/to/logs/error.log | grep "Duplicate entry"
```
應該不再出現 "Duplicate entry" 錯誤。

---

## 🎯 技術細節

### 修改的檔案
1. **BookItemServiceImpl.java**
   - 新增 `recreateItemForExistingBook()` 方法
   - 處理書籍資訊存在但物品不存在的情況
   - 更新而非重複新增

### 流程圖
```
掃描 ISBN
    ↓
檢查書籍資訊是否存在
    ↓
存在 → 檢查物品是否存在
    ↓
物品存在 → 直接返回 ✅
    ↓
物品不存在 → 建立新物品 + 更新書籍資訊關聯 ✅
    ↓
書籍資訊不存在 → 爬取 + 建立物品 + 建立書籍資訊 ✅
```

---

## ⚠️ 注意事項

1. **備份資料庫**：執行任何清理操作前請先備份
   ```bash
   mysqldump -u root -p your_database > backup_$(date +%Y%m%d).sql
   ```

2. **測試環境先測試**：建議在測試環境驗證後再部署到正式環境

3. **記錄清理結果**：保存清理前後的資料統計

4. **通知使用者**：如果有大量髒資料需要清理，可能需要通知使用者重新掃描某些書籍

---

## 📞 問題排查

### Q: 清理後掃描還是報錯？
A: 
1. 確認程式碼已重新編譯並部署
2. 確認清理腳本已執行成功
3. 檢查是否有其他資料完整性問題

### Q: 清理會影響現有資料嗎？
A: 
- 方案 A（設 item_id 為 NULL）：不會刪除任何資料，只是解除關聯
- 下次掃描會重新建立物品並關聯
- 不影響正常的書籍資料

### Q: 如何防止未來再次出現？
A:
1. 使用邏輯刪除而非物理刪除
2. 新增資料完整性檢查
3. 定期執行診斷腳本
4. 考慮新增資料庫級別的約束

---

**修復完成後，系統將能夠自動處理髒資料，避免 ISBN 重複錯誤！** 🎉
