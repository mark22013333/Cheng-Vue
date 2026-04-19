# 設計模式應用

> **目的**: 提升程式碼可維護性、可擴展性和可讀性

---

## 策略模式 (Strategy Pattern)

### 使用場景
處理不同類型的業務邏輯，避免大量 if-else

### 範例: 爬蟲類型處理

```java
// 1. 定義策略介面
public interface CrawlerStrategy {
    /**
     * 執行爬蟲
     */
    void crawl(String url);
    
    /**
     * 支援的爬蟲類型
     */
    CrawlerType getType();
}

// 2. 實作具體策略
@Component
public class CA102CrawlerStrategy implements CrawlerStrategy {
    @Override
    public void crawl(String url) {
        // CA102 特定的爬蟲邏輯
        log.info("執行 CA102 爬蟲: {}", url);
        // ...
    }
    
    @Override
    public CrawlerType getType() {
        return CrawlerType.CA102;
    }
}

@Component
public class BooksCrawlerStrategy implements CrawlerStrategy {
    @Override
    public void crawl(String url) {
        // Books.com.tw 特定的爬蟲邏輯
        log.info("執行 Books 爬蟲: {}", url);
        // ...
    }
    
    @Override
    public CrawlerType getType() {
        return CrawlerType.BOOKS;
    }
}

// 3. 策略管理器
@Service
public class CrawlerService {
    private final Map<CrawlerType, CrawlerStrategy> strategies;
    
    @Autowired
    public CrawlerService(List<CrawlerStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                CrawlerStrategy::getType,
                Function.identity()
            ));
    }
    
    public void execute(CrawlerType type, String url) {
        CrawlerStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new ServiceException("不支援的爬蟲類型: " + type);
        }
        strategy.crawl(url);
    }
}
```

### 優勢
- ✅ 易於新增新類型：只需實作新的 Strategy
- ✅ 符合開閉原則：對擴展開放，對修改關閉
- ✅ 避免大量 if-else

---

## 模板方法模式 (Template Method Pattern)

### 使用場景
統一流程控制，部分步驟由子類實作

### 範例: BaseController

```java
public abstract class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 開始分頁
     */
    protected void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 設定回應的分頁資料
     */
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查詢成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 回應返回結果
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }
}

// 子類使用
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
    @Autowired
    private ISysUserService userService;
    
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();  // 使用父類方法
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);  // 使用父類方法
    }
}
```

### 優勢
- ✅ 統一流程和規範
- ✅ 減少重複程式碼
- ✅ 易於維護

---

## 建造者模式 (Builder Pattern)

### 使用場景
建構複雜物件，參數眾多且可選

### 範例: 使用 Lombok @Builder

```java
@Data
@Builder
public class LineMessage {
    private String to;
    private MessageType type;
    private String content;
    private String altText;
    private List<Action> actions;
    private Template template;
}

// 使用
LineMessage message = LineMessage.builder()
    .to(userId)
    .type(MessageType.TEXT)
    .content("Hello, World!")
    .build();
    
LineMessage flexMessage = LineMessage.builder()
    .to(userId)
    .type(MessageType.FLEX)
    .altText("Flex Message")
    .template(buildFlexTemplate())
    .build();
```

### 優勢
- ✅ 可讀性強
- ✅ 避免構造函數參數過多
- ✅ 支援可選參數

---

## 工廠模式 (Factory Pattern)

### 使用場景
建立不同類型的物件

### 範例: 訊息處理器工廠

```java
// 1. 訊息處理器介面
public interface MessageHandler {
    void handle(Message message);
    MessageType getSupportedType();
}

// 2. 具體處理器
@Component
public class TextMessageHandler implements MessageHandler {
    @Override
    public void handle(Message message) {
        // 處理文字訊息
        log.info("處理文字訊息: {}", message.getText());
    }
    
    @Override
    public MessageType getSupportedType() {
        return MessageType.TEXT;
    }
}

@Component
public class ImageMessageHandler implements MessageHandler {
    @Override
    public void handle(Message message) {
        // 處理圖片訊息
        log.info("處理圖片訊息: {}", message.getImageUrl());
    }
    
    @Override
    public MessageType getSupportedType() {
        return MessageType.IMAGE;
    }
}

// 3. 工廠類
@Component
public class MessageHandlerFactory {
    private final Map<MessageType, MessageHandler> handlers;
    
    @Autowired
    public MessageHandlerFactory(List<MessageHandler> handlerList) {
        this.handlers = handlerList.stream()
            .collect(Collectors.toMap(
                MessageHandler::getSupportedType,
                Function.identity()
            ));
    }
    
    public MessageHandler getHandler(MessageType type) {
        MessageHandler handler = handlers.get(type);
        if (handler == null) {
            throw new ServiceException("不支援的訊息類型: " + type);
        }
        return handler;
    }
}

// 4. 使用
@Service
public class MessageService {
    @Autowired
    private MessageHandlerFactory handlerFactory;
    
    public void processMessage(Message message) {
        MessageHandler handler = handlerFactory.getHandler(message.getType());
        handler.handle(message);
    }
}
```

### 優勢
- ✅ 解耦物件建立邏輯
- ✅ 集中管理
- ✅ 易於擴展

---

## 單例模式 (Singleton Pattern)

### Spring Bean 預設單例

**注意**: Spring 管理的 Bean 預設是單例模式。

### ⚠️ 執行緒安全問題

❌ **錯誤示範**:
```java
@Service
public class UserService {
    // 危險！成員變數在多執行緒環境下會互相覆蓋
    private SysUser currentUser;
    
    public void processUser(Long userId) {
        // 執行緒 A 設定 currentUser = User A
        currentUser = userMapper.selectUserById(userId);
        
        // 此時執行緒 B 可能將 currentUser 改為 User B
        // 執行緒 A 接下來使用的 currentUser 可能是 User B 的資料！
        
        doSomething(currentUser);  // 錯誤！
    }
}
```

✅ **正確示範**:
```java
@Service
public class UserService {
    public void processUser(Long userId) {
        // 使用區域變數，執行緒安全
        SysUser currentUser = userMapper.selectUserById(userId);
        doSomething(currentUser);
    }
}
```

### 執行緒安全的成員變數

✅ **可以使用成員變數的情況**:
```java
@Service
public class UserService {
    // ✅ 無狀態的依賴：執行緒安全
    @Autowired
    private UserMapper userMapper;
    
    // ✅ 不可變的常數：執行緒安全
    private static final int MAX_RETRY = 3;
    
    // ✅ ThreadLocal：每個執行緒有自己的副本
    private ThreadLocal<SysUser> currentUser = new ThreadLocal<>();
}
```

---

## 觀察者模式 (Observer Pattern)

### 使用場景
事件驅動、解耦業務邏輯

### 範例: Spring Event

```java
// 1. 定義事件
public class UserRegisteredEvent extends ApplicationEvent {
    private final SysUser user;
    
    public UserRegisteredEvent(Object source, SysUser user) {
        super(source);
        this.user = user;
    }
    
    public SysUser getUser() {
        return user;
    }
}

// 2. 發布事件
@Service
public class UserService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public int registerUser(SysUser user) {
        // 註冊用戶
        int result = userMapper.insertUser(user);
        
        // 發布事件
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user));
        
        return result;
    }
}

// 3. 監聽事件
@Component
public class UserEventListener {
    @Autowired
    private EmailService emailService;
    
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        SysUser user = event.getUser();
        // 發送歡迎郵件
        emailService.sendWelcomeEmail(user.getEmail());
    }
}
```

### 優勢
- ✅ 解耦業務邏輯
- ✅ 易於新增新的監聽器
- ✅ 非同步處理（可配合 `@Async`）

---

## 責任鏈模式 (Chain of Responsibility)

### 使用場景
多個處理器依序處理請求

### 範例: 攔截器鏈

```java
// Spring MVC 的 Interceptor 就是責任鏈模式
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        // 認證邏輯
        if (!isAuthenticated(request)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;  // 中斷鏈
        }
        return true;  // 繼續下一個攔截器
    }
}

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        // 權限檢查邏輯
        if (!hasPermission(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }
}
```

---

## 適配器模式 (Adapter Pattern)

### 使用場景
整合不同介面

### 範例: LINE Bot SDK 適配

```java
// LINE SDK 的介面
public interface LineBotClient {
    BotApiResponse pushMessage(PushMessage pushMessage);
}

// 我們的統一介面
public interface MessageSender {
    void send(String to, String content);
}

// 適配器
@Service
public class LineBotAdapter implements MessageSender {
    @Autowired
    private LineBotClient lineBotClient;
    
    @Override
    public void send(String to, String content) {
        // 將我們的介面適配到 LINE SDK
        TextMessage textMessage = new TextMessage(content);
        PushMessage pushMessage = new PushMessage(to, textMessage);
        lineBotClient.pushMessage(pushMessage);
    }
}
```

---

## 設計模式選擇指南

| 場景 | 推薦模式 |
|------|---------|
| 不同類型的業務邏輯處理 | 策略模式 |
| 統一流程，部分步驟可變 | 模板方法模式 |
| 建構複雜物件 | 建造者模式 |
| 建立不同類型物件 | 工廠模式 |
| 事件驅動、解耦業務 | 觀察者模式 |
| 多個處理器依序處理 | 責任鏈模式 |
| 整合不同介面 | 適配器模式 |

---

**下一步**: 閱讀 [RULE_04_PERFORMANCE.md](./RULE_04_PERFORMANCE.md) 學習效能優化
