# 定時任務前端整合指南

## 📋 文件資訊
- **更新日期**: 2025-03-28
- **功能**: 前端整合任務類型選擇器
- **相關文件**: [定時任務類型管理 Enum](./QUARTZ_JOB_TYPE_ENUM.md)

---

## 🎯 快速開始

### 方案 A：使用封裝好的元件（推薦）

**步驟 1：在任務管理頁面引入元件**

```vue
<template>
  <el-dialog title="新增定時任務" :visible.sync="dialogVisible">
    <el-form :model="form" :rules="rules" ref="form">
      <!-- 任務名稱 -->
      <el-form-item label="任務名稱" prop="jobName">
        <el-input v-model="form.jobName" placeholder="請輸入任務名稱" />
      </el-form-item>
      
      <!-- 使用任務類型選擇器元件 -->
      <JobTypeSelector v-model="form.jobConfig" />
      
      <!-- 其他欄位... -->
    </el-form>
    
    <div slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm">確定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import JobTypeSelector from './components/JobTypeSelector'

export default {
  components: {
    JobTypeSelector
  },
  data() {
    return {
      dialogVisible: false,
      form: {
        jobName: '',
        jobConfig: {
          beanName: '',
          methodName: '',
          params: {},
          cronExpression: ''
        }
      }
    }
  },
  methods: {
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 組合最終的調用目標字串
          const invokeTarget = this.buildInvokeTarget()
          
          // 提交表單
          const data = {
            jobName: this.form.jobName,
            invokeTarget: invokeTarget,
            cronExpression: this.form.jobConfig.cronExpression
          }
          
          // 呼叫後端 API
          addJob(data).then(response => {
            this.$message.success('新增成功')
            this.dialogVisible = false
            this.loadJobList()
          })
        }
      })
    },
    
    buildInvokeTarget() {
      const { beanName, methodName, params } = this.form.jobConfig
      
      // 將參數轉換為字串陣列
      const paramValues = Object.values(params).map(v => {
        if (typeof v === 'string') {
          return v
        }
        return String(v)
      })
      
      // 組合格式：beanName.methodName(param1, param2, ...)
      if (paramValues.length > 0) {
        return `${beanName}.${methodName}(${paramValues.join(', ')})`
      } else {
        return `${beanName}.${methodName}()`
      }
    }
  }
}
</script>
```

---

### 方案 B：直接使用 API（自訂 UI）

**步驟 1：載入任務類型列表**

```vue
<template>
  <el-select v-model="selectedType" @change="handleTypeChange">
    <el-option
      v-for="type in jobTypes"
      :key="type.code"
      :label="type.name"
      :value="type.code" />
  </el-select>
</template>

<script>
import { listJobTypes, getJobTypeByCode } from '@/api/monitor/jobType'

export default {
  data() {
    return {
      jobTypes: [],
      selectedType: ''
    }
  },
  created() {
    this.loadJobTypes()
  },
  methods: {
    async loadJobTypes() {
      const response = await listJobTypes()
      this.jobTypes = response.data
    },
    
    async handleTypeChange(code) {
      const response = await getJobTypeByCode(code)
      const taskType = response.data
      
      // 使用任務類型資料
      console.log('Bean:', taskType.beanName)
      console.log('Method:', taskType.methodName)
      console.log('Parameters:', taskType.parameters)
    }
  }
}
</script>
```

---

## 📝 完整範例

### 任務管理頁面（完整版）

```vue
<template>
  <div class="app-container">
    <!-- 查詢表單 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="任務名稱">
        <el-input v-model="queryParams.jobName" placeholder="請輸入任務名稱" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具列 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="handleAdd">
          新增
        </el-button>
      </el-col>
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="jobList">
      <el-table-column label="任務名稱" prop="jobName" />
      <el-table-column label="調用目標" prop="invokeTarget" />
      <el-table-column label="Cron 表達式" prop="cronExpression" />
      <el-table-column label="狀態" prop="status">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)">刪除</el-button>
          <el-button size="mini" type="text" @click="handleRun(scope.row)">執行</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/修改對話框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="700px"
      @close="handleDialogClose">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <!-- 任務名稱 -->
        <el-form-item label="任務名稱" prop="jobName">
          <el-input v-model="form.jobName" placeholder="請輸入任務名稱" />
        </el-form-item>

        <!-- 任務類型選擇器 -->
        <JobTypeSelector v-model="form.jobConfig" />

        <!-- 備註 -->
        <el-form-item label="備註">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">確定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listJob, addJob, updateJob, delJob, runJob, changeJobStatus } from '@/api/monitor/job'
import JobTypeSelector from './components/JobTypeSelector'

export default {
  name: 'Job',
  components: {
    JobTypeSelector
  },
  data() {
    return {
      // 遮罩層
      loading: true,
      // 任務列表
      jobList: [],
      // 對話框
      dialogVisible: false,
      dialogTitle: '',
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined
      },
      // 表單
      form: {
        jobName: '',
        jobConfig: {
          beanName: '',
          methodName: '',
          params: {},
          cronExpression: ''
        },
        remark: ''
      },
      // 表單驗證
      rules: {
        jobName: [
          { required: true, message: '任務名稱不能為空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查詢任務列表 */
    getList() {
      this.loading = true
      listJob(this.queryParams).then(response => {
        this.jobList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    
    /** 重置按鈕操作 */
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset()
      this.dialogVisible = true
      this.dialogTitle = '新增定時任務'
    },
    
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset()
      // 載入資料...
      this.dialogVisible = true
      this.dialogTitle = '修改定時任務'
    },
    
    /** 提交按鈕 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          // 組合調用目標
          const invokeTarget = this.buildInvokeTarget()
          
          const data = {
            jobId: this.form.jobId,
            jobName: this.form.jobName,
            invokeTarget: invokeTarget,
            cronExpression: this.form.jobConfig.cronExpression,
            remark: this.form.remark
          }
          
          if (this.form.jobId != null) {
            updateJob(data).then(response => {
              this.$message.success('修改成功')
              this.dialogVisible = false
              this.getList()
            })
          } else {
            addJob(data).then(response => {
              this.$message.success('新增成功')
              this.dialogVisible = false
              this.getList()
            })
          }
        }
      })
    },
    
    /** 刪除按鈕操作 */
    handleDelete(row) {
      this.$confirm('是否確認刪除任務編號為"' + row.jobId + '"的資料選項?', '警告', {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delJob(row.jobId)
      }).then(() => {
        this.getList()
        this.$message.success('刪除成功')
      })
    },
    
    /** 任務狀態修改 */
    handleStatusChange(row) {
      let text = row.status === '0' ? '啟用' : '停用'
      this.$confirm('確認要"' + text + '""' + row.jobName + '"任務嗎?', '警告', {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return changeJobStatus(row.jobId, row.status)
      }).then(() => {
        this.$message.success(text + '成功')
      }).catch(() => {
        row.status = row.status === '0' ? '1' : '0'
      })
    },
    
    /** 立即執行 */
    handleRun(row) {
      this.$confirm('確認要立即執行一次"' + row.jobName + '"任務嗎?', '警告', {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return runJob(row.jobId)
      }).then(() => {
        this.$message.success('執行成功')
      })
    },
    
    /** 組合調用目標字串 */
    buildInvokeTarget() {
      const { beanName, methodName, params } = this.form.jobConfig
      
      if (!beanName || !methodName) {
        return ''
      }
      
      // 將參數物件轉換為陣列
      const paramValues = Object.values(params || {}).map(v => {
        // 字串類型加引號
        if (typeof v === 'string') {
          return `'${v}'`
        }
        return String(v)
      })
      
      // 組合格式：beanName.methodName(param1, param2, ...)
      if (paramValues.length > 0) {
        return `${beanName}.${methodName}(${paramValues.join(', ')})`
      } else {
        return `${beanName}.${methodName}()`
      }
    },
    
    /** 表單重置 */
    reset() {
      this.form = {
        jobId: null,
        jobName: '',
        jobConfig: {
          beanName: '',
          methodName: '',
          params: {},
          cronExpression: ''
        },
        remark: ''
      }
      this.resetForm('form')
    },
    
    /** 對話框關閉 */
    handleDialogClose() {
      this.reset()
    }
  }
}
</script>
```

---

## 🎨 UI 展示效果

### 新增任務畫面

```
┌────────────────────────────────────────┐
│ 新增定時任務                          │
├────────────────────────────────────────┤
│ 任務名稱: [________________]          │
│                                        │
│ 任務類型: [▼ 執行爬蟲（無參數）]      │
│           ├─ 爬蟲任務                  │
│           │  ├─ 執行爬蟲（無參數）     │
│           │  ├─ 執行爬蟲（帶模式）     │
│           │  └─ 執行爬蟲（完整參數）   │
│                                        │
│ Bean 名稱: [crawlerTask] 🔒           │
│ 方法名稱: [run] 🔒                    │
│                                        │
│ ┌─── 任務參數 ────────────────────┐   │
│ │ 爬蟲類型: [CA102________] STRING│   │
│ │ ⚠️ 必填參數                     │   │
│ └──────────────────────────────────┘   │
│                                        │
│ Cron 表達式: [0 0 1 * * ?]            │
│              [使用建議值 🪄]           │
│ ⏰ 建議值: 0 0 1 * * ?                │
│                                        │
│ 備註: [_________________________]     │
│                                        │
├────────────────────────────────────────┤
│               [取消]  [確定]           │
└────────────────────────────────────────┘
```

---

## 📋 參數格式說明

### invokeTarget 格式

根據 Quartz 的要求，`invokeTarget` 需要是以下格式：

```
beanName.methodName(param1, param2, ...)
```

### 範例

| 任務類型 | invokeTarget 範例 |
|---------|------------------|
| 執行爬蟲（無參數） | `crawlerTask.run('CA102')` |
| 執行爬蟲（帶模式） | `crawlerTask.runWithMode('CA102', 'today-only')` |
| 執行爬蟲（完整參數） | `crawlerTask.runAdvanced('CA102', 'full-sync', 1000, 120000)` |

### 參數類型轉換

| 前端類型 | 後端類型 | 格式 |
|---------|---------|------|
| STRING | String | `'CA102'` |
| INTEGER | Integer | `1000` |
| LONG | Long | `120000` |
| BOOLEAN | Boolean | `true` 或 `false` |

---

## 🔧 進階功能

### 1. 參數驗證

```vue
<script>
export default {
  methods: {
    validateParams() {
      const { parameters } = this.currentTaskType
      const { params } = this.form.jobConfig
      
      // 檢查必填參數
      for (const param of parameters) {
        if (param.required && !params[param.name]) {
          this.$message.error(`${param.description} 為必填參數`)
          return false
        }
      }
      
      // 檢查參數類型
      for (const param of parameters) {
        const value = params[param.name]
        if (value !== undefined) {
          if (param.type === 'INTEGER' && !Number.isInteger(Number(value))) {
            this.$message.error(`${param.description} 必須為整數`)
            return false
          }
          if (param.type === 'LONG' && isNaN(Number(value))) {
            this.$message.error(`${param.description} 必須為數字`)
            return false
          }
        }
      }
      
      return true
    }
  }
}
</script>
```

### 2. Cron 表達式輔助

```vue
<template>
  <el-form-item label="Cron 表達式">
    <el-input v-model="form.cronExpression">
      <template slot="append">
        <el-button @click="showCronHelper">Cron 輔助</el-button>
      </template>
    </el-input>
    
    <!-- Cron 表達式說明 -->
    <div class="cron-help">
      <el-collapse>
        <el-collapse-item title="常用表達式">
          <el-tag @click="setCron('0 0 1 * * ?')">每天凌晨1點</el-tag>
          <el-tag @click="setCron('0 0 */2 * * ?')">每2小時</el-tag>
          <el-tag @click="setCron('0 */30 * * * ?')">每30分鐘</el-tag>
          <el-tag @click="setCron('0 0 9-17 * * ?')">每天9-17點</el-tag>
        </el-collapse-item>
      </el-collapse>
    </div>
  </el-form-item>
</template>
```

### 3. 任務預覽

```vue
<template>
  <el-card class="preview-card">
    <div slot="header">任務預覽</div>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="調用目標">
        <code>{{ previewInvokeTarget }}</code>
      </el-descriptions-item>
      <el-descriptions-item label="執行時間">
        {{ previewNextRun }}
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script>
export default {
  computed: {
    previewInvokeTarget() {
      return this.buildInvokeTarget()
    },
    previewNextRun() {
      // 使用 cron-parser 套件計算下次執行時間
      const parser = require('cron-parser')
      try {
        const interval = parser.parseExpression(this.form.cronExpression)
        return interval.next().toString()
      } catch (e) {
        return '無效的 Cron 表達式'
      }
    }
  }
}
</script>
```

---

## ✅ 檢查清單

部署前請確認：

- [ ] 已建立 `ScheduledTaskType` enum
- [ ] 已建立 `ScheduledTaskTypeController`
- [ ] 已建立前端 API：`jobType.js`
- [ ] 已建立元件：`JobTypeSelector.vue`
- [ ] 已在任務管理頁面整合元件
- [ ] 已測試新增任務功能
- [ ] 已測試修改任務功能
- [ ] 已測試參數驗證
- [ ] 已測試 Cron 表達式

---

## 🔗 相關文件

- [定時任務類型管理 Enum](./QUARTZ_JOB_TYPE_ENUM.md)
- [爬蟲參數化與 Quartz 排程整合](./CRAWLER_PARAMS_AND_QUARTZ.md)

---

**更新日期**: 2025-03-28  
**維護者**: Cheng  
**版本**: 1.0
