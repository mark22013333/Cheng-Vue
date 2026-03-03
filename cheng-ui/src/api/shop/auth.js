import requestShop from '@/utils/requestShop'

// 會員登入
export function memberLogin(data) {
  return requestShop({
    url: '/shop/auth/login',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}

// 會員註冊
export function memberRegister(data) {
  return requestShop({
    url: '/shop/auth/register',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}

// 會員登出
export function memberLogout() {
  return requestShop({
    url: '/shop/auth/logout',
    method: 'post'
  })
}

// 取得會員資訊
export function getMemberProfile() {
  return requestShop({
    url: '/shop/auth/profile',
    method: 'get'
  })
}

// 更新會員資訊
export function updateMemberProfile(data) {
  return requestShop({
    url: '/shop/auth/profile',
    method: 'put',
    data
  })
}

// 更新會員頭像
export function updateMemberAvatar(avatar) {
  return requestShop({
    url: '/shop/auth/profile/avatar',
    method: 'put',
    data: { avatar }
  })
}

// Email 驗證（點擊驗證連結）
export function verifyEmail(params) {
  return requestShop({
    url: '/shop/auth/verify-email',
    method: 'get',
    headers: {
      isToken: false
    },
    params
  })
}

// 重寄 Email 驗證信
export function resendVerification(data) {
  return requestShop({
    url: '/shop/auth/resend-verification',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}

// 忘記密碼 - 發送重設連結
export function forgotPassword(data) {
  return requestShop({
    url: '/shop/auth/forgot-password',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}

// 忘記密碼 - 驗證重設連結有效性（POST body，安全版）
export function validateResetToken(data) {
  return requestShop({
    url: '/shop/auth/validate-reset-token',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}

// 忘記密碼 - 驗證重設連結有效性（GET 相容版，已棄用）
export function validateResetTokenLegacy(params) {
  return requestShop({
    url: '/shop/auth/validate-reset-token',
    method: 'get',
    headers: {
      isToken: false
    },
    params
  })
}

// 忘記密碼 - 取得密碼重設政策
export function getPasswordResetPolicy() {
  return requestShop({
    url: '/shop/auth/password-reset-policy',
    method: 'get',
    headers: {
      isToken: false
    }
  })
}

// 忘記密碼 - 重設密碼（使用 selector + token）
export function resetPassword(data) {
  return requestShop({
    url: '/shop/auth/reset-password',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}
