package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 岗位实体类
 * 
 * @author System
 */
@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "department_id")
    private String departmentId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "level")
    private String level;
    
    @Column(name = "count")
    private Integer count;
    
    @Column(name = "actual_count")
    private Integer actualCount;
    
    @Column(name = "salary")
    private BigDecimal salary;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "create_time")
    private Long createTime;
    
    @Column(name = "update_time")
    private Long updateTime;
    
    @Column(name = "create_by")
    private String createBy;
    
    @Column(name = "update_by")
    private String updateBy;
    
    @Column(name = "deleted")
    private Integer deleted;
}