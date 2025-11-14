import request from '@/utils/request'

// 查詢 Rich Menu 列表
export function listRichMenu(query) {
  return request({
    url: '/line/richMenu/list',
    method: 'get',
    params: query
  })
}

// 查詢 Rich Menu 詳細
export function getRichMenu(id) {
  return request({
    url: '/line/richMenu/' + id,
    method: 'get'
  })
}

// 根據 LINE richMenuId 查詢
export function getRichMenuByRichMenuId(richMenuId) {
  return request({
    url: '/line/richMenu/byRichMenuId/' + richMenuId,
    method: 'get'
  })
}

// 根據頻道 ID 查詢預設選單
export function getDefaultRichMenu(configId) {
  return request({
    url: '/line/richMenu/default/' + configId,
    method: 'get'
  })
}

// 新增 Rich Menu
export function addRichMenu(data) {
  return request({
    url: '/line/richMenu',
    method: 'post',
    data: data
  })
}

// 修改 Rich Menu
export function updateRichMenu(data) {
  return request({
    url: '/line/richMenu',
    method: 'put',
    data: data
  })
}

// 刪除 Rich Menu
export function delRichMenu(ids) {
  return request({
    url: '/line/richMenu/' + ids,
    method: 'delete'
  })
}

// 發布 Rich Menu 到 LINE 平台
export function publishRichMenu(id) {
  return request({
    url: '/line/richMenu/publish/' + id,
    method: 'post'
  })
}

// 設定為預設選單
export function setDefaultRichMenu(id) {
  return request({
    url: '/line/richMenu/setDefault/' + id,
    method: 'post'
  })
}

// 從 LINE 平台刪除 Rich Menu
export function deleteRichMenuFromLine(id) {
  return request({
    url: '/line/richMenu/deleteFromLine/' + id,
    method: 'delete'
  })
}

// 綁定 Rich Menu 到使用者
export function linkRichMenuToUser(userId, richMenuId) {
  return request({
    url: '/line/richMenu/linkToUser',
    method: 'post',
    params: { userId, richMenuId }
  })
}

// 解除使用者的 Rich Menu 綁定
export function unlinkRichMenuFromUser(userId) {
  return request({
    url: '/line/richMenu/unlinkFromUser/' + userId,
    method: 'delete'
  })
}

// 檢查選單名稱是否唯一
export function checkNameUnique(configId, name, excludeId) {
  return request({
    url: '/line/richMenu/checkNameUnique',
    method: 'get',
    params: { configId, name, id: excludeId }
  })
}

// 匯出 Rich Menu
export function exportRichMenu(query) {
  return request({
    url: '/line/richMenu/export',
    method: 'post',
    params: query
  })
}
