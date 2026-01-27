import request from '@/utils/request'

/**
 * 取得當前會員的地址列表
 */
export function getAddressList() {
  return request({
    url: '/shop/address/list',
    method: 'get'
  })
}

/**
 * 取得地址詳情
 * @param {number} addressId - 地址 ID
 */
export function getAddress(addressId) {
  return request({
    url: `/shop/address/${addressId}`,
    method: 'get'
  })
}

/**
 * 取得預設地址
 */
export function getDefaultAddress() {
  return request({
    url: '/shop/address/default',
    method: 'get'
  })
}

/**
 * 新增地址
 * @param {Object} data - 地址資料
 */
export function addAddress(data) {
  return request({
    url: '/shop/address',
    method: 'post',
    data: data
  })
}

/**
 * 更新地址
 * @param {Object} data - 地址資料
 */
export function updateAddress(data) {
  return request({
    url: '/shop/address',
    method: 'put',
    data: data
  })
}

/**
 * 刪除地址
 * @param {number} addressId - 地址 ID
 */
export function deleteAddress(addressId) {
  return request({
    url: `/shop/address/${addressId}`,
    method: 'delete'
  })
}

/**
 * 設定預設地址
 * @param {number} addressId - 地址 ID
 */
export function setDefaultAddress(addressId) {
  return request({
    url: `/shop/address/default/${addressId}`,
    method: 'put'
  })
}
