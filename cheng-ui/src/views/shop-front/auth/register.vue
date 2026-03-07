<template>
  <div class="register-page">
    <div class="register-shell">
      <section class="form-panel">
        <div class="form-surface">

          <!-- 步驟一：註冊表單 -->
          <template v-if="currentStep === 1">
            <header class="form-header">
              <span class="form-badge">建立帳號</span>
              <h2>加入 CoolApps</h2>
              <p>填寫以下資料完成註冊，我們將發送驗證信至您的信箱。</p>
            </header>

            <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              class="register-form"
              label-position="top"
            >
              <el-form-item label="電子信箱" prop="email" required>
                <el-input
                  v-model="registerForm.email"
                  size="large"
                  placeholder="name@example.com"
                  :prefix-icon="Message"
                  autocomplete="email"
                  inputmode="email"
                />
                <p class="field-hint">用於登入及接收訂單通知，請確保信箱可正常收信。</p>
              </el-form-item>

              <el-form-item label="密碼" prop="password" required>
                <el-input
                  v-model="registerForm.password"
                  size="large"
                  :type="showPassword ? 'text' : 'password'"
                  :placeholder="`請輸入 ${passwordPolicy.minLength}-${passwordPolicy.maxLength} 字元密碼`"
                  :prefix-icon="Lock"
                  autocomplete="new-password"
                  @input="checkPasswordStrength"
                >
                  <template #suffix>
                    <el-icon class="password-toggle" @click="showPassword = !showPassword">
                      <View v-if="showPassword" />
                      <Hide v-else />
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <!-- 密碼強度指示器 -->
              <div v-if="registerForm.password" class="strength-bar">
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
              <ul v-if="registerForm.password && hasComplexityRules" class="complexity-list">
                <li v-if="passwordPolicy.requireUppercase" :class="{ met: /[A-Z]/.test(registerForm.password) }">
                  {{ /[A-Z]/.test(registerForm.password) ? '✓' : '✗' }} 包含大寫英文字母
                </li>
                <li v-if="passwordPolicy.requireLowercase" :class="{ met: /[a-z]/.test(registerForm.password) }">
                  {{ /[a-z]/.test(registerForm.password) ? '✓' : '✗' }} 包含小寫英文字母
                </li>
                <li v-if="passwordPolicy.requireDigit" :class="{ met: /\d/.test(registerForm.password) }">
                  {{ /\d/.test(registerForm.password) ? '✓' : '✗' }} 包含數字
                </li>
                <li v-if="passwordPolicy.requireSpecial" :class="{ met: /[^a-zA-Z0-9]/.test(registerForm.password) }">
                  {{ /[^a-zA-Z0-9]/.test(registerForm.password) ? '✓' : '✗' }} 包含特殊符號
                </li>
              </ul>
              <p v-else-if="!registerForm.password" class="field-hint" style="margin-top: -6px;">建議使用大小寫英文、數字與特殊符號混合。</p>

              <el-form-item label="確認密碼" prop="confirmPassword" required>
                <el-input
                  v-model="registerForm.confirmPassword"
                  size="large"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  placeholder="請再次輸入密碼"
                  :prefix-icon="Lock"
                  autocomplete="new-password"
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

              <el-form-item v-if="captchaEnabled" label="驗證碼" prop="code" required>
                <div class="captcha-row">
                  <el-input
                    v-model="registerForm.code"
                    size="large"
                    placeholder="請輸入驗證碼"
                    :prefix-icon="Key"
                    autocomplete="one-time-code"
                    @keyup.enter="handleRegister"
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

              <el-form-item prop="agreement">
                <el-checkbox v-model="registerForm.agreement" class="agreement">
                  我已閱讀並同意
                  <a class="inline-link" href="javascript:;" @click.stop="openPolicy('terms')">服務條款</a>
                  與
                  <a class="inline-link" href="javascript:;" @click.stop="openPolicy('privacy')">隱私政策</a>
                </el-checkbox>
              </el-form-item>

              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleRegister"
              >
                {{ loading ? '註冊中...' : '註冊並發送驗證信' }}
              </el-button>

              <p class="form-footer">
                已經有帳號？
                <router-link :to="{ path: '/login', query: { redirect: redirect } }">
                  立即登入
                </router-link>
              </p>
            </el-form>
          </template>

          <!-- 步驟二：驗證信已發送 -->
          <template v-if="currentStep === 2">
            <div class="success-panel">
              <div class="success-icon">
                <el-icon :size="56" color="#4A6B7C"><Message /></el-icon>
              </div>
              <h2>驗證信已發送</h2>
              <p>我們已寄送驗證連結至</p>
              <p><strong>{{ maskedEmail }}</strong></p>
              <p class="hint-text">
                請至信箱點擊驗證連結以完成註冊。<br />
                沒收到信？請檢查垃圾郵件資料夾，或稍後重試。
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

          <!-- 步驟指示器 -->
          <div v-if="currentStep === 1" class="step-indicator">
            <div
              v-for="step in 2"
              :key="step"
              class="step-dot"
              :class="{ active: step === currentStep, done: step < currentStep }"
            />
          </div>
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
          <p>我們蒐集的資料包括：姓名、Email、收件地址及購物紀錄等，用於提供服務與改善體驗。</p>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Lock,
  View,
  Hide,
  Key,
  Refresh,
  Message
} from '@element-plus/icons-vue'
import { getCodeImg } from '@/api/login'
import { memberRegister, getPasswordPolicy } from '@/api/shop/auth'

const router = useRouter()
const route = useRoute()

const registerFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const captchaEnabled = ref(true)
const codeUrl = ref('')
const currentStep = ref(1)
const policyVisible = ref(false)
const policyType = ref('terms')

// 密碼政策
const passwordPolicy = ref({
  minLength: 8,
  maxLength: 50,
  requireUppercase: false,
  requireLowercase: false,
  requireDigit: false,
  requireSpecial: false
})

// 密碼強度
const strengthLevel = ref(0)
const strengthText = ref('')
const strengthColor = ref('')

// 是否有複雜度規則啟用
const hasComplexityRules = computed(() => {
  const p = passwordPolicy.value
  return p.requireUppercase || p.requireLowercase || p.requireDigit || p.requireSpecial
})

function openPolicy(type) {
  policyType.value = type
  policyVisible.value = true
}

const AUTH_PATHS = ['/register', '/login', '/forgot-password', '/reset-password']

const redirect = computed(() => {
  const r = route.query.redirect
  // 避免重導回認證頁面造成 redirect 累積
  if (r && AUTH_PATHS.some(p => r.startsWith(p))) {
    return '/'
  }
  return r || '/'
})

// 同一元件路由變化時（如 navbar 點擊「註冊」），重置為步驟一
watch(() => route.fullPath, () => {
  if (currentStep.value !== 1) {
    currentStep.value = 1
    registerForm.value = {
      email: '',
      password: '',
      confirmPassword: '',
      code: '',
      uuid: '',
      agreement: false
    }
    strengthLevel.value = 0
    strengthText.value = ''
    strengthColor.value = ''
    getCode()
  }
})

const registerForm = ref({
  email: '',
  password: '',
  confirmPassword: '',
  code: '',
  uuid: '',
  agreement: false
})

// 遮蔽 Email 顯示
const maskedEmail = computed(() => {
  const email = registerForm.value.email
  if (!email) return ''
  const [local, domain] = email.split('@')
  if (!domain) return email
  if (local.length <= 2) return email
  return local[0] + '***' + local[local.length - 1] + '@' + domain
})

// ===== 密碼強度檢測 =====
function checkPasswordStrength() {
  const pwd = registerForm.value.password
  if (!pwd) {
    strengthLevel.value = 0
    strengthText.value = ''
    strengthColor.value = ''
    return
  }

  const minLen = passwordPolicy.value.minLength
  let score = 0
  if (pwd.length >= minLen) score++
  if (pwd.length >= minLen + 4) score++
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

const validateEmail = (rule, value, callback) => {
  const email = (value || '').trim()
  if (!email) {
    callback(new Error('請輸入 Email'))
    return
  }
  const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
  if (!emailOk) {
    callback(new Error('請輸入正確的 Email 格式'))
    return
  }
  callback()
}

const validatePassword = (rule, value, callback) => {
  const p = passwordPolicy.value
  if (!value || value.length < p.minLength || value.length > p.maxLength) {
    callback(new Error(`密碼長度需在 ${p.minLength}-${p.maxLength} 字元之間`))
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

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('請同意服務條款與隱私政策'))
  } else {
    callback()
  }
}

const registerRules = {
  email: [
    { required: true, trigger: 'blur', message: '請輸入 Email' },
    { validator: validateEmail, trigger: 'blur' }
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

  loading.value = true
  try {
    await registerFormRef.value.validate()

    await memberRegister({
      email: registerForm.value.email.trim().toLowerCase(),
      password: registerForm.value.password,
      code: registerForm.value.code,
      uuid: registerForm.value.uuid
    })

    ElMessage.success('註冊成功，驗證信已發送至您的信箱')
    currentStep.value = 2
  } catch (error) {
    if (captchaEnabled.value) {
      getCode()
    }
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  router.push({ path: '/login', query: { redirect: redirect.value } })
}

onMounted(async () => {
  getCode()
  try {
    const { data } = await getPasswordPolicy()
    if (data) {
      passwordPolicy.value = {
        minLength: data.minLength ?? 8,
        maxLength: data.maxLength ?? 50,
        requireUppercase: !!data.requireUppercase,
        requireLowercase: !!data.requireLowercase,
        requireDigit: !!data.requireDigit,
        requireSpecial: !!data.requireSpecial
      }
    }
  } catch (e) {
    console.warn('載入密碼政策失敗，使用預設值', e)
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&display=swap');

.register-page {
  display: flex;
  justify-content: center;
  padding: 40px 24px;
  font-family: 'Noto Sans TC', sans-serif;
}

.register-shell {
  width: 100%;
  max-width: 520px;
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
  background: rgba(74, 107, 124, 0.12);
  border: 1px solid rgba(74, 107, 124, 0.25);
  color: #4A6B7C;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.form-header h2 {
  margin: 12px 0 8px;
  font-size: 28px;
  font-family: 'Noto Serif TC', serif;
  color: #3D2B1F;
  line-height: 1.2;
}

.form-header p {
  margin: 0;
  color: #7A6B5D;
  font-size: 14px;
  line-height: 1.6;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.register-form :deep(.el-form-item__label) {
  color: #5a4a3e;
  font-weight: 700;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.register-form :deep(.el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label-wrap > .el-form-item__label::before),
.register-form :deep(.el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label::before) {
  color: #E53E3E;
}

.register-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #E0D5C8 inset;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C4A882 inset;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 107, 124, 0.4) inset;
}

.password-toggle {
  cursor: pointer;
  color: #b0a090;
}

.password-toggle:hover {
  color: #4A6B7C;
}

.field-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #9a8b7d;
}

/* 密碼強度指示器 */
.strength-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: -4px 0 12px;
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

.agreement :deep(.el-checkbox__label) {
  color: #7a6b5d;
  line-height: 1.7;
}

.inline-link {
  color: #4A6B7C;
  text-decoration: none;
  font-weight: 700;
}

.inline-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  min-height: 50px;
  margin-top: 4px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(125deg, #4A6B7C, #5A8A9A);
  box-shadow: 0 14px 22px rgba(74, 107, 124, 0.24);
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
  box-shadow: 0 16px 26px rgba(74, 107, 124, 0.3);
}

.submit-btn:focus-visible {
  outline: 2px solid rgba(74, 107, 124, 0.5);
  outline-offset: 2px;
}

.form-footer {
  margin: 18px 0 0;
  text-align: center;
  color: #7a6b5d;
  font-size: 14px;
}

.form-footer a {
  color: #4A6B7C;
  text-decoration: none;
  font-weight: 700;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* 步驟指示器 */
.step-indicator {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
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

@media (max-width: 768px) {
  .register-page {
    padding: 16px 12px;
  }

  .form-panel {
    padding: 28px 20px;
    border-radius: 16px;
  }
}

@media (max-width: 480px) {
  .register-page {
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
