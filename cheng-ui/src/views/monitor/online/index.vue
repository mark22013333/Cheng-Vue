<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
         <el-form-item label="登入位置" prop="ipaddr">
            <el-input
               v-model="queryParams.ipaddr"
               placeholder="請輸入登入位置"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="使用者名稱" prop="userName">
            <el-input
               v-model="queryParams.userName"
               placeholder="請輸入使用者名稱"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>
      <el-table
         v-loading="loading"
         :data="onlineList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
         style="width: 100%;"
      >
         <el-table-column label="序號" width="50" type="index" align="center">
            <template #default="scope">
               <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
            </template>
         </el-table-column>
         <el-table-column label="Session編號" align="center" prop="tokenId" :show-overflow-tooltip="true" />
         <el-table-column label="登入名稱" align="center" prop="userName" :show-overflow-tooltip="true" />
         <el-table-column label="所屬部門" align="center" prop="deptName" :show-overflow-tooltip="true" />
         <el-table-column label="主機" align="center" prop="ipaddr" :show-overflow-tooltip="true" />
         <el-table-column label="登入地點" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
         <el-table-column label="作業系統" align="center" prop="os" :show-overflow-tooltip="true" />
         <el-table-column label="瀏覽器" align="center" prop="browser" :show-overflow-tooltip="true" />
         <el-table-column label="登入時間" align="center" prop="loginTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.loginTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width" fixed="right">
            <template #default="scope">
               <el-button link type="primary" icon="Delete" @click="handleForceLogout(scope.row)" v-hasPermi="['monitor:online:forceLogout']">強制退出</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />
   </div>
</template>

<script setup name="Online">
import { forceLogout, list as initData } from "@/api/monitor/online"

const { proxy } = getCurrentInstance()

const onlineList = ref([])
const loading = ref(true)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryParams = ref({
  ipaddr: undefined,
  userName: undefined
})

/** 查詢登入日誌列表 */
function getList() {
  loading.value = true
  initData(queryParams.value).then(response => {
    onlineList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜尋按鈕操作 */
function handleQuery() {
  pageNum.value = 1
  getList()
}

/** 重置按鈕操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 強制退出按鈕操作 */
function handleForceLogout(row) {
    proxy.$modal.confirm('是否確認強制退出名稱為"' + row.userName + '"的使用者?').then(function () {
  return forceLogout(row.tokenId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("刪除成功")
  }).catch(() => {})
}

getList()
</script>
