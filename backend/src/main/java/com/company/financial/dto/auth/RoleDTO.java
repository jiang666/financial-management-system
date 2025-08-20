package com.company.financial.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色信息")
public class RoleDTO {
    
    @Schema(description = "角色ID")
    private String id;
    
    @Schema(description = "角色编码")
    private String code;
    
    @Schema(description = "角色名称")
    private String name;
    
    @Schema(description = "角色描述")
    private String description;
    
    @Schema(description = "角色状态：1-启用 0-禁用")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "权限列表")
    private List<PermissionDTO> permissions;
}