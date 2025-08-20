import request from './request'
import { API_ENDPOINTS } from './config'

// 认证相关API
export const authAPI = {
  // 用户登录
  login(data) {
    return request({
      url: API_ENDPOINTS.AUTH.LOGIN,
      method: 'post',
      data
    })
  },
  
  // 用户注册
  register(data) {
    return request({
      url: API_ENDPOINTS.AUTH.REGISTER,
      method: 'post',
      data
    })
  },
  
  // 用户登出
  logout() {
    return request({
      url: API_ENDPOINTS.AUTH.LOGOUT,
      method: 'post'
    })
  },
  
  // 刷新令牌
  refreshToken(refreshToken) {
    return request({
      url: API_ENDPOINTS.AUTH.REFRESH,
      method: 'post',
      data: { refreshToken }
    })
  },
  
  // 获取当前用户信息
  getCurrentUser() {
    return request({
      url: API_ENDPOINTS.AUTH.ME,
      method: 'get'
    })
  },
  
  // 修改密码
  changePassword(data) {
    return request({
      url: API_ENDPOINTS.AUTH.CHANGE_PASSWORD,
      method: 'post',
      data
    })
  }
}