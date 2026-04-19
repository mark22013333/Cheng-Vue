# LOG 系統配置說明

## 📋 功能概述

本次更新完成了完整的 LOG 系統改造，主要功能包括：

1. ✅ **TraceId 追蹤** - 所有日誌都包含 traceId，方便追蹤請求鏈路
2. ✅ **自動壓縮** - 歷史日誌自動壓縮為 .gz 格式，節省磁碟空間
3. ✅ **目錄分離** - 當日日誌和歷史日誌分別存放在 current 和 archive 目錄
4. ✅ **完整異常堆疊** - 異常日誌包含完整的堆疊追蹤和行號定位
5. ✅ **Catalina.out 接管** - 所有日誌通過 Logback 輸出，catalina.out 保持精簡

---

## 🗂️ 日誌目錄結構

### 本地環境
```
/Users/cheng/cool-logs/
├── current/                    # 當日進行中的日誌
│   ├── sys-info.log           # INFO 級別系統日誌
│   ├── sys-error.log          # ERROR 級別系統日誌
│   └── sys-user.log           # 使用者操作日誌
└── archive/                    # 歷史日誌歸檔（自動壓縮）
    ├── sys-info.2025-11-01.log.gz
    ├── sys-info.2025-11-02.log.gz
    ├── sys-error.2025-11-01.log.gz
    └── sys-user.2025-11-01.log.gz
```

### 正式環境
```
/opt/cool-apps/logs/
├── current/
│   ├── sys-info.log
│   ├── sys-error.log
│   └── sys-user.log
└── archive/
    └── *.log.gz (歷史壓縮檔案)
```

**保留策略**：歷史日誌保留 60 天，超過 60 天的日誌會自動刪除。

---

## 📝 日誌格式說明

### 新的日誌格式
```
2025-11-02 12:30:45.123 | INFO  | [a1B2c3D4e5F6] | [http-nio-8080-exec-1] | com.cheng.web.controller.system.SysUserController.list:89 | 查詢使用者列表
```

### 格式欄位解析
| 欄位 | 說明 | 範例 |
|------|------|------|
| `2025-11-02 12:30:45.123` | 完整時間戳記（年-月-日 時:分:秒.毫秒） | 2025-11-02 12:30:45.123 |
| `INFO` | 日誌級別（INFO/WARN/ERROR 等） | INFO |
| `[a1B2c3D4e5F6]` | **TraceId**（請求追蹤 ID） | a1B2c3D4e5F6 |
| `[http-nio-8080-exec-1]` | 執行緒名稱 | http-nio-8080-exec-1 |
| `com.cheng...Controller.list:89` | 類別.方法名:行號 | SysUserController.list:89 |
| `查詢使用者列表` | 日誌訊息 | 查詢使用者列表 |

### 異常日誌格式
當發生異常時，日誌會自動包含完整的異常堆疊：
```
2025-11-02 12:30:45.123 | ERROR | [a1B2c3D4e5F6] | [http-nio-8080-exec-1] | com.cheng.service.UserService.updateUser:45 | 更新使用者失敗
java.lang.NullPointerException: 使用者對象為 null
    at com.cheng.service.impl.UserServiceImpl.updateUser(UserServiceImpl.java:45)
    at com.cheng.web.controller.system.SysUserController.edit(SysUserController.java:123)
    ... (完整堆疊追蹤)
```

---

## 🔧 核心組件說明

### 1. Logback 配置 (`logback.xml`)
- **位置**: `cheng-admin/src/main/resources/logback.xml`
- **功能**: 
  - 定義日誌格式和輸出規則
  - 配置日誌輪轉和壓縮策略
  - 設定日誌級別過濾

### 2. TraceFilter (`TraceFilter.java`)
- **位置**: `cheng-framework/.../web/filter/TraceFilter.java`
- **功能**:
  - 攔截所有 HTTP 請求
  - 自動產生或從請求標頭取得 traceId
  - 在回應標頭中返回 traceId（供前端使用）
  - 請求結束後清理 traceId

### 3. TraceTaskDecorator (`TraceTaskDecorator.java`)
- **位置**: `cheng-framework/.../config/TraceTaskDecorator.java`
- **功能**:
  - 裝飾非同步任務
  - 將父執行緒的 traceId 傳遞到子執行緒
  - 非同步任務結束後清理 traceId

### 4. ScheduledTraceAspect (`ScheduledTraceAspect.java`)
- **位置**: `cheng-framework/.../aspectj/ScheduledTraceAspect.java`
- **功能**:
  - 攔截所有 @Scheduled 定時任務
  - 為定時任務產生 traceId（前綴: SCHEDULED_）
  - 記錄定時任務的開始和結束

### 5. QuartzTraceAspect (`QuartzTraceAspect.java`)
- **位置**: `cheng-framework/.../aspectj/QuartzTraceAspect.java`
- **功能**:
  - 攔截所有 Quartz 定時任務（com.cheng.quartz.task 包）
  - 智慧提取任務識別資訊（爬蟲類型、任務名稱等）
  - 為爬蟲任務產生 traceId（前綴: QUARTZ_CA103_）
  - 為其他任務產生 traceId（前綴: QUARTZ_TaskName_）
  - 記錄任務的開始和結束

### 6. LoggingStartupListener (`LoggingStartupListener.java`)
- **位置**: `cheng-framework/.../config/LoggingStartupListener.java`
- **功能**:
  - 應用程式啟動時顯示 LOG 系統配置資訊
  - 顯示日誌目錄路徑、檔案列表、輪轉策略
  - 自動檢查和建立必要的日誌目錄

---

## 🎯 TraceId 使用說明

### TraceId 的產生規則

1. **HTTP 請求**
   - 自動產生 12 位隨機 traceId（包含數字、大小寫字母）
   - 如果請求標頭包含 `X-Trace-Id`，則使用該值

2. **非同步任務**
   - 繼承父執行緒的 traceId
   - 如果父執行緒無 traceId，則產生新的 traceId

3. **Spring 定時任務（@Scheduled）**
   - 自動產生帶 `SCHEDULED_` 前綴的 traceId
   - 格式: `SCHEDULED_a1B2c3D4e5F6`

4. **Quartz 定時任務**
   - 自動產生帶任務識別的 traceId
   - 爬蟲任務格式: `QUARTZ_CA103_a1B2c3D4e5F6`（包含爬蟲類型）
   - 其他任務格式: `QUARTZ_TaskClassName_a1B2c3D4e5F6`

### TraceId 的傳遞流程

```
HTTP 請求 (traceId: abc123)
  │
  ├─> Controller (traceId: abc123)
  │   └─> Service (traceId: abc123)
  │       └─> Mapper (traceId: abc123)
  │
  └─> 非同步任務 (traceId: abc123) ✅ 自動傳遞
      └─> 非同步 Service (traceId: abc123)
```

### 手動使用 TraceId

```java
import com.cheng.common.utils.TraceUtils;

// 取得當前 traceId
String traceId = TraceUtils.getTraceId();

// 手動初始化 traceId（一般不需要，Filter 會自動處理）
TraceUtils.initTrace();

// 手動設定 traceId
TraceUtils.setTraceId("customTraceId");

// 清理 traceId
TraceUtils.clearTrace();
```

---

## 🧪 測試方法

我們提供了專門的測試 Controller，可以驗證各種場景下的 traceId 功能。

### 測試端點

1. **HTTP 請求測試**
   ```bash
   GET http://localhost:8080/tool/trace/test-http
   ```
   驗證：普通 HTTP 請求是否自動產生 traceId

2. **非同步任務測試**
   ```bash
   GET http://localhost:8080/tool/trace/test-async
   ```
   驗證：非同步任務是否繼承父執行緒的 traceId

3. **異常測試**
   ```bash
   GET http://localhost:8080/tool/trace/test-exception
   ```
   驗證：異常日誌是否包含完整堆疊和 traceId

4. **多層呼叫測試**
   ```bash
   GET http://localhost:8080/tool/trace/test-nested
   ```
   驗證：多層方法呼叫是否保持相同的 traceId

### 測試步驟

1. 啟動應用程式
   ```bash
   cd /Users/cheng/IdeaProjects/R/Cheng-Vue
   mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password=diDsd]3FsGO@4dido
   ```

2. 訪問測試端點（使用 Postman、curl 或瀏覽器）

3. 查看日誌檔案
   ```bash
   # 查看當日 INFO 日誌
   tail -f /Users/cheng/cool-logs/current/sys-info.log
   
   # 查看當日 ERROR 日誌
   tail -f /Users/cheng/cool-logs/current/sys-error.log
   ```

4. 驗證 traceId
   - 同一個請求的所有日誌應該有相同的 traceId
   - 非同步任務的 traceId 應該與父執行緒相同
   - 定時任務的 traceId 應該有 `SCHEDULED_` 前綴

---

## 🔍 故障排查

### 問題 1: 日誌沒有顯示 traceId

**可能原因**:
- TraceFilter 沒有正確初始化
- 請求沒有經過 Filter（例如靜態資源）

**解決方法**:
```bash
# 檢查 TraceFilter 是否載入
# 啟動日誌應該包含: "TraceFilter 初始化完成"
grep "TraceFilter" /Users/cheng/cool-logs/current/sys-info.log
```

### 問題 2: 非同步任務的 traceId 不一致

**可能原因**:
- ThreadPoolTaskExecutor 沒有設定 TraceTaskDecorator

**解決方法**:
檢查 `ThreadPoolConfig.java` 中是否包含：
```java
executor.setTaskDecorator(new TraceTaskDecorator());
```

### 問題 3: 日誌目錄沒有建立

**可能原因**:
- Logback 沒有權限建立目錄

**解決方法**:
```bash
# 手動建立目錄
mkdir -p /Users/cheng/cool-logs/current
mkdir -p /Users/cheng/cool-logs/archive

# 設定權限
chmod -R 755 /Users/cheng/cool-logs
```

### 問題 4: 歷史日誌沒有壓縮

**可能原因**:
- 日誌輪轉還沒有觸發（需要等到隔天凌晨）

**驗證方法**:
```bash
# 查看 archive 目錄
ls -lh /Users/cheng/cool-logs/archive/

# 應該看到 .gz 壓縮檔案
# sys-info.2025-11-01.log.gz
```

---

## 📊 日誌查詢技巧

### 1. 根據 traceId 查詢完整請求鏈路

```bash
# 假設 traceId 是 abc123xyz
grep "abc123xyz" /Users/cheng/cool-logs/current/sys-info.log

# 跨多個日誌檔案查詢
grep "abc123xyz" /Users/cheng/cool-logs/current/*.log
grep "abc123xyz" /Users/cheng/cool-logs/archive/*.log.gz
```

### 2. 查詢特定時間範圍的日誌

```bash
# 查詢 12:00 到 13:00 的日誌
grep "2025-11-02 12:" /Users/cheng/cool-logs/current/sys-info.log
```

### 3. 查詢特定類別的日誌

```bash
# 查詢 SysUserController 的所有日誌
grep "SysUserController" /Users/cheng/cool-logs/current/sys-info.log
```

### 4. 查詢所有異常日誌

```bash
# 查看 ERROR 級別日誌
cat /Users/cheng/cool-logs/current/sys-error.log

# 查詢包含特定異常的日誌
grep "NullPointerException" /Users/cheng/cool-logs/current/sys-error.log
```

---

## 🚀 正式環境部署注意事項

1. **確認日誌目錄權限**
   ```bash
   # 在正式伺服器上建立目錄
   sudo mkdir -p /opt/cool-apps/logs/current
   sudo mkdir -p /opt/cool-apps/logs/archive
   
   # 設定權限（假設應用程式使用 tomcat 使用者）
   sudo chown -R tomcat:tomcat /opt/cool-apps/logs
   sudo chmod -R 755 /opt/cool-apps/logs
   ```

2. **監控磁碟空間**
   - 定期檢查日誌目錄的磁碟使用量
   - 可考慮調整 `maxHistory` 參數（目前是 60 天）

3. **日誌備份策略**
   - 壓縮檔案會自動產生，但超過 60 天會被刪除
   - 如需長期保存，建議定期備份到其他儲存系統

4. **效能考量**
   - TraceId 產生和傳遞的效能開銷極小
   - 日誌壓縮是非同步進行的，不影響應用程式效能

---

## 📚 相關檔案清單

### 配置檔案
- `cheng-admin/src/main/resources/logback.xml` - Logback 配置
- `cheng-admin/src/main/resources/application-local.yml` - 本地環境配置
- `cheng-admin/src/main/resources/application-prod.yml` - 正式環境配置

### 核心類別
- `cheng-common/src/main/java/com/cheng/common/utils/TraceUtils.java` - TraceId 工具類
- `cheng-framework/.../web/filter/TraceFilter.java` - HTTP 請求攔截器
- `cheng-framework/.../config/TraceTaskDecorator.java` - 非同步任務裝飾器
- `cheng-framework/.../config/ThreadPoolConfig.java` - 執行緒池配置
- `cheng-framework/.../aspectj/ScheduledTraceAspect.java` - Spring 定時任務切面
- `cheng-framework/.../aspectj/QuartzTraceAspect.java` - Quartz 定時任務切面
- `cheng-framework/.../config/LoggingStartupListener.java` - 日誌啟動監聽器
- `cheng-framework/.../manager/ShutdownManager.java` - 服務關閉管理器

### 測試類別
- `cheng-admin/.../controller/tool/TraceTestController.java` - 測試 Controller

---

## ✅ 功能檢查清單

- [x] Logback 配置更新（包含 traceId、壓縮、目錄分離）
- [x] TraceFilter 建立並註冊（HTTP 請求）
- [x] TraceTaskDecorator 建立並配置到執行緒池（非同步任務）
- [x] ScheduledTraceAspect 建立並啟用（Spring @Scheduled）
- [x] QuartzTraceAspect 建立並啟用（Quartz 定時任務、爬蟲任務）
- [x] LoggingStartupListener 建立（啟動時顯示配置）
- [x] 測試 Controller 建立
- [x] 所有日誌都包含 traceId
- [x] 歷史日誌自動壓縮為 .gz
- [x] 當日和歷史日誌分目錄存放
- [x] 異常日誌包含完整堆疊追蹤
- [x] 非同步任務正確傳遞 traceId
- [x] Spring 定時任務自動產生 traceId
- [x] Quartz 定時任務自動產生 traceId（含爬蟲類型識別）
- [x] Logger 命名規範統一（移除自訂 logger 名稱）

---

## 💡 最佳實踐建議

1. **在關鍵業務邏輯中記錄日誌**
   ```java
   log.info("開始處理訂單，訂單號: {}", orderId);
   // ... 業務邏輯
   log.info("訂單處理完成，訂單號: {}", orderId);
   ```

2. **異常處理時記錄 traceId**
   ```java
   try {
       // 業務邏輯
   } catch (Exception e) {
       log.error("處理失敗，traceId: {}", TraceUtils.getTraceId(), e);
       throw e;
   }
   ```

3. **在回應中返回 traceId（方便前端追蹤）**
   ```java
   Map<String, Object> result = new HashMap<>();
   result.put("traceId", TraceUtils.getTraceId());
   result.put("data", data);
   return AjaxResult.success(result);
   ```

4. **使用 traceId 進行問題定位**
   - 使用者回報問題時，請他們提供 traceId
   - 使用 traceId 在日誌中快速定位問題

---

## 📞 技術支援

如有任何問題或建議，請聯繫開發團隊。

**建立日期**: 2025-11-02  
**最後更新**: 2025-11-02  
**版本**: 1.0
