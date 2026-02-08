<template>
  <div class="app-container">
    <el-form ref="productFormRef" :model="form" :rules="rules" label-width="100px">
      <el-card class="mb15">
        <template #header>
          <span>基本資訊</span>
        </template>
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
        <el-form-item label="商品簡介" prop="subTitle">
          <el-input v-model="form.subTitle" type="textarea" :rows="2" placeholder="請輸入商品簡介" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="商品價格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原價" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="特惠價" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="0" placeholder="留空表示無特惠" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="特價結束時間" prop="saleEndDate">
              <el-date-picker
                v-model="form.saleEndDate"
                type="datetime"
                placeholder="留空表示長期"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <span v-if="form.salePrice > 0" class="sale-price-hint">
                <el-tag type="danger" size="small">特惠價啟用中</el-tag>
                <span style="margin-left: 8px; color: #f56c6c;">省 ${{ (form.price || 0) - (form.salePrice || 0) }}</span>
              </span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
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
          <el-col :span="8">
            <el-form-item label="熱門" prop="isHot">
              <el-switch v-model="form.isHot" :active-value="true" :inactive-value="false" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="mb15">
        <template #header>
          <span>商品圖片與規格</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="商品主圖" prop="mainImage">
              <image-upload v-model="form.mainImage" :limit="1" />
            </el-form-item>
            <el-form-item label="商品圖片" prop="sliderImages">
              <image-upload v-model="form.sliderImages" :limit="5" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <div class="sku-header">
              <span class="sku-title">商品規格 (SKU)</span>
              <el-button type="primary" size="small" icon="Plus" @click="addSku">新增規格</el-button>
            </div>
            <el-table :data="skuList" border size="small">
              <el-table-column prop="stockMode" label="庫存模式" width="110" align="center">
                <template #default="scope">
                  <el-select
                    v-model="scope.row.stockMode"
                    size="small"
                    style="width: 90px"
                    @change="(val) => handleStockModeChange(scope.row, val)"
                  >
                    <el-option label="關聯庫存" value="LINKED" />
                    <el-option label="手動輸入" value="MANUAL" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="skuName" label="規格名稱" min-width="120">
                <template #default="scope">
                  <el-input v-model="scope.row.skuName" placeholder="如：紅色/L" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="skuCode" label="SKU編碼" width="120">
                <template #default="scope">
                  <el-input v-model="scope.row.skuCode" placeholder="選填" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="price" label="價格" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.price" :min="0" :precision="0" size="small" controls-position="right" style="width: 100%" />
                </template>
              </el-table-column>
              <el-table-column prop="stockQuantity" label="庫存" width="90">
                <template #default="scope">
                  <el-tooltip
                    v-if="scope.row.stockMode === 'LINKED' && scope.row.invItemId"
                    content="庫存由關聯的物品庫存同步"
                    placement="top"
                  >
                    <el-input-number
                      v-model="scope.row.stockQuantity"
                      :min="0"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                      disabled
                    />
                  </el-tooltip>
                  <el-tooltip
                    v-else-if="scope.row.stockMode === 'MANUAL'"
                    content="預購模式：可設為 0 或預估數量"
                    placement="top"
                  >
                    <el-input-number
                      v-model="scope.row.stockQuantity"
                      :min="0"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </el-tooltip>
                  <el-input-number
                    v-else
                    v-model="scope.row.stockQuantity"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="invItemId" min-width="320">
                <template #header>
                  <span>關聯庫存物品</span>
                  <el-tooltip placement="top">
                    <template #content>
                      <div style="line-height: 1.8">
                        <div style="margin-bottom: 4px"><strong>搜尋方式：</strong></div>
                        <div>・直接輸入物品名稱或編碼</div>
                        <div>・輸入 <code style="background: rgba(255,255,255,0.2); color: #ffd666; padding: 2px 6px; border-radius: 3px; font-weight: 500;">#標籤名</code> 搜尋指定標籤的物品</div>
                        <div style="margin-top: 6px; color: #a0a0a0; font-size: 12px;">例如：<span style="color: #ffd666;">#辦公用品</span></div>
                      </div>
                    </template>
                    <el-icon style="margin-left: 4px; color: #909399; cursor: help;"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </template>
                <template #default="scope">
                  <template v-if="scope.row.stockMode === 'LINKED'">
                    <el-select
                      v-model="scope.row.invItemId"
                      placeholder="輸入名稱、編碼或 #標籤 搜尋"
                      clearable
                      filterable
                      remote
                      reserve-keyword
                      :remote-method="(query) => remoteSearchInvItems(query)"
                      :loading="invItemLoading"
                      size="small"
                      style="width: 100%"
                      popper-class="inv-item-select-popper"
                      @change="(val) => handleInvItemChange(scope.row, val)"
                      @focus="() => handleInvSelectFocus(scope.row)"
                    >
                      <el-option
                        v-for="item in invItemOptions"
                        :key="item.itemId"
                        :label="item.itemName"
                        :value="item.itemId"
                      >
                        <div class="inv-item-option">
                          <div class="inv-item-main">
                            <span class="inv-item-name">{{ item.itemName }}</span>
                            <span class="inv-item-meta">
                              <span v-if="item.itemCode" class="inv-item-code">{{ item.itemCode }}</span>
                              <span class="inv-item-stock">庫存: {{ item.currentStock ?? item.stockQuantity ?? 0 }}</span>
                            </span>
                          </div>
                          <div v-if="item.tags && item.tags.length" class="inv-item-tags">
                            <el-tag
                              v-for="tag in item.tags.slice(0, 3)"
                              :key="tag.tagId"
                              size="small"
                              type="info"
                            >{{ tag.tagName }}</el-tag>
                          </div>
                        </div>
                      </el-option>
                    </el-select>
                  </template>
                  <template v-else>
                    <span class="manual-mode-hint">
                      <el-icon><InfoFilled /></el-icon>
                      預購/叫貨模式，無需關聯庫存
                    </span>
                  </template>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="狀態" width="70" align="center">
                <template #default="scope">
                  <el-switch v-model="scope.row.status" active-value="ENABLED" inactive-value="DISABLED" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60" align="center" fixed="right">
                <template #default="scope">
                  <el-button link type="danger" icon="Delete" @click="removeSku(scope.$index)" size="small" />
                </template>
              </el-table-column>
            </el-table>
            <div v-if="skuList.length === 0" class="empty-sku">
              <el-empty description="暫無規格，點擊「新增規格」按鈕添加" :image-size="60" />
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="mb15">
        <template #header>
          <span>商品詳情</span>
        </template>
        <el-form-item label="商品詳情" prop="description">
          <editor v-model="form.description" :min-height="300" />
        </el-form-item>
      </el-card>

      <div class="footer-btns">
        <el-button type="primary" @click="submitForm">儲存商品</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup name="ShopProductEdit">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { InfoFilled, QuestionFilled } from '@element-plus/icons-vue'
import { getProduct, addProduct, updateProduct } from '@/api/shop/product'
import { treeCategory } from '@/api/shop/category'
import { listSku, batchSaveSku } from '@/api/shop/sku'
import { listItemWithStock, getItem } from '@/api/inventory/item'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const productId = computed(() => route.params.productId)
const isEdit = computed(() => !!productId.value)

const categoryOptions = ref([])
const invItemOptions = ref([])
const invItemAllOptions = ref([])
const invItemLoading = ref(false)
const skuList = ref([])

const productFormRef = ref(null)

const form = ref({
  productId: undefined,
  categoryId: undefined,
  title: undefined,
  subTitle: undefined,
  mainImage: undefined,
  sliderImages: undefined,
  price: 0,
  originalPrice: 0,
  salePrice: undefined,
  saleEndDate: undefined,
  description: undefined,
  sortOrder: 0,
  isRecommend: false,
  isNew: false,
  isHot: false
})

const rules = {
  title: [{ required: true, message: '商品名稱不能為空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '請選擇分類', trigger: 'change' }],
  price: [{ required: true, message: '商品價格不能為空', trigger: 'blur' }]
}

onMounted(() => {
  getCategoryTree()
  loadInvItems()
  if (isEdit.value) {
    loadProduct()
  }
})

function getCategoryTree() {
  treeCategory().then(response => {
    categoryOptions.value = response.data
  })
}

// 搜尋防抖計時器
let searchTimer = null

function loadInvItems(params = {}) {
  // 限制每次查詢數量，提升效能
  const query = { pageNum: 1, pageSize: 30, ...params }
  return listItemWithStock(query).then(response => {
    const items = (response.rows || []).map(item => ({
      itemId: item.itemId,
      itemCode: item.itemCode,
      itemName: item.itemName,
      barcode: item.barcode,
      currentStock: item.availableQty || 0,
      stockQuantity: item.totalQuantity || 0,
      currentPrice: item.currentPrice,
      tags: item.tags || []
    }))
    // 只有初始載入時才更新 allOptions
    if (!params.itemName && !params.tagId && !params.tagName) {
      invItemAllOptions.value = items
    }
    invItemOptions.value = items
    return items
  })
}

function remoteSearchInvItems(query) {
  // 清除之前的計時器（防抖）
  if (searchTimer) {
    clearTimeout(searchTimer)
  }

  if (!query || query.trim() === '') {
    // 空查詢時顯示初始資料
    invItemOptions.value = invItemAllOptions.value
    return
  }

  // 防抖：延遲 300ms 執行搜尋
  searchTimer = setTimeout(() => {
    invItemLoading.value = true
    const params = {}

    // 支援 #標籤 搜尋
    if (query.startsWith('#')) {
      const tagName = query.substring(1).trim()
      if (tagName) {
        params.tagName = tagName
      }
    } else {
      // 一般名稱或編碼搜尋
      params.itemName = query.trim()
    }

    loadInvItems(params).finally(() => {
      invItemLoading.value = false
    })
  }, 300)
}

function handleInvSelectFocus(row) {
  // 聚焦時載入初始資料（如果尚未載入）
  if (invItemOptions.value.length === 0) {
    loadInvItems()
  }
}

function loadProduct() {
  getProduct(productId.value).then(response => {
    const data = response.data
    // 將 JSON 陣列格式的 sliderImages 轉換為逗號分隔格式供 ImageUpload 使用
    if (data.sliderImages) {
      try {
        const images = JSON.parse(data.sliderImages)
        if (Array.isArray(images)) {
          data.sliderImages = images.join(',')
        }
      } catch (e) {
        // 已經是逗號分隔格式或其他格式，不需要轉換
      }
    }
    form.value = data
    loadSkuList()
  })
}

function loadSkuList() {
  listSku(productId.value).then(response => {
    const skus = response.data || []
    // 為每個 SKU 設置 stockMode：有關聯庫存物品的為 LINKED，否則為 MANUAL
    skuList.value = skus.map(sku => ({
      ...sku,
      stockMode: sku.invItemId ? 'LINKED' : 'MANUAL'
    }))
    // 編輯時確保已關聯的庫存物品載入到下拉選項中
    ensureLinkedItemsLoaded()
  })
}

async function ensureLinkedItemsLoaded() {
  const linkedItemIds = skuList.value
    .filter(sku => sku.invItemId)
    .map(sku => sku.invItemId)

  if (linkedItemIds.length === 0) return

  for (const itemId of linkedItemIds) {
    const exists = invItemAllOptions.value.some(item => item.itemId === itemId)
    if (!exists) {
      try {
        const response = await getItem(itemId)
        if (response.data) {
          const item = response.data
          invItemAllOptions.value.push({
            itemId: item.itemId,
            itemCode: item.itemCode,
            itemName: item.itemName,
            barcode: item.barcode,
            currentStock: item.stockQuantity || 0,
            stockQuantity: item.stockQuantity || 0,
            currentPrice: item.currentPrice,
            tags: item.tags || []
          })
        }
      } catch (e) {
        console.warn('無法載入庫存物品:', itemId)
      }
    }
  }
  invItemOptions.value = [...invItemAllOptions.value]
}

function addSku() {
  skuList.value.push({
    skuId: undefined,
    productId: productId.value,
    skuCode: '',
    skuName: '',
    price: form.value.price || 0,
    stockQuantity: 0,
    invItemId: undefined,
    stockMode: 'LINKED',
    status: 'ENABLED'
  })
}

function handleStockModeChange(row, mode) {
  if (mode === 'MANUAL') {
    // 切換到手動模式時，清除關聯的庫存物品
    row.invItemId = undefined
  }
}

function removeSku(index) {
  skuList.value.splice(index, 1)
}

function handleInvItemChange(row, invItemId) {
  if (invItemId) {
    let invItem = invItemOptions.value.find(item => item.itemId === invItemId)
    if (!invItem) {
      invItem = invItemAllOptions.value.find(item => item.itemId === invItemId)
    }
    if (invItem) {
      row.skuName = row.skuName || invItem.itemName
      row.skuCode = row.skuCode || invItem.itemCode
      row.stockQuantity = invItem.currentStock ?? invItem.stockQuantity ?? 0
    }
  } else {
    row.stockQuantity = 0
  }
}

function submitForm() {
  productFormRef.value?.validate(valid => {
    if (valid) {
      const saveProduct = isEdit.value ? updateProduct : addProduct
      saveProduct(form.value).then(response => {
        const savedProductId = response.data?.productId || form.value.productId || productId.value
        
        if (skuList.value.length > 0 && savedProductId) {
          return batchSaveSku(savedProductId, skuList.value).then(() => {
            proxy.$modal.msgSuccess(isEdit.value ? '修改成功' : '新增成功')
            handleCancel()
          })
        } else {
          proxy.$modal.msgSuccess(isEdit.value ? '修改成功' : '新增成功')
          handleCancel()
        }
      })
    }
  })
}

function handleCancel() {
  router.push('/cadm/shop/product')
}
</script>

<style scoped>
.mb15 {
  margin-bottom: 15px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sku-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}
.sku-title {
  font-weight: 600;
  color: #303133;
}
.empty-sku {
  padding: 20px 0;
}
.footer-btns {
  text-align: center;
  padding: 20px 0;
}
.inv-item-option {
  display: flex;
  flex-direction: column;
  padding: 6px 0;
  line-height: 1.5;
  min-width: 320px;
}
.inv-item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.inv-item-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.inv-item-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  flex-shrink: 0;
}
.inv-item-code {
  color: #606266;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 3px;
}
.inv-item-stock {
  color: #67c23a;
  font-weight: 500;
}
.inv-item-tags {
  display: flex;
  gap: 4px;
  margin-top: 6px;
  flex-wrap: wrap;
}
.inv-item-tags .el-tag {
  height: 20px;
  line-height: 18px;
  font-size: 11px;
}
.manual-mode-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
}
.manual-mode-hint .el-icon {
  color: #e6a23c;
}
</style>

<style>
/* 全局樣式：設定庫存物品下拉選單的寬度 */
.inv-item-select-popper {
  min-width: 380px !important;
}
.inv-item-select-popper .el-select-dropdown__item {
  height: auto;
  padding: 8px 12px;
  line-height: 1.5;
}
</style>
