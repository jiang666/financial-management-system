package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 角色资源关联实体类
 * 
 * @author System
 */
@Entity
@Table(name = "tb_role_resource")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResource {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "create_time")
    private Long createTime;
    
    @Column(name = "create_by")
    private String createBy;
    
    @Column(name = "role_id")
    private String roleId;
    
    @Column(name = "resource_id")
    private String resourceId;
}