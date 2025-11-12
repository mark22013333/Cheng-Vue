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
export function bindUser(lineUserId, sysUserId) {
  return request({
    url: `/line/user/bind/${lineUserId}/${sysUserId}`,
    method: 'put'
  })
}

// 解除綁定
export function unbindUser(lineUserId) {
  return request({
    url: '/line/user/unbind/' + lineUserId,
    method: 'put'
  })
}

// 更新使用者個人資料（從 LINE API 同步）
export function syncUserProfile(lineUserId, configId) {
  return request({
    url: '/line/user/updateProfile/' + lineUserId,
    method: 'put',
    params: { configId }
  })
}

// 刪除 LINE 使用者
export function delUser(ids) {
  return request({
    url: '/line/user/' + ids,
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

// 取得每月加入統計資料
export function getMonthlyStats(startTime, endTime) {
  return request({
    url: '/line/user/monthlyStats',
    method: 'get',
    params: { startTime, endTime }
  })
}

// 查詢所有關注中的使用者
export function listFollowingUsers() {
  return request({
    url: '/line/user/following',
    method: 'get'
  })
}

// 匯入 LINE 使用者
export function importUser(data) {
  return request({
    url: '/line/user/import',
    method: 'post',
    data: data
  })
}

// 下載匯入範本
export function downloadTemplate() {
  return request({
    url: '/line/user/importTemplate',
    method: 'post',
    responseType: 'blob'
  })
}

// 將使用者加入黑名單
export function addToBlacklist(lineUserId) {
  return request({
    url: `/line/user/blacklist/add/${lineUserId}`,
    method: 'put'
  })
}

// 將使用者從黑名單移除
export function removeFromBlacklist(lineUserId) {
  return request({
    url: `/line/user/blacklist/remove/${lineUserId}`,
    method: 'put'
  })
}

// 批次將使用者加入黑名單
export function batchAddToBlacklist(lineUserIds) {
  return request({
    url: '/line/user/blacklist/batchAdd',
    method: 'put',
    data: lineUserIds
  })
}

// 批次將使用者從黑名單移除
export function batchRemoveFromBlacklist(lineUserIds) {
  return request({
    url: '/line/user/blacklist/batchRemove',
    method: 'put',
    data: lineUserIds
  })
}
