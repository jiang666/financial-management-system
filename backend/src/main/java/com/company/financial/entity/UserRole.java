package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 用户角色关联实体类
 * 
 * @author System
 */
@Entity
@Table(name = "tb_user_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "create_time")
    private Long createTime;
    
    @Column(name = "create_by")
    private String createBy;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "role_id")
    private String roleId;
}