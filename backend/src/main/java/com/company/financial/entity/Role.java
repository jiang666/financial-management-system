package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 角色实体类
 * 
 * @author System
 */
@Entity
@Table(name = "tb_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "create_time")
    private Long createTime;
    
    @Column(name = "update_time")
    private Long updateTime;
    
    @Column(name = "create_by")
    private String createBy;
    
    @Column(name = "update_by")
    private String updateBy;
    
    @Column(name = "role_code")
    private String roleCode;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "can_delete")
    private Integer canDelete;
    
    @Column(name = "can_modify")
    private Integer canModify;
    
    @Column(name = "user_count")
    private Integer userCount;
    
    @Column(name = "deleted")
    private Integer deleted;
}