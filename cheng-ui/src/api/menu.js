import request from '@/utils/request'

// 取得路由
export const getRouters = () => {
  return request({
    url: '/getRouters',
    method: 'get'
  })
}
