<template>
  <!-- 授權使用者 -->
  <el-dialog :visible.sync="visible" append-to-body title="選擇使用者" top="5vh" width="800px">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item label="使用者名稱" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="請輸入使用者名稱"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手機號碼" prop="phonenumber">
        <el-input
          v-model="queryParams.phonenumber"
          placeholder="請輸入手機號碼"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="small" type="primary" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row>
      <el-table @row-click="clickRow" ref="table" :data="userList" @selection-change="handleSelectionChange" height="260px">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column :show-overflow-tooltip="true" label="使用者名稱" prop="userName"/>
        <el-table-column :show-overflow-tooltip="true" label="使用者暱稱" prop="nickName"/>
        <el-table-column :show-overflow-tooltip="true" label="信箱" prop="email"/>
        <el-table-column :show-overflow-tooltip="true" label="手機" prop="phonenumber"/>
        <el-table-column align="center" label="狀態" prop="status">
          <template #default="scope">
            <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="建立時間" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
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
    </el-row>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleSelectUser">確 定</el-button>
      <el-button @click="visible = false">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {authUserSelectAll, unallocatedUserList} from "@/api/system/role"

export default {
  dicts: ['sys_normal_disable'],
  props: {
    // 角色編號
    roleId: {
      type: [Number, String]
    }
  },
  data() {
    return {
      // 遮罩層
      visible: false,
      // 選中陣列值
      userIds: [],
      // 總則數
      total: 0,
      // 未授權使用者數據
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
  methods: {
    // 顯示彈框
    show() {
      this.queryParams.roleId = this.roleId
      this.getList()
      this.visible = true
    },
    clickRow(row) {
      this.$refs.table.toggleRowSelection(row)
    },
    // 多選框選中數據
    handleSelectionChange(selection) {
      this.userIds = selection.map(item => item.userId)
    },
    // 查詢表數據
    getList() {
      unallocatedUserList(this.queryParams).then(res => {
        this.userList = res.rows
        this.total = res.total
      })
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
    /** 選擇授權使用者操作 */
    handleSelectUser() {
      const roleId = this.queryParams.roleId
      const userIds = this.userIds.join(",")
      if (userIds == "") {
        this.$modal.msgError("請選擇要分配的使用者")
        return
      }
      authUserSelectAll({ roleId: roleId, userIds: userIds }).then(res => {
        this.$modal.msgSuccess(res.msg)
        this.visible = false
        this.$emit("ok")
      })
    }
  }
}
</script>
