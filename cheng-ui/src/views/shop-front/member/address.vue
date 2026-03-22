<template>
  <div class="address-page">
    <!-- 頁首 -->
    <div class="page-header">
      <div>
        <h2>收貨地址</h2>
        <p class="page-subtitle">管理您的寄送地址，讓每次購物更順暢</p>
      </div>
      <button
        v-if="!formVisible"
        class="add-btn"
        @click="handleAdd"
      >
        <span class="add-btn-icon">+</span>
        新增地址
      </button>
    </div>

    <!-- 內嵌表單面板 -->
    <Transition name="form-slide">
      <div v-if="formVisible" class="form-panel">
        <div class="form-panel-header">
          <div class="form-panel-title">
            <div class="form-panel-icon">
              <el-icon :size="16"><EditPen /></el-icon>
            </div>
            <span>{{ isEdit ? '編輯地址' : '新增寄送地址' }}</span>
          </div>
          <button class="form-close-btn" @click="handleFormClose">
            <el-icon :size="16"><Close /></el-icon>
          </button>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="address-form"
          @submit.prevent
        >
          <!-- 收件人資訊 -->
          <div class="form-section-label">
            <span class="section-dot" />
            收件人資訊
          </div>
          <div class="form-row-2">
            <el-form-item label="收件人姓名" prop="receiverName">
              <el-input
                v-model="form.receiverName"
                placeholder="中文 2~5 字 / 英文 4~10 字"
                maxlength="10"
                size="large"
              />
            </el-form-item>
            <el-form-item label="手機號碼" prop="receiverPhone">
              <el-input
                v-model="form.receiverPhone"
                placeholder="09xx-xxx-xxx"
                maxlength="10"
                size="large"
              />
            </el-form-item>
          </div>

          <!-- 地區 -->
          <div class="form-section-label">
            <span class="section-dot" />
            寄送地區
          </div>
          <div class="form-row-2">
            <el-form-item label="縣市" prop="province">
              <el-select
                v-model="selectedCityCode"
                placeholder="選擇縣市"
                filterable
                clearable
                size="large"
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
            <el-form-item label="鄉鎮市區" prop="city">
              <el-select
                v-model="form.city"
                placeholder="選擇區域"
                filterable
                clearable
                :disabled="!selectedCityCode"
                size="large"
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
          </div>

          <!-- 詳細地址 -->
          <div class="form-section-label">
            <span class="section-dot" />
            詳細地址
          </div>
          <el-form-item label="路 / 街" class="street-field">
            <el-input
              v-model="form.street"
              placeholder="例：中山路、忠孝東路四段"
              maxlength="30"
              size="large"
            />
          </el-form-item>

          <div class="lane-grid">
            <div class="lane-item">
              <label class="lane-label">巷</label>
              <el-input v-model="form.lane" placeholder="—" size="large" />
            </div>
            <div class="lane-item">
              <label class="lane-label">弄</label>
              <el-input v-model="form.alley" placeholder="—" size="large" />
            </div>
            <div class="lane-item">
              <label class="lane-label">號</label>
              <el-input v-model="form.number" placeholder="—" size="large" />
            </div>
            <div class="lane-item">
              <label class="lane-label">樓</label>
              <el-input v-model="form.floor" placeholder="—" size="large" />
            </div>
          </div>

          <!-- 郵遞區號 + 預設 -->
          <div class="form-row-footer">
            <div class="postal-display">
              <span class="postal-label">郵遞區號</span>
              <span class="postal-value" :class="{ 'has-value': form.postalCode }">
                {{ form.postalCode || '選擇地區後自動帶入' }}
              </span>
            </div>
            <label class="default-toggle" @click.prevent="form.isDefault = !form.isDefault">
              <span
                class="toggle-track"
                :class="{ active: form.isDefault }"
              >
                <span class="toggle-thumb" />
              </span>
              <span class="toggle-text">設為預設地址</span>
            </label>
          </div>

          <!-- 操作按鈕 -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="handleFormClose">取消</button>
            <button
              type="button"
              class="btn-submit"
              :class="{ loading: submitting }"
              :disabled="submitting"
              @click="handleSubmit"
            >
              <span v-if="submitting" class="btn-spinner" />
              {{ submitting ? '儲存中...' : isEdit ? '更新地址' : '儲存地址' }}
            </button>
          </div>
        </el-form>
      </div>
    </Transition>

    <!-- 地址列表 -->
    <div class="address-list" v-loading="loading">
      <!-- 空狀態 -->
      <div v-if="!loading && addressList.length === 0 && !formVisible" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <path d="M24 4C16.268 4 10 10.268 10 18c0 11 14 26 14 26s14-15 14-26c0-7.732-6.268-14-14-14z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="18" r="5" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <p class="empty-title">還沒有收貨地址</p>
        <p class="empty-desc">新增一個常用地址，讓結帳更快速</p>
        <button class="add-btn" @click="handleAdd">
          <span class="add-btn-icon">+</span>
          新增第一個地址
        </button>
      </div>

      <!-- 地址卡片 -->
      <TransitionGroup name="card-list" tag="div" class="card-container">
        <div
          v-for="address in addressList"
          :key="address.addressId"
          class="address-card"
          :class="{ 'is-default': address.isDefault }"
        >
          <!-- 預設標記 -->
          <div v-if="address.isDefault" class="default-ribbon">
            <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
              <path d="M1 5.5L3.5 8L9 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            預設
          </div>

          <div class="card-body">
            <div class="card-info">
              <div class="card-person">
                <span class="person-name">{{ address.receiverName }}</span>
                <span class="person-phone">{{ address.receiverPhone }}</span>
              </div>
              <div class="card-address">
                {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
              </div>
              <div class="card-postal" v-if="address.postalCode">
                {{ address.postalCode }}
              </div>
            </div>
          </div>

          <div class="card-actions">
            <button
              v-if="!address.isDefault"
              class="action-btn action-default"
              @click="handleSetDefault(address)"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M7 1L8.854 4.854L13 5.5L10 8.4L10.708 12.5L7 10.6L3.292 12.5L4 8.4L1 5.5L5.146 4.854L7 1Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
              </svg>
              設為預設
            </button>
            <button class="action-btn action-edit" @click="handleEdit(address)">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M10 1.5L12.5 4 4.5 12H2V9.5L10 1.5Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
              </svg>
              編輯
            </button>
            <button class="action-btn action-delete" @click="handleDelete(address)">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M2 4h10M5 4V2.5a1 1 0 011-1h2a1 1 0 011 1V4M11 4v7.5a1 1 0 01-1 1H4a1 1 0 01-1-1V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              刪除
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Close } from '@element-plus/icons-vue'
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api/shop/address'
import { getCityList, getDistrictList, getCityCodeByName, getPostalCode } from '@/utils/taiwan-address'

const loading = ref(false)
const submitting = ref(false)
const formVisible = ref(false)
const isEdit = ref(false)
const addressList = ref([])
const formRef = ref(null)

// 縣市區資料
const cityList = getCityList()
const selectedCityCode = ref('')

// 根據選擇的縣市取得區域列表
const districtList = computed(() => {
  if (!selectedCityCode.value) return []
  return getDistrictList(selectedCityCode.value)
})

const form = reactive({
  addressId: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  street: '',
  lane: '',
  alley: '',
  number: '',
  floor: '',
  detailAddress: '',
  postalCode: '',
  isDefault: false
})

// 當縣市變更時，清空區域和郵遞區號
watch(selectedCityCode, (newVal, oldVal) => {
  if (oldVal && newVal !== oldVal) {
    form.city = ''
    form.postalCode = ''
  }
  const city = cityList.find(c => c.code === newVal)
  if (city) {
    form.province = city.name
  }
})

// 當區域變更時，自動填入郵遞區號
watch(() => form.city, (newVal) => {
  if (newVal && selectedCityCode.value) {
    const zip = getPostalCode(selectedCityCode.value, newVal)
    if (zip) {
      form.postalCode = zip
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

const rules = {
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
  ]
}

onMounted(() => {
  fetchAddressList()
})

async function fetchAddressList() {
  loading.value = true
  try {
    const response = await getAddressList()
    if (response.code === 200) {
      addressList.value = response.data || []
    }
  } catch (error) {
    console.error('載入地址失敗', error)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.addressId = null
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.street = ''
  form.lane = ''
  form.alley = ''
  form.number = ''
  form.floor = ''
  form.detailAddress = ''
  form.postalCode = ''
  form.isDefault = false
  selectedCityCode.value = ''
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  formVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function handleFormClose() {
  formVisible.value = false
}

// 解析詳細地址到各欄位
function parseDetailAddress(detailAddress) {
  if (!detailAddress) return { street: '', lane: '', alley: '', number: '', floor: '' }

  const result = { street: '', lane: '', alley: '', number: '', floor: '' }

  const floorMatch = detailAddress.match(/(\d+)\s*樓/)
  if (floorMatch) result.floor = floorMatch[1]

  const numberMatch = detailAddress.match(/(\d+)\s*號/)
  if (numberMatch) result.number = numberMatch[1]

  const alleyMatch = detailAddress.match(/(\d+)\s*弄/)
  if (alleyMatch) result.alley = alleyMatch[1]

  const laneMatch = detailAddress.match(/(\d+)\s*巷/)
  if (laneMatch) result.lane = laneMatch[1]

  const streetMatch = detailAddress.match(/^([^\d]+?)(?:\d|$)/)
  if (streetMatch) result.street = streetMatch[1].trim()

  return result
}

function handleEdit(address) {
  form.addressId = address.addressId
  form.receiverName = address.receiverName
  form.receiverPhone = address.receiverPhone
  form.province = address.province || ''
  form.city = address.city || ''
  form.district = address.district || ''
  form.postalCode = address.postalCode || ''
  form.isDefault = address.isDefault || false

  const parsed = parseDetailAddress(address.detailAddress)
  form.street = parsed.street
  form.lane = parsed.lane
  form.alley = parsed.alley
  form.number = parsed.number
  form.floor = parsed.floor
  form.detailAddress = address.detailAddress || ''

  selectedCityCode.value = getCityCodeByName(address.province) || ''
  isEdit.value = true
  formVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 組合詳細地址
function composeDetailAddress() {
  let address = form.street || ''
  if (form.lane) address += form.lane + '巷'
  if (form.alley) address += form.alley + '弄'
  if (form.number) address += form.number + '號'
  if (form.floor) address += form.floor + '樓'
  return address
}

async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const detailAddress = composeDetailAddress()

    const data = {
      addressId: form.addressId,
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: detailAddress,
      postalCode: form.postalCode,
      isDefault: form.isDefault
    }

    if (isEdit.value) {
      await updateAddress(data)
      ElMessage.success('地址已更新')
    } else {
      await addAddress(data)
      ElMessage.success('地址已新增')
    }

    formVisible.value = false
    await fetchAddressList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失敗')
    }
  } finally {
    submitting.value = false
  }
}

async function handleDelete(address) {
  try {
    await ElMessageBox.confirm(
      `確定要刪除「${address.receiverName}」的地址嗎？`,
      '刪除地址',
      {
        confirmButtonText: '確定刪除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteAddress(address.addressId)
    ElMessage.success('地址已刪除')
    await fetchAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('刪除失敗')
    }
  }
}

async function handleSetDefault(address) {
  try {
    await setDefaultAddress(address.addressId)
    ElMessage.success('已設為預設地址')
    await fetchAddressList()
  } catch (error) {
    ElMessage.error('設定失敗')
  }
}
</script>

<style scoped>
.address-page {
  padding: 0;
  font-family: 'Noto Sans TC', system-ui, sans-serif;
}

/* ====== 頁首 ====== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-family: 'Noto Serif TC', system-ui, serif;
  font-size: 20px;
  font-weight: 600;
  color: #3D2B1F;
  line-height: 1.3;
}

.page-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #A09585;
}

/* ====== 新增按鈕 ====== */
.add-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border: 1.5px solid #4A6B7C;
  border-radius: 12px;
  background: transparent;
  color: #4A6B7C;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.add-btn:hover {
  background: #4A6B7C;
  color: #fff;
  box-shadow: 0 4px 16px rgba(74, 107, 124, 0.2);
  transform: translateY(-1px);
}

.add-btn:active {
  transform: translateY(0);
}

.add-btn-icon {
  font-size: 18px;
  font-weight: 300;
  line-height: 1;
}

/* ====== 內嵌表單面板 ====== */
.form-panel {
  background: #FDFCFA;
  border: 1px solid #EDE8E2;
  border-radius: 18px;
  margin-bottom: 24px;
  overflow: hidden;
}

.form-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #F0EBE5;
}

.form-panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: #3D2B1F;
}

.form-panel-icon {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  background: linear-gradient(135deg, #4A6B7C 0%, #5A8A9A 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(74, 107, 124, 0.2);
}

.form-close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #A09585;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.form-close-btn:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #3D2B1F;
}

/* ====== 表單內容 ====== */
.address-form {
  padding: 20px 24px 24px;
}

.form-section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  font-weight: 600;
  color: #9A8B7D;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 14px;
  margin-top: 4px;
}

.form-section-label:not(:first-child) {
  margin-top: 22px;
}

.section-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #C4A882;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 20px;
}

/* 表單控件暖色調覆寫 */
.address-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.address-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #5A4A3E;
  padding-bottom: 6px !important;
  line-height: 1.4;
}

.address-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
  background: #fff;
  transition: all 0.25s ease;
}

.address-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A882 inset;
}

.address-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 107, 124, 0.35) inset;
  background: #FEFEFE;
}

.address-form :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
}

/* ====== 路街欄位 ====== */
.street-field {
  margin-bottom: 0;
}

/* ====== 巷弄號樓 2x2 網格 ====== */
.lane-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 12px;
  margin-top: 14px;
}

.lane-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.lane-label {
  font-size: 12px;
  font-weight: 600;
  color: #9A8B7D;
  padding-left: 2px;
}

.lane-item :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
  background: #fff;
  transition: all 0.25s ease;
}

.lane-item :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A882 inset;
}

.lane-item :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 107, 124, 0.35) inset;
}

.lane-item :deep(.el-input__inner) {
  text-align: center;
}

/* ====== 底部：郵遞區號 + 預設開關 ====== */
.form-row-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid #F0EBE5;
}

.postal-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.postal-label {
  font-size: 12.5px;
  font-weight: 500;
  color: #9A8B7D;
}

.postal-value {
  font-size: 13px;
  color: #C4B5A6;
  font-style: italic;
}

.postal-value.has-value {
  color: #4A6B7C;
  font-weight: 600;
  font-style: normal;
  background: rgba(74, 107, 124, 0.06);
  padding: 2px 10px;
  border-radius: 6px;
}

/* 自訂 toggle */
.default-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.toggle-track {
  position: relative;
  width: 40px;
  height: 22px;
  border-radius: 11px;
  background: #DDD6CE;
  transition: background 0.25s ease;
}

.toggle-track.active {
  background: #4A6B7C;
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.toggle-track.active .toggle-thumb {
  transform: translateX(18px);
}

.toggle-text {
  font-size: 13px;
  font-weight: 500;
  color: #5A4A3E;
}

/* ====== 表單按鈕 ====== */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-cancel,
.btn-submit {
  height: 44px;
  padding: 0 28px;
  border-radius: 12px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-cancel {
  background: #fff;
  border: 1.5px solid #E0D5C8;
  color: #7A6B5D;
}

.btn-cancel:hover {
  border-color: #C4A882;
  color: #5A4A3E;
}

.btn-submit {
  background: linear-gradient(135deg, #4A6B7C 0%, #5A8A9A 100%);
  border: none;
  color: #fff;
  min-width: 140px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  box-shadow: 0 4px 16px rgba(74, 107, 124, 0.25);
}

.btn-submit:hover:not(:disabled) {
  box-shadow: 0 6px 24px rgba(74, 107, 124, 0.35);
  transform: translateY(-1px);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ====== 表單滑入動畫 ====== */
.form-slide-enter-active {
  animation: slideDown 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.form-slide-leave-active {
  animation: slideDown 0.2s ease reverse;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-12px);
    max-height: 0;
  }
  to {
    opacity: 1;
    transform: translateY(0);
    max-height: 800px;
  }
}

/* ====== 空狀態 ====== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 20px;
}

.empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(74, 107, 124, 0.08), rgba(196, 168, 130, 0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
}

.empty-icon svg {
  width: 32px;
  height: 32px;
  color: #9A8B7D;
}

.empty-title {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: #5A4A3E;
}

.empty-desc {
  margin: 0 0 20px;
  font-size: 13px;
  color: #A09585;
}

/* ====== 地址卡片 ====== */
.card-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  position: relative;
  background: #FDFCFA;
  border: 1px solid #EDE8E2;
  border-radius: 16px;
  padding: 20px 22px;
  transition: all 0.3s ease;
}

.address-card:hover {
  border-color: #D8CFC5;
  box-shadow: 0 4px 16px rgba(74, 107, 124, 0.06);
}

.address-card.is-default {
  border-color: rgba(74, 107, 124, 0.3);
  background: linear-gradient(135deg, rgba(74, 107, 124, 0.03), rgba(90, 138, 154, 0.02));
}

/* 預設標記 */
.default-ribbon {
  position: absolute;
  top: 12px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 8px;
  background: rgba(74, 107, 124, 0.08);
  font-size: 11px;
  font-weight: 700;
  color: #4A6B7C;
  letter-spacing: 0.3px;
}

/* 卡片內容 */
.card-body {
  display: flex;
  align-items: flex-start;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-person {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.person-name {
  font-size: 15px;
  font-weight: 700;
  color: #3D2B1F;
}

.person-phone {
  font-size: 13px;
  color: #9A8B7D;
  letter-spacing: 0.3px;
}

.card-address {
  font-size: 14px;
  color: #5A4A3E;
  line-height: 1.6;
}

.card-postal {
  display: inline-block;
  margin-top: 6px;
  font-size: 11.5px;
  color: #B0A090;
  background: rgba(196, 168, 130, 0.08);
  padding: 1px 8px;
  border-radius: 4px;
}

/* 卡片操作 */
.card-actions {
  display: flex;
  gap: 4px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #F0EBE5;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn svg {
  flex-shrink: 0;
}

.action-default {
  color: #C4A882;
}

.action-default:hover {
  background: rgba(196, 168, 130, 0.1);
  color: #A5885E;
}

.action-edit {
  color: #4A6B7C;
}

.action-edit:hover {
  background: rgba(74, 107, 124, 0.08);
  color: #3A5565;
}

.action-delete {
  color: #C4A882;
}

.action-delete:hover {
  background: rgba(165, 99, 92, 0.08);
  color: #A5635C;
}

/* 卡片列表動畫 */
.card-list-enter-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.card-list-leave-active {
  transition: all 0.2s ease;
}

.card-list-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.card-list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* ====== RWD ====== */
@media (max-width: 600px) {
  .page-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 14px;
  }

  .page-header > div {
    width: 100%;
  }

  .add-btn {
    width: 100%;
    justify-content: center;
  }

  .empty-state {
    padding: 36px 16px;
  }

  .form-panel {
    border-radius: 14px;
  }

  .form-panel-header {
    padding: 14px 16px;
  }

  .address-form {
    padding: 16px;
  }

  .form-row-2 {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .lane-grid {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .form-row-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }

  .address-card {
    padding: 16px;
    border-radius: 14px;
  }

  .card-actions {
    flex-wrap: wrap;
  }

  .default-ribbon {
    top: 10px;
    right: 12px;
  }
}
</style>
