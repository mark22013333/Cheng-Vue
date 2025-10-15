import request from '@/utils/request'

// 取得庫存報表資料
export function getStockReport(query) {
  return request({
    url: '/inventory/report/stock',
    method: 'get',
    params: query
  })
}

// 取得借出報表資料
export function getBorrowReport(query) {
  return request({
    url: '/inventory/report/borrow',
    method: 'get',
    params: query
  })
}

// 取得庫存異動報表資料
export function getMovementReport(query) {
  return request({
    url: '/inventory/report/movement',
    method: 'get',
    params: query
  })
}

// 取得掃描記錄報表資料
export function getScanReport(query) {
  return request({
    url: '/inventory/report/scan',
    method: 'get',
    params: query
  })
}

// 匯出庫存報表
export function exportStockReport(query) {
  return request({
    url: '/inventory/report/stock/export',
    method: 'post',
    params: query
  })
}

// 匯出借出報表
export function exportBorrowReport(query) {
  return request({
    url: '/inventory/report/borrow/export',
    method: 'post',
    params: query
  })
}

// 匯出異動報表
export function exportMovementReport(query) {
  return request({
    url: '/inventory/report/movement/export',
    method: 'post',
    params: query
  })
}

// 匯出掃描報表
export function exportScanReport(query) {
  return request({
    url: '/inventory/report/scan/export',
    method: 'post',
    params: query
  })
}
