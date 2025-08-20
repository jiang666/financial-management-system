import request from './request'
import { API_ENDPOINTS } from './config'

// 科目管理API
export const accountsAPI = {
  // 获取科目列表
  getAccounts(params) {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.LIST,
      method: 'get',
      params
    })
  },
  
  // 创建科目
  createAccount(data) {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.CREATE,
      method: 'post',
      data
    })
  },
  
  // 更新科目
  updateAccount(id, data) {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.UPDATE(id),
      method: 'put',
      data
    })
  },
  
  // 删除科目
  deleteAccount(id) {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.DELETE(id),
      method: 'delete'
    })
  },
  
  // 获取科目详情
  getAccountDetail(id) {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.DETAIL(id),
      method: 'get'
    })
  },
  
  // 获取科目树
  getAccountsTree() {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.TREE,
      method: 'get'
    })
  },
  
  // 搜索科目
  searchAccounts(keyword) {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.SEARCH,
      method: 'get',
      params: { keyword }
    })
  },
  
  // 生成科目编码
  generateAccountCode(parentCode) {
    return request({
      url: API_ENDPOINTS.ACCOUNTS.GENERATE_CODE,
      method: 'post',
      data: { parentCode }
    })
  }
}