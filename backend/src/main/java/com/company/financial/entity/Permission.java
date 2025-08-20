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
 * 权限实体类
 */
@Entity
@Table(name = "permissions")
@Data
@Slf4j
@Where(clause = "deleted = false")
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"roles"})
public class Permission {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(length = 36)
    private String id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 200)
    private String resource;
    
    @Column(length = 50)
    private String action;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
    @Column(name = "created_by", length = 36)
    private String createdBy;
    
    @Column(name = "updated_by", length = 36)
    private String updatedBy;
    
    // 权限角色关系（反向）
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
    
    // 构造方法
    public Permission() {
    }
    
    public Permission(String code, String name, String resource, String action, String description) {
        this.code = code;
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }
    
    // 业务方法
    public String getFullPermission() {
        if (resource != null && action != null) {
            return resource + ":" + action;
        }
        return code;
    }
    
    public boolean matches(String resource, String action) {
        return this.resource != null && this.resource.equals(resource) 
            && this.action != null && this.action.equals(action);
    }
}