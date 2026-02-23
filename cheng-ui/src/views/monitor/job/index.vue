<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="任務名稱" prop="jobName">
        <el-input
          v-model="queryParams.jobName"
          placeholder="請輸入任務名稱"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="任務組名" prop="jobGroup">
        <el-select v-model="queryParams.jobGroup" clearable placeholder="請選擇任務組名">
          <el-option
            v-for="dict in dict.type.sys_job_group"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任務狀態" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="請選擇任務狀態">
          <el-option
            v-for="dict in dict.type.sys_job_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <el-icon class="el-icon--left"><Search /></el-icon>
          搜尋
        </el-button>
        <el-button @click="resetQuery">
          <el-icon class="el-icon--left"><Refresh /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          @click="handleAdd"
          v-hasPermi="[MONITOR_JOB_ADD]"
        >
          <el-icon class="el-icon--left"><Plus /></el-icon>
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="[MONITOR_JOB_EDIT]"
        >
          <el-icon class="el-icon--left"><Edit /></el-icon>
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="[MONITOR_JOB_REMOVE]"
        >
          <el-icon class="el-icon--left"><Delete /></el-icon>
          刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          @click="handleExport"
          v-hasPermi="[MONITOR_JOB_EXPORT]"
        >
          <el-icon class="el-icon--left"><Download /></el-icon>
          匯出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          @click="handleJobLog"
          v-hasPermi="[MONITOR_JOB_QUERY]"
        >
          <el-icon class="el-icon--left"><Document /></el-icon>
          日誌
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange" stripe>
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column align="center" label="任務編號" prop="jobId" width="100"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="任務名稱" prop="jobName"/>
      <el-table-column align="center" label="任務組名" prop="jobGroup">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_job_group" :value="scope.row.jobGroup"/>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="呼叫目標字串" prop="invokeTarget"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="Cron執行表達式" prop="cronExpression"/>
      <el-table-column align="center" label="狀態">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            link
            @click="handleUpdate(scope.row)"
            v-hasPermi="[MONITOR_JOB_EDIT]"
          >
            <el-icon><Edit /></el-icon>
            修改
          </el-button>
          <el-button
            type="primary"
            link
            @click="handleDelete(scope.row)"
            v-hasPermi="[MONITOR_JOB_REMOVE]"
          >
            <el-icon><Delete /></el-icon>
            刪除
          </el-button>
          <el-dropdown @command="(command) => handleCommand(command, scope.row)"
                       v-if="checkPermi([MONITOR_JOB_CHANGE_STATUS, MONITOR_JOB_QUERY])">
            <el-button type="primary" link>
              <el-icon><DArrowRight /></el-icon>
              更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="handleRun"
                                  v-if="checkPermi([MONITOR_JOB_CHANGE_STATUS])">
                  <span>
                    <el-icon><CaretRight /></el-icon>
                    執行一次
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="handleView"
                                  v-if="checkPermi([MONITOR_JOB_QUERY])">
                  <span>
                    <el-icon><View /></el-icon>
                    任務詳細
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="handleJobLog"
                                  v-if="checkPermi([MONITOR_JOB_QUERY])">
                  <span>
                    <el-icon><Document /></el-icon>
                    呼叫日誌
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增或修改定時任務對話框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任務名稱" prop="jobName">
              <el-input v-model="form.jobName" placeholder="請輸入任務名稱"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任務分組" prop="jobGroup">
              <el-select 
                v-model="form.jobGroup" 
                placeholder="請選擇任務分組"
                @change="handleJobGroupChange">
                <el-option
                  v-for="dict in dict.type.sys_job_group"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="設定方式">
              <el-radio-group v-model="configMode" @change="handleConfigModeChange">
                <el-radio-button label="template">從範本選擇</el-radio-button>
                <el-radio-button label="manual">手動輸入</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <!-- 範本模式：任務類型選擇器 -->
          <el-col :span="24" v-if="configMode === 'template'">
            <el-form-item label="任務類型" prop="taskType">
              <el-select
                v-model="selectedTaskType"
                placeholder="請選擇任務類型"
                @change="handleTaskTypeChange"
                style="width: 100%"
                clearable>
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
                    <span style="float: right; color: #8492a6; font-size: 12px; margin-left: 10px">
                      {{ type.description }}
                    </span>
                  </el-option>
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 範本模式：動態參數表單 -->
          <el-col :span="24" v-if="configMode === 'template' && currentParameters.length > 0">
            <el-divider content-position="left">任務參數</el-divider>
            <el-form-item
              v-for="param in currentParameters"
              :key="param.name"
              :label="param.description"
              :required="param.required">
              <!-- SELECT 類型：下拉選單 -->
              <el-select
                v-if="param.type === 'SELECT'"
                v-model="taskParams[param.name]"
                :placeholder="`請選擇${param.description}`"
                style="width: 100%"
                clearable>
                <el-option
                  v-for="option in param.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value">
                  <span style="float: left">{{ option.label }}</span>
                  <span v-if="option.description" style="float: right; color: #8492a6; font-size: 12px">
                    {{ option.description }}
                  </span>
                </el-option>
              </el-select>
              <!-- DATE 類型：日期選擇器 -->
              <el-date-picker
                v-else-if="param.type === 'DATE'"
                v-model="taskParams[param.name]"
                type="date"
                :placeholder="`請選擇${param.description}`"
                style="width: 100%"
                value-format="yyyy-MM-dd"/>
              <!-- STRING 類型：文字輸入 -->
              <el-input
                v-else-if="param.type === 'STRING'"
                v-model="taskParams[param.name]"
                :placeholder="`範例: ${param.example}`"
                clearable>
                <template slot="prepend">{{ param.type }}</template>
              </el-input>
              <!-- TEXTAREA 類型：多行文字 -->
              <el-input
                v-else-if="param.type === 'TEXTAREA'"
                v-model="taskParams[param.name]"
                type="textarea"
                :rows="3"
                :placeholder="`範例: ${param.example}`"
                clearable/>
              <!-- NUMBER 類型：數字輸入 -->
              <el-input-number
                v-else-if="param.type === 'NUMBER' || param.type === 'INTEGER' || param.type === 'LONG'"
                v-model="taskParams[param.name]"
                :placeholder="`範例: ${param.example}`"
                style="width: 100%"
                controls-position="right"/>
              <!-- BOOLEAN 類型：開關 -->
              <el-switch
                v-else-if="param.type === 'BOOLEAN'"
                v-model="taskParams[param.name]"/>
              <!-- 其他類型：預設文字輸入 -->
              <el-input
                v-else
                v-model="taskParams[param.name]"
                :placeholder="`範例: ${param.example}`"
                clearable/>
              <div v-if="param.required" style="color: #909399; font-size: 12px; margin-top: 5px">
                <i class="el-icon-star-on"></i> 必填
              </div>
            </el-form-item>
          </el-col>

          <!-- 手動模式：呼叫方法輸入框 -->
          <el-col :span="24" v-if="configMode === 'manual'">
            <el-form-item prop="invokeTarget">
              <template #label>
                <span>
                  呼叫方法
                  <el-tooltip placement="top">
                    <template #content>
                      <div>
                        Bean呼叫示例：ryTask.ryParams('ry')
                        <br/>Class類呼叫示例：task.com.cheng.quartz.RyTask.ryParams('ry')
                        <br/>參數說明：支援字串，布林類型，長整型，浮點型，整型
                      </div>
                    </template>
                    <i class="el-icon-question"></i>
                  </el-tooltip>
                </span>
              </template>
              <el-input v-model="form.invokeTarget" placeholder="請輸入呼叫目標字串"/>
            </el-form-item>
          </el-col>

          <!-- 範本模式：顯示產生的呼叫目標（唯讀） -->
          <el-col :span="24" v-if="configMode === 'template'">
            <el-form-item label="呼叫目標">
              <el-input v-model="generatedInvokeTarget" disabled>
                <template #prepend>
                  <i class="el-icon-magic-stick"></i> 自動產生
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Cron表達式" prop="cronExpression">
              <el-input v-model="form.cronExpression" placeholder="請輸入Cron執行表達式">
                <template #append>
                  <el-button
                    v-if="configMode === 'template' && suggestedCron"
                    type="success"
                    @click="useSuggestedCron">
                    使用建議值
                    <i class="el-icon-magic-stick el-icon--right"></i>
                  </el-button>
                  <el-button type="primary" @click="handleShowCron">
                    產生表達式
                    <i class="el-icon-time el-icon--right"></i>
                  </el-button>
                </template>
              </el-input>
              <div v-if="configMode === 'template' && suggestedCron"
                   style="color: #409eff; font-size: 12px; margin-top: 5px">
                <i class="el-icon-time"></i> 建議值：<code
                style="background: #ecf5ff; padding: 2px 6px; border-radius: 3px">{{ suggestedCron }}</code>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="form.jobId !== undefined">
            <el-form-item label="狀態">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_job_status"
                  :key="dict.value"
                  :label="dict.value"
                >{{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="執行策略" prop="misfirePolicy">
              <el-radio-group v-model="form.misfirePolicy">
                <el-radio-button label="1">立即執行</el-radio-button>
                <el-radio-button label="2">執行一次</el-radio-button>
                <el-radio-button label="3">放棄執行</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否併發" prop="concurrent">
              <el-radio-group v-model="form.concurrent">
                <el-radio-button label="0">允許</el-radio-button>
                <el-radio-button label="1">禁止</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">確 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="openCron" append-to-body class="scrollbar" destroy-on-close title="Cron表達式產生器">
      <crontab @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>

    <!-- 任務日誌詳細 -->
    <el-dialog v-model="openView" append-to-body title="任務詳細" width="700px">
      <el-form ref="form" :model="form" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任務編號：">{{ form.jobId }}</el-form-item>
            <el-form-item label="任務名稱：">{{ form.jobName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任務分組：">{{ jobGroupFormat(form) }}</el-form-item>
            <el-form-item label="建立時間：">{{ form.createTime }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Cron表達式：">{{ form.cronExpression }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次執行時間：">{{ parseTime(form.nextValidTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="呼叫目標方法：">{{ form.invokeTarget }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任務狀態：">
              <div v-if="form.status == 0">正常</div>
              <div v-else-if="form.status == 1">暫停</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否併發：">
              <div v-if="form.concurrent == 0">允許</div>
              <div v-else-if="form.concurrent == 1">禁止</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="執行策略：">
              <div v-if="form.misfirePolicy == 0">預設策略</div>
              <div v-else-if="form.misfirePolicy == 1">立即執行</div>
              <div v-else-if="form.misfirePolicy == 2">執行一次</div>
              <div v-else-if="form.misfirePolicy == 3">放棄執行</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="openView = false">關 閉</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  MONITOR_JOB_ADD,
  MONITOR_JOB_CHANGE_STATUS,
  MONITOR_JOB_EDIT,
  MONITOR_JOB_EXPORT,
  MONITOR_JOB_QUERY,
  MONITOR_JOB_REMOVE
} from '@/constants/permissions'
import { Search, Refresh, Plus, Edit, Delete, Download, Document, DArrowRight, CaretRight, View } from '@element-plus/icons-vue'
import {addJob, changeJobStatus, delJob, getJob, listJob, runJob, updateJob} from "@/api/monitor/job"
import {listJobTypes, getJobTypeByCode, getTaskTypesByJobGroup} from "@/api/monitor/jobType"
import { reactive } from 'vue'
import { checkPermi } from '@/utils/permission'
import Crontab from '@/components/Crontab'

export default {
  setup() {
    return { MONITOR_JOB_ADD, MONITOR_JOB_CHANGE_STATUS, MONITOR_JOB_EDIT, MONITOR_JOB_EXPORT, MONITOR_JOB_QUERY, MONITOR_JOB_REMOVE }
  },
  components: { Search, Refresh, Plus, Edit, Delete, Download, Document, DArrowRight, CaretRight, View, Crontab },
  name: "Job",
  data() {
    return {
      // 字典數據
      dict: {
        type: {
          sys_job_group: [],
          sys_job_status: []
        }
      },
      // 設定模式：template(範本) 或 manual(手動)
      configMode: 'template',
      // 任務類型列表
      jobTypes: [],
      // 選中的任務類型
      selectedTaskType: '',
      // 當前任務類型詳情
      currentTaskType: null,
      // 任務參數
      taskParams: {},
      // 遮罩層
      loading: true,
      // 選中陣列
      ids: [],
      // 非單個禁用
      single: true,
      // 非多個禁用
      multiple: true,
      // 顯示搜尋條件
      showSearch: true,
      // 總則數
      total: 0,
      // 定時任務表格資料
      jobList: [],
      // 彈出層標題
      title: "",
      // 是否顯示彈出層
      open: false,
      // 是否顯示詳細彈出層
      openView: false,
      // 是否顯示Cron表達式彈出層
      openCron: false,
      // 傳入的表達式
      expression: "",
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined,
        jobGroup: undefined,
        status: undefined
      },
      // 表單參數
      form: {},
      // 表單校驗
      rules: {
        jobName: [
          {required: true, message: "任務名稱不能為空", trigger: "blur"}
        ],
        invokeTarget: [
          {required: true, message: "呼叫目標字串不能為空", trigger: "blur"}
        ],
        cronExpression: [
          {required: true, message: "Cron執行表達式不能為空", trigger: "blur"}
        ]
      }
    }
  },
  computed: {
    // 當前任務的參數列表（只顯示 visible 為 true 的參數）
    currentParameters() {
      if (!this.currentTaskType || !this.currentTaskType.parameters) {
        return []
      }

      // 過濾掉不可見的參數（例如：crawlerType 自動參數）
      // visible 為 false 時才隱藏
      return this.currentTaskType.parameters.filter(param => param.visible !== false)
    },

    // 所有參數列表（包含隱藏參數，用於產生 invokeTarget）
    allParameters() {
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
    },
    // 產生的呼叫目標字串
    generatedInvokeTarget() {
      if (!this.currentTaskType) {
        return ''
      }

      const {beanName, methodName} = this.currentTaskType

      // 收集所有有值的參數（包含隱藏參數）
      const paramsObject = {}
      if (this.allParameters && this.allParameters.length > 0) {
        this.allParameters.forEach(param => {
          const value = this.taskParams[param.name]
          // 只包含有值的參數（不包含 null、undefined、空字串）
          if (value !== null && value !== undefined && value !== '') {
            paramsObject[param.name] = value
          }
        })
      }

      // 將參數物件轉為 JSON 字串
      const paramsJson = JSON.stringify(paramsObject)

      // 組合格式：beanName.methodName('{"crawlerType":"CA103","mode":"today-only"}')
      return `${beanName}.${methodName}('${paramsJson}')`
    }
  },
  created() {
    // 載入字典數據
    const { sys_job_group, sys_job_status } = this.useDict('sys_job_group', 'sys_job_status')
    // 使用 reactive 確保響應性
    this.dict.type = reactive({
      sys_job_group,
      sys_job_status
    })

    this.getList()
    // 不再在頁面載入時載入所有任務類型
    // 改為在選擇 jobGroup 後動態載入對應的任務類型
  },
  methods: {
    checkPermi,
    /** 載入任務類型列表 */
    async loadJobTypes() {
      try {
        console.log('[loadJobTypes] 開始載入任務類型列表...')
        const response = await listJobTypes()
        console.log('[loadJobTypes] ✅ 成功載入任務類型:', response)

        // 判斷後端返回格式
        if (Array.isArray(response.data)) {
          // 情境 1: 後端直接回傳 List 陣列
          this.jobTypes = response.data
          console.log('[loadJobTypes] 📋 已載入任務類型數量 (陣列格式):', this.jobTypes.length)
        } else if (response.data && response.data.tasks) {
          // 情境 2: 後端回傳分類物件結構 { categories: [], tasks: { category1: [...], category2: [...] } }
          const allTasks = []
          Object.values(response.data.tasks).forEach(taskList => {
            allTasks.push(...taskList)
          })
          this.jobTypes = allTasks
          console.log('[loadJobTypes] 📋 已載入任務類型數量 (物件格式):', this.jobTypes.length)
        } else {
          this.jobTypes = []
          console.log('[loadJobTypes] ⚠️ 後端未返回任務類型數據')
        }
      } catch (error) {
        console.warn('[loadJobTypes] ⚠️ 載入任務類型失敗 (這不影響基本功能):', error)
        console.warn('[loadJobTypes] 錯誤詳情:', {
          message: error.message,
          response: error.response,
          config: error.config
        })
        // 如果後端沒有實現 jobType API，使用空陣列（手動模式仍可用）
        this.jobTypes = []
        // 不要顯示錯誤訊息給使用者，因為這是可選功能
      }
    },
    /** 處理任務分組變化（聯動載入任務類型） */
    async handleJobGroupChange(jobGroup) {
      console.log('[handleJobGroupChange] 任務分組變化:', jobGroup)
      
      // 清空任務類型選擇
      this.selectedTaskType = ''
      this.currentTaskType = null
      this.taskParams = {}
      this.jobTypes = []
      
      // 如果是範本模式且有選擇 jobGroup，載入對應的任務類型
      if (this.configMode === 'template' && jobGroup) {
        try {
          console.log('[handleJobGroupChange] 載入任務分組的任務類型...')
          const response = await getTaskTypesByJobGroup(jobGroup)
          
          if (Array.isArray(response.data)) {
            this.jobTypes = response.data
            console.log('[handleJobGroupChange] ✅ 載入成功，任務類型數量:', this.jobTypes.length)
          } else {
            this.jobTypes = []
            console.log('[handleJobGroupChange] ⚠️ 未返回任務類型數據')
          }
        } catch (error) {
          console.error('[handleJobGroupChange] ❌ 載入任務類型失敗:', error)
          this.jobTypes = []
          this.$modal.msgError('載入任務類型失敗')
        }
      }
    },
    /** 處理設定模式切換 */
    handleConfigModeChange(mode) {
      if (mode === 'template') {
        // 切換到範本模式，清空手動輸入的值
        this.selectedTaskType = ''
        this.currentTaskType = null
        this.taskParams = {}
        // 如果已選擇 jobGroup，載入對應的任務類型
        if (this.form.jobGroup) {
          this.handleJobGroupChange(this.form.jobGroup)
        }
      } else {
        // 切換到手動模式，保留 invokeTarget
      }
    },
    /** 處理任務類型選擇 */
    async handleTaskTypeChange(code) {
      if (!code) {
        this.currentTaskType = null
        this.taskParams = {}
        this.form.invokeTarget = ''
        this.form.cronExpression = ''
        return
      }

      try {
        const response = await getJobTypeByCode(code)
        this.currentTaskType = response.data

        // 清空參數
        this.taskParams = {}

        // 初始化參數預設值（包含隱藏參數）
        console.log('開始初始化參數預設值...')
        this.currentTaskType.parameters.forEach(param => {
          // 優先使用 defaultValue，然後是 example
          const defaultVal = param.defaultValue || param.example
          console.log(`參數 ${param.name} - 預設值:`, defaultVal, '是否隱藏:', param.visible === false)

          if (defaultVal) {
            this.taskParams[param.name] = defaultVal
            console.log(`已設定 ${param.name} = ${defaultVal}`)
          }
        })
        console.log('參數初始化完成，taskParams:', this.taskParams)

        // 使用建議的任務名稱（如果任務名稱為空且有預設名稱）
        if (this.currentTaskType.defaultJobName && !this.form.jobName) {
          this.form.jobName = this.currentTaskType.defaultJobName
        }

        // 使用建議的 Cron（如果有）
        if (this.currentTaskType.suggestedCron) {
          this.form.cronExpression = this.currentTaskType.suggestedCron
        }

        this.$message.success(`已選擇任務: ${this.currentTaskType.name}`)
      } catch (error) {
        this.$message.error('載入任務詳情失敗: ' + error.message)
      }
    },
    /** 使用建議的 Cron 表達式 */
    useSuggestedCron() {
      if (this.suggestedCron) {
        this.form.cronExpression = this.suggestedCron
        this.$message.success('已套用建議的 Cron 表達式')
      }
    },
    /** 查詢定時任務列表 */
    getList() {
      this.loading = true
      listJob(this.queryParams).then(response => {
        this.jobList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 任務組名字典翻譯
    jobGroupFormat(row, column) {
      return this.selectDictLabel(this.dict.type.sys_job_group, row.jobGroup)
    },
    // 取消按鈕
    cancel() {
      this.open = false
      this.reset()
    },
    // 表單重置
    reset() {
      this.form = {
        jobId: undefined,
        jobName: undefined,
        jobGroup: undefined,
        invokeTarget: undefined,
        cronExpression: undefined,
        misfirePolicy: 1,
        concurrent: 1,
        status: "0"
      }
      // 重置範本模式相關資料
      this.configMode = 'template'
      this.selectedTaskType = ''
      this.currentTaskType = null
      this.taskParams = {}
      this.resetForm("form")
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多選框選中資料
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.jobId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    // 更多操作觸發
    handleCommand(command, row) {
      switch (command) {
        case "handleRun":
          this.handleRun(row)
          break
        case "handleView":
          this.handleView(row)
          break
        case "handleJobLog":
          this.handleJobLog(row)
          break
        default:
          break
      }
    },
    // 任務狀態修改
    handleStatusChange(row) {
      let text = row.status === "0" ? "啟用" : "停用"
      this.$modal.confirm('確認要"' + text + '""' + row.jobName + '"任務嗎？').then(function () {
        return changeJobStatus(row.jobId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function () {
        row.status = row.status === "0" ? "1" : "0"
      })
    },
    /* 立即執行一次 */
    handleRun(row) {
      this.$modal.confirm('確認要立即執行一次"' + row.jobName + '"任務嗎？').then(function () {
        return runJob(row.jobId, row.jobGroup)
      }).then(() => {
        this.$modal.msgSuccess("執行成功")
      }).catch(() => {
      })
    },
    /** 任務詳細訊息 */
    handleView(row) {
      getJob(row.jobId).then(response => {
        this.form = response.data
        this.openView = true
      })
    },
    /** Cron表達式按鈕操作 */
    handleShowCron() {
      this.expression = this.form.cronExpression
      this.openCron = true
    },
    /** 確定後回傳值 */
    crontabFill(value) {
      this.form.cronExpression = value
    },
    /** 任務日誌列表查詢 */
    handleJobLog(row) {
      const jobId = row.jobId || 0
      this.$router.push('/cadm/monitor/job-log/index/' + jobId)
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增任務"
    },
    /** 修改按鈕操作 */
    async handleUpdate(row) {
      this.reset()
      const jobId = row.jobId || this.ids

      try {
        // 載入任務資料
        const response = await getJob(jobId)
        this.form = response.data
        this.open = true
        this.title = "修改任務"

        // 根據 jobGroup 載入對應的任務類型
        if (this.form.jobGroup) {
          await this.handleJobGroupChange(this.form.jobGroup)
        }

        // 優先使用 taskTypeCode（新方案）
        if (this.form.taskTypeCode) {
          console.log('使用 taskTypeCode 還原表單:', this.form.taskTypeCode)
          await this.restoreFormByTaskTypeCode(this.form.taskTypeCode, this.form.invokeTarget)
        } else {
          // 降級方案：從 invokeTarget 反向解析（舊資料）
          console.log('taskTypeCode 不存在，嘗試從 invokeTarget 解析')
          await this.parseInvokeTarget(this.form.invokeTarget)
        }
      } catch (error) {
        console.error('載入任務失敗:', error)
        this.$message.error('載入任務失敗')
      }
    },

    /** 根據 taskTypeCode 還原表單（新方案）*/
    async restoreFormByTaskTypeCode(taskTypeCode, invokeTarget) {
      try {
        // 載入任務類型詳情
        const response = await getJobTypeByCode(taskTypeCode)
        this.currentTaskType = response.data

        // 切換到範本模式
        this.configMode = 'template'
        this.selectedTaskType = taskTypeCode

        // 初始化所有參數的預設值（特別是隱藏參數）
        this.taskParams = {}
        if (this.currentTaskType.parameters) {
          this.currentTaskType.parameters.forEach(param => {
            const defaultVal = param.defaultValue || param.example
            if (defaultVal) {
              this.taskParams[param.name] = defaultVal
            }
          })
        }

        // 從 invokeTarget 解析參數值
        // 新格式：crawlerTask.run('{"crawlerType":"CA103","mode":"today-only"}')
        const regex = /^(\w+)\.(\w+)\('(.*)'\)$/
        const match = invokeTarget.match(regex)

        if (match && match[3].trim()) {
          const [, , , jsonStr] = match
          try {
            // 解析 JSON 字串
            const params = JSON.parse(jsonStr)

            // 將 JSON 物件的值覆蓋到 taskParams
            Object.keys(params).forEach(key => {
              this.taskParams[key] = params[key]
            })

            console.log('表單還原完成 - 參數:', this.taskParams)
          } catch (e) {
            console.error('解析 JSON 參數失敗:', e)
            // 如果 JSON 解析失敗，嘗試舊格式
            this.parseOldFormatParams(invokeTarget)
          }
        }
      } catch (error) {
        console.error('還原表單失敗:', error)
        this.$message.error('載入任務詳情失敗')
        // 降級到手動模式
        this.configMode = 'manual'
      }
    },

    /** 解析舊格式參數（向後相容）*/
    parseOldFormatParams(invokeTarget) {
      const regex = /^(\w+)\.(\w+)\((.*)\)$/
      const match = invokeTarget.match(regex)

      if (match && match[3].trim()) {
        const [, , , paramsStr] = match
        const paramValues = this.parseParamValues(paramsStr)

        // 只覆蓋可見參數的值（不覆蓋隱藏參數如 crawlerType）
        if (this.currentTaskType.parameters) {
          // 取得可見參數列表並排序
          const visibleParams = this.currentTaskType.parameters
            .filter(param => param.visible !== false)
            .sort((a, b) => (a.order || 0) - (b.order || 0))

          // 將解析出的值對應到可見參數
          visibleParams.forEach((param, index) => {
            if (index < paramValues.length && paramValues[index] !== 'null') {
              this.taskParams[param.name] = paramValues[index]
            }
          })
        }

        console.log('表單還原完成（舊格式）- 參數:', this.taskParams)
      }
    },

    /** 從 invokeTarget 解析任務類型和參數 */
    async parseInvokeTarget(invokeTarget) {
      if (!invokeTarget) return

      // 格式範例：crawlerTask.runWithMode('today-only')
      // 或：crawlerTask.runWithMode('date-range', '2024-01-01', '2024-12-31')
      const regex = /^(\w+)\.(\w+)\((.*)\)$/
      const match = invokeTarget.match(regex)

      if (!match) {
        // 不符合範本格式，使用手動模式
        this.configMode = 'manual'
        return
      }

      const [, beanName, methodName, paramsStr] = match

      // 解析參數值
      const paramValues = paramsStr.trim() ? this.parseParamValues(paramsStr) : []
      const paramCount = paramValues.length

      // 查找對應的任務類型（需要匹配 beanName、methodName 和參數數量）
      const candidateTypes = this.jobTypes.filter(type =>
        type.beanName === beanName && type.methodName === methodName
      )

      console.log('候選任務類型:', candidateTypes.map(t => ({
        code: t.code,
        name: t.name,
        paramCount: t.parameters ? t.parameters.length : 0
      })))
      console.log('實際參數數量:', paramCount)

      if (candidateTypes.length === 0) {
        // 找不到對應的任務類型，使用手動模式
        console.warn('找不到匹配的任務類型，切換到手動模式')
        this.configMode = 'manual'
        return
      }

      // 如果有多個候選，根據參數數量選擇最匹配的
      let taskType = null
      if (candidateTypes.length === 1) {
        taskType = candidateTypes[0]
        console.log('只有一個候選，直接使用:', taskType.code)
      } else {
        // 先嘗試精確匹配參數數量
        taskType = candidateTypes.find(type =>
          type.parameters && type.parameters.length === paramCount
        )

        if (!taskType) {
          // 找必填參數數量小於等於實際參數數量的任務類型
          const compatibleTypes = candidateTypes.filter(type => {
            if (!type.parameters) return false
            const requiredCount = type.parameters.filter(p => p.required).length
            return requiredCount <= paramCount && type.parameters.length >= paramCount
          })

          if (compatibleTypes.length > 0) {
            // 選擇參數數量最接近的
            taskType = compatibleTypes.reduce((prev, curr) => {
              const prevDiff = Math.abs(prev.parameters.length - paramCount)
              const currDiff = Math.abs(curr.parameters.length - paramCount)
              return currDiff < prevDiff ? curr : prev
            })
          }
        }

        // 如果還是找不到，使用第一個
        if (!taskType) {
          console.warn('無法精確匹配，使用第一個候選')
          taskType = candidateTypes[0]
        } else {
          console.log('匹配到任務類型:', taskType.code)
        }
      }

      // 切換到範本模式
      this.configMode = 'template'
      this.selectedTaskType = taskType.code

      // 暫存參數值（先用簡化的列表資料對應）
      let parsedParams = {}
      if (paramValues.length > 0 && taskType.parameters) {
        // 先按 order 排序參數
        const sortedParams = [...taskType.parameters].sort((a, b) => {
          const orderA = a.order || 0
          const orderB = b.order || 0
          return orderA - orderB
        })

        sortedParams.forEach((param, index) => {
          if (index < paramValues.length) {
            parsedParams[param.name] = paramValues[index]
          }
        })
      }

      console.log('解析完成 - 任務類型:', taskType.code, '暫存參數:', parsedParams)

      // 重新載入任務類型詳情（含完整的 options 等資訊）
      try {
        const response = await getJobTypeByCode(taskType.code)
        this.currentTaskType = response.data

        // 套用解析出來的參數值
        this.taskParams = {}
        Object.keys(parsedParams).forEach(key => {
          this.taskParams[key] = parsedParams[key]
        })

        console.log('載入任務詳情完成，參數已還原:', this.taskParams)
      } catch (error) {
        console.error('載入任務類型詳情失敗:', error)
        this.$message.error('載入任務詳情失敗')
        this.configMode = 'manual'
      }
    },

    /** 解析參數字串 */
    parseParamValues(paramsStr) {
      const values = []
      let current = ''
      let inString = false
      let escapeNext = false

      for (let i = 0; i < paramsStr.length; i++) {
        const char = paramsStr[i]

        if (escapeNext) {
          current += char
          escapeNext = false
          continue
        }

        if (char === '\\') {
          escapeNext = true
          continue
        }

        if (char === "'" || char === '"') {
          if (inString) {
            // 結束字串
            values.push(current)
            current = ''
            inString = false
          } else {
            // 開始字串
            inString = true
          }
          continue
        }

        if (char === ',' && !inString) {
          // 分隔符（非字串內）
          if (current.trim()) {
            values.push(current.trim())
          }
          current = ''
          continue
        }

        if (inString || char !== ' ') {
          current += char
        }
      }

      // 最後一個參數
      if (current.trim()) {
        values.push(current.trim())
      }

      return values
    },
    /** 提交按鈕 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 如果是範本模式，使用產生的 invokeTarget
          if (this.configMode === 'template') {
            console.log('開始驗證參數...')
            console.log('當前參數:', this.taskParams)
            console.log('需要驗證的參數列表:', this.currentParameters)

            // 驗證必填參數
            for (const param of this.currentParameters) {
              const value = this.taskParams[param.name]
              console.log(`驗證參數 ${param.name}:`, value, 'required:', param.required)

              if (param.required) {
                // 更嚴格的驗證：檢查值是否真的存在
                if (value === undefined || value === null || value === '') {
                  console.error(`參數 ${param.name} 驗證失敗！值為:`, value)
                  this.$message.error(`${param.description} 為必填參數`)
                  return
                }
                console.log(`參數 ${param.name} 驗證通過`)
              }
            }

            console.log('所有參數驗證通過')

            // 使用產生的 invokeTarget
            this.form.invokeTarget = this.generatedInvokeTarget

            // 儲存任務類型代號（用於編輯時還原）
            this.form.taskTypeCode = this.selectedTaskType
          }

          // 驗證 invokeTarget
          if (!this.form.invokeTarget || this.form.invokeTarget.trim() === '') {
            this.$message.error('呼叫目標不能為空')
            return
          }

          if (this.form.jobId != undefined) {
            updateJob(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addJob(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const jobIds = row.jobId || this.ids
      this.$modal.confirm('是否確認刪除定時任務編號為"' + jobIds + '"的資料選項？').then(function () {
        return delJob(jobIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
      }).catch(() => {
      })
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('monitor/job/export', {
        ...this.queryParams
      }, `job_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
