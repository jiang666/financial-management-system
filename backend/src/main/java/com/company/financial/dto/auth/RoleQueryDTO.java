package com.company.financial.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色查询DTO
 */
@Data
@Schema(description = "角色查询DTO")
public class RoleQueryDTO {
    
    @Schema(description = "角色名称", example = "财务")
    private String name;
    
    @Schema(description = "角色编码", example = "FINANCE")
    private String code;
    
    @Schema(description = "状态：1-启用 0-禁用", example = "1")
    private Integer status;
    
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    
    @Schema(description = "页大小", example = "10")
    private Integer size = 10;
    
    @Schema(description = "排序字段", example = "createdAt")
    private String sortBy = "createdAt";
    
    @Schema(description = "排序方向", example = "desc")
    private String sortDir = "desc";
}