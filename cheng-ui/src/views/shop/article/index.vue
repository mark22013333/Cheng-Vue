<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="標題" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="請輸入文章標題"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇" clearable style="width: 120px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已發布" value="PUBLISHED" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['shop:article:add']">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['shop:article:remove']">
          刪除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="articleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="coverImage" label="封面" width="120" align="center">
        <template #default="scope">
          <el-image
            v-if="scope.row.coverImage"
            :src="getImageUrl(scope.row.coverImage)"
            style="width: 80px; height: 60px"
            fit="cover"
            :preview-src-list="[getImageUrl(scope.row.coverImage)]"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="標題" min-width="200" show-overflow-tooltip />
      <el-table-column prop="productTitle" label="關聯商品" width="150" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.productTitle || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="瀏覽數" width="80" align="center" />
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="狀態" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
            {{ scope.row.status === 'PUBLISHED' ? '已發布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="建立時間" width="180" align="center" />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['shop:article:edit']">
            編輯
          </el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['shop:article:remove']">
            刪除
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

    <!-- 新增/修改對話框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="900px" append-to-body destroy-on-close>
      <el-form ref="articleFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="文章標題" prop="title">
          <el-input v-model="form.title" placeholder="請輸入文章標題" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="請輸入文章摘要（選填）" />
        </el-form-item>
        <el-form-item label="封面圖" prop="coverImage">
          <image-upload v-model="form.coverImage" :limit="1" />
        </el-form-item>
        <el-form-item label="文章內容" prop="content">
          <editor v-model="form.content" :min-height="300" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="關聯商品" prop="productId">
              <product-picker-dialog
                v-model="form.productId"
                @confirm="onProductSelected"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="狀態" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="PUBLISHED">發布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="選填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopArticle">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { listArticle, getArticle, addArticle, updateArticle, delArticle } from '@/api/shop/article'
import ProductPickerDialog from '@/components/ProductPickerDialog/index.vue'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const articleList = ref([])
const total = ref(0)
const open = ref(false)
const dialogTitle = ref('')
const ids = ref([])
const multiple = ref(true)

const queryFormRef = ref(null)
const articleFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  status: undefined
})

const form = ref({})

const rules = {
  title: [{ required: true, message: '文章標題不能為空', trigger: 'blur' }]
}

onMounted(() => {
  getList()
})

function getList() {
  loading.value = true
  listArticle(queryParams).then(response => {
    articleList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.articleId)
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  dialogTitle.value = '新增文章'
}

function handleUpdate(row) {
  reset()
  getArticle(row.articleId).then(response => {
    form.value = response.data
    open.value = true
    dialogTitle.value = '修改文章'
  })
}

function handleDelete(row) {
  const articleIds = row.articleId || ids.value
  proxy.$modal.confirm('確認刪除選中的文章嗎？').then(() => {
    return delArticle(articleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function submitForm() {
  articleFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.articleId) {
        updateArticle(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addArticle(form.value).then(() => {
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
    articleId: undefined,
    title: undefined,
    summary: undefined,
    content: undefined,
    coverImage: undefined,
    productId: undefined,
    sortOrder: 0,
    status: 'DRAFT',
    remark: undefined
  }
  articleFormRef.value?.resetFields()
}

function onProductSelected(product) {
  form.value.productId = product.productId
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}
</script>
