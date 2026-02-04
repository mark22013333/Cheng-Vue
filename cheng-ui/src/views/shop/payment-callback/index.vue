<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="訂單編號" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="請輸入訂單編號"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="付款方式" prop="paymentMethod">
        <el-select v-model="queryParams.paymentMethod" placeholder="請選擇" clearable style="width: 160px">
          <el-option v-for="item in paymentMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="回調來源" prop="callbackType">
        <el-select v-model="queryParams.callbackType" placeholder="請選擇" clearable style="width: 140px">
          <el-option label="伺服器" value="SERVER" />
          <el-option label="瀏覽器" value="BROWSER" />
        </el-select>
      </el-form-item>
      <el-form-item label="驗簽結果" prop="verifyStatus">
        <el-select v-model="queryParams.verifyStatus" placeholder="請選擇" clearable style="width: 120px">
          <el-option label="成功" :value="1" />
          <el-option label="失敗" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="交易編號" prop="tradeNo">
        <el-input
          v-model="queryParams.tradeNo"
          placeholder="請輸入交易編號"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="回調時間" prop="dateRange">
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

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="logList" style="width: 100%">
      <el-table-column prop="orderNo" label="訂單編號" min-width="180" show-overflow-tooltip />
      <el-table-column prop="paymentMethod" label="付款方式" min-width="120" align="center">
        <template #default="scope">
          {{ getPaymentMethodLabel(scope.row.paymentMethod) }}
        </template>
      </el-table-column>
      <el-table-column prop="callbackType" label="回調來源" min-width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.callbackType === 'SERVER' ? 'success' : 'info'">
            {{ scope.row.callbackType === 'SERVER' ? '伺服器' : '瀏覽器' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tradeNo" label="交易編號" min-width="160" show-overflow-tooltip />
      <el-table-column prop="rtnCode" label="回傳碼" min-width="90" align="center" />
      <el-table-column prop="verifyStatus" label="驗簽" min-width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.verifyStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.verifyStatus === 1 ? '成功' : '失敗' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="回調時間" min-width="160" align="center" />
      <el-table-column label="操作" min-width="120" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['shop:payment:callback:query']">
            查看
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

    <!-- 詳情對話框 -->
    <el-dialog title="回傳內容" v-model="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border v-if="detailRow">
        <el-descriptions-item label="訂單編號">{{ detailRow.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ getPaymentMethodLabel(detailRow.paymentMethod) }}</el-descriptions-item>
        <el-descriptions-item label="回調來源">{{ detailRow.callbackType === 'SERVER' ? '伺服器' : '瀏覽器' }}</el-descriptions-item>
        <el-descriptions-item label="交易編號">{{ detailRow.tradeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="回傳碼">{{ detailRow.rtnCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驗簽結果">
          {{ detailRow.verifyStatus === 1 ? '成功' : '失敗' }}
        </el-descriptions-item>
        <el-descriptions-item label="驗簽訊息" :span="2">
          {{ detailRow.verifyMessage || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">原始回傳內容</el-divider>
      <el-input v-model="detailRaw" type="textarea" :rows="12" readonly />
    </el-dialog>
  </div>
</template>

<script setup name="ShopPaymentCallbackLog">
import { ref, reactive, onMounted } from 'vue'
import { listPaymentCallbackLog } from '@/api/shop/paymentCallback'

const loading = ref(false)
const showSearch = ref(true)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])

const queryFormRef = ref(null)
const detailOpen = ref(false)
const detailRow = ref(null)
const detailRaw = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: undefined,
  paymentMethod: undefined,
  callbackType: undefined,
  verifyStatus: undefined,
  tradeNo: undefined
})

const paymentMethodOptions = [
  { value: 'ECPAY', label: '綠界金流' },
  { value: 'LINE_PAY', label: 'LINE Pay' },
  { value: 'CREDIT_CARD', label: '信用卡' },
  { value: 'ATM', label: 'ATM轉帳' },
  { value: 'CVS', label: '超商付款' },
  { value: 'COD', label: '貨到付款' }
]

const paymentMethodMap = paymentMethodOptions.reduce((acc, item) => {
  acc[item.value] = item.label
  return acc
}, {})

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
  listPaymentCallbackLog(params).then(response => {
    logList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getPaymentMethodLabel(code) {
  return paymentMethodMap[code] || code || '-'
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

function handleView(row) {
  detailRow.value = row
  detailRaw.value = formatJson(row.rawInfo)
  detailOpen.value = true
}

function formatJson(raw) {
  if (!raw) return ''
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return String(raw)
  }
}
</script>
