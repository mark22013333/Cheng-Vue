import request from '@/utils/request'

// 登入方法
export function login(username, password, code, uuid) {
  const data = {
    username,
    password,
    code,
    uuid
  }
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: data
  })
}

// 註冊方法
export function register(data) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 取得使用者詳細訊息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 登出方法
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 取得驗證碼
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}
