// 用户权限页面 - API集成示例
// 这个文件展示如何将mock数据替换为真实API调用

import { usersAPI, rolesAPI } from '@/api'
import { ElMessage } from 'element-plus'

// ========== 用户管理相关 ==========

// 加载用户列表
export const loadUsers = async (params = {}) => {
  try {
    const response = await usersAPI.getUsers({
      page: params.page || 0,
      size: params.size || 10,
      username: params.username,
      realName: params.realName,
      status: params.status
    })
    return {
      data: response.content || [],
      total: response.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    throw error
  }
}

// 创建用户
export const createUser = async (userData) => {
  try {
    await usersAPI.createUser({
      username: userData.username,
      password: userData.password,
      email: userData.email,
      realName: userData.realName,
      phone: userData.phone,
      departmentId: userData.department,
      status: userData.status === '启用' ? 1 : 0
    })
    ElMessage.success('用户创建成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建用户失败')
    throw error
  }
}

// 更新用户
export const updateUser = async (userId, userData) => {
  try {
    await usersAPI.updateUser(userId, {
      email: userData.email,
      realName: userData.realName,
      phone: userData.phone,
      departmentId: userData.department,
      status: userData.status === '启用' ? 1 : 0
    })
    ElMessage.success('用户更新成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新用户失败')
    throw error
  }
}

// 删除用户
export const deleteUser = async (userId) => {
  try {
    await usersAPI.deleteUser(userId)
    ElMessage.success('用户删除成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '删除用户失败')
    throw error
  }
}

// 重置密码
export const resetUserPassword = async (userId, newPassword = '123456') => {
  try {
    await usersAPI.resetPassword(userId, newPassword)
    ElMessage.success(`密码重置成功，新密码为：${newPassword}`)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '重置密码失败')
    throw error
  }
}

// ========== 角色管理相关 ==========

// 加载角色列表
export const loadRoles = async (params = {}) => {
  try {
    const response = await rolesAPI.getRoles({
      page: params.page || 0,
      size: params.size || 10
    })
    return {
      data: response.content || [],
      total: response.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
    throw error
  }
}

// 获取所有权限
export const loadPermissions = async () => {
  try {
    const response = await rolesAPI.getPermissions()
    return response || []
  } catch (error) {
    ElMessage.error('加载权限列表失败')
    throw error
  }
}

// ========== 数据转换工具 ==========

// 将后端用户数据转换为前端格式
export const transformUserData = (backendUser) => {
  return {
    id: backendUser.id,
    username: backendUser.username,
    realName: backendUser.realName,
    role: backendUser.roles?.[0]?.name || '',
    department: backendUser.departmentName || '',
    email: backendUser.email,
    phone: backendUser.phone,
    status: backendUser.status === 1 ? '启用' : '停用',
    lastLogin: backendUser.lastLoginAt
  }
}

// 将后端角色数据转换为前端格式
export const transformRoleData = (backendRole) => {
  return {
    id: backendRole.id,
    name: backendRole.name,
    code: backendRole.code,
    description: backendRole.description,
    userCount: backendRole.userCount || 0,
    status: '启用' // 后端暂无状态字段
  }
}

// ========== 使用示例 ==========
/*
// 在组件中使用:
import { loadUsers, createUser, updateUser, deleteUser } from './api-integration'

// 加载用户数据
const loadUserData = async () => {
  userLoading.value = true
  try {
    const result = await loadUsers({
      page: currentPage.value - 1,
      size: pageSize.value,
      ...userSearchForm.value
    })
    userData.value = result.data.map(transformUserData)
    totalUsers.value = result.total
  } finally {
    userLoading.value = false
  }
}

// 保存用户
const saveUser = async () => {
  const valid = await userFormRef.value.validate()
  if (!valid) return
  
  try {
    if (userForm.value.id) {
      await updateUser(userForm.value.id, userForm.value)
    } else {
      await createUser(userForm.value)
    }
    userDialog.value = false
    await loadUserData()
  } catch (error) {
    console.error('保存用户失败:', error)
  }
}
*/