package com.company.financial.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 外部汇率API服务接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
public interface ExternalRateService {
    
    /**
     * 获取指定基准币种的所有汇率
     */
    Map<String, BigDecimal> getRatesByBaseCurrency(String baseCurrencyCode);
    
    /**
     * 获取指定币种对的汇率
     */
    BigDecimal getRate(String fromCurrencyCode, String toCurrencyCode);
    
    /**
     * 检查外部API服务是否可用
     */
    boolean isServiceAvailable();
    
    /**
     * 获取支持的币种列表
     */
    String[] getSupportedCurrencies();
}