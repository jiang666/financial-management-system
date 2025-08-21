import request from '@/api/request'

// 获取角色列表（分页）
export function getRoleList(params) {
  return request({
    url: '/v1/roles',
    method: 'get',
    params
  })
}

// 获取所有角色（不分页）
export function getAllRoles() {
  return request({
    url: '/v1/roles/all',
    method: 'get'
  })
}

// 获取角色详情
export function getRoleDetail(id) {
  return request({
    url: `/v1/roles/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/v1/roles',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id, data) {
  return request({
    url: `/v1/roles/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/v1/roles/${id}`,
    method: 'delete'
  })
}

// 批量删除角色
export function batchDeleteRoles(ids) {
  return request({
    url: '/v1/roles/batch',
    method: 'delete',
    data: { ids }
  })
}

// 更新角色状态
export function updateRoleStatus(id, status) {
  return request({
    url: `/v1/roles/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 配置角色权限
export function configRolePermissions(id, resourceIds) {
  return request({
    url: `/v1/roles/${id}/permissions`,
    method: 'put',
    data: { resourceIds }
  })
}

// 获取角色权限
export function getRolePermissions(id) {
  return request({
    url: `/v1/roles/${id}/permissions`,
    method: 'get'
  })
}