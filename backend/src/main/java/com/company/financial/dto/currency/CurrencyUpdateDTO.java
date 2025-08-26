package com.company.financial.dto.currency;

import lombok.Data;

/**
 * 币种更新DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class CurrencyUpdateDTO {
    
    private String name;
    
    private String nameEn;
    
    private String symbol;
    
    private Integer decimalPlaces;
    
    private Integer sortOrder;
}