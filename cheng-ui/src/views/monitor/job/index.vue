<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="任務名稱" prop="jobName">
        <el-input
          v-model="queryParams.jobName"
          placeholder="請輸入任務名稱"
          clearable
          @keyup.enter.native="handleQuery"
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
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['monitor:job:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['monitor:job:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['monitor:job:remove']"
        >刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['monitor:job:export']"
        >匯出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-s-operation"
          size="mini"
          @click="handleJobLog"
          v-hasPermi="['monitor:job:query']"
        >日誌
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column align="center" label="任務編號" prop="jobId" width="100"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="任務名稱" prop="jobName"/>
      <el-table-column align="center" label="任務組名" prop="jobGroup">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_job_group" :value="scope.row.jobGroup"/>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="呼叫目標字串" prop="invokeTarget"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="Cron執行表達式" prop="cronExpression"/>
      <el-table-column align="center" label="狀態">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['monitor:job:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['monitor:job:remove']"
          >刪除
          </el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['monitor:job:changeStatus', 'monitor:job:query']">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleRun" icon="el-icon-caret-right"
                                v-hasPermi="['monitor:job:changeStatus']">執行一次
              </el-dropdown-item>
              <el-dropdown-item command="handleView" icon="el-icon-view"
                                v-hasPermi="['monitor:job:query']">任務詳細
              </el-dropdown-item>
              <el-dropdown-item command="handleJobLog" icon="el-icon-s-operation"
                                v-hasPermi="['monitor:job:query']">呼叫日誌
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增或修改定時任務對話框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任務名稱" prop="jobName">
              <el-input v-model="form.jobName" placeholder="請輸入任務名稱"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任務分組" prop="jobGroup">
              <el-select v-model="form.jobGroup" placeholder="請選擇任務分組">
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
            <el-form-item prop="invokeTarget">
              <span slot="label">
                呼叫方法
                <el-tooltip placement="top">
                  <div slot="content">
                    Bean呼叫示例：ryTask.ryParams('ry')
                    <br/>Class類呼叫示例：task.com.cheng.quartz.RyTask.ryParams('ry')
                    <br/>參數說明：支援字串，布林類型，長整型，浮點型，整型
                  </div>
                  <i class="el-icon-question"></i>
                </el-tooltip>
              </span>
              <el-input v-model="form.invokeTarget" placeholder="請輸入呼叫目標字串"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Cron表達式" prop="cronExpression">
              <el-input v-model="form.cronExpression" placeholder="請輸入Cron執行表達式">
                <template slot="append">
                  <el-button type="primary" @click="handleShowCron">
                    產生表達式
                    <i class="el-icon-time el-icon--right"></i>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="form.jobId !== undefined">
            <el-form-item label="狀態">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_job_status"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="執行策略" prop="misfirePolicy">
              <el-radio-group v-model="form.misfirePolicy" size="small">
                <el-radio-button label="1">立即執行</el-radio-button>
                <el-radio-button label="2">執行一次</el-radio-button>
                <el-radio-button label="3">放棄執行</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否併發" prop="concurrent">
              <el-radio-group v-model="form.concurrent" size="small">
                <el-radio-button label="0">允許</el-radio-button>
                <el-radio-button label="1">禁止</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="openCron" append-to-body class="scrollbar" destroy-on-close title="Cron表達式產生器">
      <crontab @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>

    <!-- 任務日誌詳細 -->
    <el-dialog :visible.sync="openView" append-to-body title="任務詳細" width="700px">
      <el-form ref="form" :model="form" label-width="120px" size="mini">
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
      <div slot="footer" class="dialog-footer">
        <el-button @click="openView = false">關 閉</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addJob, changeJobStatus, delJob, getJob, listJob, runJob, updateJob} from "@/api/monitor/job"
import Crontab from '@/components/Crontab'

export default {
  components: { Crontab },
  name: "Job",
  dicts: ['sys_job_group', 'sys_job_status'],
  data() {
    return {
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
      // 定時任務表格數據
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
  created() {
    this.getList()
  },
  methods: {
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
    // 多選框選中數據
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
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0"
      })
    },
    /* 立即執行一次 */
    handleRun(row) {
      this.$modal.confirm('確認要立即執行一次"' + row.jobName + '"任務嗎？').then(function () {
        return runJob(row.jobId, row.jobGroup)
      }).then(() => {
        this.$modal.msgSuccess("執行成功")
      }).catch(() => {})
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
      this.$router.push('/monitor/job-log/index/' + jobId)
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增任務"
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset()
      const jobId = row.jobId || this.ids
      getJob(jobId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改任務"
      })
    },
    /** 提交按鈕 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
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
      this.$modal.confirm('是否確認刪除定時任務編號為"' + jobIds + '"的數據項？').then(function () {
        return delJob(jobIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
      }).catch(() => {})
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
