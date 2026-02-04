import request from '@/utils/requestShop'

// 使用會員端點（不需後台權限，只需登入）
const BASE_URL = '/shop/member/cart'

/**
 * 取得購物車內容
 */
export function getCart() {
  return request({
    url: BASE_URL,
    method: 'get'
  })
}

/**
 * 取得購物車商品數量
 */
export function getCartCount() {
  return request({
    url: `${BASE_URL}/count`,
    method: 'get'
  })
}

/**
 * 加入購物車
 * @param {Object} data - { skuId, quantity }
 */
export function addToCart(data) {
  return request({
    url: `${BASE_URL}/add`,
    method: 'post',
    data: data
  })
}

/**
 * 更新購物車數量
 * @param {Object} data - { cartId, quantity }
 */
export function updateCartQuantity(data) {
  return request({
    url: `${BASE_URL}/update`,
    method: 'put',
    data: data
  })
}

/**
 * 更新選中狀態
 * @param {number} cartId - 購物車項目ID
 * @param {boolean} selected - 是否選中
 */
export function updateCartSelected(cartId, selected) {
  return request({
    url: `${BASE_URL}/select/${cartId}`,
    method: 'put',
    params: { selected }
  })
}

/**
 * 全選/取消全選
 * @param {boolean} selected - 是否選中
 */
export function selectAllCart(selected) {
  return request({
    url: `${BASE_URL}/selectAll`,
    method: 'put',
    params: { selected }
  })
}

/**
 * 移除購物車項目
 * @param {number} cartId - 購物車項目ID
 */
export function removeCartItem(cartId) {
  return request({
    url: `${BASE_URL}/remove/${cartId}`,
    method: 'delete'
  })
}

/**
 * 批量刪除購物車項目
 * @param {Array<number>} cartIds - 購物車項目ID陣列
 */
export function removeCartItems(cartIds) {
  return request({
    url: `${BASE_URL}/remove`,
    method: 'delete',
    data: cartIds
  })
}

/**
 * 清空購物車
 */
export function clearCart() {
  return request({
    url: `${BASE_URL}/clear`,
    method: 'delete'
  })
}

/**
 * 刪除已選中項目
 */
export function removeSelectedItems() {
  return request({
    url: `${BASE_URL}/removeSelected`,
    method: 'delete'
  })
}

/**
 * 取得已選中的購物車項目（結帳用）
 */
export function getSelectedCartItems() {
  return request({
    url: `${BASE_URL}/selected`,
    method: 'get'
  })
}

/**
 * 合併訪客購物車（登入後呼叫）
 * @param {Array} items - [{ skuId, quantity }]
 */
export function mergeGuestCart(items) {
  return request({
    url: `${BASE_URL}/merge`,
    method: 'post',
    data: items
  })
}
