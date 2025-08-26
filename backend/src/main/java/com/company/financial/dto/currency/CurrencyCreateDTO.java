package com.company.financial.dto.currency;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 币种创建DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class CurrencyCreateDTO {
    
    @NotBlank(message = "币种代码不能为空")
    @Pattern(regexp = "^[A-Z]{3}$", message = "币种代码必须为3位大写字母")
    private String code;
    
    @NotBlank(message = "币种名称不能为空")
    private String name;
    
    private String nameEn;
    
    @NotBlank(message = "币种符号不能为空")
    private String symbol;
    
    private Integer decimalPlaces = 2;
    
    private Integer sortOrder;
}