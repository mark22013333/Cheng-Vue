import request from '@/utils/request'

// 查詢借出記錄列表
export function listBorrow(query) {
  return request({
    url: '/inventory/borrow/list',
    method: 'get',
    params: query
  })
}

// 查詢借出記錄詳細
export function getBorrow(borrowId) {
  return request({
    url: '/inventory/borrow/' + borrowId,
    method: 'get'
  })
}

// 新增借出記錄
export function addBorrow(data) {
  return request({
    url: '/inventory/borrow',
    method: 'post',
    data: data
  })
}

// 修改借出記錄
export function updateBorrow(data) {
  return request({
    url: '/inventory/borrow',
    method: 'put',
    data: data
  })
}

// 刪除借出記錄
export function delBorrow(borrowId) {
  return request({
    url: '/inventory/borrow/' + borrowId,
    method: 'delete'
  })
}

// 借出物品
export function borrowItem(data) {
  return request({
    url: '/inventory/borrow/borrow',
    method: 'post',
    data: data
  })
}

// 歸還物品
export function returnBorrow(data) {
  return request({
    url: '/inventory/borrow/returnItem',
    method: 'post',
    data: data
  })
}

// 遺失物品
export function lostItem(data) {
  return request({
    url: '/inventory/borrow/lostItem',
    method: 'post',
    data: data
  })
}

// 審核借出申請
export function approveBorrow(data) {
  return request({
    url: '/inventory/borrow/approve',
    method: 'post',
    data: data
  })
}

// 取得借出統計
export function getBorrowStats(query) {
  return request({
    url: '/inventory/borrow/stats',
    method: 'get',
    params: query
  })
}

// 取得逾期記錄
export function getOverdueList(query) {
  return request({
    url: '/inventory/borrow/overdue',
    method: 'get',
    params: query
  })
}

// 匯出借出記錄
export function exportBorrow(query) {
  return request({
    url: '/inventory/borrow/export',
    method: 'get',
    params: query
  })
}

// 查詢借出記錄的歸還記錄
export function getReturnRecords(borrowId) {
  return request({
    url: '/inventory/borrow/returnRecords/' + borrowId,
    method: 'get'
  })
}
