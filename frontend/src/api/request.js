import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import store from '@/store'
import router from '@/router'
import { API_CONFIG } from './config'

// 创建axios实例
const service = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: API_CONFIG.HEADERS
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加认证token
    const token = store.getters.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 打印请求信息（开发环境）
    if (process.env.NODE_ENV === 'development') {
      console.log('🚀 API Request:', {
        url: config.url,
        method: config.method,
        data: config.data,
        params: config.params
      })
    }
    
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 打印响应信息（开发环境）
    if (process.env.NODE_ENV === 'development') {
      console.log('📥 API Response:', {
        url: response.config.url,
        status: response.status,
        data: res
      })
    }
    
    // 检查响应状态码
    if (res.code === API_CONFIG.STATUS_CODES.SUCCESS) {
      return res.data // 直接返回数据部分
    }
    
    // 处理业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    console.error('Response Error:', error)
    
    const { response } = error
    let message = '网络错误'
    
    if (response) {
      switch (response.status) {
        case API_CONFIG.STATUS_CODES.UNAUTHORIZED:
          message = '登录已过期，请重新登录'
          store.dispatch('logout')
          router.push('/login')
          break
        case API_CONFIG.STATUS_CODES.FORBIDDEN:
          message = '没有权限访问'
          break
        case API_CONFIG.STATUS_CODES.NOT_FOUND:
          message = '请求的资源不存在'
          break
        case API_CONFIG.STATUS_CODES.SERVER_ERROR:
          message = '服务器内部错误'
          break
        default:
          message = response.data?.message || `请求失败(${response.status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (error.message === 'Network Error') {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service