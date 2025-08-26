package com.company.financial.dto.currency;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 汇率更新DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class RateUpdateDTO {
    
    @NotNull(message = "汇率值不能为空")
    @DecimalMin(value = "0.000001", message = "汇率值必须大于0")
    private BigDecimal rate;
    
    private Long effectiveDate;
    
    private Long expiryDate;
    
    private String source;
    
    private String remark;
}