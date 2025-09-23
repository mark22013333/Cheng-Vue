<template>
  <div class="app-container">
     <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
       <el-form-item label="使用者名稱" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="請輸入使用者名稱"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
       <el-form-item label="手機號碼" prop="phonenumber">
        <el-input
          v-model="queryParams.phonenumber"
          placeholder="請輸入手機號碼"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
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
          @click="openSelectUser"
          v-hasPermi="['system:role:add']"
        >新增使用者
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-circle-close"
          size="mini"
          :disabled="multiple"
          @click="cancelAuthUserAll"
          v-hasPermi="['system:role:remove']"
        >批次取消授權
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          size="mini"
          @click="handleClose"
        >關閉
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :show-overflow-tooltip="true" label="使用者名稱" prop="userName"/>
      <el-table-column :show-overflow-tooltip="true" label="使用者暱稱" prop="nickName"/>
      <el-table-column :show-overflow-tooltip="true" label="信箱" prop="email"/>
      <el-table-column :show-overflow-tooltip="true" label="手機" prop="phonenumber"/>
      <el-table-column align="center" label="狀態" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="建立時間" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-circle-close"
            @click="cancelAuthUser(scope.row)"
            v-hasPermi="['system:role:remove']"
          >取消授權
          </el-button>
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
    <select-user ref="select" :roleId="queryParams.roleId" @ok="handleQuery" />
  </div>
</template>

<script>
import {allocatedUserList, authUserCancel, authUserCancelAll} from "@/api/system/role"
import selectUser from "./selectUser"

export default {
  name: "AuthUser",
  dicts: ['sys_normal_disable'],
  components: { selectUser },
  data() {
    return {
      // 遮罩層
      loading: true,
      // 選中使用者組
      userIds: [],
      // 非多個禁用
      multiple: true,
      // 顯示搜尋條件
      showSearch: true,
      // 總則數
      total: 0,
      // 使用者表格數據
      userList: [],
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        roleId: undefined,
        userName: undefined,
        phonenumber: undefined
      }
    }
  },
  created() {
    const roleId = this.$route.params && this.$route.params.roleId
    if (roleId) {
      this.queryParams.roleId = roleId
      this.getList()
    }
  },
  methods: {
    /** 查詢授權使用者列表 */
    getList() {
      this.loading = true
      allocatedUserList(this.queryParams).then(response => {
          this.userList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    // 返回按鈕
    handleClose() {
      const obj = { path: "/system/role" }
      this.$tab.closeOpenPage(obj)
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
      this.userIds = selection.map(item => item.userId)
      this.multiple = !selection.length
    },
    /** 打開授權使用者表彈窗 */
    openSelectUser() {
      this.$refs.select.show()
    },
    /** 取消授權按鈕操作 */
    cancelAuthUser(row) {
      const roleId = this.queryParams.roleId
      this.$modal.confirm('確認要取消該使用者"' + row.userName + '"角色嗎？').then(function () {
        return authUserCancel({ userId: row.userId, roleId: roleId })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("取消授權成功")
      }).catch(() => {})
    },
    /** 批次取消授權按鈕操作 */
    cancelAuthUserAll(row) {
      const roleId = this.queryParams.roleId
      const userIds = this.userIds.join(",")
      this.$modal.confirm('是否取消選中使用者授權數據項？').then(function () {
        return authUserCancelAll({ roleId: roleId, userIds: userIds })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("取消授權成功")
      }).catch(() => {})
    }
  }
}
</script>
