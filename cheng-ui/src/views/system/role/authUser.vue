<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true">
      <el-form-item label="使用者名稱" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="請輸入使用者名稱"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手機號碼" prop="phonenumber">
        <el-input
          v-model="queryParams.phonenumber"
          placeholder="請輸入手機號碼"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="openSelectUser"
          v-hasPermi="[SYSTEM_ROLE_ADD]"
        >新增使用者
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="CircleClose"
          :disabled="multiple"
          @click="cancelAuthUserAll"
          v-hasPermi="[SYSTEM_ROLE_REMOVE]"
        >批量取消授權
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Close"
          @click="handleClose"
        >關閉
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="使用者名稱" prop="userName" :show-overflow-tooltip="true"/>
      <el-table-column label="使用者暱稱" prop="nickName" :show-overflow-tooltip="true"/>
      <el-table-column label="電子郵件" prop="email" :show-overflow-tooltip="true"/>
      <el-table-column label="手機" prop="phonenumber" :show-overflow-tooltip="true"/>
      <el-table-column label="狀態" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="建立時間" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="CircleClose" @click="cancelAuthUser(scope.row)"
                     v-hasPermi="[SYSTEM_ROLE_REMOVE]">取消授權
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
    <select-user ref="selectRef" :roleId="queryParams.roleId" @ok="handleQuery"/>
  </div>
</template>

<script setup name="AuthUser">
import {
  SYSTEM_ROLE_ADD,
  SYSTEM_ROLE_REMOVE
} from '@/constants/permissions'
import selectUser from "./selectUser"
import {allocatedUserList, authUserCancel, authUserCancelAll} from "@/api/system/role"

const route = useRoute()
const {proxy} = getCurrentInstance()
const {sys_normal_disable} = proxy.useDict("sys_normal_disable")

const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const userIds = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: route.params.roleId,
  userName: undefined,
  phonenumber: undefined,
})

/** 查詢授權使用者列表 */
function getList() {
  loading.value = true
  allocatedUserList(queryParams).then(response => {
    userList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 返回按鈕 */
function handleClose() {
  const obj = {path: "/system/role"}
  proxy.$tab.closeOpenPage(obj)
}

/** 搜尋按鈕操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按鈕操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多選框選中資料 */
function handleSelectionChange(selection) {
  userIds.value = selection.map(item => item.userId)
  multiple.value = !selection.length
}

/** 打開授權使用者表彈窗 */
function openSelectUser() {
  proxy.$refs["selectRef"].show()
}

/** 取消授權按鈕操作 */
function cancelAuthUser(row) {
  proxy.$modal.confirm('確認要取消該使用者"' + row.userName + '"角色嗎？').then(function () {
    return authUserCancel({userId: row.userId, roleId: queryParams.roleId})
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("取消授權成功")
  }).catch(() => {
  })
}

/** 批量取消授權按鈕操作 */
function cancelAuthUserAll(row) {
  const roleId = queryParams.roleId
  const uIds = userIds.value.join(",")
  proxy.$modal.confirm("是否取消選中使用者授權資料選項?").then(function () {
    return authUserCancelAll({roleId: roleId, userIds: uIds})
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("取消授權成功")
  }).catch(() => {
  })
}

getList()
</script>
