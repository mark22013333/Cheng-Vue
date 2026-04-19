# 03. Enum 狀態設計

> **核心原則**：所有狀態必須定義 Enum，禁止魔術數字

---

## 3.1 商品狀態 (ProductStatus)

```java
@Getter
@RequiredArgsConstructor
public enum ProductStatus implements CodedEnum<String> {
    DRAFT("DRAFT", "草稿"),
    PREVIEW("PREVIEW", "預覽"),
    ON_SALE("ON_SALE", "上架中"),
    OFF_SALE("OFF_SALE", "已下架");

    private final String code;
    private final String description;

    public boolean canSell() {
        return this == ON_SALE;
    }

    public boolean canEdit() {
        return this == DRAFT || this == OFF_SALE;
    }

    public String getColor() {
        return switch (this) {
            case DRAFT -> "#909399";
            case PREVIEW -> "#E6A23C";
            case ON_SALE -> "#67C23A";
            case OFF_SALE -> "#F56C6C";
        };
    }

    public static ProductStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ProductStatus.class, code);
    }
}
```

---

## 3.2 訂單狀態 (OrderStatus)

```java
@Getter
@RequiredArgsConstructor
public enum OrderStatus implements CodedEnum<String> {
    PENDING("PENDING", "待付款"),
    PAID("PAID", "已付款"),
    PROCESSING("PROCESSING", "處理中"),
    SHIPPED("SHIPPED", "已出貨"),
    DELIVERED("DELIVERED", "已送達"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUNDING("REFUNDING", "退款中"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String description;

    /** 取得可流轉的下一個狀態 */
    public List<OrderStatus> getNextStatuses() {
        return switch (this) {
            case PENDING -> List.of(PAID, CANCELLED);
            case PAID -> List.of(PROCESSING, REFUNDING);
            case PROCESSING -> List.of(SHIPPED, REFUNDING);
            case SHIPPED -> List.of(DELIVERED);
            case DELIVERED -> List.of(COMPLETED, REFUNDING);
            case REFUNDING -> List.of(REFUNDED);
            default -> List.of();
        };
    }

    public boolean canCancel() { return this == PENDING; }
    public boolean canRefund() { return this == PAID || this == PROCESSING || this == DELIVERED; }
    public boolean isFinal() { return this == COMPLETED || this == CANCELLED || this == REFUNDED; }

    public String getColor() {
        return switch (this) {
            case PENDING -> "#E6A23C";
            case PAID, PROCESSING -> "#409EFF";
            case SHIPPED, DELIVERED, COMPLETED -> "#67C23A";
            case CANCELLED -> "#909399";
            case REFUNDING, REFUNDED -> "#F56C6C";
        };
    }

    public static OrderStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(OrderStatus.class, code);
    }
}
```

---

## 3.3 付款狀態 (PayStatus)

```java
@Getter
@RequiredArgsConstructor
public enum PayStatus implements CodedEnum<String> {
    UNPAID("UNPAID", "未付款"),
    PAYING("PAYING", "付款中"),
    PAID("PAID", "已付款"),
    REFUNDING("REFUNDING", "退款中"),
    REFUNDED("REFUNDED", "已退款"),
    FAILED("FAILED", "付款失敗");

    private final String code;
    private final String description;

    public static PayStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(PayStatus.class, code);
    }
}
```

---

## 3.4 物流狀態 (ShipStatus)

```java
@Getter
@RequiredArgsConstructor
public enum ShipStatus implements CodedEnum<String> {
    UNSHIPPED("UNSHIPPED", "未出貨"),
    PREPARING("PREPARING", "備貨中"),
    SHIPPED("SHIPPED", "已出貨"),
    IN_TRANSIT("IN_TRANSIT", "運送中"),
    DELIVERED("DELIVERED", "已送達"),
    RETURNED("RETURNED", "已退回");

    private final String code;
    private final String description;

    public static ShipStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ShipStatus.class, code);
    }
}
```

---

## 3.5 會員狀態 (MemberStatus)

```java
@Getter
@RequiredArgsConstructor
public enum MemberStatus implements CodedEnum<String> {
    ACTIVE("ACTIVE", "正常"),
    DISABLED("DISABLED", "停用"),
    FROZEN("FROZEN", "凍結");

    private final String code;
    private final String description;

    public boolean canLogin() { return this == ACTIVE; }

    public static MemberStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(MemberStatus.class, code);
    }
}
```

---

## 3.6 第三方登入平台 (SocialProvider)

```java
@Getter
@RequiredArgsConstructor
public enum SocialProvider implements CodedEnum<String> {
    LINE("LINE", "LINE"),
    GOOGLE("GOOGLE", "Google"),
    FACEBOOK("FACEBOOK", "Facebook");

    private final String code;
    private final String description;

    public static SocialProvider fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(SocialProvider.class, code);
    }
}
```

---

## 3.7 輪播連結類型 (BannerLinkType)

```java
@Getter
@RequiredArgsConstructor
public enum BannerLinkType implements CodedEnum<String> {
    NONE("NONE", "無連結"),
    PRODUCT("PRODUCT", "商品詳情"),
    CATEGORY("CATEGORY", "商品分類"),
    URL("URL", "外部連結");

    private final String code;
    private final String description;

    public static BannerLinkType fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(BannerLinkType.class, code);
    }
}
```

---

## 3.8 區塊類型 (BlockType)

```java
@Getter
@RequiredArgsConstructor
public enum BlockType implements CodedEnum<String> {
    TEXT("TEXT", "純文字"),
    IMAGE("IMAGE", "圖片"),
    HTML("HTML", "富文本"),
    PRODUCT_LIST("PRODUCT_LIST", "商品列表"),
    CATEGORY_LIST("CATEGORY_LIST", "分類列表");

    private final String code;
    private final String description;

    public static BlockType fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(BlockType.class, code);
    }
}
```

---

## 3.9 付款方式 (PaymentMethod)

```java
@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements CodedEnum<String> {
    LINE_PAY("LINE_PAY", "LINE Pay"),
    CREDIT_CARD("CREDIT_CARD", "信用卡"),
    ATM("ATM", "ATM轉帳"),
    CVS("CVS", "超商付款"),
    COD("COD", "貨到付款");

    private final String code;
    private final String description;

    public static PaymentMethod fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(PaymentMethod.class, code);
    }
}
```

---

## 3.10 物流方式 (ShippingMethod)

```java
@Getter
@RequiredArgsConstructor
public enum ShippingMethod implements CodedEnum<String> {
    HOME_DELIVERY("HOME_DELIVERY", "宅配到府"),
    CVS_711("CVS_711", "7-11 超取"),
    CVS_FAMILY("CVS_FAMILY", "全家超取"),
    CVS_HILIFE("CVS_HILIFE", "萊爾富超取"),
    STORE_PICKUP("STORE_PICKUP", "門市自取");

    private final String code;
    private final String description;

    public boolean isCvs() {
        return this == CVS_711 || this == CVS_FAMILY || this == CVS_HILIFE;
    }

    public static ShippingMethod fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ShippingMethod.class, code);
    }
}
```

---

## 3.11 通用狀態 (CommonStatus)

```java
@Getter
@RequiredArgsConstructor
public enum CommonStatus implements CodedEnum<String> {
    ENABLED("ENABLED", "啟用"),
    DISABLED("DISABLED", "停用");

    private final String code;
    private final String description;

    public boolean isEnabled() { return this == ENABLED; }

    public static CommonStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(CommonStatus.class, code);
    }
}
```

---

## 3.12 Enum 使用規範

### ✅ 正確做法

```java
// 使用 Enum 比較
if (order.getStatusEnum() == OrderStatus.PENDING) {
    // 處理待付款訂單
}

// 使用 Enum 方法
if (order.getStatusEnum().canCancel()) {
    // 可以取消
}

// 取得描述和顏色
String desc = status.getDescription();
String color = status.getColor();
```

### ❌ 禁止做法

```java
// 禁止：使用魔術字串
if (order.getStatus().equals("PENDING")) { ... }

// 禁止：在 Service 中定義狀態映射
private String getStatusDesc(String status) {
    return switch (status) {
        case "PENDING" -> "待付款";
        // ...
    };
}
```
