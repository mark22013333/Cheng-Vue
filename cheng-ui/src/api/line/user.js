import request from '@/utils/request'

// 查詢 LINE 使用者列表
export function listUser(query) {
  return request({
    url: '/line/user/list',
    method: 'get',
    params: query
  })
}

// 查詢 LINE 使用者詳細
export function getUser(id) {
  return request({
    url: '/line/user/' + id,
    method: 'get'
  })
}

// 綁定系統使用者
export function bindUser(data) {
  return request({
    url: '/line/user/bind',
    method: 'post',
    data: data
  })
}

// 解除綁定
export function unbindUser(id) {
  return request({
    url: '/line/user/unbind/' + id,
    method: 'post'
  })
}

// 更新使用者個人資料（從 LINE API 同步）
export function syncUserProfile(lineUserId) {
  return request({
    url: '/line/user/sync/' + lineUserId,
    method: 'post'
  })
}

// 刪除 LINE 使用者
export function delUser(id) {
  return request({
    url: '/line/user/' + id,
    method: 'delete'
  })
}

// 匯出 LINE 使用者資料
export function exportUser(query) {
  return request({
    url: '/line/user/export',
    method: 'post',
    params: query
  })
}

// 取得使用者統計資料
export function getUserStats() {
  return request({
    url: '/line/user/stats',
    method: 'get'
  })
}

// 查詢所有關注中的使用者
export function listFollowingUsers() {
  return request({
    url: '/line/user/following',
    method: 'get'
  })
}

// 查詢所有已綁定的使用者
export function listBoundUsers() {
  return request({
    url: '/line/user/bound',
    method: 'get'
  })
}

// 批次綁定使用者
export function batchBindUsers(data) {
  return request({
    url: '/line/user/batchBind',
    method: 'post',
    data: data
  })
}

// 批次解除綁定
export function batchUnbindUsers(ids) {
  return request({
    url: '/line/user/batchUnbind',
    method: 'post',
    data: ids
  })
}
