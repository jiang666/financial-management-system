package com.company.financial.dto.currency;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 汇率更新DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Slf4j
@Data
public class RateUpdateDTO {
    
    @NotNull(message = "汇率值不能为空")
    @DecimalMin(value = "0.000001", message = "汇率值必须大于0")
    private BigDecimal rate;
    
    // 添加setter用于调试
    public void setRate(BigDecimal rate) {
        log.info("RateUpdateDTO设置汇率: 原始值={}, toPlainString={}", 
            rate, rate != null ? rate.toPlainString() : "null");
        this.rate = rate;
    }
    
    private Long effectiveDate;
    
    private Long expiryDate;
    
    private String source;
    
    private String remark;
}