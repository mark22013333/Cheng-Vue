<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="分類名稱" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="請輸入分類名稱"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇" clearable style="width: 150px">
          <el-option label="啟用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['shop:category:add']">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">
          展開/摺疊
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="categoryList"
      row-key="categoryId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="name" label="分類名稱" min-width="200" />
      <el-table-column prop="icon" label="圖示" width="100" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.icon" :src="getImageUrl(scope.row.icon)" style="width: 40px; height: 40px" fit="cover" :preview-src-list="[getImageUrl(scope.row.icon)]" preview-teleported />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="狀態" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'ENABLED' ? 'success' : 'danger'">
            {{ scope.row.status === 'ENABLED' ? '啟用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="建立時間" width="180" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['shop:category:edit']">
            修改
          </el-button>
          <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['shop:category:add']">
            新增
          </el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['shop:category:remove']">
            刪除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/修改對話框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="categoryFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上級分類" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryOptions"
            :props="{ value: 'categoryId', label: 'name', children: 'children' }"
            value-key="categoryId"
            placeholder="選擇上級分類"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分類名稱" prop="name">
          <el-input v-model="form.name" placeholder="請輸入分類名稱" />
        </el-form-item>
        <el-form-item label="分類圖示" prop="icon">
          <image-upload v-model="form.icon" :limit="1" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ENABLED">啟用</el-radio>
            <el-radio label="DISABLED">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopCategory">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { listCategory, getCategory, addCategory, updateCategory, delCategory, treeCategory } from '@/api/shop/category'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const categoryList = ref([])
const categoryOptions = ref([])
const open = ref(false)
const title = ref('')
const isExpandAll = ref(true)
const refreshTable = ref(true)

const queryFormRef = ref(null)
const categoryFormRef = ref(null)

const queryParams = reactive({
  name: undefined,
  status: undefined
})

const form = ref({})

const rules = {
  name: [{ required: true, message: '分類名稱不能為空', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '排序不能為空', trigger: 'blur' }]
}

onMounted(() => {
  getList()
})

function getList() {
  loading.value = true
  listCategory(queryParams).then(response => {
    categoryList.value = proxy.handleTree(response.data, 'categoryId', 'parentId')
    loading.value = false
  })
}

function getCategoryTree() {
  treeCategory().then(response => {
    categoryOptions.value = [{ categoryId: 0, name: '頂級分類', children: response.data }]
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

function handleAdd(row) {
  reset()
  getCategoryTree()
  if (row && row.categoryId) {
    form.value.parentId = row.categoryId
  } else {
    form.value.parentId = 0
  }
  open.value = true
  title.value = '新增分類'
}

function handleUpdate(row) {
  reset()
  getCategoryTree()
  getCategory(row.categoryId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改分類'
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('確認刪除分類「' + row.name + '」嗎？').then(() => {
    return delCategory(row.categoryId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function submitForm() {
  categoryFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.categoryId) {
        updateCategory(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addCategory(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    categoryId: undefined,
    parentId: 0,
    name: undefined,
    icon: undefined,
    sortOrder: 0,
    status: 'ENABLED'
  }
  categoryFormRef.value?.resetFields()
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}
</script>
