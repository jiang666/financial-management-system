import request from '@/api/request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/v1/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/v1/users/${id}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/v1/users',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/v1/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/v1/users/${id}`,
    method: 'delete'
  })
}

// 批量删除用户
export function batchDeleteUsers(ids) {
  return request({
    url: '/v1/users/batch',
    method: 'delete',
    data: { ids }
  })
}

// 重置用户密码
export function resetUserPassword(id, password) {
  return request({
    url: `/v1/users/${id}/password`,
    method: 'put',
    data: { password }
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/v1/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 分配用户角色
export function assignUserRoles(id, roleIds) {
  return request({
    url: `/v1/users/${id}/roles`,
    method: 'put',
    data: { roleIds }
  })
}

// 移除用户角色
export function removeUserRoles(id, roleIds) {
  return request({
    url: `/v1/users/${id}/roles`,
    method: 'delete',
    data: { roleIds }
  })
}

// 导出用户数据
export function exportUsers(params) {
  return request({
    url: '/v1/users/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}