import request from '@/api/request'

// 币种管理接口
export const currencyApi = {
  // 分页查询币种列表
  getCurrencyList(params) {
    return request({
      url: '/v1/currencies/list',
      method: 'post',
      data: params
    })
  },

  // 获取所有币种列表
  getAllCurrencies() {
    return request({
      url: '/v1/currencies/all',
      method: 'get'
    })
  },

  // 根据ID获取币种详情
  getCurrencyById(id) {
    return request({
      url: `/v1/currencies/${id}`,
      method: 'get'
    })
  },

  // 根据代码获取币种详情
  getCurrencyByCode(code) {
    return request({
      url: `/v1/currencies/code/${code}`,
      method: 'get'
    })
  },

  // 创建币种
  createCurrency(data) {
    return request({
      url: '/v1/currencies',
      method: 'post',
      data
    })
  },

  // 更新币种
  updateCurrency(id, data) {
    return request({
      url: `/v1/currencies/${id}`,
      method: 'put',
      data
    })
  },

  // 启用/禁用币种
  toggleCurrencyStatus(id, status) {
    return request({
      url: `/v1/currencies/${id}/status/${status}`,
      method: 'put'
    })
  },

  // 设置基准币种
  setBaseCurrency(id) {
    return request({
      url: `/v1/currencies/${id}/set-base`,
      method: 'put'
    })
  },

  // 删除币种
  deleteCurrency(id) {
    return request({
      url: `/v1/currencies/${id}`,
      method: 'delete'
    })
  },

  // 获取基准币种
  getBaseCurrency() {
    return request({
      url: '/v1/currencies/base',
      method: 'get'
    })
  },

  // 检查币种代码是否存在
  checkCurrencyCode(code) {
    return request({
      url: `/v1/currencies/exists/${code}`,
      method: 'get'
    })
  }
}

// 汇率管理接口
export const exchangeRateApi = {
  // 获取最新汇率
  getLatestRate(fromCurrencyId, toCurrencyId) {
    return request({
      url: '/v1/exchange-rates/latest',
      method: 'get',
      params: { fromCurrencyId, toCurrencyId }
    })
  },

  // 批量获取当前汇率
  getCurrentRates(baseCurrencyId) {
    return request({
      url: `/v1/exchange-rates/current/${baseCurrencyId}`,
      method: 'get'
    })
  },

  // 分页查询汇率历史
  getRateHistory(params) {
    return request({
      url: '/v1/exchange-rates/history',
      method: 'post',
      data: params
    })
  },

  // 获取指定币种对的汇率历史
  getRateHistoryByCurrencyPair(fromCurrencyId, toCurrencyId) {
    return request({
      url: `/v1/exchange-rates/history/${fromCurrencyId}/${toCurrencyId}`,
      method: 'get'
    })
  },

  // 手动更新汇率
  updateRate(fromCurrencyId, toCurrencyId, data) {
    return request({
      url: `/v1/exchange-rates/${fromCurrencyId}/${toCurrencyId}`,
      method: 'put',
      data
    })
  },

  // 从外部API同步汇率
  syncRatesFromExternalAPI() {
    return request({
      url: '/v1/exchange-rates/sync',
      method: 'post'
    })
  },

  // 同步指定币种的汇率
  syncRateForCurrency(currencyCode) {
    return request({
      url: `/v1/exchange-rates/sync/${currencyCode}`,
      method: 'post'
    })
  },

  // 获取汇率同步状态
  getLastSyncStatus() {
    return request({
      url: '/v1/exchange-rates/sync/status',
      method: 'get'
    })
  }
}