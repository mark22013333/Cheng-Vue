# 定時任務範本功能修復說明

## 📋 修復資訊
- **修復日期**: 2025-03-28
- **問題**: 1) 參數驗證錯誤 2) 缺少預設任務名稱
- **影響範圍**: 前端驗證邏輯、後端 Enum

---

## 🐛 問題 1：參數已帶入卻出現必填警告

### 問題描述
使用者選擇任務類型後，參數已經自動帶入預設值（如 `CA102`），但提交時仍然出現「必填參數」的錯誤提示。

### 根本原因
前端驗證邏輯使用 `!this.taskParams[param.name]` 來檢查參數是否存在，這會將以下值都當作「沒有值」：
- 空字串 `''`
- 數字 `0`
- 布林值 `false`

這導致即使參數有值，也可能被誤判為空。

### 修復方案

**修改前**：
```javascript
if (param.required && !this.taskParams[param.name]) {
  this.$message.error(`${param.description} 為必填參數`)
  return
}
```

**修復後**：
```javascript
const value = this.taskParams[param.name]
if (param.required && (value === undefined || value === null || value === '')) {
  this.$message.error(`${param.description} 為必填參數`)
  return
}
```

### 改進效果
- ✅ 正確判斷參數是否為空
- ✅ 支援數字 0 和布林值 false
- ✅ 只在真正為空時才提示錯誤

---

## 🎯 問題 2：缺少預設任務名稱

### 需求描述
使用者希望每個排程任務都可以從後端自訂預設的任務名稱，這樣在建立任務時可以自動填入一個有意義的名稱，使用者可以直接使用或修改。

### 實作方案

#### 1. 後端 Enum 新增欄位

**修改檔案**: `ScheduledTaskType.java`

**新增欄位**：
```java
/**
 * 預設任務名稱（建議值）
 */
private final String defaultJobName;
```

**更新所有任務定義**：
```java
CRAWLER_RUN(
    "crawler_run",
    "執行爬蟲（無參數）",
    "執行指定類型的爬蟲，使用預設參數",
    "crawlerTask",
    "run",
    List.of(
        new TaskParameter("crawlerType", "STRING", true, "爬蟲類型", "CA102")
    ),
    "0 0 1 * * ?",
    "爬蟲任務",
    "每日爬蟲任務"  // ← 新增：預設任務名稱
),
```

#### 2. Controller 回傳欄位

**修改檔案**: `ScheduledTaskTypeController.java`

**TaskTypeVO 新增欄位**：
```java
public static class TaskTypeVO {
    private String code;
    private String name;
    private String description;
    private String beanName;
    private String methodName;
    private List<ParameterVO> parameters;
    private String suggestedCron;
    private String category;
    private String defaultJobName;  // ← 新增
}
```

**轉換方法更新**：
```java
vo.setDefaultJobName(taskType.getDefaultJobName());
```

#### 3. 前端自動填入

**修改檔案**: `index.vue`

**選擇任務類型時自動填入**：
```javascript
async handleTaskTypeChange(code) {
  // ...
  
  // 使用建議的任務名稱（如果任務名稱為空且有預設名稱）
  if (this.currentTaskType.defaultJobName && !this.form.jobName) {
    this.form.jobName = this.currentTaskType.defaultJobName
  }
  
  // ...
}
```

---

## 📊 預設任務名稱列表

| 任務類型 | 預設名稱 |
|---------|---------|
| `CRAWLER_RUN` | 每日爬蟲任務 |
| `CRAWLER_RUN_WITH_MODE` | 今日爬蟲任務 |
| `CRAWLER_RUN_ADVANCED` | 完整爬蟲任務 |
| `CRAWLER_RUN_DATE_RANGE` | 日期範圍爬蟲任務 |
| `CRAWLER_RUN_FULL` | 完整配置爬蟲任務 |

---

## 🎨 使用者體驗改進

### 改進前
```
1. 選擇任務類型：「執行爬蟲（無參數）」
2. 任務名稱欄位：[空的_____________]  ← 需要手動輸入
3. 參數自動填入：CA102
4. 點選確定
5. ❌ 錯誤：「爬蟲類型 為必填參數」  ← 明明有值卻報錯
```

### 改進後
```
1. 選擇任務類型：「執行爬蟲（無參數）」
2. 任務名稱欄位：[每日爬蟲任務_____]  ← 自動填入預設名稱
3. 參數自動填入：CA102
4. 點選確定
5. ✅ 成功：任務建立成功
```

---

## 🔧 技術細節

### API 回應範例

**修改後的回應格式**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
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
    "category": "爬蟲任務",
    "defaultJobName": "每日爬蟲任務"  // ← 新增欄位
  }
}
```

### 前端自動填入邏輯

```javascript
// 自動填入的優先順序
handleTaskTypeChange(code) {
  // 1. 載入任務類型詳情
  const taskType = await getJobTypeByCode(code)
  
  // 2. 自動填入參數預設值
  taskType.parameters.forEach(param => {
    if (param.example) {
      this.taskParams[param.name] = param.example
    }
  })
  
  // 3. 自動填入任務名稱（如果欄位為空）
  if (taskType.defaultJobName && !this.form.jobName) {
    this.form.jobName = taskType.defaultJobName
  }
  
  // 4. 自動填入 Cron 表達式
  if (taskType.suggestedCron) {
    this.form.cronExpression = taskType.suggestedCron
  }
}
```

---

## ✅ 測試驗證

### 測試案例 1：參數驗證修復

**步驟**：
1. 選擇任務類型：「執行爬蟲（無參數）」
2. 參數自動填入：`CA102`
3. 直接點選「確定」

**預期結果**：
- ✅ 提交成功，不再出現必填錯誤
- ✅ 任務正確建立

### 測試案例 2：預設任務名稱

**步驟**：
1. 點選「新增」按鈕
2. 任務名稱欄位為空
3. 選擇任務類型：「執行爬蟲（帶模式）」

**預期結果**：
- ✅ 任務名稱自動填入：`今日爬蟲任務`
- ✅ 使用者可以修改或直接使用

### 測試案例 3：已有名稱不覆蓋

**步驟**：
1. 點選「新增」按鈕
2. 先輸入任務名稱：`我的自訂任務`
3. 選擇任務類型：「執行爬蟲（無參數）」

**預期結果**：
- ✅ 任務名稱保持為：`我的自訂任務`
- ✅ 不會被預設名稱覆蓋

### 測試案例 4：數字和布林值參數

**步驟**：
1. 選擇任務類型：「執行爬蟲（完整參數）」
2. 批次大小設為：`0`（數字 0）
3. 點選「確定」

**預期結果**：
- ✅ 數字 0 被正確識別為有效值
- ✅ 提交成功

---

## 📝 修改檔案清單

### 後端（2 個檔案）

1. **ScheduledTaskType.java**
   - 新增 `defaultJobName` 欄位
   - 更新所有 5 個任務定義

2. **ScheduledTaskTypeController.java**
   - TaskTypeVO 新增 `defaultJobName` 欄位
   - convertToVO 方法加入欄位轉換

### 前端（1 個檔案）

3. **index.vue**
   - 修復參數驗證邏輯（更精確的空值判斷）
   - 新增自動填入預設任務名稱邏輯

---

## 🎉 改進總結

### 問題 1 修復效果

| 項目 | 修復前 | 修復後 |
|-----|-------|-------|
| **參數有值判斷** | ❌ 不精確 | ✅ 精確判斷 |
| **數字 0 支援** | ❌ 被當作空值 | ✅ 正確識別 |
| **布林 false 支援** | ❌ 被當作空值 | ✅ 正確識別 |
| **誤報率** | 高 | 無 |

### 問題 2 實作效果

| 項目 | 實作前 | 實作後 |
|-----|-------|-------|
| **任務名稱** | ❌ 需手動輸入 | ✅ 自動填入 |
| **名稱來源** | ❌ 無建議 | ✅ 後端定義 |
| **可修改性** | - | ✅ 可自由修改 |
| **一致性** | ❌ 命名不統一 | ✅ 統一規範 |

---

## 🔗 相關文件

- [定時任務快速使用指南](./QUARTZ_QUICK_START.md)
- [功能總結](./QUARTZ_TEMPLATE_FEATURE.md)
- [部署檢查清單](./DEPLOYMENT_CHECKLIST_QUARTZ.md)

---

**修復日期**: 2025-03-28  
**修復者**: Cheng  
**版本**: 1.1 (修復版本)
