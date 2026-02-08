<template>
  <div class="checkout-page">
    <div class="checkout-container" v-loading="loading">
      <h1 class="page-title">確認訂單</h1>

      <!-- 收貨地址 -->
      <div class="checkout-section">
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

      <!-- 付款方式 -->
      <div class="checkout-section">
        <h2>付款方式</h2>
        <el-radio-group v-model="paymentMethod" class="payment-group">
          <el-radio value="COD" size="large">
            <div class="payment-option">
              <span class="payment-name">貨到付款</span>
              <span class="payment-desc">收到商品後現金支付</span>
            </div>
          </el-radio>
          <el-radio value="ECPAY" size="large">
            <div class="payment-option">
              <span class="payment-name">綠界金流</span>
              <span class="payment-desc">支援信用卡、ATM、超商付款</span>
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
          <span v-if="checkoutData.shippingFee > 0">${{ formatPrice(checkoutData.shippingFee) }}</span>
          <span v-else class="free-shipping">免運費</span>
        </div>
        <div v-if="checkoutData.discountAmount > 0" class="amount-row discount">
          <span>折扣</span>
          <span>-${{ formatPrice(checkoutData.discountAmount) }}</span>
        </div>
        <div class="amount-row total">
          <span>應付金額</span>
          <span class="total-price">${{ formatPrice(checkoutData.payableAmount) }}</span>
        </div>
      </div>

      <!-- 提交按鈕 -->
      <div class="checkout-footer">
        <div class="footer-info">
          共 <span class="highlight">{{ checkoutData.totalQuantity }}</span> 件商品，
          應付：<span class="total-price">${{ formatPrice(checkoutData.payableAmount) }}</span>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!selectedAddress"
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
          <el-input v-model="addressForm.receiverName" placeholder="請輸入收件人姓名" />
        </el-form-item>
        <el-form-item label="手機號碼" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="請輸入手機號碼" />
        </el-form-item>
        <el-form-item label="縣市" prop="province">
          <el-input v-model="addressForm.province" placeholder="請輸入縣市" />
        </el-form-item>
        <el-form-item label="區域" prop="city">
          <el-input v-model="addressForm.city" placeholder="請輸入市/區" />
        </el-form-item>
        <el-form-item label="鄉鎮" prop="district">
          <el-input v-model="addressForm.district" placeholder="請輸入區/鄉鎮" />
        </el-form-item>
        <el-form-item label="詳細地址" prop="detailAddress">
          <el-input
            v-model="addressForm.detailAddress"
            type="textarea"
            :rows="2"
            placeholder="請輸入詳細地址"
          />
        </el-form-item>
        <el-form-item label="郵遞區號" prop="postalCode">
          <el-input v-model="addressForm.postalCode" placeholder="請輸入郵遞區號" style="width: 120px" />
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { getCheckoutPreview, submitOrder } from '@/api/shop/checkout'
import { createEcpayPayment } from '@/api/shop/payment'
import { addAddress } from '@/api/shop/address'
import { getMemberToken } from '@/utils/memberAuth'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const showAddressDialog = ref(false)
const showAddAddressDialog = ref(false)
const addingAddress = ref(false)
const remark = ref('')
const paymentMethod = ref('COD')
const selectedAddress = ref(null)
const selectedGiftId = ref(null)
const addressFormRef = ref(null)

// 新增地址表單
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: true
})

const addressRules = {
  receiverName: [
    { required: true, message: '請輸入收件人姓名', trigger: 'blur' },
    { max: 50, message: '姓名長度不能超過 50 字元', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '請輸入手機號碼', trigger: 'blur' },
    { pattern: /^09\d{8}$/, message: '請輸入正確的手機號碼', trigger: 'blur' }
  ],
  detailAddress: [
    { required: true, message: '請輸入詳細地址', trigger: 'blur' },
    { max: 200, message: '地址長度不能超過 200 字元', trigger: 'blur' }
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

onMounted(() => {
  // 檢查登入狀態，未登入則跳轉到商城登入頁
  if (!getMemberToken()) {
    ElMessage.warning('請先登入後再結帳')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  fetchCheckoutPreview()
})

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
  addressForm.detailAddress = ''
  addressForm.postalCode = ''
  addressForm.isDefault = true

  showAddressDialog.value = false
  showAddAddressDialog.value = true
}

async function handleAddAddress() {
  if (!addressFormRef.value) return

  try {
    await addressFormRef.value.validate()
    addingAddress.value = true

    const response = await addAddress({ ...addressForm })
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
  if (!selectedAddress.value) {
    ElMessage.warning('請選擇收貨地址')
    return
  }

  try {
    await ElMessageBox.confirm(
      `確認提交訂單？應付金額：$${formatPrice(checkoutData.payableAmount)}`,
      '確認訂單',
      {
        confirmButtonText: '確認提交',
        cancelButtonText: '再想想',
        type: 'info'
      }
    )

    submitting.value = true

    const submitData = {
      addressId: selectedAddress.value.addressId,
      remark: remark.value,
      paymentMethod: paymentMethod.value
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

/* 付款方式 */
.payment-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.payment-name {
  font-size: 14px;
  color: #303133;
}

.payment-desc {
  font-size: 12px;
  color: #909399;
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
}
</style>
