<template>
  <div class="oauth-callback-page">
    <div class="callback-shell">
      <div class="callback-card">
        <!-- 載入中 -->
        <template v-if="status === 'loading'">
          <el-icon class="spin-icon" :size="48" color="#4A6B7C">
            <Loading />
          </el-icon>
          <h2>正在登入中...</h2>
          <p>正在透過第三方帳號驗證，請稍候。</p>
        </template>

        <!-- 成功 -->
        <template v-else-if="status === 'success'">
          <el-icon :size="48" color="#67C23A">
            <CircleCheckFilled />
          </el-icon>
          <h2>登入成功</h2>
          <p>即將為您跳轉...</p>
        </template>

        <!-- 失敗 -->
        <template v-else>
          <el-icon :size="48" color="#F56C6C">
            <CircleCloseFilled />
          </el-icon>
          <h2>登入失敗</h2>
          <p class="error-msg">{{ errorMessage }}</p>
          <el-button type="primary" class="back-btn" @click="goLogin">
            返回登入頁
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { handleOAuthCallback } from '@/api/shop/auth'
import { setMemberToken } from '@/utils/memberAuth'
import useMemberStore from '@/store/modules/member'
import { useCartStore } from '@/store/modules/cart'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()
const cartStore = useCartStore()

const status = ref('loading')
const errorMessage = ref('')

function goLogin() {
  router.push('/login')
}

onMounted(async () => {
  const { code, state } = route.query

  // 從 sessionStorage 取出登入前儲存的資訊
  const provider = sessionStorage.getItem('oauth_provider')
  const redirectPath = sessionStorage.getItem('oauth_redirect') || '/'
  const redirectUri = sessionStorage.getItem('oauth_redirect_uri') || ''

  // 清除暫存
  sessionStorage.removeItem('oauth_provider')
  sessionStorage.removeItem('oauth_redirect')
  sessionStorage.removeItem('oauth_redirect_uri')

  if (!code || !state || !provider) {
    status.value = 'error'
    errorMessage.value = '缺少必要的授權參數，請重新登入。'
    return
  }

  try {
    const res = await handleOAuthCallback(
      { provider, code, state },
      redirectUri
    )

    // 儲存 JWT
    setMemberToken(res.token)
    memberStore.token = res.token

    // 設定會員資料（如果 API 回傳了 member）
    if (res.member) {
      memberStore.setMember(res.member)
    } else {
      // 回傳只有 token 時，用 getProfile 取得會員資料
      try {
        await memberStore.getProfile()
      } catch (e) {
        console.warn('取得會員資料失敗', e)
      }
    }

    // 合併訪客購物車
    try {
      await cartStore.mergeGuestCartOnLogin()
    } catch (e) {
      console.error('合併購物車失敗', e)
    }

    status.value = 'success'
    ElMessage.success('登入成功')

    // 延遲跳轉讓使用者看到成功畫面
    setTimeout(() => {
      router.replace(redirectPath)
    }, 800)
  } catch (error) {
    status.value = 'error'
    errorMessage.value = error?.msg || error?.message || '第三方登入失敗，請稍後再試。'
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&family=Noto+Sans+Symbols+2&family=Noto+Color+Emoji&display=swap');

.oauth-callback-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
  padding: 40px 24px;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
}

.callback-shell {
  width: 100%;
  max-width: 420px;
}

.callback-card {
  text-align: center;
  padding: 48px 36px;
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid var(--mall-border-color, #E8E4DF);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}

.callback-card h2 {
  margin: 20px 0 8px;
  font-size: 24px;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  color: #3D2B1F;
}

.callback-card p {
  margin: 0;
  color: #7A6B5D;
  font-size: 14px;
}

.error-msg {
  color: #F56C6C !important;
  margin-bottom: 20px !important;
}

.back-btn {
  min-width: 160px;
  min-height: 44px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(125deg, #4A6B7C, #5A8A9A);
  font-weight: 700;
}

.spin-icon {
  animation: spin 1.2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 480px) {
  .callback-card {
    padding: 32px 20px;
    border-radius: 16px;
  }
}
</style>
