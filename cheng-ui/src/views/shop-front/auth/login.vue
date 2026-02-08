<template>
  <div class="login-page">
    <div class="motion-background" :style="motionBackgroundStyle" aria-hidden="true"></div>
    <div class="gradient-orb orb-one" aria-hidden="true"></div>
    <div class="gradient-orb orb-two" aria-hidden="true"></div>
    <div class="gradient-orb orb-three" aria-hidden="true"></div>

    <div class="login-shell">
      <aside class="hero-panel">
        <div class="hero-content">
          <div class="hero-logo">
            <img src="@/assets/logo/logo.png" alt="CoolApps" />
          </div>
          <p class="hero-kicker">WELCOME BACK</p>
          <h1 class="hero-title">登入會員，快速完成你的下單旅程</h1>
          <p class="hero-desc">
            立即登入以查看購物車、訂單進度與會員優惠，享受更順暢、更安全的購物體驗。
          </p>

          <div class="hero-grid">
            <article class="hero-card">
              <el-icon><ShoppingCart /></el-icon>
              <div>
                <h3>快速購物流程</h3>
                <p>登入後可保留購物車內容與收件資訊。</p>
              </div>
            </article>
            <article class="hero-card">
              <el-icon><Van /></el-icon>
              <div>
                <h3>即時訂單追蹤</h3>
                <p>配送狀態與付款結果即時同步。</p>
              </div>
            </article>
            <article class="hero-card">
              <el-icon><Service /></el-icon>
              <div>
                <h3>專屬客服協助</h3>
                <p>登入會員後可快速取得售後支援。</p>
              </div>
            </article>
          </div>

          <div class="promo-chip">
            <span>MEMBER EXCLUSIVE</span>
            <strong>本週會員限定免運活動</strong>
          </div>
        </div>
      </aside>

      <section class="form-panel">
        <div class="form-surface">
          <header class="form-header">
            <span class="form-badge">會員登入</span>
            <h2>歡迎回來</h2>
            <p>請輸入帳號與密碼，繼續你的購物行程。</p>
          </header>

          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" label-position="top">
            <el-form-item label="帳號（手機或 Email）" prop="username">
              <el-input
                v-model="loginForm.username"
                size="large"
                placeholder="請輸入手機或 Email"
                :prefix-icon="User"
                autocomplete="username"
              />
            </el-form-item>

            <el-form-item label="密碼" prop="password">
              <el-input
                v-model="loginForm.password"
                size="large"
                :type="showPassword ? 'text' : 'password'"
                placeholder="請輸入密碼"
                :prefix-icon="Lock"
                autocomplete="current-password"
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

            <el-form-item v-if="captchaEnabled" label="驗證碼" prop="code">
              <div class="captcha-row">
                <el-input
                  v-model="loginForm.code"
                  size="large"
                  placeholder="請輸入驗證碼"
                  :prefix-icon="Key"
                  autocomplete="one-time-code"
                  @keyup.enter="handleLogin"
                />
                <button type="button" class="captcha-btn" @click="getCode">
                  <img :src="codeUrl" alt="驗證碼" />
                  <span class="captcha-mask">
                    <el-icon><Refresh /></el-icon>
                    <em>更新</em>
                  </span>
                </button>
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
              {{ loading ? '登入中...' : '立即登入' }}
            </el-button>

            <div class="form-footer">
              <span>還沒有帳號？</span>
              <router-link :to="{ path: '/register', query: { redirect: redirect } }">
                立即註冊
              </router-link>
            </div>

            <p class="policy-note">
              登入即表示同意
              <router-link class="inline-link" to="/terms" target="_blank" rel="noopener noreferrer">
                服務條款
              </router-link>
              與
              <router-link class="inline-link" to="/privacy" target="_blank" rel="noopener noreferrer">
                隱私政策
              </router-link>
            </p>

            <div class="divider">
              <span>或使用其他方式登入</span>
            </div>

            <div class="social-login">
              <el-button class="social-btn line-btn" @click="handleLineLogin">
                <svg viewBox="0 0 24 24" width="18" height="18">
                  <path fill="currentColor" d="M19.365 9.863c.349 0 .63.285.63.631 0 .345-.281.63-.63.63H17.61v1.125h1.755c.349 0 .63.283.63.63 0 .344-.281.629-.63.629h-2.386c-.345 0-.627-.285-.627-.629V8.108c0-.345.282-.63.63-.63h2.386c.349 0 .63.285.63.63 0 .349-.281.63-.63.63H17.61v1.125h1.755zm-3.855 3.016c0 .27-.174.51-.432.596-.064.021-.133.031-.199.031-.211 0-.391-.09-.51-.25l-2.443-3.317v2.94c0 .344-.279.629-.631.629-.346 0-.626-.285-.626-.629V8.108c0-.27.173-.51.43-.595.06-.023.136-.033.194-.033.195 0 .375.104.495.254l2.462 3.33V8.108c0-.345.282-.63.63-.63.345 0 .63.285.63.63v4.771zm-5.741 0c0 .344-.282.629-.631.629-.345 0-.627-.285-.627-.629V8.108c0-.345.282-.63.63-.63.346 0 .628.285.628.63v4.771zm-2.466.629H4.917c-.345 0-.63-.285-.63-.629V8.108c0-.345.285-.63.63-.63.348 0 .63.285.63.63v4.141h1.756c.348 0 .629.283.629.63 0 .344-.282.629-.629.629M24 10.314C24 4.943 18.615.572 12 .572S0 4.943 0 10.314c0 4.811 4.27 8.842 10.035 9.608.391.082.923.258 1.058.59.12.301.079.766.038 1.08l-.164 1.02c-.045.301-.24 1.186 1.049.645 1.291-.539 6.916-4.078 9.436-6.975C23.176 14.393 24 12.458 24 10.314"/>
                </svg>
                <span>LINE 登入</span>
              </el-button>
            </div>
          </el-form>
        </div>
      </section>
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
import useMemberStore from '@/store/modules/member'
import { useCartStore } from '@/store/modules/cart'
import loginBackground from '@/assets/images/login-background.jpg'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()
const cartStore = useCartStore()

const loginFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const captchaEnabled = ref(true)
const codeUrl = ref('')

const redirect = computed(() => route.query.redirect || '/')
const motionBackgroundStyle = computed(() => ({
  backgroundImage: `linear-gradient(160deg, rgba(7, 42, 104, 0.84), rgba(22, 108, 205, 0.66)), url(${loginBackground})`
}))

const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false,
  code: '',
  uuid: ''
})

const loginRules = {
  username: [
    { required: true, trigger: 'blur', message: '請輸入手機或 Email' }
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

  loading.value = true
  try {
    await loginFormRef.value.validate()

    if (loginForm.value.rememberMe) {
      Cookies.set('username', loginForm.value.username, { expires: 30 })
      Cookies.set('password', encrypt(loginForm.value.password), { expires: 30 })
      Cookies.set('rememberMe', 'true', { expires: 30 })
    } else {
      Cookies.remove('username')
      Cookies.remove('password')
      Cookies.remove('rememberMe')
    }

    await memberStore.login({
      account: loginForm.value.username,
      password: loginForm.value.password,
      code: loginForm.value.code,
      uuid: loginForm.value.uuid
    })

    try {
      await cartStore.mergeGuestCartOnLogin()
    } catch (e) {
      console.error('合併購物車失敗', e)
    }

    ElMessage.success('登入成功')
    await router.push(redirect.value)
  } catch (error) {
    if (captchaEnabled.value) {
      getCode()
    }
  } finally {
    loading.value = false
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
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&display=swap');

.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(145deg, #041a45, #0a2d69 40%, #0f3f87);
  font-family: 'Noto Sans TC', sans-serif;
}

.motion-background {
  position: absolute;
  inset: auto -10% -18% -10%;
  height: 70%;
  background-size: cover;
  background-position: center;
  filter: saturate(1.15) brightness(0.92);
  transform-origin: center;
  animation: bgDrift 18s ease-in-out infinite alternate;
  opacity: 0.48;
}

.motion-background::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(0deg, rgba(3, 10, 25, 0.72), rgba(3, 10, 25, 0));
}

.gradient-orb {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.orb-one {
  width: 360px;
  height: 360px;
  top: -120px;
  right: 12%;
  background: radial-gradient(circle, rgba(121, 208, 255, 0.82), rgba(121, 208, 255, 0));
  animation: orbFloat 9s ease-in-out infinite;
}

.orb-two {
  width: 420px;
  height: 420px;
  left: -150px;
  top: 28%;
  background: radial-gradient(circle, rgba(79, 145, 255, 0.64), rgba(79, 145, 255, 0));
  animation: orbFloat 10s ease-in-out infinite 1.3s;
}

.orb-three {
  width: 320px;
  height: 320px;
  right: -80px;
  bottom: 6%;
  background: radial-gradient(circle, rgba(49, 102, 221, 0.72), rgba(49, 102, 221, 0));
  animation: orbFloat 11s ease-in-out infinite 2.2s;
}

.login-shell {
  width: min(1220px, 100%);
  min-height: 750px;
  display: grid;
  grid-template-columns: 52% 48%;
  border-radius: 32px;
  overflow: hidden;
  position: relative;
  z-index: 2;
  border: 1px solid rgba(166, 213, 255, 0.34);
  box-shadow: 0 28px 56px rgba(2, 8, 24, 0.48);
}

.hero-panel {
  padding: 56px 48px;
  background:
    linear-gradient(152deg, rgba(8, 34, 85, 0.88), rgba(12, 72, 149, 0.74)),
    radial-gradient(circle at 15% 12%, rgba(121, 208, 255, 0.2), transparent 45%);
  position: relative;
}

.hero-panel::after {
  content: '';
  position: absolute;
  inset: 20px;
  border-radius: 24px;
  border: 1px solid rgba(201, 228, 255, 0.22);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 1;
  color: #eaf4ff;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.hero-logo {
  width: 94px;
  height: 94px;
  border-radius: 24px;
  background: rgba(241, 248, 255, 0.96);
  box-shadow: 0 14px 26px rgba(3, 15, 43, 0.36);
  padding: 12px;
}

.hero-logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.hero-kicker {
  margin: 24px 0 10px;
  font-size: 12px;
  letter-spacing: 0.2em;
  font-weight: 700;
  color: rgba(225, 242, 255, 0.86);
}

.hero-title {
  margin: 0;
  font-size: 40px;
  line-height: 1.2;
  font-family: 'Noto Serif TC', serif;
}

.hero-desc {
  margin: 18px 0 24px;
  color: rgba(227, 243, 255, 0.84);
  line-height: 1.8;
  font-size: 15px;
}

.hero-grid {
  display: grid;
  gap: 12px;
}

.hero-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(219, 239, 255, 0.22);
  backdrop-filter: blur(5px);
}

.hero-card .el-icon {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(121, 208, 255, 0.22);
  color: #e8f5ff;
  flex-shrink: 0;
}

.hero-card h3 {
  margin: 0 0 3px;
  font-size: 14px;
  color: #eef7ff;
}

.hero-card p {
  margin: 0;
  font-size: 12px;
  color: rgba(225, 242, 255, 0.78);
  line-height: 1.6;
}

.promo-chip {
  margin-top: auto;
  border-radius: 16px;
  padding: 16px;
  background: linear-gradient(130deg, rgba(121, 208, 255, 0.24), rgba(63, 147, 255, 0.36));
  border: 1px solid rgba(203, 233, 255, 0.36);
  box-shadow: 0 14px 26px rgba(6, 24, 64, 0.24);
}

.promo-chip span {
  display: inline-flex;
  align-items: center;
  height: 24px;
  border-radius: 999px;
  padding: 0 10px;
  background: rgba(7, 36, 87, 0.42);
  font-size: 11px;
  letter-spacing: 0.12em;
  color: #d8eeff;
}

.promo-chip strong {
  display: block;
  margin-top: 8px;
  font-size: 26px;
  font-family: 'Lexend', sans-serif;
}

.form-panel {
  padding: 44px 34px;
  background: linear-gradient(180deg, rgba(238, 246, 255, 0.9), rgba(232, 243, 255, 0.96));
  display: flex;
  align-items: center;
}

.form-surface {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  border-radius: 24px;
  padding: 28px 28px 24px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(185, 214, 246, 0.76);
  box-shadow: 0 18px 32px rgba(23, 58, 108, 0.16);
}

.form-header {
  margin-bottom: 16px;
}

.form-badge {
  display: inline-flex;
  align-items: center;
  height: 28px;
  border-radius: 999px;
  padding: 0 12px;
  background: rgba(63, 147, 255, 0.16);
  border: 1px solid rgba(63, 147, 255, 0.32);
  color: #1f5bbf;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.form-header h2 {
  margin: 12px 0 8px;
  font-size: 32px;
  font-family: 'Noto Serif TC', serif;
  color: #11294e;
  line-height: 1.14;
}

.form-header p {
  margin: 0;
  color: #4b6592;
  font-size: 14px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.login-form :deep(.el-form-item__label) {
  color: #2b4c7e;
  font-weight: 700;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #d7e7fb inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #9dc9ff inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(63, 147, 255, 0.4) inset;
}

.password-toggle {
  cursor: pointer;
  color: #83a3cd;
}

.password-toggle:hover {
  color: #2c68c7;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 130px;
  gap: 10px;
}

.captcha-btn {
  border: 1px solid #d6e7fc;
  border-radius: 12px;
  overflow: hidden;
  padding: 0;
  position: relative;
  cursor: pointer;
  background: #fff;
}

.captcha-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-mask {
  position: absolute;
  inset: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s ease;
  background: rgba(14, 40, 87, 0.45);
  color: #e9f6ff;
  font-size: 12px;
}

.captcha-mask em {
  font-style: normal;
}

.captcha-btn:hover .captcha-mask {
  opacity: 1;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.form-options :deep(.el-checkbox__label) {
  color: #4f6c95;
}

.forgot-link {
  color: #2165ce;
  font-weight: 600;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  min-height: 50px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(125deg, #1b63ca, #47a6ff);
  box-shadow: 0 14px 22px rgba(24, 87, 176, 0.24);
  font-size: 16px;
  font-weight: 700;
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: -140%;
  left: -34%;
  width: 25%;
  height: 380%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  transform: rotate(20deg);
  transition: left 0.4s ease;
}

.submit-btn:hover::before {
  left: 150%;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 26px rgba(24, 87, 176, 0.3);
}

.submit-btn:focus-visible {
  outline: 2px solid rgba(65, 157, 255, 0.62);
  outline-offset: 2px;
}

.form-footer {
  margin-top: 16px;
  text-align: center;
  color: #5d759d;
  font-size: 14px;
}

.form-footer a {
  color: #2165ce;
  text-decoration: none;
  font-weight: 700;
  margin-left: 4px;
}

.form-footer a:hover {
  text-decoration: underline;
}

.policy-note {
  margin: 12px 0 0;
  text-align: center;
  color: #6b81a4;
  font-size: 12px;
}

.inline-link {
  color: #2165ce;
  text-decoration: none;
  font-weight: 700;
  margin: 0 2px;
}

.inline-link:hover {
  text-decoration: underline;
}

.divider {
  display: flex;
  align-items: center;
  margin: 22px 0 14px;
  color: #86a2ca;
  font-size: 12px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #d7e7fc;
}

.divider span {
  padding: 0 12px;
}

.social-login {
  display: flex;
}

.social-btn {
  flex: 1;
  min-height: 44px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 700;
}

.line-btn {
  background: #00c300;
  border-color: #00c300;
  color: #fff;
}

.line-btn:hover {
  background: #00ae00;
  border-color: #00ae00;
  transform: translateY(-1px);
}

@keyframes bgDrift {
  0% {
    transform: scale(1.05) translateY(0);
    background-position: center 52%;
  }
  50% {
    transform: scale(1.09) translateY(-6px);
    background-position: center 46%;
  }
  100% {
    transform: scale(1.05) translateY(0);
    background-position: center 56%;
  }
}

@keyframes orbFloat {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }
  50% {
    transform: translate3d(0, -14px, 0);
  }
}

@media (max-width: 1280px) {
  .login-page {
    padding: 18px;
  }

  .login-shell {
    grid-template-columns: 50% 50%;
  }

  .hero-panel {
    padding: 46px 36px;
  }

  .hero-title {
    font-size: 34px;
  }
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
    border-radius: 22px;
  }

  .hero-panel {
    padding: 28px 22px;
  }

  .hero-panel::after {
    inset: 12px;
  }

  .hero-title {
    font-size: 26px;
  }

  .promo-chip {
    margin-top: 18px;
  }

  .form-panel {
    padding: 22px 14px 24px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 10px;
  }

  .hero-logo {
    width: 74px;
    height: 74px;
    border-radius: 16px;
  }

  .hero-grid {
    gap: 10px;
  }

  .hero-card {
    padding: 12px;
  }

  .form-surface {
    border-radius: 16px;
    padding: 20px 14px;
  }

  .form-header h2 {
    font-size: 26px;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .captcha-btn {
    min-height: 48px;
  }
}
</style>
