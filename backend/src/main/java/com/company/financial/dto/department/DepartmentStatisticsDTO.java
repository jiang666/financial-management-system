package com.company.financial.dto.department;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DepartmentStatisticsDTO {
    private String departmentId;
    private String departmentName;
    private Integer totalEmployees;
    private Integer positionCount;
    private Integer subDepartmentCount;
    private BigDecimal totalSalaryBudget;
    private BigDecimal actualSalaryCost;
    private List<PositionEmployeeCount> employeesByPosition;
    
    @Data
    public static class PositionEmployeeCount {
        private String positionName;
        private Integer count;
    }
}