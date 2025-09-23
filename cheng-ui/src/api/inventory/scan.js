import request from '@/utils/request'

// 掃描物品
export function scanItem(data) {
  return request({
    url: '/inventory/item/scan',
    method: 'post',
    data: data
  })
}

// 獲取掃描歷史
export function getScanHistory(query) {
  return request({
    url: '/inventory/scan/history',
    method: 'get',
    params: query
  })
}

// 清除掃描歷史
export function clearScanHistory() {
  return request({
    url: '/inventory/scan/history',
    method: 'delete'
  })
}
