package com.company.financial.dto.role;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 角色创建DTO
 * 
 * @author System
 */
@Data
public class RoleCreateDTO {
    
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 100, message = "角色编码长度不能超过100个字符")
    private String roleCode;
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    private String description;
    
    private String status;
    
    private Integer canDelete;
    
    private Integer canModify;
    
    private List<String> resourceIds;
}