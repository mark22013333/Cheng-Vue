<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="75px">
      <el-form-item label="IP位置" prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          placeholder="請輸入IP位置"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="使用者名稱" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="請輸入使用者名稱"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="登入狀態"
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
      <el-form-item label="登入時間">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange"
          range-separator="-"
          end-placeholder="結束日期"
          start-placeholder="開始日期"
          :default-time="['00:00:00', '23:59:59']"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="small" type="primary" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['monitor:logininfor:remove']"
        >刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          @click="handleClean"
          v-hasPermi="['monitor:logininfor:remove']"
        >清除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-unlock"
          size="small"
          :disabled="single"
          @click="handleUnlock"
          v-hasPermi="['monitor:logininfor:unlock']"
        >解鎖
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['monitor:logininfor:export']"
        >匯出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column align="center" label="訪問編號" prop="infoId"/>
      <el-table-column :show-overflow-tooltip="true" :sort-orders="['descending', 'ascending']" align="center" label="使用者名稱" prop="userName"
                       sortable="custom"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="IP位置" prop="ipaddr" width="130"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="登入地點" prop="loginLocation"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="瀏覽器" prop="browser"/>
      <el-table-column align="center" label="作業系統" prop="os"/>
      <el-table-column align="center" label="登入狀態" prop="status">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_common_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="操作訊息" prop="msg"/>
      <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="登入日期" prop="loginTime"
                       sortable="custom" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
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
  </div>
</template>

<script>
import {cleanLogininfor, delLogininfor, list, unlockLogininfor} from "@/api/monitor/logininfor"

export default {
  name: "Logininfor",
  dicts: ['sys_common_status'],
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
      // 選擇使用者名
      selectName: "",
      // 顯示搜尋條件
      showSearch: true,
      // 總則數
      total: 0,
      // 表格數據
      list: [],
      // 日期範圍
      dateRange: [],
      // 預設排序
      defaultSort: { prop: "loginTime", order: "descending" },
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ipaddr: undefined,
        userName: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查詢登入日誌列表 */
    getList() {
      this.loading = true
      list(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.list = response.rows
          this.total = response.total
          this.loading = false
        }
      )
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
      this.queryParams.pageNum = 1
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order)
    },
    /** 多選框選中數據 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.infoId)
      this.single = selection.length!=1
      this.multiple = !selection.length
      this.selectName = selection.map(item => item.userName)
    },
    /** 排序觸發事件 */
    handleSortChange(column, prop, order) {
      this.queryParams.orderByColumn = column.prop
      this.queryParams.isAsc = column.order
      this.getList()
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const infoIds = row.infoId || this.ids
      this.$modal.confirm('是否確認刪除訪問編號為"' + infoIds + '"的數據項？').then(function () {
        return delLogininfor(infoIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
      }).catch(() => {})
    },
    /** 清除按鈕操作 */
    handleClean() {
      this.$modal.confirm('是否確認清除所有登入日誌數據項？').then(function () {
        return cleanLogininfor()
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("清除成功")
      }).catch(() => {})
    },
    /** 解鎖按鈕操作 */
    handleUnlock() {
      const username = this.selectName
      this.$modal.confirm('是否確認解鎖使用者"' + username + '"數據項?').then(function () {
        return unlockLogininfor(username)
      }).then(() => {
        this.$modal.msgSuccess("使用者" + username + "解鎖成功")
      }).catch(() => {})
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('monitor/logininfor/export', {
        ...this.queryParams
      }, `logininfor_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

