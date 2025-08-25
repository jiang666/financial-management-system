package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 用户实体类
 * 
 * @author System
 */
@Entity
@Table(name = "tb_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
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
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "real_name")
    private String realName;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "department_id")
    private String departmentId;
    
    @Column(name = "position")
    private String position;
    
    @Column(name = "position_id")
    private String positionId;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "last_login_at")
    private Long lastLoginTime;
    
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    
    @Column(name = "deleted")
    private Integer deleted;
}