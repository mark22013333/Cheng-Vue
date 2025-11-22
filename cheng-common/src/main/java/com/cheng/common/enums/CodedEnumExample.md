# CodedEnum 統一列舉設計

## 概述
所有使用 `code + description` 模式的列舉都實作 `CodedEnum<T>` 介面，並使用 `EnumUtils` 工具類統一處理。

---

## 架構設計

### 1. 基礎介面 - CodedEnum
```java
public interface CodedEnum<T> {
    T getCode();
    String getDescription();
}
```

**泛型參數**：
- `T` - 代碼類型（`Integer`、`String` 等）

---

### 2. 工具類 - EnumUtils

提供統一的列舉查詢和轉換方法：

| 方法 | 說明 | 返回值 |
|-----|------|--------|
| `fromCode(Class, code)` | 根據 code 查找列舉 | 找不到返回 `null` |
| `fromCodeOrThrow(Class, code)` | 根據 code 查找列舉 | 找不到拋出異常 |
| `getDescription(Class, code)` | 根據 code 取得描述 | 找不到返回空字串 |
| `getDescription(Class, code, default)` | 根據 code 取得描述 | 找不到返回預設值 |
| `isValidCode(Class, code)` | 檢查 code 是否有效 | `true`/`false` |

---

## 實作範例

### 範例 1：Status（Integer code）
```java
@Getter
@RequiredArgsConstructor
public enum Status implements CodedEnum<Integer> {
    DISABLE(0, "停用"),
    ENABLE(1, "啟用");

    private final Integer code;
    private final String description;

    public static Status fromCode(Integer code) {
        return EnumUtils.fromCodeOrThrow(Status.class, code);
    }
}
```

### 範例 2：ScanResult（String code）
```java
@Getter
@RequiredArgsConstructor
public enum ScanResult implements CodedEnum<String> {
    SUCCESS("0", "成功"),
    FAILURE("1", "失敗");

    private final String code;
    private final String description;

    public static ScanResult fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ScanResult.class, code);
    }
}
```

### 範例 3：ScanType（String code）
```java
@Getter
@RequiredArgsConstructor
public enum ScanType implements CodedEnum<String> {
    BARCODE("1", "條碼"),
    QRCODE("2", "QR Code");

    private final String code;
    private final String description;

    public static ScanType fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ScanType.class, code);
    }
}
```

---

## 使用方式

### 1. 基本轉換
```java
// 從 code 取得列舉（找不到返回 null）
Status status = EnumUtils.fromCode(Status.class, 1);

// 從 code 取得列舉（找不到拋出異常）
ScanResult result = EnumUtils.fromCodeOrThrow(ScanResult.class, "0");

// 使用列舉自己的 fromCode 方法（推薦）
ScanType type = ScanType.fromCode("1");
```

### 2. 取得描述
```java
// 取得描述（找不到返回空字串）
String desc = EnumUtils.getDescription(Status.class, 1);
// 結果："啟用"

// 取得描述（找不到返回預設值）
String desc2 = EnumUtils.getDescription(ScanResult.class, "9", "未知");
// 結果："未知"
```

### 3. 驗證代碼
```java
// 檢查代碼是否有效
boolean valid = EnumUtils.isValidCode(ScanType.class, "1");
// 結果：true

boolean invalid = EnumUtils.isValidCode(ScanType.class, "999");
// 結果：false
```

### 4. 實際應用範例
```java
// Controller 中使用
public AjaxResult updateStatus(@RequestParam Integer statusCode) {
    // 驗證狀態代碼
    if (!EnumUtils.isValidCode(Status.class, statusCode)) {
        return error("無效的狀態代碼");
    }
    
    Status status = Status.fromCode(statusCode);
    // ... 業務邏輯
}

// Service 中使用
public void processScanResult(String resultCode) {
    ScanResult result = EnumUtils.fromCode(ScanResult.class, resultCode);
    if (result == null) {
        log.warn("未知的掃描結果代碼: {}", resultCode);
        return;
    }
    
    if (result.isSuccess()) {
        // 處理成功邏輯
    } else {
        // 處理失敗邏輯
    }
}
```

---

## 統計查詢範例

### 優化前（使用魔術數字）
```java
statistics.put("successScans", 
    allList.stream().filter(s -> "0".equals(s.getScanResult())).count());
statistics.put("failedScans", 
    allList.stream().filter(s -> "1".equals(s.getScanResult())).count());
```

### 優化後（使用 Enum）
```java
statistics.put("successScans", 
    allList.stream()
        .filter(s -> ScanResult.SUCCESS.getCode().equals(s.getScanResult()))
        .count());
statistics.put("failedScans", 
    allList.stream()
        .filter(s -> ScanResult.FAILURE.getCode().equals(s.getScanResult()))
        .count());
```

### 進階寫法（使用 EnumUtils）
```java
statistics.put("successScans", 
    allList.stream()
        .map(InvScanLog::getScanResult)
        .filter(code -> EnumUtils.isValidCode(ScanResult.class, code))
        .map(code -> ScanResult.fromCode(code))
        .filter(ScanResult::isSuccess)
        .count());
```

---

## 優勢總結

### ✅ 統一設計
- 所有列舉遵循相同的設計模式
- 統一的 API 降低學習成本
- 容易維護和擴展

### ✅ 類型安全
- 泛型支援不同的 code 類型（`Integer`、`String`）
- 編譯期檢查，減少執行期錯誤
- IDE 自動補全和重構支援

### ✅ 簡化程式碼
```java
// ❌ 之前：每個列舉都要寫一遍邏輯
public static Status fromCode(Integer code) {
    if (code == null) return null;
    for (Status status : values()) {
        if (status.code.equals(code)) return status;
    }
    throw new IllegalArgumentException("未知的狀態代碼: " + code);
}

// ✅ 現在：一行搞定
public static Status fromCode(Integer code) {
    return EnumUtils.fromCodeOrThrow(Status.class, code);
}
```

### ✅ 彈性處理
- `fromCode` - 找不到返回 null，適合可選情況
- `fromCodeOrThrow` - 找不到拋出異常，適合必須情況
- `isValidCode` - 預先驗證，避免異常

### ✅ 可讀性提升
```java
// ❌ 魔術數字
if ("0".equals(scanResult)) { }

// ✅ 語意清晰
if (ScanResult.SUCCESS.getCode().equals(scanResult)) { }

// ✅ 更清晰
ScanResult result = ScanResult.fromCode(scanResult);
if (result.isSuccess()) { }
```

---

## 新增列舉指南

要新增一個符合此規範的列舉：

1. **實作 CodedEnum 介面**
   ```java
   public enum MyEnum implements CodedEnum<String> { }
   ```

2. **定義欄位**
   ```java
   private final String code;
   private final String description;
   ```

3. **使用 Lombok 註解**
   ```java
   @Getter
   @RequiredArgsConstructor
   ```

4. **提供 fromCode 方法**
   ```java
   public static MyEnum fromCode(String code) {
       return EnumUtils.fromCodeOrThrow(MyEnum.class, code);
   }
   ```

5. **（可選）新增便捷判斷方法**
   ```java
   public boolean isXxx() {
       return this == XXX;
   }
   ```

完整範例：
```java
@Getter
@RequiredArgsConstructor
public enum OrderStatus implements CodedEnum<String> {
    PENDING("0", "待處理"),
    APPROVED("1", "已批准"),
    REJECTED("2", "已拒絕");

    private final String code;
    private final String description;

    public static OrderStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(OrderStatus.class, code);
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }
}
```

---

## 現有列舉清單

| 列舉 | Code 類型 | 用途 |
|-----|----------|------|
| `Status` | `Integer` | 通用啟用/停用狀態 |
| `ScanResult` | `String` | 掃描結果（成功/失敗）|
| `ScanType` | `String` | 掃描類型（條碼/QR Code）|

未來新增列舉建議都遵循此設計模式。
