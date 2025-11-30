import request from '@/utils/request'

// 查詢任務類型列表（取得所有任務類型選項）
export function listJobTypes(query) {
  return request({
    url: '/monitor/job/types',
    method: 'get',
    params: query
  })
}

// 根據分類取得任務類型
export function getTaskTypesByCategory(category) {
  return request({
    url: `/monitor/job/types/category/${category}`,
    method: 'get'
  })
}

// 根據代號取得任務類型（注意：後端沒有此 API，保留以防未來使用）
export function getJobTypeByCode(code) {
  return request({
    url: `/monitor/job/types/${code}`,
    method: 'get'
  })
}

// 新增任務類型（注意：後端沒有此 API，保留以防未來使用）
export function addJobType(data) {
  return request({
    url: '/monitor/job/taskTypes',
    method: 'post',
    data: data
  })
}

// 修改任務類型（注意：後端沒有此 API，保留以防未來使用）
export function updateJobType(data) {
  return request({
    url: '/monitor/job/taskTypes',
    method: 'put',
    data: data
  })
}

// 刪除任務類型（注意：後端沒有此 API，保留以防未來使用）
export function delJobType(jobTypeId) {
  return request({
    url: '/monitor/job/taskTypes/' + jobTypeId,
    method: 'delete'
  })
}
