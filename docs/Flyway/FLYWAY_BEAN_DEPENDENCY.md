# 🔧 Flyway 與 Bean 初始化順序問題解決方案

## 問題描述

### ❌ 原始問題

新資料庫啟動時的執行順序：

```
1. Spring 容器初始化
   ↓
2. 建立 DataSource Bean
   ↓
3. 建立 Service Bean (SysConfigServiceImpl, SysDictTypeServiceImpl, SysJobServiceImpl)
   ↓
4. 執行 @PostConstruct 方法（嘗試查詢資料庫）
   ↓
   ❌ 失敗！表還不存在
   ↓
5. Flyway 開始執行遷移
   ↓
6. 建立所有資料表
   ↓
7. 應用啟動完成
```

**結果**：
- 應用程式啟動失敗（如果沒有 try-catch）
- 或者初始化被跳過，快取沒有資料（如果有 try-catch）

---

## ❌ 錯誤的解決方案

### 方案 A：加上 try-catch（不推薦）

```java
@PostConstruct
public void init() {
    try {
        loadingConfigCache();
    } catch (Exception e) {
        System.out.println("警告：無法載入配置");
    }
}
```

**問題**：
- `@PostConstruct` 只執行一次
- 如果第一次失敗，之後表建立完成後也不會再執行
- 結果：**快取永遠是空的**

---

## ✅ 正確的解決方案

### 使用 `@DependsOn("flywayInitializer")`

```java
@Service
@DependsOn("flywayInitializer")  // 關鍵！
public class SysConfigServiceImpl implements ISysConfigService {
    
    @PostConstruct
    public void init() {
        loadingConfigCache();  // 不需要 try-catch
    }
}
```

### 執行順序變更

```
1. Spring 容器初始化
   ↓
2. 建立 DataSource Bean
   ↓
3. Flyway 開始執行（flywayInitializer Bean 建立）
   ↓
4. Flyway 建立所有資料表 ✅
   ↓
5. Flyway 完成（flywayInitializer Bean 初始化完成）
   ↓
6. 建立依賴 Flyway 的 Service Bean
   ├─ SysConfigServiceImpl
   ├─ SysDictTypeServiceImpl
   └─ SysJobServiceImpl
   ↓
7. 執行 @PostConstruct 方法（表已經存在）✅
   ├─ 載入系統配置到 Redis
   ├─ 載入字典資料到快取
   └─ 初始化定時任務
   ↓
8. 應用啟動完成 🎉
```

---

## 📝 修改的檔案

### 1. SysConfigServiceImpl.java

```java
@Service
@DependsOn("flywayInitializer")
public class SysConfigServiceImpl implements ISysConfigService {
    @PostConstruct
    public void init() {
        loadingConfigCache();  // 載入系統配置到 Redis
    }
}
```

**功能**：從 `sys_config` 表載入配置到 Redis 快取

### 2. SysDictTypeServiceImpl.java

```java
@Service
@DependsOn("flywayInitializer")
public class SysDictTypeServiceImpl implements ISysDictTypeService {
    @PostConstruct
    public void init() {
        loadingDictCache();  // 載入字典資料到快取
    }
}
```

**功能**：從 `sys_dict_type` 和 `sys_dict_data` 表載入字典資料

### 3. SysJobServiceImpl.java

```java
@Service
@DependsOn("flywayInitializer")
public class SysJobServiceImpl implements ISysJobService {
    @PostConstruct
    public void init() throws SchedulerException, TaskException {
        scheduler.clear();
        List<SysJob> jobList = jobMapper.selectJobAll();
        for (SysJob job : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }
}
```

**功能**：從 `sys_job` 表載入定時任務並註冊到 Quartz

---

## 🎯 關鍵概念

### `@DependsOn` 註解

```java
@DependsOn("beanName")
```

**作用**：
- 指定 Bean 的建立順序
- 確保依賴的 Bean 先建立和初始化完成
- 不影響自動注入（`@Autowired`）

### Spring Boot Flyway Bean 名稱

Spring Boot 自動配置會建立以下 Bean：

- `flyway` - Flyway 實例
- `flywayInitializer` - Flyway 初始化器（執行遷移）

我們使用 `flywayInitializer` 因為它確保遷移已完成。

---

## 🔍 如何驗證

### 1. 觀察啟動日誌順序

```log
[INFO] Flyway Community Edition 10.21.0
[INFO] Database: jdbc:mysql://localhost:23506/cool-test
[INFO] Successfully applied 8 migrations  ← Flyway 先執行
[INFO] Started CoolAppsApplication         ← Service 初始化在這之後
```

### 2. 檢查快取資料

```bash
# 連線到 Redis
redis-cli

# 檢查系統配置
KEYS sys:config:*

# 檢查字典資料
KEYS sys:dict:*
```

應該看到配置和字典資料已載入。

### 3. 檢查定時任務

登入系統後台 → 系統監控 → 定時任務

應該看到任務已經自動載入並可以執行。

---

## ⚠️ 注意事項

### 1. Bean 名稱必須正確

```java
@DependsOn("flywayInitializer")  // ✅ 正確
@DependsOn("flyway")             // ⚠️ 可能不夠，遷移可能還沒完成
@DependsOn("Flyway")             // ❌ 錯誤，Bean 名稱大小寫敏感
```

### 2. 如果 Flyway 被停用

如果設定 `spring.flyway.enabled=false`，啟動會失敗，因為找不到 `flywayInitializer` Bean。

**解決方式**：
- 確保 Flyway 始終啟用
- 或者使用條件註解：`@ConditionalOnBean(name = "flywayInitializer")`

### 3. 循環依賴問題

如果 Flyway 的遷移腳本需要依賴這些 Service（通常不會），可能會產生循環依賴。

---

## 📚 相關資源

- [Spring @DependsOn 文件](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/DependsOn.html)
- [Spring Boot Flyway 自動配置](https://docs.spring.io/spring-boot/reference/howto/data-initialization.html)
- [Flyway 官方文件](https://flywaydb.org/documentation/)

---

## ✅ 總結

### 修改前

```
❌ 應用啟動失敗（表不存在）
❌ 或快取沒有資料（被 catch 跳過）
```

### 修改後

```
✅ Flyway 先執行，建立所有表
✅ Service 後初始化，正確載入快取資料
✅ 定時任務自動啟動
✅ 應用程式正常執行
```

---

**最後更新**：2025-10-22  
**維護者**：Cheng  
**重要性**：⭐⭐⭐⭐⭐ 關鍵修改，影響系統正常運作
