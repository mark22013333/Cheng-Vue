<template>
  <div class="address-page">
    <div class="page-header">
      <h2>收貨地址</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增地址
      </el-button>
    </div>

    <!-- 地址列表 -->
    <div class="address-list" v-loading="loading">
      <el-empty v-if="addressList.length === 0" description="暫無收貨地址" />

      <div
        v-for="address in addressList"
        :key="address.addressId"
        class="address-card"
        :class="{ 'is-default': address.isDefault }"
      >
        <div class="address-content">
          <div class="address-header">
            <span class="receiver-name">{{ address.receiverName }}</span>
            <span class="receiver-phone">{{ address.receiverPhone }}</span>
            <el-tag v-if="address.isDefault" type="primary" size="small">預設</el-tag>
          </div>
          <div class="address-detail">
            {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
          </div>
          <div class="postal-code" v-if="address.postalCode">
            郵遞區號：{{ address.postalCode }}
          </div>
        </div>
        <div class="address-actions">
          <el-button
            v-if="!address.isDefault"
            text
            type="primary"
            @click="handleSetDefault(address)"
          >
            設為預設
          </el-button>
          <el-button text type="primary" @click="handleEdit(address)">
            編輯
          </el-button>
          <el-button text type="danger" @click="handleDelete(address)">
            刪除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增/編輯對話框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '編輯地址' : '新增地址'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.prevent
      >
        <el-form-item label="收件人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="中文 2~5 字 / 英文 4~10 字" maxlength="10" show-word-limit />
        </el-form-item>
        <el-form-item label="手機號碼" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="09 開頭，共 10 碼" maxlength="10" />
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
            v-model="form.city"
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
          <el-input v-model="form.street" placeholder="例：中山路、忠孝東路四段" maxlength="30" />
        </el-form-item>
        <el-form-item label="巷弄號樓" class="address-detail-row">
          <div class="address-inputs">
            <el-input v-model="form.lane" placeholder="巷" style="width: 70px">
              <template #append>巷</template>
            </el-input>
            <el-input v-model="form.alley" placeholder="弄" style="width: 70px">
              <template #append>弄</template>
            </el-input>
            <el-input v-model="form.number" placeholder="號" style="width: 70px">
              <template #append>號</template>
            </el-input>
            <el-input v-model="form.floor" placeholder="樓" style="width: 70px">
              <template #append>樓</template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item label="郵遞區號">
          <el-input v-model="form.postalCode" disabled style="width: 100px" />
          <span class="postal-hint">（自動帶入）</span>
        </el-form-item>
        <el-form-item label="設為預設">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          確定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
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
const dialogVisible = ref(false)
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
  street: '',      // 路/街
  lane: '',        // 巷
  alley: '',       // 弄
  number: '',      // 號
  floor: '',       // 樓
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
  // 更新 province 欄位
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
    // 中文字元（基本漢字區塊）
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
  dialogVisible.value = true
}

// 解析詳細地址到各欄位
function parseDetailAddress(detailAddress) {
  if (!detailAddress) return { street: '', lane: '', alley: '', number: '', floor: '' }

  const result = { street: '', lane: '', alley: '', number: '', floor: '' }

  // 嘗試解析格式：XX路XX巷XX弄XX號XX樓
  const floorMatch = detailAddress.match(/(\d+)\s*樓/)
  if (floorMatch) result.floor = floorMatch[1]

  const numberMatch = detailAddress.match(/(\d+)\s*號/)
  if (numberMatch) result.number = numberMatch[1]

  const alleyMatch = detailAddress.match(/(\d+)\s*弄/)
  if (alleyMatch) result.alley = alleyMatch[1]

  const laneMatch = detailAddress.match(/(\d+)\s*巷/)
  if (laneMatch) result.lane = laneMatch[1]

  // 路/街：取到第一個數字前的部分
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

  // 解析詳細地址
  const parsed = parseDetailAddress(address.detailAddress)
  form.street = parsed.street
  form.lane = parsed.lane
  form.alley = parsed.alley
  form.number = parsed.number
  form.floor = parsed.floor
  form.detailAddress = address.detailAddress || ''

  // 根據縣市名稱找到對應的代碼
  selectedCityCode.value = getCityCodeByName(address.province) || ''
  isEdit.value = true
  dialogVisible.value = true
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

    // 組合詳細地址
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
      ElMessage.success('更新成功')
    } else {
      await addAddress(data)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
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
    await ElMessageBox.confirm('確定要刪除此地址嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteAddress(address.addressId)
    ElMessage.success('刪除成功')
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
    ElMessage.success('設定成功')
    await fetchAddressList()
  } catch (error) {
    ElMessage.error('設定失敗')
  }
}
</script>

<style scoped>
.address-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.address-card:hover {
  background: #f5f7fa;
}

.address-card.is-default {
  border-color: var(--mall-primary, #409eff);
  background: var(--mall-primary-light, #ecf5ff);
}

.address-content {
  flex: 1;
}

.address-header {
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

.postal-code {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.postal-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

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

.address-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 響應式 */
@media (max-width: 768px) {
  .address-card {
    flex-direction: column;
    gap: 16px;
  }

  .address-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
