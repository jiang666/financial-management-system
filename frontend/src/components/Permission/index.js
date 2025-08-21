import PermissionButton from './PermissionButton.vue'

// 导出权限组件
export { PermissionButton }

// 权限检查工具函数
export function hasPermission(permission, store) {
  const authorities = store.getters.authorities
  const resources = store.getters.resources
  
  if (!authorities || authorities.length === 0) {
    return false
  }
  
  // 检查角色权限
  if (authorities.includes(permission)) {
    return true
  }
  
  // 检查资源权限
  if (resources) {
    return checkResourcePermission(permission, resources)
  }
  
  return false
}

/**
 * 递归检查资源权限
 */
function checkResourcePermission(permission, resource) {
  if (resource.resourceCode === permission || resource.resourceName === permission) {
    return true
  }
  
  if (resource.children && resource.children.length > 0) {
    return resource.children.some(child => checkResourcePermission(permission, child))
  }
  
  return false
}