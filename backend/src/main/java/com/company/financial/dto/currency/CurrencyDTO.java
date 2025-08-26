package com.company.financial.dto.currency;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 币种响应DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class CurrencyDTO {
    
    private String id;
    
    private String code;
    
    private String name;
    
    private String nameEn;
    
    private String symbol;
    
    private Integer isBase;
    
    private Integer decimalPlaces;
    
    private Integer status;
    
    private Integer sortOrder;
    
    private BigDecimal currentRate;
    
    private Long lastRateUpdate;
    
    private Long createdAt;
    
    private Long updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
}