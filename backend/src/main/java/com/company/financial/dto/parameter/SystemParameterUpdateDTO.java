package com.company.financial.dto.parameter;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 系统参数更新DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class SystemParameterUpdateDTO {
    @NotBlank(message = "参数值不能为空")
    private String paramValue;
    
    private String description;
    private String validationRule;
    private String minValue;
    private String maxValue;
    private String options;
    private String defaultValue;
    private Boolean required;
    private Boolean visible;
    private Boolean editable;
    private Boolean effectiveImmediately;
    private Boolean restartRequired;
}