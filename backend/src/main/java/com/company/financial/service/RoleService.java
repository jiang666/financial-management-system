package com.company.financial.service;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.auth.PermissionDTO;
import com.company.financial.dto.auth.RoleDTO;
import com.company.financial.dto.auth.RoleCreateDTO;
import com.company.financial.dto.auth.RoleUpdateDTO;
import com.company.financial.dto.auth.RoleQueryDTO;

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
    
    /**
     * 创建角色
     */
    RoleDTO createRole(RoleCreateDTO createDTO);
    
    /**
     * 更新角色
     */
    RoleDTO updateRole(String roleId, RoleUpdateDTO updateDTO);
    
    /**
     * 删除角色
     */
    void deleteRole(String roleId);
    
    /**
     * 分页查询角色
     */
    ResponsePageDataEntity<RoleDTO> queryRoles(RoleQueryDTO queryDTO);
    
    /**
     * 更新角色状态
     */
    void updateRoleStatus(String roleId, Integer status);
    
    /**
     * 获取角色的权限
     */
    List<PermissionDTO> getRolePermissions(String roleId);
    
    /**
     * 分配角色权限
     */
    void assignRolePermissions(String roleId, List<String> permissionIds);
    
    /**
     * 移除角色权限
     */
    void removeRolePermissions(String roleId, List<String> permissionIds);
    
    /**
     * 保存角色权限配置(完全替换)
     */
    void saveRolePermissions(String roleId, List<String> permissionIds);
}