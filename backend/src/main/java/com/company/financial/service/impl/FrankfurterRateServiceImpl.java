package com.company.financial.service.impl;

import com.company.financial.service.ExternalRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Frankfurter外部汇率API服务实现
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Slf4j
@Service
public class FrankfurterRateServiceImpl implements ExternalRateService {
    
    private final RestTemplate restTemplate;
    
    // Frankfurter API配置
    private static final String API_BASE_URL = "https://api.frankfurter.app";
    private static final String LATEST_ENDPOINT = "/latest";
    private static final String CURRENCIES_ENDPOINT = "/currencies";
    
    // 支持的主要币种
    private static final String[] SUPPORTED_CURRENCIES = {
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD",
        "MXN", "SGD", "HKD", "NOK", "KRW", "TRY", "RUB", "INR", "BRL", "ZAR"
    };
    
    public FrankfurterRateServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public Map<String, BigDecimal> getRatesByBaseCurrency(String baseCurrencyCode) {
        try {
            String url = API_BASE_URL + LATEST_ENDPOINT + "?base=" + baseCurrencyCode;
            log.info("调用Frankfurter API: {}", url);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            log.info("Frankfurter API响应: {}", response);
            
            if (response != null && response.containsKey("rates")) {
                Map<String, BigDecimal> rates = (Map<String, BigDecimal>) response.get("rates");
                log.info("解析到汇率数量: {}", rates.size());
                return rates;
            } else {
                log.error("Invalid response from Frankfurter API: {}", response);
                throw new RuntimeException("外部API返回数据格式错误");
            }
            
        } catch (RestClientException e) {
            log.error("Failed to fetch rates from Frankfurter API for base currency: {}", baseCurrencyCode, e);
            throw new RuntimeException("调用外部汇率API失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calling Frankfurter API: {}", e.getMessage(), e);
            throw new RuntimeException("调用外部汇率API时发生未知错误: " + e.getMessage());
        }
    }
    
    @Override
    public BigDecimal getRate(String fromCurrencyCode, String toCurrencyCode) {
        try {
            String url = API_BASE_URL + LATEST_ENDPOINT + 
                        "?base=" + fromCurrencyCode + 
                        "&symbols=" + toCurrencyCode;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("rates")) {
                Map<String, BigDecimal> rates = (Map<String, BigDecimal>) response.get("rates");
                BigDecimal rate = rates.get(toCurrencyCode);
                
                if (rate != null) {
                    return rate;
                } else {
                    throw new RuntimeException("未获取到指定币种对的汇率");
                }
            } else {
                log.error("Invalid response from Frankfurter API: {}", response);
                throw new RuntimeException("外部API返回数据格式错误");
            }
            
        } catch (RestClientException e) {
            log.error("Failed to fetch rate from Frankfurter API for {} to {}", fromCurrencyCode, toCurrencyCode, e);
            throw new RuntimeException("调用外部汇率API失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isServiceAvailable() {
        try {
            String url = API_BASE_URL + CURRENCIES_ENDPOINT;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.warn("Frankfurter API service is not available: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String[] getSupportedCurrencies() {
        try {
            String url = API_BASE_URL + CURRENCIES_ENDPOINT;
            Map<String, String> currencies = restTemplate.getForObject(url, Map.class);
            
            if (currencies != null) {
                return currencies.keySet().toArray(new String[0]);
            } else {
                log.warn("Failed to fetch supported currencies from API, using default list");
                return SUPPORTED_CURRENCIES;
            }
            
        } catch (Exception e) {
            log.warn("Failed to fetch supported currencies from Frankfurter API: {}", e.getMessage());
            return SUPPORTED_CURRENCIES;
        }
    }
}