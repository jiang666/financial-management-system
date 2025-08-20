package com.company.financial.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Where(clause = "deleted = false")
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"password", "roles"})
public class User {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(length = 36)
    private String id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(name = "real_name", length = 50)
    private String realName;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "department_id", length = 36)
    private String departmentId;
    
    @Column(nullable = false)
    private Integer status = 1;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 36)
    private String createdBy;
    
    @Column(name = "updated_by", length = 36)
    private String updatedBy;
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
    @Version
    private Integer version = 0;
    
    // 用户角色关系
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // 业务方法
    public boolean isEnabled() {
        return status == 1;
    }
    
    public void enable() {
        this.status = 1;
    }
    
    public void disable() {
        this.status = 0;
    }
    
    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }
    
    public void removeRole(Role role) {
        if (roles != null) {
            roles.remove(role);
        }
    }
    
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}