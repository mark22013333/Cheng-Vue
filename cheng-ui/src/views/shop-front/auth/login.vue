<template>
  <div class="login-page">
    <div class="login-shell">
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

            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe">記住我</el-checkbox>
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

    <el-dialog
      v-model="policyVisible"
      :title="policyType === 'terms' ? '服務條款' : '隱私政策'"
      width="600px"
      top="8vh"
      class="policy-dialog"
      destroy-on-close
    >
      <div class="policy-content">
        <template v-if="policyType === 'terms'">
          <h3>CoolApps 服務條款</h3>
          <p>歡迎使用 CoolApps 電商平台。使用本服務前，請仔細閱讀以下條款。</p>
          <h4>一、服務範圍</h4>
          <p>CoolApps 提供線上商品瀏覽、購買、訂單管理等電子商務服務。我們保留隨時修改或中止服務的權利。</p>
          <h4>二、帳號管理</h4>
          <p>您有責任妥善保管帳號及密碼，因帳號或密碼外洩所產生的一切損失由用戶自行承擔。</p>
          <h4>三、購物與付款</h4>
          <p>商品價格以下單時頁面顯示為準。付款成功後，系統將寄送訂單確認通知。如有退換貨需求，請參閱退換貨政策。</p>
          <h4>四、智慧財產權</h4>
          <p>本平台所有內容（包括但不限於文字、圖片、商標）均受智慧財產權法律保護，未經授權不得使用。</p>
          <h4>五、免責聲明</h4>
          <p>本平台對因不可抗力、系統維護或第三方服務中斷導致的損失不承擔責任。</p>
        </template>
        <template v-else>
          <h3>CoolApps 隱私政策</h3>
          <p>我們重視您的隱私權，本政策說明我們如何蒐集、使用及保護您的個人資料。</p>
          <h4>一、資料蒐集</h4>
          <p>我們蒐集的資料包括：姓名、Email、手機號碼、收件地址及購物紀錄等，用於提供服務與改善體驗。</p>
          <h4>二、資料使用</h4>
          <p>您的資料僅用於訂單處理、客戶服務、行銷通知（經同意後）及平台改善分析。</p>
          <h4>三、資料保護</h4>
          <p>我們採用業界標準的加密技術與安全措施保護您的個人資料，防止未經授權的存取。</p>
          <h4>四、Cookie 使用</h4>
          <p>本平台使用 Cookie 記錄您的偏好設定與登入狀態，以提供更好的瀏覽體驗。</p>
          <h4>五、您的權利</h4>
          <p>您可以隨時要求查閱、更正或刪除您的個人資料，請透過客服聯繫我們。</p>
        </template>
      </div>
      <template #footer>
        <el-button type="primary" @click="policyVisible = false">我知道了</el-button>
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
import { getOAuthAuthorizeUrl } from '@/api/shop/auth'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()
const cartStore = useCartStore()

const loginFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const policyVisible = ref(false)
const policyType = ref('terms')

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
      password: loginForm.value.password
    })

    try {
      await cartStore.mergeGuestCartOnLogin()
    } catch (e) {
      console.error('合併購物車失敗', e)
    }

    ElMessage.success('登入成功')
    await router.push(redirect.value)
  } catch (error) {
    // 登入失敗，不需額外處理
  } finally {
    loading.value = false
  }
}

async function handleLineLogin() {
  try {
    // 組裝 OAuth 回調 URI（前端 /oauth/callback 頁面）
    const redirectUri = `${window.location.origin}/oauth/callback`

    // 暫存必要資訊到 sessionStorage（供 oauth-callback.vue 使用）
    sessionStorage.setItem('oauth_provider', 'LINE')
    sessionStorage.setItem('oauth_redirect', redirect.value)
    sessionStorage.setItem('oauth_redirect_uri', redirectUri)

    // 呼叫後端取得 LINE 授權 URL
    const res = await getOAuthAuthorizeUrl('LINE', redirectUri)
    const authorizeUrl = res.data || res.authorizeUrl || res

    if (!authorizeUrl) {
      ElMessage.error('無法取得授權連結，請稍後再試')
      return
    }

    // 跳轉到 LINE 授權頁面
    window.location.href = authorizeUrl
  } catch (error) {
    ElMessage.error(error?.msg || '取得 LINE 登入連結失敗')
  }
}

onMounted(() => {
  getCookie()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&display=swap');

.login-page {
  display: flex;
  justify-content: center;
  padding: 40px 24px;
  font-family: 'Noto Sans TC', sans-serif;
}

.login-shell {
  width: 100%;
  max-width: 480px;
}

.form-panel {
  padding: 40px 36px;
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid var(--mall-border-color, #E8E4DF);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}

.form-surface {
  width: 100%;
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
  background: rgba(165, 99, 92, 0.12);
  border: 1px solid rgba(165, 99, 92, 0.25);
  color: #8B4A42;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.form-header h2 {
  margin: 12px 0 8px;
  font-size: 32px;
  font-family: 'Noto Serif TC', serif;
  color: #3D2B1F;
  line-height: 1.14;
}

.form-header p {
  margin: 0;
  color: #7A6B5D;
  font-size: 14px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.login-form :deep(.el-form-item__label) {
  color: #5A4A3C;
  font-weight: 700;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A98A inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 107, 124, 0.35) inset;
}

.password-toggle {
  cursor: pointer;
  color: #A89585;
}

.password-toggle:hover {
  color: #4A6B7C;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.form-options :deep(.el-checkbox__label) {
  color: #7A6B5D;
}

.forgot-link {
  color: #4A6B7C;
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
  background: linear-gradient(125deg, #4A6B7C, #5A8A9A);
  box-shadow: 0 14px 22px rgba(74, 107, 124, 0.2);
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
  box-shadow: 0 16px 26px rgba(74, 107, 124, 0.28);
}

.submit-btn:focus-visible {
  outline: 2px solid rgba(74, 107, 124, 0.5);
  outline-offset: 2px;
}

.form-footer {
  margin-top: 16px;
  text-align: center;
  color: #7A6B5D;
  font-size: 14px;
}

.form-footer a {
  color: #4A6B7C;
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
  color: #9A8B7D;
  font-size: 12px;
}

.inline-link {
  color: #4A6B7C;
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
  color: #A89585;
  font-size: 12px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #E0D5C8;
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

@media (max-width: 768px) {
  .login-page {
    padding: 16px 12px;
  }

  .form-panel {
    padding: 28px 20px;
    border-radius: 16px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 8px;
  }

  .form-panel {
    padding: 20px 14px;
    border-radius: 14px;
  }

  .form-header h2 {
    font-size: 24px;
  }
}

.policy-content {
  max-height: 60vh;
  overflow-y: auto;
  line-height: 1.8;
  color: #5A4A3C;
  font-size: 14px;
}

.policy-content h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #3D2B1F;
}

.policy-content h4 {
  margin: 16px 0 6px;
  font-size: 15px;
  color: #4A6B7C;
}

.policy-content p {
  margin: 0 0 8px;
}
</style>
