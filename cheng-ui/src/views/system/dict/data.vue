<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="字典名稱" prop="dictType">
            <el-select v-model="queryParams.dictType" style="width: 200px">
               <el-option
                  v-for="item in typeOptions"
                  :key="item.dictId"
                  :label="item.dictName"
                  :value="item.dictType"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="字典標籤" prop="dictLabel">
            <el-input
               v-model="queryParams.dictLabel"
               placeholder="請輸入字典標籤"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="狀態" prop="status">
            <el-select v-model="queryParams.status" placeholder="資料狀態" clearable style="width: 200px">
               <el-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
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
               @click="handleAdd"
               v-hasPermi="['system:dict:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['system:dict:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['system:dict:remove']"
            >刪除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['system:dict:export']"
            >匯出</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Close"
               @click="handleClose"
            >關閉</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="字典編碼" align="center" prop="dictCode" />
         <el-table-column label="字典標籤" align="center" prop="dictLabel">
            <template #default="scope">
               <span v-if="(scope.row.listClass == '' || scope.row.listClass == 'default') && (scope.row.cssClass == '' || scope.row.cssClass == null)">{{ scope.row.dictLabel }}</span>
               <el-tag v-else :type="scope.row.listClass == 'primary' ? '' : scope.row.listClass" :class="scope.row.cssClass">{{ scope.row.dictLabel }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="字典鍵值" align="center" prop="dictValue" />
         <el-table-column label="字典排序" align="center" prop="dictSort" />
         <el-table-column label="狀態" align="center" prop="status">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="備註" align="center" prop="remark" :show-overflow-tooltip="true" />
         <el-table-column label="建立時間" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:dict:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:dict:remove']">刪除</el-button>
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

      <!-- 新增或修改參數配置對話框 -->
      <el-dialog :title="title" v-model="open" width="500px" append-to-body>
         <el-form ref="dataRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="字典類型">
               <el-input v-model="form.dictType" :disabled="true" />
            </el-form-item>
            <el-form-item label="資料標籤" prop="dictLabel">
               <el-input v-model="form.dictLabel" placeholder="請輸入資料標籤" />
            </el-form-item>
            <el-form-item label="資料鍵值" prop="dictValue">
               <el-input v-model="form.dictValue" placeholder="請輸入資料鍵值" />
            </el-form-item>
            <el-form-item label="樣式屬性" prop="cssClass">
               <el-input v-model="form.cssClass" placeholder="請輸入樣式屬性" />
            </el-form-item>
            <el-form-item label="顯示排序" prop="dictSort">
               <el-input-number v-model="form.dictSort" controls-position="right" :min="0" />
            </el-form-item>
            <el-form-item label="回顯樣式" prop="listClass">
               <el-select v-model="form.listClass">
                  <el-option
                     v-for="item in listClassOptions"
                     :key="item.value"
                     :label="item.label + '(' + item.value + ')'"
                     :value="item.value"
                  ></el-option>
               </el-select>
            </el-form-item>
            <el-form-item label="狀態" prop="status">
               <el-radio-group v-model="form.status">
                  <el-radio
                     v-for="dict in sys_normal_disable"
                     :key="dict.value"
                     :value="dict.value"
                  >{{ dict.label }}</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item label="備註" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="請輸入内容"></el-input>
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">確 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Data">
import useDictStore from '@/store/modules/dict'
import { optionselect as getDictOptionselect, getType } from "@/api/system/dict/type"
import { listData, getData, delData, addData, updateData } from "@/api/system/dict/data"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const dataList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const defaultDictType = ref("")
const typeOptions = ref([])
const route = useRoute()
// 資料標籤回顯樣式
const listClassOptions = ref([
  { value: "default", label: "預設" },
  { value: "primary", label: "主要" },
  { value: "success", label: "成功" },
  { value: "info", label: "訊息" },
  { value: "warning", label: "警告" },
  { value: "danger", label: "危险" }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictType: undefined,
    dictLabel: undefined,
    status: undefined
  },
  rules: {
    dictLabel: [{ required: true, message: "資料標籤不能為空", trigger: "blur" }],
    dictValue: [{ required: true, message: "資料鍵值不能為空", trigger: "blur" }],
    dictSort: [{ required: true, message: "資料順序不能為空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查詢字典類型詳細 */
function getTypes(dictId) {
  getType(dictId).then(response => {
    queryParams.value.dictType = response.data.dictType
    defaultDictType.value = response.data.dictType
    getList()
  })
}

/** 查詢字典類型列表 */
function getTypeList() {
  getDictOptionselect().then(response => {
    typeOptions.value = response.data
  })
}

/** 查詢字典資料列表 */
function getList() {
  loading.value = true
  listData(queryParams.value).then(response => {
    dataList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按鈕 */
function cancel() {
  open.value = false
  reset()
}

/** 表單重置 */
function reset() {
  form.value = {
    dictCode: undefined,
    dictLabel: undefined,
    dictValue: undefined,
    cssClass: undefined,
    listClass: "default",
    dictSort: 0,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("dataRef")
}

/** 搜尋按鈕操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 返回按鈕操作 */
function handleClose() {
  const obj = { path: "/system/dict" }
  proxy.$tab.closeOpenPage(obj)
}

/** 重置按鈕操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.dictType = defaultDictType.value
  handleQuery()
}

/** 新增按鈕操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增字典資料"
  form.value.dictType = queryParams.value.dictType
}

/** 多選框選中資料 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dictCode)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 修改按鈕操作 */
function handleUpdate(row) {
  reset()
  const dictCode = row.dictCode || ids.value
  getData(dictCode).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改字典資料"
  })
}

/** 提交按鈕 */
function submitForm() {
  proxy.$refs["dataRef"].validate(valid => {
    if (valid) {
      if (form.value.dictCode != undefined) {
        updateData(form.value).then(response => {
          useDictStore().removeDict(queryParams.value.dictType)
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addData(form.value).then(response => {
          useDictStore().removeDict(queryParams.value.dictType)
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 刪除按鈕操作 */
function handleDelete(row) {
  const dictCodes = row.dictCode || ids.value
  proxy.$modal.confirm('是否確認刪除字典編碼為"' + dictCodes + '"的資料選項？').then(function() {
    return delData(dictCodes)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("刪除成功")
    useDictStore().removeDict(queryParams.value.dictType)
  }).catch(() => {})
}

/** 匯出按鈕操作 */
function handleExport() {
  proxy.download("system/dict/data/export", {
    ...queryParams.value
  }, `dict_data_${new Date().getTime()}.xlsx`)
}

getTypes(route.params && route.params.dictId)
getTypeList()
</script>
