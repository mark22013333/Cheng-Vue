<template>
  <div class="checkout-page">
    <div class="checkout-container" v-loading="loading">
      <h1 class="page-title">確認訂單</h1>

      <!-- 收貨地址（僅宅配時顯示） -->
      <div class="checkout-section" v-if="!isCvsShipping">
        <div class="section-header">
          <h2>收貨地址</h2>
          <el-button text type="primary" @click="showAddressDialog = true">
            更換地址
          </el-button>
        </div>
        <div class="address-card" v-if="selectedAddress">
          <div class="address-info">
            <span class="receiver-name">{{ selectedAddress.receiverName }}</span>
            <span class="receiver-phone">{{ selectedAddress.receiverPhone }}</span>
            <el-tag v-if="selectedAddress.isDefault" type="primary" size="small">預設</el-tag>
          </div>
          <div class="address-detail">
            {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detailAddress }}
          </div>
        </div>
        <el-empty v-else description="請新增收貨地址">
          <el-button type="primary" @click="goAddAddress">新增地址</el-button>
        </el-empty>
      </div>

      <!-- 商品列表 -->
      <div class="checkout-section">
        <h2>商品資訊</h2>
        <div class="product-list">
          <div v-for="item in checkoutData.items" :key="item.cartId" class="product-item">
            <img :src="getImageUrl(item.skuImage || item.productImage)" class="product-image" />
            <div class="product-info">
              <div class="product-name">{{ item.productName }}</div>
              <div class="product-sku" v-if="item.skuName">規格：{{ item.skuName }}</div>
            </div>
            <div class="product-price">${{ formatPrice(item.price) }}</div>
            <div class="product-quantity">x{{ item.quantity }}</div>
            <div class="product-subtotal">${{ formatPrice(item.price * item.quantity) }}</div>
          </div>
        </div>
      </div>

      <!-- 訂單備註 -->
      <div class="checkout-section">
        <h2>訂單備註</h2>
        <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          placeholder="選填，可填寫配送時間等特殊要求"
          maxlength="200"
          show-word-limit
        />
      </div>

      <!-- 滿額禮物 -->
      <div class="checkout-section" v-if="checkoutData.availableGifts && checkoutData.availableGifts.length > 0">
        <h2>滿額禮物</h2>
        <div class="gift-list">
          <div
            v-for="gift in checkoutData.availableGifts"
            :key="gift.giftId"
            class="gift-option"
            :class="{ 'is-selected': selectedGiftId === gift.giftId }"
            @click="selectGift(gift)"
          >
            <el-image
              v-if="gift.imageUrl"
              :src="getImageUrl(gift.imageUrl)"
              class="gift-image"
              fit="cover"
            />
            <div class="gift-info">
              <div class="gift-name">{{ gift.name }}</div>
              <div class="gift-threshold">滿 ${{ formatPrice(gift.thresholdAmount) }} 可選</div>
            </div>
            <el-icon v-if="selectedGiftId === gift.giftId" class="gift-check">
              <CircleCheckFilled />
            </el-icon>
          </div>
        </div>
        <el-button v-if="selectedGiftId" text type="info" size="small" @click="selectedGiftId = null" style="margin-top: 8px;">
          取消選擇禮物
        </el-button>
      </div>

      <!-- 物流方式 -->
      <div class="checkout-section" v-if="shippingMethods.length > 0">
        <h2>配送方式</h2>
        <el-radio-group v-model="shippingMethod" class="shipping-group" @change="handleShippingChange">
          <el-radio
            v-for="method in shippingMethods"
            :key="method.code"
            :value="method.code"
            size="large"
          >
            <div class="shipping-option">
              <el-icon class="shipping-icon">
                <Location v-if="!method.requireCvsStore" />
                <Shop v-else />
              </el-icon>
              <div class="shipping-info">
                <span class="shipping-name">{{ method.name }}</span>
                <span class="shipping-desc">{{ method.description }}</span>
                <span class="shipping-fee" v-if="method.fee > 0">運費 ${{ formatPrice(method.fee) }}</span>
                <span class="shipping-fee free" v-else>免運費</span>
              </div>
            </div>
          </el-radio>
        </el-radio-group>

        <!-- 超商門市選擇 -->
        <div v-if="isCvsShipping" class="cvs-store-section">
          <div v-if="selectedCvsStore" class="cvs-store-card">
            <div class="store-info">
              <span class="store-name">{{ selectedCvsStore.storeName }}</span>
              <span class="store-address">{{ selectedCvsStore.storeAddress }}</span>
            </div>
            <el-button text type="primary" @click="openCvsMap">重新選擇</el-button>
          </div>
          <div v-else class="cvs-store-empty">
            <el-button type="primary" :loading="openingMap" @click="openCvsMap">
              <el-icon><Shop /></el-icon>
              選擇取貨門市
            </el-button>
          </div>

          <!-- 超商取貨收件人資訊 -->
          <div class="cvs-receiver-form">
            <div class="form-hint">
              <el-icon><InfoFilled /></el-icon>
              <span>以下資訊將用於物流出貨單，請務必填寫正確</span>
            </div>
            <el-form
              ref="cvsReceiverFormRef"
              :model="cvsReceiverForm"
              :rules="cvsReceiverRules"
              label-width="100px"
              @submit.prevent
            >
              <el-form-item label="取貨人姓名" prop="receiverName">
                <el-input
                  v-model="cvsReceiverForm.receiverName"
                  placeholder="中文 2~5 字 / 英文 4~10 字"
                  maxlength="10"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item label="取貨人手機" prop="receiverPhone">
                <el-input
                  v-model="cvsReceiverForm.receiverPhone"
                  placeholder="09 開頭，共 10 碼"
                  maxlength="10"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 付款方式 -->
      <div class="checkout-section" v-if="availablePaymentMethods.length > 0">
        <h2>付款方式</h2>
        <el-radio-group v-model="paymentMethod" class="payment-group">
          <el-radio
            v-for="method in filteredPaymentMethods"
            :key="method.code"
            :value="method.code"
            size="large"
          >
            <div class="payment-option">
              <span class="payment-name">{{ method.name }}</span>
              <span class="payment-desc">{{ method.description }}</span>
            </div>
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 金額明細 -->
      <div class="checkout-section amount-section">
        <div class="amount-row">
          <span>商品金額</span>
          <span>${{ formatPrice(checkoutData.productAmount) }}</span>
        </div>
        <div class="amount-row">
          <span>運費</span>
          <span v-if="calculatedShippingFee > 0">${{ formatPrice(calculatedShippingFee) }}</span>
          <span v-else class="free-shipping">免運費</span>
        </div>
        <div v-if="checkoutData.discountAmount > 0" class="amount-row discount">
          <span>折扣</span>
          <span>-${{ formatPrice(checkoutData.discountAmount) }}</span>
        </div>
        <div class="amount-row total">
          <span>應付金額</span>
          <span class="total-price">${{ formatPrice(totalPayableAmount) }}</span>
        </div>
      </div>

      <!-- 提交按鈕 -->
      <div class="checkout-footer">
        <div class="footer-info">
          共 <span class="highlight">{{ checkoutData.totalQuantity }}</span> 件商品，
          應付：<span class="total-price">${{ formatPrice(totalPayableAmount) }}</span>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          提交訂單
        </el-button>
      </div>
    </div>

    <!-- 選擇地址對話框 -->
    <el-dialog v-model="showAddressDialog" title="選擇收貨地址" width="600px">
      <div class="address-list">
        <div
          v-for="addr in checkoutData.addressList"
          :key="addr.addressId"
          class="address-option"
          :class="{ 'is-selected': selectedAddress?.addressId === addr.addressId }"
          @click="selectAddress(addr)"
        >
          <div class="address-info">
            <span class="receiver-name">{{ addr.receiverName }}</span>
            <span class="receiver-phone">{{ addr.receiverPhone }}</span>
            <el-tag v-if="addr.isDefault" type="primary" size="small">預設</el-tag>
          </div>
          <div class="address-detail">
            {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
          </div>
          <el-icon v-if="selectedAddress?.addressId === addr.addressId" class="check-icon">
            <CircleCheckFilled />
          </el-icon>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="goAddAddress">新增地址</el-button>
      </template>
    </el-dialog>

    <!-- 新增地址對話框 -->
    <el-dialog v-model="showAddAddressDialog" title="新增收貨地址" width="500px" :close-on-click-modal="false">
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="100px"
        @submit.prevent
      >
        <el-form-item label="收件人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="中文 2~5 字 / 英文 4~10 字" maxlength="10" show-word-limit />
        </el-form-item>
        <el-form-item label="手機號碼" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="09 開頭，共 10 碼" maxlength="10" />
        </el-form-item>
        <el-form-item label="縣市" prop="province">
          <el-select
            v-model="selectedCityCode"
            placeholder="輸入或選擇縣市"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="city in cityList"
              :key="city.code"
              :label="city.name"
              :value="city.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="區域" prop="city">
          <el-select
            v-model="addressForm.city"
            placeholder="輸入或選擇區域"
            filterable
            clearable
            :disabled="!selectedCityCode"
            style="width: 100%"
          >
            <el-option
              v-for="district in districtList"
              :key="district.name"
              :label="district.name"
              :value="district.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路/街">
          <el-input v-model="addressForm.street" placeholder="例：中山路、忠孝東路四段" maxlength="30" />
        </el-form-item>
        <el-form-item label="巷弄號樓" prop="number" class="address-detail-row">
          <div class="address-inputs">
            <el-input v-model="addressForm.lane" placeholder="巷" style="width: 70px">
              <template #append>巷</template>
            </el-input>
            <el-input v-model="addressForm.alley" placeholder="弄" style="width: 70px">
              <template #append>弄</template>
            </el-input>
            <el-input v-model="addressForm.number" placeholder="號" style="width: 70px">
              <template #append>號</template>
            </el-input>
            <el-input v-model="addressForm.floor" placeholder="樓" style="width: 70px">
              <template #append>樓</template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item label="郵遞區號">
          <el-input v-model="addressForm.postalCode" disabled style="width: 100px" />
          <span class="postal-hint">（自動帶入）</span>
        </el-form-item>
        <el-form-item label="設為預設">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddAddressDialog = false">取消</el-button>
        <el-button type="primary" :loading="addingAddress" @click="handleAddAddress">確定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, Location, Shop, InfoFilled } from '@element-plus/icons-vue'
import { getCheckoutPreview, submitOrder } from '@/api/shop/checkout'
import { createEcpayPayment } from '@/api/shop/payment'
import { addAddress } from '@/api/shop/address'
import { listPaymentMethods } from '@/api/shop/front'
import { getShippingMethods, getMapFormHtml, getCvsStore } from '@/api/shop/logistics'
import { getMemberToken } from '@/utils/memberAuth'
import { getCityList, getDistrictList, getPostalCode } from '@/utils/taiwan-address'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const showAddressDialog = ref(false)
const showAddAddressDialog = ref(false)
const addingAddress = ref(false)
const remark = ref('')
const paymentMethod = ref('')
const availablePaymentMethods = ref([])
const selectedAddress = ref(null)
const selectedGiftId = ref(null)
const addressFormRef = ref(null)

// 物流相關
const shippingMethod = ref('HOME_DELIVERY')
const shippingMethods = ref([])
const cvsStoreKey = ref('')
const selectedCvsStore = ref(null)
const cvsPollingTimer = ref(null)
const openingMap = ref(false)
const cvsReceiverFormRef = ref(null)

// 超商取貨收件人表單
const cvsReceiverForm = reactive({
  receiverName: '',
  receiverPhone: ''
})

// 超商取貨收件人驗證規則
const cvsReceiverRules = {
  receiverName: [
    { required: true, message: '請輸入取貨人姓名', trigger: 'blur' },
    { validator: validateReceiverName, trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '請輸入取貨人手機', trigger: 'blur' },
    { pattern: /^09\d{8}$/, message: '請輸入正確的手機號碼（09 開頭共 10 碼）', trigger: 'blur' }
  ]
}

// 計算屬性：是否為超商取貨
const isCvsShipping = computed(() => {
  const method = shippingMethods.value.find(m => m.code === shippingMethod.value)
  return method?.requireCvsStore ?? false
})

// 計算屬性：根據物流方式計算運費
const calculatedShippingFee = computed(() => {
  const method = shippingMethods.value.find(m => m.code === shippingMethod.value)
  return method?.fee ?? checkoutData.shippingFee
})

// 計算屬性：總應付金額
const totalPayableAmount = computed(() => {
  return (checkoutData.productAmount || 0) + calculatedShippingFee.value - (checkoutData.discountAmount || 0)
})

// 計算屬性：過濾付款方式（超商取貨不支援貨到付款）
const filteredPaymentMethods = computed(() => {
  if (isCvsShipping.value) {
    return availablePaymentMethods.value.filter(m => m.code !== 'COD')
  }
  return availablePaymentMethods.value
})

// 計算屬性：是否可以提交訂單
const canSubmit = computed(() => {
  if (isCvsShipping.value) {
    // 超商取貨需要選擇門市且填寫收件人資訊
    return !!selectedCvsStore.value &&
           !!cvsReceiverForm.receiverName &&
           /^09\d{8}$/.test(cvsReceiverForm.receiverPhone)
  }
  return !!selectedAddress.value
})

// 縣市區資料
const cityList = getCityList()
const selectedCityCode = ref('')

// 根據選擇的縣市取得區域列表
const districtList = computed(() => {
  if (!selectedCityCode.value) return []
  return getDistrictList(selectedCityCode.value)
})

// 新增地址表單
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  street: '',      // 路/街
  lane: '',        // 巷
  alley: '',       // 弄
  number: '',      // 號
  floor: '',       // 樓
  detailAddress: '',
  postalCode: '',
  isDefault: true
})

// 當縣市變更時，清空區域和郵遞區號
watch(selectedCityCode, (newVal, oldVal) => {
  if (oldVal && newVal !== oldVal) {
    addressForm.city = ''
    addressForm.postalCode = ''
  }
  // 更新 province 欄位
  const city = cityList.find(c => c.code === newVal)
  if (city) {
    addressForm.province = city.name
  }
})

// 當區域變更時，自動填入郵遞區號
watch(() => addressForm.city, (newVal) => {
  if (newVal && selectedCityCode.value) {
    const zip = getPostalCode(selectedCityCode.value, newVal)
    if (zip) {
      addressForm.postalCode = zip
    }
  }
})

// 計算字串的位元組長度（中文字 2 位元組，英文字 1 位元組）
function getByteLength(str) {
  if (!str) return 0
  let len = 0
  for (let i = 0; i < str.length; i++) {
    if (str.charCodeAt(i) > 127) {
      len += 2
    } else {
      len += 1
    }
  }
  return len
}

// ECPay 收件人姓名驗證器
function validateReceiverName(rule, value, callback) {
  if (!value) {
    callback(new Error('請輸入收件人姓名'))
    return
  }
  const byteLen = getByteLength(value)
  if (byteLen < 4) {
    callback(new Error('姓名太短，中文至少 2 個字，英文至少 4 個字'))
    return
  }
  if (byteLen > 10) {
    callback(new Error('姓名太長，中文最多 5 個字，英文最多 10 個字'))
    return
  }
  callback()
}

const addressRules = {
  receiverName: [
    { required: true, message: '請輸入收件人姓名', trigger: 'blur' },
    { validator: validateReceiverName, trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '請輸入手機號碼', trigger: 'blur' },
    { pattern: /^09\d{8}$/, message: '請輸入正確的手機號碼（09 開頭共 10 碼）', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '請選擇縣市', trigger: 'change' }
  ],
  city: [
    { required: true, message: '請選擇區域', trigger: 'change' }
  ],
  number: [
    { required: true, message: '請輸入門牌號碼', trigger: 'blur' }
  ]
}

const checkoutData = reactive({
  items: [],
  addressList: [],
  totalQuantity: 0,
  productAmount: 0,
  shippingFee: 0,
  discountAmount: 0,
  payableAmount: 0,
  availableGifts: []
})

onMounted(async () => {
  // 檢查登入狀態，未登入則跳轉到商城登入頁
  if (!getMemberToken()) {
    ElMessage.warning('請先登入後再結帳')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  // 產生 CVS store key
  cvsStoreKey.value = generateCvsStoreKey()
  // 載入付款方式
  await fetchPaymentMethods()
  // 載入結帳預覽（取得商品金額後再載入物流方式）
  await fetchCheckoutPreview()
  // 載入物流方式
  await fetchShippingMethods()
})

onUnmounted(() => {
  // 清理輪詢計時器
  if (cvsPollingTimer.value) {
    clearInterval(cvsPollingTimer.value)
    cvsPollingTimer.value = null
  }
})

async function fetchPaymentMethods() {
  try {
    const response = await listPaymentMethods()
    if (response.code === 200 && response.data) {
      availablePaymentMethods.value = response.data
      // 預設選擇第一個付款方式
      if (response.data.length > 0) {
        paymentMethod.value = response.data[0].code
      }
    }
  } catch (error) {
    console.error('載入付款方式失敗', error)
  }
}

async function fetchShippingMethods() {
  try {
    const response = await getShippingMethods(checkoutData.productAmount)
    if (response.code === 200 && response.data) {
      shippingMethods.value = response.data
      // 預設選擇第一個物流方式
      if (response.data.length > 0 && !shippingMethod.value) {
        shippingMethod.value = response.data[0].code
      }
    }
  } catch (error) {
    console.error('載入物流方式失敗', error)
  }
}

function generateCvsStoreKey() {
  return 'cvs_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

function handleShippingChange(newMethod) {
  // 切換物流方式時，清除已選的門市
  selectedCvsStore.value = null

  // 如果切換到超商取貨，產生新的 storeKey
  const method = shippingMethods.value.find(m => m.code === newMethod)
  if (method?.requireCvsStore) {
    cvsStoreKey.value = generateCvsStoreKey()
  }

  // 如果當前付款方式是 COD 且切換到超商取貨，自動切換付款方式
  if (method?.requireCvsStore && paymentMethod.value === 'COD') {
    const firstOnlineMethod = filteredPaymentMethods.value[0]
    if (firstOnlineMethod) {
      paymentMethod.value = firstOnlineMethod.code
    }
  }
}

async function openCvsMap() {
  if (!shippingMethod.value) {
    ElMessage.warning('請先選擇物流方式')
    return
  }

  openingMap.value = true

  try {
    const response = await getMapFormHtml(shippingMethod.value, cvsStoreKey.value)
    if (response.code === 200 && response.data?.formHtml) {
      // 開啟新視窗顯示電子地圖
      const mapWindow = window.open('', '_blank', 'width=800,height=600')
      if (mapWindow) {
        mapWindow.document.write(response.data.formHtml)
        mapWindow.document.close()

        // 開始輪詢門市選取結果
        startCvsPolling()
      } else {
        ElMessage.warning('請允許瀏覽器開啟新視窗')
      }
    } else {
      ElMessage.error(response.msg || '無法開啟門市地圖')
    }
  } catch (error) {
    console.error('開啟門市地圖失敗', error)
    ElMessage.error('開啟門市地圖失敗')
  } finally {
    openingMap.value = false
  }
}

function startCvsPolling() {
  // 清除舊的計時器
  if (cvsPollingTimer.value) {
    clearInterval(cvsPollingTimer.value)
  }

  let pollCount = 0
  const maxPolls = 60 // 最多輪詢 60 次（3 分鐘）

  cvsPollingTimer.value = setInterval(async () => {
    pollCount++

    if (pollCount > maxPolls) {
      clearInterval(cvsPollingTimer.value)
      cvsPollingTimer.value = null
      return
    }

    try {
      const response = await getCvsStore(cvsStoreKey.value)
      if (response.code === 200 && response.data?.storeId) {
        // 取得門市資訊
        selectedCvsStore.value = response.data
        ElMessage.success('已選取門市：' + response.data.storeName)

        // 停止輪詢
        clearInterval(cvsPollingTimer.value)
        cvsPollingTimer.value = null
      }
    } catch (error) {
      console.error('查詢門市資訊失敗', error)
    }
  }, 3000) // 每 3 秒輪詢一次
}

async function fetchCheckoutPreview(addressId) {
  loading.value = true
  try {
    const response = await getCheckoutPreview(addressId)
    if (response.code === 200) {
      const data = response.data
      checkoutData.items = data.items || []
      checkoutData.addressList = data.addressList || []
      checkoutData.totalQuantity = data.totalQuantity || 0
      checkoutData.productAmount = data.productAmount || 0
      checkoutData.shippingFee = data.shippingFee || 0
      checkoutData.discountAmount = data.discountAmount || 0
      checkoutData.payableAmount = data.payableAmount || 0
      checkoutData.availableGifts = data.availableGifts || []

      // 清除已選禮物（如果不在可用列表中）
      if (selectedGiftId.value) {
        const stillAvailable = checkoutData.availableGifts.some(g => g.giftId === selectedGiftId.value)
        if (!stillAvailable) {
          selectedGiftId.value = null
        }
      }

      if (data.address) {
        selectedAddress.value = data.address
      }

      // 如果沒有商品，跳回購物車
      if (checkoutData.items.length === 0) {
        ElMessage.warning('請先選擇要結帳的商品')
        router.push('/cart')
      }
    }
  } catch (error) {
    console.error('載入結帳資訊失敗', error)
    ElMessage.error('載入結帳資訊失敗')
    router.push('/cart')
  } finally {
    loading.value = false
  }
}

function getImageUrl(url) {
  if (!url) return '/placeholder-image.png'
  if (url.startsWith('http')) return url
  if (url.startsWith('/profile')) return url
  return '/profile' + (url.startsWith('/') ? url : '/' + url)
}

function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}

function selectGift(gift) {
  if (selectedGiftId.value === gift.giftId) {
    selectedGiftId.value = null
  } else {
    selectedGiftId.value = gift.giftId
  }
}

function selectAddress(addr) {
  selectedAddress.value = addr
  showAddressDialog.value = false
  // 重新計算運費
  fetchCheckoutPreview(addr.addressId)
}

function goAddAddress() {
  // 重置表單
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.street = ''
  addressForm.lane = ''
  addressForm.alley = ''
  addressForm.number = ''
  addressForm.floor = ''
  addressForm.detailAddress = ''
  addressForm.postalCode = ''
  addressForm.isDefault = true
  selectedCityCode.value = ''

  showAddressDialog.value = false
  showAddAddressDialog.value = true
}

// 組合詳細地址
function composeDetailAddress() {
  let address = addressForm.street || ''
  if (addressForm.lane) address += addressForm.lane + '巷'
  if (addressForm.alley) address += addressForm.alley + '弄'
  if (addressForm.number) address += addressForm.number + '號'
  if (addressForm.floor) address += addressForm.floor + '樓'
  return address
}

async function handleAddAddress() {
  if (!addressFormRef.value) return

  try {
    await addressFormRef.value.validate()
    addingAddress.value = true

    // 組合詳細地址
    const detailAddress = composeDetailAddress()

    const data = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.province,
      city: addressForm.city,
      district: addressForm.district,
      detailAddress: detailAddress,
      postalCode: addressForm.postalCode,
      isDefault: addressForm.isDefault
    }

    const response = await addAddress(data)
    if (response.code === 200) {
      ElMessage.success('新增地址成功')
      showAddAddressDialog.value = false

      // 重新載入結帳預覽（包含新地址）
      await fetchCheckoutPreview()

      // 如果是第一個地址或設為預設，自動選中
      if (checkoutData.addressList.length > 0) {
        const newAddr = checkoutData.addressList.find(a => a.isDefault) || checkoutData.addressList[0]
        selectedAddress.value = newAddr
      }
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '新增地址失敗')
    }
  } finally {
    addingAddress.value = false
  }
}

async function handleEcpayPayment(orderNo) {
  try {
    const res = await createEcpayPayment(orderNo)
    if (res.code === 200 && res.data?.formHtml) {
      // 將 ECPay 自動提交表單插入頁面並執行
      const container = document.createElement('div')
      container.innerHTML = res.data.formHtml
      document.body.appendChild(container)
      const form = container.querySelector('form')
      if (form) {
        form.submit()
      }
    } else {
      ElMessage.error(res.msg || '建立付款失敗，請至訂單頁面重新付款')
      router.push(`/member/order/${orderNo}`)
    }
  } catch (error) {
    ElMessage.error('建立付款失敗，請至訂單頁面重新付款')
    router.push(`/member/order/${orderNo}`)
  }
}

async function handleSubmit() {
  // 驗證
  if (isCvsShipping.value) {
    if (!selectedCvsStore.value) {
      ElMessage.warning('請選擇取貨門市')
      return
    }
    // 驗證超商取貨收件人表單
    if (cvsReceiverFormRef.value) {
      try {
        await cvsReceiverFormRef.value.validate()
      } catch {
        ElMessage.warning('請填寫正確的取貨人資訊')
        return
      }
    }
  } else {
    if (!selectedAddress.value) {
      ElMessage.warning('請選擇收貨地址')
      return
    }
  }

  try {
    await ElMessageBox.confirm(
      `確認提交訂單？應付金額：$${formatPrice(totalPayableAmount.value)}`,
      '確認訂單',
      {
        confirmButtonText: '確認提交',
        cancelButtonText: '再想想',
        type: 'info'
      }
    )

    submitting.value = true

    const submitData = {
      remark: remark.value,
      paymentMethod: paymentMethod.value,
      shippingMethod: shippingMethod.value
    }

    // 根據物流方式設定收貨資訊
    if (isCvsShipping.value) {
      submitData.cvsStoreKey = cvsStoreKey.value
      // 超商取貨收件人資訊（物流出貨單必填）
      submitData.receiverName = cvsReceiverForm.receiverName
      submitData.receiverPhone = cvsReceiverForm.receiverPhone
    } else {
      submitData.addressId = selectedAddress.value.addressId
    }

    if (selectedGiftId.value) {
      submitData.giftId = selectedGiftId.value
    }
    const response = await submitOrder(submitData)

    if (response.code === 200) {
      const result = response.data
      // 若需要線上付款（ECPay），導向金流頁面
      if (result.needOnlinePayment) {
        await handleEcpayPayment(result.orderNo)
      } else {
        ElMessage.success('訂單提交成功')
        router.push(`/order-success/${result.orderNo}`)
      }
    } else {
      ElMessage.error(response.msg || '訂單提交失敗')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '訂單提交失敗')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.checkout-page {
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
  padding: 24px;
}

.checkout-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 24px;
}

.checkout-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkout-section h2 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 16px;
}

/* 地址卡片 */
.address-card {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.receiver-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.receiver-phone {
  font-size: 14px;
  color: #606266;
}

.address-detail {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

/* 商品列表 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.product-sku {
  font-size: 12px;
  color: #909399;
}

.product-price,
.product-quantity {
  font-size: 14px;
  color: #606266;
  width: 80px;
  text-align: center;
}

.product-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  width: 100px;
  text-align: right;
}

/* 物流方式 */
.shipping-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.shipping-group :deep(.el-radio) {
  flex: 1;
  min-width: 200px;
  max-width: calc(50% - 8px);
  height: auto;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-right: 0;
  align-items: flex-start;
}

.shipping-group :deep(.el-radio.is-checked) {
  border-color: #409eff;
  background: #ecf5ff;
}

.shipping-group :deep(.el-radio__input) {
  margin-top: 4px;
}

.shipping-group :deep(.el-radio__label) {
  padding-left: 12px;
  white-space: normal;
}

.shipping-option {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.shipping-icon {
  font-size: 24px;
  color: #409eff;
  flex-shrink: 0;
}

.shipping-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.shipping-name {
  font-size: 15px;
  color: #303133;
  font-weight: 600;
  line-height: 1.4;
}

.shipping-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.shipping-fee {
  font-size: 13px;
  color: #f56c6c;
  font-weight: 500;
  margin-top: 4px;
}

.shipping-fee.free {
  color: #67c23a;
}

/* 超商門市選擇 */
.cvs-store-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.cvs-store-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f0f9eb;
  border-radius: 8px;
  border: 1px solid #e1f3d8;
}

.store-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.store-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.store-address {
  font-size: 12px;
  color: #606266;
}

.cvs-store-empty {
  text-align: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
}

/* 超商取貨收件人表單 */
.cvs-receiver-form {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.cvs-receiver-form .form-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  padding: 10px 12px;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  font-size: 13px;
  color: #e6a23c;
}

.cvs-receiver-form .form-hint .el-icon {
  font-size: 16px;
}

.cvs-receiver-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.cvs-receiver-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* 付款方式 */
.payment-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.payment-group :deep(.el-radio) {
  flex: 1;
  min-width: 200px;
  max-width: calc(50% - 8px);
  height: auto;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-right: 0;
  align-items: flex-start;
}

.payment-group :deep(.el-radio.is-checked) {
  border-color: #409eff;
  background: #ecf5ff;
}

.payment-group :deep(.el-radio__input) {
  margin-top: 4px;
}

.payment-group :deep(.el-radio__label) {
  padding-left: 12px;
  white-space: normal;
}

.payment-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.payment-name {
  font-size: 15px;
  color: #303133;
  font-weight: 600;
  line-height: 1.4;
}

.payment-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 金額明細 */
.amount-section {
  background: #fafafa;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
}

.amount-row.discount {
  color: #f56c6c;
}

.amount-row.total {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
  margin-top: 8px;
}

.free-shipping {
  color: #67c23a;
}

.total-price {
  font-size: 24px;
  font-weight: 600;
  color: #f56c6c;
}

/* 底部 */
.checkout-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  position: sticky;
  bottom: 24px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.footer-info {
  font-size: 14px;
  color: #606266;
}

.footer-info .highlight {
  color: var(--mall-primary, #409eff);
  font-weight: 600;
}

/* 地址選擇列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.address-option {
  position: relative;
  padding: 16px;
  background: #fafafa;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-option:hover {
  background: #f0f2f5;
}

.address-option.is-selected {
  border-color: var(--mall-primary, #409eff);
  background: var(--mall-primary-light, #ecf5ff);
}

.check-icon {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 20px;
  color: var(--mall-primary, #409eff);
}

/* 禮物選擇 */
.gift-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.gift-option {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fafafa;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 200px;
  flex: 1;
}

.gift-option:hover {
  background: #f0f2f5;
}

.gift-option.is-selected {
  border-color: var(--mall-primary, #409eff);
  background: var(--mall-primary-light, #ecf5ff);
}

.gift-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  flex-shrink: 0;
}

.gift-info {
  flex: 1;
}

.gift-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 4px;
}

.gift-threshold {
  font-size: 12px;
  color: #909399;
}

.gift-check {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 18px;
  color: var(--mall-primary, #409eff);
}

/* 地址表單樣式 */
.address-inputs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.address-inputs :deep(.el-input) {
  width: auto;
}

.address-inputs :deep(.el-input__wrapper) {
  padding-right: 0;
}

.address-inputs :deep(.el-input-group__append) {
  padding: 0 8px;
  background: #f5f7fa;
}

.postal-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

/* 響應式 */
@media (max-width: 768px) {
  .product-item {
    flex-wrap: wrap;
  }

  .product-price,
  .product-quantity,
  .product-subtotal {
    width: auto;
  }

  .checkout-footer {
    flex-direction: column;
    gap: 16px;
  }

  .shipping-group :deep(.el-radio),
  .payment-group :deep(.el-radio) {
    max-width: 100%;
    min-width: 100%;
  }
}
</style>
