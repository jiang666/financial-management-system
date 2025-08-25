package com.company.financial.dto.parameter;

import lombok.Data;

/**
 * 系统参数查询DTO
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
public class SystemParameterQueryDTO {
    /**
     * 参数分类
     */
    private String category;
    
    /**
     * 关键字搜索
     */
    private String keyword;
    
    /**
     * 是否系统参数
     */
    private Boolean isSystem;
    
    /**
     * 是否可见
     */
    private Boolean visible;
    
    /**
     * 页码
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 20;
}