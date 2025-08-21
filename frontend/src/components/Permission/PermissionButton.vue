<template>
  <span v-if="hasPermission">
    <slot></slot>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const props = defineProps({
  // 权限码，可以是单个字符串或数组
  permission: {
    type: [String, Array],
    required: true
  },
  // 权限验证模式：'any' 表示满足任一权限即可，'all' 表示需要满足所有权限
  mode: {
    type: String,
    default: 'any',
    validator: (value) => ['any', 'all'].includes(value)
  }
})

const store = useStore()

const hasPermission = computed(() => {
  const authorities = store.getters.authorities
  const resources = store.getters.resources
  
  if (!authorities || authorities.length === 0) {
    return false
  }
  
  // 获取权限列表
  const permissions = Array.isArray(props.permission) ? props.permission : [props.permission]
  
  if (props.mode === 'all') {
    // 需要满足所有权限
    return permissions.every(permission => checkPermission(permission, authorities, resources))
  } else {
    // 满足任一权限即可
    return permissions.some(permission => checkPermission(permission, authorities, resources))
  }
})

/**
 * 检查权限
 * @param {string} permission 权限码
 * @param {Array} authorities 用户权限列表
 * @param {Object} resources 用户资源树
 */
function checkPermission(permission, authorities, resources) {
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
 * @param {string} permission 权限码
 * @param {Object} resource 资源节点
 */
function checkResourcePermission(permission, resource) {
  // 检查当前资源节点
  if (resource.resourceCode === permission || resource.resourceName === permission) {
    return true
  }
  
  // 递归检查子资源
  if (resource.children && resource.children.length > 0) {
    return resource.children.some(child => checkResourcePermission(permission, child))
  }
  
  return false
}
</script>