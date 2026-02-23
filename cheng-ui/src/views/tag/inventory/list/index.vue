<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="標籤名稱" prop="tagName">
        <el-input
          v-model="queryParams.tagName"
          placeholder="請輸入標籤名稱"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="標籤代碼" prop="tagCode">
        <el-input
          v-model="queryParams.tagCode"
          placeholder="請輸入標籤代碼"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="標籤狀態" clearable style="width: 200px">
          <el-option label="啟用" value="1" />
          <el-option label="停用" value="0" />
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
          v-hasPermi="[TAG_INVENTORY_ADD]"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="[TAG_INVENTORY_EDIT]"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="[TAG_INVENTORY_REMOVE]"
        >刪除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="tagList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="標籤ID" align="center" prop="tagId" width="80" />
      <el-table-column label="標籤名稱" align="center" prop="tagName" min-width="120">
        <template #default="scope">
          <bookmark-tag :label="scope.row.tagName" :color="scope.row.tagColor" />
        </template>
      </el-table-column>
      <el-table-column label="標籤代碼" align="center" prop="tagCode" min-width="120" />
      <el-table-column label="標籤顏色" align="center" prop="tagColor" width="120">
        <template #default="scope">
          <el-tooltip v-if="scope.row.tagColor" content="點擊複製色碼" placement="top">
            <div 
              class="color-box"
              :style="{ backgroundColor: scope.row.tagColor }"
              @click="copyColorCode(scope.row.tagColor)"
            >
              <span class="color-code">{{ scope.row.tagColor }}</span>
            </div>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="物品數" align="center" prop="itemCount" width="100" />
      <el-table-column label="狀態" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '啟用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortOrder" width="80" />
      <el-table-column label="建立時間" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="[TAG_INVENTORY_EDIT]">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="[TAG_INVENTORY_REMOVE]">刪除</el-button>
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

    <!-- 新增或修改標籤對話框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="標籤名稱" prop="tagName">
          <el-input v-model="form.tagName" placeholder="請輸入標籤名稱" />
        </el-form-item>
        <el-form-item label="標籤顏色" prop="tagColor">
          <el-color-picker v-model="form.tagColor" />
        </el-form-item>
        <el-form-item label="標籤描述" prop="tagDescription">
          <el-input v-model="form.tagDescription" type="textarea" placeholder="請輸入標籤描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="1">啟用</el-radio>
            <el-radio label="0">停用</el-radio>
          </el-radio-group>
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

<script setup name="InventoryTagList">
import {
  TAG_INVENTORY_ADD,
  TAG_INVENTORY_EDIT,
  TAG_INVENTORY_REMOVE
} from '@/constants/permissions'
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { listInventoryTags, getInventoryTag, addInventoryTag, updateInventoryTag, delInventoryTag } from '@/api/tag/inventory'

const { proxy } = getCurrentInstance()

const tagList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const queryFormRef = ref(null)
const formRef = ref(null)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tagName: undefined,
    tagCode: undefined,
    status: undefined
  },
  rules: {
    tagName: [{ required: true, message: '標籤名稱不能為空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listInventoryTags(queryParams.value).then(response => {
    tagList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    tagId: undefined,
    tagName: undefined,
    tagCode: undefined,
    tagColor: '#67C23A',
    tagDescription: undefined,
    sortOrder: 0,
    status: '1'
  }
  proxy.resetForm('formRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryFormRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.tagId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增庫存標籤'
}

function handleUpdate(row) {
  reset()
  const tagId = row.tagId || ids.value[0]
  getInventoryTag(tagId).then(response => {
    form.value = response.data
    form.value.status = String(response.data.status)
    open.value = true
    title.value = '修改庫存標籤'
  })
}

function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.tagId != null) {
        updateInventoryTag(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addInventoryTag(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const tagIds = row.tagId || ids.value
  proxy.$modal.confirm('確認要刪除標籤嗎？').then(() => {
    return delInventoryTag(tagIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function copyColorCode(colorCode) {
  navigator.clipboard.writeText(colorCode).then(() => {
    proxy.$modal.msgSuccess(`已複製色碼: ${colorCode}`)
  }).catch(() => {
    proxy.$modal.msgError('複製失敗')
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.color-box {
  width: 80px;
  height: 28px;
  border-radius: 4px;
  margin: 0 auto;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}
.color-box:hover {
  transform: scale(1.05);
}
.color-code {
  font-size: 11px;
  color: #fff;
  text-shadow: 0 0 2px rgba(0,0,0,0.5);
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
