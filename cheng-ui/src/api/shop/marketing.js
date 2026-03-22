import request from '@/utils/requestShop'

/**
 * 上報瀏覽事件（公開，支援訪客與會員）
 */
export function reportBrowse(data) {
  return request({
    url: '/shop/front/tracking/browse',
    method: 'post',
    data
  })
}

/**
 * 上報搜尋事件（公開，支援訪客與會員）
 */
export function reportSearch(data) {
  return request({
    url: '/shop/front/tracking/search',
    method: 'post',
    data
  })
}

/**
 * 合併訪客追蹤記錄至當前會員（需登入）
 */
export function mergeGuestTracking(guestId) {
  return request({
    url: '/shop/member/tracking/merge',
    method: 'post',
    data: { guestId }
  })
}

/**
 * 查詢個人最近瀏覽（需登入）
 */
export function getRecentViews(limit = 10) {
  return request({
    url: '/shop/member/tracking/recent-views',
    method: 'get',
    params: { limit }
  })
}

/**
 * 查詢熱門搜尋關鍵字（公開）
 */
export function getPopularSearches(days = 7, limit = 10) {
  return request({
    url: '/shop/front/tracking/popular-searches',
    method: 'get',
    params: { days, limit }
  })
}

/**
 * 查詢熱門瀏覽商品（公開）
 */
export function getHotProducts(days = 7, limit = 10) {
  return request({
    url: '/shop/front/tracking/hot-products',
    method: 'get',
    params: { days, limit }
  })
}
