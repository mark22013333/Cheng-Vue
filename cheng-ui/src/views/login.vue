<template>
  <div class="login">
    <!-- 動態背景氣泡 -->
    <div class="background-bubbles">
      <div class="bubble" v-for="i in 8" :key="i" :style="getBubbleStyle(i)"></div>
    </div>

    <!-- 登入卡片 -->
    <div class="login-container">
      <div class="login-card">
        <!-- 標題區塊 -->
        <div class="login-header">
          <div class="logo-container">
            <div class="logo-circle">
              <i class="el-icon-s-home"></i>
            </div>
          </div>
          <h2 class="title">{{ title }}</h2>
          <p class="subtitle">歡迎回來，請登入您的帳號</p>
        </div>

        <!-- 表單區塊 -->
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
          <!-- 帳號輸入 -->
          <el-form-item prop="username">
            <div class="input-wrapper">
              <div class="input-icon-prefix">
                <i class="el-icon-user"></i>
              </div>
              <el-input
                v-model="loginForm.username"
                type="text"
                auto-complete="off"
                placeholder="請輸入帳號"
                class="custom-input"
              />
            </div>
          </el-form-item>

          <!-- 密碼輸入 -->
          <el-form-item prop="password">
            <div class="input-wrapper">
              <div class="input-icon-prefix">
                <i class="el-icon-lock"></i>
              </div>
              <el-input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                auto-complete="off"
                placeholder="請輸入密碼"
                class="custom-input"
                @keyup.enter.native="handleLogin"
              />
              <div class="input-icon-suffix" @click="togglePassword">
                <i :class="showPassword ? 'el-icon-view' : 'el-icon-lock'" :title="showPassword ? '隱藏密碼' : '顯示密碼'"></i>
              </div>
            </div>
          </el-form-item>

          <!-- 驗證碼輸入 -->
          <el-form-item prop="code" v-if="captchaEnabled">
            <div class="captcha-wrapper">
              <div class="input-wrapper captcha-input">
                <div class="input-icon-prefix">
                  <i class="el-icon-picture"></i>
                </div>
                <el-input
                  v-model="loginForm.code"
                  type="tel"
                  pattern="[0-9]*"
                  maxlength="4"
                  auto-complete="off"
                  placeholder="請輸入驗證碼"
                  class="custom-input"
                  @input="handleCodeInput"
                  @keyup.enter.native="handleLogin"
                />
              </div>
              <div class="captcha-image" @click="getCode">
                <img :src="codeUrl" alt="驗證碼"/>
                <div class="captcha-refresh">
                  <i class="el-icon-refresh"></i>
                </div>
              </div>
            </div>
          </el-form-item>

          <!-- 記住密碼 -->
          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe" class="custom-checkbox">
              記住密碼
            </el-checkbox>
            <router-link v-if="register" :to="'/register'" class="register-link">
              立即註冊
            </router-link>
          </div>

          <!-- 登入按鈕 -->
          <el-form-item style="margin-bottom: 0;">
            <el-button
              :loading="loading"
              type="primary"
              class="login-button"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">
                <i class="el-icon-right"></i>
                立即登入
              </span>
              <span v-else>
                驗證中...
              </span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 底部版權 -->
    <div class="el-login-footer">
      <span>Copyright © 2018-2025 Cool-Apps All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import {getCodeImg} from "@/api/login"
import Cookies from "js-cookie"
import {decrypt, encrypt} from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      showPassword: false,
      loginForm: {
        username: "admin",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          {required: true, trigger: "blur", message: "請輸入您的帳號"}
        ],
        password: [
          {required: true, trigger: "blur", message: "請輸入您的密碼"}
        ],
        code: [{required: true, trigger: "change", message: "請輸入驗證碼"}]
      },
      loading: false,
      // 驗證碼開關
      captchaEnabled: true,
      // 註冊開關
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword
    },
    handleCodeInput() {
      // 自動過濾非數字字元，並限制為4位數
      this.loginForm.code = this.loginForm.code.replace(/\D/g, '').slice(0, 4)
    },
    getBubbleStyle(index) {
      const colors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
        'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
      ]

      const sizes = [120, 100, 150, 80, 130, 90, 110, 140]
      const delays = [0, 2, 4, 6, 1, 3, 5, 7]
      const durations = [25, 20, 30, 22, 28, 24, 26, 21]

      return {
        width: sizes[index - 1] + 'px',
        height: sizes[index - 1] + 'px',
        background: colors[index - 1],
        left: (index * 12) + '%',
        animationDelay: delays[index - 1] + 's',
        animationDuration: durations[index - 1] + 's'
      }
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  position: relative;
}

/* 動態背景氣泡 */
.background-bubbles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.bubble {
  position: absolute;
  bottom: -150px;
  border-radius: 50%;
  opacity: 0.6;
  animation: rise infinite ease-in;
  filter: blur(2px);
}

@keyframes rise {
  0% {
    bottom: -150px;
    transform: translateX(0);
  }
  50% {
    transform: translateX(100px);
  }
  100% {
    bottom: 110%;
    transform: translateX(-100px);
  }
}

/* 登入容器 */
.login-container {
  z-index: 10;
  position: relative;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 玻璃擬態卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  padding: 48px 40px;
  width: 440px;
  max-width: 90vw;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 48px 0 rgba(31, 38, 135, 0.45);
  }
}

/* 標題區塊 */
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.logo-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  animation: pulse 2s ease-in-out infinite;

  i {
    font-size: 40px;
    color: #ffffff;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 12px 32px rgba(102, 126, 234, 0.6);
  }
}

.title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 0.5px;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 400;
}

/* 表單樣式 */
.login-form {
  ::v-deep .el-form-item {
    margin-bottom: 24px;
  }

  ::v-deep .el-form-item__error {
    color: #ff6b6b;
    font-weight: 500;
    background: rgba(255, 107, 107, 0.1);
    padding: 4px 8px;
    border-radius: 4px;
    margin-top: 8px;
  }
}

/* 輸入框包裝器 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 0 16px;
  transition: all 0.3s ease;
  border: 2px solid transparent;

  &:focus-within {
    background: rgba(255, 255, 255, 1);
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
    transform: translateY(-2px);
  }

  &:hover {
    background: rgba(255, 255, 255, 0.95);
  }
}

.input-icon-prefix {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #667eea;
  font-size: 20px;
  transition: all 0.3s ease;

  .input-wrapper:focus-within & {
    color: #764ba2;
    transform: scale(1.1);
  }
}

.input-icon-suffix {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 12px;
  color: #667eea;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 8px;

  &:hover {
    background: rgba(102, 126, 234, 0.1);
    color: #764ba2;
    transform: scale(1.1);
  }

  &:active {
    transform: scale(0.95);
  }
}

.custom-input {
  flex: 1;

  ::v-deep .el-input__inner {
    border: none;
    background: transparent;
    color: #333;
    font-size: 15px;
    height: 48px;
    line-height: 48px;
    padding: 0;
    font-weight: 500;

    &::placeholder {
      color: #999;
      font-weight: 400;
    }

    &:focus {
      background: transparent;
    }
  }
}

/* 驗證碼區域 */
.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  position: relative;
  width: 140px;
  height: 48px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.3);

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .captcha-refresh {
      opacity: 1;
    }
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .captcha-refresh {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;

    i {
      font-size: 24px;
      color: #fff;
      animation: rotate 1s linear;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 選項區域 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.custom-checkbox {
  ::v-deep .el-checkbox__label {
    color: rgba(255, 255, 255, 0.95);
    font-weight: 500;
    font-size: 14px;
  }

  ::v-deep .el-checkbox__inner {
    border-color: rgba(255, 255, 255, 0.6);
    background: rgba(255, 255, 255, 0.2);
  }

  ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: #667eea;
    border-color: #667eea;
  }

  &:hover ::v-deep .el-checkbox__inner {
    border-color: #667eea;
  }
}

.register-link {
  color: #ffffff;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 6px;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateX(-2px);
  }
}

/* 登入按鈕 */
.login-button {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);
    background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  }

  &:active {
    transform: translateY(0);
  }

  i {
    margin-right: 8px;
    font-size: 18px;
  }
}

/* 底部版權 */
.el-login-footer {
  position: fixed;
  bottom: 20px;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-family: Arial, sans-serif;
  font-size: 13px;
  letter-spacing: 1px;
  z-index: 100;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
  font-weight: 500;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .login-card {
    padding: 32px 24px;
    width: 90vw;
  }

  .title {
    font-size: 26px;
  }

  .logo-circle {
    width: 64px;
    height: 64px;

    i {
      font-size: 32px;
    }
  }

  .captcha-wrapper {
    flex-direction: column;

    .captcha-input {
      width: 100%;
    }

    .captcha-image {
      width: 100%;
      height: 56px;
    }
  }
}
</style>
