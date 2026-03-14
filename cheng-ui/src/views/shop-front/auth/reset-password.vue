<template>
  <div class="reset-page">
    <div class="reset-shell">
      <section class="form-panel">
        <div class="form-surface">

          <!-- 載入中 -->
          <template v-if="pageState === 'loading'">
            <div class="loading-panel">
              <el-icon :size="40" class="loading-spinner"><Loading /></el-icon>
              <p>正在驗證重設連結...</p>
            </div>
          </template>

          <!-- 連結無效或已過期 -->
          <template v-if="pageState === 'invalid'">
            <div class="error-panel">
              <div class="error-icon">
                <el-icon :size="56" color="#C53030"><CircleCloseFilled /></el-icon>
              </div>
              <h2>{{ errorTitle }}</h2>
              <p>{{ errorMessage }}</p>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                @click="goToForgotPassword"
              >
                重新申請重設連結
              </el-button>
            </div>
          </template>

          <!-- 重設密碼表單 -->
          <template v-if="pageState === 'form'">
            <header class="form-header">
              <span class="form-badge">重設密碼</span>
              <h2>設定新密碼</h2>
              <p>請輸入你的新密碼，密碼需為 <strong>{{ passwordPolicy.minLength }}-{{ passwordPolicy.maxLength }} 字元</strong>。</p>
            </header>

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              class="reset-form"
              label-position="top"
            >
              <el-form-item label="新密碼" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  size="large"
                  :placeholder="`請輸入新密碼（${passwordPolicy.minLength}-${passwordPolicy.maxLength} 字元）`"
                  :prefix-icon="Lock"
                  show-password
                  autocomplete="new-password"
                  @input="checkPasswordStrength"
                />
              </el-form-item>

              <!-- 密碼強度指示器 -->
              <div class="strength-bar">
                <div class="strength-track">
                  <div
                    v-for="i in 4"
                    :key="i"
                    class="strength-segment"
                    :class="{ active: i <= strengthLevel }"
                    :style="{ background: i <= strengthLevel ? strengthColor : '' }"
                  />
                </div>
                <span v-if="strengthLevel > 0" class="strength-label" :style="{ color: strengthColor }">
                  {{ strengthText }}
                </span>
              </div>

              <!-- 複雜度需求清單 -->
              <ul v-if="passwordForm.newPassword && hasComplexityRules" class="complexity-list">
                <li v-if="passwordPolicy.requireUppercase" :class="{ met: /[A-Z]/.test(passwordForm.newPassword) }">
                  {{ /[A-Z]/.test(passwordForm.newPassword) ? '✓' : '✗' }} 包含大寫英文字母
                </li>
                <li v-if="passwordPolicy.requireLowercase" :class="{ met: /[a-z]/.test(passwordForm.newPassword) }">
                  {{ /[a-z]/.test(passwordForm.newPassword) ? '✓' : '✗' }} 包含小寫英文字母
                </li>
                <li v-if="passwordPolicy.requireDigit" :class="{ met: /\d/.test(passwordForm.newPassword) }">
                  {{ /\d/.test(passwordForm.newPassword) ? '✓' : '✗' }} 包含數字
                </li>
                <li v-if="passwordPolicy.requireSpecial" :class="{ met: /[^a-zA-Z0-9]/.test(passwordForm.newPassword) }">
                  {{ /[^a-zA-Z0-9]/.test(passwordForm.newPassword) ? '✓' : '✗' }} 包含特殊符號
                </li>
              </ul>

              <el-form-item label="確認新密碼" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  size="large"
                  placeholder="請再次輸入新密碼"
                  :prefix-icon="Lock"
                  show-password
                  autocomplete="new-password"
                  @keyup.enter="handleResetPassword"
                />
              </el-form-item>

              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleResetPassword"
              >
                {{ loading ? '重設中...' : '確認重設密碼' }}
              </el-button>
            </el-form>
          </template>

          <!-- 重設成功 -->
          <template v-if="pageState === 'success'">
            <div class="success-panel">
              <div class="success-icon">
                <el-icon :size="56" color="#48BB78"><CircleCheckFilled /></el-icon>
              </div>
              <h2>密碼重設成功</h2>
              <p>你的密碼已成功更新。</p>
              <p>系統將在 <strong>{{ redirectCountdown }}</strong> 秒後自動導向登入頁面。</p>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                @click="goToLogin"
              >
                立即前往登入
              </el-button>
            </div>
          </template>

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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Lock,
  Loading,
  CircleCheckFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import { validateResetToken, resetPassword, getPasswordResetPolicy } from '@/api/shop/auth'

const route = useRoute()
const router = useRouter()

// ===== 頁面狀態 =====
const pageState = ref('loading') // loading | invalid | form | success
const loading = ref(false)
const errorTitle = ref('連結無效')
const errorMessage = ref('此重設連結已過期或無效，請重新申請。')

// ===== URL 參數 =====
const selector = ref('')
const token = ref('')
const minPasswordLength = ref(8)

// 密碼政策
const passwordPolicy = ref({
  minLength: 8,
  maxLength: 50,
  requireUppercase: false,
  requireLowercase: false,
  requireDigit: false,
  requireSpecial: false
})

const hasComplexityRules = computed(() => {
  const p = passwordPolicy.value
  return p.requireUppercase || p.requireLowercase || p.requireDigit || p.requireSpecial
})

// ===== 密碼表單 =====
const passwordFormRef = ref(null)
const passwordForm = ref({
  newPassword: '',
  confirmPassword: ''
})

// 密碼強度
const strengthLevel = ref(0)
const strengthText = ref('')
const strengthColor = ref('')

// 重設成功後倒計時
const redirectCountdown = ref(5)
let redirectTimer = null

// ===== 表單規則 =====
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('兩次密碼輸入不一致'))
  } else {
    callback()
  }
}

const validatePasswordLength = (rule, value, callback) => {
  const p = passwordPolicy.value
  if (!value) {
    callback(new Error('請輸入新密碼'))
    return
  }
  if (value.length < p.minLength || value.length > p.maxLength) {
    callback(new Error(`密碼長度須為 ${p.minLength}-${p.maxLength} 字元`))
    return
  }
  if (p.requireUppercase && !/[A-Z]/.test(value)) {
    callback(new Error('密碼需包含至少一個大寫英文字母'))
    return
  }
  if (p.requireLowercase && !/[a-z]/.test(value)) {
    callback(new Error('密碼需包含至少一個小寫英文字母'))
    return
  }
  if (p.requireDigit && !/\d/.test(value)) {
    callback(new Error('密碼需包含至少一個數字'))
    return
  }
  if (p.requireSpecial && !/[^a-zA-Z0-9]/.test(value)) {
    callback(new Error('密碼需包含至少一個特殊符號'))
    return
  }
  callback()
}

const passwordRules = {
  newPassword: [
    { required: true, message: '請輸入新密碼', trigger: 'blur' },
    { validator: validatePasswordLength, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '請再次輸入新密碼', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// ===== 密碼強度檢測 =====
function checkPasswordStrength() {
  const pwd = passwordForm.value.newPassword
  if (!pwd) {
    strengthLevel.value = 0
    strengthText.value = ''
    strengthColor.value = ''
    return
  }

  let score = 0
  if (pwd.length >= minPasswordLength.value) score++
  if (pwd.length >= (minPasswordLength.value + 4)) score++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
  if (/\d/.test(pwd)) score++
  if (/[^a-zA-Z0-9]/.test(pwd)) score++

  if (score <= 1) {
    strengthLevel.value = 1
    strengthText.value = '弱'
    strengthColor.value = '#E53E3E'
  } else if (score <= 2) {
    strengthLevel.value = 2
    strengthText.value = '普通'
    strengthColor.value = '#DD6B20'
  } else if (score <= 3) {
    strengthLevel.value = 3
    strengthText.value = '中等'
    strengthColor.value = '#D69E2E'
  } else {
    strengthLevel.value = 4
    strengthText.value = '強'
    strengthColor.value = '#38A169'
  }
}

// ===== 驗證連結有效性 =====
async function validateLink() {
  const hashParams = new URLSearchParams((route.hash || '').replace(/^#/, ''))
  const hashSelector = hashParams.get('selector') || ''
  const hashToken = hashParams.get('token') || ''
  const querySelector = route.query.selector ? String(route.query.selector) : ''
  const queryToken = route.query.token ? String(route.query.token) : ''

  selector.value = hashSelector || querySelector
  token.value = hashToken || queryToken

  if (!selector.value || !token.value) {
    errorTitle.value = '連結無效'
    errorMessage.value = '重設連結缺少必要參數，請從信箱重新點擊連結。'
    pageState.value = 'invalid'
    return
  }

  clearSensitiveParamsFromAddressBar()

  try {
    await validateResetToken({
      selector: selector.value,
      token: token.value
    })
    pageState.value = 'form'
  } catch (error) {
    const msg = error?.message || ''
    if (isInvalidTokenMessage(msg)) {
      errorTitle.value = '連結無效或已過期'
      errorMessage.value = '此重設連結已過期、已使用或無效，請重新申請重設連結。'
    } else {
      errorTitle.value = '無法驗證連結'
      errorMessage.value = '目前無法驗證重設連結，請稍後再試或重新申請。'
    }
    pageState.value = 'invalid'
  }
}

// ===== 重設密碼 =====
async function handleResetPassword() {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await resetPassword({
      selector: selector.value,
      token: token.value,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密碼重設成功')
    pageState.value = 'success'
    startRedirectCountdown()
  } catch (error) {
    const msg = error?.message || ''
    if (isInvalidTokenMessage(msg)) {
      errorTitle.value = '重設失敗'
      errorMessage.value = '密碼重設失敗，連結可能已過期或已使用，請重新申請。'
      pageState.value = 'invalid'
    } else {
      ElMessage.error(msg || '密碼重設失敗，請稍後再試')
      pageState.value = 'form'
    }
  } finally {
    loading.value = false
  }
}

// ===== 成功後倒計時跳轉 =====
function startRedirectCountdown() {
  redirectCountdown.value = 5
  redirectTimer = setInterval(() => {
    redirectCountdown.value--
    if (redirectCountdown.value <= 0) {
      clearInterval(redirectTimer)
      goToLogin()
    }
  }, 1000)
}

function goToLogin() {
  router.push('/login')
}

function goToForgotPassword() {
  router.push('/forgot-password')
}

function clearSensitiveParamsFromAddressBar() {
  const cleanPath = route.path || '/reset-password'
  window.history.replaceState({}, document.title, cleanPath)
}

function isInvalidTokenMessage(message) {
  return /連結|token|無效|過期|已使用|失效/.test(message || '')
}

async function loadPasswordResetPolicy() {
  try {
    const res = await getPasswordResetPolicy()
    const data = res?.data || {}
    // 優先讀取新欄位 minLength，向後相容 minPasswordLength
    const minLen = Number(data.minLength ?? data.minPasswordLength)
    if (Number.isFinite(minLen) && minLen > 0) {
      minPasswordLength.value = minLen
    }
    passwordPolicy.value = {
      minLength: minPasswordLength.value,
      maxLength: Number(data.maxLength) || 50,
      requireUppercase: !!data.requireUppercase,
      requireLowercase: !!data.requireLowercase,
      requireDigit: !!data.requireDigit,
      requireSpecial: !!data.requireSpecial
    }
  } catch {
    // 使用預設值
  }
}

onMounted(() => {
  loadPasswordResetPolicy()
  validateLink()
})

onUnmounted(() => {
  if (redirectTimer) {
    clearInterval(redirectTimer)
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&family=Noto+Sans+Symbols+2&family=Noto+Color+Emoji&display=swap');

.reset-page {
  display: flex;
  justify-content: center;
  padding: 40px 24px;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
}

.reset-shell {
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

.reset-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.reset-form :deep(.el-form-item__label) {
  color: #5A4A3C;
  font-weight: 700;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.reset-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
}

.reset-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A98A inset;
}

.reset-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 107, 124, 0.35) inset;
}

/* 密碼強度指示器 */
.strength-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: -8px 0 16px;
}

.strength-track {
  display: flex;
  gap: 4px;
  flex: 1;
}

.strength-segment {
  height: 4px;
  flex: 1;
  border-radius: 2px;
  background: #E0D5C8;
  transition: background 0.3s ease;
}

.strength-label {
  font-size: 12px;
  font-weight: 700;
  min-width: 32px;
  text-align: right;
}

/* 複雜度需求清單 */
.complexity-list {
  list-style: none;
  padding: 0;
  margin: -2px 0 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px 14px;
  font-size: 12px;
  color: #b0a090;
}

.complexity-list li {
  transition: color 0.2s ease;
}

.complexity-list li.met {
  color: #38A169;
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
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  color: #3D2B1F;
}

.error-panel p {
  margin: 0 0 20px;
  color: #7A6B5D;
  font-size: 14px;
  line-height: 1.6;
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

/* RWD */
@media (max-width: 768px) {
  .reset-page {
    padding: 16px 12px;
  }

  .form-panel {
    padding: 28px 20px;
    border-radius: 16px;
  }
}

@media (max-width: 480px) {
  .reset-page {
    padding: 8px;
  }

  .form-panel {
    padding: 20px 14px;
    border-radius: 14px;
  }

  .form-header h2 {
    font-size: 22px;
  }
}
</style>
