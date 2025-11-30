<template>
  <div class="app-container line-user-container">
    <!-- 統計卡片區域 -->
    <stats-card :stats="stats" :loading="statsLoading" @refresh="getStats" />

    <!-- 每月加入圖表（當有時間範圍篩選時顯示） -->
    <monthly-join-chart v-if="dateRange && dateRange.length === 2" :date-range="dateRange" />

    <!-- 查詢區域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="85px">
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
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="開始日期"
          end-placeholder="結束日期"
          :disabled-date="disabledDate"
          :shortcuts="dateShortcuts"
          @change="handleDateChange"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕區域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          :icon="Upload"
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
          :icon="Download"
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
          :icon="Delete"
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
          :icon="Remove"
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
          :icon="CircleCheck"
          :disabled="multiple"
          @click="handleBatchRemoveBlacklist"
          v-hasPermi="['line:user:edit']"
        >
          批次移除黑名單
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="頭像" align="center" width="80">
        <template #default="scope">
          <el-avatar :src="scope.row.linePictureUrl" size="default">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="顯示名稱" align="center" prop="lineDisplayName" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="LINE ID" align="center" prop="lineUserId" :show-overflow-tooltip="true" min-width="150">
        <template #default="scope">
          <span>{{ scope.row.lineUserId }}</span>
          <el-button
            type="text"
            :icon="DocumentCopy"
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
            <template #content>
              <div>
                發送：{{ scope.row.totalMessagesSent || 0 }}<br/>
                接收：{{ scope.row.totalMessagesReceived || 0 }}
              </div>
            </template>
            <el-tag size="small">
              <el-icon><Comment /></el-icon>
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
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width operation-column" fixed="right">
        <template #default="scope">
          <el-button v-if="checkPermi(['line:user:query'])" link type="primary" :icon="View" @click="handleDetail(scope.row)">詳情</el-button>
          <el-dropdown @command="(command) => handleCommand(command, scope.row)">
            <el-button link type="primary" :icon="DArrowRight">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
              <el-dropdown-item v-if="checkPermi(['line:user:bind'])" command="bind" :icon="Link">
                綁定
              </el-dropdown-item>
              <el-dropdown-item v-if="checkPermi(['line:user:bind']) && scope.row.bindStatus === 'BOUND'" command="unbind" :icon="Unlock">
                解綁
              </el-dropdown-item>
              <el-dropdown-item v-if="checkPermi(['line:user:edit'])" command="sync" :icon="Refresh">
                同步資料
              </el-dropdown-item>
              <el-dropdown-item
                v-if="checkPermi(['line:user:edit']) && scope.row.followStatus !== 'BLACKLISTED'"
                command="addBlacklist"
                :icon="Remove"
                divided
              >
                加入黑名單
              </el-dropdown-item>
              <el-dropdown-item
                v-if="checkPermi(['line:user:edit']) && scope.row.followStatus === 'BLACKLISTED'"
                command="removeBlacklist"
                :icon="CircleCheck"
              >
                移除黑名單
              </el-dropdown-item>
              <el-dropdown-item v-if="checkPermi(['line:user:remove'])" command="delete" :icon="Delete" divided>
                刪除
              </el-dropdown-item>
              </el-dropdown-menu>
            </template>
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
      v-model:visible="detailVisible"
      :user-id="currentUserId"
      @close="detailVisible = false"
    />

    <!-- 綁定對話框 -->
    <bind-dialog
      v-model:visible="bindVisible"
      :line-user-id="currentLineUserId"
      @success="handleBindSuccess"
    />

    <!-- 匯入對話框 -->
    <import-dialog
      v-model:visible="importVisible"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script>
import { h } from 'vue'
import { listUser, delUser, getUserStats, unbindUser, syncUserProfile, exportUser, addToBlacklist, removeFromBlacklist, batchAddToBlacklist, batchRemoveFromBlacklist } from '@/api/line/user'
import {
  Search, Refresh, Upload, Download, Delete, Remove, CircleCheck,
  UserFilled, DocumentCopy, Comment, View, DArrowRight, Link, Unlock, RemoveFilled
} from '@element-plus/icons-vue'
import StatsCard from './components/StatsCard'
import UserDetail from './components/UserDetail'
import BindDialog from './components/BindDialog'
import ImportDialog from './components/ImportDialog'
import MonthlyJoinChart from './components/MonthlyJoinChart'
import { checkPermi } from '@/utils/permission'

export default {
  name: 'LineUser',
  components: {
    Comment,
    StatsCard,
    UserDetail,
    BindDialog,
    ImportDialog,
    MonthlyJoinChart
  },
  setup() {
    return {
      Search, Refresh, Upload, Download, Delete, Remove, CircleCheck,
      UserFilled, DocumentCopy, Comment, View, DArrowRight, Link, Unlock, RemoveFilled
    }
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
      // 日期快捷選項
      dateShortcuts: [
        {
          text: '最近一週',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          }
        },
        {
          text: '最近一個月',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          }
        },
        {
          text: '最近三個月',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
          }
        },
        {
          text: '最近一年',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 365)
            return [start, end]
          }
        },
        {
          text: '最近三年',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 365 * 3)
            return [start, end]
          }
        },
        {
          text: '最近五年',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 365 * 5)
            return [start, end]
          }
        }
      ],
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
    /** 權限檢查 */
    checkPermi,
    /** 禁用未來日期 */
    disabledDate(time) {
      return time.getTime() > Date.now()
    },
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
    async handleCopy(text) {
      try {
        await navigator.clipboard.writeText(text)
        this.$message.success('已複製到剪貼簿')
      } catch (error) {
        console.error('複製失敗:', error)
        this.$message.error('複製失敗')
      }
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
      this.$confirm(
        h('div', null, [
          h('p', null, `是否將使用者「${displayName}」加入黑名單？`),
          h('p', { style: 'color: #F56C6C; margin-top: 10px; font-weight: bold;' },
            '⚠️ 加入後該使用者將無法參與活動，也不會收到任何訊息。')
        ]),
        '警告',
        {
          confirmButtonText: '確認加入',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: false
        }
      ).then(() => {
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
