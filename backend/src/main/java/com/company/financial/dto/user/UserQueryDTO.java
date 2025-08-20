package com.company.financial.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
@Schema(description = "用户查询条件")
public class UserQueryDTO {
    
    @Schema(description = "用户名（模糊查询）")
    private String username;
    
    @Schema(description = "邮箱（模糊查询）")
    private String email;
    
    @Schema(description = "真实姓名（模糊查询）")
    private String realName;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "部门ID")
    private String departmentId;
    
    @Schema(description = "用户状态：1-启用 0-禁用")
    private Integer status;
    
    @Schema(description = "角色ID")
    private String roleId;
    
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    
    @Schema(description = "页大小", example = "10")
    private Integer size = 10;
    
    @Schema(description = "排序字段", example = "createdAt")
    private String sortBy = "createdAt";
    
    @Schema(description = "排序方向：asc/desc", example = "desc")
    private String sortDir = "desc";
}