# API 設計規範

> **核心原則**: RESTful 風格、統一回應格式、清晰的 API 路徑

---

## 統一回應格式

### AjaxResult 標準格式

```java
public class AjaxResult {
    private int code;      // 狀態碼：200=成功, 500=失敗
    private String msg;    // 訊息
    private Object data;   // 資料
}
```

**JSON 格式**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "userName": "admin"
  }
}
```

### Controller 使用範例

```java
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
    
    @Autowired
    private ISysUserService userService;
    
    /**
     * 查詢單個資料
     */
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        return AjaxResult.success(user);
    }
    
    /**
     * 查詢列表（分頁）
     */
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }
    
    /**
     * 新增
     */
    @PostMapping
    public AjaxResult add(@Valid @RequestBody SysUser user) {
        return toAjax(userService.insertUser(user));
    }
    
    /**
     * 修改
     */
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody SysUser user) {
        return toAjax(userService.updateUser(user));
    }
    
    /**
     * 刪除
     */
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByIds(userIds));
    }
}
```

### 分頁回應格式

```java
public class TableDataInfo {
    private int code;      // 200
    private String msg;    // "查詢成功"
    private long total;    // 總筆數
    private List<?> rows;  // 資料列表
}
```

**JSON 格式**:
```json
{
  "code": 200,
  "msg": "查詢成功",
  "total": 100,
  "rows": [
    {
      "userId": 1,
      "userName": "admin"
    },
    {
      "userId": 2,
      "userName": "test"
    }
  ]
}
```

---

## RESTful 規範

### HTTP Method 使用

| Method | 用途 | 範例 | 說明 |
|--------|------|------|------|
| GET | 查詢 | `GET /system/user/1` | 查詢單個資料 |
| GET | 列表 | `GET /system/user/list` | 查詢列表 |
| POST | 新增 | `POST /system/user` | 新增資料 |
| PUT | 修改 | `PUT /system/user` | 修改資料 |
| DELETE | 刪除 | `DELETE /system/user/1` | 刪除資料 |

### URL 設計規範

✅ **正確範例**:
```java
GET    /system/user/list        // 查詢用戶列表
GET    /system/user/{userId}    // 查詢單個用戶
POST   /system/user             // 新增用戶
PUT    /system/user             // 修改用戶
DELETE /system/user/{userIds}   // 刪除用戶（支援批次）

// 巢狀資源
GET    /system/dept/{deptId}/users     // 查詢部門下的用戶
POST   /system/user/{userId}/roles     // 為用戶分配角色

// 特殊操作
PUT    /system/user/{userId}/status    // 修改用戶狀態
POST   /system/user/resetPwd           // 重設密碼
POST   /system/user/import             // 匯入用戶
```

❌ **錯誤範例**:
```java
// 不要使用動詞
GET /system/getUserList              // ❌
GET /system/user/list                // ✅

// 不要使用複數（專案統一使用單數）
GET /system/users/list               // ❌
GET /system/user/list                // ✅

// 不要混用駝峰和底線
GET /system/user-list                // ❌
GET /system/user/list                // ✅
```

---

## 分頁參數規範

### 統一使用 pageNum 和 pageSize

```java
@GetMapping("/list")
public TableDataInfo list(
    @RequestParam(defaultValue = "1") Integer pageNum,      // 頁碼（從 1 開始）
    @RequestParam(defaultValue = "10") Integer pageSize,    // 每頁數量
    SysUser user                                             // 查詢條件
) {
    startPage();  // PageHelper 自動從參數取得 pageNum 和 pageSize
    List<SysUser> list = userService.selectUserList(user);
    return getDataTable(list);
}
```

### 前端請求範例

```javascript
// GET 請求
axios.get('/system/user/list', {
  params: {
    pageNum: 1,
    pageSize: 10,
    userName: 'admin',
    status: '0'
  }
})

// 回應
{
  "code": 200,
  "msg": "查詢成功",
  "total": 100,
  "rows": [...]
}
```

---

## 時間格式統一

### 統一使用 yyyy-MM-dd HH:mm:ss (台灣時區)

```java
@Data
public class BaseEntity {
    
    /** 建立時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date createTime;
    
    /** 更新時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date updateTime;
}
```

### 前端傳遞時間

```javascript
// 前端傳遞
{
  "startTime": "2025-01-01 00:00:00",
  "endTime": "2025-01-31 23:59:59"
}

// 後端自動轉換為 Date
@Data
public class QueryParams {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
```

---

## Enum 序列化

### 後端傳前端：使用 code

```java
@JsonValue  // JSON 序列化時使用 code
public String getCode() {
    return code;
}

// 回應
{
  "status": "0",           // code，不是 "NORMAL"
  "channelType": "MAIN"    // code
}
```

### 前端傳後端：自動轉換

```javascript
// 前端傳遞
{
  "status": "0",
  "channelType": "MAIN"
}

// 後端自動轉換（需要 Converter）
@Data
public class SysUser {
    private Status status;           // "0" -> Status.NORMAL
    private ChannelType channelType; // "MAIN" -> ChannelType.MAIN
}
```

---

## 參數驗證

### Controller 層驗證

```java
@PostMapping
public AjaxResult add(@Valid @RequestBody SysUser user) {
    // @Valid 觸發參數驗證
    return toAjax(userService.insertUser(user));
}
```

### Entity 驗證註解

```java
@Data
public class SysUser {
    
    @NotBlank(message = "用戶名稱不能為空")
    @Size(min = 2, max = 30, message = "用戶名稱長度必須介於 2 到 30 之間")
    private String userName;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 5, max = 20, message = "密碼長度必須介於 5 到 20 之間")
    private String password;
    
    @Email(message = "信箱格式不正確")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手機號碼格式不正確")
    private String phonenumber;
}
```

### 驗證失敗回應

```json
{
  "code": 500,
  "msg": "用戶名稱不能為空"
}
```

---

## 錯誤碼設計

### HttpStatus 常數

```java
public class HttpStatus {
    public static final int SUCCESS = 200;           // 成功
    public static final int CREATED = 201;           // 已建立
    public static final int ACCEPTED = 202;          // 已接受
    public static final int NO_CONTENT = 204;        // 無內容
    public static final int BAD_REQUEST = 400;       // 錯誤請求
    public static final int UNAUTHORIZED = 401;      // 未授權
    public static final int FORBIDDEN = 403;         // 禁止訪問
    public static final int NOT_FOUND = 404;         // 未找到
    public static final int ERROR = 500;             // 伺服器錯誤
}
```

### 使用範例

```java
@GetMapping("/{userId}")
public AjaxResult getInfo(@PathVariable Long userId) {
    SysUser user = userService.selectUserById(userId);
    if (user == null) {
        return AjaxResult.error(HttpStatus.NOT_FOUND, "用戶不存在");
    }
    return AjaxResult.success(user);
}
```

---

## API 文件 (Swagger)

### 註解使用

```java
@Api(tags = "用戶管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    
    @ApiOperation("查詢用戶列表")
    @GetMapping("/list")
    public TableDataInfo list(
        @ApiParam("用戶名稱") @RequestParam(required = false) String userName,
        @ApiParam("用戶狀態") @RequestParam(required = false) String status
    ) {
        // ...
    }
    
    @ApiOperation("查詢用戶詳細")
    @GetMapping("/{userId}")
    public AjaxResult getInfo(
        @ApiParam(value = "用戶ID", required = true) @PathVariable Long userId
    ) {
        // ...
    }
}
```

### 訪問 Swagger UI

- **本地環境**: `http://localhost:8080/swagger-ui/index.html`
- **正式環境**: `https://cool-apps.zeabur.app/swagger-ui/index.html`

---

## CORS 處理

### 已配置的 CORS

專案已在 Nginx 配置 CORS：

```nginx
location /prod-api/ {
    # CORS 處理
    add_header Access-Control-Allow-Origin $http_origin always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type, Accept" always;
    add_header Access-Control-Allow-Credentials true always;
    
    # 處理 OPTIONS 預檢請求
    if ($request_method = 'OPTIONS') {
        return 204;
    }
}
```

---

## 檢查清單

開發 API 時必須檢查：

- [ ] 是否遵循 RESTful 規範？
- [ ] 是否使用統一的回應格式？
- [ ] 分頁參數是否使用 pageNum/pageSize？
- [ ] 時間格式是否統一？
- [ ] Enum 是否使用 code 序列化？
- [ ] 是否有參數驗證？
- [ ] 錯誤訊息是否清晰明確？
- [ ] 是否有 API 文件註解？

---

**下一步**: 閱讀 [RULE_09_DATA_VALIDATION.md](./RULE_09_DATA_VALIDATION.md) 學習資料驗證規範
