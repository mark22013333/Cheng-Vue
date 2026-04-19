# 常見陷阱與注意事項

> **目的**: 避免常見錯誤，提升程式碼品質

---

## MyBatis 陷阱

### 1. 動態 SQL 字串比較錯誤

❌ **錯誤**:
```xml
<if test="status == '0'">
    AND status = #{status}
</if>
```

**問題**: MyBatis 中 `==` 比較的是物件，不是字串值。

✅ **正確**:
```xml
<if test="status != null and status == '0'.toString()">
    AND status = #{status}
</if>

<!-- 或使用 equals -->
<if test="status != null and '0'.equals(status)">
    AND status = #{status}
</if>
```

### 2. foreach 集合為空的陷阱

❌ **錯誤**:
```xml
<select id="selectByIds">
    SELECT * FROM sys_user
    WHERE user_id IN
    <foreach collection="ids" item="id" open="(" close=")" separator=",">
        #{id}
    </foreach>
</select>
```

**問題**: 如果 `ids` 為空，會產生 `WHERE user_id IN ()` 的錯誤 SQL。

✅ **正確**:
```xml
<select id="selectByIds">
    SELECT * FROM sys_user
    <where>
        <if test="ids != null and ids.size() > 0">
            AND user_id IN
            <foreach collection="ids" item="id" open="(" close=")" separator=",">
                #{id}
            </foreach>
        </if>
    </where>
</select>
```

### 3. #{}  vs ${}

❌ **危險**:
```xml
<!-- SQL 注入風險！ -->
<select id="selectByCondition">
    SELECT * FROM sys_user WHERE user_name = ${userName}
</select>
```

✅ **正確**:
```xml
<!-- 參數化查詢，防止 SQL 注入 -->
<select id="selectByCondition">
    SELECT * FROM sys_user WHERE user_name = #{userName}
</select>

<!-- 只在動態表名或欄位名時使用 ${}（且要驗證輸入） -->
<select id="selectByTable">
    SELECT * FROM ${tableName}  <!-- 必須驗證 tableName 的合法性 -->
    WHERE id = #{id}
</select>
```

---

## 日期比較陷阱

### 1. 不要用字串比較日期

❌ **錯誤**:
```java
// 錯誤：字串比較
if (date1 > date2) { ... }  // 編譯錯誤

String dateStr1 = "2025-01-01";
String dateStr2 = "2025-12-31";
if (dateStr1.compareTo(dateStr2) > 0) { ... }  // 可能錯誤
```

✅ **正確**:
```java
// 使用 Date.compareTo()
Date date1 = ...;
Date date2 = ...;
if (date1.after(date2)) { ... }
if (date1.before(date2)) { ... }
if (date1.compareTo(date2) > 0) { ... }

// 或使用 LocalDateTime
LocalDateTime dateTime1 = ...;
LocalDateTime dateTime2 = ...;
if (dateTime1.isAfter(dateTime2)) { ... }
if (dateTime1.isBefore(dateTime2)) { ... }
```

### 2. 時區問題

```java
// 注意時區
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
private Date createTime;

// LocalDateTime 沒有時區資訊
LocalDateTime now = LocalDateTime.now();  // 系統時區

// ZonedDateTime 有時區資訊
ZonedDateTime nowInTaipei = ZonedDateTime.now(ZoneId.of("Asia/Taipei"));
```

---

## List 判空陷阱

### 1. 不要只判斷 null

❌ **錯誤**:
```java
List<SysUser> list = userMapper.selectUserList(user);
if (list != null) {
    // 可能是空 List！
    SysUser first = list.get(0);  // IndexOutOfBoundsException
}
```

✅ **正確**:
```java
List<SysUser> list = userMapper.selectUserList(user);
if (StringUtils.isNotEmpty(list)) {
    SysUser first = list.get(0);
}

// 或
if (list != null && !list.isEmpty()) {
    SysUser first = list.get(0);
}
```

---

## Long 比較陷阱

### 1. 不要用 == 比較 Long

❌ **錯誤**:
```java
Long userId1 = 1000L;
Long userId2 = 1000L;
if (userId1 == userId2) {  // 可能返回 false！
    // ...
}
```

**原因**: Java 會快取 -128 到 127 的 Long，超出範圍時 `==` 比較的是物件參考。

✅ **正確**:
```java
Long userId1 = 1000L;
Long userId2 = 1000L;
if (userId1.equals(userId2)) {  // 正確
    // ...
}

// 或先判空
if (Objects.equals(userId1, userId2)) {  // 推薦，自動處理 null
    // ...
}
```

---

## BigDecimal 陷阱

### 1. 不要用 double 建構 BigDecimal

❌ **錯誤**:
```java
BigDecimal amount = new BigDecimal(0.1);  // 精度問題！
System.out.println(amount);  // 0.1000000000000000055511151231257827021181583404541015625
```

✅ **正確**:
```java
BigDecimal amount = new BigDecimal("0.1");  // 使用字串
// 或
BigDecimal amount = BigDecimal.valueOf(0.1);
```

### 2. 不要用 == 比較 BigDecimal

❌ **錯誤**:
```java
BigDecimal amount1 = new BigDecimal("10.0");
BigDecimal amount2 = new BigDecimal("10.00");
if (amount1 == amount2) { ... }  // false
if (amount1.equals(amount2)) { ... }  // false（scale 不同）
```

✅ **正確**:
```java
if (amount1.compareTo(amount2) == 0) { ... }  // true
```

### 3. 除法要設定精度

❌ **錯誤**:
```java
BigDecimal result = amount1.divide(amount2);  // 可能拋出 ArithmeticException
```

✅ **正確**:
```java
BigDecimal result = amount1.divide(amount2, 2, RoundingMode.HALF_UP);
// 保留 2 位小數，四捨五入
```

---

## 執行緒安全陷阱

### 1. Spring Bean 成員變數

❌ **危險**:
```java
@Service
public class UserService {
    // 危險！多執行緒會互相覆蓋
    private SysUser currentUser;
    
    public void processUser(Long userId) {
        currentUser = userMapper.selectUserById(userId);
        // 執行緒 A 執行到這裡時，執行緒 B 可能已經改變了 currentUser
        doSomething(currentUser);  // 可能使用錯誤的資料
    }
}
```

✅ **正確**:
```java
@Service
public class UserService {
    // 使用區域變數
    public void processUser(Long userId) {
        SysUser currentUser = userMapper.selectUserById(userId);
        doSomething(currentUser);
    }
    
    // 或使用 ThreadLocal
    private ThreadLocal<SysUser> currentUser = new ThreadLocal<>();
    
    public void processUser(Long userId) {
        try {
            currentUser.set(userMapper.selectUserById(userId));
            doSomething(currentUser.get());
        } finally {
            currentUser.remove();  // 記得清理
        }
    }
}
```

### 2. SimpleDateFormat 不是執行緒安全

❌ **危險**:
```java
@Service
public class DateService {
    // 不是執行緒安全！
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    public String format(Date date) {
        return sdf.format(date);  // 多執行緒會出錯
    }
}
```

✅ **正確**:
```java
@Service
public class DateService {
    // 方式 1：使用區域變數
    public String format(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
    // 方式 2：使用 DateTimeFormatter（執行緒安全）
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public String format(LocalDate date) {
        return date.format(formatter);
    }
}
```

---

## 空指標陷阱

### 1. 自動拆箱 NPE

❌ **危險**:
```java
public int calculateTotal(Integer count, Integer price) {
    return count * price;  // 如果 count 或 price 為 null，NPE！
}
```

✅ **正確**:
```java
public int calculateTotal(Integer count, Integer price) {
    if (count == null || price == null) {
        return 0;
    }
    return count * price;
}

// 或使用 Optional
public int calculateTotal(Integer count, Integer price) {
    return Optional.ofNullable(count).orElse(0) * 
           Optional.ofNullable(price).orElse(0);
}
```

### 2. 鏈式呼叫 NPE

❌ **危險**:
```java
String deptName = user.getDept().getDeptName();  // NPE 如果 dept 為 null
```

✅ **正確**:
```java
String deptName = null;
if (user != null && user.getDept() != null) {
    deptName = user.getDept().getDeptName();
}

// 或使用 Optional
String deptName = Optional.ofNullable(user)
    .map(SysUser::getDept)
    .map(SysDept::getDeptName)
    .orElse(null);
```

---

## 集合操作陷阱

### 1. Arrays.asList() 返回固定大小 List

❌ **錯誤**:
```java
List<String> list = Arrays.asList("a", "b", "c");
list.add("d");  // UnsupportedOperationException
list.remove(0);  // UnsupportedOperationException
```

✅ **正確**:
```java
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
list.add("d");  // OK
```

### 2. 遍歷時修改集合

❌ **錯誤**:
```java
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
for (String item : list) {
    if ("b".equals(item)) {
        list.remove(item);  // ConcurrentModificationException
    }
}
```

✅ **正確**:
```java
// 方式 1：使用 Iterator
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String item = it.next();
    if ("b".equals(item)) {
        it.remove();
    }
}

// 方式 2：使用 removeIf（Java 8+）
list.removeIf(item -> "b".equals(item));

// 方式 3：建立新 List
List<String> newList = list.stream()
    .filter(item -> !"b".equals(item))
    .collect(Collectors.toList());
```

---

## JSON 序列化陷阱

### 1. Date 序列化為時間戳

```java
@Data
public class SysUser {
    // 沒有 @JsonFormat，會序列化為時間戳
    private Date createTime;  // 1641024000000
}
```

✅ **正確**:
```java
@Data
public class SysUser {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date createTime;  // "2022-01-01 12:00:00"
}
```

### 2. 循環參考

❌ **錯誤**:
```java
@Data
public class SysUser {
    private SysDept dept;
}

@Data
public class SysDept {
    private List<SysUser> users;  // 循環參考
}
```

**問題**: 序列化時會無限遞迴。

✅ **正確**:
```java
@Data
public class SysDept {
    @JsonIgnore  // 忽略此欄位
    private List<SysUser> users;
}

// 或使用 VO 避免循環參考
```

---

## 事務陷阱

### 1. @Transactional 只對 public 有效

❌ **無效**:
```java
@Service
public class UserService {
    @Transactional
    private void updateUser(SysUser user) {
        // 事務不會生效！
    }
}
```

✅ **正確**:
```java
@Service
public class UserService {
    @Transactional
    public void updateUser(SysUser user) {
        // 事務生效
    }
}
```

### 2. 同類別內部呼叫事務不生效

❌ **無效**:
```java
@Service
public class UserService {
    public void methodA() {
        this.methodB();  // 事務不會生效
    }
    
    @Transactional
    public void methodB() {
        // 事務不會生效
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
        self.methodB();  // 透過代理呼叫，事務生效
    }
    
    @Transactional
    public void methodB() {
        // 事務生效
    }
}
```

---

## 快速參考

### 最容易犯的錯誤 TOP 10

1. ⚠️ 使用魔法數字/字串而非 Enum
2. ⚠️ 查詢不使用分頁
3. ⚠️ Long 使用 `==` 比較
4. ⚠️ MyBatis 動態 SQL 字串比較錯誤
5. ⚠️ Spring Bean 使用成員變數儲存請求資料
6. ⚠️ 異常被吞掉沒有記錄或拋出
7. ⚠️ 多表操作沒有加事務
8. ⚠️ List 只判斷 null 不判斷 isEmpty
9. ⚠️ BigDecimal 使用 double 建構
10. ⚠️ 日期使用字串比較

---

**完成**: 您已閱讀完所有開發規範！請回到 [RULE_00_INDEX.md](./RULE_00_INDEX.md) 查看完整索引。
