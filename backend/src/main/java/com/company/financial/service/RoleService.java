package com.company.financial.service;

import com.company.financial.dto.auth.PermissionDTO;
import com.company.financial.dto.auth.RoleDTO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 获取所有角色
     */
    List<RoleDTO> getAllRoles();
    
    /**
     * 根据ID获取角色详情
     */
    RoleDTO getRoleById(String roleId);
    
    /**
     * 根据角色编码获取角色
     */
    RoleDTO getRoleByCode(String roleCode);
    
    /**
     * 获取所有权限
     */
    List<PermissionDTO> getAllPermissions();
    
    /**
     * 根据用户ID获取用户的所有权限
     */
    List<PermissionDTO> getUserPermissions(String userId);
    
    /**
     * 根据用户ID获取用户的所有角色
     */
    List<RoleDTO> getUserRoles(String userId);
    
    /**
     * 检查用户是否拥有指定权限
     */
    boolean hasPermission(String userId, String permissionCode);
    
    /**
     * 检查用户是否拥有指定角色
     */
    boolean hasRole(String userId, String roleCode);
    
    /**
     * 检查用户是否拥有任一指定权限
     */
    boolean hasAnyPermission(String userId, String... permissionCodes);
    
    /**
     * 检查用户是否拥有所有指定权限
     */
    boolean hasAllPermissions(String userId, String... permissionCodes);
}