package com.company.financial.dto.parameter;

import lombok.Data;

import java.util.List;

/**
 * 参数分类DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class ParameterCategoryDTO {
    private String code;
    private String name;
    private String icon;
    private Integer sortOrder;
    private List<ParameterCategoryDTO> subcategories;
    private List<String> parameterKeys;
}