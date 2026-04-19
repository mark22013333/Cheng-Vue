# 效能優化規範

> **目標**: 支援 500 人同時線上、100 人並發操作

---

## 資料庫層優化

### 必須使用分頁查詢

✅ **正確示範**:
```java
@GetMapping("/list")
public TableDataInfo list(SysUser user) {
    startPage();  // PageHelper 自動從請求參數取得 pageNum 和 pageSize
    List<SysUser> list = userService.selectUserList(user);
    return getDataTable(list);
}
```

❌ **錯誤示範**:
```java
@GetMapping("/list")
public AjaxResult list(SysUser user) {
    // 沒有分頁！可能返回數萬筆資料
    List<SysUser> list = userService.selectUserList(user);
    return AjaxResult.success(list);
}
```

### 避免 N+1 查詢問題

❌ **N+1 查詢**:
```xml
<resultMap id="UserResult" type="SysUser">
    <id property="userId" column="user_id"/>
    <result property="userName" column="user_name"/>
    <!-- 每個 user 都會額外查詢一次 dept -->
    <association property="dept" column="dept_id" 
                 select="selectDeptById" />
</resultMap>

<!-- 查詢 100 個用戶 = 1 次主查詢 + 100 次子查詢 = 101 次查詢 -->
```

✅ **使用 JOIN**:
```xml
<select id="selectUserList" resultMap="UserResult">
    SELECT u.*, d.dept_name, d.leader
    FROM sys_user u
    LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
    WHERE u.del_flag = '0'
</select>

<!-- 只需 1 次查詢 -->
```

### 建立適當索引

```sql
-- 經常作為 WHERE 條件的欄位
CREATE INDEX idx_user_name ON sys_user(user_name);

-- JOIN 關聯的欄位
CREATE INDEX idx_dept_id ON sys_user(dept_id);

-- 組合索引（注意順序）
CREATE INDEX idx_status_create_time ON sys_user(status, create_time);

-- 避免對小表（< 1000 筆）建立過多索引
```

### Druid 連線池設定

```yaml
druid:
  initialSize: 5       # 初始連線數
  minIdle: 10          # 最小閒置連線
  maxActive: 20        # 最大連線數（針對 100 並發）
  maxWait: 60000       # 取得連線最大等待時間 (60 秒)
  
  # 連線存活檢查
  timeBetweenEvictionRunsMillis: 60000
  minEvictableIdleTimeMillis: 300000
  validationQuery: SELECT 1 FROM DUAL
  testWhileIdle: true
  
  # 慢查詢記錄
  filter:
    stat:
      log-slow-sql: true
      slow-sql-millis: 1000  # 超過 1 秒記錄
```

**監控位置**: `http://localhost:8080/druid/sql.html`

---

## 快取層優化

### Redis 快取熱點資料

```java
@Service
public class SysConfigService {
    @Autowired
    private RedisCache redisCache;
    
    private static final String CONFIG_CACHE_KEY = "sys_config:";
    
    public String getConfigValueByKey(String configKey) {
        // 1. 先查快取
        String cacheKey = CONFIG_CACHE_KEY + configKey;
        String value = redisCache.getCacheObject(cacheKey);
        if (StringUtils.isNotEmpty(value)) {
            return value;  // 快取命中
        }
        
        // 2. 查詢資料庫
        value = configMapper.selectConfigByKey(configKey);
        
        // 3. 寫入快取（30 分鐘過期）
        redisCache.setCacheObject(cacheKey, value, 30, TimeUnit.MINUTES);
        return value;
    }
    
    public void updateConfig(SysConfig config) {
        // 更新資料庫
        configMapper.updateConfig(config);
        
        // 刪除快取
        String cacheKey = CONFIG_CACHE_KEY + config.getConfigKey();
        redisCache.deleteObject(cacheKey);
    }
}
```

### 快取策略

| 資料類型 | 過期時間 | 更新策略 |
|---------|---------|--------|
| 字典資料 | 30 分鐘 | 更新時刪除快取 |
| 用戶權限 | 30 分鐘 | 登出/修改權限時刪除 |
| 系統配置 | 30 分鐘 | 更新時刪除快取 |
| 驗證碼 | 5 分鐘 | 驗證後立即刪除 |
| JWT Token | 30 分鐘 | 登出時加入黑名單 |

### 防止快取穿透

```java
public SysUser getUserById(Long userId) {
    String cacheKey = "user:" + userId;
    
    // 查快取
    SysUser user = redisCache.getCacheObject(cacheKey);
    if (user != null) {
        return user;
    }
    
    // 查資料庫
    user = userMapper.selectUserById(userId);
    
    if (user != null) {
        // 正常資料：快取 30 分鐘
        redisCache.setCacheObject(cacheKey, user, 30, TimeUnit.MINUTES);
    } else {
        // 空資料：快取 5 分鐘，防止穿透
        redisCache.setCacheObject(cacheKey, new SysUser(), 5, TimeUnit.MINUTES);
    }
    
    return user;
}
```

---

## 並發控制

### 防止重複提交 (@RepeatSubmit)

```java
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    
    @PostMapping
    @RepeatSubmit  // 5 秒內防止重複提交
    public AjaxResult add(@RequestBody SysUser user) {
        return toAjax(userService.insertUser(user));
    }
    
    @PostMapping("/order")
    @RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS)  // 自訂間隔
    public AjaxResult createOrder(@RequestBody Order order) {
        return toAjax(orderService.createOrder(order));
    }
}
```

**原理**: 使用 Redis 記錄請求的唯一標識（URL + 參數 + Token），設定過期時間。

### 介面限流 (@RateLimiter)

```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/data")
    @RateLimiter(time = 60, count = 100)  // 每分鐘最多 100 次
    public AjaxResult getData() {
        return AjaxResult.success(data);
    }
    
    @PostMapping("/export")
    @RateLimiter(time = 60, count = 10)  // 匯出操作：每分鐘最多 10 次
    public void export() {
        // 匯出邏輯
    }
}
```

### 樂觀鎖處理並發更新

```java
@Data
@TableName("inv_stock")
public class InvStock {
    private Long stockId;
    private Integer quantity;
    
    @Version  // MyBatis-Plus 樂觀鎖
    private Integer version;
}

// 或使用原生 SQL
@Update("UPDATE inv_stock SET quantity = #{quantity}, version = version + 1 " +
        "WHERE stock_id = #{stockId} AND version = #{version}")
int updateStock(InvStock stock);

// Service 層處理
@Service
public class StockService {
    public void decreaseStock(Long stockId, Integer quantity) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            InvStock stock = stockMapper.selectById(stockId);
            stock.setQuantity(stock.getQuantity() - quantity);
            
            int result = stockMapper.updateStock(stock);
            if (result > 0) {
                return;  // 更新成功
            }
            // 更新失敗，重試
        }
        throw new ServiceException("庫存更新失敗，請稍後再試");
    }
}
```

### 分散式鎖 (Redis)

```java
@Service
public class OrderService {
    @Autowired
    private RedisCache redisCache;
    
    public void processOrder(String orderId) {
        String lockKey = "order:lock:" + orderId;
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // 取得鎖（10 秒過期）
            boolean locked = redisCache.setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new ServiceException("訂單處理中，請稍後再試");
            }
            
            // 執行業務邏輯
            doProcessOrder(orderId);
            
        } finally {
            // 釋放鎖（確認是自己的鎖）
            String currentValue = redisCache.getCacheObject(lockKey);
            if (lockValue.equals(currentValue)) {
                redisCache.deleteObject(lockKey);
            }
        }
    }
}
```

---

## 前端優化

### 路由懶載入

```javascript
// router/index.js
export default new Router({
  routes: [
    {
      path: '/system/user',
      component: () => import('@/views/system/user/index'),  // 懶載入
      name: 'User',
      meta: { title: '用戶管理' }
    }
  ]
})
```

### 圖片懶載入

```vue
<template>
  <el-image 
    v-for="url in imageList" 
    :key="url"
    :src="url" 
    lazy
  />
</template>
```

### 列表虛擬滾動

針對大量資料（> 1000 筆）使用虛擬滾動：

```javascript
import VirtualList from 'vue-virtual-scroll-list'

export default {
  components: { VirtualList },
  template: `
    <virtual-list
      :data-key="'id'"
      :data-sources="items"
      :data-component="itemComponent"
      :estimate-size="50"
    />
  `
}
```

---

## 效能監控

### 關鍵指標

```java
@Slf4j
@Aspect
@Component
public class PerformanceMonitorAspect {
    
    @Around("@annotation(com.cheng.common.annotation.Monitor)")
    public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 記錄執行時間
            log.info("方法 {} 執行時間: {}ms", 
                joinPoint.getSignature().getName(), duration);
            
            // 超過 1 秒警告
            if (duration > 1000) {
                log.warn("方法 {} 執行緩慢: {}ms", 
                    joinPoint.getSignature().getName(), duration);
            }
            
            return result;
        } catch (Throwable throwable) {
            log.error("方法 {} 執行異常", joinPoint.getSignature().getName(), throwable);
            throw throwable;
        }
    }
}
```

### 效能檢查清單

開發完成後檢查：

- [ ] 所有列表查詢都使用分頁？
- [ ] 沒有 N+1 查詢問題？
- [ ] 熱點資料有快取？
- [ ] 有適當的索引？
- [ ] 關鍵操作有並發控制？
- [ ] API 回應時間 < 1 秒？
- [ ] 慢查詢數 = 0？

---

**下一步**: 閱讀 [RULE_05_EXCEPTION_HANDLING.md](./RULE_05_EXCEPTION_HANDLING.md) 學習異常處理
