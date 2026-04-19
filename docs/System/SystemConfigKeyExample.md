# SystemConfigKey 系統配置鍵統一管理

## 概述

`SystemConfigKey` enum 統一管理 `sys_config` 表中的所有配置鍵，避免魔術字串散落各處，提升程式碼可維護性和可讀性。

---

## 設計原則

### ✅ 統一管理配置鍵

**之前（魔術字串）**：
```java
// ❌ 分散在各處，難以維護
configService.selectConfigByKey("sys.user.initPassword");
configService.selectConfigByKey("sys.account.captchaEnabled");
configService.selectConfigByKey("sys.login.blackIPList");
```

**現在（使用 Enum）**：
```java
// ✅ 集中管理，易於維護
configService.selectConfigByKey(SystemConfigKey.USER_INIT_PASSWORD.getCode());
configService.selectConfigByKey(SystemConfigKey.ACCOUNT_CAPTCHA_ENABLED.getCode());
configService.selectConfigByKey(SystemConfigKey.LOGIN_BLACK_IP_LIST.getCode());
```

### ✅ 類型安全

```java
// ❌ 魔術字串容易拼寫錯誤，編譯期無法發現
String value = configService.selectConfigByKey("sys.user.initPasswrod");  // 拼錯了！

// ✅ Enum 保證類型安全，編譯期即可發現錯誤
String value = configService.selectConfigByKey(SystemConfigKey.USER_INIT_PASSWORD.getCode());
```

### ✅ IDE 自動補全

使用 Enum 後，IDE 會自動提示所有可用的配置鍵，無需查閱文件或資料庫。

---

## 配置鍵分類

### 1. 主框架頁配置

```java
// 介面樣式名稱
SystemConfigKey.INDEX_SKIN_NAME.getCode()
// 可選值：skin-blue、skin-green、skin-purple、skin-red、skin-yellow

// 側邊欄主題
SystemConfigKey.INDEX_SIDE_THEME.getCode()
// 可選值：theme-dark（深色）、theme-light（淺色）
```

### 2. 使用者管理配置

```java
// 帳號初始密碼
SystemConfigKey.USER_INIT_PASSWORD.getCode()
// 預設值：123456

// 初始密碼修改策略
SystemConfigKey.ACCOUNT_INIT_PASSWORD_MODIFY.getCode()
// 0：關閉策略，無提示
// 1：提醒使用者，登入時提醒修改密碼

// 帳號密碼更新週期（天數）
SystemConfigKey.ACCOUNT_PASSWORD_VALIDATE_DAYS.getCode()
// 0：不限制
// 1-365：指定天數，超過後登入時提醒修改密碼
```

### 3. 帳號自助配置

```java
// 驗證碼開關
SystemConfigKey.ACCOUNT_CAPTCHA_ENABLED.getCode()
// true：開啟驗證碼功能
// false：關閉驗證碼功能

// 使用者註冊功能
SystemConfigKey.ACCOUNT_REGISTER_USER.getCode()
// true：開啟註冊功能
// false：關閉註冊功能
```

### 4. 登入安全配置

```java
// 黑名單列表
SystemConfigKey.LOGIN_BLACK_IP_LIST.getCode()
// 多個 IP 以分號分隔，支援通配符（*）和網段
// 範例：192.168.1.*;10.0.0.0/24
```

---

## 使用範例

### 範例 1：取得配置值

```java
@Autowired
private ISysConfigService configService;

public void example1() {
    // 取得初始密碼
    String initPassword = configService.selectConfigByKey(
        SystemConfigKey.USER_INIT_PASSWORD.getCode()
    );
    
    // 取得驗證碼開關
    boolean captchaEnabled = Convert.toBool(
        configService.selectConfigByKey(
            SystemConfigKey.ACCOUNT_CAPTCHA_ENABLED.getCode()
        )
    );
    
    // 取得密碼更新週期
    Integer validateDays = Convert.toInt(
        configService.selectConfigByKey(
            SystemConfigKey.ACCOUNT_PASSWORD_VALIDATE_DAYS.getCode()
        )
    );
}
```

### 範例 2：檢查配置鍵是否有效

```java
public void example2() {
    String configKey = "sys.user.initPassword";
    
    // 驗證配置鍵是否有效
    if (SystemConfigKey.isValidConfigKey(configKey)) {
        System.out.println("有效的配置鍵");
    } else {
        System.out.println("無效的配置鍵");
    }
}
```

### 範例 3：根據配置鍵取得 Enum

```java
public void example3() {
    String configKey = "sys.index.skinName";
    
    // 取得對應的 Enum（找不到返回 null）
    SystemConfigKey key = SystemConfigKey.fromCodeOrNull(configKey);
    
    if (key != null) {
        System.out.println("配置名稱：" + key.getDescription());
        System.out.println("配置鍵：" + key.getCode());
    }
    
    // 或者直接拋出異常（找不到時）
    SystemConfigKey key2 = SystemConfigKey.fromCode(configKey);
}
```

### 範例 4：使用便捷判斷方法

```java
public void example4() {
    SystemConfigKey key = SystemConfigKey.USER_INIT_PASSWORD;
    
    // 判斷是否為使用者管理相關配置
    if (key.isUserConfig()) {
        System.out.println("這是使用者管理配置");
    }
    
    // 判斷是否為介面樣式相關配置
    if (key.isIndexConfig()) {
        System.out.println("這是介面樣式配置");
    }
    
    // 判斷是否為帳號自助相關配置
    if (key.isAccountConfig()) {
        System.out.println("這是帳號自助配置");
    }
    
    // 判斷是否為登入安全相關配置
    if (key.isLoginConfig()) {
        System.out.println("這是登入安全配置");
    }
}
```

---

## 實際重構案例

### 案例 1：SysUserServiceImpl.java

**重構前**：
```java
String password = configService.selectConfigByKey("sys.user.initPassword");
user.setPassword(SecurityUtils.encryptPassword(password));
```

**重構後**：
```java
String password = configService.selectConfigByKey(
    SystemConfigKey.USER_INIT_PASSWORD.getCode()
);
user.setPassword(SecurityUtils.encryptPassword(password));
```

### 案例 2：SysLoginController.java

**重構前**：
```java
Integer initPasswordModify = Convert.toInt(
    configService.selectConfigByKey("sys.account.initPasswordModify")
);
```

**重構後**：
```java
Integer initPasswordModify = Convert.toInt(
    configService.selectConfigByKey(
        SystemConfigKey.ACCOUNT_INIT_PASSWORD_MODIFY.getCode()
    )
);
```

### 案例 3：SysConfigServiceImpl.java

**重構前**：
```java
@Override
public boolean selectCaptchaEnabled() {
    String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
    if (StringUtils.isEmpty(captchaEnabled)) {
        return true;
    }
    return Convert.toBool(captchaEnabled);
}
```

**重構後**：
```java
@Override
public boolean selectCaptchaEnabled() {
    String captchaEnabled = selectConfigByKey(
        SystemConfigKey.ACCOUNT_CAPTCHA_ENABLED.getCode()
    );
    if (StringUtils.isEmpty(captchaEnabled)) {
        return true;
    }
    return Convert.toBool(captchaEnabled);
}
```

---

## 新增配置鍵指南

當需要新增系統配置時：

### 1. 在資料庫中新增配置

```sql
insert into sys_config
values (9, '新配置名稱', 'sys.new.config', '預設值', 'Y', 'admin', sysdate(), '', null, '配置說明');
```

### 2. 在 SystemConfigKey enum 中新增對應項目

```java
/**
 * 新配置描述
 * 說明可選值或格式
 */
NEW_CONFIG("sys.new.config", "新配置名稱"),
```

### 3. 在程式碼中使用

```java
String value = configService.selectConfigByKey(
    SystemConfigKey.NEW_CONFIG.getCode()
);
```

---

## 優勢總結

### ✅ 可維護性

- 集中管理所有配置鍵，修改時只需改一處
- 避免魔術字串散落各處，難以追蹤

### ✅ 可讀性

- 使用 Enum 常量，語意清晰
- 配置鍵和描述一目了然
- 註釋詳細說明可選值和格式

### ✅ 類型安全

- 編譯期檢查，避免拼寫錯誤
- IDE 自動補全，提升開發效率
- 重構友好，重新命名自動更新所有引用

### ✅ 可擴展性

- 新增配置鍵只需在 enum 中增加一項
- 自動繼承所有工具方法（fromCode、isValidCode 等）
- 支援分類判斷（isUserConfig、isAccountConfig 等）

---

## 相關檔案

- **Enum 定義**：`/cheng-common/src/main/java/com/cheng/common/enums/SystemConfigKey.java`
- **基礎介面**：`/cheng-common/src/main/java/com/cheng/common/enums/CodedEnum.java`
- **工具類**：`/cheng-common/src/main/java/com/cheng/common/utils/EnumUtils.java`
- **資料庫初始化**：`/cheng-admin/src/main/resources/db/migration/V0.1__init_system_core.sql`

---

## 已重構的檔案

1. ✅ **SysUserServiceImpl.java** - 使用者管理服務
2. ✅ **SysLoginController.java** - 登入控制器
3. ✅ **SysRegisterController.java** - 註冊控制器
4. ✅ **SysLoginService.java** - 登入服務
5. ✅ **SysConfigServiceImpl.java** - 配置服務

所有使用系統配置鍵的地方都已經重構為使用 `SystemConfigKey` enum！
