import request from '@/utils/request'

// 查詢物品資訊列表
export function listItem(query) {
  return request({
    url: '/inventory/item/list',
    method: 'get',
    params: query
  })
}

// 查詢物品列表（含庫存數量，供商城使用）
export function listItemWithStock(query) {
  return request({
    url: '/inventory/item/listWithStock',
    method: 'get',
    params: query
  })
}

// 查詢物品資訊詳細
export function getItem(itemId) {
  return request({
    url: '/inventory/item/' + itemId,
    method: 'get'
  })
}

// 根據物品編碼查詢物品資訊
export function getItemByCode(itemCode) {
  return request({
    url: '/inventory/item/code/' + itemCode,
    method: 'get'
  })
}

// 新增物品資訊
export function addItem(data) {
  return request({
    url: '/inventory/item',
    method: 'post',
    data: data
  })
}

// 修改物品資訊
export function updateItem(data) {
  return request({
    url: '/inventory/item',
    method: 'put',
    data: data
  })
}

// 刪除物品資訊
export function delItem(itemId) {
  return request({
    url: '/inventory/item/' + itemId,
    method: 'delete'
  })
}

// 查詢低庫存物品列表
export function listLowStockItem() {
  return request({
    url: '/inventory/item/lowStock',
    method: 'get'
  })
}

// 掃描條碼或QR碼
export function scanItem(data) {
  return request({
    url: '/inventory/item/scan',
    method: 'post',
    data: data
  })
}
