<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="訂單編號" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="請輸入訂單編號"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="收件人" prop="receiverName">
        <el-input
          v-model="queryParams.receiverName"
          placeholder="請輸入收件人"
          clearable
          style="width: 150px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="訂單狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇" clearable style="width: 150px">
          <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="付款狀態" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="請選擇" clearable style="width: 120px">
          <el-option label="未付款" value="UNPAID" />
          <el-option label="已付款" value="PAID" />
          <el-option label="已退款" value="REFUNDED" />
        </el-select>
      </el-form-item>
      <el-form-item label="建立時間" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
          start-placeholder="開始日期"
          end-placeholder="結束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['shop:order:export']">
          匯出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="orderList" style="width: 100%">
      <el-table-column prop="orderNo" label="訂單編號" min-width="180" show-overflow-tooltip />
      <el-table-column prop="memberNickname" label="會員" min-width="100" show-overflow-tooltip />
      <el-table-column prop="totalAmount" label="訂單金額" min-width="120" align="right">
        <template #default="scope">
          <span class="price">NT$ {{ formatAmount(scope.row.totalAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="訂單狀態" min-width="100" align="center">
        <template #default="scope">
          <el-tag :type="getOrderStatusType(scope.row.status)">{{ getOrderStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payStatus" label="付款狀態" min-width="100" align="center">
        <template #default="scope">
          <el-dropdown v-if="canUpdatePayStatus(scope.row)" trigger="click" @command="(cmd) => handleUpdatePayStatus(scope.row, cmd)">
            <el-tag :type="getPayStatusType(scope.row.payStatus)" style="cursor: pointer">
              {{ getPayStatusLabel(scope.row.payStatus) }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-tag>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="PAID" :disabled="scope.row.payStatus === 'PAID'">標記為已付款</el-dropdown-item>
                <el-dropdown-item command="UNPAID" :disabled="scope.row.payStatus === 'UNPAID'">標記為未付款</el-dropdown-item>
                <el-dropdown-item command="REFUNDED" :disabled="scope.row.payStatus === 'REFUNDED'">標記為已退款</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-tag v-else :type="getPayStatusType(scope.row.payStatus)">{{ getPayStatusLabel(scope.row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="shipStatus" label="物流狀態" min-width="100" align="center">
        <template #default="scope">
          <el-dropdown v-if="canUpdateShipStatus(scope.row)" trigger="click" @command="(cmd) => handleUpdateShipStatus(scope.row, cmd)">
            <el-tag :type="getShipStatusType(scope.row.shipStatus)" style="cursor: pointer">
              {{ getShipStatusLabel(scope.row.shipStatus) }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-tag>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="UNSHIPPED" :disabled="scope.row.shipStatus === 'UNSHIPPED'">標記為未出貨</el-dropdown-item>
                <el-dropdown-item command="SHIPPED" :disabled="scope.row.shipStatus === 'SHIPPED'">標記為已出貨</el-dropdown-item>
                <el-dropdown-item command="DELIVERED" :disabled="scope.row.shipStatus === 'DELIVERED'">標記為已到貨</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-tag v-else :type="getShipStatusType(scope.row.shipStatus)">{{ getShipStatusLabel(scope.row.shipStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收件人" min-width="90" show-overflow-tooltip />
      <el-table-column prop="receiverMobile" label="收件電話" min-width="120" />
      <el-table-column prop="createTime" label="下單時間" min-width="160" align="center" />
      <el-table-column label="操作" min-width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['shop:order:query']">
            詳情
          </el-button>
          <el-button v-if="canShip(scope.row)" link type="success" icon="Van" @click="handleShip(scope.row)" v-hasPermi="['shop:order:ship']">
            出貨
          </el-button>
          <el-button v-if="canCancel(scope.row)" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['shop:order:cancel']">
            取消
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

    <!-- 訂單詳情對話框 -->
    <el-dialog title="訂單詳情" v-model="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border v-if="orderDetail">
        <el-descriptions-item label="訂單編號">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="會員暱稱">{{ orderDetail.memberNickname }}</el-descriptions-item>
        <el-descriptions-item label="訂單狀態">
          <el-tag :type="getOrderStatusType(orderDetail.status)">{{ getOrderStatusLabel(orderDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="付款狀態">
          <el-tag :type="getPayStatusType(orderDetail.payStatus)">{{ getPayStatusLabel(orderDetail.payStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品金額">NT$ {{ orderDetail.productAmount }}</el-descriptions-item>
        <el-descriptions-item label="運費">NT$ {{ orderDetail.shippingAmount }}</el-descriptions-item>
        <el-descriptions-item label="折扣金額">NT$ {{ orderDetail.discountAmount }}</el-descriptions-item>
        <el-descriptions-item label="訂單總額">
          <span class="price">NT$ {{ orderDetail.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="收件人">{{ orderDetail.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="收件電話">{{ orderDetail.receiverMobile }}</el-descriptions-item>
        <el-descriptions-item label="收件地址" :span="2">{{ orderDetail.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="物流單號" v-if="orderDetail.shippingNo">{{ orderDetail.shippingNo }}</el-descriptions-item>
        <el-descriptions-item label="付款方式" v-if="orderDetail.paymentMethod">{{ orderDetail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="買家備註" :span="2" v-if="orderDetail.buyerRemark">{{ orderDetail.buyerRemark }}</el-descriptions-item>
        <el-descriptions-item label="賣家備註" :span="2">
          <el-input v-model="orderDetail.sellerRemark" type="textarea" :rows="2" placeholder="輸入備註" />
          <el-button type="primary" size="small" style="margin-top: 8px" @click="handleUpdateRemark">儲存備註</el-button>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">商品明細</el-divider>
      <el-table :data="orderDetail?.orderItems || []" border>
        <el-table-column prop="skuImage" label="圖片" width="80" align="center">
          <template #default="scope">
            <el-image v-if="scope.row.skuImage" :src="getImageUrl(scope.row.skuImage)" style="width: 50px; height: 50px" fit="cover" :preview-src-list="[getImageUrl(scope.row.skuImage)]" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名稱" min-width="150" />
        <el-table-column prop="skuName" label="規格" width="120" />
        <el-table-column prop="unitPrice" label="單價" width="100" align="right">
          <template #default="scope">NT$ {{ scope.row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="數量" width="80" align="center" />
        <el-table-column prop="totalPrice" label="小計" width="100" align="right">
          <template #default="scope">NT$ {{ scope.row.totalPrice }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 出貨對話框 -->
    <el-dialog title="訂單出貨" v-model="shipOpen" width="500px" append-to-body>
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="物流方式" prop="shippingMethod">
          <el-select v-model="shipForm.shippingMethod" placeholder="請選擇" style="width: 100%">
            <el-option label="宅配" value="HOME_DELIVERY" />
            <el-option label="超商取貨" value="CVS_PICKUP" />
            <el-option label="郵寄" value="POST" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流單號" prop="shippingNo">
          <el-input v-model="shipForm.shippingNo" placeholder="請輸入物流單號" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitShip">確認出貨</el-button>
        <el-button @click="shipOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 取消訂單對話框 -->
    <el-dialog title="取消訂單" v-model="cancelOpen" width="500px" append-to-body>
      <el-form ref="cancelFormRef" :model="cancelForm" :rules="cancelRules" label-width="100px">
        <el-form-item label="取消原因" prop="reason">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="請輸入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitCancel">確認取消</el-button>
        <el-button @click="cancelOpen = false">返 回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopOrder">
import { ref, reactive, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { listOrder, getOrder, shipOrder, cancelOrder, updateOrderRemark, updatePayStatus, updateShipStatus } from '@/api/shop/order'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const loading = ref(false)
const showSearch = ref(true)
const orderList = ref([])
const total = ref(0)
const detailOpen = ref(false)
const shipOpen = ref(false)
const cancelOpen = ref(false)
const orderDetail = ref(null)
const dateRange = ref([])

const queryFormRef = ref(null)
const shipFormRef = ref(null)
const cancelFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: undefined,
  receiverName: undefined,
  status: undefined,
  payStatus: undefined
})

const shipForm = ref({})
const cancelForm = ref({})

const shipRules = {
  shippingMethod: [{ required: true, message: '請選擇物流方式', trigger: 'change' }],
  shippingNo: [{ required: true, message: '請輸入物流單號', trigger: 'blur' }]
}

const cancelRules = {
  reason: [{ required: true, message: '請輸入取消原因', trigger: 'blur' }]
}

const orderStatusOptions = [
  { value: 'PENDING', label: '待付款' },
  { value: 'PAID', label: '已付款' },
  { value: 'PROCESSING', label: '處理中' },
  { value: 'SHIPPED', label: '已出貨' },
  { value: 'DELIVERED', label: '已簽收' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

const orderStatusMap = {
  PENDING: { label: '待付款', type: 'warning' },
  PAID: { label: '已付款', type: 'success' },
  PROCESSING: { label: '處理中', type: 'primary' },
  SHIPPED: { label: '已出貨', type: 'primary' },
  DELIVERED: { label: '已簽收', type: 'success' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' }
}

const payStatusMap = {
  UNPAID: { label: '未付款', type: 'warning' },
  PAID: { label: '已付款', type: 'success' },
  REFUNDED: { label: '已退款', type: 'info' }
}

const shipStatusMap = {
  UNSHIPPED: { label: '未出貨', type: 'info' },
  SHIPPED: { label: '已出貨', type: 'primary' },
  DELIVERED: { label: '已簽收', type: 'success' }
}

onMounted(() => {
  getList()
})

function getList() {
  loading.value = true
  const params = { ...queryParams }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  listOrder(params).then(response => {
    orderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getOrderStatusType(status) {
  return orderStatusMap[status]?.type || 'info'
}

function getOrderStatusLabel(status) {
  return orderStatusMap[status]?.label || status
}

function getPayStatusType(status) {
  return payStatusMap[status]?.type || 'info'
}

function getPayStatusLabel(status) {
  return payStatusMap[status]?.label || status
}

function getShipStatusType(status) {
  return shipStatusMap[status]?.type || 'info'
}

function getShipStatusLabel(status) {
  return shipStatusMap[status]?.label || status
}

function canShip(row) {
  return row.payStatus === 'PAID' && row.shipStatus === 'UNSHIPPED'
}

function canCancel(row) {
  return row.status === 'PENDING' || row.status === 'PAID'
}

// 檢查是否有修改付款狀態的權限
function hasPayStatusPermission() {
  return userStore.permissions.includes('*:*:*') || userStore.permissions.includes('shop:order:updatePayStatus')
}

// 檢查是否有修改物流狀態的權限
function hasShipStatusPermission() {
  return userStore.permissions.includes('*:*:*') || userStore.permissions.includes('shop:order:updateShipStatus')
}

// 可以更新付款狀態（訂單未完成且有權限）
function canUpdatePayStatus(row) {
  return hasPayStatusPermission() && row.status !== 'COMPLETED' && row.status !== 'CANCELLED'
}

// 可以更新物流狀態（訂單未完成且有權限）
function canUpdateShipStatus(row) {
  return hasShipStatusPermission() && row.status !== 'COMPLETED' && row.status !== 'CANCELLED'
}

// 格式化金額（千分位）
function formatAmount(amount) {
  if (amount == null) return '0'
  return Number(amount).toLocaleString('zh-TW')
}

// 更新付款狀態
function handleUpdatePayStatus(row, newStatus) {
  if (row.payStatus === newStatus) return

  const statusLabel = getPayStatusLabel(newStatus)
  proxy.$modal.confirm(`確定將此訂單的付款狀態改為「${statusLabel}」？`).then(() => {
    updatePayStatus(row.orderId, newStatus).then(() => {
      proxy.$modal.msgSuccess('付款狀態已更新')
      getList()
    })
  }).catch(() => {})
}

// 更新物流狀態
function handleUpdateShipStatus(row, newStatus) {
  if (row.shipStatus === newStatus) return

  const statusLabel = getShipStatusLabel(newStatus)
  proxy.$modal.confirm(`確定將此訂單的物流狀態改為「${statusLabel}」？`).then(() => {
    updateShipStatus(row.orderId, newStatus).then(() => {
      proxy.$modal.msgSuccess('物流狀態已更新')
      getList()
    })
  }).catch(() => {})
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  dateRange.value = []
  handleQuery()
}

function handleDetail(row) {
  getOrder(row.orderId).then(response => {
    orderDetail.value = response.data
    detailOpen.value = true
  })
}

function handleShip(row) {
  shipForm.value = {
    orderId: row.orderId,
    shippingMethod: undefined,
    shippingNo: undefined
  }
  shipOpen.value = true
}

function submitShip() {
  shipFormRef.value?.validate(valid => {
    if (valid) {
      shipOrder(shipForm.value).then(() => {
        proxy.$modal.msgSuccess('出貨成功')
        shipOpen.value = false
        getList()
      })
    }
  })
}

function handleCancel(row) {
  cancelForm.value = {
    orderId: row.orderId,
    reason: undefined
  }
  cancelOpen.value = true
}

function submitCancel() {
  cancelFormRef.value?.validate(valid => {
    if (valid) {
      cancelOrder(cancelForm.value.orderId, cancelForm.value.reason).then(() => {
        proxy.$modal.msgSuccess('取消成功')
        cancelOpen.value = false
        getList()
      })
    }
  })
}

function handleUpdateRemark() {
  updateOrderRemark({
    orderId: orderDetail.value.orderId,
    sellerRemark: orderDetail.value.sellerRemark
  }).then(() => {
    proxy.$modal.msgSuccess('備註已儲存')
  })
}

function handleExport() {
  proxy.$modal.msgWarning('匯出功能開發中')
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}
</script>

<style scoped>
.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>
