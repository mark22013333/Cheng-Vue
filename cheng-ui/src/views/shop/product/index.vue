<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="商品名稱" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="請輸入商品名稱"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品分類" prop="categoryId">
        <el-tree-select
          v-model="queryParams.categoryId"
          :data="categoryOptions"
          :props="{ value: 'categoryId', label: 'name', children: 'children' }"
          placeholder="請選擇分類"
          check-strictly
          clearable
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="商品狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇" clearable style="width: 150px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="上架" value="ON_SALE" />
          <el-option label="下架" value="OFF_SALE" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="[SHOP_PRODUCT_ADD]">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="[SHOP_PRODUCT_REMOVE]">
          刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Star" :disabled="multiple" @click="handleBatchFlag('is_hot', true)" v-hasPermi="[SHOP_PRODUCT_EDIT]">
          設為熱門
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Promotion" :disabled="multiple" @click="handleBatchFlag('is_new', true)" v-hasPermi="[SHOP_PRODUCT_EDIT]">
          設為新品
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Pointer" :disabled="multiple" @click="handleBatchFlag('is_recommend', true)" v-hasPermi="[SHOP_PRODUCT_EDIT]">
          設為推薦
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="mainImage" label="主圖" width="80" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.mainImage" :src="getImageUrl(scope.row.mainImage)" style="width: 50px; height: 50px" fit="cover" :preview-src-list="[getImageUrl(scope.row.mainImage)]" preview-teleported />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="商品名稱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分類" width="100" align="center" />
      <el-table-column prop="price" label="價格" width="120" align="right">
        <template #default="scope">
          <span class="price">{{ scope.row.price ? formatCurrency(scope.row.price) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="salesCount" label="銷量" width="70" align="center" />
      <el-table-column prop="isHot" label="熱門" width="70" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.isHot"
            :active-value="true"
            :inactive-value="false"
            @change="handleFlagChange(scope.row, 'is_hot', scope.row.isHot)"
            v-hasPermi="[SHOP_PRODUCT_EDIT]"
          />
        </template>
      </el-table-column>
      <el-table-column prop="isNew" label="新品" width="70" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.isNew"
            :active-value="true"
            :inactive-value="false"
            @change="handleFlagChange(scope.row, 'is_new', scope.row.isNew)"
            v-hasPermi="[SHOP_PRODUCT_EDIT]"
          />
        </template>
      </el-table-column>
      <el-table-column prop="isRecommend" label="推薦" width="70" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.isRecommend"
            :active-value="true"
            :inactive-value="false"
            @change="handleFlagChange(scope.row, 'is_recommend', scope.row.isRecommend)"
            v-hasPermi="[SHOP_PRODUCT_EDIT]"
          />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="狀態" width="80" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">{{ getStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="建立時間" width="160" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="[SHOP_PRODUCT_EDIT]">
            編輯
          </el-button>
          <el-button v-if="scope.row.status !== 'ON_SALE'" link type="success" icon="Top" @click="handleOnSale(scope.row)" v-hasPermi="[SHOP_PRODUCT_EDIT]">
            上架
          </el-button>
          <el-button v-if="scope.row.status === 'ON_SALE'" link type="warning" icon="Bottom" @click="handleOffSale(scope.row)" v-hasPermi="[SHOP_PRODUCT_EDIT]">
            下架
          </el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="[SHOP_PRODUCT_REMOVE]">
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
    <el-dialog :title="title" v-model="open" width="900px" append-to-body destroy-on-close>
      <el-form ref="productFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名稱" prop="title">
              <el-input v-model="form.title" placeholder="請輸入商品名稱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分類" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryOptions"
                :props="{ value: 'categoryId', label: 'name', children: 'children' }"
                placeholder="選擇分類"
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品價格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原價" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品主圖" prop="mainImage">
          <image-upload v-model="form.mainImage" :limit="1" />
        </el-form-item>
        <el-form-item label="商品圖片" prop="sliderImages">
          <image-upload v-model="form.sliderImages" :limit="5" />
        </el-form-item>
        <el-form-item label="商品簡介" prop="subTitle">
          <el-input v-model="form.subTitle" type="textarea" :rows="2" placeholder="請輸入商品簡介" />
        </el-form-item>
        <el-form-item label="商品詳情" prop="description">
          <editor v-model="form.description" :min-height="200" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推薦" prop="isRecommend">
              <el-switch v-model="form.isRecommend" :active-value="true" :inactive-value="false" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="新品" prop="isNew">
              <el-switch v-model="form.isNew" :active-value="true" :inactive-value="false" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopProduct">
import {
  SHOP_PRODUCT_ADD,
  SHOP_PRODUCT_EDIT,
  SHOP_PRODUCT_REMOVE
} from '@/constants/permissions'
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listProduct, getProduct, addProduct, updateProduct, delProduct, onSaleProduct, offSaleProduct, updateProductFlag } from '@/api/shop/product'
import { treeCategory } from '@/api/shop/category'
import { formatCurrency } from '@/utils/cheng'

const { proxy } = getCurrentInstance()
const router = useRouter()

const loading = ref(false)
const showSearch = ref(true)
const productList = ref([])
const categoryOptions = ref([])
const open = ref(false)
const title = ref('')
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const queryFormRef = ref(null)
const productFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  categoryId: undefined,
  status: undefined
})

const form = ref({})

const rules = {
  title: [{ required: true, message: '商品名稱不能為空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '請選擇分類', trigger: 'change' }],
  price: [{ required: true, message: '商品價格不能為空', trigger: 'blur' }]
}

const statusMap = {
  DRAFT: { label: '草稿', type: 'info' },
  ON_SALE: { label: '上架', type: 'success' },
  OFF_SALE: { label: '下架', type: 'warning' }
}

onMounted(() => {
  getList()
  getCategoryTree()
})

function getList() {
  loading.value = true
  listProduct(queryParams).then(response => {
    productList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getCategoryTree() {
  treeCategory().then(response => {
    categoryOptions.value = response.data
  })
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

function getStatusLabel(status) {
  return statusMap[status]?.label || status
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
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
  ids.value = selection.map(item => item.productId)
  multiple.value = !selection.length
}

function handleAdd() {
  router.push('/cadm/shop/product-edit/index')
}

function handleUpdate(row) {
  router.push(`/cadm/shop/product-edit/index/${row.productId}`)
}

function handleOnSale(row) {
  proxy.$modal.confirm('確認上架商品「' + row.title + '」嗎？').then(() => {
    return onSaleProduct(row.productId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('上架成功')
  }).catch(() => {})
}

function handleOffSale(row) {
  proxy.$modal.confirm('確認下架商品「' + row.title + '」嗎？').then(() => {
    return offSaleProduct(row.productId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('下架成功')
  }).catch(() => {})
}

const flagLabels = { is_hot: '熱門', is_new: '新品', is_recommend: '推薦' }

function handleFlagChange(row, flagName, flagValue) {
  updateProductFlag({ productIds: [row.productId], flagName, flagValue }).then(() => {
    proxy.$modal.msgSuccess('更新成功')
  }).catch(() => {
    // 復原開關
    if (flagName === 'is_hot') row.isHot = !flagValue
    if (flagName === 'is_new') row.isNew = !flagValue
    if (flagName === 'is_recommend') row.isRecommend = !flagValue
  })
}

function handleBatchFlag(flagName, flagValue) {
  const label = flagLabels[flagName]
  proxy.$modal.confirm(`確認將選中的 ${ids.value.length} 個商品設為「${label}」嗎？`).then(() => {
    return updateProductFlag({ productIds: ids.value, flagName, flagValue })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('批量設定成功')
  }).catch(() => {})
}

function handleDelete(row) {
  const productIds = row.productId || ids.value
  proxy.$modal.confirm('確認刪除選中的商品嗎？').then(() => {
    return delProduct(productIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function submitForm() {
  productFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.productId) {
        updateProduct(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addProduct(form.value).then(() => {
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
    productId: undefined,
    categoryId: undefined,
    title: undefined,
    subTitle: undefined,
    mainImage: undefined,
    sliderImages: undefined,
    price: 0,
    originalPrice: 0,
    description: undefined,
    sortOrder: 0,
    isRecommend: false,
    isNew: false
  }
  productFormRef.value?.resetFields()
}
</script>

<style scoped>
.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>
