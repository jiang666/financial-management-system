package com.company.financial.dto.parameter;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 系统参数批量更新DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class SystemParameterBatchUpdateDTO {
    @NotEmpty(message = "参数列表不能为空")
    @Valid
    private List<ParameterItem> parameters;
    
    @Data
    public static class ParameterItem {
        @NotBlank(message = "参数键不能为空")
        private String paramKey;
        
        @NotBlank(message = "参数值不能为空")
        private String paramValue;
    }
}