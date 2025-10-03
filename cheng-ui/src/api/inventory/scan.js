import request from '@/utils/request'

// 提交掃描結果
export function submitScan(data) {
  return request({
    url: '/inventory/scan/submit',
    method: 'post',
    data: data
  })
}
