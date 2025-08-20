// API配置
export const API_CONFIG = {
  // 后端API基础URL (开发环境使用代理，生产环境使用完整URL)
  BASE_URL: process.env.NODE_ENV === 'production' ? 'http://localhost:8080/api' : '/api',
  
  // 请求超时时间
  TIMEOUT: 10000,
  
  // 请求头配置
  HEADERS: {
    'Content-Type': 'application/json',
  },
  
  // 响应状态码
  STATUS_CODES: {
    SUCCESS: 200,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    SERVER_ERROR: 500
  }
}

// API端点配置
export const API_ENDPOINTS = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    ME: '/auth/me',
    CHANGE_PASSWORD: '/auth/change-password'
  },
  
  // 用户管理
  USERS: {
    LIST: '/users',
    CREATE: '/users',
    UPDATE: (id) => `/users/${id}`,
    DELETE: (id) => `/users/${id}`,
    DETAIL: (id) => `/users/${id}`,
    RESET_PASSWORD: (id) => `/users/${id}/reset-password`
  },
  
  // 角色权限
  ROLES: {
    LIST: '/roles',
    CREATE: '/roles',
    UPDATE: (id) => `/roles/${id}`,
    DELETE: (id) => `/roles/${id}`,
    PERMISSIONS: '/roles/permissions'
  },
  
  // 科目管理
  ACCOUNTS: {
    LIST: '/accounts',
    CREATE: '/accounts',
    UPDATE: (id) => `/accounts/${id}`,
    DELETE: (id) => `/accounts/${id}`,
    DETAIL: (id) => `/accounts/${id}`,
    TREE: '/accounts/tree',
    SEARCH: '/accounts/search',
    GENERATE_CODE: '/accounts/generate-code'
  },
  
  // 期初余额
  INITIAL_BALANCES: {
    LIST: '/initial-balances',
    CREATE: '/initial-balances',
    UPDATE: (id) => `/initial-balances/${id}`,
    DELETE: (id) => `/initial-balances/${id}`,
    TRIAL_BALANCE: (year) => `/initial-balances/trial-balance/${year}`,
    CONFIRM: (year) => `/initial-balances/confirm/${year}`
  },
  
  // 系统健康检查
  HEALTH: {
    CHECK: '/health/check'
  }
}