<template>
  <div class="register-page">
    <div class="motion-background" :style="motionBackgroundStyle" aria-hidden="true"></div>
    <div class="gradient-orb orb-one" aria-hidden="true"></div>
    <div class="gradient-orb orb-two" aria-hidden="true"></div>
    <div class="gradient-orb orb-three" aria-hidden="true"></div>

    <div class="register-shell">
      <aside class="hero-panel">
        <div class="hero-content">
          <div class="hero-logo">
            <img src="@/assets/logo/logo.png" alt="CoolApps" />
          </div>
          <p class="hero-kicker">COOLAPPS MEMBERSHIP</p>
          <h1 class="hero-title">加入藍海會員，打開更聰明的購物旅程</h1>
          <p class="hero-desc">
            註冊後即可享有會員專屬折扣、快速結帳、訂單進度通知與收藏同步，打造更流暢的電商體驗。
          </p>

          <div class="hero-grid">
            <article class="hero-card">
              <el-icon><Discount /></el-icon>
              <div>
                <h3>首購折扣</h3>
                <p>新會員首筆訂單立即享優惠。</p>
              </div>
            </article>
            <article class="hero-card">
              <el-icon><Present /></el-icon>
              <div>
                <h3>會員活動</h3>
                <p>節慶限定活動與回饋不漏接。</p>
              </div>
            </article>
            <article class="hero-card">
              <el-icon><Star /></el-icon>
              <div>
                <h3>收藏同步</h3>
                <p>喜愛商品與訂單狀態集中管理。</p>
              </div>
            </article>
          </div>

          <div class="promo-chip">
            <span>NEW MEMBER DEAL</span>
            <strong>首購享 NT$120 折抵</strong>
          </div>
        </div>
      </aside>

      <section class="form-panel">
        <div class="form-surface">
          <header class="form-header">
            <span class="form-badge">免費註冊</span>
            <h2>建立你的會員帳號</h2>
            <p>完成註冊後，立即啟用專屬購物禮遇。</p>
          </header>

          <div class="signup-steps">
            <span class="step active">1. 聯絡方式</span>
            <span class="step active">2. 密碼設定</span>
            <span class="step">3. 完成</span>
          </div>

          <div class="contact-switch" role="tablist" aria-label="註冊方式">
            <button
              type="button"
              class="switch-btn"
              :class="{ active: contactType === 'email' }"
              :aria-pressed="contactType === 'email'"
              @click="switchContactType('email')"
            >
              <span class="switch-main">
                <el-icon><Message /></el-icon>
                Email 註冊
              </span>
              <small>適合接收帳務通知</small>
            </button>
            <button
              type="button"
              class="switch-btn"
              :class="{ active: contactType === 'mobile' }"
              :aria-pressed="contactType === 'mobile'"
              @click="switchContactType('mobile')"
            >
              <span class="switch-main">
                <el-icon><Iphone /></el-icon>
                手機註冊
              </span>
              <small>適合快速登入驗證</small>
            </button>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
            label-position="top"
          >
            <el-form-item label="會員暱稱" prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                size="large"
                placeholder="請輸入暱稱"
                :prefix-icon="User"
                maxlength="20"
                autocomplete="nickname"
              />
            </el-form-item>

            <el-form-item v-if="contactType === 'email'" label="Email（必填）" prop="email">
              <el-input
                v-model="registerForm.email"
                size="large"
                placeholder="name@example.com"
                :prefix-icon="Message"
                autocomplete="email"
                inputmode="email"
              />
            </el-form-item>

            <el-form-item v-else label="手機號碼（必填）" prop="mobile">
              <el-input
                v-model="registerForm.mobile"
                size="large"
                placeholder="請輸入 8-15 碼手機號碼"
                :prefix-icon="Iphone"
                autocomplete="tel"
                inputmode="tel"
              />
            </el-form-item>

            <el-form-item v-if="contactType === 'email'" label="手機號碼（選填）" prop="mobile">
              <el-input
                v-model="registerForm.mobile"
                size="large"
                placeholder="可填寫手機接收物流通知"
                :prefix-icon="Iphone"
                autocomplete="tel"
                inputmode="tel"
              />
            </el-form-item>

            <el-form-item v-else label="Email（選填）" prop="email">
              <el-input
                v-model="registerForm.email"
                size="large"
                placeholder="可填寫 Email 接收促銷通知"
                :prefix-icon="Message"
                autocomplete="email"
                inputmode="email"
              />
            </el-form-item>

            <el-form-item label="密碼" prop="password">
              <el-input
                v-model="registerForm.password"
                size="large"
                :type="showPassword ? 'text' : 'password'"
                placeholder="請輸入 6-20 字元密碼"
                :prefix-icon="Lock"
                autocomplete="new-password"
              >
                <template #suffix>
                  <el-icon class="password-toggle" @click="showPassword = !showPassword">
                    <View v-if="showPassword" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
              <p class="field-hint">建議使用英數混合，提高帳號安全性。</p>
            </el-form-item>

            <el-form-item label="確認密碼" prop="confirmPassword">
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

            <el-form-item v-if="captchaEnabled" label="驗證碼" prop="code">
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
                <router-link class="inline-link" to="/mall/terms" target="_blank" rel="noopener noreferrer">
                  服務條款
                </router-link>
                與
                <router-link class="inline-link" to="/mall/privacy" target="_blank" rel="noopener noreferrer">
                  隱私政策
                </router-link>
              </el-checkbox>
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
            >
              {{ loading ? '註冊中...' : '立即註冊並開始購物' }}
            </el-button>

            <p class="form-footer">
              已經有帳號？
              <router-link :to="{ path: '/mall/login', query: { redirect: redirect } }">
                立即登入
              </router-link>
            </p>
          </el-form>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
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
  Iphone,
  Discount,
  Present,
  Star
} from '@element-plus/icons-vue'
import { getCodeImg } from '@/api/login'
import useMemberStore from '@/store/modules/member'
import { useCartStore } from '@/store/modules/cart'
import loginBackground from '@/assets/images/login-background.jpg'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()
const cartStore = useCartStore()

const registerFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const captchaEnabled = ref(true)
const codeUrl = ref('')
const contactType = ref('email')

const redirect = computed(() => route.query.redirect || '/mall')
const motionBackgroundStyle = computed(() => ({
  backgroundImage: `linear-gradient(160deg, rgba(6, 48, 104, 0.86), rgba(18, 97, 183, 0.7)), url(${loginBackground})`
}))

const registerForm = ref({
  nickname: '',
  mobile: '',
  email: '',
  password: '',
  confirmPassword: '',
  code: '',
  uuid: '',
  agreement: false
})

function switchContactType(type) {
  if (contactType.value === type) return
  contactType.value = type
  nextTick(() => {
    registerFormRef.value?.clearValidate(['mobile', 'email'])
  })
}

const validateMobile = (rule, value, callback) => {
  const mobile = (registerForm.value.mobile || '').trim()
  if (!mobile) {
    if (contactType.value === 'mobile') {
      callback(new Error('請輸入手機號碼'))
      return
    }
    callback()
    return
  }
  const mobileOk = /^[0-9]{8,15}$/.test(mobile)
  if (!mobileOk) {
    callback(new Error('請輸入正確的手機號碼'))
    return
  }
  callback()
}

const validateEmail = (rule, value, callback) => {
  const email = (registerForm.value.email || '').trim()
  if (!email) {
    if (contactType.value === 'email') {
      callback(new Error('請輸入 Email'))
      return
    }
    callback()
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
    callback(new Error('請同意服務條款與隱私政策'))
  } else {
    callback()
  }
}

const registerRules = {
  nickname: [
    { required: true, trigger: 'blur', message: '請輸入暱稱' },
    { min: 2, max: 20, trigger: 'blur', message: '暱稱長度需在 2-20 字元之間' }
  ],
  mobile: [
    { validator: validateMobile, trigger: 'blur' }
  ],
  email: [
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

    await memberStore.register({
      nickname: registerForm.value.nickname.trim(),
      mobile: (registerForm.value.mobile || '').trim(),
      email: (registerForm.value.email || '').trim(),
      password: registerForm.value.password,
      code: registerForm.value.code,
      uuid: registerForm.value.uuid
    })

    try {
      await cartStore.mergeGuestCartOnLogin()
    } catch (e) {
      console.error('合併購物車失敗', e)
    }

    ElMessage.success('註冊成功')
    await router.push(redirect.value)
  } catch (error) {
    if (captchaEnabled.value) {
      getCode()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getCode()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&display=swap');

.register-page {
  --rg-ink: #eaf4ff;
  --rg-primary: #3f93ff;
  --rg-secondary: #255fc3;
  --rg-accent: #79d0ff;
  --rg-panel: rgba(8, 23, 56, 0.72);
  --rg-form-bg: rgba(255, 255, 255, 0.95);
  --rg-line: #d6e6ff;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #041a45, #0a2d69 40%, #0f3f87);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  font-family: 'Noto Sans TC', sans-serif;
}

.motion-background {
  position: absolute;
  inset: auto -10% -18% -10%;
  height: 70%;
  background-size: cover;
  background-position: center;
  filter: saturate(1.15) brightness(0.9);
  transform-origin: center;
  animation: bgDrift 18s ease-in-out infinite alternate;
  opacity: 0.52;
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
  filter: blur(2px);
}

.orb-one {
  width: 360px;
  height: 360px;
  top: -120px;
  right: 12%;
  background: radial-gradient(circle, rgba(121, 208, 255, 0.8), rgba(121, 208, 255, 0));
  animation: orbFloat 9s ease-in-out infinite;
}

.orb-two {
  width: 420px;
  height: 420px;
  left: -150px;
  top: 28%;
  background: radial-gradient(circle, rgba(79, 145, 255, 0.65), rgba(79, 145, 255, 0));
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

.register-shell {
  width: min(1220px, 100%);
  min-height: 760px;
  display: grid;
  grid-template-columns: 52% 48%;
  border-radius: 32px;
  overflow: hidden;
  position: relative;
  z-index: 2;
  border: 1px solid rgba(166, 213, 255, 0.34);
  box-shadow: 0 28px 56px rgba(2, 8, 24, 0.48);
  backdrop-filter: blur(3px);
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
  color: var(--rg-ink);
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
  text-wrap: pretty;
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
  max-width: 510px;
  margin: 0 auto;
  border-radius: 24px;
  padding: 28px 28px 24px;
  background: var(--rg-form-bg);
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

.signup-steps {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.step {
  display: inline-flex;
  align-items: center;
  height: 26px;
  border-radius: 999px;
  border: 1px solid #cfe2fb;
  color: #6a7c9e;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 600;
}

.step.active {
  color: #1f5bbf;
  border-color: rgba(63, 147, 255, 0.42);
  background: rgba(111, 181, 255, 0.18);
}

.contact-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 18px;
}

.switch-btn {
  border: 1px solid #d4e6ff;
  border-radius: 14px;
  padding: 10px 12px;
  min-height: 64px;
  background: #f8fbff;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
  cursor: pointer;
  transition: all 0.22s ease;
}

.switch-main {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #365483;
}

.switch-btn small {
  color: #7288aa;
  font-size: 11px;
}

.switch-btn:hover {
  transform: translateY(-1px);
  border-color: #8bc2ff;
  box-shadow: 0 10px 16px rgba(41, 93, 171, 0.12);
}

.switch-btn.active {
  background: linear-gradient(130deg, rgba(121, 208, 255, 0.22), rgba(63, 147, 255, 0.16));
  border-color: rgba(63, 147, 255, 0.44);
}

.switch-btn.active .switch-main {
  color: #1f5bbf;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.register-form :deep(.el-form-item__label) {
  color: #2b4c7e;
  font-weight: 700;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.register-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #d7e7fb inset;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #9dc9ff inset;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(63, 147, 255, 0.4) inset;
}

.password-toggle {
  cursor: pointer;
  color: #83a3cd;
}

.password-toggle:hover {
  color: #2c68c7;
}

.field-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #6782ab;
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

.agreement :deep(.el-checkbox__label) {
  color: #4f6c95;
  line-height: 1.7;
}

.inline-link {
  color: #2165ce;
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
  margin: 18px 0 0;
  text-align: center;
  color: #5d759d;
  font-size: 14px;
}

.form-footer a {
  color: #2165ce;
  text-decoration: none;
  font-weight: 700;
}

.form-footer a:hover {
  text-decoration: underline;
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
  .register-page {
    padding: 18px;
  }

  .register-shell {
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
  .register-shell {
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
  .register-page {
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

  .contact-switch {
    grid-template-columns: 1fr;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .captcha-btn {
    min-height: 48px;
  }
}
</style>
