package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Entity
@Table(name = "user_roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false, length = 36)
    private String roleId;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 创建人
     */
    @Column(name = "created_by", length = 36)
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}