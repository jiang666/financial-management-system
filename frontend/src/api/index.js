// API统一导出
export { authAPI } from './auth'
export { usersAPI } from './users'
export { accountsAPI } from './accounts'

// 健康检查API
import request from './request'
import { API_ENDPOINTS } from './config'

export const healthAPI = {
  check() {
    return request({
      url: API_ENDPOINTS.HEALTH.CHECK,
      method: 'get'
    })
  }
}