<template>
  <div class="login-page" @mousemove="handleMouseMove">
    <div class="parallax-layer layer-back" :style="layerBackStyle"></div>
    <div class="parallax-layer layer-mid" :style="layerMidStyle"></div>

    <div class="mouse-glow" :style="glowStyle"></div>

    <div class="login-container">
      <div class="login-card">
        <div class="card-header">
          <div class="logo-wrapper">
            <div class="logo-icon">
              <i class="el-icon-s-cooperation"></i>
            </div>
            <div class="logo-ring"></div>
          </div>
          <h2 class="app-title">{{ title }}</h2>
          <p class="app-subtitle">System Access Portal</p>
        </div>

        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">

          <el-form-item prop="username">
            <div class="input-group" :class="{ 'is-focused': focusedInput === 'username' }">
              <i class="el-icon-user prefix-icon"></i>
              <el-input
                v-model="loginForm.username"
                placeholder="使用者帳號 / User ID"
                @focus="focusedInput = 'username'"
                @blur="focusedInput = ''"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-group" :class="{ 'is-focused': focusedInput === 'password' }">
              <i class="el-icon-lock prefix-icon"></i>
              <el-input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="密碼 / Password"
                @focus="focusedInput = 'password'"
                @blur="focusedInput = ''"
                @keyup.enter.native="handleLogin"
              />
              <i
                class="suffix-icon"
                :class="showPassword ? 'el-icon-view' : 'el-icon-lock'"
                @click="togglePassword"
              ></i>
            </div>
          </el-form-item>

          <el-form-item prop="code" v-if="captchaEnabled">
            <div class="captcha-row">
              <div class="input-group captcha-input" :class="{ 'is-focused': focusedInput === 'code' }">
                <i class="el-icon-key prefix-icon"></i>
                <el-input
                  v-model="loginForm.code"
                  placeholder="驗證碼"
                  @focus="focusedInput = 'code'"
                  @blur="focusedInput = ''"
                  @keyup.enter.native="handleLogin"
                />
              </div>
              <div class="captcha-img-wrapper" @click="getCode">
                <img :src="codeUrl" alt="captcha" class="captcha-img"/>
                <div class="captcha-overlay">
                  <i class="el-icon-refresh"></i>
                </div>
              </div>
            </div>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">記住我</el-checkbox>
            <span class="forgot-pwd">忘記密碼?</span>
          </div>

          <el-button
            class="submit-btn"
            :loading="loading"
            @click.native.prevent="handleLogin"
          >
            {{ loading ? '驗證身分中...' : '登 入 系 統' }}
            <i class="el-icon-right" v-if="!loading"></i>
          </el-button>

          <div class="register-hint" v-if="register">
            尚未擁有帳號?
            <router-link to="/register">立即註冊</router-link>
          </div>
        </el-form>
      </div>
    </div>

    <div class="copyright">
      <span>Copyright © 2025 Cool-Apps Technology. All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import {getCodeImg} from "@/api/login";
import Cookies from "js-cookie";
import {decrypt, encrypt} from '@/utils/jsencrypt';

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE || "CoolApps Admin",
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [{required: true, trigger: "blur", message: "請輸入帳號"}],
        password: [{required: true, trigger: "blur", message: "請輸入密碼"}],
        code: [{required: true, trigger: "change", message: "請輸入驗證碼"}]
      },
      loading: false,
      showPassword: false,
      captchaEnabled: true,
      register: false,
      redirect: undefined,
      focusedInput: '', // 用於控制輸入框樣式

      // 視差效果參數
      mouseX: 0,
      mouseY: 0,
      winCenterX: window.innerWidth / 2,
      winCenterY: window.innerHeight / 2
    };
  },
  computed: {
    // 背景層移動 (較慢)
    layerBackStyle() {
      const x = (this.mouseX - this.winCenterX) * 0.02;
      const y = (this.mouseY - this.winCenterY) * 0.02;
      return {transform: `translate(${x}px, ${y}px)`};
    },
    // 中景層移動 (稍快)
    layerMidStyle() {
      const x = (this.mouseX - this.winCenterX) * 0.05;
      const y = (this.mouseY - this.winCenterY) * 0.05;
      return {transform: `translate(${x}px, ${y}px)`};
    },
    // 滑鼠光暈位置
    glowStyle() {
      return {
        left: `${this.mouseX}px`,
        top: `${this.mouseY}px`
      };
    }
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  mounted() {
    // 更新視窗中心點 (RWD)
    window.addEventListener('resize', () => {
      this.winCenterX = window.innerWidth / 2;
      this.winCenterY = window.innerHeight / 2;
    });
  },
  created() {
    this.getCode();
    this.getCookie();
  },
  methods: {
    handleMouseMove(e) {
      this.mouseX = e.clientX;
      this.mouseY = e.clientY;
    },
    togglePassword() {
      this.showPassword = !this.showPassword;
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe');
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, {expires: 30});
            Cookies.set("password", encrypt(this.loginForm.password), {expires: 30});
            Cookies.set('rememberMe', this.loginForm.rememberMe, {expires: 30});
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({path: this.redirect || "/"}).catch(() => {
            });
          }).catch(() => {
            this.loading = false;
            if (this.captchaEnabled) {
              this.getCode();
            }
          });
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
// --- 變數定義 ---
$bg-dark: #0f172a;
$primary: #667eea;
$accent: #764ba2;
$glow-color: #8f94fb;
$glass-bg: rgba(15, 23, 42, 0.6);
$glass-border: rgba(255, 255, 255, 0.1);
$text-light: #e2e8f0;

.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  // 與 Loading 頁面一致的深邃背景
  background: radial-gradient(circle at center, #2b32b2 0%, #0f172a 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Segoe UI', sans-serif;
}

// --- 視差背景層 (取代模糊氣泡) ---
.parallax-layer {
  position: absolute;
  top: -10%;
  left: -10%;
  width: 120%;
  height: 120%;
  pointer-events: none;
  transition: transform 0.1s linear; // 平滑跟隨
}

.layer-back {
  // 星塵 / 遠景粒子
  background-image: radial-gradient(2px 2px at 20px 30px, #fff, transparent),
  radial-gradient(2px 2px at 40px 70px, #fff, transparent),
  radial-gradient(2px 2px at 50px 160px, #fff, transparent),
  radial-gradient(2px 2px at 90px 40px, #fff, transparent),
  radial-gradient(2px 2px at 130px 80px, #fff, transparent);
  background-size: 200px 200px;
  opacity: 0.4;
}

.layer-mid {
  // 近景光斑 (較大，較亮)
  background-image: radial-gradient(circle at 50% 50%, rgba(102, 126, 234, 0.1) 0%, transparent 50%),
  radial-gradient(circle at 20% 80%, rgba(118, 75, 162, 0.15) 0%, transparent 40%);
  background-size: 100% 100%;
}

// --- 滑鼠光暈 ---
.mouse-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 1;
  mix-blend-mode: overlay;
  transition: opacity 0.3s;
}

// --- 卡片容器 ---
.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 20px;
  perspective: 1000px; // 3D 空間
}

.login-card {
  background: $glass-bg;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid $glass-border;
  border-top: 1px solid rgba(255, 255, 255, 0.2); // 頂部高光
  border-radius: 24px;
  padding: 40px 36px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4),
  0 0 0 1px rgba(255, 255, 255, 0.05);
  transform-style: preserve-3d;
  animation: floatCard 6s ease-in-out infinite;
}

@keyframes floatCard {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// --- Logo & Header ---
.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-wrapper {
  position: relative;
  width: 70px;
  height: 70px;
  margin: 0 auto 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-icon {
  font-size: 32px;
  color: #fff;
  z-index: 2;
  text-shadow: 0 0 15px $primary;
}

.logo-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid transparent;
  border-top-color: $primary;
  border-right-color: $accent;
  animation: spin 3s linear infinite;
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.3);
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.app-title {
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
  background: linear-gradient(to right, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.app-subtitle {
  color: #94a3b8;
  font-size: 13px;
  margin-top: 6px;
  letter-spacing: 2px;
  text-transform: uppercase;
}

// --- Form Styles ---
.input-group {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(30, 41, 59, 0.6); // 深色半透明
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 0 14px;
  transition: all 0.3s ease;
  height: 48px;

  &.is-focused {
    background: rgba(30, 41, 59, 0.9);
    border-color: $primary;
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);

    .prefix-icon, .suffix-icon {
      color: $primary;
      text-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
    }
  }

  .prefix-icon {
    font-size: 18px;
    color: #64748b;
    margin-right: 12px;
    transition: color 0.3s;
  }

  .suffix-icon {
    font-size: 16px;
    color: #64748b;
    cursor: pointer;
    padding: 5px;
    transition: color 0.3s;

    &:hover {
      color: #fff;
    }
  }

  // Element UI Input Override
  ::v-deep .el-input__inner {
    background: transparent;
    border: none;
    height: 100%;
    color: #fff;
    font-size: 15px;
    padding: 0;

    &::placeholder {
      color: #475569;
    }
  }
}

// --- Captcha ---
.captcha-row {
  display: flex;
  gap: 12px;

  .captcha-input {
    flex: 1;
  }

  .captcha-img-wrapper {
    position: relative;
    width: 120px;
    height: 48px;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid rgba(255, 255, 255, 0.1);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      opacity: 0.8;
      transition: opacity 0.3s;
    }

    .captcha-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;

      i {
        color: #fff;
        font-size: 20px;
      }
    }

    &:hover {
      img {
        opacity: 0.4;
      }

      .captcha-overlay {
        opacity: 1;
      }
    }
  }
}

// --- Options (Checkbox & Forgot Pwd) ---
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 4px;

  ::v-deep .el-checkbox__label {
    color: #94a3b8;
    font-size: 13px;
  }

  ::v-deep .el-checkbox__inner {
    background: transparent;
    border-color: #475569;
  }

  ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
    background: $primary;
    border-color: $primary;
  }

  .forgot-pwd {
    font-size: 13px;
    color: #94a3b8;
    cursor: pointer;
    transition: color 0.3s;

    &:hover {
      color: $primary;
      text-decoration: underline;
    }
  }
}

// --- Submit Button ---
.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, $primary 0%, $accent 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
    background: linear-gradient(135deg, lighten($primary, 5%) 0%, lighten($accent, 5%) 100%);
  }

  &:active {
    transform: translateY(0);
  }

  i {
    margin-left: 8px;
  }
}

.register-hint {
  text-align: center;
  margin-top: 20px;
  color: #64748b;
  font-size: 14px;

  a {
    color: $primary;
    text-decoration: none;
    font-weight: 600;
    margin-left: 5px;

    &:hover {
      text-decoration: underline;
    }
  }
}

// --- Footer ---
.copyright {
  position: absolute;
  bottom: 20px;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  letter-spacing: 1px;
}

// --- Mobile Adjustments ---
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
  }
  .login-card {
    padding: 30px 20px;
  }

  .parallax-layer, .mouse-glow {
    display: none; // 手機端為了效能，關閉複雜背景特效
  }

  .login-page {
    background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  }
}
</style>
