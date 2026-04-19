# Enum 使用規範 V2 ⭐ 重要更新

> **核心原則**: 任何狀態、類型、分類都必須使用 Enum，禁止使用魔法數字或字串！
> 
> **V2 更新**: 區分兩種 Enum 模式，避免冗餘設計

---

## 🎯 兩種 Enum 設計模式

### 模式 A：Code ≠ Name（資料庫儲存與 Enum 名稱不同）

**使用時機**:
- 資料庫儲存的是**數字**（0, 1, 2...）
- 資料庫儲存的是**簡短代碼**（"0", "1", "Y", "N"...）
- 需要與舊系統相容（既有資料庫設計）

**範例**:
```java
// ✅ 正確：code 和 name 不同，code 有存在價值
@Getter
@RequiredArgsConstructor
public enum Status {
    DISABLE(0, "停用"),    // code=0,   name="DISABLE"
    ENABLE(1, "啟用");     // code=1,   name="ENABLE"
    
    private final Integer code;        // 資料庫儲存 0/1
    private final String description;
    
    @JsonValue
    public Integer getCode() {
        return code;
    }
    
    public static Status fromCode(Integer code) {
        for (Status status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的狀態代碼: " + code);
    }
}
```

```java
// ✅ 正確：code 是簡短代碼
@Getter
@RequiredArgsConstructor
public enum UserStatus {
    OK("0", "正常"),       // code="0",  name="OK"
    DISABLE("1", "停用"),  // code="1",  name="DISABLE"
    DELETED("2", "刪除");  // code="2",  name="DELETED"
    
    private final String code;         // 資料庫儲存 "0"/"1"/"2"
    private final String description;
}
```

---

### 模式 B：Code = Name（資料庫儲存 Enum 名稱）

**使用時機**:
- 資料庫儲存的是**完整的 Enum 名稱**（"FOLLOWING", "BLOCKED"...）
- 新系統設計，沒有歷史包袱
- 資料庫欄位使用 VARCHAR，儲存可讀性高的值

**❌ 錯誤設計（冗餘）**:
```java
// ❌ 錯誤：code 和 name 相同，code 欄位是多餘的
@Getter
@RequiredArgsConstructor
public enum FollowStatus {
    UNFOLLOWED("UNFOLLOWED", "未關注"),  // code=name="UNFOLLOWED" 冗餘！
    FOLLOWING("FOLLOWING", "好友"),      // code=name="FOLLOWING" 冗餘！
    BLOCKED("BLOCKED", "已封鎖");        // code=name="BLOCKED" 冗餘！
    
    private final String code;        // ❌ 多餘！和 name() 一樣
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;                  // ❌ 直接用 name() 就好
    }
    
    public static FollowStatus fromCode(String code) {
        for (FollowStatus status : values()) {
            if (status.code.equals(code)) {  // ❌ 直接用 valueOf() 就好
                return status;
            }
        }
        throw new IllegalArgumentException("未知的關注狀態: " + code);
    }
}
```

**✅ 正確設計（簡化）**:
```java
// ✅ 正確：移除冗餘的 code 欄位
@Getter
@RequiredArgsConstructor
public enum FollowStatus {
    UNFOLLOWED("未關注"),
    FOLLOWING("好友"),
    BLOCKED("已封鎖");
    
    private final String description;  // 只保留描述
    
    /**
     * JSON 序列化時使用 Enum 的 name()
     */
    @JsonValue
    public String getCode() {
        return name();  // ✅ 直接返回 "UNFOLLOWED", "FOLLOWING", "BLOCKED"
    }
    
    /**
     * 從 code 字串轉換為 Enum
     */
    public static FollowStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);  // ✅ 直接使用 Java Enum 的 valueOf()
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的關注狀態: " + code, e);
        }
    }
}
```

**資料庫設計**:
```sql
CREATE TABLE sys_line_user (
    follow_status VARCHAR(20) NOT NULL DEFAULT 'UNFOLLOWED'
    -- 資料庫直接儲存 'UNFOLLOWED', 'FOLLOWING', 'BLOCKED'
    -- 不需要額外的 code 對照表
);
```

---

## 📋 兩種模式比較表

| 特性 | 模式 A (Code ≠ Name) | 模式 B (Code = Name) |
|-----|---------------------|---------------------|
| **資料庫儲存** | 數字或簡短代碼 (0, 1, "Y", "N") | Enum 名稱字串 ("FOLLOWING", "BLOCKED") |
| **code 欄位** | ✅ 必須 | ❌ 冗餘（移除） |
| **getCode()** | 返回 code 欄位 | 返回 name() |
| **fromCode()** | 遍歷比對 code | 使用 valueOf() |
| **可讀性** | 資料庫值需要對照 | 資料庫值直接可讀 |
| **適用場景** | 舊系統、節省空間 | 新系統、強調可讀性 |

---

## ⚠️ 重要決策指南

### 何時使用模式 A？

1. **資料庫已存在**，使用數字或簡短代碼
2. **需要節省空間**（高頻表，億級數據）
3. **與第三方系統對接**，必須使用特定代碼

**範例**:
```java
Status.DISABLE(0, "停用")          // ✅ 節省空間
YesNo.NO(0, "否")                  // ✅ Boolean 對應
UserStatus.OK("0", "正常")         // ✅ 舊系統相容
```

### 何時使用模式 B？

1. **新系統開發**，沒有歷史包袱
2. **強調可讀性**，資料庫值要一眼看懂
3. **VARCHAR 欄位**，不在意多幾個位元組

**範例**:
```java
FollowStatus.FOLLOWING("好友")     // ✅ 資料庫儲存 "FOLLOWING"
ChannelType.MAIN("主頻道")         // ✅ 資料庫儲存 "MAIN"
MessageType.PUSH("推播訊息")       // ✅ 資料庫儲存 "PUSH"
```

---

## 🔧 標準範本

### 範本 A：Code ≠ Name

```java
package com.cheng.xxx.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * XXX 狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum XxxStatus {
    
    VALUE_ONE(0, "值一"),
    VALUE_TWO(1, "值二");
    
    private final Integer code;        // 或 String code
    private final String description;
    
    /**
     * JSON 序列化時使用 code
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }
    
    /**
     * 從 code 轉換為 Enum
     */
    public static XxxStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (XxxStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的狀態代碼: " + code);
    }
}
```

### 範本 B：Code = Name

```java
package com.cheng.xxx.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * XXX 狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum XxxStatus {
    
    VALUE_ONE("值一"),
    VALUE_TWO("值二");
    
    private final String description;
    
    /**
     * JSON 序列化時使用 Enum name
     */
    @JsonValue
    public String getCode() {
        return name();  // 返回 "VALUE_ONE", "VALUE_TWO"
    }
    
    /**
     * 從 code 轉換為 Enum
     */
    public static XxxStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的狀態: " + code, e);
        }
    }
}
```

---

## 🚨 常見錯誤

### 錯誤 1：模式 B 還保留 code 欄位

```java
// ❌ 錯誤
public enum FollowStatus {
    FOLLOWING("FOLLOWING", "好友");  // code 和 name 重複！
    private final String code;
}

// ✅ 正確
public enum FollowStatus {
    FOLLOWING("好友");  // 只保留 description
    private final String description;
}
```

### 錯誤 2：fromCode() 還在遍歷

```java
// ❌ 錯誤（模式 B 不需要遍歷）
public static FollowStatus fromCode(String code) {
    for (FollowStatus status : values()) {
        if (status.name().equals(code)) {  // 多此一舉！
            return status;
        }
    }
}

// ✅ 正確
public static FollowStatus fromCode(String code) {
    return valueOf(code);  // 直接用 valueOf()
}
```

### 錯誤 3：@JsonValue 標註錯誤

```java
// ❌ 錯誤（模式 B 不應該有 code 欄位）
@JsonValue
public String getCode() {
    return code;  // code 欄位不存在！
}

// ✅ 正確
@JsonValue
public String getCode() {
    return name();  // 返回 Enum 的 name
}
```

---

## ✅ 檢查清單

開發新 Enum 時必須檢查：

### 設計階段
- [ ] 確認資料庫儲存的是什麼？（數字？代碼？Enum 名稱？）
- [ ] 選擇正確的模式（A 或 B）
- [ ] 如果是模式 B，確認不要加 code 欄位

### 實作階段
- [ ] 是否加上 @JsonValue 註解？
- [ ] fromCode() 方法是否正確？（模式 A 遍歷，模式 B 用 valueOf）
- [ ] 是否有 description 欄位？
- [ ] 是否有 JavaDoc 註解？

### Review 階段
- [ ] 檢查是否有冗餘的 code 欄位（code = name 的情況）
- [ ] JSON 序列化是否正確？
- [ ] 前後端資料格式是否一致？

---

## 📚 專案中的範例

### 模式 A 範例

| Enum | code | name | 原因 |
|------|------|------|------|
| `Status` | 0, 1 | DISABLE, ENABLE | 資料庫儲存數字 |
| `YesNo` | 0, 1 | NO, YES | Boolean 對應 |
| `UserStatus` | "0", "1", "2" | OK, DISABLE, DELETED | 舊系統相容 |

### 模式 B 範例

| Enum | 資料庫值 | 說明 |
|------|---------|------|
| `FollowStatus` | "FOLLOWING", "BLOCKED" | 新系統，強調可讀性 |
| `BindStatus` | "BOUND", "UNBOUND" | 新系統，強調可讀性 |
| `ChannelType` | "MAIN", "SUB", "TEST" | 新系統，強調可讀性 |
| `MessageType` | "PUSH", "REPLY" | 新系統，強調可讀性 |

---

## 🔄 從 V1 遷移到 V2

如果現有 Enum 是模式 B 但還有 code 欄位，請參考以下步驟優化：

### 步驟 1：確認是否為模式 B
```java
// 檢查：如果 code 和 name 相同，就是模式 B
FOLLOWING("FOLLOWING", "好友")  // code = name = "FOLLOWING" ✅ 是模式 B
```

### 步驟 2：簡化 Enum
```java
// 移除 code 欄位，只保留 description
FOLLOWING("好友")
```

### 步驟 3：更新 getCode()
```java
@JsonValue
public String getCode() {
    return name();  // 改用 name()
}
```

### 步驟 4：更新 fromCode()
```java
public static FollowStatus fromCode(String code) {
    return valueOf(code);  // 改用 valueOf()
}
```

詳細遷移指南請參考：`ENUM_OPTIMIZATION_GUIDE.md`

---

**總結**: 選對模式，避免冗餘，讓程式碼更簡潔！
