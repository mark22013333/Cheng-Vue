import request from '@/utils/request'

// 查詢物品與庫存整合列表
export function listManagement(query) {
  return request({
    url: '/inventory/management/list',
    method: 'get',
    params: query
  })
}

// 查詢低庫存物品列表
export function listLowStock() {
  return request({
    url: '/inventory/management/lowStock',
    method: 'get'
  })
}

// 查詢物品與庫存詳細
export function getManagement(itemId) {
  return request({
    url: '/inventory/management/' + itemId,
    method: 'get'
  })
}

// 新增物品資訊
export function addManagement(data) {
  return request({
    url: '/inventory/management',
    method: 'post',
    data: data
  })
}

// 修改物品資訊
export function updateManagement(data) {
  return request({
    url: '/inventory/management',
    method: 'put',
    data: data
  })
}

// 刪除物品資訊
export function delManagement(itemIds) {
  return request({
    url: '/inventory/management/' + itemIds,
    method: 'delete'
  })
}

// 匯出物品與庫存資料
export function exportManagement(query) {
  return request({
    url: '/inventory/management/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 入庫操作
export function stockIn(data) {
  return request({
    url: '/inventory/management/stockIn',
    method: 'post',
    data: data
  })
}

// 手機端快速入庫（只需掃描權限）
export function quickStockIn(data) {
  return request({
    url: '/inventory/management/quickStockIn',
    method: 'post',
    data: data
  })
}

// 出庫操作
export function stockOut(data) {
  return request({
    url: '/inventory/management/stockOut',
    method: 'post',
    data: data
  })
}

// 盤點操作
export function stockCheck(data) {
  return request({
    url: '/inventory/management/stockCheck',
    method: 'post',
    data: data
  })
}
