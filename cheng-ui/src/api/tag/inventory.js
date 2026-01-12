import request from '@/utils/request'

// 查詢庫存標籤列表
export function listInventoryTags(query) {
  return request({
    url: '/tag/inventory/list',
    method: 'get',
    params: query
  })
}

// 查詢庫存標籤下拉選項
export function getInventoryTagOptions(status) {
  return request({
    url: '/tag/inventory/options',
    method: 'get',
    params: { status }
  })
}

// 查詢標籤詳情
export function getInventoryTag(tagId) {
  return request({
    url: '/tag/inventory/' + tagId,
    method: 'get'
  })
}

// 新增庫存標籤
export function addInventoryTag(data) {
  return request({
    url: '/tag/inventory',
    method: 'post',
    data: data
  })
}

// 修改庫存標籤
export function updateInventoryTag(data) {
  return request({
    url: '/tag/inventory',
    method: 'put',
    data: data
  })
}

// 刪除庫存標籤
export function delInventoryTag(tagIds) {
  return request({
    url: '/tag/inventory/' + tagIds,
    method: 'delete'
  })
}

// 查詢物品標籤關聯列表
export function listInvItemTagRelations(query) {
  return request({
    url: '/tag/inventory/relation/list',
    method: 'get',
    params: query
  })
}

// 查詢指定物品的標籤
export function getItemTags(itemId) {
  return request({
    url: '/tag/inventory/relation/item/' + itemId,
    method: 'get'
  })
}

// 查詢指定標籤的物品
export function getTagItems(tagId) {
  return request({
    url: '/tag/inventory/relation/tag/' + tagId,
    method: 'get'
  })
}

// 為物品貼標
export function bindInvItemTag(data) {
  return request({
    url: '/tag/inventory/relation/bind',
    method: 'post',
    data: data
  })
}

// 更新物品標籤（支援多標籤）
export function updateInvItemTags(data) {
  return request({
    url: '/tag/inventory/relation/updateTags',
    method: 'post',
    data: data
  })
}

// 批次為物品貼標
export function batchBindInvItemTag(data) {
  return request({
    url: '/tag/inventory/relation/batchBind',
    method: 'post',
    data: data
  })
}

// 批次為物品貼標（含驗證和額外標籤）
export function batchBindInvItemTagWithValidation(data) {
  return request({
    url: '/tag/inventory/relation/batchBindWithValidation',
    method: 'post',
    data: data,
    timeout: 300000
  })
}

// 移除物品標籤
export function unbindInvItemTag(data) {
  return request({
    url: '/tag/inventory/relation/unbind',
    method: 'delete',
    data: data
  })
}

// 批次刪除關聯
export function delInvItemTagRelations(ids) {
  return request({
    url: '/tag/inventory/relation/' + ids,
    method: 'delete'
  })
}
