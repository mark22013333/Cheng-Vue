<template>
  <div class="mall-auth-page">
    <!-- 背景裝飾 -->
    <div class="auth-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>

    <div class="auth-container">
      <!-- 左側品牌區 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo">
            <img src="@/assets/logo/logo.png" alt="Logo" />
          </div>
          <h1 class="brand-title">CoolApps 商城</h1>
          <p class="brand-slogan">加入會員，享受專屬優惠</p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><Discount /></el-icon>
              <span>新會員優惠</span>
            </div>
            <div class="feature-item">
              <el-icon><Present /></el-icon>
              <span>生日禮物</span>
            </div>
            <div class="feature-item">
              <el-icon><Star /></el-icon>
              <span>累積積分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右側註冊表單 -->
      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h2>會員註冊</h2>
            <p>填寫資料，立即成為會員</p>
          </div>

          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="auth-form">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                size="large"
                placeholder="請輸入帳號"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                size="large"
                placeholder="請輸入 Email"
                :prefix-icon="Message"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                size="large"
                :type="showPassword ? 'text' : 'password'"
                placeholder="請輸入密碼（6-20 字元）"
                :prefix-icon="Lock"
              >
                <template #suffix>
                  <el-icon class="password-toggle" @click="showPassword = !showPassword">
                    <View v-if="showPassword" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                size="large"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="請再次輸入密碼"
                :prefix-icon="Lock"
                @keyup.enter="handleRegister"
              >
                <template #suffix>
                  <el-icon class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
                    <View v-if="showConfirmPassword" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="code" v-if="captchaEnabled">
              <div class="captcha-row">
                <el-input
                  v-model="registerForm.code"
                  size="large"
                  placeholder="驗證碼"
                  :prefix-icon="Key"
                  @keyup.enter="handleRegister"
                />
                <div class="captcha-img" @click="getCode">
                  <img :src="codeUrl" alt="驗證碼" />
                  <div class="captcha-refresh">
                    <el-icon><Refresh /></el-icon>
                  </div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="agreement">
              <el-checkbox v-model="registerForm.agreement">
                我已閱讀並同意
                <a href="javascript:;" class="link">服務條款</a>
                和
                <a href="javascript:;" class="link">隱私政策</a>
              </el-checkbox>
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
            >
              {{ loading ? '註冊中...' : '立即註冊' }}
            </el-button>

            <div class="form-footer">
              <span>已經有帳號？</span>
              <router-link :to="{ path: '/mall/login', query: { redirect: redirect } }">
                立即登入
              </router-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  View,
  Hide,
  Key,
  Refresh,
  Message,
  Discount,
  Present,
  Star
} from '@element-plus/icons-vue'
import { getCodeImg, register } from '@/api/login'

const router = useRouter()
const route = useRoute()

const registerFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const captchaEnabled = ref(true)
const codeUrl = ref('')

const redirect = computed(() => route.query.redirect || '/mall')

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  code: '',
  uuid: '',
  agreement: false
})

// 自訂驗證規則
const validatePassword = (rule, value, callback) => {
  if (value.length < 6 || value.length > 20) {
    callback(new Error('密碼長度需在 6-20 字元之間'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('請同意服務條款和隱私政策'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: 'blur', message: '請輸入帳號' },
    { min: 4, max: 20, trigger: 'blur', message: '帳號長度需在 4-20 字元之間' }
  ],
  email: [
    { required: true, trigger: 'blur', message: '請輸入 Email' },
    { type: 'email', trigger: 'blur', message: '請輸入正確的 Email 格式' }
  ],
  password: [
    { required: true, trigger: 'blur', message: '請輸入密碼' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, trigger: 'blur', message: '請再次輸入密碼' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  code: [
    { required: true, trigger: 'change', message: '請輸入驗證碼' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

async function getCode() {
  try {
    const res = await getCodeImg()
    captchaEnabled.value = res.captchaEnabled !== false
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      registerForm.value.uuid = res.uuid
    }
  } catch (error) {
    console.error('獲取驗證碼失敗', error)
  }
}

async function handleRegister() {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    await register(registerForm.value)

    ElMessage.success('註冊成功，請登入')
    router.push({
      path: '/mall/login',
      query: { redirect: redirect.value }
    })
  } catch (error) {
    loading.value = false
    if (captchaEnabled.value) {
      getCode()
    }
  }
}

onMounted(() => {
  getCode()
})
</script>

<style scoped>
.mall-auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 背景裝飾 */
.auth-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -100px;
  animation: float 20s infinite ease-in-out;
}

.shape-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
  animation: float 15s infinite ease-in-out reverse;
}

.shape-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  left: 30%;
  animation: float 18s infinite ease-in-out;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(30px, -30px) scale(1.05);
  }
}

/* 主容器 */
.auth-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

/* 左側品牌區 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M30 30L0 0h60L30 30zM30 30L60 60H0l30-30z' fill='rgba(255,255,255,0.03)' fill-rule='evenodd'/%3E%3C/svg%3E");
  opacity: 0.5;
}

.brand-content {
  position: relative;
  text-align: center;
  color: white;
}

.brand-logo {
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  background: white;
  border-radius: 24px;
  padding: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.brand-logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.brand-slogan {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 40px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.9;
}

.feature-item .el-icon {
  font-size: 24px;
  background: rgba(255, 255, 255, 0.2);
  padding: 10px;
  border-radius: 12px;
}

/* 右側表單區 */
.form-section {
  flex: 1;
  padding: 50px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-card {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 28px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--el-color-primary) inset;
}

.password-toggle {
  cursor: pointer;
  color: #909399;
  transition: color 0.3s;
}

.password-toggle:hover {
  color: #606266;
}

/* 驗證碼 */
.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 1px solid #dcdfe6;
}

.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-refresh {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.captcha-img:hover .captcha-refresh {
  opacity: 1;
}

/* 連結 */
.link {
  color: var(--el-color-primary);
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

/* 提交按鈕 */
.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 表單底部 */
.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #909399;
}

.form-footer a {
  color: var(--el-color-primary);
  font-weight: 500;
  text-decoration: none;
  margin-left: 4px;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* 響應式 */
@media (max-width: 768px) {
  .auth-container {
    flex-direction: column;
  }

  .brand-section {
    padding: 40px 24px;
  }

  .brand-title {
    font-size: 24px;
  }

  .brand-features {
    display: none;
  }

  .form-section {
    padding: 40px 24px;
  }
}
</style>
