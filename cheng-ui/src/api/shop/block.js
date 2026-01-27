import request from '@/utils/request'

// 查詢區塊列表
export function listBlock(query) {
  return request({
    url: '/shop/block/list',
    method: 'get',
    params: query
  })
}

// 查詢區塊詳細
export function getBlock(blockId) {
  return request({
    url: '/shop/block/' + blockId,
    method: 'get'
  })
}

// 根據代碼查詢區塊
export function getBlockByCode(blockCode) {
  return request({
    url: '/shop/block/code/' + blockCode,
    method: 'get'
  })
}

// 新增區塊
export function addBlock(data) {
  return request({
    url: '/shop/block',
    method: 'post',
    data: data
  })
}

// 修改區塊
export function updateBlock(data) {
  return request({
    url: '/shop/block',
    method: 'put',
    data: data
  })
}

// 刪除區塊
export function delBlock(blockId) {
  return request({
    url: '/shop/block/' + blockId,
    method: 'delete'
  })
}
