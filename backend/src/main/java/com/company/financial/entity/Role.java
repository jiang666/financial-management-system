package com.company.financial.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
 * 角色实体类
 */
@Entity
@Table(name = "roles")
@Data
@Slf4j
@Where(clause = "deleted = false")
@EqualsAndHashCode(exclude = {"users", "permissions"})
@ToString(exclude = {"users", "permissions"})
public class Role {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(length = 36)
    private String id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer status = 1;
    
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
    
    // 角色用户关系（反向）
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    
    // 角色权限关系
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
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
    
    public void addPermission(Permission permission) {
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(permission);
    }
    
    public void removePermission(Permission permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }
    
    public void clearPermissions() {
        if (permissions != null) {
            permissions.clear();
        }
    }
    
    public boolean hasPermission(String permissionCode) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> p.getCode().equals(permissionCode));
    }
}