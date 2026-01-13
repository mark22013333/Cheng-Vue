import request from '@/utils/request'

// ==================== LINE 標籤群組 ====================

// 查詢 LINE 標籤群組列表
export function listLineTagGroups(query) {
  return request({
    url: '/tag/group/line/list',
    method: 'get',
    params: query
  })
}

// 取得 LINE 標籤群組詳情
export function getLineTagGroup(groupId) {
  return request({
    url: '/tag/group/line/' + groupId,
    method: 'get'
  })
}

// 新增 LINE 標籤群組
export function addLineTagGroup(data) {
  return request({
    url: '/tag/group/line',
    method: 'post',
    data: data
  })
}

// 修改 LINE 標籤群組
export function updateLineTagGroup(data) {
  return request({
    url: '/tag/group/line',
    method: 'put',
    data: data
  })
}

// 刪除 LINE 標籤群組
export function delLineTagGroup(groupIds) {
  return request({
    url: '/tag/group/line/' + groupIds,
    method: 'delete'
  })
}

// 執行 LINE 群組運算
export function calcLineTagGroup(groupId) {
  return request({
    url: '/tag/group/line/calc/' + groupId,
    method: 'post',
    timeout: 300000
  })
}

// 預覽 LINE 群組運算結果
export function previewLineTagGroup(groupId, limit = 10) {
  return request({
    url: '/tag/group/line/preview/' + groupId,
    method: 'get',
    params: { limit }
  })
}

// ==================== 庫存標籤群組 ====================

// 查詢庫存標籤群組列表
export function listInvTagGroups(query) {
  return request({
    url: '/tag/group/inventory/list',
    method: 'get',
    params: query
  })
}

// 取得庫存標籤群組詳情
export function getInvTagGroup(groupId) {
  return request({
    url: '/tag/group/inventory/' + groupId,
    method: 'get'
  })
}

// 新增庫存標籤群組
export function addInvTagGroup(data) {
  return request({
    url: '/tag/group/inventory',
    method: 'post',
    data: data
  })
}

// 修改庫存標籤群組
export function updateInvTagGroup(data) {
  return request({
    url: '/tag/group/inventory',
    method: 'put',
    data: data
  })
}

// 刪除庫存標籤群組
export function delInvTagGroup(groupIds) {
  return request({
    url: '/tag/group/inventory/' + groupIds,
    method: 'delete'
  })
}

// 執行庫存群組運算
export function calcInvTagGroup(groupId) {
  return request({
    url: '/tag/group/inventory/calc/' + groupId,
    method: 'post',
    timeout: 300000
  })
}

// 預覽庫存群組運算結果
export function previewInvTagGroup(groupId, limit = 10) {
  return request({
    url: '/tag/group/inventory/preview/' + groupId,
    method: 'get',
    params: { limit }
  })
}
