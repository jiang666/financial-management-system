package com.company.financial.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 权限DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限信息")
public class PermissionDTO {
    
    @Schema(description = "权限ID")
    private String id;
    
    @Schema(description = "权限编码")
    private String code;
    
    @Schema(description = "权限名称")
    private String name;
    
    @Schema(description = "资源标识")
    private String resource;
    
    @Schema(description = "操作类型")
    private String action;
    
    @Schema(description = "权限描述")
    private String description;
}