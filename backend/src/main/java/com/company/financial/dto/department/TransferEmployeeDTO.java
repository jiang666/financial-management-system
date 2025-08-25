package com.company.financial.dto.department;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 员工部门调动DTO
 */
@Data
public class TransferEmployeeDTO {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    @NotBlank(message = "原部门ID不能为空")
    private String fromDepartmentId;
    
    @NotBlank(message = "目标部门ID不能为空")
    private String toDepartmentId;
    
    private String positionId;
    
    private String reason;
}