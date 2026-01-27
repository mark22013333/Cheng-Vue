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
          <p class="brand-slogan">探索優質商品，享受購物樂趣</p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><ShoppingCart /></el-icon>
              <span>精選好物</span>
            </div>
            <div class="feature-item">
              <el-icon><Van /></el-icon>
              <span>快速配送</span>
            </div>
            <div class="feature-item">
              <el-icon><Service /></el-icon>
              <span>優質服務</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右側登入表單 -->
      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h2>會員登入</h2>
            <p>歡迎回來，請登入您的帳號</p>
          </div>

          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="auth-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                size="large"
                placeholder="請輸入帳號 / Email"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                size="large"
                :type="showPassword ? 'text' : 'password'"
                placeholder="請輸入密碼"
                :prefix-icon="Lock"
                @keyup.enter="handleLogin"
              >
                <template #suffix>
                  <el-icon class="password-toggle" @click="showPassword = !showPassword">
                    <View v-if="showPassword" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="code" v-if="captchaEnabled">
              <div class="captcha-row">
                <el-input
                  v-model="loginForm.code"
                  size="large"
                  placeholder="驗證碼"
                  :prefix-icon="Key"
                  @keyup.enter="handleLogin"
                />
                <div class="captcha-img" @click="getCode">
                  <img :src="codeUrl" alt="驗證碼" />
                  <div class="captcha-refresh">
                    <el-icon><Refresh /></el-icon>
                  </div>
                </div>
              </div>
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe">記住我</el-checkbox>
              <a href="javascript:;" class="forgot-link">忘記密碼？</a>
            </div>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登入中...' : '登入' }}
            </el-button>

            <div class="form-footer">
              <span>還沒有帳號？</span>
              <router-link :to="{ path: '/mall/register', query: { redirect: redirect } }">
                立即註冊
              </router-link>
            </div>

            <div class="divider">
              <span>或使用其他方式登入</span>
            </div>

            <div class="social-login">
              <el-button class="social-btn line-btn" @click="handleLineLogin">
                <svg viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M19.365 9.863c.349 0 .63.285.63.631 0 .345-.281.63-.63.63H17.61v1.125h1.755c.349 0 .63.283.63.63 0 .344-.281.629-.63.629h-2.386c-.345 0-.627-.285-.627-.629V8.108c0-.345.282-.63.63-.63h2.386c.349 0 .63.285.63.63 0 .349-.281.63-.63.63H17.61v1.125h1.755zm-3.855 3.016c0 .27-.174.51-.432.596-.064.021-.133.031-.199.031-.211 0-.391-.09-.51-.25l-2.443-3.317v2.94c0 .344-.279.629-.631.629-.346 0-.626-.285-.626-.629V8.108c0-.27.173-.51.43-.595.06-.023.136-.033.194-.033.195 0 .375.104.495.254l2.462 3.33V8.108c0-.345.282-.63.63-.63.345 0 .63.285.63.63v4.771zm-5.741 0c0 .344-.282.629-.631.629-.345 0-.627-.285-.627-.629V8.108c0-.345.282-.63.63-.63.346 0 .628.285.628.63v4.771zm-2.466.629H4.917c-.345 0-.63-.285-.63-.629V8.108c0-.345.285-.63.63-.63.348 0 .63.285.63.63v4.141h1.756c.348 0 .629.283.629.63 0 .344-.282.629-.629.629M24 10.314C24 4.943 18.615.572 12 .572S0 4.943 0 10.314c0 4.811 4.27 8.842 10.035 9.608.391.082.923.258 1.058.59.12.301.079.766.038 1.08l-.164 1.02c-.045.301-.24 1.186 1.049.645 1.291-.539 6.916-4.078 9.436-6.975C23.176 14.393 24 12.458 24 10.314"/>
                </svg>
                <span>LINE 登入</span>
              </el-button>
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
  ShoppingCart,
  Van,
  Service
} from '@element-plus/icons-vue'
import { getCodeImg } from '@/api/login'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import Cookies from 'js-cookie'
import useUserStore from '@/store/modules/user'
import { useCartStore } from '@/store/modules/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const loginFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const captchaEnabled = ref(true)
const codeUrl = ref('')

const redirect = computed(() => route.query.redirect || '/mall')

const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false,
  code: '',
  uuid: ''
})

const loginRules = {
  username: [
    { required: true, trigger: 'blur', message: '請輸入帳號' }
  ],
  password: [
    { required: true, trigger: 'blur', message: '請輸入密碼' }
  ],
  code: [
    { required: true, trigger: 'change', message: '請輸入驗證碼' }
  ]
}

async function getCode() {
  try {
    const res = await getCodeImg()
    captchaEnabled.value = res.captchaEnabled !== false
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      loginForm.value.uuid = res.uuid
    }
  } catch (error) {
    console.error('獲取驗證碼失敗', error)
  }
}

function getCookie() {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')
  loginForm.value = {
    username: username || '',
    password: password ? decrypt(password) : '',
    rememberMe: rememberMe === 'true',
    code: '',
    uuid: ''
  }
}

async function handleLogin() {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 記住我
    if (loginForm.value.rememberMe) {
      Cookies.set('username', loginForm.value.username, { expires: 30 })
      Cookies.set('password', encrypt(loginForm.value.password), { expires: 30 })
      Cookies.set('rememberMe', 'true', { expires: 30 })
    } else {
      Cookies.remove('username')
      Cookies.remove('password')
      Cookies.remove('rememberMe')
    }

    await userStore.login(loginForm.value)

    // 登入成功後合併訪客購物車
    try {
      await cartStore.mergeGuestCartOnLogin()
    } catch (e) {
      console.error('合併購物車失敗', e)
    }

    ElMessage.success('登入成功')
    router.push(redirect.value)
  } catch (error) {
    loading.value = false
    if (captchaEnabled.value) {
      getCode()
    }
  }
}

function handleLineLogin() {
  ElMessage.info('LINE 登入功能開發中')
}

onMounted(() => {
  getCode()
  getCookie()
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
  padding: 60px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-card {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 32px;
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
  margin-bottom: 20px;
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

/* 表單選項 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 14px;
  color: var(--el-color-primary);
  text-decoration: none;
}

.forgot-link:hover {
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

/* 分隔線 */
.divider {
  display: flex;
  align-items: center;
  margin: 28px 0;
  color: #c0c4cc;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #ebeef5;
}

.divider span {
  padding: 0 16px;
}

/* 社群登入 */
.social-login {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.social-btn {
  flex: 1;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

.line-btn {
  background: #00C300;
  border-color: #00C300;
  color: white;
}

.line-btn:hover {
  background: #00B300;
  border-color: #00B300;
  transform: translateY(-2px);
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
