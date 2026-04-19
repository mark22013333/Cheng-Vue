# Windsurf Rules 填寫範本

請將以下內容複製到 `.windsurf/rules/cool-apps.md` 檔案中。

---

## 填寫說明

### 1. Description 欄位（250 字元限制）
```
CoolApps 專案開發規範：強制使用 Enum、效能優化、前後端資料格式轉換、異常處理等核心規範
```

### 2. Glob Pattern 欄位（250 字元限制）
```
**/*.{java,vue,js,ts,xml,yml,yaml,md}
```

### 3. Content 欄位（12000 字元限制）

將以下內容複製到 Content 欄位：

---

# CoolApps 專案開發規範

> **完整文件**: `/docs/Development/` 目錄包含 13 份詳細規範

---

## ⭐ 核心規範（必須遵守）

### 1. 強制使用 Enum（最重要！）

❌ **禁止使用魔法數字或字串**:
```java
if (status.equals("0")) { ... }  // 不要這樣寫
if (type == 1) { ... }
```

✅ **必須使用 Enum**:
```java
if (status == Status.NORMAL) { ... }  // 清晰明確
if (channelType == ChannelType.MAIN) { ... }
```

**Enum 標準範本**:
```java
public enum Status {
    NORMAL("0", "正常"),
    DISABLE("1", "停用");

    private final String code;
    private final String description;

    Status(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static Status getByCode(String code) {
        return Arrays.stream(values())
            .filter(e -> e.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }
}
```

---

### 2. 前端狀態參數處理（極其重要！）

#### 後端返回 → 前端顯示
```javascript
// 處理多種可能的格式
let statusValue = '1'
if (response.data.status) {
  const status = response.data.status
  if (typeof status === 'object' && status.code !== undefined) {
    statusValue = String(status.code)  // 枚舉物件
  } else if (typeof status === 'number') {
    statusValue = String(status)  // 數字
  } else if (typeof status === 'string') {
    statusValue = status === 'ENABLE' ? '1' : '0'  // 字串
  }
}
this.form.status = statusValue
```

#### 前端提交 → 後端接收
```javascript
const submitData = {
  ...this.form,
  statusCode: parseInt(this.form.status),  // 字串 → 數字
  isDefaultCode: this.form.isDefault ? 1 : 0,  // 布林 → 數字
  status: undefined,  // 移除避免衝突
  isDefault: undefined
}
```

#### Element UI 元件規範
- `el-radio-group`: label 必須是字串 `label="1"`
- `el-switch`: 綁定布林值 `:active-value="true"`
- `el-select`: value 使用 Enum code `value="MAIN"`

---

### 3. 效能優化（支援 500 人線上、100 人並發）

#### 必須使用分頁
```java
@GetMapping("/list")
public TableDataInfo list(SysUser user) {
    startPage();  // 必須！
    List<SysUser> list = userService.selectUserList(user);
    return getDataTable(list);
}
```

#### 快取熱點資料
```java
// 查詢前先查快取
String cacheKey = "config:" + configKey;
String value = redisCache.getCacheObject(cacheKey);
if (StringUtils.isNotEmpty(value)) {
    return value;
}
// 查詢資料庫並寫入快取
value = mapper.selectByKey(configKey);
redisCache.setCacheObject(cacheKey, value, 30, TimeUnit.MINUTES);
```

#### 防止重複提交
```java
@PostMapping
@RepeatSubmit  // 5 秒內防止重複提交
public AjaxResult add(@RequestBody SysUser user) {
    return toAjax(userService.insertUser(user));
}
```

---

### 4. 異常處理規範

✅ **統一使用 ServiceException**:
```java
// 明確的異常訊息
throw new ServiceException(
    String.format("分類「%s」正被 %d 個物品使用，無法刪除", 
        categoryName, itemCount)
);
```

❌ **不要使用模糊訊息**:
```java
throw new ServiceException("刪除失敗");  // 太模糊
```

---

### 5. 事務管理規範

✅ **多表操作必須加事務**:
```java
@Transactional(rollbackFor = Exception.class)
public int borrowItem(InvBorrow borrow) {
    borrowMapper.insert(borrow);
    stockMapper.decreaseQuantity(borrow.getItemId(), borrow.getQuantity());
    stockRecordMapper.insert(buildRecord(borrow));
    return 1;
}
```

⚠️ **注意事項**:
- @Transactional 只對 public 方法有效
- 同類別內部呼叫事務不生效（需注入自己）
- 捕獲異常後要重新拋出或手動回滾

---

### 6. 資料驗證規範

#### Controller 層
```java
@PostMapping
public AjaxResult add(@Valid @RequestBody SysUser user) {
    return toAjax(userService.insertUser(user));
}
```

#### Service 層
```java
// 檢查唯一性
if (userMapper.selectByUserName(user.getUserName()) != null) {
    throw new ServiceException("用戶名稱已存在");
}

// 檢查關聯資料
int itemCount = itemMapper.countByCategoryId(categoryId);
if (itemCount > 0) {
    throw new ServiceException(
        String.format("分類正被 %d 個物品使用，無法刪除", itemCount)
    );
}
```

---

### 7. API 設計規範

#### RESTful 規範
- GET: 查詢 `/system/user/list`
- POST: 新增 `/system/user`
- PUT: 修改 `/system/user`
- DELETE: 刪除 `/system/user/{ids}`

#### 統一回應格式
```java
@GetMapping("/{userId}")
public AjaxResult getInfo(@PathVariable Long userId) {
    SysUser user = userService.selectUserById(userId);
    return AjaxResult.success(user);
}
```

#### 時間格式
```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
private Date createTime;
```

---

## ⚠️ 常見陷阱

### 1. MyBatis 動態 SQL
```xml
<!-- ❌ 錯誤 -->
<if test="status == '0'">  <!-- 物件比較 -->

<!-- ✅ 正確 -->
<if test="status != null and '0'.equals(status)">
```

### 2. Long 比較
```java
// ❌ 錯誤
if (userId1 == userId2) { ... }

// ✅ 正確
if (Objects.equals(userId1, userId2)) { ... }
```

### 3. 執行緒安全
```java
// ❌ 危險：Spring Bean 成員變數
@Service
public class UserService {
    private SysUser currentUser;  // 多執行緒會互相覆蓋
}

// ✅ 正確：使用區域變數
public void processUser(Long userId) {
    SysUser currentUser = userMapper.selectById(userId);
}
```

### 4. List 判空
```java
// ❌ 錯誤
if (list != null) {
    list.get(0);  // 可能 IndexOutOfBoundsException
}

// ✅ 正確
if (StringUtils.isNotEmpty(list)) {
    list.get(0);
}
```

---

## 🔐 環境配置

### Jasypt 加密
啟動時必須加上解密密碼：
```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 環境切換
- **Local**: `spring.profiles.active=local`
  - LOG: `/Users/cheng/cool-logs`
  - 上傳: `/Users/cheng/uploadPath`

- **PROD**: `spring.profiles.active=prod`
  - LOG: `/opt/cool-apps/logs`
  - 上傳: `/opt/cool-apps/uploadFile`

---

## ✅ 開發前檢查清單

### 必須檢查（最重要）
- [ ] ⭐ 是否使用 Enum 替代魔法數字？
- [ ] ⭐ 前端狀態參數是否正確轉換？
- [ ] ⭐ 所有列表查詢都使用分頁？
- [ ] ⭐ 異常訊息是否清晰明確？
- [ ] ⭐ 多表操作是否加上事務？
- [ ] ⭐ 敏感資訊是否加密？

### 效能檢查
- [ ] 熱點資料是否有快取？
- [ ] 是否避免 N+1 查詢？
- [ ] 是否有適當的資料庫索引？
- [ ] 慢查詢數是否為 0（< 1 秒）？

### 安全檢查
- [ ] 是否使用參數化查詢（防 SQL 注入）？
- [ ] 密碼是否使用 BCrypt 加密？
- [ ] 日誌是否記錄敏感資訊？

### 前端檢查
- [ ] el-radio-group 的 label 是否為字串？
- [ ] 後端返回的枚舉物件是否提取 code？
- [ ] 提交時是否使用 xxxCode 欄位名？
- [ ] 是否移除原始欄位避免衝突？

---

## 📖 完整文件位置

詳細規範請查閱：
- `/docs/Development/RULE_00_INDEX.md` - 索引
- `/docs/Development/RULE_02_ENUM_STANDARDS.md` - Enum 規範 ⭐
- `/docs/Development/RULE_13_FRONTEND_STANDARDS.md` - 前端規範 ⭐
- `/docs/Development/RULE_04_PERFORMANCE.md` - 效能優化
- `/docs/Development/RULE_11_CODE_REVIEW.md` - 檢查清單

---

**記住**: 這些規範基於實際開發經驗總結，務必遵守以確保程式碼品質！
