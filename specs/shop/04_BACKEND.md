# 04. 後端架構設計（策略模式）

> **核心原則**：避免 if-else 堆疊，使用策略模式實現可擴充設計

---

## 4.1 策略模式概述

策略模式將演算法封裝在獨立的類別中，使它們可以互換。適用於：

- **支付方式**：LINE Pay、信用卡、ATM 等
- **物流方式**：宅配、超商取貨等
- **折扣計算**：滿額折、百分比折等
- **第三方登入**：LINE、Google 等

---

## 4.2 支付策略

### 4.2.1 策略介面

```java
public interface PaymentStrategy {
    
    /** 取得支付方式 */
    PaymentMethod getPaymentMethod();

    /** 建立支付請求 */
    PaymentRequest createPayment(ShopOrder order);

    /** 處理支付回調 */
    PaymentResult handleCallback(Map<String, String> params);

    /** 查詢支付狀態 */
    PaymentStatus queryStatus(String paymentNo);

    /** 申請退款 */
    RefundResult refund(ShopOrder order, BigDecimal amount);
}
```

### 4.2.2 LINE Pay 實現

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class LinePayStrategy implements PaymentStrategy {
    
    private final LinePayConfig linePayConfig;
    
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.LINE_PAY;
    }
    
    @Override
    public PaymentRequest createPayment(ShopOrder order) {
        // 呼叫 LINE Pay API 建立支付
        // 返回支付頁面 URL
    }
    
    @Override
    public PaymentResult handleCallback(Map<String, String> params) {
        // 處理 LINE Pay 回調
        // 驗證簽名、更新訂單狀態
    }
    
    @Override
    public PaymentStatus queryStatus(String paymentNo) {
        // 查詢 LINE Pay 訂單狀態
    }
    
    @Override
    public RefundResult refund(ShopOrder order, BigDecimal amount) {
        // 呼叫 LINE Pay 退款 API
    }
}
```

### 4.2.3 策略工廠

```java
@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {
    
    private final Map<PaymentMethod, PaymentStrategy> strategyMap;

    @Autowired
    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(
                PaymentStrategy::getPaymentMethod,
                Function.identity()
            ));
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategyMap.get(method);
        if (strategy == null) {
            throw new ServiceException("不支援的付款方式: " + method.getDescription());
        }
        return strategy;
    }
}
```

### 4.2.4 使用方式

```java
@Service
@RequiredArgsConstructor
public class ShopOrderServiceImpl implements IShopOrderService {
    
    private final PaymentStrategyFactory paymentStrategyFactory;
    
    @Override
    public PaymentRequest createPayment(Long orderId, PaymentMethod method) {
        ShopOrder order = getOrderById(orderId);
        
        // 透過工廠取得對應策略，不需要 if-else
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);
        return strategy.createPayment(order);
    }
}
```

---

## 4.3 物流策略

### 4.3.1 策略介面

```java
public interface ShippingStrategy {
    
    /** 取得物流方式 */
    ShippingMethod getShippingMethod();
    
    /** 計算運費 */
    BigDecimal calculateFee(ShopOrder order);
    
    /** 建立物流單 */
    ShipmentResult createShipment(ShopOrder order);
    
    /** 查詢物流狀態 */
    TrackingInfo queryTracking(String trackingNo);
}
```

### 4.3.2 宅配實現

```java
@Component
@RequiredArgsConstructor
public class HomeDeliveryStrategy implements ShippingStrategy {
    
    @Override
    public ShippingMethod getShippingMethod() {
        return ShippingMethod.HOME_DELIVERY;
    }
    
    @Override
    public BigDecimal calculateFee(ShopOrder order) {
        // 滿額免運，否則固定運費
        if (order.getProductAmount().compareTo(new BigDecimal("1000")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("100");
    }
    
    @Override
    public ShipmentResult createShipment(ShopOrder order) {
        // 呼叫物流 API 建立託運單
    }
    
    @Override
    public TrackingInfo queryTracking(String trackingNo) {
        // 查詢物流狀態
    }
}
```

### 4.3.3 超商取貨實現

```java
@Component
@RequiredArgsConstructor
public class CvsPickupStrategy implements ShippingStrategy {
    
    private final ShippingMethod method;
    
    @Override
    public ShippingMethod getShippingMethod() {
        return method;
    }
    
    @Override
    public BigDecimal calculateFee(ShopOrder order) {
        // 超商取貨固定運費
        return new BigDecimal("60");
    }
    
    // ...
}
```

---

## 4.4 折扣策略

### 4.4.1 策略介面

```java
public interface DiscountStrategy {
    
    /** 取得折扣類型 */
    String getDiscountType();
    
    /** 檢查是否適用 */
    boolean isApplicable(ShopOrder order, DiscountRule rule);
    
    /** 計算折扣金額 */
    BigDecimal calculate(ShopOrder order, DiscountRule rule);
}
```

### 4.4.2 滿額折扣

```java
@Component
public class AmountDiscountStrategy implements DiscountStrategy {
    
    @Override
    public String getDiscountType() {
        return "AMOUNT_OFF";
    }
    
    @Override
    public boolean isApplicable(ShopOrder order, DiscountRule rule) {
        // 檢查訂單金額是否達到門檻
        return order.getProductAmount().compareTo(rule.getThreshold()) >= 0;
    }
    
    @Override
    public BigDecimal calculate(ShopOrder order, DiscountRule rule) {
        // 返回固定折扣金額
        return rule.getDiscountAmount();
    }
}
```

### 4.4.3 百分比折扣

```java
@Component
public class PercentDiscountStrategy implements DiscountStrategy {
    
    @Override
    public String getDiscountType() {
        return "PERCENT_OFF";
    }
    
    @Override
    public boolean isApplicable(ShopOrder order, DiscountRule rule) {
        return order.getProductAmount().compareTo(rule.getThreshold()) >= 0;
    }
    
    @Override
    public BigDecimal calculate(ShopOrder order, DiscountRule rule) {
        // 計算百分比折扣
        BigDecimal percent = rule.getDiscountPercent();
        return order.getProductAmount()
            .multiply(percent)
            .divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
    }
}
```

---

## 4.5 第三方登入策略

### 4.5.1 策略介面

```java
public interface SocialAuthStrategy {
    
    /** 取得平台 */
    SocialProvider getProvider();
    
    /** 建構授權 URL */
    String buildAuthUrl(String redirectUri, String state);
    
    /** 認證（用 code 換取用戶資訊） */
    SocialUserInfo authenticate(String code);
}
```

### 4.5.2 LINE 登入

```java
@Component
@RequiredArgsConstructor
public class LineAuthStrategy implements SocialAuthStrategy {
    
    private final LineLoginConfig config;
    
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.LINE;
    }
    
    @Override
    public String buildAuthUrl(String redirectUri, String state) {
        return String.format(
            "https://access.line.me/oauth2/v2.1/authorize?" +
            "response_type=code&client_id=%s&redirect_uri=%s&state=%s&scope=profile%%20openid",
            config.getClientId(),
            URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
            state
        );
    }
    
    @Override
    public SocialUserInfo authenticate(String code) {
        // 1. 用 code 換取 access_token
        // 2. 用 access_token 取得用戶資料
        // 3. 返回標準化的 SocialUserInfo
    }
}
```

### 4.5.3 Google 登入

```java
@Component
@RequiredArgsConstructor
public class GoogleAuthStrategy implements SocialAuthStrategy {
    
    private final GoogleLoginConfig config;
    
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }
    
    @Override
    public String buildAuthUrl(String redirectUri, String state) {
        return String.format(
            "https://accounts.google.com/o/oauth2/v2/auth?" +
            "client_id=%s&redirect_uri=%s&response_type=code&scope=openid%%20profile%%20email&state=%s",
            config.getClientId(),
            URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
            state
        );
    }
    
    @Override
    public SocialUserInfo authenticate(String code) {
        // 實現 Google OAuth 流程
    }
}
```

---

## 4.6 服務介面設計

### 4.6.1 商品服務

```java
public interface IShopProductService {
    
    // === 前台 API ===
    PageInfo<ProductListVO> listProducts(ProductQueryDTO query);
    ProductDetailVO getProductDetail(Long productId);
    List<ProductListVO> getHotProducts(int limit);
    List<ProductListVO> getNewProducts(int limit);
    List<ProductListVO> getRecommendProducts(int limit);

    // === 後台 API ===
    PageInfo<ShopProduct> adminListProducts(ProductAdminQueryDTO query);
    int createProduct(ProductCreateDTO dto);
    int updateProduct(ProductUpdateDTO dto);
    int updateProductStatus(Long productId, ProductStatus status);
    int deleteProduct(Long productId);
}
```

### 4.6.2 訂單服務

```java
public interface IShopOrderService {
    
    // === 前台 API ===
    OrderCreateResult createOrder(OrderCreateDTO dto);
    PageInfo<OrderListVO> listMemberOrders(Long memberId, OrderQueryDTO query);
    OrderDetailVO getOrderDetail(Long memberId, Long orderId);
    int cancelOrder(Long memberId, Long orderId, String reason);

    // === 後台 API ===
    PageInfo<ShopOrder> adminListOrders(OrderAdminQueryDTO query);
    int updateOrderStatus(Long orderId, OrderStatus status);
    int shipOrder(Long orderId, ShipOrderDTO dto);
}
```

### 4.6.3 會員服務

```java
public interface IShopMemberService {
    
    // === 認證 ===
    MemberLoginResult login(MemberLoginDTO dto);
    MemberLoginResult socialLogin(SocialProvider provider, String code);
    int register(MemberRegisterDTO dto);
    int logout(Long memberId);

    // === 會員資料 ===
    MemberInfoVO getMemberInfo(Long memberId);
    int updateMemberInfo(Long memberId, MemberUpdateDTO dto);
    int changePassword(Long memberId, ChangePasswordDTO dto);

    // === 後台 API ===
    PageInfo<ShopMember> adminListMembers(MemberAdminQueryDTO query);
    int updateMemberStatus(Long memberId, MemberStatus status);
}
```

### 4.6.4 內容服務

```java
public interface IShopContentService {
    
    // === 輪播 ===
    List<BannerVO> getActiveBanners(String position);
    int createBanner(BannerCreateDTO dto);
    int updateBanner(BannerUpdateDTO dto);
    int deleteBanner(Long bannerId);

    // === 頁面區塊 ===
    List<PageBlockVO> getPageBlocks(String pageKey);
    int savePageBlock(PageBlockSaveDTO dto);
}
```

### 4.6.5 購物車服務

```java
public interface IShopCartService {
    
    List<CartItemVO> getCartItems(Long memberId);
    int addToCart(Long memberId, Long skuId, int quantity);
    int updateCartQuantity(Long memberId, Long cartId, int quantity);
    int removeFromCart(Long memberId, Long cartId);
    int clearCart(Long memberId);
    int selectCartItems(Long memberId, List<Long> cartIds, boolean selected);
}
```

---

## 4.7 策略模式優點

| 優點 | 說明 |
|-----|-----|
| **消除 if-else** | 新增支付方式只需新增類別，無需修改現有程式碼 |
| **開放封閉原則** | 對擴展開放，對修改封閉 |
| **單一職責** | 每個策略類別只負責一種處理方式 |
| **易於測試** | 每個策略可獨立測試 |
| **易於維護** | 修改單一策略不影響其他策略 |

### 新增支付方式步驟

1. 建立新的 `XxxPayStrategy` 類別
2. 實現 `PaymentStrategy` 介面
3. 加上 `@Component` 註解
4. 完成！工廠會自動註冊
