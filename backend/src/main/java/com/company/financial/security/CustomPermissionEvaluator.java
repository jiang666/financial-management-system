package com.company.financial.security;

import com.company.financial.entity.Resource;
import com.company.financial.repository.ResourceRepository;
import com.company.financial.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义权限评估器
 * 
 * @author System
 */
@Component
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        String permissionString = permission.toString();
        
        // 获取用户的所有资源权限
        List<Resource> userResources = resourceRepository.findResourcesByUserId(currentUserId);
        
        // 检查用户是否有指定权限
        return userResources.stream()
                .anyMatch(resource -> permissionString.equals(resource.getResourceCode()) 
                        || permissionString.equals(resource.getResourceName()));
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, 
                               String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        String permissionString = permission.toString();
        
        // 获取用户的所有资源权限
        List<Resource> userResources = resourceRepository.findResourcesByUserId(currentUserId);
        
        // 检查用户是否有指定权限
        return userResources.stream()
                .anyMatch(resource -> permissionString.equals(resource.getResourceCode()) 
                        || permissionString.equals(resource.getResourceName()));
    }
}