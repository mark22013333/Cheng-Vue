import request from '@/utils/request'

/**
 * 查詢禮物列表
 */
export function listGift(query) {
  return request({
    url: '/shop/gift/list',
    method: 'get',
    params: query
  })
}

/**
 * 查詢禮物詳情
 */
export function getGift(giftId) {
  return request({
    url: '/shop/gift/' + giftId,
    method: 'get'
  })
}

/**
 * 新增禮物
 */
export function addGift(data) {
  return request({
    url: '/shop/gift',
    method: 'post',
    data: data
  })
}

/**
 * 修改禮物
 */
export function updateGift(data) {
  return request({
    url: '/shop/gift',
    method: 'put',
    data: data
  })
}

/**
 * 刪除禮物
 */
export function delGift(giftIds) {
  return request({
    url: '/shop/gift/' + giftIds,
    method: 'delete'
  })
}
