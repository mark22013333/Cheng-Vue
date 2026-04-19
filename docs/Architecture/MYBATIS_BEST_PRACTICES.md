# MyBatis 最佳實踐與常見陷阱

## 🔴 關鍵教訓：數值比較不可使用字串

### 問題發現
**日期**：2025-10-05  
**發現者**：User  
**嚴重程度**：🔴 嚴重（導致核心查詢功能完全失效）

### 問題描述
在 MyBatis Mapper XML 的 `<when test="...">` 條件判斷中，使用字串比較數值型態會導致條件判斷失效。

### 錯誤範例 ❌
```xml
<!-- 錯誤：使用字串比較數字 -->
<when test="stockStatus == '2'">
    and (s.available_qty is null or s.available_qty &lt;= 0)
</when>
<when test="stockStatus == '1'">
    and s.available_qty > 0
</when>
<when test="stockStatus == '0'">
    and s.available_qty > 0
</when>
```

**問題**：
- 當 `stockStatus` 是 Integer 型態時，`stockStatus == '2'` 會進行類型轉換
- 轉換可能失敗，導致條件永遠為 `false`
- 查詢結果會包含不應該出現的資料

### 正確範例 ✅
```xml
<!-- 正確：直接使用數字比較 -->
<when test="stockStatus == 2">
    and (s.available_qty is null or s.available_qty &lt;= 0)
</when>
<when test="stockStatus == 1">
    and s.available_qty > 0
</when>
<when test="stockStatus == 0">
    and s.available_qty > 0
</when>
```

## MyBatis Test 條件判斷規則

### 1. 數值型態比較

| Java 型態 | XML 條件寫法 | 說明 |
|----------|-------------|------|
| Integer | `test="value == 1"` | ✅ 正確 |
| Integer | `test="value == '1'"` | ❌ 錯誤（類型不匹配） |
| Long | `test="id == 100"` | ✅ 正確 |
| Long | `test="id == '100'"` | ❌ 錯誤 |

### 2. 字串型態比較

| Java 型態 | XML 條件寫法 | 說明 |
|----------|-------------|------|
| String | `test="status == 'active'"` | ✅ 正確 |
| String | `test="status == active"` | ❌ 錯誤（會當成變數） |
| String | `test='status == "active"'` | ✅ 正確（外單內雙） |

### 3. 布林型態比較

| Java 型態 | XML 條件寫法 | 說明 |
|----------|-------------|------|
| Boolean | `test="isActive == true"` | ✅ 正確 |
| Boolean | `test="isActive"` | ✅ 正確（簡寫） |
| Boolean | `test="isActive == 'true'"` | ❌ 錯誤 |

### 4. NULL 檢查

```xml
<!-- 檢查是否為 null -->
<if test="value != null">...</if>

<!-- 檢查字串不為空 -->
<if test="value != null and value != ''">...</if>

<!-- 檢查數字不為 null 且大於 0 -->
<if test="value != null and value > 0">...</if>
```

## 常見陷阱與解決方案

### 陷阱 1：SQL 中的字串比較 vs MyBatis Test 條件

```xml
<!-- MyBatis Test 條件：比較 Java 物件屬性 -->
<if test="status == 1">  <!-- ✅ 數字型態用數字 -->
    <!-- SQL WHERE 子句：比較資料庫欄位 -->
    and db_status = '1'   <!-- ✅ 資料庫欄位可能是字串型態 -->
</if>
```

**重點**：
- `test="..."` 中的比較是 **Java 物件屬性**的比較
- SQL WHERE 中的比較是 **資料庫欄位**的比較
- 兩者型態可能不同，需分別處理

### 陷阱 2：動態 SQL 中的 CASE 語句

```xml
<!-- SQL 中的 CASE：使用資料庫欄位型態 -->
<select id="selectStats">
    select 
        sum(case when status = '1' then 1 else 0 end) as borrowed,  <!-- ✅ -->
        sum(case when status = '2' then 1 else 0 end) as returned   <!-- ✅ -->
    from inv_borrow
    <where>
        <!-- MyBatis Test：使用 Java 型態 -->
        <if test="statusFilter == 1">  <!-- ✅ -->
            and status = '1'            <!-- ✅ -->
        </if>
    </where>
</select>
```

### 陷阱 3：參數綁定與條件判斷混淆

```xml
<!-- 錯誤：混淆參數綁定和條件判斷 -->
<if test="status == #{status}">  <!-- ❌ 錯誤！ -->

<!-- 正確：分開處理 -->
<if test="status != null">       <!-- ✅ 條件判斷 -->
    and db_status = #{status}    <!-- ✅ 參數綁定 -->
</if>
```

## 檢查清單

在撰寫或修改 MyBatis Mapper XML 時，請檢查：

- [ ] `<when test="...">` 中比較數值時，是否移除了單引號？
- [ ] `<if test="...">` 中的型態是否與 Java DTO 屬性型態一致？
- [ ] 是否區分了「MyBatis 條件判斷」和「SQL WHERE 比較」？
- [ ] NULL 檢查是否正確（`!= null`）？
- [ ] 字串比較是否正確使用了引號（`== 'value'`）？

## 偵錯技巧

### 1. 啟用 MyBatis SQL 日誌

在 `application.yml` 中設定：
```yaml
logging:
  level:
    com.cheng.system.mapper: debug  # 顯示實際執行的 SQL
```

### 2. 在 Controller 中加入參數日誌

```java
@GetMapping("/list")
public TableDataInfo list(InvItemWithStockDTO dto) {
    log.info("查詢參數: {}", JSON.toJSONString(dto));  // 記錄收到的參數
    startPage();
    List<InvItemWithStockDTO> list = invItemMapper.selectItemWithStockList(dto);
    log.info("查詢結果數量: {}", list.size());  // 記錄結果數量
    return getDataTable(list);
}
```

### 3. 直接測試 SQL

從日誌複製實際執行的 SQL，在資料庫工具中執行，確認結果是否符合預期。

## 相關修復案例

### 案例：庫存狀態查詢失效

**檔案**：`InvItemMapper.xml`  
**問題**：選擇「無庫存」時出現「正常」狀態的資料  
**原因**：`<when test="stockStatus == '2'">` 使用字串比較，導致條件失效  
**解決**：改為 `<when test="stockStatus == 2">`  
**日期**：2025-10-05

詳細說明請參考：
- `docs/STOCK_STATUS_FIX.md`
- `docs/CRITICAL_FIX_SUMMARY.md`

## 團隊規範

### Code Review 檢查點

在審查 MyBatis Mapper XML 時，特別注意：

1. **數值比較檢查**
   - 搜尋：`test=".*== '[0-9]'"`
   - 應改為：直接使用數字（無引號）

2. **類型一致性**
   - 檢查 DTO 屬性型態與 XML 條件是否匹配
   - Integer/Long 用數字，String 用引號

3. **NULL 處理**
   - 所有可能為 null 的屬性都應先檢查 `!= null`
   - 字串應同時檢查 `!= null and != ''`

### IDE 設定建議

在 IntelliJ IDEA 中，可以設定自定義檢查規則：
1. Settings → Editor → Inspections → XML
2. 新增自定義規則，檢測 `test=".*== '[0-9]'"` 模式
3. 設為 Warning 或 Error 等級

## 參考資源

### MyBatis 官方文件
- [Dynamic SQL](https://mybatis.org/mybatis-3/dynamic-sql.html)
- [OGNL Expression](https://commons.apache.org/proper/commons-ognl/language-guide.html)

### 相關議題
- MyBatis OGNL 表達式會進行自動類型轉換，但不保證成功
- 字串與數字比較時，優先嘗試將字串轉為數字
- 轉換失敗時，條件結果為 `false`（而非拋出異常）

## 修訂歷史

| 日期 | 版本 | 修訂內容 | 作者 |
|------|------|---------|------|
| 2025-10-05 | 1.0 | 初版建立，記錄數值比較陷阱 | Cascade AI |

---

**重要提醒**：此文件記錄了一個容易忽略但影響重大的問題。請所有團隊成員熟讀，並在撰寫 MyBatis XML 時格外注意！
