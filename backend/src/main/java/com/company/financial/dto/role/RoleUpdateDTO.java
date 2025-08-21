package com.company.financial.dto.role;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 角色更新DTO
 * 
 * @author System
 */
@Data
public class RoleUpdateDTO {
    
    private String id;
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    private String description;
    
    @NotBlank(message = "状态不能为空")
    private String status;
    
    private List<String> resourceIds;
}