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
          <el-input v-model="form.receiverName" placeholder="請輸入收件人姓名" />
        </el-form-item>
        <el-form-item label="手機號碼" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="請輸入手機號碼" />
        </el-form-item>
        <el-form-item label="縣市" prop="province">
          <el-input v-model="form.province" placeholder="請輸入縣市" />
        </el-form-item>
        <el-form-item label="區域" prop="city">
          <el-input v-model="form.city" placeholder="請輸入市/區" />
        </el-form-item>
        <el-form-item label="鄉鎮" prop="district">
          <el-input v-model="form.district" placeholder="請輸入區/鄉鎮" />
        </el-form-item>
        <el-form-item label="詳細地址" prop="detailAddress">
          <el-input
            v-model="form.detailAddress"
            type="textarea"
            :rows="2"
            placeholder="請輸入詳細地址"
          />
        </el-form-item>
        <el-form-item label="郵遞區號" prop="postalCode">
          <el-input v-model="form.postalCode" placeholder="請輸入郵遞區號" style="width: 120px" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api/shop/address'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const addressList = ref([])
const formRef = ref(null)

const form = reactive({
  addressId: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: false
})

const rules = {
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
  form.detailAddress = ''
  form.postalCode = ''
  form.isDefault = false
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(address) {
  form.addressId = address.addressId
  form.receiverName = address.receiverName
  form.receiverPhone = address.receiverPhone
  form.province = address.province || ''
  form.city = address.city || ''
  form.district = address.district || ''
  form.detailAddress = address.detailAddress
  form.postalCode = address.postalCode || ''
  form.isDefault = address.isDefault || false
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const data = { ...form }

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
