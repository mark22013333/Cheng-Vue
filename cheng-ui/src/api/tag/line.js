import request from '@/utils/request'

// 查詢 LINE 標籤列表
export function listLineTags(query) {
  return request({
    url: '/tag/line/list',
    method: 'get',
    params: query
  })
}

// 查詢 LINE 標籤下拉選項
export function getLineTagOptions(status) {
  return request({
    url: '/tag/line/options',
    method: 'get',
    params: { status }
  })
}

// 查詢標籤詳情
export function getLineTag(tagId) {
  return request({
    url: '/tag/line/' + tagId,
    method: 'get'
  })
}

// 新增 LINE 標籤
export function addLineTag(data) {
  return request({
    url: '/tag/line',
    method: 'post',
    data: data
  })
}

// 修改 LINE 標籤
export function updateLineTag(data) {
  return request({
    url: '/tag/line',
    method: 'put',
    data: data
  })
}

// 刪除 LINE 標籤
export function delLineTag(tagIds) {
  return request({
    url: '/tag/line/' + tagIds,
    method: 'delete'
  })
}

// 查詢使用者標籤關聯列表
export function listLineUserTagRelations(query) {
  return request({
    url: '/tag/line/relation/list',
    method: 'get',
    params: query
  })
}

// 查詢指定使用者的標籤
export function getUserTags(lineUserId) {
  return request({
    url: '/tag/line/relation/user/' + lineUserId,
    method: 'get'
  })
}

// 查詢指定標籤的使用者
export function getTagUsers(tagId) {
  return request({
    url: '/tag/line/relation/tag/' + tagId,
    method: 'get'
  })
}

// 為使用者貼標
export function bindLineUserTag(data) {
  return request({
    url: '/tag/line/relation/bind',
    method: 'post',
    data: data
  })
}

// 更新使用者標籤（支援多標籤）
export function updateLineUserTags(data) {
  return request({
    url: '/tag/line/relation/updateTags',
    method: 'post',
    data: data
  })
}

// 批次為使用者貼標
export function batchBindLineUserTag(data) {
  return request({
    url: '/tag/line/relation/batchBind',
    method: 'post',
    data: data
  })
}

// 批次為使用者貼標（含驗證和額外標籤）
export function batchBindLineUserTagWithValidation(data) {
  return request({
    url: '/tag/line/relation/batchBindWithValidation',
    method: 'post',
    data: data,
    timeout: 300000 // 5 分鐘超時（大量資料處理）
  })
}

// 移除使用者標籤
export function unbindLineUserTag(data) {
  return request({
    url: '/tag/line/relation/unbind',
    method: 'delete',
    data: data
  })
}

// 批次刪除關聯
export function delLineUserTagRelations(ids) {
  return request({
    url: '/tag/line/relation/' + ids,
    method: 'delete'
  })
}
