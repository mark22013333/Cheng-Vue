# 異常處理規範

> **核心原則**: 統一使用 ServiceException，異常訊息要清晰明確

---

## 統一使用 ServiceException

### 為什麼使用 ServiceException？

```java
package com.cheng.common.exception;

/**
 * 業務異常
 */
public class ServiceException extends RuntimeException {
    private Integer code;
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}
```

**優勢**:
- ✅ 統一異常處理
- ✅ 自動回滾事務（RuntimeException）
- ✅ 全域攔截處理
- ✅ 前端友善的錯誤訊息

---

## 異常訊息規範

### ✅ 好的異常訊息

**清晰、具體、可操作**:

```java
// ✅ 明確說明原因和影響
throw new ServiceException("分類「電子產品」正被 5 個物品使用，無法刪除");

// ✅ 告知用戶如何解決
throw new ServiceException("部門停用，不允許新增子部門");

// ✅ 包含關鍵資訊
throw new ServiceException(
    String.format("用戶「%s」已存在，請使用其他用戶名稱", userName)
);

// ✅ 給出具體的限制
throw new ServiceException("檔案大小超過 10MB 限制");
```

### ❌ 不好的異常訊息

**模糊、籠統、無用**:

```java
// ❌ 太模糊
throw new ServiceException("刪除失敗");

// ❌ 太技術化
throw new ServiceException("NullPointerException occurred");

// ❌ 英文訊息
throw new ServiceException("Delete failed");

// ❌ 沒有說明原因
throw new ServiceException("操作錯誤");
```

---

## 使用範例

### 檢查資料是否存在

```java
@Service
public class InvCategoryService {
    
    public int deleteCategory(Long categoryId) {
        // 檢查分類是否存在
        InvCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new ServiceException("分類不存在");
        }
        
        // 檢查是否被使用
        List<InvItem> items = itemMapper.selectByCategoryId(categoryId);
        if (items != null && !items.isEmpty()) {
            throw new ServiceException(
                String.format("分類「%s」正被 %d 個物品使用，無法刪除", 
                    category.getCategoryName(), items.size())
            );
        }
        
        return categoryMapper.deleteById(categoryId);
    }
}
```

### 檢查業務規則

```java
@Service
public class SysUserService {
    
    public int insertUser(SysUser user) {
        // 檢查用戶名稱
        if (StringUtils.isEmpty(user.getUserName())) {
            throw new ServiceException("用戶名稱不能為空");
        }
        
        // 檢查唯一性
        SysUser existUser = userMapper.selectUserByUserName(user.getUserName());
        if (existUser != null) {
            throw new ServiceException(
                String.format("新增用戶「%s」失敗，用戶名稱已存在", user.getUserName())
            );
        }
        
        // 檢查部門狀態
        SysDept dept = deptMapper.selectDeptById(user.getDeptId());
        if (dept.getStatus() == Status.DISABLE) {
            throw new ServiceException("部門停用，不允許新增用戶");
        }
        
        return userMapper.insertUser(user);
    }
}
```

### 檢查權限

```java
@Service
public class SysRoleService {
    
    public void checkRoleAllowed(SysRole role) {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin()) {
            throw new ServiceException("不允許操作超級管理員角色");
        }
    }
    
    public int deleteRoles(Long[] roleIds) {
        for (Long roleId : roleIds) {
            SysRole role = selectRoleById(roleId);
            checkRoleAllowed(role);
            
            // 檢查是否已分配
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new ServiceException(
                    String.format("角色「%s」已分配給用戶，不能刪除", role.getRoleName())
                );
            }
        }
        return roleMapper.deleteRoleByIds(roleIds);
    }
}
```

### 檢查資料合法性

```java
@Service
public class InvItemService {
    
    public InvItem scanItemByCode(String scanCode, String scanType) {
        // 檢查掃描內容
        if (StringUtils.isEmpty(scanCode)) {
            throw new ServiceException("掃描內容不能為空");
        }
        
        // 查詢物品
        InvItem item = null;
        if ("QR_CODE".equals(scanType)) {
            item = itemMapper.selectItemByQrCode(scanCode);
        } else if ("BARCODE".equals(scanType)) {
            item = itemMapper.selectItemByBarcode(scanCode);
        }
        
        // 檢查是否找到
        if (item == null) {
            throw new ServiceException(
                String.format("未找到對應的物品，掃描內容：%s", scanCode)
            );
        }
        
        return item;
    }
}
```

---

## 全域異常處理

### GlobalExceptionHandler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 業務異常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("業務異常: {} - {}", request.getRequestURI(), e.getMessage());
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) 
            ? AjaxResult.error(code, e.getMessage()) 
            : AjaxResult.error(e.getMessage());
    }
    
    /**
     * 權限異常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("權限異常: {} - {}", request.getRequestURI(), e.getMessage());
        return AjaxResult.error(HttpStatus.FORBIDDEN, "沒有權限，請聯繫管理員授權");
    }
    
    /**
     * 參數驗證異常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("參數驗證異常: {}", message);
        return AjaxResult.error(message);
    }
    
    /**
     * 系統異常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        log.error("系統異常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return AjaxResult.error("系統繁忙，請稍後再試");
    }
}
```

---

## 異常處理最佳實踐

### 1. 在 Service 層拋出異常

✅ **正確**:
```java
@Service
public class UserService {
    public void updateUser(SysUser user) {
        if (user.getUserId() == null) {
            throw new ServiceException("用戶 ID 不能為空");
        }
        userMapper.updateUser(user);
    }
}
```

❌ **錯誤**:
```java
@RestController
public class UserController {
    public AjaxResult update(@RequestBody SysUser user) {
        // 不要在 Controller 拋出業務異常
        if (user.getUserId() == null) {
            throw new ServiceException("用戶 ID 不能為空");
        }
        return toAjax(userService.updateUser(user));
    }
}
```

### 2. 不要吞掉異常

❌ **錯誤**:
```java
try {
    processData();
} catch (Exception e) {
    // 什麼都不做，吞掉異常！
}
```

✅ **正確**:
```java
try {
    processData();
} catch (Exception e) {
    log.error("處理資料失敗", e);
    throw new ServiceException("處理資料失敗：" + e.getMessage());
}
```

### 3. 使用 finally 或 try-with-resources

✅ **資源清理**:
```java
// 使用 try-with-resources（推薦）
try (InputStream is = new FileInputStream(file)) {
    // 處理檔案
} catch (IOException e) {
    throw new ServiceException("讀取檔案失敗");
}

// 或使用 finally
InputStream is = null;
try {
    is = new FileInputStream(file);
    // 處理檔案
} catch (IOException e) {
    throw new ServiceException("讀取檔案失敗");
} finally {
    if (is != null) {
        try {
            is.close();
        } catch (IOException e) {
            log.error("關閉檔案失敗", e);
        }
    }
}
```

### 4. 異常訊息不要洩漏敏感資訊

❌ **錯誤**:
```java
throw new ServiceException("SQL: SELECT * FROM sys_user WHERE password = '123456'");
throw new ServiceException("連線資料庫失敗：jdbc:mysql://192.168.1.100:3306/db");
```

✅ **正確**:
```java
throw new ServiceException("查詢用戶失敗");
throw new ServiceException("連線資料庫失敗");
```

---

## 異常分類

### 業務異常 (ServiceException)
- 用戶輸入錯誤
- 業務規則不滿足
- 資料不存在
- 權限不足

### 系統異常 (Exception)
- 資料庫連線失敗
- 網路請求失敗
- 檔案讀寫失敗
- 第三方 API 錯誤

---

## 檢查清單

開發時必須檢查：

- [ ] 是否使用 ServiceException？
- [ ] 異常訊息是否清晰明確？
- [ ] 異常訊息是否使用繁體中文？
- [ ] 是否包含關鍵資訊（如：資料名稱、原因）？
- [ ] 是否告知用戶如何解決？
- [ ] 是否洩漏敏感資訊？
- [ ] 異常是否被正確處理（不要吞掉）？
- [ ] 資源是否正確釋放？

---

**下一步**: 閱讀 [RULE_06_TRANSACTION.md](./RULE_06_TRANSACTION.md) 學習事務管理
