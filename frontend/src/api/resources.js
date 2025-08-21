import request from '@/api/request'

// 获取资源树
export function getResourceTree() {
  return request({
    url: '/v1/resources/tree',
    method: 'get'
  })
}

// 获取用户菜单
export function getUserMenus(userId) {
  return request({
    url: `/v1/resources/user/${userId}/menus`,
    method: 'get'
  })
}

// 获取用户权限
export function getUserPermissions(userId) {
  return request({
    url: `/v1/resources/user/${userId}/permissions`,
    method: 'get'
  })
}

// 检查用户是否有指定权限
export function checkUserPermission(userId, permission) {
  return request({
    url: `/v1/resources/user/${userId}/permission/${permission}`,
    method: 'get'
  })
}

// 按类型查询资源
export function getResourcesByType(type) {
  return request({
    url: `/v1/resources/type/${type}`,
    method: 'get'
  })
}