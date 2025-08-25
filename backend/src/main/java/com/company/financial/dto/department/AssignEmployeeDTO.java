package com.company.financial.dto.department;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 分配员工到部门DTO
 */
@Data
public class AssignEmployeeDTO {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    @NotBlank(message = "部门ID不能为空")
    private String departmentId;
    
    private String positionId;
}