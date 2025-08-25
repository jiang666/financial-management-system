package com.company.financial.dto.position;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PositionDTO {
    private String id;
    private String departmentId;
    private String name;
    private String level;
    private Integer count;
    private Integer actualCount;
    private BigDecimal salary;
    private String status;
    private String description;
    private Long createTime;
    private Long updateTime;
}