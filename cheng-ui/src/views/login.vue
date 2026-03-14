<template>
  <div class="login-page">

    <!-- ═══ 左側品牌面板 ═══ -->
    <div class="brand-panel" :class="timeClass">
      <div class="dot-grid"></div>
      <div class="ring ring-a"></div>
      <div class="ring ring-b"></div>
      <div class="ring ring-c"></div>

      <div class="brand-content">
        <!-- Wordmark -->
        <div class="brand-mark">
          <div class="brand-icon">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="brand-words">
            <span class="brand-name">{{ appName }}</span>
            <span class="brand-sub">Admin Console</span>
          </div>
        </div>

        <div class="brand-rule"></div>

        <!-- 時間感知問候 -->
        <div class="brand-hero">
          <p class="hero-greeting">{{ greeting }}</p>
          <p class="hero-date">{{ todayLabel }}</p>
        </div>

        <!-- 底部版本 -->
        <div class="brand-foot">v2.1.0 · CoolApps Technology</div>
      </div>
    </div>

    <!-- ═══ 右側表單面板 ═══ -->
    <div class="form-panel">
      <div class="form-inner">

        <div class="form-head">
          <h1 class="form-title">歡迎回來</h1>
          <p class="form-hint">請使用管理員帳號登入系統</p>
        </div>

        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="lform">

          <!-- 帳號 -->
          <el-form-item prop="username" class="lform-item">
            <div class="field-wrap">
              <label class="field-label">帳號</label>
              <div class="field-box" :class="{ focused: focusedInput === 'username' }">
                <el-icon class="f-icon"><User /></el-icon>
                <el-input
                  v-model="loginForm.username"
                  placeholder="請輸入管理員帳號"
                  @focus="focusedInput = 'username'"
                  @blur="focusedInput = ''"
                />
              </div>
            </div>
          </el-form-item>

          <!-- 密碼 -->
          <el-form-item prop="password" class="lform-item">
            <div class="field-wrap">
              <label class="field-label">密碼</label>
              <div class="field-box" :class="{ focused: focusedInput === 'password' }">
                <el-icon class="f-icon"><Lock /></el-icon>
                <el-input
                  v-model="loginForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="請輸入登入密碼"
                  @focus="focusedInput = 'password'"
                  @blur="focusedInput = ''"
                  @keyup.enter="handleLogin"
                />
                <el-icon class="f-toggle" @click="togglePassword">
                  <View v-if="showPassword" />
                  <Hide v-else />
                </el-icon>
              </div>
            </div>
          </el-form-item>

          <!-- 驗證碼 -->
          <el-form-item prop="code" v-if="captchaEnabled" class="lform-item">
            <div class="field-wrap">
              <label class="field-label">驗證碼</label>
              <div class="captcha-row">
                <div class="field-box captcha-box" :class="{ focused: focusedInput === 'code' }">
                  <el-icon class="f-icon"><Key /></el-icon>
                  <el-input
                    v-model="loginForm.code"
                    placeholder="請輸入圖形驗證碼"
                    inputmode="numeric"
                    :formatter="(value) => value.replace(/\D/g, '')"
                    :parser="(value) => value.replace(/\D/g, '')"
                    @focus="focusedInput = 'code'"
                    @blur="focusedInput = ''"
                    @keyup.enter="handleLogin"
                  />
                </div>
                <div class="captcha-img" @click="getCode">
                  <img :src="codeUrl" alt="驗證碼" />
                  <div class="captcha-mask">
                    <el-icon><Refresh /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>

          <!-- 記住我 -->
          <div class="form-opts">
            <el-checkbox v-model="loginForm.rememberMe">記住我</el-checkbox>
          </div>

          <!-- 登入按鈕 -->
          <el-button
            class="submit-btn"
            :loading="loading"
            @click.prevent="handleLogin"
          >
            <span>{{ loading ? '驗證身分中...' : '登入系統' }}</span>
            <el-icon v-if="!loading" class="btn-icon"><Right /></el-icon>
          </el-button>

          <div class="register-hint" v-if="register">
            尚未擁有帳號？
            <router-link to="/register">立即申請</router-link>
          </div>

        </el-form>

        <p class="form-copy">Copyright © 2025 Cool-Apps Technology. All Rights Reserved.</p>
      </div>
    </div>

  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { decrypt, encrypt } from '@/utils/jsencrypt';
import { Connection, User, Lock, View, Hide, Key, Refresh, Right } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

export default {
  name: "Login",
  components: { Connection, User, Lock, View, Hide, Key, Refresh, Right },
  data() {
    return {
      title: import.meta.env.VITE_APP_TITLE || "CoolApps Admin",
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [{ required: true, trigger: "blur", message: "請輸入帳號" }],
        password: [{ required: true, trigger: "blur", message: "請輸入密碼" }],
        code: [{ required: true, trigger: "change", message: "請輸入驗證碼" }]
      },
      loading: false,
      showPassword: false,
      captchaEnabled: true,
      register: false,
      redirect: undefined,
      focusedInput: ''
    };
  },
  computed: {
    appName() {
      return (this.title || 'CoolApps')
        .replace(/\s*[Aa]dmin.*$/, '')
        .trim() || 'CoolApps';
    },
    timeContext() {
      const h = new Date().getHours();
      if (h >= 5 && h < 12) return 'morning';
      if (h >= 12 && h < 18) return 'afternoon';
      return 'evening';
    },
    timeClass() {
      return `time-${this.timeContext}`;
    },
    greeting() {
      const map = { morning: '早安，管理員', afternoon: '午安，管理員', evening: '晚安，管理員' };
      return map[this.timeContext];
    },
    todayLabel() {
      const d = new Date();
      const days = ['日', '一', '二', '三', '四', '五', '六'];
      return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日 · 星期${days[d.getDay()]}`;
    }
  },
  watch: {
    $route: {
      handler(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
    this.getCookie();
  },
  methods: {
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
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          const userStore = useUserStore();
          userStore.login(this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/cadm/index" }).catch(() => {});
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

// ─────────────────────────────────────────────
// 設計變數
// ─────────────────────────────────────────────
$brand-bg   : #0f172a;
$form-bg    : #f0f4f8;
$white      : #ffffff;
$text-1     : #111827;
$text-2     : #6b7280;
$text-3     : #9ca3af;
$border     : rgba(0, 0, 0, 0.08);
$primary    : #409EFF;
$radius-lg  : 12px;
$radius-md  : 10px;
$radius-card: 20px;

// ─────────────────────────────────────────────
// 外層佈局
// ─────────────────────────────────────────────
.login-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-family: 'Inter', 'Avenir', 'Helvetica Neue', Arial, sans-serif;
}

// ─────────────────────────────────────────────
// 左側：品牌面板
// ─────────────────────────────────────────────
.brand-panel {
  position: relative;
  flex: 0 0 420px;
  background: $brand-bg;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 56px 48px;

  // 時間感知色調覆層（與儀表板 Hero 同語言）
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 1;
    transition: background 1s ease;
  }
  &.time-morning::before {
    background: radial-gradient(ellipse at 15% 85%, rgba(251, 191, 36, 0.08) 0%, transparent 55%);
  }
  &.time-afternoon::before {
    background: radial-gradient(ellipse at 15% 85%, rgba(59, 130, 246, 0.08) 0%, transparent 55%);
  }
  &.time-evening::before {
    background: radial-gradient(ellipse at 15% 85%, rgba(139, 92, 246, 0.08) 0%, transparent 55%);
  }
}

// 點格紋理
.dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255, 255, 255, 0.09) 1px, transparent 1px);
  background-size: 26px 26px;
  pointer-events: none;
}

// 同心環裝飾（右上角，CSS-only）
.ring {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.ring-a {
  width: 480px;
  height: 480px;
  top: -180px;
  right: -180px;
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.ring-b {
  width: 320px;
  height: 320px;
  top: -100px;
  right: -100px;
  border: 1px solid rgba(255, 255, 255, 0.055);
}
.ring-c {
  width: 180px;
  height: 180px;
  top: -30px;
  right: -30px;
  border: 1px solid rgba(255, 255, 255, 0.07);
}

// 內容
.brand-content {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-mark {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 52px;
}

.brand-icon {
  width: 44px;
  height: 44px;
  border-radius: $radius-lg;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  :deep(.el-icon) {
    font-size: 22px;
    color: rgba(255, 255, 255, 0.88);
  }
}

.brand-words {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.01em;
  line-height: 1.2;
}

.brand-sub {
  font-size: 11px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.32);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  margin-top: 3px;
}

.brand-rule {
  width: 28px;
  height: 1px;
  background: rgba(255, 255, 255, 0.14);
  margin-bottom: 36px;
}

.brand-hero {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-greeting {
  font-size: 34px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1.2;
  margin: 0 0 12px;
}

.hero-date {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.36);
  font-weight: 400;
  letter-spacing: 0.02em;
  margin: 0;
}

.brand-foot {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.18);
  letter-spacing: 0.04em;
}

// ─────────────────────────────────────────────
// 右側：表單面板
// ─────────────────────────────────────────────
.form-panel {
  flex: 1;
  // 雙節點微型漸層：左上藍暈 + 右下紫暈，讓背景有「來自某處」的感覺
  background:
    radial-gradient(ellipse at 18% 20%, rgba(64, 158, 255, 0.09) 0%, transparent 52%),
    radial-gradient(ellipse at 82% 82%, rgba(99, 102, 241, 0.07) 0%, transparent 52%),
    $form-bg;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  overflow-y: auto;
}

.form-inner {
  position: relative;
  width: 100%;
  max-width: 460px;

  // 白卡浮起，與左側深色面板形成清晰的「暗/亮」語言
  background: $white;
  border-radius: $radius-card;
  border: 1px solid rgba(0, 0, 0, 0.07);
  box-shadow:
    0 2px 4px   rgba(0, 0, 0, 0.03),
    0 8px 32px  rgba(0, 0, 0, 0.07),
    0 24px 64px rgba(0, 0, 0, 0.04);
  padding: 48px 44px 44px;
  overflow: hidden;

  animation: slideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1) both;

  // 卡片頂部品牌漸層線 — 從品牌藍延伸到紫，呼應背景雙色暈
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
    border-radius: $radius-card $radius-card 0 0;
    background: linear-gradient(90deg, #409EFF 0%, #818cf8 60%, transparent 100%);
  }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(18px); }
  to   { opacity: 1; transform: translateY(0); }
}

.form-head {
  margin-bottom: 36px;

  // 設計系統簽名：光暈膠囊出現在表單面板，與側邊欄 / 樹節點一脈相承
  &::before {
    content: '';
    display: block;
    width: 28px;
    height: 3px;
    border-radius: 3px;
    background: $primary;
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
    margin-bottom: 18px;
  }
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: $text-1;
  letter-spacing: -0.02em;
  line-height: 1.2;
  margin: 0 0 8px;
}

.form-hint {
  font-size: 14px;
  color: $text-2;
  margin: 0;
}

// ─────────────────────────────────────────────
// 表單元件
// ─────────────────────────────────────────────
.lform {
  // el-form-item 間距與錯誤訊息
  :deep(.el-form-item) {
    margin-bottom: 20px;

    .el-form-item__content {
      width: 100%;
      display: block;
      line-height: 1;
    }

    .el-form-item__error {
      position: static;
      display: block;
      margin-top: 5px;
      padding-left: 2px;
      font-size: 12px;
      color: #ef4444;
    }
  }
}

.field-wrap {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 7px;
  display: block;
}

.field-box {
  display: flex;
  align-items: center;
  background: #f8fafc;  // 白卡內輸入框略深，視覺上「凹入」感
  border: 1px solid $border;
  border-radius: $radius-md;
  padding: 0 14px;
  height: 46px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;

  &.focused {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.10);

    .f-icon { color: $primary; }
  }
}

.f-icon {
  font-size: 16px;
  color: $text-3;
  margin-right: 10px;
  flex-shrink: 0;
  transition: color 0.15s ease;
}

.f-toggle {
  font-size: 17px;
  color: $text-3;
  cursor: pointer;
  margin-left: 8px;
  padding: 4px;
  flex-shrink: 0;
  transition: color 0.15s ease;

  &:hover { color: $text-2; }
}

// 輸入框透明覆蓋
:deep(.field-box .el-input) { flex: 1; }
:deep(.field-box .el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none;
  padding: 0;
}
:deep(.field-box .el-input__inner) {
  font-size: 14px;
  color: $text-1;
  height: 44px;

  &::placeholder {
    color: $text-3;
    font-weight: 400;
  }
}

// 驗證碼列
.captcha-row {
  display: flex;
  gap: 10px;

  .captcha-box { flex: 1; }

  .captcha-img {
    position: relative;
    width: 116px;
    height: 46px;
    border-radius: $radius-md;
    overflow: hidden;
    cursor: pointer;
    flex-shrink: 0;
    border: 1px solid $border;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
      transition: opacity 0.2s ease;
    }

    .captcha-mask {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.32);
      opacity: 0;
      transition: opacity 0.2s ease;

      :deep(.el-icon) { color: #fff; font-size: 18px; }
    }

    &:hover {
      img { opacity: 0.45; }
      .captcha-mask { opacity: 1; }
    }
  }
}

// 記住我
.form-opts {
  margin-bottom: 24px;

  :deep(.el-checkbox__label) {
    font-size: 13px;
    color: $text-2;
  }
  :deep(.el-checkbox__inner) {
    border-radius: 5px;
    border-color: rgba(0, 0, 0, 0.15);
  }
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background: $primary;
    border-color: $primary;
  }
}

// 登入按鈕 — 漸層 + 多層光暈，讓最重要的操作有相應的視覺重量
.submit-btn.el-button {
  width: 100%;
  height: 48px;
  border-radius: $radius-md;
  border: none;
  background: linear-gradient(135deg, #409EFF 0%, #3b7dd8 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.02em;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
  box-shadow:
    0 4px 20px rgba(64, 158, 255, 0.38),
    0 1px 4px  rgba(64, 158, 255, 0.20);

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #5aabff 0%, #409EFF 100%);
    box-shadow:
      0 6px 28px rgba(64, 158, 255, 0.52),
      0 2px 8px  rgba(64, 158, 255, 0.30);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.28);
  }

  .btn-icon { font-size: 14px; }
}

// 立即申請
.register-hint {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: $text-3;

  a {
    color: $primary;
    text-decoration: none;
    font-weight: 500;
    margin-left: 4px;
    &:hover { text-decoration: underline; }
  }
}

// 版權
.form-copy {
  margin-top: 52px;
  font-size: 11px;
  color: $text-3;
  text-align: center;
  letter-spacing: 0.01em;
}

// ─────────────────────────────────────────────
// RWD
// ─────────────────────────────────────────────
@media (max-width: 1024px) {
  .brand-panel { flex: 0 0 360px; padding: 48px 40px; }
  .hero-greeting { font-size: 28px; }
}

@media (max-width: 768px) {
  .login-page { flex-direction: column; }

  .brand-panel {
    flex: 0 0 auto;
    padding: 24px 28px;
    flex-direction: row;
    align-items: center;
    min-height: 80px;

    &::before { display: none; }
  }

  .dot-grid, .ring { display: none; }

  .brand-content {
    flex-direction: row;
    align-items: center;
    height: auto;
    gap: 16px;
  }

  .brand-mark     { margin-bottom: 0; }
  .brand-rule     { display: none; }
  .brand-hero     { flex: 1; text-align: right; }
  .hero-greeting  { font-size: 16px; margin: 0; font-weight: 600; }
  .hero-date      { display: none; }
  .brand-foot     { display: none; }

  .form-panel { padding: 24px 20px; align-items: flex-start; }
  .form-inner { max-width: 100%; padding: 36px 28px 32px; }
}

@media (max-width: 480px) {
  .brand-panel { padding: 20px 20px; }
  .brand-icon  { width: 36px; height: 36px; }
  .brand-name  { font-size: 15px; }
  .form-panel  { padding: 20px 16px; }
  .form-inner  { padding: 28px 20px 24px; border-radius: 16px; }
  .form-title  { font-size: 22px; }
}
</style>
