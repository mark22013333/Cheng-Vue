<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="登入地址" prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          placeholder="請輸入登入地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="使用者名稱" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="請輸入使用者名稱"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="small" type="primary" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>

    </el-form>
    <el-table
      v-loading="loading"
      :data="list.slice((pageNum-1)*pageSize,pageNum*pageSize)"
      style="width: 100%;"
    >
      <el-table-column align="center" label="序號" type="index">
        <template #default="scope">
          <span>{{(pageNum - 1) * pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="會話編號" prop="tokenId"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="登入名稱" prop="userName"/>
      <el-table-column align="center" label="部門名稱" prop="deptName"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="主機" prop="ipaddr"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="登入地點" prop="loginLocation"/>
      <el-table-column align="center" label="瀏覽器" prop="browser"/>
      <el-table-column align="center" label="作業系統" prop="os"/>
      <el-table-column align="center" label="登入時間" prop="loginTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="el-icon-delete"
            @click="handleForceLogout(scope.row)"
            v-hasPermi="['monitor:online:forceLogout']"
          >強制登出
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="pageNum" :limit.sync="pageSize" />
  </div>
</template>

<script>
import {forceLogout, list} from "@/api/monitor/online"

export default {
  name: "Online",
  data() {
    return {
      // 遮罩層
      loading: true,
      // 總則數
      total: 0,
      // 表格數據
      list: [],
      pageNum: 1,
      pageSize: 10,
      // 查詢參數
      queryParams: {
        ipaddr: undefined,
        userName: undefined
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
      list(this.queryParams).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.pageNum = 1
      this.getList()
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 強制登出按鈕操作 */
    handleForceLogout(row) {
      this.$modal.confirm('是否確認強制登出名稱為"' + row.userName + '"的使用者？').then(function () {
        return forceLogout(row.tokenId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("強制登出成功")
      }).catch(() => {})
    }
  }
}
</script>

