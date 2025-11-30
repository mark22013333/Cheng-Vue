<template>
  <div class="job-type-selector">
    <!-- 任務類型選擇 -->
    <el-form-item label="任務類型" prop="taskType">
      <el-select
        v-model="selectedTaskType"
        placeholder="請選擇任務類型"
        @change="handleTaskTypeChange"
        style="width: 100%">
        <el-option-group
          v-for="category in groupedJobTypes"
          :key="category.name"
          :label="category.name">
          <el-option
            v-for="type in category.types"
            :key="type.code"
            :label="type.name"
            :value="type.code">
            <span style="float: left">{{ type.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 12px">{{ type.description }}</span>
          </el-option>
        </el-option-group>
      </el-select>
    </el-form-item>

    <!-- Bean 名稱（自動填入，唯讀） -->
    <el-form-item label="Bean 名稱" prop="invokeTarget">
      <el-input v-model="form.beanName" disabled>
        <template slot="append">
          <el-tooltip content="此欄位自動填入，無需修改" placement="top">
            <i class="el-icon-info"></i>
          </el-tooltip>
        </template>
      </el-input>
    </el-form-item>

    <!-- 方法名稱（自動填入，唯讀） -->
    <el-form-item label="方法名稱" prop="methodName">
      <el-input v-model="form.methodName" disabled>
        <template slot="append">
          <el-tooltip content="此欄位自動填入，無需修改" placement="top">
            <i class="el-icon-info"></i>
          </el-tooltip>
        </template>
      </el-input>
    </el-form-item>

    <!-- 動態參數表單 -->
    <div v-if="currentParameters.length > 0" class="parameters-section">
      <el-divider content-position="left">任務參數</el-divider>
      
      <el-form-item
        v-for="param in currentParameters"
        :key="param.name"
        :label="param.description"
        :prop="`params.${param.name}`"
        :required="param.required">
        
        <!-- 字串類型 -->
        <el-input
          v-if="param.type === 'STRING'"
          v-model="form.params[param.name]"
          :placeholder="`範例: ${param.example}`"
          clearable>
          <template slot="prepend">{{ param.type }}</template>
        </el-input>

        <!-- 整數類型 -->
        <el-input-number
          v-else-if="param.type === 'INTEGER'"
          v-model="form.params[param.name]"
          :placeholder="`範例: ${param.example}`"
          style="width: 100%"
          controls-position="right" />

        <!-- 長整數類型 -->
        <el-input-number
          v-else-if="param.type === 'LONG'"
          v-model="form.params[param.name]"
          :placeholder="`範例: ${param.example}`"
          style="width: 100%"
          controls-position="right" />

        <!-- 布林類型 -->
        <el-switch
          v-else-if="param.type === 'BOOLEAN'"
          v-model="form.params[param.name]" />

        <!-- 預設（字串） -->
        <el-input
          v-else
          v-model="form.params[param.name]"
          :placeholder="`範例: ${param.example}`"
          clearable />
        
        <!-- 參數說明 -->
        <div v-if="param.required" class="param-hint required">
          <i class="el-icon-warning"></i> 必填參數
        </div>
        <div v-else class="param-hint optional">
          <i class="el-icon-info"></i> 選填參數
        </div>
      </el-form-item>
    </div>

    <!-- Cron 表達式 -->
    <el-form-item label="Cron 表達式" prop="cronExpression">
      <el-input v-model="form.cronExpression" placeholder="請輸入 Cron 表達式">
        <template slot="append">
          <el-button
            v-if="suggestedCron"
            @click="useSuggestedCron"
            icon="el-icon-magic-stick"
            size="small">
            使用建議值
          </el-button>
        </template>
      </el-input>
      <div v-if="suggestedCron" class="cron-suggestion">
        <i class="el-icon-time"></i> 建議值：<code>{{ suggestedCron }}</code>
      </div>
    </el-form-item>
  </div>
</template>

<script>
import { listJobTypes, getJobTypeByCode } from '@/api/monitor/jobType'

export default {
  name: 'JobTypeSelector',
  props: {
    // 表單資料（v-model）
    value: {
      type: Object,
      default: () => ({})
    }
  },
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
    // 當前任務的參數列表
    currentParameters() {
      return this.currentTaskType ? this.currentTaskType.parameters : []
    },
    
    // 建議的 Cron 表達式
    suggestedCron() {
      return this.currentTaskType ? this.currentTaskType.suggestedCron : null
    },
    
    // 按分類分組的任務類型
    groupedJobTypes() {
      const groups = {}
      this.jobTypes.forEach(type => {
        if (!groups[type.category]) {
          groups[type.category] = {
            name: type.category,
            types: []
          }
        }
        groups[type.category].types.push(type)
      })
      return Object.values(groups)
    }
  },
  watch: {
    form: {
      handler(val) {
        this.$emit('input', val)
      },
      deep: true
    }
  },
  created() {
    this.loadJobTypes()
  },
  methods: {
    // 載入所有任務類型
    async loadJobTypes() {
      try {
        const response = await listJobTypes()
        this.jobTypes = response.data || []
      } catch (error) {
        console.error('載入任務類型失敗:', error)
        this.jobTypes = []
      }
    },
    
    // 選擇任務類型
    async handleTaskTypeChange(code) {
      try {
        const response = await getJobTypeByCode(code)
        this.currentTaskType = response.data
        
        // 自動填入 Bean 和方法
        this.form.beanName = this.currentTaskType.beanName
        this.form.methodName = this.currentTaskType.methodName
        
        // 清空參數
        this.form.params = {}
        
        // 初始化參數預設值
        this.currentTaskType.parameters.forEach(param => {
          if (param.example) {
            this.$set(this.form.params, param.name, param.example)
          }
        })
        
        // 使用建議的 Cron（如果有）
        if (this.currentTaskType.suggestedCron) {
          this.form.cronExpression = this.currentTaskType.suggestedCron
        }
        
        this.$message.success(`已選擇任務: ${this.currentTaskType.name}`)
      } catch (error) {
        this.$message.error('載入任務詳情失敗: ' + error.message)
      }
    },
    
    // 使用建議的 Cron
    useSuggestedCron() {
      this.form.cronExpression = this.suggestedCron
      this.$message.success('已套用建議的 Cron 表達式')
    }
  }
}
</script>

<style scoped>
.job-type-selector {
  width: 100%;
}

.parameters-section {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin: 15px 0;
}

.param-hint {
  font-size: 12px;
  margin-top: 5px;
}

.param-hint.required {
  color: #f56c6c;
}

.param-hint.optional {
  color: #909399;
}

.cron-suggestion {
  margin-top: 5px;
  font-size: 12px;
  color: #409eff;
}

.cron-suggestion code {
  background: #ecf5ff;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}
</style>
