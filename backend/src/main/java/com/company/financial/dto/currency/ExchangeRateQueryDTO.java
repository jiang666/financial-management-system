package com.company.financial.dto.currency;

import lombok.Data;

/**
 * 汇率查询DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class ExchangeRateQueryDTO {
    
    private Integer page = 0;
    
    private Integer size = 20;
    
    private String currencyId;
    
    private String currencyCode;
    
    private Long startDate;
    
    private Long endDate;
    
    private String source;
    
    private String sort = "effective_date,desc";
}