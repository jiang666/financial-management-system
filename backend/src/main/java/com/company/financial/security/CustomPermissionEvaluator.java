package com.company.financial.security;

import com.company.financial.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 自定义权限评估器
 * 用于在@PreAuthorize中进行权限检查
 */
@Slf4j
@Component("customPermissionEvaluator")
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    private final RoleService roleService;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String userId = authentication.getName();
        String permissionCode = permission.toString();
        
        log.debug("权限检查: userId={}, permissionCode={}", userId, permissionCode);
        
        try {
            return roleService.hasPermission(userId, permissionCode);
        } catch (Exception e) {
            log.error("权限检查异常: userId={}, permissionCode={}", userId, permissionCode, e);
            return false;
        }
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }
    
    /**
     * 检查用户是否拥有指定角色
     */
    public boolean hasRole(Authentication authentication, String roleCode) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String userId = authentication.getName();
        
        log.debug("角色检查: userId={}, roleCode={}", userId, roleCode);
        
        try {
            return roleService.hasRole(userId, roleCode);
        } catch (Exception e) {
            log.error("角色检查异常: userId={}, roleCode={}", userId, roleCode, e);
            return false;
        }
    }
    
    /**
     * 检查用户是否拥有任一指定权限
     */
    public boolean hasAnyPermission(Authentication authentication, String... permissionCodes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String userId = authentication.getName();
        
        log.debug("任一权限检查: userId={}, permissionCodes={}", userId, permissionCodes);
        
        try {
            return roleService.hasAnyPermission(userId, permissionCodes);
        } catch (Exception e) {
            log.error("任一权限检查异常: userId={}, permissionCodes={}", userId, permissionCodes, e);
            return false;
        }
    }
    
    /**
     * 检查用户是否拥有所有指定权限
     */
    public boolean hasAllPermissions(Authentication authentication, String... permissionCodes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String userId = authentication.getName();
        
        log.debug("所有权限检查: userId={}, permissionCodes={}", userId, permissionCodes);
        
        try {
            return roleService.hasAllPermissions(userId, permissionCodes);
        } catch (Exception e) {
            log.error("所有权限检查异常: userId={}, permissionCodes={}", userId, permissionCodes, e);
            return false;
        }
    }
}