package com.company.financial.dto.department;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CreateDepartmentDTO {
    @NotBlank(message = "部门名称不能为空")
    private String name;
    
    private String code;
    
    @NotBlank(message = "部门类型不能为空")
    private String type;
    
    private String parentId;
    
    private String managerId;
    
    private String phone;
    
    private String description;
}