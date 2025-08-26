package com.company.financial.service.impl;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.util.UUIDUtils;
import com.company.financial.dto.currency.*;
import com.company.financial.entity.Currency;
import com.company.financial.repository.CurrencyRepository;
import com.company.financial.service.CurrencyService;
import com.company.financial.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 币种管理Service实现
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateService exchangeRateService;
    
    @Override
    public ResponsePageDataEntity<CurrencyDTO> getCurrencyList(CurrencyQueryDTO queryDTO) {
        // 构建排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (queryDTO.getSort() != null && !queryDTO.getSort().isEmpty()) {
            String[] sortParts = queryDTO.getSort().split(",");
            if (sortParts.length == 2) {
                Sort.Direction direction = "asc".equalsIgnoreCase(sortParts[1]) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
                sort = Sort.by(direction, sortParts[0]);
            }
        }
        
        Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);
        Page<Currency> currencyPage = currencyRepository.findByConditions(
            queryDTO.getCode(),
            queryDTO.getName(),
            queryDTO.getStatus(),
            queryDTO.getIsBase(),
            pageable
        );
        
        List<CurrencyDTO> currencyDTOs = currencyPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        ResponsePageDataEntity<CurrencyDTO> response = new ResponsePageDataEntity<>();
        response.setContent(currencyDTOs);
        response.setTotalElements(currencyPage.getTotalElements());
        response.setTotalPages(currencyPage.getTotalPages());
        response.setSize(currencyPage.getSize());
        response.setNumber(currencyPage.getNumber());
        response.setNumberOfElements(currencyPage.getNumberOfElements());
        response.setFirst(currencyPage.isFirst());
        response.setLast(currencyPage.isLast());
        
        return response;
    }
    
    @Override
    // @Cacheable(value = "currencies", key = "'all'")
    public List<CurrencyDTO> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAllActiveCurrencies();
        return currencies.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    // @Cacheable(value = "currencies", key = "#id")
    public CurrencyDTO getCurrencyById(String id) {
        Currency currency = currencyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("币种不存在"));
        return convertToDTO(currency);
    }
    
    @Override
    // @Cacheable(value = "currencies", key = "'code_' + #code")
    public CurrencyDTO getCurrencyByCode(String code) {
        Currency currency = currencyRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("币种不存在"));
        return convertToDTO(currency);
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO createCurrency(CurrencyCreateDTO createDTO) {
        // 检查代码是否已存在
        if (currencyRepository.findByCode(createDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("币种代码已存在");
        }
        
        Currency currency = new Currency();
        currency.setId(UUIDUtils.generate());
        currency.setCode(createDTO.getCode());
        currency.setName(createDTO.getName());
        currency.setNameEn(createDTO.getNameEn());
        currency.setSymbol(createDTO.getSymbol());
        currency.setDecimalPlaces(createDTO.getDecimalPlaces() != null ? createDTO.getDecimalPlaces() : 8);
        currency.setSortOrder(createDTO.getSortOrder());
        currency.setIsBase(0);
        currency.setStatus(1);
        currency.setDeleted(0);
        
        Long currentTime = System.currentTimeMillis();
        String currentUser = getCurrentUser();
        currency.setCreatedAt(currentTime);
        currency.setUpdatedAt(currentTime);
        currency.setCreatedBy(currentUser);
        currency.setUpdatedBy(currentUser);
        
        Currency savedCurrency = currencyRepository.save(currency);
        return convertToDTO(savedCurrency);
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO updateCurrency(String id, CurrencyUpdateDTO updateDTO) {
        Currency currency = currencyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("币种不存在"));
        
        if (updateDTO.getName() != null) {
            currency.setName(updateDTO.getName());
        }
        if (updateDTO.getNameEn() != null) {
            currency.setNameEn(updateDTO.getNameEn());
        }
        if (updateDTO.getSymbol() != null) {
            currency.setSymbol(updateDTO.getSymbol());
        }
        if (updateDTO.getDecimalPlaces() != null) {
            currency.setDecimalPlaces(updateDTO.getDecimalPlaces());
        }
        if (updateDTO.getSortOrder() != null) {
            currency.setSortOrder(updateDTO.getSortOrder());
        }
        
        currency.setUpdatedAt(System.currentTimeMillis());
        currency.setUpdatedBy(getCurrentUser());
        
        Currency savedCurrency = currencyRepository.save(currency);
        return convertToDTO(savedCurrency);
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "currencies", allEntries = true)
    public void toggleCurrencyStatus(String id, Integer status) {
        Currency currency = currencyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("币种不存在"));
        
        currency.setStatus(status);
        currency.setUpdatedAt(System.currentTimeMillis());
        currency.setUpdatedBy(getCurrentUser());
        
        currencyRepository.save(currency);
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "currencies", allEntries = true)
    public void setBaseCurrency(String id) {
        Currency currency = currencyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("币种不存在"));
        
        // 先将所有币种设为非基准币种
        // 手动更新所有币种为非基准币种
        List<Currency> allCurrencies = currencyRepository.findAll();
        for (Currency c : allCurrencies) {
            if (c.getIsBase() == 1) {
                c.setIsBase(0);
                c.setUpdatedAt(System.currentTimeMillis());
                c.setUpdatedBy(getCurrentUser());
                currencyRepository.save(c);
            }
        }
        
        // 设置新的基准币种
        currency.setIsBase(1);
        currency.setUpdatedAt(System.currentTimeMillis());
        currency.setUpdatedBy(getCurrentUser());
        
        currencyRepository.save(currency);
    }
    
    @Override
    @Transactional
    // @CacheEvict(value = "currencies", allEntries = true)
    public void deleteCurrency(String id) {
        Currency currency = currencyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("币种不存在"));
        
        // 检查是否为基准币种
        if (currency.getIsBase() == 1) {
            throw new IllegalArgumentException("基准币种不能删除");
        }
        
        // 删除相关汇率记录
        exchangeRateService.deleteRatesByCurrency(id);
        
        // 逻辑删除币种
        currency.setDeleted(1);
        currency.setUpdatedAt(System.currentTimeMillis());
        currency.setUpdatedBy(getCurrentUser());
        
        currencyRepository.save(currency);
    }
    
    @Override
    // @Cacheable(value = "currencies", key = "'base'")
    public CurrencyDTO getBaseCurrency() {
        Currency baseCurrency = currencyRepository.findBaseCurrency()
            .orElse(null);
        return baseCurrency != null ? convertToDTO(baseCurrency) : null;
    }
    
    @Override
    public boolean existsByCode(String code) {
        return currencyRepository.findByCode(code).isPresent();
    }
    
    private CurrencyDTO convertToDTO(Currency currency) {
        CurrencyDTO dto = new CurrencyDTO();
        BeanUtils.copyProperties(currency, dto);
        
        // 获取当前汇率
        if (currency.getIsBase() != 1) {
            CurrencyDTO baseCurrency = getBaseCurrency();
            if (baseCurrency != null) {
                try {
                    BigDecimal currentRate = exchangeRateService.getLatestRate(baseCurrency.getId(), currency.getId());
                    dto.setCurrentRate(currentRate);
                    dto.setLastRateUpdate(System.currentTimeMillis());
                    log.info("币种 {} 当前汇率: {}", currency.getCode(), currentRate.toPlainString());
                } catch (Exception e) {
                    // 记录汇率获取异常
                    log.warn("获取币种 {} 汇率失败: {}", currency.getCode(), e.getMessage());
                    dto.setCurrentRate(null);
                    dto.setLastRateUpdate(null);
                }
            }
        } else {
            dto.setCurrentRate(BigDecimal.ONE);
            dto.setLastRateUpdate(System.currentTimeMillis());
        }
        
        return dto;
    }
    
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}