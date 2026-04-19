# Quartz 爬蟲任務 TraceId 支援說明

## 📋 問題描述

在查看爬蟲任務日誌時，發現所有日誌都顯示 `[NO_TRACE]`，無法追蹤單次爬蟲任務的完整執行鏈路。

**問題範例**：
```log
2025-11-02 13:24:48.261 | INFO  | [NO_TRACE] | [CoolAppsScheduler_Worker-2] | com.cheng.quartz.task.CrawlerTask:46 | 開始執行爬蟲任務，參數: {"crawlerType":"CA103","mode":"today-only"}
2025-11-02 13:24:48.263 | INFO  | [NO_TRACE] | [CoolAppsScheduler_Worker-2] | com.cheng.crawler.handler.CrawlerHandler:201 | [CA103] 開始執行爬蟲任務
```

**問題根源**：
- Quartz 任務由獨立的執行緒池執行（`CoolAppsScheduler_Worker-*`）
- 不經過 HTTP Filter，沒有自動初始化 traceId
- 之前的 `ScheduledTraceAspect` 只攔截 Spring 的 `@Scheduled` 註解，無法處理 Quartz 任務

---

## ✅ 解決方案

在 **`AbstractQuartzJob`** 中直接處理 traceId，確保整個 Quartz 任務生命週期都有 traceId。

### 為什麼不用 AOP 切面？

最初嘗試使用 `QuartzTraceAspect` 切面攔截 `com.cheng.quartz.task` 包，但發現：
- Quartz 任務執行完成後，還會執行 `after()` 方法記錄 job log
- 這個記錄操作在切面範圍之外，導致 Mapper 的 SQL 日誌沒有 traceId
- 解決方案：直接在 `AbstractQuartzJob.execute()` 中處理 traceId

### 核心特性

1. **覆蓋完整生命週期**
   - 在 `execute()` 開始時初始化 traceId
   - 在 `finally` 中清理 traceId（確保 job log 記錄完成後）
   - 無需修改現有的 Quartz 任務程式碼

2. **使用任務名稱作為識別**
   - 從 `SysJob` 中取得任務名稱
   - 自動處理特殊字元（如：`[CA103] 臺灣證券交易所` → `CA103_臺灣證券交易所`）
   - 限制長度避免 traceId 過長

3. **產生帶識別的 traceId**
   - 格式：`QUARTZ_任務名稱_a1B2c3D4e5F6`
   - 範例：`QUARTZ_CA103_臺灣證券交易所_a1B2c3D4e5F6`

---

## 🎯 效果展示

### 修改前
```log
2025-11-02 13:24:48.261 | INFO  | [NO_TRACE] | [CoolAppsScheduler_Worker-2] | com.cheng.quartz.task.CrawlerTask:46 | 開始執行爬蟲任務
2025-11-02 13:24:48.263 | INFO  | [NO_TRACE] | [CoolAppsScheduler_Worker-2] | com.cheng.crawler.handler.CrawlerHandler:201 | [CA103] 開始執行爬蟲任務
2025-11-02 13:24:49.128 | INFO  | [NO_TRACE] | [CoolAppsScheduler_Worker-2] | com.cheng.crawler.handler.CA103WHandler:102 | 開始執行證券交易所收盤價爬蟲
```

### 修改後
```log
2025-11-02 13:24:48.261 | INFO  | [QUARTZ_CA103_a1B2c3D4e5F6] | [CoolAppsScheduler_Worker-2] | com.cheng.quartz.task.CrawlerTask:46 | 開始執行爬蟲任務
2025-11-02 13:24:48.263 | INFO  | [QUARTZ_CA103_a1B2c3D4e5F6] | [CoolAppsScheduler_Worker-2] | com.cheng.crawler.handler.CrawlerHandler:201 | [CA103] 開始執行爬蟲任務
2025-11-02 13:24:49.128 | INFO  | [QUARTZ_CA103_a1B2c3D4e5F6] | [CoolAppsScheduler_Worker-2] | com.cheng.crawler.handler.CA103WHandler:102 | 開始執行證券交易所收盤價爬蟲
2025-11-02 13:31:46.907 | DEBUG | [QUARTZ_CA103_a1B2c3D4e5F6] | [CoolAppsScheduler_Worker-1] | c.cheng.quartz.mapper.SysJobLogMapper.insertJobLog:135 | ==>  Preparing: insert into sys_job_log...
```

**優點**：
- ✅ 可以清楚看到這是 CA103 爬蟲任務
- ✅ 同一次執行的所有日誌都有相同的 traceId
- ✅ **包含 Mapper 的 SQL 日誌**（重要！）
- ✅ **包含 job log 記錄的日誌**（重要！）
- ✅ 方便追蹤完整的爬蟲執行鏈路

---

## 🔧 實作細節

### 核心程式碼

**檔案位置**：`cheng-quartz/src/main/java/com/cheng/quartz/util/AbstractQuartzJob.java`

**主要功能**：

1. **在 execute() 開始時初始化 traceId**
   ```java
   @Override
   public void execute(JobExecutionContext context) {
       SysJob sysJob = new SysJob();
       BeanUtils.copyBeanProp(sysJob, context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES));
       
       // 初始化 traceId，使用任務名稱作為前綴
       String taskIdentifier = buildTaskIdentifier(sysJob);
       TraceUtils.initTrace(taskIdentifier);
       
       try {
           before(context, sysJob);
           doExecute(context, sysJob);   // 執行實際任務
           after(context, sysJob, null);  // 記錄 job log
       } catch (Exception e) {
           log.error("任務執行異常  - ：", e);
           after(context, sysJob, e);
       } finally {
           // 清理 traceId（確保在 job log 記錄之後）
           TraceUtils.clearTrace();
       }
   }
   ```

2. **建立任務識別字串（智慧提取代號）**
   ```java
   private String buildTaskIdentifier(SysJob sysJob) {
       StringBuilder identifier = new StringBuilder("QUARTZ");
       
       if (sysJob != null && StringUtils.isNotEmpty(sysJob.getJobName())) {
           String jobName = sysJob.getJobName();
           
           // 嘗試提取方括號內的代號（如：[CA103] → CA103）
           if (jobName.contains("[") && jobName.contains("]")) {
               int start = jobName.indexOf('[');
               int end = jobName.indexOf(']');
               if (start < end) {
                   String code = jobName.substring(start + 1, end).trim();
                   if (!code.isEmpty()) {
                       identifier.append("_").append(code);
                       return identifier.toString();
                   }
               }
           }
           
           // 如果沒有方括號，使用完整任務名稱（簡化處理）
           jobName = jobName
                   .replaceAll("[\\[\\]\\s\\-]", "_")
                   .replaceAll("_+", "_")
                   .replaceAll("^_|_$", "");
           
           if (jobName.length() > 20) {
               jobName = jobName.substring(0, 20);
           }
           
           identifier.append("_").append(jobName);
       }
       
       return identifier.toString();
   }
   ```

3. **範例任務名稱轉換**
   - 爬蟲任務：`[CA103] 臺灣證券交易所 - 上市公司每日收盤價` → `QUARTZ_CA103_xxx`
   - 一般任務：`系統資料同步任務` → `QUARTZ_系統資料同步任務_xxx`
   - 帶方括號：`[Data-Sync] 資料庫備份` → `QUARTZ_Data-Sync_xxx`

---

## 📝 TraceId 產生規則

### 智慧提取任務識別

TraceId 會自動從 `SysJob.jobName` 中智慧提取識別資訊：

**規則 1：優先提取方括號內的代號（爬蟲任務適用）**
```
任務名稱: [CA103] 臺灣證券交易所 - 上市公司每日收盤價
提取代號: CA103
產生的 traceId: QUARTZ_CA103_a1B2c3D4e5F6  ← 簡潔明瞭！
```

```
任務名稱: [Data-Sync] 資料庫備份
提取代號: Data-Sync
產生的 traceId: QUARTZ_Data-Sync_a1B2c3D4e5F6
```

**規則 2：沒有方括號時使用完整名稱（限制 20 字元）**
```
任務名稱: 系統資料同步任務
產生的 traceId: QUARTZ_系統資料同步任務_a1B2c3D4e5F6
```

```
任務名稱: 這是一個非常非常非常長的任務名稱
產生的 traceId: QUARTZ_這是一個非常非常非常長的任_a1B2c3D4e5F6  ← 自動截斷
```

### 特殊字元處理

- **有方括號**：直接使用方括號內的內容，保留原始格式
- **無方括號**：`[`, `]`, 空格, `-` → 轉換為 `_`，連續的 `_` 會合併

---

## 🧪 測試方法

### 1. 手動觸發 Quartz 任務

透過後台介面手動執行爬蟲任務，然後查看日誌：

```bash
# 查看日誌
tail -f /Users/cheng/cool-logs/current/sys-info.log | grep QUARTZ_CA103
```

### 2. 驗證 traceId

確認以下幾點：
- ✅ traceId 包含 `QUARTZ_` 前綴
- ✅ traceId 包含爬蟲類型（如：`CA103`）
- ✅ 同一次執行的所有日誌都有相同的 traceId
- ✅ 不同次執行有不同的 traceId

### 3. 根據 traceId 查詢完整鏈路

```bash
# 假設 traceId 是 QUARTZ_CA103_a1B2c3D4e5F6
grep "QUARTZ_CA103_a1B2c3D4e5F6" /Users/cheng/cool-logs/current/sys-info.log

# 應該能看到完整的執行鏈路：
# - CrawlerTask 開始執行
# - CrawlerHandler 處理
# - CA103WHandler 爬取資料
# - 資料處理和儲存
# - 任務完成
```

---

## 🎯 擴展性說明

### 支援其他 Quartz 任務

`QuartzTraceAspect` 會自動攔截 `com.cheng.quartz.task` 包下的所有任務：

1. **如果是爬蟲任務**
   - 自動提取 crawlerType
   - 產生格式：`QUARTZ_CA103_xxx`

2. **如果是其他任務**
   - 使用類別名稱作為識別
   - 產生格式：`QUARTZ_TaskClassName_xxx`

### 新增自訂任務範例

```java
package com.cheng.quartz.task;

@Component("customTask")
public class CustomTask {
    
    public void execute(String param) {
        // 自動產生 traceId: QUARTZ_CustomTask_xxx
        log.info("執行自訂任務，參數: {}", param);
    }
}
```

不需要任何額外配置，traceId 會自動產生！

---

## 📊 TraceId 前綴總覽

| 任務類型 | TraceId 格式 | 範例 |
|---------|------------|------|
| HTTP 請求 | 無前綴 | `a1B2c3D4e5F6` |
| 非同步任務 | 繼承父執行緒 | `a1B2c3D4e5F6` |
| Spring 定時任務 | `SCHEDULED_` | `SCHEDULED_a1B2c3D4e5F6` |
| Quartz 爬蟲任務 | `QUARTZ_代號_` | `QUARTZ_CA103_a1B2c3D4e5F6` |
| Quartz 其他任務 | `QUARTZ_任務名稱_` | `QUARTZ_系統資料同步任務_a1B2c3D4e5F6` |

---

## ✅ 完成檢查清單

- [x] 修改 `AbstractQuartzJob` 直接處理 traceId
- [x] 智慧提取任務識別（優先使用方括號內的代號）
- [x] 爬蟲任務 traceId 簡化為 `QUARTZ_CA103_xxx`（不含完整名稱）
- [x] 支援所有 Quartz 任務（無需修改任務程式碼）
- [x] 自動初始化和清理 traceId
- [x] **確保 Mapper SQL 日誌包含 traceId**（重要！）
- [x] **確保 job log 記錄包含 traceId**（重要！）
- [x] 停用原有的 `QuartzTraceAspect` 切面
- [x] 更新系統文件（LOG系統配置說明.md、Quartz爬蟲TraceId支援說明.md）

---

## 💡 使用建議

1. **查詢特定爬蟲任務日誌**
   ```bash
   # 查詢所有 CA103 爬蟲執行記錄
   grep "QUARTZ_CA103" /Users/cheng/cool-logs/current/sys-info.log
   
   # 查詢特定任務的完整鏈路
   grep "QUARTZ_CA103_a1B2c3D4e5F6" /Users/cheng/cool-logs/current/sys-info.log
   ```

2. **監控爬蟲任務執行狀況**
   ```bash
   # 即時監控所有 Quartz 任務
   tail -f /Users/cheng/cool-logs/current/sys-info.log | grep QUARTZ
   ```

3. **問題排查**
   - 如果爬蟲任務出現問題，先取得 traceId
   - 使用 traceId 查詢完整的執行鏈路
   - 可以看到每一步的執行時間、參數、結果

---

## 📚 相關文件

- [LOG系統配置說明.md](./LOG系統配置說明.md) - 完整的 LOG 系統文件
- `QuartzTraceAspect.java` - 原始碼位置
- `CrawlerTask.java` - 爬蟲任務入口

---

**建立日期**: 2025-11-02  
**版本**: 1.0
