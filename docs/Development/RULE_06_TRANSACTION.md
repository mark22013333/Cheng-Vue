# 事務管理規範

> **核心原則**: 正確使用 @Transactional，避免大事務

---

## @Transactional 使用時機

### ✅ 必須使用事務的場景

#### 1. 多表操作

```java
@Service
public class InvBorrowService {
    
    @Transactional(rollbackFor = Exception.class)
    public int borrowItem(InvBorrow borrow) {
        // 1. 新增借出記錄
        borrowMapper.insert(borrow);
        
        // 2. 更新庫存數量
        stockMapper.decreaseQuantity(borrow.getItemId(), borrow.getQuantity());
        
        // 3. 新增庫存異動記錄
        InvStockRecord record = buildStockRecord(borrow);
        stockRecordMapper.insert(record);
        
        return 1;
    }
}
```

#### 2. 涉及金額、庫存等關鍵資料

```java
@Service
public class AccountService {
    
    @Transactional(rollbackFor = Exception.class)
    public void transferAmount(Long fromUserId, Long toUserId, BigDecimal amount) {
        // 檢查餘額
        Account fromAccount = accountMapper.selectById(fromUserId);
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ServiceException("餘額不足");
        }
        
        // 扣款
        accountMapper.decreaseBalance(fromUserId, amount);
        
        // 加款
        accountMapper.increaseBalance(toUserId, amount);
        
        // 記錄交易
        Transaction transaction = buildTransaction(fromUserId, toUserId, amount);
        transactionMapper.insert(transaction);
    }
}
```

#### 3. 主從表操作

```java
@Service
public class OrderService {
    
    @Transactional(rollbackFor = Exception.class)
    public int createOrder(Order order, List<OrderItem> items) {
        // 1. 新增訂單主表
        orderMapper.insert(order);
        
        // 2. 新增訂單明細
        for (OrderItem item : items) {
            item.setOrderId(order.getOrderId());
            orderItemMapper.insert(item);
        }
        
        // 3. 扣減庫存
        for (OrderItem item : items) {
            stockMapper.decreaseQuantity(item.getItemId(), item.getQuantity());
        }
        
        return 1;
    }
}
```

### ❌ 不需要事務的場景

```java
// 僅查詢操作，不需要 @Transactional
@Service
public class UserService {
    public List<SysUser> selectUserList(SysUser user) {
        return userMapper.selectUserList(user);
    }
    
    public SysUser selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }
}
```

---

## 事務傳播行為

### REQUIRED（預設）

```java
@Transactional  // 預設 REQUIRED
public void methodA() {
    // 如果有現有事務，加入該事務
    // 如果沒有事務，新建一個事務
    methodB();
}

@Transactional
public void methodB() {
    // 會加入 methodA 的事務
}
```

### REQUIRES_NEW

```java
@Transactional
public void methodA() {
    // 外層事務
    methodB();  // methodB 的異常不會影響 methodA
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void methodB() {
    // 總是新建事務，掛起 methodA 的事務
    // methodB 的異常只會回滾 methodB
}
```

**使用場景**: 記錄日誌、發送通知等獨立操作

```java
@Service
public class UserService {
    @Autowired
    private LogService logService;
    
    @Transactional
    public void importUsers(List<SysUser> users) {
        for (SysUser user : users) {
            try {
                userMapper.insert(user);
                // 記錄成功日誌（即使主事務失敗也要記錄）
                logService.recordSuccess(user.getUserName());
            } catch (Exception e) {
                // 記錄失敗日誌（即使主事務失敗也要記錄）
                logService.recordError(user.getUserName(), e.getMessage());
            }
        }
    }
}

@Service
public class LogService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String userName) {
        // 獨立事務，不受外層事務影響
        logMapper.insert(buildLog(userName, "SUCCESS"));
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordError(String userName, String error) {
        // 獨立事務，不受外層事務影響
        logMapper.insert(buildLog(userName, "ERROR", error));
    }
}
```

---

## 事務回滾配置

### RuntimeException 自動回滾

```java
@Transactional  // RuntimeException 自動回滾
public void updateUser(SysUser user) {
    userMapper.updateUser(user);
    
    if (someCondition) {
        // ServiceException 繼承 RuntimeException，會自動回滾
        throw new ServiceException("更新失敗");
    }
}
```

### Checked Exception 需手動配置

```java
@Transactional(rollbackFor = Exception.class)  // 建議加上
public void processFile(File file) throws IOException {
    // IOException 是 checked exception
    // 如果不加 rollbackFor，不會回滾
    userMapper.insert(user);
    
    if (!file.exists()) {
        throw new IOException("檔案不存在");  // 會回滾
    }
}
```

**建議**: 統一使用 `rollbackFor = Exception.class`

---

## 避免大事務

### ❌ 錯誤示範：大事務

```java
@Transactional
public void importUsers(List<SysUser> users) {
    // 假設 10,000 筆資料
    for (SysUser user : users) {
        // 1. 查詢部門（可能很慢）
        SysDept dept = deptMapper.selectDeptByName(user.getDeptName());
        
        // 2. 呼叫外部 API（可能很慢）
        String avatar = externalApi.getAvatar(user.getEmail());
        user.setAvatar(avatar);
        
        // 3. 新增用戶
        userMapper.insert(user);
        
        // 4. 發送郵件（可能很慢）
        emailService.sendWelcomeEmail(user.getEmail());
    }
    // 整個事務可能持續數分鐘，鎖表時間過長！
}
```

### ✅ 正確示範：拆分事務

```java
public void importUsers(List<SysUser> users) {
    for (SysUser user : users) {
        try {
            // 非核心邏輯：移到事務外
            SysDept dept = deptMapper.selectDeptByName(user.getDeptName());
            user.setDeptId(dept.getDeptId());
            
            String avatar = externalApi.getAvatar(user.getEmail());
            user.setAvatar(avatar);
            
            // 核心邏輯：使用事務
            insertUserWithTransaction(user);
            
            // 非核心邏輯：移到事務外
            emailService.sendWelcomeEmail(user.getEmail());
            
        } catch (Exception e) {
            log.error("匯入用戶失敗: {}", user.getUserName(), e);
        }
    }
}

@Transactional(rollbackFor = Exception.class)
private void insertUserWithTransaction(SysUser user) {
    // 只包含核心的資料庫操作
    userMapper.insert(user);
    
    // 新增用戶角色關聯
    userRoleMapper.insertUserRole(user.getUserId(), user.getRoleIds());
}
```

---

## 事務注意事項

### 1. @Transactional 只對 public 方法有效

❌ **錯誤**:
```java
@Transactional
private void updateUser(SysUser user) {
    // 事務不會生效！
}
```

✅ **正確**:
```java
@Transactional
public void updateUser(SysUser user) {
    // 事務生效
}
```

### 2. 同類別內部呼叫事務不生效

❌ **錯誤**:
```java
@Service
public class UserService {
    public void methodA() {
        // 內部呼叫，事務不生效
        this.methodB();
    }
    
    @Transactional
    public void methodB() {
        // 事務不會生效！
    }
}
```

✅ **正確**:
```java
@Service
public class UserService {
    @Autowired
    private UserService self;  // 注入自己
    
    public void methodA() {
        // 透過代理呼叫，事務生效
        self.methodB();
    }
    
    @Transactional
    public void methodB() {
        // 事務生效
    }
}
```

### 3. 捕獲異常後要手動回滾

❌ **錯誤**:
```java
@Transactional
public void updateUser(SysUser user) {
    try {
        userMapper.updateUser(user);
        // 可能拋出異常
        someRiskyOperation();
    } catch (Exception e) {
        log.error("操作失敗", e);
        // 異常被捕獲，事務不會回滾！
    }
}
```

✅ **正確方式 1：重新拋出異常**:
```java
@Transactional
public void updateUser(SysUser user) {
    try {
        userMapper.updateUser(user);
        someRiskyOperation();
    } catch (Exception e) {
        log.error("操作失敗", e);
        throw new ServiceException("更新用戶失敗", e);  // 重新拋出
    }
}
```

✅ **正確方式 2：手動回滾**:
```java
@Transactional
public void updateUser(SysUser user) {
    try {
        userMapper.updateUser(user);
        someRiskyOperation();
    } catch (Exception e) {
        log.error("操作失敗", e);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return;  // 或拋出異常
    }
}
```

---

## 事務最佳實踐

### 1. 統一使用 rollbackFor

```java
// 建議所有事務都加上 rollbackFor = Exception.class
@Transactional(rollbackFor = Exception.class)
public void someMethod() {
    // ...
}
```

### 2. 設定合理的逾時時間

```java
@Transactional(rollbackFor = Exception.class, timeout = 30)  // 30 秒
public void longRunningOperation() {
    // 長時間操作
}
```

### 3. 唯讀事務優化

```java
@Transactional(readOnly = true)
public List<SysUser> selectUserList(SysUser user) {
    // 唯讀事務，資料庫可以優化
    return userMapper.selectUserList(user);
}
```

---

## 檢查清單

開發時必須檢查：

- [ ] 多表操作是否加上 @Transactional？
- [ ] 是否加上 rollbackFor = Exception.class？
- [ ] 是否避免大事務（事務時間 < 3 秒）？
- [ ] 非核心邏輯是否移到事務外？
- [ ] @Transactional 是否加在 public 方法？
- [ ] 是否正確處理異常（不要吞掉）？
- [ ] 唯讀操作是否加上 readOnly = true？

---

**下一步**: 閱讀 [RULE_07_LOGGING.md](./RULE_07_LOGGING.md) 學習日誌規範
