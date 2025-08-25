package com.company.financial.dto.parameter;

import lombok.Data;
import java.util.List;

/**
 * 参数验证结果DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class ParameterValidateResultDTO {
    private Boolean valid;
    private List<String> errors;
}