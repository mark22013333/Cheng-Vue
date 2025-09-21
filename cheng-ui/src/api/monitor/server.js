import request from '@/utils/request'

// 取得服務訊息
export function getServer() {
  return request({
    url: '/monitor/server',
    method: 'get'
  })
}
