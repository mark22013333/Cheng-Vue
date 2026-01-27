import request from '@/utils/request'

// 查詢訂單列表
export function listOrder(query) {
  return request({
    url: '/shop/order/list',
    method: 'get',
    params: query
  })
}

// 查詢訂單詳細
export function getOrder(orderId) {
  return request({
    url: '/shop/order/' + orderId,
    method: 'get'
  })
}

// 出貨
export function shipOrder(data) {
  return request({
    url: '/shop/order/ship',
    method: 'put',
    data: data
  })
}

// 取消訂單
export function cancelOrder(orderId, reason) {
  return request({
    url: '/shop/order/cancel',
    method: 'put',
    data: { orderId, cancelReason: reason }
  })
}

// 確認收貨
export function deliverOrder(orderId) {
  return request({
    url: '/shop/order/deliver/' + orderId,
    method: 'put'
  })
}

// 完成訂單
export function completeOrder(orderId) {
  return request({
    url: '/shop/order/complete/' + orderId,
    method: 'put'
  })
}

// 更新訂單備註
export function updateOrderRemark(data) {
  return request({
    url: '/shop/order/remark',
    method: 'put',
    data: data
  })
}

// 手動更新付款狀態
export function updatePayStatus(orderId, payStatus) {
  return request({
    url: `/shop/order/${orderId}/payStatus`,
    method: 'put',
    params: { payStatus }
  })
}

// 手動更新物流狀態
export function updateShipStatus(orderId, shipStatus) {
  return request({
    url: `/shop/order/${orderId}/shipStatus`,
    method: 'put',
    params: { shipStatus }
  })
}

// ============ 會員前台訂單 API ============

/**
 * 取得當前會員的訂單列表
 * @param {string} status - 訂單狀態篩選（可選）
 */
export function getMyOrders(status) {
  return request({
    url: '/shop/order/my/list',
    method: 'get',
    params: status ? { status } : {}
  })
}

/**
 * 取得會員訂單詳情
 * @param {string} orderNo - 訂單編號
 */
export function getMyOrderDetail(orderNo) {
  return request({
    url: `/shop/order/my/${orderNo}`,
    method: 'get'
  })
}

/**
 * 取得訂單狀態統計
 */
export function getOrderStats() {
  return request({
    url: '/shop/order/my/stats',
    method: 'get'
  })
}

/**
 * 會員取消訂單
 * @param {number} orderId - 訂單 ID
 * @param {string} reason - 取消原因
 */
export function memberCancelOrder(orderId, reason) {
  return request({
    url: `/shop/order/my/cancel/${orderId}`,
    method: 'post',
    params: reason ? { reason } : {}
  })
}

/**
 * 會員確認收貨
 * @param {number} orderId - 訂單 ID
 */
export function memberConfirmReceipt(orderId) {
  return request({
    url: `/shop/order/my/confirm/${orderId}`,
    method: 'post'
  })
}
