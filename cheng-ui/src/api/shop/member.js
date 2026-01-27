import request from '@/utils/request'

// 查詢會員列表
export function listMember(query) {
  return request({
    url: '/shop/member/list',
    method: 'get',
    params: query
  })
}

// 查詢會員詳細
export function getMember(memberId) {
  return request({
    url: '/shop/member/' + memberId,
    method: 'get'
  })
}

// 新增會員
export function addMember(data) {
  return request({
    url: '/shop/member',
    method: 'post',
    data: data
  })
}

// 修改會員
export function updateMember(data) {
  return request({
    url: '/shop/member',
    method: 'put',
    data: data
  })
}

// 刪除會員
export function delMember(memberId) {
  return request({
    url: '/shop/member/' + memberId,
    method: 'delete'
  })
}

// 更新會員狀態
export function updateMemberStatus(memberId, status) {
  return request({
    url: '/shop/member/status',
    method: 'put',
    data: { memberId, status }
  })
}

// 調整會員積分
export function adjustPoints(memberId, points, type) {
  return request({
    url: '/shop/member/points',
    method: 'put',
    data: { memberId, points, type }
  })
}
