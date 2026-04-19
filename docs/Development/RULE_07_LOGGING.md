# 日誌與監控規範

> **核心原則**: 合理使用日誌級別，保護敏感資訊

---

## 日誌級別使用

### ERROR - 系統錯誤、需要立即處理

```java
@Slf4j
@Service
public class UserService {
    
    public void processUser(Long userId) {
        try {
            // 業務邏輯
            userMapper.updateUser(user);
        } catch (Exception e) {
            // ERROR: 系統錯誤、異常堆疊
            log.error("處理用戶資料失敗, userId={}", userId, e);
            throw new ServiceException("處理失敗");
        }
    }
}
```

**使用時機**:
- 系統異常、需要人工介入
- 資料庫連線失敗
- 第三方 API 呼叫失敗
- 重要業務邏輯失敗

### WARN - 業務警告、降級處理

```java
@Slf4j
@Service
public class UserService {
    
    public void login(String userName, String password) {
        SysUser user = userMapper.selectUserByUserName(userName);
        
        // WARN: 業務警告
        if (user == null) {
            log.warn("用戶登入失敗，用戶不存在, userName={}", userName);
            throw new ServiceException("用戶名稱或密碼錯誤");
        }
        
        if (user.getStatus() == Status.DISABLE) {
            log.warn("用戶已停用, userId={}, userName={}", user.getUserId(), userName);
            throw new ServiceException("用戶已停用");
        }
        
        // 登入邏輯
    }
}
```

**使用時機**:
- 業務異常（如：用戶不存在、權限不足）
- 降級處理（如：快取失效改查資料庫）
- 可能有問題但不影響運作
- 超過閾值的警告

### INFO - 重要業務操作、狀態變更

```java
@Slf4j
@Service
public class UserService {
    
    public void login(String userName) {
        // INFO: 重要業務操作
        log.info("用戶登入成功, userName={}", userName);
    }
    
    @Transactional
    public int insertUser(SysUser user) {
        int result = userMapper.insertUser(user);
        // INFO: 重要資料變更
        log.info("新增用戶成功, userId={}, userName={}", user.getUserId(), user.getUserName());
        return result;
    }
    
    public void updateUserStatus(Long userId, Status status) {
        userMapper.updateStatus(userId, status);
        // INFO: 狀態變更
        log.info("更新用戶狀態, userId={}, status={}", userId, status.getDescription());
    }
}
```

**使用時機**:
- 用戶登入/登出
- 重要業務操作（新增/修改/刪除）
- 系統啟動/關閉
- 定時任務執行

### DEBUG - 開發除錯資訊（正式環境關閉）

```java
@Slf4j
@Service
public class UserService {
    
    public List<SysUser> selectUserList(SysUser user) {
        // DEBUG: 開發除錯
        log.debug("查詢用戶列表, 查詢條件: {}", user);
        
        List<SysUser> list = userMapper.selectUserList(user);
        
        log.debug("查詢結果數量: {}", list.size());
        return list;
    }
}
```

**使用時機**:
- 開發除錯
- 查看變數值
- 追蹤程式執行流程
- **正式環境必須關閉 DEBUG 級別**

---

## 敏感資訊脫敏

### ❌ 不要記錄敏感資訊

```java
// ❌ 絕對不要記錄密碼
log.info("用戶登入, userName={}, password={}", userName, password);

// ❌ 不要記錄 Token
log.info("API Token: {}", apiToken);

// ❌ 不要記錄完整信用卡號
log.info("信用卡號: {}", cardNumber);

// ❌ 不要記錄身分證號
log.info("身分證號: {}", idNumber);
```

### ✅ 正確的做法

```java
@Slf4j
@Service
public class UserService {
    
    public void login(String userName, String password) {
        // ✅ 只記錄用戶名稱
        log.info("用戶嘗試登入, userName={}", userName);
        
        // 驗證密碼...
        
        log.info("用戶登入成功, userName={}", userName);
    }
    
    public void processPayment(String cardNumber, BigDecimal amount) {
        // ✅ 脫敏：只顯示後四碼
        String maskedCard = maskCardNumber(cardNumber);
        log.info("處理付款, 卡號後四碼={}, 金額={}", maskedCard, amount);
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
```

### 脫敏工具方法

```java
public class SensitiveUtils {
    
    /**
     * 手機號脫敏：138****5678
     */
    public static String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 信箱脫敏：t****@example.com
     */
    public static String maskEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) {
            return email;
        }
        int index = email.indexOf("@");
        String username = email.substring(0, index);
        String domain = email.substring(index);
        
        if (username.length() <= 1) {
            return "*" + domain;
        }
        return username.charAt(0) + "****" + domain;
    }
    
    /**
     * 身分證號脫敏：A12****567
     */
    public static String maskIdNumber(String idNumber) {
        if (StringUtils.isEmpty(idNumber) || idNumber.length() < 10) {
            return "****";
        }
        return idNumber.substring(0, 3) + "****" + idNumber.substring(7);
    }
}
```

---

## 結構化日誌

### 包含關鍵資訊

```java
@Slf4j
@Service
public class OrderService {
    
    @Transactional
    public void processOrder(Order order) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 處理訂單邏輯
            orderMapper.insert(order);
            stockMapper.decreaseQuantity(order.getItemId(), order.getQuantity());
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 結構化日誌：包含 traceId, userId, operation, duration
            log.info("處理訂單成功, orderId={}, userId={}, itemId={}, quantity={}, duration={}ms",
                order.getOrderId(), 
                order.getUserId(), 
                order.getItemId(), 
                order.getQuantity(),
                duration);
                
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("處理訂單失敗, orderId={}, userId={}, duration={}ms", 
                order.getOrderId(), order.getUserId(), duration, e);
            throw new ServiceException("處理訂單失敗");
        }
    }
}
```

### 使用 MDC 追蹤請求

```java
@Component
public class TraceIdInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        // 產生 traceId
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("traceId", traceId);
        
        // 也可以從請求標頭取得
        String requestTraceId = request.getHeader("X-Trace-Id");
        if (StringUtils.isNotEmpty(requestTraceId)) {
            MDC.put("traceId", requestTraceId);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Object handler, 
                                Exception ex) {
        // 清除 MDC
        MDC.clear();
    }
}
```

**logback.xml 配置**:
```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder>
            <!-- 包含 traceId -->
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

---

## Druid 慢查詢監控

### 配置說明

```yaml
spring:
  datasource:
    druid:
      filter:
        stat:
          enabled: true
          log-slow-sql: true        # 啟用慢查詢記錄
          slow-sql-millis: 1000     # 慢查詢閾值：1 秒
          merge-sql: true           # 合併相同 SQL
```

### 查看慢查詢

**監控頁面**: `http://localhost:8080/druid/sql.html`

**Druid 監控登入**:
- 帳號: `cheng`
- 密碼: `123456`
- 語言: 繁體中文（已配置 `language: zh_TW`）

### 慢查詢分析

```sql
-- 查看執行時間最長的 SQL
SELECT * FROM sys_user WHERE user_name LIKE '%test%';  -- ❌ 全表掃描

-- 優化：加索引
CREATE INDEX idx_user_name ON sys_user(user_name);

-- 或避免前導萬用字元
SELECT * FROM sys_user WHERE user_name LIKE 'test%';  -- ✅ 使用索引
```

---

## 日誌級別配置

### application.yml

```yaml
logging:
  level:
    root: INFO                                    # 根日誌級別
    com.cheng: INFO                              # 專案日誌級別
    com.cheng.system.mapper: DEBUG               # Mapper SQL 日誌（開發環境）
    org.springframework: WARN                    # Spring 框架
    org.mybatis: WARN                            # MyBatis
    com.alibaba.druid: INFO                      # Druid
```

### 不同環境的配置

**application-local.yml**（本地開發）:
```yaml
logging:
  level:
    root: INFO
    com.cheng: DEBUG                             # 開發環境使用 DEBUG
    com.cheng.system.mapper: DEBUG               # 顯示 SQL
```

**application-prod.yml**（正式環境）:
```yaml
logging:
  level:
    root: INFO
    com.cheng: INFO                              # 正式環境使用 INFO
    com.cheng.system.mapper: WARN                # 不顯示 SQL
```

---

## 日誌最佳實踐

### 1. 使用參數化日誌

✅ **正確**:
```java
log.info("用戶登入, userName={}, ip={}", userName, ip);
```

❌ **錯誤**:
```java
log.info("用戶登入, userName=" + userName + ", ip=" + ip);  // 字串拼接，效能差
```

### 2. 避免無意義的日誌

❌ **錯誤**:
```java
log.info("進入 selectUserList 方法");
log.info("離開 selectUserList 方法");
```

✅ **正確**:
```java
// 只記錄關鍵資訊
log.info("查詢用戶列表, 查詢條件: {}, 結果數量: {}", user, list.size());
```

### 3. 異常日誌必須包含堆疊

✅ **正確**:
```java
try {
    processData();
} catch (Exception e) {
    log.error("處理資料失敗", e);  // 包含堆疊追蹤
}
```

❌ **錯誤**:
```java
try {
    processData();
} catch (Exception e) {
    log.error("處理資料失敗: {}", e.getMessage());  // 沒有堆疊追蹤
}
```

### 4. 使用 @Slf4j 註解

```java
@Slf4j  // Lombok 自動產生 log 欄位
@Service
public class UserService {
    public void processUser() {
        log.info("處理用戶");
    }
}
```

---

## 檢查清單

開發時必須檢查：

- [ ] 日誌級別是否正確？
- [ ] 是否記錄敏感資訊（密碼、Token）？
- [ ] 異常日誌是否包含堆疊追蹤？
- [ ] 是否使用參數化日誌？
- [ ] 重要操作是否有日誌記錄？
- [ ] 正式環境是否關閉 DEBUG 級別？
- [ ] 慢查詢數是否為 0？

---

**下一步**: 閱讀 [RULE_08_API_DESIGN.md](./RULE_08_API_DESIGN.md) 學習 API 設計規範
