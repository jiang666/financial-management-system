package com.company.financial.dto.currency;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 汇率历史DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class ExchangeRateHistoryDTO {
    
    private String id;
    
    private String fromCurrencyId;
    
    private String fromCurrencyCode;
    
    private String fromCurrencyName;
    
    private String toCurrencyId;
    
    private String toCurrencyCode;
    
    private String toCurrencyName;
    
    private BigDecimal rate;
    
    private Long effectiveDate;
    
    private Long expiryDate;
    
    private String source;
    
    private String remark;
    
    private Integer status;
    
    private Long createdAt;
    
    private String createdBy;
}