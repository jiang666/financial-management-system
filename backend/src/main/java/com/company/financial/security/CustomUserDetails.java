package com.company.financial.security;

import com.company.financial.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义用户详情类
 */
@Getter
public class CustomUserDetails implements UserDetails {
    
    private final String id;
    private final String username;
    private final String password;
    private final String email;
    private final String realName;
    private final String departmentId;
    private final Integer status;
    private final Collection<? extends GrantedAuthority> authorities;
    
    public CustomUserDetails(User user, Set<String> permissions) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.realName = user.getRealName();
        this.departmentId = user.getDepartmentId();
        this.status = user.getStatus();
        this.authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return status != null && status == 1;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 获取用户权限代码列表
     */
    public Set<String> getPermissionCodes() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
    
    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        return getPermissionCodes().contains(permissionCode);
    }
    
    /**
     * 检查是否拥有任一权限
     */
    public boolean hasAnyPermission(String... permissionCodes) {
        Set<String> userPermissions = getPermissionCodes();
        for (String code : permissionCodes) {
            if (userPermissions.contains(code)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否拥有所有权限
     */
    public boolean hasAllPermissions(String... permissionCodes) {
        Set<String> userPermissions = getPermissionCodes();
        for (String code : permissionCodes) {
            if (!userPermissions.contains(code)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取用户ID
     */
    public String getUserId() {
        return id;
    }
}