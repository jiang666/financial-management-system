package com.company.financial.dto.currency;

import lombok.Data;

/**
 * 币种查询DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class CurrencyQueryDTO {
    
    private Integer page = 0;
    
    private Integer size = 20;
    
    private String code;
    
    private String name;
    
    private Integer status;
    
    private Integer isBase;
    
    private String sort = "created_at,desc";
}