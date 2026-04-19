# 資料驗證規範

> **核心原則**: Controller 層參數驗證 + Service 層業務驗證

---

## Controller 層參數驗證

### 使用 @Valid 觸發驗證

```java
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    
    @PostMapping
    public AjaxResult add(@Valid @RequestBody SysUser user) {
        // @Valid 會自動驗證 user 的欄位
        return toAjax(userService.insertUser(user));
    }
    
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody SysUser user) {
        return toAjax(userService.updateUser(user));
    }
}
```

### Entity 欄位驗證註解

```java
@Data
public class SysUser {
    
    /** 用戶名稱 */
    @NotBlank(message = "用戶名稱不能為空")
    @Size(min = 2, max = 30, message = "用戶名稱長度必須介於 2 到 30 之間")
    private String userName;
    
    /** 暱稱 */
    @NotBlank(message = "暱稱不能為空")
    @Size(min = 2, max = 30, message = "暱稱長度必須介於 2 到 30 之間")
    private String nickName;
    
    /** 密碼 */
    @NotBlank(message = "密碼不能為空", groups = {Add.class})  // 只在新增時驗證
    @Size(min = 5, max = 20, message = "密碼長度必須介於 5 到 20 之間")
    private String password;
    
    /** 信箱 */
    @Email(message = "信箱格式不正確")
    @Size(max = 50, message = "信箱長度不能超過 50 個字元")
    private String email;
    
    /** 手機號碼 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手機號碼格式不正確")
    private String phonenumber;
    
    /** 性別 */
    @NotNull(message = "性別不能為空")
    private String sex;
    
    /** 狀態 */
    @NotNull(message = "狀態不能為空")
    private Status status;
    
    /** 部門 ID */
    @NotNull(message = "部門不能為空")
    private Long deptId;
    
    /** 角色 IDs */
    @NotEmpty(message = "至少要分配一個角色")
    private Long[] roleIds;
}
```

### 常用驗證註解

| 註解 | 說明 | 範例 |
|------|------|------|
| `@NotNull` | 不能為 null | `@NotNull(message = "ID 不能為空")` |
| `@NotBlank` | 字串不能為空（去空白後） | `@NotBlank(message = "名稱不能為空")` |
| `@NotEmpty` | 集合/陣列不能為空 | `@NotEmpty(message = "至少選擇一項")` |
| `@Size` | 長度限制 | `@Size(min = 2, max = 30)` |
| `@Min` | 最小值 | `@Min(value = 0, message = "不能小於 0")` |
| `@Max` | 最大值 | `@Max(value = 100)` |
| `@Email` | 信箱格式 | `@Email(message = "信箱格式不正確")` |
| `@Pattern` | 正規表示式 | `@Pattern(regexp = "^1[3-9]\\d{9}$")` |
| `@DecimalMin` | BigDecimal 最小值 | `@DecimalMin("0.01")` |
| `@DecimalMax` | BigDecimal 最大值 | `@DecimalMax("999999.99")` |
| `@Future` | 未來日期 | `@Future` |
| `@Past` | 過去日期 | `@Past` |

### 分組驗證

```java
// 定義驗證組
public interface Add {}
public interface Edit {}

@Data
public class SysUser {
    
    @NotNull(message = "用戶 ID 不能為空", groups = {Edit.class})
    private Long userId;
    
    @NotBlank(message = "用戶名稱不能為空")
    private String userName;
    
    @NotBlank(message = "密碼不能為空", groups = {Add.class})
    private String password;
}

// Controller 使用
@PostMapping
public AjaxResult add(@Validated(Add.class) @RequestBody SysUser user) {
    return toAjax(userService.insertUser(user));
}

@PutMapping
public AjaxResult edit(@Validated(Edit.class) @RequestBody SysUser user) {
    return toAjax(userService.updateUser(user));
}
```

---

## Service 層業務驗證

### 檢查必填欄位

```java
@Service
public class SysUserServiceImpl implements ISysUserService {
    
    @Override
    public int insertUser(SysUser user) {
        // 檢查用戶名稱
        if (StringUtils.isEmpty(user.getUserName())) {
            throw new ServiceException("用戶名稱不能為空");
        }
        
        // 檢查部門
        if (user.getDeptId() == null) {
            throw new ServiceException("部門不能為空");
        }
        
        return userMapper.insertUser(user);
    }
}
```

### 檢查唯一性

```java
@Override
public int insertUser(SysUser user) {
    // 檢查用戶名稱是否已存在
    SysUser existUser = userMapper.selectUserByUserName(user.getUserName());
    if (existUser != null) {
        throw new ServiceException(
            String.format("新增用戶「%s」失敗，用戶名稱已存在", user.getUserName())
        );
    }
    
    // 檢查信箱是否已存在
    if (StringUtils.isNotEmpty(user.getEmail())) {
        SysUser existEmail = userMapper.selectUserByEmail(user.getEmail());
        if (existEmail != null) {
            throw new ServiceException(
                String.format("新增用戶「%s」失敗，信箱已存在", user.getEmail())
            );
        }
    }
    
    return userMapper.insertUser(user);
}
```

### 檢查業務規則

```java
@Override
public int updateUserStatus(Long userId, Status status) {
    // 檢查用戶是否存在
    SysUser user = userMapper.selectUserById(userId);
    if (user == null) {
        throw new ServiceException("用戶不存在");
    }
    
    // 不能停用超級管理員
    if (user.isAdmin() && status == Status.DISABLE) {
        throw new ServiceException("不允許停用超級管理員");
    }
    
    // 檢查部門狀態
    SysDept dept = deptMapper.selectDeptById(user.getDeptId());
    if (dept.getStatus() == Status.DISABLE) {
        throw new ServiceException("所屬部門已停用，無法啟用用戶");
    }
    
    user.setStatus(status);
    return userMapper.updateUser(user);
}
```

### 檢查關聯資料

```java
@Override
public int deleteCategory(Long categoryId) {
    // 檢查分類是否存在
    InvCategory category = categoryMapper.selectById(categoryId);
    if (category == null) {
        throw new ServiceException("分類不存在");
    }
    
    // 檢查是否有子分類
    List<InvCategory> children = categoryMapper.selectChildrenById(categoryId);
    if (!children.isEmpty()) {
        throw new ServiceException(
            String.format("分類「%s」存在 %d 個子分類，無法刪除", 
                category.getCategoryName(), children.size())
        );
    }
    
    // 檢查是否被物品使用
    int itemCount = itemMapper.countByCategoryId(categoryId);
    if (itemCount > 0) {
        throw new ServiceException(
            String.format("分類「%s」正被 %d 個物品使用，無法刪除", 
                category.getCategoryName(), itemCount)
        );
    }
    
    return categoryMapper.deleteById(categoryId);
}
```

---

## 工具類判空方法

### StringUtils 判空

```java
// 字串判空
StringUtils.isEmpty(str)           // null 或 ""
StringUtils.isNotEmpty(str)        // 不為 null 且不為 ""
StringUtils.isBlank(str)           // null 或 "" 或空白
StringUtils.isNotBlank(str)        // 不為 null 且不為 "" 且不為空白

// 範例
if (StringUtils.isEmpty(user.getUserName())) {
    throw new ServiceException("用戶名稱不能為空");
}

if (StringUtils.isBlank(user.getEmail())) {
    // 信箱為空或只有空白字元
}
```

### 物件判空

```java
// 物件判空
StringUtils.isNull(obj)            // obj == null
StringUtils.isNotNull(obj)         // obj != null

// List 判空
StringUtils.isEmpty(list)          // list == null || list.isEmpty()
StringUtils.isNotEmpty(list)       // list != null && !list.isEmpty()

// 範例
if (StringUtils.isNull(user)) {
    throw new ServiceException("用戶不存在");
}

List<SysUser> list = userMapper.selectUserList(user);
if (StringUtils.isEmpty(list)) {
    return Collections.emptyList();
}
```

### 陣列判空

```java
// 陣列判空
if (ArrayUtils.isEmpty(userIds)) {
    throw new ServiceException("請選擇要刪除的用戶");
}

if (ArrayUtils.isNotEmpty(roleIds)) {
    // 處理角色 IDs
}
```

---

## 數字驗證

### 正整數驗證

```java
@Min(value = 1, message = "數量必須大於 0")
@Max(value = 99999, message = "數量不能超過 99999")
private Integer quantity;

// Service 層
if (quantity == null || quantity <= 0) {
    throw new ServiceException("數量必須大於 0");
}
```

### 金額驗證

```java
@DecimalMin(value = "0.01", message = "金額必須大於 0")
@DecimalMax(value = "999999.99", message = "金額不能超過 999999.99")
@Digits(integer = 6, fraction = 2, message = "金額格式不正確")
private BigDecimal amount;

// Service 層
if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
    throw new ServiceException("金額必須大於 0");
}

if (amount.compareTo(new BigDecimal("999999.99")) > 0) {
    throw new ServiceException("金額不能超過 999999.99");
}
```

---

## 日期驗證

### 日期範圍驗證

```java
@Service
public class ReportService {
    
    public List<Report> generateReport(Date startTime, Date endTime) {
        // 檢查開始時間
        if (startTime == null) {
            throw new ServiceException("開始時間不能為空");
        }
        
        // 檢查結束時間
        if (endTime == null) {
            throw new ServiceException("結束時間不能為空");
        }
        
        // 檢查時間順序
        if (startTime.after(endTime)) {
            throw new ServiceException("開始時間不能晚於結束時間");
        }
        
        // 檢查時間範圍（不能超過 1 年）
        long days = (endTime.getTime() - startTime.getTime()) / (1000 * 60 * 60 * 24);
        if (days > 365) {
            throw new ServiceException("查詢範圍不能超過 1 年");
        }
        
        return reportMapper.selectReport(startTime, endTime);
    }
}
```

---

## 檔案上傳驗證

### 檔案大小和類型驗證

```java
@Service
public class FileUploadService {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    
    public String uploadImage(MultipartFile file) {
        // 檢查檔案是否為空
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上傳檔案不能為空");
        }
        
        // 檢查檔案大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException("檔案大小不能超過 10MB");
        }
        
        // 檢查檔案類型
        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)) {
            throw new ServiceException("只允許上傳 jpg、jpeg、png、gif 格式的圖片");
        }
        
        // 上傳檔案
        return fileService.upload(file);
    }
}
```

---

## 集合驗證

### List/Set 驗證

```java
@Service
public class BatchService {
    
    public int batchDelete(List<Long> ids) {
        // 檢查 List 是否為空
        if (StringUtils.isEmpty(ids)) {
            throw new ServiceException("請選擇要刪除的資料");
        }
        
        // 檢查數量限制
        if (ids.size() > 100) {
            throw new ServiceException("批次刪除數量不能超過 100");
        }
        
        // 檢查是否有重複
        Set<Long> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() != ids.size()) {
            throw new ServiceException("存在重複的 ID");
        }
        
        return mapper.batchDelete(ids);
    }
}
```

---

## 自定義驗證器

### 建立自定義註解

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
    String message() default "手機號碼格式不正確";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 實作驗證器

```java
public class PhoneValidator implements ConstraintValidator<Phone, String> {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;  // null 由 @NotBlank 處理
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}
```

### 使用自定義驗證器

```java
@Data
public class SysUser {
    @Phone(message = "手機號碼格式不正確")
    private String phonenumber;
}
```

---

## 檢查清單

開發時必須檢查：

- [ ] Controller 是否加上 @Valid 或 @Validated？
- [ ] Entity 欄位是否有驗證註解？
- [ ] Service 是否檢查必填欄位？
- [ ] Service 是否檢查唯一性？
- [ ] Service 是否檢查業務規則？
- [ ] Service 是否檢查關聯資料？
- [ ] 是否使用 StringUtils 判空？
- [ ] 異常訊息是否清晰明確？

---

**下一步**: 閱讀 [RULE_11_CODE_REVIEW.md](./RULE_11_CODE_REVIEW.md) 學習程式碼審查清單
