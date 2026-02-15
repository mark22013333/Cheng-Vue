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
