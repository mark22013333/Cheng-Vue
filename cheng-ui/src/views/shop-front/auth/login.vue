<template>
  <div class="login-page">
    <!-- 動態漸層背景 -->
    <div class="mesh-bg">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
    </div>

    <!-- Skeleton Loading -->
    <div v-if="!pageReady" class="glass-card skeleton-card">
      <div class="skeleton-header">
        <div class="skeleton-badge shimmer"></div>
        <div class="skeleton-title shimmer"></div>
        <div class="skeleton-subtitle shimmer"></div>
      </div>
      <div class="skeleton-body">
        <div class="skeleton-label shimmer"></div>
        <div class="skeleton-input shimmer"></div>
        <div class="skeleton-label shimmer"></div>
        <div class="skeleton-input shimmer"></div>
        <div class="skeleton-row">
          <div class="skeleton-check shimmer"></div>
          <div class="skeleton-link shimmer"></div>
        </div>
        <div class="skeleton-btn shimmer"></div>
        <div class="skeleton-footer shimmer"></div>
        <div class="skeleton-divider shimmer"></div>
        <div class="skeleton-social">
          <div class="skeleton-social-btn shimmer"></div>
          <div class="skeleton-social-btn shimmer"></div>
        </div>
      </div>
    </div>

    <!-- 登入表單 Glass Card -->
    <transition name="card-reveal">
      <div v-if="pageReady" class="glass-card form-card">
        <!-- 品牌 Logo -->
        <div class="card-brand">
          <img src="@/assets/logo/logo.png" alt="" class="card-logo" />
          <span class="card-brand-text">CoolApps</span>
        </div>

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

          <div class="form-options">
            <el-tooltip
              :disabled="functionalAllowed"
              content="需接受 Cookie 才能使用此功能"
              placement="top"
            >
              <el-checkbox
                v-model="loginForm.rememberMe"
                :disabled="!functionalAllowed"
              >記住我</el-checkbox>
            </el-tooltip>
            <router-link to="/forgot-password" class="forgot-link">忘記密碼？</router-link>
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
            <a class="inline-link" href="javascript:;" @click="openPolicy('terms')">服務條款</a>
            與
            <a class="inline-link" href="javascript:;" @click="openPolicy('privacy')">隱私政策</a>
          </p>

          <div class="divider">
            <span>或使用其他方式登入</span>
          </div>

          <div class="social-login">
            <button type="button" class="social-btn line-btn" @click="handleLineLogin">
              <svg viewBox="0 0 24 24" width="18" height="18">
                <path fill="currentColor" d="M19.365 9.863c.349 0 .63.285.63.631 0 .345-.281.63-.63.63H17.61v1.125h1.755c.349 0 .63.283.63.63 0 .344-.281.629-.63.629h-2.386c-.345 0-.627-.285-.627-.629V8.108c0-.345.282-.63.63-.63h2.386c.349 0 .63.285.63.63 0 .349-.281.63-.63.63H17.61v1.125h1.755zm-3.855 3.016c0 .27-.174.51-.432.596-.064.021-.133.031-.199.031-.211 0-.391-.09-.51-.25l-2.443-3.317v2.94c0 .344-.279.629-.631.629-.346 0-.626-.285-.626-.629V8.108c0-.27.173-.51.43-.595.06-.023.136-.033.194-.033.195 0 .375.104.495.254l2.462 3.33V8.108c0-.345.282-.63.63-.63.345 0 .63.285.63.63v4.771zm-5.741 0c0 .344-.282.629-.631.629-.345 0-.627-.285-.627-.629V8.108c0-.345.282-.63.63-.63.346 0 .628.285.628.63v4.771zm-2.466.629H4.917c-.345 0-.63-.285-.63-.629V8.108c0-.345.285-.63.63-.63.348 0 .63.285.63.63v4.141h1.756c.348 0 .629.283.629.63 0 .344-.282.629-.629.629M24 10.314C24 4.943 18.615.572 12 .572S0 4.943 0 10.314c0 4.811 4.27 8.842 10.035 9.608.391.082.923.258 1.058.59.12.301.079.766.038 1.08l-.164 1.02c-.045.301-.24 1.186 1.049.645 1.291-.539 6.916-4.078 9.436-6.975C23.176 14.393 24 12.458 24 10.314"/>
              </svg>
              <span>LINE 登入</span>
            </button>
            <button type="button" class="social-btn google-btn" @click="handleGoogleLogin">
              <svg viewBox="0 0 24 24" width="18" height="18">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              <span>Google 登入</span>
            </button>
          </div>
        </el-form>
      </div>
    </transition>

    <el-dialog
      v-model="policyVisible"
      width="640px"
      top="6vh"
      class="policy-dialog"
      modal-class="policy-overlay"
      destroy-on-close
      :show-close="false"
    >
      <template #header>
        <div class="policy-header">
          <div class="policy-icon-wrap">
            <svg v-if="policyType === 'terms'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          </div>
          <div>
            <h3 class="policy-title">{{ policyType === 'terms' ? '服務條款' : '隱私政策' }}</h3>
            <p class="policy-subtitle">{{ policyType === 'terms' ? '使用本服務前，請詳閱以下條款' : '了解我們如何保護您的個人資料' }}</p>
          </div>
          <button class="policy-close-btn" @click="policyVisible = false" aria-label="關閉">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </template>
      <div class="policy-content">
        <template v-if="policyType === 'terms'">
          <div class="policy-section">
            <h4><span class="section-num">01</span>服務範圍</h4>
            <p>CoolApps 提供線上商品瀏覽、購買、訂單管理等電子商務服務。我們保留隨時修改或中止服務的權利。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">02</span>帳號管理</h4>
            <p>您有責任妥善保管帳號及密碼，因帳號或密碼外洩所產生的一切損失由用戶自行承擔。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">03</span>購物與付款</h4>
            <p>商品價格以下單時頁面顯示為準。付款成功後，系統將寄送訂單確認通知。如有退換貨需求，請參閱退換貨政策。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">04</span>智慧財產權</h4>
            <p>本平台所有內容（包括但不限於文字、圖片、商標）均受智慧財產權法律保護，未經授權不得使用。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">05</span>免責聲明</h4>
            <p>本平台對因不可抗力、系統維護或第三方服務中斷導致的損失不承擔責任。</p>
          </div>
        </template>
        <template v-else>
          <div class="policy-section">
            <h4><span class="section-num">01</span>資料蒐集</h4>
            <p>我們蒐集的資料包括：姓名、Email、手機號碼、收件地址及購物紀錄等，用於提供服務與改善體驗。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">02</span>資料使用</h4>
            <p>您的資料僅用於訂單處理、客戶服務、行銷通知（經同意後）及平台改善分析。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">03</span>資料保護</h4>
            <p>我們採用業界標準的加密技術與安全措施保護您的個人資料，防止未經授權的存取。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">04</span>Cookie 使用</h4>
            <p>本平台使用 Cookie 記錄您的偏好設定與登入狀態，以提供更好的瀏覽體驗。</p>
          </div>
          <div class="policy-section">
            <h4><span class="section-num">05</span>您的權利</h4>
            <p>您可以隨時要求查閱、更正或刪除您的個人資料，請透過客服聯繫我們。</p>
          </div>
        </template>
      </div>
      <template #footer>
        <div class="policy-footer">
          <p class="policy-updated">最後更新：2025 年 1 月</p>
          <button class="policy-confirm-btn" @click="policyVisible = false">我已瞭解</button>
        </div>
      </template>
    </el-dialog>
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
  Hide
} from '@element-plus/icons-vue'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import Cookies from 'js-cookie'
import useMemberStore from '@/store/modules/member'
import { useCartStore } from '@/store/modules/cart'
import { useConsentStore } from '@/store/modules/consent'
import { useSocialLogin } from '@/composables/useSocialLogin'
import { mergeGuestTracking } from '@/api/shop/marketing'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()
const cartStore = useCartStore()
const consentStore = useConsentStore()

const functionalAllowed = computed(() => consentStore.isAllowed('functional'))

const loginFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const policyVisible = ref(false)
const policyType = ref('terms')
const pageReady = ref(false)

function openPolicy(type) {
  policyType.value = type
  policyVisible.value = true
}

const AUTH_PATHS = ['/register', '/login', '/forgot-password', '/reset-password']

const redirect = computed(() => {
  const r = route.query.redirect
  if (r && AUTH_PATHS.some(p => r.startsWith(p))) {
    return '/'
  }
  return r || '/'
})

const { handleGoogleLogin, handleLineLogin } = useSocialLogin(redirect)

const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, trigger: 'blur', message: '請輸入手機或 Email' }
  ],
  password: [
    { required: true, trigger: 'blur', message: '請輸入密碼' }
  ]
}

function getCookie() {
  if (!consentStore.isAllowed('functional')) return

  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')
  loginForm.value = {
    username: username || '',
    password: password ? decrypt(password) : '',
    rememberMe: rememberMe === 'true'
  }
}

async function handleLogin() {
  if (!loginFormRef.value) return

  loading.value = true
  try {
    await loginFormRef.value.validate()

    if (consentStore.isAllowed('functional')) {
      if (loginForm.value.rememberMe) {
        Cookies.set('username', loginForm.value.username, { expires: 30 })
        Cookies.set('password', encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set('rememberMe', 'true', { expires: 30 })
      } else {
        Cookies.remove('username')
        Cookies.remove('password')
        Cookies.remove('rememberMe')
      }
    }

    await memberStore.login({
      account: loginForm.value.username,
      password: loginForm.value.password
    })

    try {
      await cartStore.mergeGuestCartOnLogin()
    } catch (e) {
      console.error('合併購物車失敗', e)
    }

    // 合併訪客追蹤記錄（fire-and-forget）
    const guestTrackingId = Cookies.get('guest_tracking_id')
    if (guestTrackingId) {
      mergeGuestTracking(guestTrackingId).catch(() => {})
      Cookies.remove('guest_tracking_id', { path: '/' })
    }

    ElMessage.success('登入成功')
    await router.push(redirect.value)
  } catch (error) {
    // 登入失敗，不需額外處理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getCookie()
  setTimeout(() => { pageReady.value = true }, 600)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&family=Noto+Sans+Symbols+2&family=Noto+Color+Emoji&display=swap');

/* ====== 頁面容器 ====== */
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100dvh - 90px);
  padding: 40px 24px;
  overflow: hidden;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  /* 突破父層 max-width 限制，讓背景延伸全寬 */
  width: 100vw;
  margin-left: calc(50% - 50vw);
}

/* ====== 動態漸層背景 ====== */
.mesh-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #F5F0EB 0%, #EDE8E2 30%, #E8E0D8 60%, #F0ECE6 100%);
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  will-change: transform;
}

.orb-1 {
  width: 420px;
  height: 420px;
  background: color-mix(in srgb, var(--mall-primary, #4A6B7C) 35%, transparent);
  top: -12%;
  left: -8%;
  animation: orb-drift-1 18s ease-in-out infinite;
}

.orb-2 {
  width: 380px;
  height: 380px;
  background: rgba(165, 99, 92, 0.28);
  bottom: -15%;
  right: -8%;
  animation: orb-drift-2 22s ease-in-out infinite;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: rgba(196, 168, 130, 0.3);
  top: 35%;
  right: 20%;
  animation: orb-drift-3 25s ease-in-out infinite;
}

.orb-4 {
  width: 250px;
  height: 250px;
  background: rgba(90, 138, 154, 0.2);
  bottom: 20%;
  left: 15%;
  animation: orb-drift-4 20s ease-in-out infinite;
}

@keyframes orb-drift-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(60px, 40px) scale(1.05); }
  66% { transform: translate(-30px, 70px) scale(0.95); }
}

@keyframes orb-drift-2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-50px, -40px) scale(1.08); }
  66% { transform: translate(40px, -60px) scale(0.92); }
}

@keyframes orb-drift-3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-40px, 30px) scale(1.1); }
}

@keyframes orb-drift-4 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(50px, -25px) scale(1.06); }
}

/* ====== Glassmorphism 卡片 ====== */
.glass-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  padding: 36px 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.06),
    0 2px 8px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

/* ====== Skeleton Loading ====== */
.skeleton-card {
  animation: skeleton-pulse 0.6s ease-out;
}

@keyframes skeleton-pulse {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.shimmer {
  background: linear-gradient(90deg, #e8e2da 25%, #f0ece6 37%, #e8e2da 63%);
  background-size: 400% 100%;
  animation: shimmer 1.6s ease-in-out infinite;
  border-radius: 8px;
}

@keyframes shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.skeleton-header { margin-bottom: 24px; }
.skeleton-badge { width: 80px; height: 26px; border-radius: 999px; margin-bottom: 14px; }
.skeleton-title { width: 160px; height: 32px; margin-bottom: 10px; }
.skeleton-subtitle { width: 240px; height: 16px; }
.skeleton-body { display: flex; flex-direction: column; gap: 12px; }
.skeleton-label { width: 140px; height: 14px; }
.skeleton-input { width: 100%; height: 48px; border-radius: 12px; }
.skeleton-row { display: flex; justify-content: space-between; align-items: center; }
.skeleton-check { width: 70px; height: 16px; }
.skeleton-link { width: 80px; height: 14px; }
.skeleton-btn { width: 100%; height: 50px; border-radius: 14px; margin-top: 4px; }
.skeleton-footer { width: 160px; height: 16px; align-self: center; }
.skeleton-divider { width: 100%; height: 1px; margin: 8px 0; }
.skeleton-social { display: flex; gap: 10px; }
.skeleton-social-btn { flex: 1; height: 46px; border-radius: 12px; }

/* ====== 卡片出場動畫 ====== */
.card-reveal-enter-active {
  animation: card-enter 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes card-enter {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.96);
    filter: blur(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}

/* ====== 品牌 Logo ====== */
.card-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 24px;
}

.card-logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
  opacity: 0.85;
}

.card-brand-text {
  font-family: 'Inter', 'Lexend', 'Avenir', sans-serif;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #3D2B1F;
}

/* ====== 表頭 ====== */
.form-header {
  text-align: center;
  margin-bottom: 28px;
}

.form-badge {
  display: inline-flex;
  align-items: center;
  height: 26px;
  border-radius: 999px;
  padding: 0 12px;
  background: color-mix(in srgb, var(--mall-primary, #4A6B7C) 12%, transparent);
  backdrop-filter: blur(8px);
  border: 1px solid color-mix(in srgb, var(--mall-primary, #4A6B7C) 15%, transparent);
  color: var(--mall-primary, #4A6B7C);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.06em;
}

.form-header h2 {
  margin: 14px 0 8px;
  font-size: 28px;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif;
  color: #3D2B1F;
  line-height: 1.2;
  letter-spacing: -0.01em;
}

.form-header p {
  margin: 0;
  color: #7A6B5D;
  font-size: 14px;
  line-height: 1.5;
}

/* ====== 表單元件 — Glass Input ====== */
.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-form-item__label) {
  color: #5A4A3C;
  font-weight: 600;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 6px;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 14px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06) inset;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--mall-primary, #4A6B7C) 25%, transparent) inset;
  background: rgba(255, 255, 255, 0.7);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 2px color-mix(in srgb, var(--mall-primary, #4A6B7C) 35%, transparent) inset,
    0 0 20px color-mix(in srgb, var(--mall-primary, #4A6B7C) 8%, transparent);
  background: rgba(255, 255, 255, 0.8);
}

.password-toggle {
  cursor: pointer;
  color: #A89585;
  transition: color 0.2s, transform 0.2s;
}

.password-toggle:hover {
  color: var(--mall-primary, #4A6B7C);
  transform: scale(1.1);
}

/* ====== 選項列 ====== */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.form-options :deep(.el-checkbox__label) {
  color: #7A6B5D;
}

.forgot-link {
  color: var(--mall-primary, #4A6B7C);
  font-weight: 600;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  filter: brightness(0.85);
  text-decoration: underline;
}

/* ====== 送出按鈕 ====== */
.submit-btn {
  width: 100%;
  min-height: 50px;
  border-radius: 16px;
  border: none;
  background: linear-gradient(135deg, var(--mall-primary, #4A6B7C) 0%, var(--mall-primary-end, #5A8A9A) 100%);
  background-size: 200% 100%;
  box-shadow: 0 4px 16px color-mix(in srgb, var(--mall-primary, #4A6B7C) 25%, transparent);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.3px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.25), transparent);
  transition: left 0.6s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px color-mix(in srgb, var(--mall-primary, #4A6B7C) 35%, transparent);
  background-position: 100% 0;
}

.submit-btn:hover::before {
  left: 100%;
}

.submit-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px color-mix(in srgb, var(--mall-primary, #4A6B7C) 20%, transparent);
}

.submit-btn:focus-visible {
  outline: 2px solid color-mix(in srgb, var(--mall-primary, #4A6B7C) 50%, transparent);
  outline-offset: 3px;
}

/* ====== 連結 ====== */
.form-footer {
  margin-top: 18px;
  text-align: center;
  color: #7A6B5D;
  font-size: 14px;
}

.form-footer a {
  color: var(--mall-primary, #4A6B7C);
  text-decoration: none;
  font-weight: 700;
  margin-left: 4px;
  transition: color 0.2s;
}

.form-footer a:hover {
  filter: brightness(0.85);
  text-decoration: underline;
}

.policy-note {
  margin: 10px 0 0;
  text-align: center;
  color: #9A8B7D;
  font-size: 12px;
}

.inline-link {
  color: var(--mall-primary, #4A6B7C);
  text-decoration: none;
  font-weight: 600;
  margin: 0 2px;
}

.inline-link:hover { text-decoration: underline; }

/* ====== 分隔線 ====== */
.divider {
  display: flex;
  align-items: center;
  margin: 24px 0 18px;
  color: #A89585;
  font-size: 12px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0,0,0,0.08), transparent);
}

.divider span {
  padding: 0 14px;
  white-space: nowrap;
}

/* ====== 社群登入 — Glass Buttons ====== */
.social-login {
  display: flex;
  gap: 12px;
}

.social-btn {
  flex: 1;
  min-height: 46px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: inherit;
}

.line-btn {
  background: rgba(6, 199, 85, 0.9);
  backdrop-filter: blur(8px);
  border-color: rgba(6, 199, 85, 0.3);
  color: #fff;
}

.line-btn:hover {
  background: rgba(6, 199, 85, 1);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(6, 199, 85, 0.3);
}

.google-btn {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  border-color: rgba(0, 0, 0, 0.08);
  color: #5A4A3C;
}

.google-btn:hover {
  background: rgba(255, 255, 255, 0.85);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

/* ====== RWD ====== */
@media (max-width: 600px) {
  .login-page {
    padding: 20px 12px;
    min-height: calc(100dvh - 120px);
  }

  .glass-card {
    padding: 28px 20px;
    border-radius: 20px;
  }

  .form-header h2 {
    font-size: 24px;
  }

  .social-login {
    flex-direction: column;
  }

  .orb { filter: blur(60px); }
  .orb-1 { width: 280px; height: 280px; }
  .orb-2 { width: 240px; height: 240px; }
  .orb-3 { width: 200px; height: 200px; }
  .orb-4 { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .orb, .shimmer { animation: none; }
  .card-reveal-enter-active { animation: none; }
}

/* ====== 條款彈窗內部元件 ====== */
.policy-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px 28px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
}

.policy-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, color-mix(in srgb, var(--mall-primary, #4A6B7C) 12%, transparent), color-mix(in srgb, var(--mall-primary, #4A6B7C) 6%, transparent));
  border: 1px solid color-mix(in srgb, var(--mall-primary, #4A6B7C) 15%, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-primary, #4A6B7C);
  flex-shrink: 0;
}

.policy-title {
  margin: 0;
  font-size: 18px;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif;
  font-weight: 700;
  color: #3D2B1F;
  line-height: 1.3;
}

.policy-subtitle {
  margin: 2px 0 0;
  font-size: 13px;
  color: #9A8B7D;
  line-height: 1.4;
}

.policy-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  color: #7A6B5D;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.policy-close-btn:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #3D2B1F;
  transform: scale(1.05);
}

.policy-content {
  max-height: 55vh;
  overflow-y: auto;
  padding: 24px 0;
  scrollbar-width: thin;
  scrollbar-color: color-mix(in srgb, var(--mall-primary, #4A6B7C) 20%, transparent) transparent;
}

.policy-content::-webkit-scrollbar {
  width: 5px;
}

.policy-content::-webkit-scrollbar-track {
  background: transparent;
}

.policy-content::-webkit-scrollbar-thumb {
  background: color-mix(in srgb, var(--mall-primary, #4A6B7C) 20%, transparent);
  border-radius: 999px;
}

.policy-content::-webkit-scrollbar-thumb:hover {
  background: color-mix(in srgb, var(--mall-primary, #4A6B7C) 35%, transparent);
}

.policy-section {
  padding: 16px 20px;
  margin-bottom: 10px;
  border-radius: 14px;
  background: rgba(240, 236, 230, 0.45);
  border: 1px solid rgba(224, 213, 200, 0.5);
  transition: background 0.2s ease, box-shadow 0.2s ease;
}

.policy-section:hover {
  background: rgba(240, 236, 230, 0.65);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.policy-section:last-child {
  margin-bottom: 0;
}

.policy-section h4 {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: #3D2B1F;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-num {
  font-family: 'Inter', 'Lexend', monospace;
  font-size: 12px;
  font-weight: 700;
  color: var(--mall-primary, #4A6B7C);
  background: color-mix(in srgb, var(--mall-primary, #4A6B7C) 10%, transparent);
  border-radius: 6px;
  padding: 2px 8px;
  letter-spacing: 0.04em;
}

.policy-section p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #5A4A3C;
}

.policy-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 28px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.policy-updated {
  margin: 0;
  font-size: 12px;
  color: #A89585;
}

.policy-confirm-btn {
  min-width: 120px;
  height: 42px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(135deg, var(--mall-primary, #4A6B7C) 0%, var(--mall-primary-end, #5A8A9A) 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px color-mix(in srgb, var(--mall-primary, #4A6B7C) 20%, transparent);
  position: relative;
  overflow: hidden;
}

.policy-confirm-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.policy-confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px color-mix(in srgb, var(--mall-primary, #4A6B7C) 30%, transparent);
}

.policy-confirm-btn:hover::before {
  left: 100%;
}

.policy-confirm-btn:active {
  transform: translateY(0);
}

@media (max-width: 600px) {
  .policy-header {
    padding: 20px 20px 16px;
  }

  .policy-footer {
    flex-direction: column;
    gap: 12px;
    padding: 16px 20px 20px;
  }

  .policy-confirm-btn {
    width: 100%;
  }
}
</style>

<!-- 全域樣式：覆蓋 Element Plus Dialog（teleport 到 body，scoped 無法觸及） -->
<style>
.policy-overlay {
  background: rgba(61, 43, 31, 0.45) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.policy-dialog.el-dialog {
  --el-dialog-bg-color: transparent;
  background: #FDFCFA;
  border: 1px solid rgba(224, 213, 200, 0.6);
  border-radius: 24px;
  box-shadow:
    0 24px 80px rgba(0, 0, 0, 0.14),
    0 8px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.policy-dialog .el-dialog__header {
  padding: 0;
  margin: 0;
}

.policy-dialog .el-dialog__body {
  padding: 0 28px;
}

.policy-dialog .el-dialog__footer {
  padding: 0;
}

@media (max-width: 600px) {
  .policy-dialog.el-dialog {
    width: calc(100vw - 32px) !important;
    margin: 16px;
    border-radius: 20px;
  }

  .policy-dialog .el-dialog__body {
    padding: 0 20px;
  }
}
</style>
