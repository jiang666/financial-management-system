package com.company.financial.dto.department;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UpdateDepartmentDTO {
    @NotBlank(message = "部门名称不能为空")
    private String name;
    
    @NotBlank(message = "部门类型不能为空")
    private String type;
    
    private String parentId;
    
    private String managerId;
    
    private String phone;
    
    private String status;
    
    private String description;
}