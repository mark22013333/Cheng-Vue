<template>
  <div class="forgot-page">
    <div class="forgot-shell">
      <section class="form-panel">
        <div class="form-surface">

          <!-- 步驟一：輸入 Email -->
          <template v-if="currentStep === 1">
            <header class="form-header">
              <span class="form-badge">忘記密碼</span>
              <h2>重設你的密碼</h2>
              <p>請輸入註冊時使用的 Email，我們將寄送重設連結給你。</p>
            </header>

            <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" class="forgot-form" label-position="top">
              <el-form-item label="電子信箱" prop="email">
                <el-input
                  v-model="emailForm.email"
                  size="large"
                  placeholder="請輸入 Email"
                  :prefix-icon="Message"
                  autocomplete="email"
                  @keyup.enter="handleSendLink"
                />
              </el-form-item>

              <el-form-item v-if="captchaEnabled" label="驗證碼" prop="code">
                <div class="captcha-row">
                  <el-input
                    v-model="emailForm.code"
                    size="large"
                    placeholder="請輸入驗證碼"
                    :prefix-icon="Key"
                    autocomplete="one-time-code"
                    @keyup.enter="handleSendLink"
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
                :loading="loading"
                :disabled="countdown > 0"
                @click="handleSendLink"
              >
                {{ countdown > 0 ? `${countdown} 秒後可重新發送` : (loading ? '發送中...' : '發送重設連結') }}
              </el-button>
            </el-form>
          </template>

          <!-- 步驟二：已發送確認 -->
          <template v-if="currentStep === 2">
            <div class="success-panel">
              <div class="success-icon">
                <el-icon :size="56" color="#4A6B7C"><Message /></el-icon>
              </div>
              <h2>重設連結已發送</h2>
              <p>若 <strong>{{ maskedEmail }}</strong> 為已註冊信箱，</p>
              <p>重設連結已寄至您的信箱，有效期限 {{ expireMinutes }} 分鐘。</p>
              <p class="hint-text">
                沒收到信？請檢查垃圾郵件資料夾，或稍後重試。
              </p>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :disabled="countdown > 0"
                @click="handleResend"
              >
                {{ countdown > 0 ? `${countdown} 秒後可重新發送` : '重新發送' }}
              </el-button>
            </div>
          </template>

          <!-- 步驟指示器 -->
          <div v-if="currentStep === 1" class="step-indicator">
            <div
              v-for="step in 2"
              :key="step"
              class="step-dot"
              :class="{ active: step === currentStep, done: step < currentStep }"
            />
          </div>

          <!-- 返回登入連結 -->
          <div class="back-to-login">
            <router-link to="/login">← 返回登入</router-link>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Message,
  Key,
  Refresh
} from '@element-plus/icons-vue'
import { forgotPassword, getPasswordResetPolicy } from '@/api/shop/auth'
import { getCodeImg } from '@/api/login'

const currentStep = ref(1)
const loading = ref(false)
const countdown = ref(0)

const captchaEnabled = ref(true)
const codeUrl = ref('')
const expireMinutes = ref(30)
const resendCooldownSeconds = ref(60)

let countdownTimer = null

// ===== Email 表單 =====
const emailFormRef = ref(null)
const emailForm = ref({ email: '', code: '', uuid: '' })
const emailRules = {
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: '請輸入正確的 Email 格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '請輸入驗證碼', trigger: 'change' }
  ]
}

// 遮蔽 Email 顯示
const maskedEmail = computed(() => {
  const email = emailForm.value.email
  if (!email) return ''
  const [local, domain] = email.split('@')
  if (local.length <= 2) return email
  return local[0] + '***' + local[local.length - 1] + '@' + domain
})

async function getCode() {
  try {
    const res = await getCodeImg()
    captchaEnabled.value = res.captchaEnabled !== false
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      emailForm.value.uuid = res.uuid
    }
  } catch {
    // 驗證碼取得失敗時靜默處理
  }
}

function startCountdown() {
  countdown.value = resendCooldownSeconds.value
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

async function handleSendLink() {
  if (!emailFormRef.value) return
  try {
    await emailFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await forgotPassword({
      email: emailForm.value.email,
      code: emailForm.value.code,
      uuid: emailForm.value.uuid
    })
    ElMessage.success('重設連結已發送，請檢查您的信箱')
    startCountdown()
    currentStep.value = 2
  } catch {
    // 錯誤時重新整理驗證碼
    if (captchaEnabled.value) {
      getCode()
      emailForm.value.code = ''
    }
  } finally {
    loading.value = false
  }
}

function handleResend() {
  // 回到步驟一讓使用者重新輸入圖形驗證碼
  currentStep.value = 1
  emailForm.value.code = ''
  getCode()
}

async function loadPasswordResetPolicy() {
  try {
    const res = await getPasswordResetPolicy()
    const data = res?.data || {}
    expireMinutes.value = Number(data.expireMinutes) > 0 ? Number(data.expireMinutes) : 30
    resendCooldownSeconds.value = Number(data.resendCooldownSeconds) > 0
      ? Number(data.resendCooldownSeconds) : 60
  } catch {
    // 使用預設值
  }
}

onMounted(() => {
  loadPasswordResetPolicy()
  getCode()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&family=Noto+Sans+Symbols+2&family=Noto+Color+Emoji&display=swap');

.forgot-page {
  display: flex;
  justify-content: center;
  padding: 40px 24px;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
}

.forgot-shell {
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
  margin-bottom: 24px;
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
  font-size: 28px;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  color: #3D2B1F;
  line-height: 1.2;
}

.form-header p {
  margin: 0;
  color: #7A6B5D;
  font-size: 14px;
  line-height: 1.6;
}

.form-header strong {
  color: #4A6B7C;
  font-weight: 700;
}

.forgot-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.forgot-form :deep(.el-form-item__label) {
  color: #5A4A3C;
  font-weight: 700;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.forgot-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
}

.forgot-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A98A inset;
}

.forgot-form :deep(.el-input__wrapper.is-focus) {
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

/* 步驟指示器 */
.step-indicator {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 28px;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #E0D5C8;
  transition: all 0.3s ease;
}

.step-dot.active {
  width: 24px;
  border-radius: 4px;
  background: #4A6B7C;
}

.step-dot.done {
  background: #A5C4B8;
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
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
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

/* RWD */
@media (max-width: 768px) {
  .forgot-page {
    padding: 16px 12px;
  }

  .form-panel {
    padding: 28px 20px;
    border-radius: 16px;
  }
}

@media (max-width: 480px) {
  .forgot-page {
    padding: 8px;
  }

  .form-panel {
    padding: 20px 14px;
    border-radius: 14px;
  }

  .form-header h2 {
    font-size: 22px;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .captcha-btn {
    min-height: 48px;
  }
}
</style>
