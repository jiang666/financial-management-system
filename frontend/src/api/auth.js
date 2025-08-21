import request from './request'

/**
 * 用户登录
 * @param {Object} data 登录数据
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @param {boolean} data.rememberMe 记住我
 */
export function login(data) {
  return request({
    url: '/v1/auth/login',
    method: 'post',
    data: {
      username: data.username,
      password: data.password,
      rememberMe: data.rememberMe || false
    }
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/v1/auth/logout',
    method: 'post'
  })
}

/**
 * 刷新Token
 * @param {string} refreshToken 刷新令牌
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/v1/auth/refresh',
    method: 'post',
    data: {
      refreshToken
    }
  })
}

/**
 * 修改密码
 * @param {Object} data 密码数据
 * @param {string} data.oldPassword 原密码
 * @param {string} data.newPassword 新密码
 * @param {string} data.confirmPassword 确认密码
 */
export function changePassword(data) {
  return request({
    url: '/v1/auth/password',
    method: 'put',
    data
  })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/v1/auth/me',
    method: 'get'
  })
}

/**
 * 获取当前用户权限资源
 */
export function getCurrentUserResources() {
  return request({
    url: '/v1/auth/resources',
    method: 'get'
  })
}

/**
 * 验证Token是否有效
 */
export function validateToken() {
  return request({
    url: '/v1/auth/validate',
    method: 'get'
  })
}