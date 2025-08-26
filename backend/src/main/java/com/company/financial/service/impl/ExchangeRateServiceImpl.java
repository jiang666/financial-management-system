package com.company.financial.service.impl;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.util.UUIDUtils;
import com.company.financial.dto.currency.*;
import com.company.financial.entity.Currency;
import com.company.financial.entity.ExchangeRate;
import com.company.financial.repository.CurrencyRepository;
import com.company.financial.repository.ExchangeRateRepository;
import com.company.financial.service.ExchangeRateService;
import com.company.financial.service.ExternalRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 汇率管理Service实现
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {
    
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final ExternalRateService externalRateService;
    
    @Override
    // @Cacheable(value = "exchange_rates", key = "#fromCurrencyId + '_' + #toCurrencyId")
    public BigDecimal getLatestRate(String fromCurrencyId, String toCurrencyId) {
        Long currentTime = System.currentTimeMillis();
        Optional<ExchangeRate> rateOpt = exchangeRateRepository.findLatestRate(
            fromCurrencyId, toCurrencyId, currentTime);
        
        if (rateOpt.isPresent()) {
            BigDecimal rate = rateOpt.get().getRate();
            log.info("获取汇率: fromCurrencyId={}, toCurrencyId={}, rate={}", 
                fromCurrencyId, toCurrencyId, rate.toPlainString());
            return rate;
        }
        
        // 如果没有找到汇率，尝试反向查找
        Optional<ExchangeRate> reverseRateOpt = exchangeRateRepository.findLatestRate(
            toCurrencyId, fromCurrencyId, currentTime);
        
        if (reverseRateOpt.isPresent()) {
            BigDecimal reverseRate = reverseRateOpt.get().getRate();
            BigDecimal rate = BigDecimal.ONE.divide(reverseRate, 8, RoundingMode.HALF_UP);
            log.info("获取反向汇率: fromCurrencyId={}, toCurrencyId={}, reverseRate={}, rate={}", 
                fromCurrencyId, toCurrencyId, reverseRate.toPlainString(), rate.toPlainString());
            return rate;
        }
        
        throw new IllegalArgumentException("未找到相关汇率信息");
    }
    
    @Override
    public List<ExchangeRateHistoryDTO> getCurrentRates(String baseCurrencyId) {
        List<ExchangeRate> rates = exchangeRateRepository.findByCurrencyId(baseCurrencyId);
        return rates.stream()
            .map(this::convertToHistoryDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public ResponsePageDataEntity<ExchangeRateHistoryDTO> getRateHistory(ExchangeRateQueryDTO queryDTO) {
        try {
            log.info("查询汇率历史: queryDTO={}", queryDTO);
            
            // 构建排序
            Sort sort = Sort.by(Sort.Direction.DESC, "effectiveDate");
            if (queryDTO.getSort() != null && !queryDTO.getSort().isEmpty()) {
                String[] sortParts = queryDTO.getSort().split(",");
                if (sortParts.length == 2) {
                    Sort.Direction direction = "asc".equalsIgnoreCase(sortParts[1]) ? 
                        Sort.Direction.ASC : Sort.Direction.DESC;
                    // 将 snake_case 转换为 camelCase
                    String fieldName = sortParts[0];
                    if ("effective_date".equals(fieldName)) {
                        fieldName = "effectiveDate";
                    }
                    sort = Sort.by(direction, fieldName);
                }
            }
            
            // 如果提供了currencyCode，需要先转换为currencyId
            String currencyIdForQuery = queryDTO.getCurrencyId();
            if (currencyIdForQuery == null && queryDTO.getCurrencyCode() != null) {
                Currency currency = currencyRepository.findByCode(queryDTO.getCurrencyCode()).orElse(null);
                if (currency != null) {
                    currencyIdForQuery = currency.getId();
                }
            }
            
            Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);
            Page<ExchangeRate> ratePage = exchangeRateRepository.findRateHistory(
                currencyIdForQuery,
                queryDTO.getStartDate(),
                queryDTO.getEndDate(),
                queryDTO.getSource(),
                pageable
            );
            
            List<ExchangeRateHistoryDTO> rateDTOs = ratePage.getContent().stream()
                .map(this::convertToHistoryDTO)
                .collect(Collectors.toList());
            
            ResponsePageDataEntity<ExchangeRateHistoryDTO> response = new ResponsePageDataEntity<>();
            response.setContent(rateDTOs);
            response.setTotalElements(ratePage.getTotalElements());
            response.setTotalPages(ratePage.getTotalPages());
            response.setSize(ratePage.getSize());
            response.setNumber(ratePage.getNumber());
            response.setNumberOfElements(ratePage.getNumberOfElements());
            response.setFirst(ratePage.isFirst());
            response.setLast(ratePage.isLast());
            
            log.info("查询汇率历史成功: 返回{}条记录", rateDTOs.size());
            return response;
        } catch (Exception e) {
            log.error("查询汇率历史失败", e);
            throw new RuntimeException("查询汇率历史失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<ExchangeRateHistoryDTO> getRateHistoryByCurrencyPair(String fromCurrencyId, String toCurrencyId) {
        List<ExchangeRate> rates = exchangeRateRepository.findRateHistoryByCurrencyPair(fromCurrencyId, toCurrencyId);
        return rates.stream()
            .map(this::convertToHistoryDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "exchange_rates", allEntries = true)
    public void updateRate(String fromCurrencyId, String toCurrencyId, RateUpdateDTO updateDTO) {
        log.info("更新汇率: fromCurrencyId={}, toCurrencyId={}, rate={}", 
            fromCurrencyId, toCurrencyId, updateDTO.getRate().toPlainString());
        
        // 验证币种是否存在
        Currency fromCurrency = currencyRepository.findById(fromCurrencyId)
            .orElseThrow(() -> new IllegalArgumentException("源币种不存在"));
        Currency toCurrency = currencyRepository.findById(toCurrencyId)
            .orElseThrow(() -> new IllegalArgumentException("目标币种不存在"));
        
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setId(UUIDUtils.generate());
        exchangeRate.setFromCurrencyId(fromCurrencyId);
        exchangeRate.setToCurrencyId(toCurrencyId);
        exchangeRate.setRate(updateDTO.getRate());
        exchangeRate.setEffectiveDate(updateDTO.getEffectiveDate() != null ? 
            updateDTO.getEffectiveDate() : System.currentTimeMillis());
        exchangeRate.setExpiryDate(updateDTO.getExpiryDate());
        exchangeRate.setSource(updateDTO.getSource() != null ? updateDTO.getSource() : "MANUAL");
        exchangeRate.setRemark(updateDTO.getRemark());
        exchangeRate.setStatus(1);
        exchangeRate.setDeleted(0);
        
        Long currentTime = System.currentTimeMillis();
        String currentUser = getCurrentUser();
        exchangeRate.setCreatedAt(currentTime);
        exchangeRate.setUpdatedAt(currentTime);
        exchangeRate.setCreatedBy(currentUser);
        exchangeRate.setUpdatedBy(currentUser);
        
        exchangeRateRepository.save(exchangeRate);
        log.info("汇率保存成功: id={}, rate={}", 
            exchangeRate.getId(), exchangeRate.getRate().toPlainString());
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "exchange_rates", allEntries = true)
    public RateSyncDTO syncRatesFromExternalAPI() {
        RateSyncDTO syncResult = new RateSyncDTO();
        syncResult.setSyncTime(System.currentTimeMillis());
        syncResult.setSource("FRANKFURTER");
        
        try {
            // 获取基准币种
            Currency baseCurrency = currencyRepository.findBaseCurrency()
                .orElseThrow(() -> new IllegalArgumentException("未设置基准币种"));
            log.info("获取到基准币种: {}", baseCurrency.getCode());
            
            // 获取所有活跃币种
            List<Currency> activeCurrencies = currencyRepository.findAllActiveCurrencies();
            log.info("获取到活跃币种数量: {}, 币种列表: {}", 
                activeCurrencies.size(),
                activeCurrencies.stream().map(Currency::getCode).toArray());
            
            // 调用外部API获取汇率
            log.info("正在调用外部API获取汇率...");
            Map<String, BigDecimal> rates = externalRateService.getRatesByBaseCurrency(baseCurrency.getCode());
            log.info("外部API返回汇率数量: {}, 汇率数据: {}", 
                rates != null ? rates.size() : 0, rates);
                
            int successCount = 0;
            int failCount = 0;
            
            for (Currency currency : activeCurrencies) {
                log.info("处理币种: {}, isBase: {}", currency.getCode(), currency.getIsBase());
                if (currency.getIsBase() == 1) {
                    log.info("跳过基准币种: {}", currency.getCode());
                    continue; // 跳过基准币种
                }
                
                try {
                    BigDecimal rate = rates.get(currency.getCode());
                    log.info("币种 {} 对应汇率: {}", currency.getCode(), rate);
                    if (rate != null) {
                            // 创建汇率记录
                            ExchangeRate exchangeRate = new ExchangeRate();
                            exchangeRate.setId(UUIDUtils.generate());
                            exchangeRate.setFromCurrencyId(baseCurrency.getId());
                            exchangeRate.setToCurrencyId(currency.getId());
                            exchangeRate.setRate(rate);
                            exchangeRate.setEffectiveDate(System.currentTimeMillis());
                            exchangeRate.setSource("FRANKFURTER");
                            exchangeRate.setRemark("自动同步");
                            exchangeRate.setStatus(1);
                            exchangeRate.setDeleted(0);
                            
                            Long currentTime = System.currentTimeMillis();
                            String currentUser = getCurrentUser();
                            exchangeRate.setCreatedAt(currentTime);
                            exchangeRate.setUpdatedAt(currentTime);
                            exchangeRate.setCreatedBy(currentUser);
                            exchangeRate.setUpdatedBy(currentUser);
                            
                            ExchangeRate savedRate = exchangeRateRepository.save(exchangeRate);
                            log.info("成功保存汇率: {} -> {}, rate: {}, exchangeRateId: {}", 
                                baseCurrency.getCode(), currency.getCode(), rate.toPlainString(), savedRate.getId());
                            successCount++;
                        } else {
                            log.warn("币种 {} 未获取到汇率", currency.getCode());
                            failCount++;
                        }
                    } catch (Exception e) {
                        log.error("处理币种 {} 时发生异常: {}", currency.getCode(), e.getMessage(), e);
                        failCount++;
                    }
            }
            
            syncResult.setBaseCurrencyCode(baseCurrency.getCode());
            syncResult.setRates(rates);
            syncResult.setSuccessCount(successCount);
            syncResult.setFailCount(failCount);
            
        } catch (Exception e) {
            syncResult.setErrorMessage("同步失败: " + e.getMessage());
            syncResult.setSuccessCount(0);
            syncResult.setFailCount(0);
        }
        
        return syncResult;
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "exchange_rates", allEntries = true)
    public void syncRateForCurrency(String currencyCode) {
        Currency currency = currencyRepository.findByCode(currencyCode)
            .orElseThrow(() -> new IllegalArgumentException("币种不存在"));
        
        Currency baseCurrency = currencyRepository.findBaseCurrency()
            .orElseThrow(() -> new IllegalArgumentException("未设置基准币种"));
        
        try {
            BigDecimal rate = externalRateService.getRate(baseCurrency.getCode(), currencyCode);
            
            if (rate != null) {
                ExchangeRate exchangeRate = new ExchangeRate();
                exchangeRate.setId(UUIDUtils.generate());
                exchangeRate.setFromCurrencyId(baseCurrency.getId());
                exchangeRate.setToCurrencyId(currency.getId());
                exchangeRate.setRate(rate);
                exchangeRate.setEffectiveDate(System.currentTimeMillis());
                exchangeRate.setSource("FRANKFURTER");
                exchangeRate.setRemark("手动同步");
                exchangeRate.setStatus(1);
                exchangeRate.setDeleted(0);
                
                Long currentTime = System.currentTimeMillis();
                String currentUser = getCurrentUser();
                exchangeRate.setCreatedAt(currentTime);
                exchangeRate.setUpdatedAt(currentTime);
                exchangeRate.setCreatedBy(currentUser);
                exchangeRate.setUpdatedBy(currentUser);
                
                exchangeRateRepository.save(exchangeRate);
            } else {
                throw new RuntimeException("外部API未返回该币种汇率");
            }
        } catch (Exception e) {
            throw new RuntimeException("同步汇率失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "exchange_rates", allEntries = true)
    public void deleteRatesByCurrency(String currencyId) {
        exchangeRateRepository.logicalDeleteByCurrencyId(
            currencyId, System.currentTimeMillis(), getCurrentUser());
    }
    
    @Override
    public RateSyncDTO getLastSyncStatus() {
        // 这里可以从缓存或数据库中获取最后一次同步状态
        // 暂时返回空对象
        return new RateSyncDTO();
    }
    
    private ExchangeRateHistoryDTO convertToHistoryDTO(ExchangeRate exchangeRate) {
        ExchangeRateHistoryDTO dto = new ExchangeRateHistoryDTO();
        BeanUtils.copyProperties(exchangeRate, dto);
        
        // 设置币种信息
        try {
            Currency fromCurrency = currencyRepository.findById(exchangeRate.getFromCurrencyId()).orElse(null);
            if (fromCurrency != null) {
                dto.setFromCurrencyCode(fromCurrency.getCode());
                dto.setFromCurrencyName(fromCurrency.getName());
            }
            
            Currency toCurrency = currencyRepository.findById(exchangeRate.getToCurrencyId()).orElse(null);
            if (toCurrency != null) {
                dto.setToCurrencyCode(toCurrency.getCode());
                dto.setToCurrencyName(toCurrency.getName());
            }
        } catch (Exception e) {
            // 忽略币种信息获取异常
        }
        
        return dto;
    }
    
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}