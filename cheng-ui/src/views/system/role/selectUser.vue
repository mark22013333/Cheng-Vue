<template>
   <!-- 授權使用者 -->
   <el-dialog title="選擇使用者" v-model="visible" width="800px" top="5vh" append-to-body>
      <el-form :model="queryParams" ref="queryRef" :inline="true">
         <el-form-item label="使用者名稱" prop="userName">
            <el-input
               v-model="queryParams.userName"
               placeholder="請輸入使用者名稱"
               clearable
               style="width: 180px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="手機號碼" prop="phonenumber">
            <el-input
               v-model="queryParams.phonenumber"
               placeholder="請輸入手機號碼"
               clearable
               style="width: 180px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>
      <el-row>
         <el-table @row-click="clickRow" ref="refTable" :data="userList" @selection-change="handleSelectionChange" height="260px">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column label="使用者名稱" prop="userName" :show-overflow-tooltip="true" />
            <el-table-column label="使用者暱稱" prop="nickName" :show-overflow-tooltip="true" />
            <el-table-column label="電子郵件" prop="email" :show-overflow-tooltip="true" />
            <el-table-column label="手機" prop="phonenumber" :show-overflow-tooltip="true" />
            <el-table-column label="狀態" align="center" prop="status">
               <template #default="scope">
                  <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
               </template>
            </el-table-column>
            <el-table-column label="建立時間" align="center" prop="createTime" width="180">
               <template #default="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
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
      </el-row>
      <template #footer>
         <div class="dialog-footer">
            <el-button type="primary" @click="handleSelectUser">確 定</el-button>
            <el-button @click="visible = false">取 消</el-button>
         </div>
      </template>
   </el-dialog>
</template>

<script setup name="SelectUser">
import { authUserSelectAll, unallocatedUserList } from "@/api/system/role"

const props = defineProps({
  roleId: {
    type: [Number, String]
  }
})

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const userList = ref([])
const visible = ref(false)
const total = ref(0)
const userIds = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: undefined,
  userName: undefined,
  phonenumber: undefined
})

// 顯示彈框
function show() {
  queryParams.roleId = props.roleId
  getList()
  visible.value = true
}

/**選擇行 */
function clickRow(row) {
  proxy.$refs["refTable"].toggleRowSelection(row)
}

// 多選框選中資料
function handleSelectionChange(selection) {
  userIds.value = selection.map(item => item.userId)
}

// 查詢表資料
function getList() {
  unallocatedUserList(queryParams).then(res => {
    userList.value = res.rows
    total.value = res.total
  })
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

const emit = defineEmits(["ok"])
/** 選擇授權使用者操作 */
function handleSelectUser() {
  const roleId = queryParams.roleId
  const uIds = userIds.value.join(",")
  if (uIds == "") {
    proxy.$modal.msgError("請選擇要分配的使用者")
    return
  }
  authUserSelectAll({ roleId: roleId, userIds: uIds }).then(res => {
    proxy.$modal.msgSuccess(res.msg)
    visible.value = false
    emit("ok")
  })
}

defineExpose({
  show,
})
</script>
