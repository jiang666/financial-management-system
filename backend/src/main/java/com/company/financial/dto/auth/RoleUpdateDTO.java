package com.company.financial.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 角色更新DTO
 */
@Data
@Schema(description = "角色更新DTO")
public class RoleUpdateDTO {
    
    @Schema(description = "角色名称", example = "财务经理")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100")
    private String name;
    
    @Schema(description = "角色描述", example = "负责财务管理相关工作")
    @Size(max = 500, message = "角色描述长度不能超过500")
    private String description;
    
    @Schema(description = "状态：1-启用 0-禁用", example = "1")
    private Integer status;
}