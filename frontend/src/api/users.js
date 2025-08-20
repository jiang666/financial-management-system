import request from './request'
import { API_ENDPOINTS } from './config'

// 用户管理API
export const usersAPI = {
  // 获取用户列表
  getUsers(params) {
    return request({
      url: API_ENDPOINTS.USERS.LIST,
      method: 'get',
      params
    })
  },
  
  // 创建用户
  createUser(data) {
    return request({
      url: API_ENDPOINTS.USERS.CREATE,
      method: 'post',
      data
    })
  },
  
  // 更新用户
  updateUser(id, data) {
    return request({
      url: API_ENDPOINTS.USERS.UPDATE(id),
      method: 'put',
      data
    })
  },
  
  // 删除用户
  deleteUser(id) {
    return request({
      url: API_ENDPOINTS.USERS.DELETE(id),
      method: 'delete'
    })
  },
  
  // 获取用户详情
  getUserDetail(id) {
    return request({
      url: API_ENDPOINTS.USERS.DETAIL(id),
      method: 'get'
    })
  },
  
  // 重置用户密码
  resetPassword(id, newPassword) {
    return request({
      url: API_ENDPOINTS.USERS.RESET_PASSWORD(id),
      method: 'post',
      data: { newPassword }
    })
  }
}