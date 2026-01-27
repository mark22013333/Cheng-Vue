import request from '@/utils/request'

// 查詢輪播列表
export function listBanner(query) {
  return request({
    url: '/shop/banner/list',
    method: 'get',
    params: query
  })
}

// 查詢輪播詳細
export function getBanner(bannerId) {
  return request({
    url: '/shop/banner/' + bannerId,
    method: 'get'
  })
}

// 新增輪播
export function addBanner(data) {
  return request({
    url: '/shop/banner',
    method: 'post',
    data: data
  })
}

// 修改輪播
export function updateBanner(data) {
  return request({
    url: '/shop/banner',
    method: 'put',
    data: data
  })
}

// 刪除輪播
export function delBanner(bannerId) {
  return request({
    url: '/shop/banner/' + bannerId,
    method: 'delete'
  })
}
