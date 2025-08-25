package com.company.financial.dto.parameter;

import lombok.Data;

/**
 * 参数历史记录DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class ParameterHistoryDTO {
    private String id;
    private String paramKey;
    private String oldValue;
    private String newValue;
    private String operation;
    private String operator;
    private Long operateTime;
    private String remark;
}