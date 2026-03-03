<template>
  <div class="verify-page">
    <div class="verify-shell">
      <section class="form-panel">
        <div class="form-surface">

          <!-- 載入中 -->
          <template v-if="pageState === 'loading'">
            <div class="loading-panel">
              <el-icon :size="40" class="loading-spinner"><Loading /></el-icon>
              <p>正在驗證您的 Email...</p>
            </div>
          </template>

          <!-- 驗證成功 -->
          <template v-if="pageState === 'success'">
            <div class="success-panel">
              <div class="success-icon">
                <el-icon :size="56" color="#48BB78"><CircleCheckFilled /></el-icon>
              </div>
              <h2>Email 驗證成功</h2>
              <p>歡迎加入 CoolApps！您的帳號已啟用。</p>
              <p>系統將在 <strong>{{ redirectCountdown }}</strong> 秒後自動導向首頁。</p>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                @click="goToHome"
              >
                立即開始購物
              </el-button>
            </div>
          </template>

          <!-- 驗證失敗 -->
          <template v-if="pageState === 'error'">
            <div class="error-panel">
              <div class="error-icon">
                <el-icon :size="56" color="#C53030"><CircleCloseFilled /></el-icon>
              </div>
              <h2>{{ errorTitle }}</h2>
              <p>{{ errorMessage }}</p>

              <!-- 重寄驗證信區塊 -->
              <div class="resend-section">
                <p class="resend-hint">輸入註冊時使用的 Email，重新發送驗證連結：</p>
                <el-form ref="resendFormRef" :model="resendForm" :rules="resendRules" class="resend-form" label-position="top">
                  <el-form-item label="電子信箱" prop="email">
                    <el-input
                      v-model="resendForm.email"
                      size="large"
                      placeholder="請輸入 Email"
                      :prefix-icon="Message"
                      autocomplete="email"
                    />
                  </el-form-item>

                  <el-form-item v-if="captchaEnabled" label="驗證碼" prop="code">
                    <div class="captcha-row">
                      <el-input
                        v-model="resendForm.code"
                        size="large"
                        placeholder="請輸入驗證碼"
                        :prefix-icon="Key"
                        autocomplete="one-time-code"
                        @keyup.enter="handleResend"
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

                  <el-button
                    type="primary"
                    size="large"
                    class="submit-btn"
                    :loading="resendLoading"
                    :disabled="resendCountdown > 0"
                    @click="handleResend"
                  >
                    {{ resendCountdown > 0 ? `${resendCountdown} 秒後可重新發送` : (resendLoading ? '發送中...' : '重新發送驗證信') }}
                  </el-button>
                </el-form>
              </div>
            </div>
          </template>

          <!-- 重寄成功 -->
          <template v-if="pageState === 'resent'">
            <div class="success-panel">
              <div class="success-icon">
                <el-icon :size="56" color="#4A6B7C"><Message /></el-icon>
              </div>
              <h2>驗證信已重新發送</h2>
              <p>若此信箱為已註冊帳號，驗證連結已寄出。</p>
              <p class="hint-text">
                請至信箱點擊驗證連結以完成驗證。<br />
                沒收到信？請檢查垃圾郵件資料夾。
              </p>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                @click="goToLogin"
              >
                前往登入頁面
              </el-button>
            </div>
          </template>

          <!-- 返回連結 -->
          <div class="back-to-login">
            <router-link to="/login">← 返回登入</router-link>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Message,
  Key,
  Refresh,
  Loading,
  CircleCheckFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import { verifyEmail, resendVerification } from '@/api/shop/auth'
import { getCodeImg } from '@/api/login'
import useMemberStore from '@/store/modules/member'
import { setMemberToken } from '@/utils/memberAuth'

const route = useRoute()
const router = useRouter()
const memberStore = useMemberStore()

// ===== 頁面狀態 =====
const pageState = ref('loading') // loading | success | error | resent
const errorTitle = ref('驗證失敗')
const errorMessage = ref('驗證連結已過期或無效，請重新發送驗證信。')

// ===== 成功後倒計時 =====
const redirectCountdown = ref(5)
let redirectTimer = null

// ===== 重寄驗證信 =====
const resendFormRef = ref(null)
const resendLoading = ref(false)
const resendCountdown = ref(0)
let resendTimer = null

const captchaEnabled = ref(true)
const codeUrl = ref('')

const resendForm = ref({
  email: '',
  code: '',
  uuid: ''
})

const resendRules = {
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: '請輸入正確的 Email 格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '請輸入驗證碼', trigger: 'change' }
  ]
}

// ===== 驗證碼 =====
async function getCode() {
  try {
    const res = await getCodeImg()
    captchaEnabled.value = res.captchaEnabled !== false
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      resendForm.value.uuid = res.uuid
    }
  } catch {
    // 驗證碼取得失敗時靜默處理
  }
}

// ===== 驗證 Email =====
async function doVerify() {
  const selector = route.query.selector || ''
  const token = route.query.token || ''

  if (!selector || !token) {
    errorTitle.value = '驗證連結無效'
    errorMessage.value = '驗證連結缺少必要參數，請從信箱重新點擊連結。'
    pageState.value = 'error'
    getCode()
    return
  }

  try {
    const res = await verifyEmail({ selector, token })

    // 驗證成功 → 自動登入
    if (res.token) {
      setMemberToken(res.token)
      memberStore.token = res.token
      if (res.member) {
        memberStore.setMember(res.member)
      }
    }

    pageState.value = 'success'
    startRedirectCountdown()
  } catch (error) {
    const msg = error?.msg || error?.message || ''
    if (msg.includes('已使用')) {
      errorTitle.value = '連結已使用'
      errorMessage.value = '此驗證連結已使用過。若您已完成驗證，請直接登入。'
    } else if (msg.includes('已過期')) {
      errorTitle.value = '連結已過期'
      errorMessage.value = '此驗證連結已過期，請重新發送驗證信。'
    } else {
      errorTitle.value = '驗證失敗'
      errorMessage.value = '驗證連結無效或已過期，請重新發送驗證信。'
    }
    pageState.value = 'error'
    getCode()
  }
}

// ===== 成功後倒計時跳轉 =====
function startRedirectCountdown() {
  redirectCountdown.value = 5
  redirectTimer = setInterval(() => {
    redirectCountdown.value--
    if (redirectCountdown.value <= 0) {
      clearInterval(redirectTimer)
      goToHome()
    }
  }, 1000)
}

// ===== 重寄驗證信 =====
function startResendCountdown() {
  resendCountdown.value = 60
  clearInterval(resendTimer)
  resendTimer = setInterval(() => {
    resendCountdown.value--
    if (resendCountdown.value <= 0) {
      clearInterval(resendTimer)
    }
  }, 1000)
}

async function handleResend() {
  if (!resendFormRef.value) return
  try {
    await resendFormRef.value.validate()
  } catch {
    return
  }

  resendLoading.value = true
  try {
    await resendVerification({
      email: resendForm.value.email.trim().toLowerCase(),
      code: resendForm.value.code,
      uuid: resendForm.value.uuid
    })
    ElMessage.success('驗證信已重新發送，請檢查您的信箱')
    startResendCountdown()
    pageState.value = 'resent'
  } catch {
    if (captchaEnabled.value) {
      getCode()
      resendForm.value.code = ''
    }
  } finally {
    resendLoading.value = false
  }
}

// ===== 導航 =====
function goToHome() {
  router.push('/')
}

function goToLogin() {
  router.push('/login')
}

onMounted(() => {
  doVerify()
})

onUnmounted(() => {
  if (redirectTimer) {
    clearInterval(redirectTimer)
  }
  if (resendTimer) {
    clearInterval(resendTimer)
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&display=swap');

.verify-page {
  display: flex;
  justify-content: center;
  padding: 40px 24px;
  font-family: 'Noto Sans TC', sans-serif;
}

.verify-shell {
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

/* 載入畫面 */
.loading-panel {
  text-align: center;
  padding: 40px 0;
}

.loading-spinner {
  color: #4A6B7C;
  animation: spin 1.2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-panel p {
  margin: 16px 0 0;
  color: #7A6B5D;
  font-size: 14px;
}

/* 成功畫面 */
.success-panel {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 20px;
}

.success-panel h2 {
  margin: 0 0 12px;
  font-size: 26px;
  font-family: 'Noto Serif TC', serif;
  color: #3D2B1F;
}

.success-panel p {
  margin: 0 0 4px;
  color: #7A6B5D;
  font-size: 14px;
  line-height: 1.6;
}

.success-panel strong {
  color: #4A6B7C;
  font-weight: 700;
}

.hint-text {
  color: #9A8B7D !important;
  font-size: 13px !important;
  margin-top: 12px !important;
  margin-bottom: 24px !important;
}

/* 錯誤畫面 */
.error-panel {
  text-align: center;
  padding: 20px 0;
}

.error-icon {
  margin-bottom: 20px;
}

.error-panel h2 {
  margin: 0 0 12px;
  font-size: 26px;
  font-family: 'Noto Serif TC', serif;
  color: #3D2B1F;
}

.error-panel > p {
  margin: 0 0 20px;
  color: #7A6B5D;
  font-size: 14px;
  line-height: 1.6;
}

/* 重寄區塊 */
.resend-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #E8E4DF;
  text-align: left;
}

.resend-hint {
  margin: 0 0 16px;
  color: #7A6B5D;
  font-size: 14px;
  text-align: center;
}

.resend-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.resend-form :deep(.el-form-item__label) {
  color: #5A4A3C;
  font-weight: 700;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.resend-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
}

.resend-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A98A inset;
}

.resend-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 107, 124, 0.35) inset;
}

/* 驗證碼 */
.captcha-row {
  display: grid;
  grid-template-columns: 1fr 130px;
  gap: 10px;
}

.captcha-btn {
  border: 1px solid #E0D5C8;
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
  background: rgba(60, 35, 20, 0.45);
  color: #fff5eb;
  font-size: 12px;
}

.captcha-mask em {
  font-style: normal;
}

.captcha-btn:hover .captcha-mask {
  opacity: 1;
}

/* 按鈕 */
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

/* 返回登入 */
.back-to-login {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
}

.back-to-login a {
  color: #7A6B5D;
  text-decoration: none;
  font-weight: 500;
}

.back-to-login a:hover {
  color: #4A6B7C;
  text-decoration: underline;
}

/* RWD */
@media (max-width: 768px) {
  .verify-page {
    padding: 16px 12px;
  }

  .form-panel {
    padding: 28px 20px;
    border-radius: 16px;
  }
}

@media (max-width: 480px) {
  .verify-page {
    padding: 8px;
  }

  .form-panel {
    padding: 20px 14px;
    border-radius: 14px;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .captcha-btn {
    min-height: 48px;
  }
}
</style>
