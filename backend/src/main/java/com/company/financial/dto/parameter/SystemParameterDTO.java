package com.company.financial.dto.parameter;

import lombok.Data;

/**
 * 系统参数DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class SystemParameterDTO {
    private String id;
    private String paramKey;
    private String paramValue;
    private String paramType;
    private String category;
    private String description;
    private Boolean isSystem;
    private Boolean isEncrypted;
    private Integer sortOrder;
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
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}