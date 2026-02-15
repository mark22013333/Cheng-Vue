<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>個人資料</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      style="max-width: 500px"
    >
      <el-form-item label="頭像">
        <div class="avatar-uploader" @click="triggerFileInput">
          <el-avatar :size="80" :src="displayAvatar" />
          <div class="avatar-overlay">
            <el-icon><Camera /></el-icon>
            <span>更換頭像</span>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/png, image/jpeg, image/gif"
            style="display: none"
            @change="handleFileChange"
          />
        </div>
        <div class="avatar-tip">點擊頭像更換，支援 JPG、PNG、GIF 格式</div>
      </el-form-item>
      <el-form-item label="帳號">
        <el-input v-model="form.userName" disabled />
      </el-form-item>
      <el-form-item label="暱稱" prop="nickName">
        <el-input v-model="form.nickName" placeholder="請輸入暱稱" />
      </el-form-item>
      <el-form-item label="手機號碼" prop="phonenumber">
        <el-input v-model="form.phonenumber" placeholder="請輸入手機號碼" />
      </el-form-item>
      <el-form-item label="電子郵件" prop="email">
        <el-input v-model="form.email" placeholder="請輸入電子郵件" />
      </el-form-item>
      <el-form-item label="性別">
        <el-radio-group v-model="form.sex">
          <el-radio value="0">男</el-radio>
          <el-radio value="1">女</el-radio>
          <el-radio value="2">未知</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">儲存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import useMemberStore from '@/store/modules/member'
import { updateMemberProfile, updateMemberAvatar } from '@/api/shop/auth'
import requestShop from '@/utils/requestShop'

const memberStore = useMemberStore()

const formRef = ref(null)
const fileInputRef = ref(null)
const saving = ref(false)
const tempAvatar = ref('')

const userAvatar = computed(() => memberStore.avatar || '')
const displayAvatar = computed(() => tempAvatar.value || userAvatar.value)

const form = reactive({
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '2'
})

const rules = {
  nickName: [
    { required: true, message: '請輸入暱稱', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '請輸入正確的電子郵件地址', trigger: 'blur' }
  ]
}

onMounted(() => {
  // 載入用戶資料
  form.userName = memberStore.mobile || memberStore.email || ''
  form.nickName = memberStore.nickname || ''
  form.phonenumber = memberStore.mobile || ''
  form.email = memberStore.email || ''
})

function triggerFileInput() {
  fileInputRef.value?.click()
}

async function handleFileChange(event) {
  const file = event.target.files[0]
  if (!file) return

  // 驗證檔案類型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支援 JPG、PNG、GIF 格式的圖片')
    return
  }

  // 驗證檔案大小（最大 2MB）
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('圖片大小不能超過 2MB')
    return
  }

  const loading = ElLoading.service({
    text: '上傳中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 上傳圖片
    const formData = new FormData()
    formData.append('file', file)
    formData.append('category', 'member/avatar')

    const uploadRes = await requestShop({
      url: '/shop/member/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (uploadRes.code === 200) {
      const avatarPath = uploadRes.fileName
      tempAvatar.value = uploadRes.url

      // 更新會員頭像
      const updateRes = await updateMemberAvatar(avatarPath)
      if (updateRes.code === 200) {
        memberStore.setMember(updateRes.data)
        tempAvatar.value = ''
        ElMessage.success('頭像更新成功')
      } else {
        tempAvatar.value = ''
        ElMessage.error(updateRes.msg || '頭像更新失敗')
      }
    } else {
      ElMessage.error(uploadRes.msg || '圖片上傳失敗')
    }
  } catch (error) {
    tempAvatar.value = ''
    ElMessage.error('上傳失敗，請稍後再試')
  } finally {
    loading.close()
    // 清空 input 以便再次選擇同一檔案
    event.target.value = ''
  }
}

async function handleSave() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    // 呼叫 API 儲存用戶資料
    const response = await updateMemberProfile({
      nickname: form.nickName,
      mobile: form.phonenumber,
      email: form.email,
      gender: form.sex
    })

    if (response.code === 200) {
      // 更新 store 中的會員資訊
      memberStore.setMember(response.data)
      ElMessage.success('儲存成功')
    } else {
      ElMessage.error(response.msg || '儲存失敗')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '儲存失敗')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
  display: inline-block;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-overlay .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
