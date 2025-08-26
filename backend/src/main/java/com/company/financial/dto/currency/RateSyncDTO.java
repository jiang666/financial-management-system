package com.company.financial.dto.currency;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 汇率同步DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class RateSyncDTO {
    
    private String baseCurrencyCode;
    
    private Map<String, BigDecimal> rates;
    
    private Long syncTime;
    
    private String source;
    
    private Integer successCount;
    
    private Integer failCount;
    
    private String errorMessage;
}