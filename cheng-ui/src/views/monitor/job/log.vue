<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="任務名稱" prop="jobName">
        <el-input
          v-model="queryParams.jobName"
          placeholder="請輸入任務名稱"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="任務組名" prop="jobGroup">
        <el-select
          v-model="queryParams.jobGroup"
          placeholder="請選擇任務組名"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.sys_job_group"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="執行狀態" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="請選擇執行狀態"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.sys_common_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="執行時間">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          end-placeholder="結束日期"
          start-placeholder="開始日期"
        ></el-date-picker>
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
          type="danger"
          plain
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['monitor:job:remove']"
        >
          <el-icon class="el-icon--left"><Delete /></el-icon>
          刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          @click="handleClean"
          v-hasPermi="['monitor:job:remove']"
        >
          <el-icon class="el-icon--left"><Delete /></el-icon>
          清除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          @click="handleExport"
          v-hasPermi="['monitor:job:export']"
        >
          <el-icon class="el-icon--left"><Download /></el-icon>
          匯出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          @click="handleClose"
        >
          <el-icon class="el-icon--left"><Close /></el-icon>
          關閉
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobLogList" @selection-change="handleSelectionChange" stripe>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column align="center" label="日誌編號" prop="jobLogId" width="80"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="任務名稱" prop="jobName"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="任務組名" prop="jobGroup">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_job_group" :value="scope.row.jobGroup"/>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="呼叫目標字串" prop="invokeTarget"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="日誌訊息" prop="jobMessage"/>
      <el-table-column align="center" label="執行狀態" prop="status">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_common_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="執行時間" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            type="primary"
            link
            @click="handleView(scope.row)"
            v-hasPermi="['monitor:job:query']"
          >
            <el-icon><View /></el-icon>
            詳細
          </el-button>
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

    <!-- 呼叫日誌詳細 -->
    <el-dialog v-model="open" append-to-body title="呼叫日誌詳細" width="700px">
      <el-form ref="form" :model="form" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="日誌序號：">{{ form.jobLogId }}</el-form-item>
            <el-form-item label="任務名稱：">{{ form.jobName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任務分組：">{{ form.jobGroup }}</el-form-item>
            <el-form-item label="執行時間：">{{ form.createTime }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="呼叫方法：">{{ form.invokeTarget }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="日誌訊息：">{{ form.jobMessage }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="執行狀態：">
              <div v-if="form.status == 0">正常</div>
              <div v-else-if="form.status == 1">失敗</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item v-if="form.status == 1" label="異常訊息：">{{ form.exceptionInfo }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">關 閉</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search, Refresh, Delete, Download, Close, View } from '@element-plus/icons-vue'
import {getJob} from "@/api/monitor/job"
import {cleanJobLog, delJobLog, listJobLog} from "@/api/monitor/jobLog"

export default {
  name: "JobLog",
  components: { Search, Refresh, Delete, Download, Close, View },
  dicts: ['sys_common_status', 'sys_job_group'],
  data() {
    return {
      // 遮罩層
      loading: true,
      // 選中陣列
      ids: [],
      // 非多個禁用
      multiple: true,
      // 顯示搜尋條件
      showSearch: true,
      // 總則數
      total: 0,
      // 呼叫日誌表格資料
      jobLogList: [],
      // 是否顯示彈出層
      open: false,
      // 日期範圍
      dateRange: [],
      // 表單參數
      form: {},
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined,
        jobGroup: undefined,
        status: undefined
      }
    }
  },
  created() {
    const jobId = this.$route.params && this.$route.params.jobId
    if (jobId !== undefined && jobId != 0) {
      getJob(jobId).then(response => {
        this.queryParams.jobName = response.data.jobName
        this.queryParams.jobGroup = response.data.jobGroup
        this.getList()
      })
    } else {
      this.getList()
    }
  },
  methods: {
    /** 查詢呼叫日誌列表 */
    getList() {
      this.loading = true
      listJobLog(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.jobLogList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    // 返回按鈕
    handleClose() {
      const obj = { path: "/monitor/job" }
      this.$tab.closeOpenPage(obj)
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多選框選中資料
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.jobLogId)
      this.multiple = !selection.length
    },
    /** 詳細按鈕操作 */
    handleView(row) {
      this.open = true
      this.form = row
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const jobLogIds = this.ids
      this.$modal.confirm('是否確認刪除呼叫日誌編號為"' + jobLogIds + '"的資料選項？').then(function () {
        return delJobLog(jobLogIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
      }).catch(() => {})
    },
    /** 清除按鈕操作 */
    handleClean() {
      this.$modal.confirm('是否確認清除所有呼叫日誌資料選項？').then(function () {
        return cleanJobLog()
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("清除成功")
      }).catch(() => {})
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('/monitor/jobLog/export', {
        ...this.queryParams
      }, `log_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
