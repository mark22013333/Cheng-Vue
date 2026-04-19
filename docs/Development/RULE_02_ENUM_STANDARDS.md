# Enum 使用規範 ⭐ 最重要

> **核心原則**: 任何狀態、類型、分類都必須使用 Enum，禁止使用魔法數字或字串！

---

## 為什麼必須使用 Enum？

### ❌ 魔法數字/字串的問題

```java
// 不要這樣寫！
if (status.equals("0")) {
    // 這是什麼狀態？正常？停用？誰知道？
}

if (type == 1) {
    // 類型 1 是什麼？主頻道？副頻道？
}

user.setStatus("1");  // 1 是什麼意思？
```

**問題**:
1. ❌ 可讀性差：看不懂 "0"、"1" 代表什麼
2. ❌ 容易出錯：可能寫錯值 (例如: "2" 但實際只有 "0" 和 "1")
3. ❌ 維護困難：需要查資料庫或文件才知道含義
4. ❌ IDE 無法提示：無法自動完成，容易拼錯
5. ❌ 重構困難：無法全域搜尋和替換

### ✅ 使用 Enum 的優勢

```java
// 正確示範
if (status == Status.NORMAL) {
    // 清楚明瞭！是正常狀態
}

if (channelType == ChannelType.MAIN) {
    // 一目了然！是主頻道
}

user.setStatus(Status.NORMAL);  // 語義清晰
```

**優勢**:
1. ✅ 可讀性強：一看就懂
2. ✅ 類型安全：編譯期檢查，避免錯誤
3. ✅ IDE 支援：自動完成、重構
4. ✅ 文件清晰：Enum 即文件
5. ✅ 維護容易：集中管理所有狀態

---

## 標準 Enum 設計範本

### 基本結構

```java
package com.cheng.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * 狀態 Enum
 * 
 * @author CoolApps
 */
public enum Status {
    /** 正常 */
    NORMAL("0", "正常"),
    /** 停用 */
    DISABLE("1", "停用");

    private final String code;
    private final String description;

    Status(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 取得狀態代碼（儲存到資料庫的值）
     * @JsonValue 註解：JSON 序列化時使用 code 欄位
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 取得狀態描述（中文說明）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根據 code 取得對應的 Enum
     * 
     * @param code 狀態代碼
     * @return 對應的 Enum，找不到返回 null
     */
    public static Status getByCode(String code) {
        return Arrays.stream(values())
            .filter(e -> e.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 檢查 code 是否有效
     * 
     * @param code 狀態代碼
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}
```

### 必要元素清單

每個 Enum 必須包含：
- ✅ `code` 欄位：儲存到資料庫的值
- ✅ `description` 欄位：中文描述
- ✅ `@JsonValue` 註解：JSON 序列化使用 code
- ✅ `getByCode()` 靜態方法：根據 code 取得 Enum
- ✅ JavaDoc 註解：說明每個值的含義

---

## Spring Converter 配置

### 為什麼需要 Converter？

前端傳遞 `code` (例如: "0")，後端需要自動轉換為 Enum。

### Converter 範本

```java
package com.cheng.framework.config.converter;

import com.cheng.common.enums.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 字串轉 Status Enum 轉換器
 * 
 * @author CoolApps
 */
@Component
public class StringToStatusConverter implements Converter<String, Status> {
    @Override
    public Status convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return Status.getByCode(source);
    }
}
```

### 註冊 Converter

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private StringToStatusConverter statusConverter;
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(statusConverter);
    }
}
```

---

## 使用範例

### 實體類使用

```java
@Data
@TableName("sys_user")
public class SysUser {
    private Long userId;
    private String userName;
    
    /** 用戶狀態：使用 Enum */
    private Status status;
    
    /** 性別：使用自定義 Enum */
    private Gender gender;
}
```

### Controller 使用

```java
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    
    @PostMapping
    public AjaxResult add(@RequestBody SysUser user) {
        // 前端傳遞: {"status": "0"}
        // 自動轉換為: Status.NORMAL
        
        if (user.getStatus() == Status.DISABLE) {
            return AjaxResult.error("不能新增停用的用戶");
        }
        
        return toAjax(userService.insertUser(user));
    }
}
```

### Service 使用

```java
@Service
public class SysUserServiceImpl implements ISysUserService {
    
    @Override
    public int updateUserStatus(Long userId, Status status) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(status);
        
        // MyBatis 會自動將 Enum 的 code 存入資料庫
        return userMapper.updateUser(user);
    }
    
    @Override
    public List<SysUser> selectNormalUsers() {
        // 使用 Enum 進行查詢
        return userMapper.selectUserList(
            SysUser.builder()
                .status(Status.NORMAL)
                .build()
        );
    }
}
```

### MyBatis Mapper 使用

```xml
<select id="selectUserList" resultMap="UserResult">
    SELECT * FROM sys_user
    WHERE del_flag = '0'
    <if test="status != null">
        AND status = #{status}
    </if>
</select>
```

**注意**: MyBatis 會自動取得 Enum 的 `code` 欄位（因為有 `@JsonValue` 註解）

---

## 現有 Enum 列表

### cheng-common 模組

#### Status (通用狀態)
```java
NORMAL("0", "正常")
DISABLE("1", "停用")
```
**用途**: 用戶狀態、角色狀態、選單狀態、部門狀態等

#### YesNo (是否)
```java
YES("Y", "是")
NO("N", "否")
```
**用途**: 是否預設、是否內建、是否必填等

#### UserStatus (用戶狀態)
```java
OK("0", "正常")
DISABLE("1", "停用")
DELETED("2", "刪除")
```

#### BusinessType (業務操作類型)
```java
OTHER(0, "其他")
INSERT(1, "新增")
UPDATE(2, "修改")
DELETE(3, "刪除")
GRANT(4, "授權")
EXPORT(5, "匯出")
IMPORT(6, "匯入")
FORCE(7, "強制登出")
CLEAN(8, "清空資料")
```
**用途**: 操作日誌記錄

#### BusinessStatus (業務操作狀態)
```java
SUCCESS(0, "成功")
FAIL(1, "失敗")
```

#### DataSourceType (資料來源類型)
```java
MASTER("master", "主庫")
SLAVE("slave", "從庫")
```

#### HttpMethod (HTTP 請求方法)
```java
GET("GET", "查詢")
POST("POST", "新增")
PUT("PUT", "修改")
DELETE("DELETE", "刪除")
PATCH("PATCH", "部分更新")
```

#### OperatorType (操作者類型)
```java
OTHER(0, "其他")
MANAGE(1, "後台用戶")
MOBILE(2, "手機端用戶")
```

### cheng-line 模組

#### ChannelType (LINE 頻道類型)
```java
MAIN("MAIN", "主頻道")
SUB("SUB", "副頻道")
TEST("TEST", "測試頻道")
```

#### BindStatus (綁定狀態)
```java
UNBOUND(0, "未綁定")
BOUND(1, "已綁定")
```

#### FollowStatus (追蹤狀態)
```java
UNFOLLOWED(0, "未追蹤")
FOLLOWED(1, "已追蹤")
```

#### MessageType (訊息類型)
```java
TEXT(1, "文字訊息")
IMAGE(2, "圖片訊息")
VIDEO(3, "影片訊息")
AUDIO(4, "音訊訊息")
LOCATION(5, "位置訊息")
STICKER(6, "貼圖訊息")
TEMPLATE(7, "範本訊息")
FLEX(8, "Flex 訊息")
```

#### ContentType (內容類型)
```java
TEXT("text", "文字")
IMAGE("image", "圖片")
VIDEO("video", "影片")
AUDIO("audio", "音訊")
FILE("file", "檔案")
LOCATION("location", "位置")
STICKER("sticker", "貼圖")
IMAGEMAP("imagemap", "圖片地圖")
TEMPLATE("template", "範本")
FLEX("flex", "Flex 訊息")
```

#### SendStatus (發送狀態)
```java
PENDING(0, "待發送")
SUCCESS(1, "發送成功")
FAILED(2, "發送失敗")
CANCELLED(3, "已取消")
```

#### TargetType (目標類型)
```java
USER("user", "個人")
GROUP("group", "群組")
ROOM("room", "聊天室")
```

---

## 常見錯誤與解決方案

### 錯誤 1: 忘記加 @JsonValue

❌ **錯誤**:
```java
public enum Status {
    NORMAL("0", "正常"),
    DISABLE("1", "停用");
    
    private final String code;
    
    // 沒有 @JsonValue 註解
    public String getCode() {
        return code;
    }
}
```

**結果**: JSON 序列化時返回 `"NORMAL"` 而非 `"0"`

✅ **正確**:
```java
@JsonValue  // 加上這個註解！
public String getCode() {
    return code;
}
```

### 錯誤 2: 前端傳遞 Enum 名稱而非 code

❌ **錯誤請求**:
```json
{
  "status": "NORMAL"
}
```

✅ **正確請求**:
```json
{
  "status": "0"
}
```

### 錯誤 3: 資料庫儲存 Enum 名稱

❌ **錯誤**: 資料庫欄位值為 `"NORMAL"`

✅ **正確**: 資料庫欄位值為 `"0"`

**確保 MyBatis 配置**:
```yaml
mybatis:
  configuration:
    default-enum-type-handler: org.apache.ibatis.type.EnumTypeHandler
```

---

## 新增 Enum 的步驟

1. **建立 Enum 類別**:
   - 放在 `cheng-common/src/main/java/com/cheng/common/enums/`
   - 或 `cheng-line/src/main/java/com/cheng/line/enums/` (LINE 專用)

2. **複製標準範本**:
   - 參考 `Status.java` 或 `ChannelType.java`

3. **定義 Enum 值**:
   - 使用有意義的名稱 (全大寫，底線分隔)
   - 設定 code 和 description

4. **建立 Converter** (如果需要接收前端參數):
   ```java
   @Component
   public class StringToXxxConverter implements Converter<String, Xxx> {
       @Override
       public Xxx convert(String source) {
           return Xxx.getByCode(source);
       }
   }
   ```

5. **註冊 Converter**:
   - 在 `WebMvcConfig` 中註冊

6. **更新文件**:
   - 在本檔案的「現有 Enum 列表」中補充

---

## 檢查清單

開發時必須自我檢查：

- [ ] 是否有使用魔法數字或字串？
- [ ] 所有狀態/類型是否都使用 Enum？
- [ ] Enum 是否包含 `code` 和 `description`？
- [ ] 是否加上 `@JsonValue` 註解？
- [ ] 是否提供 `getByCode()` 方法？
- [ ] 是否建立對應的 Converter？
- [ ] 資料庫欄位是否儲存 code 而非 name？

---

**下一步**: 閱讀 [RULE_03_DESIGN_PATTERNS.md](./RULE_03_DESIGN_PATTERNS.md) 學習設計模式應用
