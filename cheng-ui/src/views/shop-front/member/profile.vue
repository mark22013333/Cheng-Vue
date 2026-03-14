<template>
  <div class="profile-page">
    <!-- 頭像區塊 -->
    <div class="avatar-hero">
      <div class="avatar-ring" @click="triggerFileInput">
        <el-avatar :size="108" :src="displayAvatar" class="hero-avatar" />
        <div class="avatar-hover-mask">
          <el-icon :size="24"><Camera /></el-icon>
          <span>更換頭像</span>
        </div>
        <div class="ring-glow" />
        <input
          ref="fileInputRef"
          type="file"
          accept="image/png, image/jpeg, image/gif"
          style="display: none"
          @change="handleFileChange"
        />
      </div>
      <h1 class="profile-name">{{ form.nickName || '會員' }}</h1>
      <p class="profile-account">{{ form.userName }}</p>
      <p class="avatar-hint">點擊頭像更換，支援 JPG、PNG、GIF（最大 2MB）</p>
    </div>

    <!-- 表單區塊 -->
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      class="profile-form"
      label-position="top"
      @submit.prevent
    >
      <!-- 基本資料 -->
      <div class="form-section">
        <div class="section-header">
          <div class="section-icon">
            <el-icon :size="18"><User /></el-icon>
          </div>
          <div>
            <h3>基本資料</h3>
            <p>您的公開顯示名稱與個人資訊</p>
          </div>
        </div>

        <div class="field-grid">
          <el-form-item label="暱稱" prop="nickName" class="field-item">
            <el-input
              v-model="form.nickName"
              placeholder="為自己取個名字吧"
              size="large"
              :prefix-icon="EditPen"
            />
          </el-form-item>

          <div class="field-item gender-field">
            <label class="custom-label">性別</label>
            <div class="gender-pills">
              <button
                v-for="opt in genderOptions"
                :key="opt.value"
                type="button"
                class="gender-pill"
                :class="{ active: form.sex === opt.value }"
                @click="form.sex = opt.value"
              >
                <span class="pill-icon">{{ opt.icon }}</span>
                <span>{{ opt.label }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 聯絡方式 -->
      <div class="form-section">
        <div class="section-header">
          <div class="section-icon">
            <el-icon :size="18"><ChatDotRound /></el-icon>
          </div>
          <div>
            <h3>聯絡方式</h3>
            <p>用於訂單通知與帳戶驗證</p>
          </div>
        </div>

        <div class="field-grid">
          <el-form-item label="手機號碼" prop="phonenumber" class="field-item">
            <el-input
              v-model="form.phonenumber"
              placeholder="09xx-xxx-xxx"
              size="large"
              :prefix-icon="Iphone"
            />
          </el-form-item>

          <el-form-item label="電子郵件" prop="email" class="field-item">
            <el-input
              v-model="form.email"
              placeholder="name@example.com"
              size="large"
              :prefix-icon="Message"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 儲存按鈕 -->
      <div class="form-actions">
        <el-button
          type="primary"
          size="large"
          class="save-btn"
          :loading="saving"
          @click="handleSave"
        >
          <el-icon v-if="!saving"><Check /></el-icon>
          {{ saving ? '儲存中...' : '儲存變更' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import {
  Camera,
  User,
  EditPen,
  Iphone,
  Message,
  ChatDotRound,
  Check
} from '@element-plus/icons-vue'
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

const genderOptions = [
  { value: '0', label: '男', icon: '♂' },
  { value: '1', label: '女', icon: '♀' },
  { value: '2', label: '不透露', icon: '○' }
]

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
  form.userName = memberStore.mobile || memberStore.email || ''
  form.nickName = memberStore.nickname || ''
  form.phonenumber = memberStore.mobile || ''
  form.email = memberStore.email || ''
  form.sex = memberStore.gender || '2'
})

function triggerFileInput() {
  fileInputRef.value?.click()
}

async function handleFileChange(event) {
  const file = event.target.files[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支援 JPG、PNG、GIF 格式的圖片')
    return
  }

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
    event.target.value = ''
  }
}

async function handleSave() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    const response = await updateMemberProfile({
      nickname: form.nickName,
      mobile: form.phonenumber,
      email: form.email,
      gender: form.sex
    })

    if (response.code === 200) {
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
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&family=Noto+Sans+Symbols+2&family=Noto+Color+Emoji&display=swap');

.profile-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 8px 0 32px;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
}

/* ====== 頭像英雄區 ====== */
.avatar-hero {
  text-align: center;
  padding: 12px 0 28px;
  position: relative;
}

.avatar-ring {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  padding: 5px;
  background: conic-gradient(
    from 45deg,
    #4A6B7C,
    #7BA3B5,
    #C4A882,
    #A5635C,
    #4A6B7C
  );
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.avatar-ring:hover {
  transform: scale(1.05);
}

.avatar-ring :deep(.hero-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.avatar-hover-mask {
  position: absolute;
  inset: 5px;
  border-radius: 50%;
  background: rgba(61, 43, 31, 0.55);
  backdrop-filter: blur(2px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.avatar-ring:hover .avatar-hover-mask {
  opacity: 1;
}

.ring-glow {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  background: conic-gradient(
    from 45deg,
    rgba(74, 107, 124, 0.15),
    rgba(196, 168, 130, 0.15),
    rgba(165, 99, 92, 0.1),
    rgba(74, 107, 124, 0.15)
  );
  filter: blur(12px);
  opacity: 0;
  transition: opacity 0.4s ease;
  z-index: -1;
}

.avatar-ring:hover .ring-glow {
  opacity: 1;
}

.profile-name {
  margin: 16px 0 2px;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 22px;
  font-weight: 600;
  color: #3D2B1F;
  line-height: 1.3;
}

.profile-account {
  margin: 0 0 6px;
  font-size: 13px;
  color: #9A8B7D;
  letter-spacing: 0.3px;
}

.avatar-hint {
  margin: 0;
  font-size: 12px;
  color: #C4B5A6;
}

/* ====== 表單 ====== */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* ====== 區塊 ====== */
.form-section {
  background: #FDFCFA;
  border: 1px solid #EDE8E2;
  border-radius: 16px;
  padding: 24px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.form-section:hover {
  border-color: #D8CFC5;
  box-shadow: 0 2px 16px rgba(74, 107, 124, 0.05);
}

.section-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F0EBE5;
}

.section-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4A6B7C 0%, #5A8A9A 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 3px 10px rgba(74, 107, 124, 0.2);
}

.section-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #3D2B1F;
  line-height: 1.4;
}

.section-header p {
  margin: 2px 0 0;
  font-size: 12.5px;
  color: #A09585;
  line-height: 1.4;
}

/* ====== 欄位網格 ====== */
.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px 24px;
}

.field-item {
  min-width: 0;
}

/* ====== 自訂 el-form-item 樣式 ====== */
.profile-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.profile-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #5A4A3E;
  padding-bottom: 6px !important;
  line-height: 1.4;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
  background: #fff;
  transition: all 0.25s ease;
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A882 inset;
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 107, 124, 0.35) inset;
  background: #FEFEFE;
}

.profile-form :deep(.el-input__prefix .el-icon) {
  color: #B0A090;
}

.profile-form :deep(.el-input__wrapper.is-focus .el-input__prefix .el-icon) {
  color: #4A6B7C;
}

/* ====== 性別選擇器 ====== */
.custom-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #5A4A3E;
  margin-bottom: 6px;
  line-height: 1.4;
}

.gender-pills {
  display: flex;
  gap: 8px;
}

.gender-pill {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 42px;
  border: 1.5px solid #E0D5C8;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 13.5px;
  font-weight: 500;
  color: #7A6B5D;
  transition: all 0.25s ease;
}

.gender-pill:hover {
  border-color: #4A6B7C;
  color: #4A6B7C;
  background: rgba(74, 107, 124, 0.03);
}

.gender-pill.active {
  border-color: #4A6B7C;
  background: linear-gradient(135deg, rgba(74, 107, 124, 0.08), rgba(90, 138, 154, 0.06));
  color: #4A6B7C;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(74, 107, 124, 0.12);
}

.pill-icon {
  font-size: 15px;
  line-height: 1;
}

/* ====== 儲存按鈕 ====== */
.form-actions {
  padding-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  min-width: 160px;
  height: 46px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #4A6B7C 0%, #5A8A9A 100%);
  font-size: 15px;
  font-weight: 700;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  letter-spacing: 0.5px;
  box-shadow: 0 6px 20px rgba(74, 107, 124, 0.25);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.save-btn::before {
  content: '';
  position: absolute;
  top: -150%;
  left: -40%;
  width: 25%;
  height: 400%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transform: rotate(20deg);
  transition: left 0.5s ease;
}

.save-btn:hover::before {
  left: 140%;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(74, 107, 124, 0.35);
}

.save-btn:active {
  transform: translateY(0);
}

.save-btn :deep(.el-icon) {
  margin-right: 4px;
}

/* ====== RWD ====== */
@media (max-width: 600px) {
  .profile-page {
    padding: 4px 0 24px;
  }

  .field-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .form-section {
    padding: 18px 16px;
    border-radius: 14px;
  }

  .form-actions {
    justify-content: stretch;
  }

  .save-btn {
    width: 100%;
  }

  .profile-name {
    font-size: 20px;
  }
}
</style>
