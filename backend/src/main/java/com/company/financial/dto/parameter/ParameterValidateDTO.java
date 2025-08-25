package com.company.financial.dto.parameter;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 参数验证DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class ParameterValidateDTO {
    @NotBlank(message = "参数键不能为空")
    private String paramKey;
    
    @NotBlank(message = "参数值不能为空")
    private String paramValue;
}