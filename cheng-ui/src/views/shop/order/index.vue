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
      <el-table-column prop="shippingMethod" label="配送方式" min-width="120" align="center">
        <template #default="scope">
          <el-tag :type="getShippingMethodType(scope.row.shippingMethod)" size="small">
            {{ getShippingMethodLabel(scope.row.shippingMethod) }}
          </el-tag>
          <div v-if="scope.row.cvsStoreName" class="cvs-store-mini">
            {{ scope.row.cvsStoreName }}
          </div>
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
        <el-descriptions-item label="配送方式">
          <el-tag :type="getShippingMethodType(orderDetail.shippingMethod)" size="small">
            {{ getShippingMethodLabel(orderDetail.shippingMethod) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ getPaymentMethodLabel(orderDetail.paymentMethod) }}</el-descriptions-item>
        <!-- 超商取貨資訊 -->
        <template v-if="orderDetail.cvsStoreName">
          <el-descriptions-item label="取貨門市" :span="2">
            <div class="cvs-store-info">
              <el-tag type="primary" size="small">{{ getShippingMethodLabel(orderDetail.shippingMethod) }}</el-tag>
              <span class="store-name">{{ orderDetail.cvsStoreName }}</span>
              <span class="store-id" v-if="orderDetail.cvsStoreId">（代號：{{ orderDetail.cvsStoreId }}）</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="門市地址" :span="2">{{ orderDetail.cvsStoreAddress || orderDetail.receiverAddress }}</el-descriptions-item>
        </template>
        <!-- 宅配地址 -->
        <el-descriptions-item v-else label="收件地址" :span="2">{{ orderDetail.receiverAddress }}</el-descriptions-item>
        <!-- 物流單號 -->
        <el-descriptions-item label="物流單號" v-if="orderDetail.shippingNo" :span="2">
          <span class="shipping-no">{{ orderDetail.shippingNo }}</span>
          <el-button type="primary" link size="small" @click="copyText(orderDetail.shippingNo)">複製</el-button>
        </el-descriptions-item>
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
    <el-dialog title="訂單出貨" v-model="shipOpen" width="550px" append-to-body>
      <div class="ship-dialog-content" v-if="shipForm.order">
        <!-- 配送方式 -->
        <div class="ship-info-row">
          <span class="label">配送方式</span>
          <el-tag :type="getShippingMethodType(shipForm.order.shippingMethod)">
            {{ getShippingMethodLabel(shipForm.order.shippingMethod) }}
          </el-tag>
        </div>

        <!-- 超商取貨資訊 -->
        <template v-if="isCvsOrder(shipForm.order)">
          <div class="ship-section">
            <div class="section-title">取貨門市</div>
            <div class="cvs-store-box">
              <div class="store-name">{{ shipForm.order.cvsStoreName || '未設定門市' }}</div>
              <div class="store-id" v-if="shipForm.order.cvsStoreId">門市代號：{{ shipForm.order.cvsStoreId }}</div>
              <div class="store-address">{{ shipForm.order.cvsStoreAddress || shipForm.order.receiverAddress }}</div>
            </div>
          </div>

          <!-- 物流單號已存在 -->
          <template v-if="shipForm.order.shippingNo">
            <div class="ship-info-row">
              <span class="label">物流單號</span>
              <span class="shipping-no">{{ shipForm.order.shippingNo }}</span>
              <el-button type="primary" link size="small" @click="copyText(shipForm.order.shippingNo)">複製</el-button>
            </div>

            <el-alert type="info" :closable="false" class="ship-tips">
              <template #title>
                <div class="tips-title">出貨提示</div>
              </template>
              <ol class="tips-list">
                <li>請至綠界廠商後台列印託運單</li>
                <li>攜帶商品與託運單至指定超商寄件</li>
                <li>確認寄件後點擊「確認出貨」</li>
              </ol>
              <el-button type="primary" size="small" @click="openEcpayBackend" style="margin-top: 8px">
                前往綠界後台
              </el-button>
            </el-alert>
          </template>

          <!-- 物流單號未產生（異常情況） -->
          <template v-else>
            <el-alert type="warning" :closable="false" class="ship-tips">
              <template #title>
                <div class="tips-title">物流單號尚未產生</div>
              </template>
              <div class="tips-content">
                可能原因：綠界物流 API 呼叫失敗或系統處理延遲
              </div>
              <div class="tips-actions">
                <el-button type="primary" size="small" :loading="recreatingLogistics" @click="handleRecreateLogistics">
                  重新建立物流單
                </el-button>
                <el-button size="small" @click="showManualInput = true" v-if="!showManualInput">
                  手動輸入單號
                </el-button>
              </div>
            </el-alert>

            <el-form-item v-if="showManualInput" label="物流單號" label-width="100px" style="margin-top: 16px">
              <el-input v-model="shipForm.shippingNo" placeholder="請輸入物流單號" />
            </el-form-item>
          </template>
        </template>

        <!-- 宅配資訊 -->
        <template v-else>
          <div class="ship-section">
            <div class="section-title">收件資訊</div>
            <div class="receiver-box">
              <div class="receiver-name">{{ shipForm.order.receiverName }} {{ shipForm.order.receiverMobile }}</div>
              <div class="receiver-address">{{ shipForm.order.receiverAddress }}</div>
            </div>
          </div>

          <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
            <el-form-item label="物流單號" prop="shippingNo">
              <el-input v-model="shipForm.shippingNo" placeholder="請輸入物流單號" />
            </el-form-item>
          </el-form>
        </template>
      </div>

      <template #footer>
        <el-button type="primary" @click="submitShip" :disabled="!canSubmitShip">確認出貨</el-button>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { listOrder, getOrder, shipOrder, cancelOrder, updateOrderRemark, updatePayStatus, updateShipStatus, recreateLogistics } from '@/api/shop/order'
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
const recreatingLogistics = ref(false)
const showManualInput = ref(false)

const shipRules = {
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

const shippingMethodMap = {
  HOME_DELIVERY: { label: '宅配到府', type: 'info' },
  CVS_711: { label: '7-11 超取', type: 'success' },
  CVS_FAMILY: { label: '全家超取', type: 'primary' },
  CVS_HILIFE: { label: '萊爾富超取', type: 'warning' },
  STORE_PICKUP: { label: '門市自取', type: 'info' }
}

const paymentMethodMap = {
  COD: '貨到付款',
  ECPAY: '綠界金流',
  CREDIT_CARD: '信用卡',
  LINE_PAY: 'LINE Pay',
  BANK_TRANSFER: '銀行轉帳'
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

function getShippingMethodType(method) {
  return shippingMethodMap[method]?.type || ''
}

function getShippingMethodLabel(method) {
  return shippingMethodMap[method]?.label || method || '未指定'
}

function getPaymentMethodLabel(method) {
  return paymentMethodMap[method] || method || '未指定'
}

function copyText(text) {
  navigator.clipboard.writeText(text)
    .then(() => proxy.$modal.msgSuccess('已複製到剪貼簿'))
    .catch(() => proxy.$modal.msgError('複製失敗'))
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
  // 載入完整訂單資訊
  getOrder(row.orderId).then(response => {
    const order = response.data
    shipForm.value = {
      orderId: order.orderId,
      order: order,
      shippingNo: order.shippingNo || ''
    }
    showManualInput.value = false
    shipOpen.value = true
  })
}

// 判斷是否為超商取貨訂單
function isCvsOrder(order) {
  if (!order || !order.shippingMethod) return false
  return ['CVS_711', 'CVS_FAMILY', 'CVS_HILIFE'].includes(order.shippingMethod)
}

// 計算是否可以提交出貨
const canSubmitShip = computed(() => {
  if (!shipForm.value.order) return false
  const order = shipForm.value.order

  if (isCvsOrder(order)) {
    // 超商取貨：需要物流單號存在，或手動輸入
    return !!order.shippingNo || (showManualInput.value && !!shipForm.value.shippingNo)
  } else {
    // 宅配：需要輸入物流單號
    return !!shipForm.value.shippingNo
  }
})

// 重新建立物流訂單
async function handleRecreateLogistics() {
  if (!shipForm.value.orderId) return

  recreatingLogistics.value = true
  try {
    const response = await recreateLogistics(shipForm.value.orderId)
    if (response.code === 200) {
      proxy.$modal.msgSuccess('物流訂單建立成功')
      // 更新表單中的物流單號
      shipForm.value.order.shippingNo = response.shippingNo
      shipForm.value.shippingNo = response.shippingNo
    } else {
      proxy.$modal.msgError(response.msg || '建立失敗')
    }
  } catch (error) {
    proxy.$modal.msgError(error.message || '建立失敗')
  } finally {
    recreatingLogistics.value = false
  }
}

// 開啟綠界廠商後台
function openEcpayBackend() {
  // 測試環境：https://vendor-stage.ecpay.com.tw/
  // 正式環境：https://vendor.ecpay.com.tw/
  window.open('https://vendor.ecpay.com.tw/', '_blank')
}

function submitShip() {
  const order = shipForm.value.order
  if (!order) return

  // 確定要使用的物流單號
  let shippingNo = shipForm.value.shippingNo
  if (isCvsOrder(order) && order.shippingNo && !showManualInput.value) {
    // 超商取貨且已有物流單號，使用已存在的單號
    shippingNo = order.shippingNo
  }

  if (!shippingNo) {
    proxy.$modal.msgWarning('請輸入物流單號')
    return
  }

  const submitData = {
    orderId: shipForm.value.orderId,
    shippingNo: shippingNo
  }

  shipOrder(submitData).then(() => {
    proxy.$modal.msgSuccess('出貨成功')
    shipOpen.value = false
    getList()
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

/* 列表中的超商門市顯示 */
.cvs-store-mini {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
}

/* 詳情中的超商資訊 */
.cvs-store-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.cvs-store-info .store-name {
  font-weight: 500;
  color: #303133;
}

.cvs-store-info .store-id {
  color: #909399;
  font-size: 13px;
}

/* 物流單號 */
.shipping-no {
  font-family: 'Courier New', monospace;
  font-weight: 500;
  color: #409eff;
  margin-right: 8px;
}

/* 出貨對話框 */
.ship-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ship-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ship-info-row .label {
  color: #909399;
  min-width: 70px;
}

.ship-section {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.ship-section .section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
}

.cvs-store-box,
.receiver-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cvs-store-box .store-name,
.receiver-box .receiver-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.cvs-store-box .store-id {
  font-size: 13px;
  color: #909399;
}

.cvs-store-box .store-address,
.receiver-box .receiver-address {
  font-size: 13px;
  color: #606266;
}

.ship-tips {
  margin-top: 8px;
}

.ship-tips .tips-title {
  font-weight: 500;
}

.ship-tips .tips-list {
  margin: 8px 0 0;
  padding-left: 20px;
}

.ship-tips .tips-list li {
  margin-bottom: 4px;
  color: #606266;
}

.ship-tips .tips-content {
  margin: 8px 0;
  color: #606266;
}

.ship-tips .tips-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
