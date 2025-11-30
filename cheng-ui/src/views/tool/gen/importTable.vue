<template>
  <!-- 匯入表 -->
  <el-dialog title="匯入表" v-model="visible" width="800px" top="5vh" append-to-body>
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="表名稱" prop="tableName">
        <el-input
          v-model="queryParams.tableName"
          placeholder="請輸入表名稱"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="表描述" prop="tableComment">
        <el-input
          v-model="queryParams.tableComment"
          placeholder="請輸入表描述"
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
      <el-table @row-click="clickRow" ref="table" :data="dbTableList" @selection-change="handleSelectionChange" height="260px">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="tableName" label="表名稱" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="tableComment" label="表描述" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="createTime" label="建立時間"></el-table-column>
        <el-table-column prop="updateTime" label="更新時間"></el-table-column>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-row>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleImportTable">確 定</el-button>
        <el-button @click="visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { listDbTable, importTable } from "@/api/tool/gen"

const total = ref(0)
const visible = ref(false)
const tables = ref([])
const dbTableList = ref([])
const { proxy } = getCurrentInstance()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  tableName: undefined,
  tableComment: undefined
})

const emit = defineEmits(["ok"])

/** 查詢參數列表 */
function show() {
  getList()
  visible.value = true
}

/** 點擊選擇行 */
function clickRow(row) {
  proxy.$refs.table.toggleRowSelection(row)
}

/** 多選框選中資料 */
function handleSelectionChange(selection) {
  tables.value = selection.map(item => item.tableName)
}

/** 查詢表資料 */
function getList() {
  listDbTable(queryParams).then(res => {
    dbTableList.value = res.rows
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

/** 匯入按鈕操作 */
function handleImportTable() {
  const tableNames = tables.value.join(",")
  if (tableNames == "") {
    proxy.$modal.msgError("請選擇要匯入的表")
    return
  }
  importTable({ tables: tableNames }).then(res => {
    proxy.$modal.msgSuccess(res.msg)
    if (res.code === 200) {
      visible.value = false
      emit("ok")
    }
  })
}

defineExpose({
  show,
})
</script>
