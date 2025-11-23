<template>
  <div class="app-container line-user-container">
    <!-- 統計卡片區域 -->
    <stats-card :stats="stats" :loading="statsLoading" @refresh="getStats" />

    <!-- 每月加入圖表（當有時間範圍篩選時顯示） -->
    <monthly-join-chart v-if="dateRange && dateRange.length === 2" :date-range="dateRange" />

    <!-- 查詢區域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="85px">
      <el-form-item label="使用者ID" prop="lineUserId">
        <el-input
          v-model="queryParams.lineUserId"
          placeholder="請輸入LINE使用者ID"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="顯示名稱" prop="lineDisplayName">
        <el-input
          v-model="queryParams.lineDisplayName"
          placeholder="請輸入顯示名稱"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="綁定狀態" prop="bindStatus">
        <el-select v-model="queryParams.bindStatus" placeholder="綁定狀態" clearable style="width: 140px">
          <el-option label="未綁定" value="UNBOUND" />
          <el-option label="已綁定" value="BOUND" />
        </el-select>
      </el-form-item>
      <el-form-item label="關注狀態" prop="followStatus">
        <el-select v-model="queryParams.followStatus" placeholder="關注狀態" clearable style="width: 140px">
          <el-option label="關注中" value="FOLLOWING" />
          <el-option label="已取消" value="UNFOLLOWED" />
          <el-option label="黑名單" value="BLACKLISTED" />
        </el-select>
      </el-form-item>
      <el-form-item label="加入時間">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="開始日期"
          end-placeholder="結束日期"
          :picker-options="pickerOptions"
          @change="handleDateChange"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕區域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-upload"
          size="small"
          @click="handleImport"
          v-hasPermi="['line:user:import']"
        >
          匯入
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['line:user:export']"
        >
          匯出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['line:user:remove']"
        >
          刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-remove-outline"
          size="small"
          :disabled="multiple"
          @click="handleBatchAddBlacklist"
          v-hasPermi="['line:user:edit']"
        >
          批次加入黑名單
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-circle-check"
          size="small"
          :disabled="multiple"
          @click="handleBatchRemoveBlacklist"
          v-hasPermi="['line:user:edit']"
        >
          批次移除黑名單
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="頭像" align="center" width="80">
        <template #default="scope">
          <el-avatar :src="scope.row.linePictureUrl" size="default">
            <i class="el-icon-user-solid"></i>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="顯示名稱" align="center" prop="lineDisplayName" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="LINE ID" align="center" prop="lineUserId" :show-overflow-tooltip="true" min-width="150">
        <template #default="scope">
          <span>{{ scope.row.lineUserId }}</span>
          <el-button
            type="text"
            icon="el-icon-document-copy"
            size="small"
            @click="handleCopy(scope.row.lineUserId)"
            style="margin-left: 5px"
          ></el-button>
        </template>
      </el-table-column>
      <el-table-column label="關注狀態" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.followStatus === 'FOLLOWING'" type="success" size="small">關注中</el-tag>
          <el-tag v-else-if="scope.row.followStatus === 'BLACKLISTED'" type="danger" size="small">黑名單</el-tag>
          <el-tag v-else type="info" size="small">未關注</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="綁定狀態" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.bindStatus === 'BOUND'" type="primary" size="small">已綁定</el-tag>
          <el-tag v-else type="warning" size="small">未綁定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="互動統計" align="center" width="120">
        <template #default="scope">
          <el-tooltip placement="top">
            <div slot="content">
              發送：{{ scope.row.totalMessagesSent || 0 }}<br/>
              接收：{{ scope.row.totalMessagesReceived || 0 }}
            </div>
            <el-tag size="small">
              <i class="el-icon-s-comment"></i>
              {{ (scope.row.totalMessagesSent || 0) + (scope.row.totalMessagesReceived || 0) }}
            </el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="最後互動" align="center" prop="lastInteractionTime" width="160">
        <template #default="scope">
          <span v-if="scope.row.lastInteractionTime">{{ parseTime(scope.row.lastInteractionTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            v-hasPermi="['line:user:query']"
          >詳情</el-button>
          <el-dropdown size="small" @command="(command) => handleCommand(command, scope.row)">
            <el-button size="small" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="bind" icon="el-icon-link" v-hasPermi="['line:user:bind']">
                綁定
              </el-dropdown-item>
              <el-dropdown-item command="unbind" icon="el-icon-unlock" v-hasPermi="['line:user:bind']" v-if="scope.row.bindStatus === 'BOUND'">
                解綁
              </el-dropdown-item>
              <el-dropdown-item command="sync" icon="el-icon-refresh" v-hasPermi="['line:user:edit']">
                同步資料
              </el-dropdown-item>
              <el-dropdown-item
                command="addBlacklist"
                icon="el-icon-remove-outline"
                divided
                v-hasPermi="['line:user:edit']"
                v-if="scope.row.followStatus !== 'BLACKLISTED'"
              >
                加入黑名單
              </el-dropdown-item>
              <el-dropdown-item
                command="removeBlacklist"
                icon="el-icon-circle-check"
                v-hasPermi="['line:user:edit']"
                v-if="scope.row.followStatus === 'BLACKLISTED'"
              >
                移除黑名單
              </el-dropdown-item>
              <el-dropdown-item command="delete" icon="el-icon-delete" divided v-hasPermi="['line:user:remove']">
                刪除
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分頁 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 使用者詳情抽屜 -->
    <user-detail
      :visible.sync="detailVisible"
      :user-id="currentUserId"
      @close="detailVisible = false"
    />

    <!-- 綁定對話框 -->
    <bind-dialog
      :visible.sync="bindVisible"
      :line-user-id="currentLineUserId"
      @success="handleBindSuccess"
    />

    <!-- 匯入對話框 -->
    <import-dialog
      :visible.sync="importVisible"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script>
import { listUser, delUser, getUserStats, unbindUser, syncUserProfile, exportUser, addToBlacklist, removeFromBlacklist, batchAddToBlacklist, batchRemoveFromBlacklist } from '@/api/line/user'
import StatsCard from './components/StatsCard'
import UserDetail from './components/UserDetail'
import BindDialog from './components/BindDialog'
import ImportDialog from './components/ImportDialog'
import MonthlyJoinChart from './components/MonthlyJoinChart'

export default {
  name: 'LineUser',
  components: {
    StatsCard,
    UserDetail,
    BindDialog,
    ImportDialog,
    MonthlyJoinChart
  },
  data() {
    return {
      // 載入狀態
      loading: true,
      statsLoading: true,
      // 選中陣列
      ids: [],
      // 選中的 LINE 使用者 ID
      lineUserIds: [],
      // 非單個停用
      single: true,
      // 非多個停用
      multiple: true,
      // 顯示搜尋條件
      showSearch: true,
      // 總條數
      total: 0,
      // 使用者列表
      userList: [],
      // 統計資料
      stats: {},
      // 日期範圍
      dateRange: [],
      // 日期選擇器選項
      pickerOptions: {
        disabledDate(time) {
          // 限制只能選擇今天及之前的日期
          return time.getTime() > Date.now()
        },
        shortcuts: [
          {
            text: '最近一週',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近一個月',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近三個月',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近一年',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 365)
              picker.$emit('pick', [start, end])
            }
          }
        ]
      },
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lineUserId: undefined,
        lineDisplayName: undefined,
        bindStatus: undefined,
        followStatus: undefined
      },
      // 使用者詳情
      detailVisible: false,
      currentUserId: null,
      // 綁定對話框
      bindVisible: false,
      currentLineUserId: null,
      // 匯入對話框
      importVisible: false
    }
  },
  created() {
    this.getList()
    this.getStats()
  },
  methods: {
    /** 查詢使用者列表 */
    getList() {
      this.loading = true
      listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.userList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查詢統計資料 */
    getStats() {
      this.statsLoading = true
      getUserStats().then(response => {
        this.stats = response.data
        this.statsLoading = false
      })
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      // 驗證日期範圍
      if (!this.validateDateRange()) {
        return
      }
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 日期變更處理 */
    handleDateChange(value) {
      if (value && value.length === 2) {
        this.validateDateRange()
      }
    },
    /** 驗證日期範圍 */
    validateDateRange() {
      if (!this.dateRange || this.dateRange.length !== 2) {
        return true
      }

      const startDate = new Date(this.dateRange[0])
      const endDate = new Date(this.dateRange[1])
      const today = new Date()
      today.setHours(23, 59, 59, 999)

      // 檢查起始日期是否晚於結束日期
      if (startDate > endDate) {
        this.$message.warning('開始日期不能晚於結束日期')
        this.dateRange = []
        return false
      }

      // 檢查日期是否超過今天
      if (endDate > today) {
        this.$message.warning('結束日期不能超過今天')
        this.dateRange = []
        return false
      }

      // 計算日期範圍（天數）
      const diffTime = Math.abs(endDate - startDate)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      const fiveYearsInDays = 365 * 5

      // 檢查是否超過五年
      if (diffDays > fiveYearsInDays) {
        this.$message.warning('查詢範圍不能超過五年')
        this.dateRange = []
        return false
      }

      return true
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 多選框選中資料 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.lineUserIds = selection.map(item => item.lineUserId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('line/user/export', {
        ...this.queryParams
      }, `line_user_${new Date().getTime()}.xlsx`)
    },
    /** 匯入按鈕操作 */
    handleImport() {
      this.importVisible = true
    },
    /** 匯入成功Callback */
    handleImportSuccess(result) {
      this.importVisible = false
      this.getList()
      this.getStats()

      // 顯示匯入結果
      const h = this.$createElement
      const message = h('div', null, [
        h('p', null, `總計：${result.totalCount} 筆`),
        h('p', { style: 'color: #67C23A' }, `成功：${result.successCount} 筆（新增：${result.newCount}，更新：${result.updateCount}）`),
        result.failCount > 0 ? h('p', { style: 'color: #F56C6C' }, `失敗：${result.failCount} 筆`) : null,
        result.failDetails && result.failDetails.length > 0 ? h('div', { style: 'margin-top: 10px; max-height: 200px; overflow-y: auto' }, [
          h('p', { style: 'font-weight: bold' }, '失敗詳情：'),
          ...result.failDetails.slice(0, 10).map(detail =>
            h('p', { style: 'font-size: 12px' }, `行 ${detail.rowNumber}: ${detail.lineUserId} - ${detail.reason}`)
          ),
          result.failDetails.length > 10 ? h('p', { style: 'font-size: 12px; color: #909399' }, `... 還有 ${result.failDetails.length - 10} 筆失敗記錄`) : null
        ]) : null
      ])

      this.$notify({
        title: '匯入完成',
        message: message,
        type: result.failCount > 0 ? 'warning' : 'success',
        duration: 10000,
        dangerouslyUseHTMLString: false
      })
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否確認刪除選中的 LINE 使用者？').then(() => {
        return delUser(ids)
      }).then(() => {
        this.getList()
        this.getStats()
        this.$modal.msgSuccess('刪除成功')
      }).catch(() => {})
    },
    /** 查看詳情 */
    handleDetail(row) {
      this.currentUserId = row.id
      this.detailVisible = true
    },
    /** 綁定成功Callback */
    handleBindSuccess() {
      this.bindVisible = false
      this.getList()
      this.getStats()
    },
    /** 複製 LINE ID */
    handleCopy(text) {
      this.$copyText(text).then(() => {
        this.$message.success('已複製到剪貼簿')
      }).catch(() => {
        this.$message.error('複製失敗')
      })
    },
    /** 下拉選單操作 */
    handleCommand(command, row) {
      switch (command) {
        case 'bind':
          this.currentLineUserId = row.lineUserId
          this.bindVisible = true
          break
        case 'unbind':
          this.handleUnbind(row)
          break
        case 'sync':
          this.handleSync(row)
          break
        case 'addBlacklist':
          this.handleAddToBlacklist(row)
          break
        case 'removeBlacklist':
          this.handleRemoveFromBlacklist(row)
          break
        case 'delete':
          this.handleDelete(row)
          break
      }
    },
    /** 解除綁定 */
    handleUnbind(row) {
      this.$modal.confirm('是否確認解除綁定？').then(() => {
        return unbindUser(row.lineUserId)
      }).then(() => {
        this.getList()
        this.getStats()
        this.$modal.msgSuccess('解除綁定成功')
      }).catch(() => {})
    },
    /** 同步資料 */
    handleSync(row) {
      this.$modal.confirm('是否從 LINE API 同步使用者最新資料？').then(() => {
        return syncUserProfile(row.lineUserId, null)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('同步成功')
      }).catch(() => {})
    },
    /** 加入黑名單 */
    handleAddToBlacklist(row) {
      const displayName = row.lineDisplayName || row.lineUserId
      const h = this.$createElement
      this.$confirm('', '警告', {
        message: h('div', null, [
          h('p', null, `是否將使用者「${displayName}」加入黑名單？`),
          h('p', { style: 'color: #F56C6C; margin-top: 10px; font-weight: bold;' },
            '⚠️ 加入後該使用者將無法參與活動，也不會收到任何訊息。')
        ]),
        confirmButtonText: '確認加入',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }).then(() => {
        return addToBlacklist(row.lineUserId)
      }).then(() => {
        this.getList()
        this.getStats()
        this.$modal.msgSuccess('已加入黑名單')
      }).catch(() => {})
    },
    /** 移除黑名單 */
    handleRemoveFromBlacklist(row) {
      const displayName = row.lineDisplayName || row.lineUserId
      this.$modal.confirm(`是否將使用者「${displayName}」從黑名單移除？`, '提示').then(() => {
        return removeFromBlacklist(row.lineUserId)
      }).then(() => {
        this.getList()
        this.getStats()
        this.$modal.msgSuccess('已移除黑名單')
      }).catch(() => {})
    },
    /** 批次加入黑名單 */
    handleBatchAddBlacklist() {
      if (this.lineUserIds.length === 0) {
        this.$modal.msgWarning('請選擇要加入黑名單的使用者')
        return
      }

      const h = this.$createElement
      this.$confirm('', '警告', {
        message: h('div', null, [
          h('p', null, `是否將選中的 ${this.lineUserIds.length} 位使用者加入黑名單？`),
          h('p', { style: 'color: #F56C6C; margin-top: 10px; font-weight: bold;' },
            '⚠️ 加入後這些使用者將無法參與活動，也不會收到任何訊息。')
        ]),
        confirmButtonText: '確認加入',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }).then(() => {
        return batchAddToBlacklist(this.lineUserIds)
      }).then(response => {
        this.getList()
        this.getStats()
        this.$modal.msgSuccess(response.msg || '批次加入黑名單成功')
      }).catch(() => {})
    },
    /** 批次移除黑名單 */
    handleBatchRemoveBlacklist() {
      if (this.lineUserIds.length === 0) {
        this.$modal.msgWarning('請選擇要移除黑名單的使用者')
        return
      }

      this.$modal.confirm(
        `是否將選中的 ${this.lineUserIds.length} 位使用者從黑名單移除？`
      ).then(() => {
        return batchRemoveFromBlacklist(this.lineUserIds)
      }).then(response => {
        this.getList()
        this.getStats()
        this.$modal.msgSuccess(response.msg || '批次移除黑名單成功')
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.line-user-container {
  padding: 20px;
}
</style>
