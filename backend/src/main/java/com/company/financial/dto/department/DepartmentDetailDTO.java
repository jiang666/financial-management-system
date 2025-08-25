package com.company.financial.dto.department;

import lombok.Data;

@Data
public class DepartmentDetailDTO {
    private String id;
    private String code;
    private String name;
    private String type;
    private String parentId;
    private String parentName;
    private String managerId;
    private String manager;
    private String phone;
    private Integer employeeCount;
    private String status;
    private String description;
    private Integer level;
    private String path;
    private Long createTime;
    private Long updateTime;
    private String createBy;
    private String updateBy;
}