package com.company.financial.service;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.currency.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 汇率管理Service接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
public interface ExchangeRateService {
    
    /**
     * 获取最新汇率
     */
    BigDecimal getLatestRate(String fromCurrencyId, String toCurrencyId);
    
    /**
     * 批量获取当前汇率
     */
    List<ExchangeRateHistoryDTO> getCurrentRates(String baseCurrencyId);
    
    /**
     * 分页查询汇率历史
     */
    ResponsePageDataEntity<ExchangeRateHistoryDTO> getRateHistory(ExchangeRateQueryDTO queryDTO);
    
    /**
     * 获取指定币种对的汇率历史
     */
    List<ExchangeRateHistoryDTO> getRateHistoryByCurrencyPair(String fromCurrencyId, String toCurrencyId);
    
    /**
     * 手动更新汇率
     */
    void updateRate(String fromCurrencyId, String toCurrencyId, RateUpdateDTO updateDTO);
    
    /**
     * 从外部API同步汇率
     */
    RateSyncDTO syncRatesFromExternalAPI();
    
    /**
     * 同步指定币种的汇率
     */
    void syncRateForCurrency(String currencyCode);
    
    /**
     * 删除币种相关的所有汇率记录
     */
    void deleteRatesByCurrency(String currencyId);
    
    /**
     * 获取汇率同步状态
     */
    RateSyncDTO getLastSyncStatus();
}