# 定時任務類型管理 Enum

## 📋 更新資訊
- **更新日期**: 2025-03-28
- **功能**: 建立任務類型 Enum，讓後台介面可以下拉選擇
- **影響範圍**: Quartz 模組、前端任務管理

## 🎯 設計目標

### 問題
後台介面的定時任務 `/monitor/job` 需要使用者手動輸入：
- ❌ Bean 名稱（如：`crawlerTask`）
- ❌ 方法名稱（如：`run`）
- ❌ 參數格式（不清楚要傳什麼參數）

這樣對使用者很不直觀，容易出錯。

### 解決方案
建立 `ScheduledTaskType` enum：
- ✅ 定義所有可用的任務類型
- ✅ 包含任務的元資料（名稱、描述、參數等）
- ✅ 前端透過 API 取得並展示為下拉選單
- ✅ 選擇任務後自動填入 Bean、方法、參數

---

## 🏗️ 架構設計

### 1. Enum 定義

**`ScheduledTaskType.java`**：

```java
@Getter
@AllArgsConstructor
public enum ScheduledTaskType {
    
    CRAWLER_RUN(
        "crawler_run",                    // 任務代碼
        "執行爬蟲（無參數）",             // 任務名稱
        "執行指定類型的爬蟲，使用預設參數",  // 描述
        "crawlerTask",                     // Bean 名稱
        "run",                            // 方法名稱
        Arrays.asList(                    // 參數列表
            new TaskParameter("crawlerType", "STRING", true, "爬蟲類型", "CA102")
        ),
        "0 0 1 * * ?",                   // 建議的 Cron 表達式
        "爬蟲任務"                        // 分類
    ),
    
    CRAWLER_RUN_WITH_MODE(...),
    CRAWLER_RUN_ADVANCED(...),
    // ... 更多任務類型
}
```

### 2. Controller API

**提供 4 個 API**：

| API | 說明 |
|-----|------|
| `GET /monitor/job/types` | 取得所有任務類型 |
| `GET /monitor/job/types/category/{category}` | 根據分類取得任務類型 |
| `GET /monitor/job/types/categories` | 取得所有分類 |
| `GET /monitor/job/types/{code}` | 根據 code 取得詳情 |

### 3. 前端整合

**API 檔案**：`cheng-ui/src/api/monitor/jobType.js`

---

## 🚀 使用方式

### 後端：定義新任務

**步驟 1：在 enum 中新增任務類型**

```java
public enum ScheduledTaskType {
    
    // 新增任務
    DATA_SYNC_TASK(
        "data_sync_task",
        "資料同步任務",
        "從外部系統同步資料到本地資料庫",
        "dataSyncTask",           // 對應的 Bean
        "syncData",               // 對應的方法
        Arrays.asList(
            new TaskParameter("source", "STRING", true, "資料來源", "external_api"),
            new TaskParameter("batchSize", "INTEGER", false, "批次大小", "100")
        ),
        "0 0 */4 * * ?",         // 每 4 小時執行
        "資料同步"
    ),
    
    // ... 其他任務
}
```

**步驟 2：實作對應的 Task Bean**

```java
@Slf4j
@Component("dataSyncTask")
public class DataSyncTask {
    
    public void syncData(String source, Integer batchSize) {
        log.info("開始同步資料，來源: {}, 批次: {}", source, batchSize);
        // 實作邏輯
    }
}
```

**完成！前端會自動顯示新任務。**

---

### 前端：使用任務類型

#### 範例 1：簡單的下拉選單

```vue
<template>
  <el-form>
    <!-- 任務類型下拉選單 -->
    <el-form-item label="任務類型">
      <el-select v-model="selectedTaskType" @change="onTaskTypeChange">
        <el-option
          v-for="type in jobTypes"
          :key="type.code"
          :label="type.name"
          :value="type.code">
          <span>{{ type.name }}</span>
          <span style="color: #8492a6; font-size: 13px">{{ type.description }}</span>
        </el-option>
      </el-select>
    </el-form-item>
    
    <!-- 自動填入的欄位 -->
    <el-form-item label="Bean 名稱">
      <el-input v-model="form.beanName" disabled />
    </el-form-item>
    
    <el-form-item label="方法名稱">
      <el-input v-model="form.methodName" disabled />
    </el-form-item>
    
    <!-- 動態參數表單 -->
    <el-form-item
      v-for="param in currentParameters"
      :key="param.name"
      :label="param.description"
      :required="param.required">
      <el-input
        v-model="form.params[param.name]"
        :placeholder="param.example">
        <template slot="append">{{ param.type }}</template>
      </el-input>
    </el-form-item>
    
    <!-- Cron 表達式（帶建議） -->
    <el-form-item label="Cron 表達式">
      <el-input v-model="form.cronExpression" />
      <div v-if="suggestedCron" class="suggestion">
        建議：{{ suggestedCron }}
        <el-button type="text" @click="useSuggestedCron">使用建議值</el-button>
      </div>
    </el-form-item>
  </el-form>
</template>

<script>
import { listJobTypes, getJobTypeByCode } from '@/api/monitor/jobType'

export default {
  data() {
    return {
      jobTypes: [],
      selectedTaskType: '',
      currentTaskType: null,
      form: {
        beanName: '',
        methodName: '',
        params: {},
        cronExpression: ''
      }
    }
  },
  computed: {
    currentParameters() {
      return this.currentTaskType ? this.currentTaskType.parameters : []
    },
    suggestedCron() {
      return this.currentTaskType ? this.currentTaskType.suggestedCron : null
    }
  },
  created() {
    this.loadJobTypes()
  },
  methods: {
    // 載入所有任務類型
    async loadJobTypes() {
      const response = await listJobTypes()
      this.jobTypes = response.data
    },
    
    // 選擇任務類型
    async onTaskTypeChange(code) {
      const response = await getJobTypeByCode(code)
      this.currentTaskType = response.data
      
      // 自動填入 Bean 和方法
      this.form.beanName = this.currentTaskType.beanName
      this.form.methodName = this.currentTaskType.methodName
      
      // 清空參數
      this.form.params = {}
      
      // 使用建議的 Cron（如果有）
      if (this.currentTaskType.suggestedCron) {
        this.form.cronExpression = this.currentTaskType.suggestedCron
      }
    },
    
    // 使用建議的 Cron
    useSuggestedCron() {
      this.form.cronExpression = this.suggestedCron
    }
  }
}
</script>
```

#### 範例 2：分類選擇

```vue
<template>
  <el-form>
    <!-- 先選擇分類 -->
    <el-form-item label="任務分類">
      <el-radio-group v-model="selectedCategory" @change="onCategoryChange">
        <el-radio-button
          v-for="category in categories"
          :key="category"
          :label="category">
          {{ category }}
        </el-radio-button>
      </el-radio-group>
    </el-form-item>
    
    <!-- 再選擇該分類下的任務 -->
    <el-form-item label="任務類型">
      <el-select v-model="selectedTaskType" @change="onTaskTypeChange">
        <el-option
          v-for="type in filteredJobTypes"
          :key="type.code"
          :label="type.name"
          :value="type.code">
        </el-option>
      </el-select>
    </el-form-item>
    
    <!-- ... 其他欄位 -->
  </el-form>
</template>

<script>
import { listJobCategories, listJobTypesByCategory } from '@/api/monitor/jobType'

export default {
  data() {
    return {
      categories: [],
      selectedCategory: '',
      filteredJobTypes: [],
      selectedTaskType: ''
    }
  },
  created() {
    this.loadCategories()
  },
  methods: {
    async loadCategories() {
      const response = await listJobCategories()
      this.categories = response.data
      
      // 預設選擇第一個分類
      if (this.categories.length > 0) {
        this.selectedCategory = this.categories[0]
        this.onCategoryChange(this.selectedCategory)
      }
    },
    
    async onCategoryChange(category) {
      const response = await listJobTypesByCategory(category)
      this.filteredJobTypes = response.data
      this.selectedTaskType = ''
    },
    
    onTaskTypeChange(code) {
      // ... 同範例 1
    }
  }
}
</script>
```

---

## 📝 API 回應範例

### GET /monitor/job/types

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "code": "crawler_run",
      "name": "執行爬蟲（無參數）",
      "description": "執行指定類型的爬蟲，使用預設參數",
      "beanName": "crawlerTask",
      "methodName": "run",
      "parameters": [
        {
          "name": "crawlerType",
          "type": "STRING",
          "required": true,
          "description": "爬蟲類型",
          "example": "CA102"
        }
      ],
      "suggestedCron": "0 0 1 * * ?",
      "category": "爬蟲任務"
    },
    {
      "code": "crawler_run_with_mode",
      "name": "執行爬蟲（帶模式）",
      "description": "執行指定類型的爬蟲，可設定執行模式",
      "beanName": "crawlerTask",
      "methodName": "runWithMode",
      "parameters": [
        {
          "name": "crawlerType",
          "type": "STRING",
          "required": true,
          "description": "爬蟲類型",
          "example": "CA102"
        },
        {
          "name": "mode",
          "type": "STRING",
          "required": true,
          "description": "執行模式",
          "example": "today-only"
        }
      ],
      "suggestedCron": "0 0 9,12,15 * * ?",
      "category": "爬蟲任務"
    }
  ]
}
```

### GET /monitor/job/types/categories

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    "爬蟲任務"
  ]
}
```

---

## 🎨 前端 UI 流程

### 新增任務流程

```
1. 使用者點選「新增定時任務」
   ↓
2. 選擇「任務分類」（如：爬蟲任務）
   ↓
3. 從該分類的任務中選擇「任務類型」
   ↓
4. 系統自動填入：
   - Bean 名稱
   - 方法名稱
   - 建議的 Cron 表達式
   ↓
5. 系統動態產生參數表單
   - 根據任務類型的參數定義
   - 顯示參數說明和範例
   - 標示必填/非必填
   ↓
6. 使用者填入參數值
   ↓
7. 儲存任務
```

### 編輯任務流程

```
1. 載入現有任務資料
   ↓
2. 根據 beanName + methodName 反查任務類型
   ↓
3. 載入任務類型的元資料
   ↓
4. 顯示參數表單
   ↓
5. 使用者修改
   ↓
6. 儲存
```

---

## ✅ 優勢總結

### 改進前

| 問題 | 影響 |
|-----|------|
| 手動輸入 Bean 名稱 | ❌ 容易拼錯 |
| 手動輸入方法名稱 | ❌ 不知道有哪些方法可用 |
| 不知道參數格式 | ❌ 常常傳錯參數 |
| 沒有參數說明 | ❌ 不知道各參數的意義 |
| 沒有 Cron 建議 | ❌ 需要自己查詢 Cron 語法 |

### 改進後

| 特性 | 效果 |
|-----|------|
| **下拉選單** | ✅ 不會拼錯，使用者友善 |
| **自動填入** | ✅ Bean、方法自動帶入 |
| **參數表單** | ✅ 動態產生，帶說明和範例 |
| **參數驗證** | ✅ 前端可驗證必填和類型 |
| **Cron 建議** | ✅ 常用場景直接套用 |
| **分類管理** | ✅ 任務井然有序 |
| **可擴充** | ✅ 新增任務只需加 enum |

---

## 🔧 實際案例

### 案例 1：設定 CA102 爬蟲每天執行

**操作流程**：
1. 選擇任務類型：「執行爬蟲（無參數）」
2. 系統自動填入：
   - Bean：`crawlerTask`
   - 方法：`run`
   - Cron：`0 0 1 * * ?`
3. 填入參數：
   - crawlerType：`CA102`
4. 儲存

**後台實際執行**：
```java
crawlerTask.run("CA102")
```

### 案例 2：設定 CA102 爬蟲每天多次執行

**操作流程**：
1. 選擇任務類型：「執行爬蟲（帶模式）」
2. 系統自動填入：
   - Bean：`crawlerTask`
   - 方法：`runWithMode`
   - Cron：`0 0 9,12,15 * * ?`
3. 填入參數：
   - crawlerType：`CA102`
   - mode：`today-only`
4. 儲存

**後台實際執行**：
```java
crawlerTask.runWithMode("CA102", "today-only")
```

### 案例 3：設定完整參數的爬蟲

**操作流程**：
1. 選擇任務類型：「執行爬蟲（完整參數）」
2. 系統自動填入相關資訊
3. 填入參數：
   - crawlerType：`CA103`
   - mode：`full-sync`
   - batchSize：`1000`
   - timeout：`120000`
4. 儲存

**後台實際執行**：
```java
crawlerTask.runAdvanced("CA103", "full-sync", 1000, 120000L)
```

---

## 📋 後續擴充

### 1. 新增其他類型任務

```java
// 資料同步任務
DATA_SYNC_TASK(...),

// 報表產生任務
REPORT_GENERATION_TASK(...),

// 備份任務
BACKUP_TASK(...),

// 通知任務
NOTIFICATION_TASK(...)
```

### 2. 參數類型擴充

```java
// 支援更多參數類型
public enum ParameterType {
    STRING,
    INTEGER,
    LONG,
    BOOLEAN,
    DATE,
    DATETIME,
    ENUM,        // 枚舉類型（前端顯示為下拉選單）
    ARRAY,       // 陣列類型
    JSON         // JSON 物件
}
```

### 3. 參數驗證規則

```java
public static class TaskParameter {
    private String name;
    private String type;
    private boolean required;
    private String description;
    private String example;
    
    // 新增驗證規則
    private Integer minValue;      // 數值最小值
    private Integer maxValue;      // 數值最大值
    private Integer minLength;     // 字串最小長度
    private Integer maxLength;     // 字串最大長度
    private String pattern;        // 正則表達式
    private List<String> enumValues;  // 枚舉值列表
}
```

---

## 🔗 相關文件

- [爬蟲參數化與 Quartz 排程整合](./CRAWLER_PARAMS_AND_QUARTZ.md)
- [爬蟲框架架構總結](./ARCHITECTURE_SUMMARY.md)

---

**更新日期**: 2025-03-28  
**維護者**: Cheng  
**版本**: 1.0 (任務類型 Enum 管理)
