import request from '@/utils/request'

// 查詢庫存列表
export function listStock(query) {
  return request({
    url: '/inventory/stock/list',
    method: 'get',
    params: query
  })
}

// 查詢庫存詳細
export function getStock(stockId) {
  return request({
    url: '/inventory/stock/' + stockId,
    method: 'get'
  })
}

// 新增庫存
export function addStock(data) {
  return request({
    url: '/inventory/stock',
    method: 'post',
    data: data
  })
}

// 修改庫存
export function updateStock(data) {
  return request({
    url: '/inventory/stock',
    method: 'put',
    data: data
  })
}

// 刪除庫存
export function delStock(stockId) {
  return request({
    url: '/inventory/stock/' + stockId,
    method: 'delete'
  })
}

// 入庫操作
export function stockIn(data) {
  return request({
    url: '/inventory/stock/in',
    method: 'post',
    data: data
  })
}

// 出庫操作
export function stockOut(data) {
  return request({
    url: '/inventory/stock/out',
    method: 'post',
    data: data
  })
}

// 庫存盤點
export function stockCheck(data) {
  return request({
    url: '/inventory/stock/check',
    method: 'post',
    data: data
  })
}

// 取得庫存統計
export function getStockStats() {
  return request({
    url: '/inventory/stock/statistics',
    method: 'get'
  })
}

// 取得低庫存列表
export function getLowStockList(query) {
  return request({
    url: '/inventory/stock/lowStock',
    method: 'get',
    params: query
  })
}

// 匯出庫存
export function exportStock(data) {
  return request({
    url: '/inventory/stock/export',
    method: 'post',
    data: data
  })
}
