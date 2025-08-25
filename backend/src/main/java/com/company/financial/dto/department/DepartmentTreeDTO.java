package com.company.financial.dto.department;

import lombok.Data;
import java.util.List;

@Data
public class DepartmentTreeDTO {
    private String id;
    private String code;
    private String name;
    private String type;
    private String parentId;
    private String managerId;
    private String manager;
    private String phone;
    private Integer employeeCount;
    private String status;
    private String description;
    private List<DepartmentTreeDTO> children;
}