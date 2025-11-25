import request from '@/utils/request'

// 查詢任務類型列表
export function listJobTypes(query) {
  return request({
    url: '/monitor/jobType/list',
    method: 'get',
    params: query
  })
}

// 根據代號取得任務類型
export function getJobTypeByCode(code) {
  return request({
    url: '/monitor/jobType/' + code,
    method: 'get'
  })
}

// 新增任務類型
export function addJobType(data) {
  return request({
    url: '/monitor/jobType',
    method: 'post',
    data: data
  })
}

// 修改任務類型
export function updateJobType(data) {
  return request({
    url: '/monitor/jobType',
    method: 'put',
    data: data
  })
}

// 刪除任務類型
export function delJobType(jobTypeId) {
  return request({
    url: '/monitor/jobType/' + jobTypeId,
    method: 'delete'
  })
}
