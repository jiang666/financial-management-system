const TOKEN_KEY = 'financial-management-token'
const REFRESH_TOKEN_KEY = 'financial-management-refresh-token'
const USER_INFO_KEY = 'financial-management-user-info'
const USER_RESOURCES_KEY = 'financial-management-user-resources'

/**
 * 获取Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 * @param {string} token JWT Token
 */
export function setToken(token) {
  return localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除Token
 */
export function removeToken() {
  return localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取刷新Token
 */
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新Token
 * @param {string} refreshToken 刷新Token
 */
export function setRefreshToken(refreshToken) {
  return localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

/**
 * 移除刷新Token
 */
export function removeRefreshToken() {
  return localStorage.removeItem(REFRESH_TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * @param {Object} userInfo 用户信息
 */
export function setUserInfo(userInfo) {
  return localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  return localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取用户权限资源
 */
export function getUserResources() {
  const resources = localStorage.getItem(USER_RESOURCES_KEY)
  return resources ? JSON.parse(resources) : null
}

/**
 * 设置用户权限资源
 * @param {Object} resources 权限资源树
 */
export function setUserResources(resources) {
  return localStorage.setItem(USER_RESOURCES_KEY, JSON.stringify(resources))
}

/**
 * 移除用户权限资源
 */
export function removeUserResources() {
  return localStorage.removeItem(USER_RESOURCES_KEY)
}

/**
 * 清空所有认证信息
 */
export function clearAuth() {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
  removeUserResources()
}