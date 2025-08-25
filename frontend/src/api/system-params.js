import request from './request'

// 系统参数API
export const systemParamApi = {
  // 获取参数列表
  getParameters(params) {
    return request({
      url: '/v1/system/parameters',
      method: 'get',
      params: {
        category: params?.category,
        keyword: params?.keyword,
        isSystem: params?.isSystem,
        visible: params?.visible,
        page: params?.page || 0,
        size: params?.size || 20
      }
    })
  },
  
  // 获取单个参数
  getParameter(key) {
    return request({
      url: `/v1/system/parameters/${key}`,
      method: 'get'
    })
  },
  
  // 更新参数
  updateParameter(key, data) {
    return request({
      url: `/v1/system/parameters/${key}`,
      method: 'put',
      data
    })
  },
  
  // 批量更新参数
  batchUpdate(parameters) {
    return request({
      url: '/v1/system/parameters/batch',
      method: 'post',
      data: { parameters }
    })
  },
  
  // 获取参数分类
  getCategories() {
    return request({
      url: '/v1/system/parameters/categories',
      method: 'get'
    })
  },
  
  // 导出配置
  exportConfig(category) {
    return request({
      url: '/v1/system/parameters/export',
      method: 'get',
      params: { category },
      responseType: 'blob'
    })
  },
  
  // 导入配置
  importConfig(file, overwrite = false) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/v1/system/parameters/import',
      method: 'post',
      params: { overwrite },
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 获取参数修改历史
  getParameterHistory(key, params) {
    return request({
      url: `/v1/system/parameters/${key}/history`,
      method: 'get',
      params: {
        startTime: params?.startTime,
        endTime: params?.endTime,
        page: params?.page || 0,
        size: params?.size || 20
      }
    })
  },
  
  // 验证参数值
  validateParameter(data) {
    return request({
      url: '/v1/system/parameters/validate',
      method: 'post',
      data
    })
  },
  
  // 刷新参数缓存
  refreshCache(category) {
    return request({
      url: '/v1/system/parameters/refresh',
      method: 'post',
      params: { category }
    })
  }
}