package com.company.financial.service;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.currency.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 币种管理Service接口
 * 
 * @author Claude
 * @date 2025-08-25
 */
public interface CurrencyService {
    
    /**
     * 分页查询币种列表
     */
    ResponsePageDataEntity<CurrencyDTO> getCurrencyList(CurrencyQueryDTO queryDTO);
    
    /**
     * 获取所有币种下拉列表
     */
    List<CurrencyDTO> getAllCurrencies();
    
    /**
     * 根据ID获取币种详情
     */
    CurrencyDTO getCurrencyById(String id);
    
    /**
     * 根据代码获取币种详情
     */
    CurrencyDTO getCurrencyByCode(String code);
    
    /**
     * 创建币种
     */
    CurrencyDTO createCurrency(CurrencyCreateDTO createDTO);
    
    /**
     * 更新币种
     */
    CurrencyDTO updateCurrency(String id, CurrencyUpdateDTO updateDTO);
    
    /**
     * 启用/禁用币种
     */
    void toggleCurrencyStatus(String id, Integer status);
    
    /**
     * 设置基准币种
     */
    void setBaseCurrency(String id);
    
    /**
     * 删除币种
     */
    void deleteCurrency(String id);
    
    /**
     * 获取基准币种
     */
    CurrencyDTO getBaseCurrency();
    
    /**
     * 检查币种代码是否存在
     */
    boolean existsByCode(String code);
}