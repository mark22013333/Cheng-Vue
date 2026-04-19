# NoticeType 和 NoticeStatus Enum 使用範例

## 為什麼需要 Enum？

根據專案規範，**禁止使用魔術字串**，必須使用 Enum 提升程式碼可讀性和類型安全。

```java
// ❌ 錯誤：使用魔術字串
if (notice.getNoticeType().equals("1")) { ... }
if (notice.getStatus().equals("0")) { ... }

// ✅ 正確：使用 Enum
if (notice.getNoticeTypeEnum() == NoticeType.NOTIFICATION) { ... }
if (notice.getStatusEnum() == NoticeStatus.NORMAL) { ... }
```

---

## NoticeType - 公告類型

### Enum 定義

```java
public enum NoticeType implements CodedEnum<String> {
    NOTIFICATION("1", "通知"),  // 強制閱讀的通知
    ANNOUNCEMENT("2", "公告");  // 首頁展示的公告

    private final String code;
    private final String description;
}
```

### 使用範例

```java
// 判斷公告類型
if (notice.getNoticeTypeEnum() == NoticeType.NOTIFICATION) {
    // 這是需要強制閱讀的通知
    sendToUserNotificationCenter(notice);
} else if (notice.getNoticeTypeEnum() == NoticeType.ANNOUNCEMENT) {
    // 這是首頁顯示的公告
    addToHomepageCarousel(notice);
}

// 建立新公告時設定類型
SysNotice notice = new SysNotice();
notice.setNoticeTypeEnum(NoticeType.ANNOUNCEMENT);
notice.setNoticeTitle("新功能上線");

// 過濾特定類型的公告
List<SysNotice> notifications = allNotices.stream()
    .filter(n -> n.getNoticeTypeEnum() == NoticeType.NOTIFICATION)
    .collect(Collectors.toList());
```

---

## NoticeStatus - 公告狀態

### Enum 定義

```java
public enum NoticeStatus implements CodedEnum<String> {
    NORMAL("0", "正常"),  // 啟用狀態，會顯示給使用者
    CLOSED("1", "關閉");  // 停用狀態，不會顯示

    private final String code;
    private final String description;
}
```

### 使用範例

```java
// 判斷公告是否正常
if (notice.getStatusEnum() == NoticeStatus.NORMAL) {
    // 公告啟用中，可以顯示
    displayNotice(notice);
}

// 關閉公告
notice.setStatusEnum(NoticeStatus.CLOSED);
noticeService.updateNotice(notice);

// 過濾正常狀態的公告
List<SysNotice> activeNotices = allNotices.stream()
    .filter(n -> n.getStatusEnum() == NoticeStatus.NORMAL)
    .collect(Collectors.toList());
```

---

## 完整業務邏輯範例

```java
@Service
public class NoticeBusinessService {
    
    @Autowired
    private ISysNoticeService noticeService;
    
    /**
     * 發布系統通知（強制閱讀）
     */
    public void publishNotification(String title, String content) {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setNoticeTypeEnum(NoticeType.NOTIFICATION);  // ✅ 使用 Enum
        notice.setStatusEnum(NoticeStatus.NORMAL);          // ✅ 使用 Enum
        noticeService.insertNotice(notice);
    }
    
    /**
     * 發布首頁公告
     */
    public void publishAnnouncement(String title, String content) {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setNoticeTypeEnum(NoticeType.ANNOUNCEMENT);  // ✅ 使用 Enum
        notice.setStatusEnum(NoticeStatus.NORMAL);          // ✅ 使用 Enum
        noticeService.insertNotice(notice);
    }
    
    /**
     * 檢查並處理公告
     */
    public void processNotice(SysNotice notice) {
        // ✅ 使用 Enum 進行類型安全的判斷
        NoticeType type = notice.getNoticeTypeEnum();
        NoticeStatus status = notice.getStatusEnum();
        
        if (status != NoticeStatus.NORMAL) {
            log.info("公告已關閉，不進行處理");
            return;
        }
        
        switch (type) {
            case NOTIFICATION:
                // 處理通知：發送到使用者通知中心
                handleNotification(notice);
                break;
            case ANNOUNCEMENT:
                // 處理公告：加入首頁輪播
                handleAnnouncement(notice);
                break;
            default:
                log.warn("未知的公告類型");
        }
    }
    
    /**
     * 統計各類型公告數量
     */
    public Map<NoticeType, Long> countByType(List<SysNotice> notices) {
        return notices.stream()
            .filter(n -> n.getStatusEnum() == NoticeStatus.NORMAL)  // ✅ 使用 Enum
            .collect(Collectors.groupingBy(
                SysNotice::getNoticeTypeEnum,  // ✅ 使用 Enum
                Collectors.counting()
            ));
    }
}
```

---

## Domain 層實現細節

`SysNotice.java` 提供了 Enum 的 getter/setter 方法：

```java
@Setter
public class SysNotice extends BaseEntity {
    
    // 資料庫欄位（MyBatis 映射用）
    @Getter
    private String noticeType;  // "1" 或 "2"
    
    @Getter
    private String status;      // "0" 或 "1"
    
    // ✅ Enum getter/setter（業務邏輯使用）
    public NoticeType getNoticeTypeEnum() {
        return NoticeType.fromCode(noticeType);
    }
    
    public void setNoticeTypeEnum(NoticeType noticeTypeEnum) {
        this.noticeType = noticeTypeEnum != null ? noticeTypeEnum.getCode() : null;
    }
    
    public NoticeStatus getStatusEnum() {
        return NoticeStatus.fromCode(status);
    }
    
    public void setStatusEnum(NoticeStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }
}
```

---

## MyBatis SQL 中的註解

在 XML 中使用字串是不可避免的，但應該加上註解說明：

```xml
<!-- ✅ 好的做法：加上 Enum 註解 -->
<!-- 查詢首頁公告列表：NoticeType.ANNOUNCEMENT='2', NoticeStatus.NORMAL='0' -->
<select id="selectAnnouncementList" resultMap="SysNoticeResult">
    SELECT * FROM sys_notice
    WHERE notice_type = '2'
      AND status = '0'
</select>

<!-- ❌ 不好的做法：沒有說明 -->
<select id="selectAnnouncementList" resultMap="SysNoticeResult">
    SELECT * FROM sys_notice
    WHERE notice_type = '2'
      AND status = '0'
</select>
```

---

## 總結

1. **Domain 層**：保留 String 類型欄位（MyBatis 映射），提供 Enum getter/setter
2. **Service 層**：使用 Enum 進行業務邏輯判斷
3. **Mapper XML**：使用字串但加上 Enum 註解
4. **Controller 層**：接收前端字串參數，轉換為 Enum 後傳給 Service

這樣既保持了 MyBatis 的簡單性，又實現了類型安全和可讀性。
