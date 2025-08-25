package com.company.financial.dto.department;

import lombok.Data;

/**
 * 部门员工信息DTO
 */
@Data
public class EmployeeDTO {
    private String id;
    private String username;
    private String realName;
    private String employeeId;
    private String departmentId;
    private String departmentName;
    private String positionId;
    private String positionName;
    private String phone;
    private String email;
    private Long hireDate;
    private String status;
}