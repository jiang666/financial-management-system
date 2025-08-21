package com.company.financial.service;

import com.company.financial.dto.role.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 角色服务接口
 * 
 * @author System
 */
public interface RoleService {
    
    /**
     * 分页查询角色列表
     * 
     * @param queryDTO 查询条件
     * @return 角色分页数据
     */
    Page<RoleDetailDTO> findRoles(RoleQueryDTO queryDTO);
    
    /**
     * 查询所有角色列表
     * 
     * @return 角色列表
     */
    List<RoleDetailDTO> findAllRoles();
    
    /**
     * 根据ID查询角色详情
     * 
     * @param id 角色ID
     * @return 角色详情
     */
    RoleDetailDTO findRoleById(String id);
    
    /**
     * 创建角色
     * 
     * @param createDTO 创建信息
     * @return 角色详情
     */
    RoleDetailDTO createRole(RoleCreateDTO createDTO);
    
    /**
     * 更新角色
     * 
     * @param updateDTO 更新信息
     * @return 角色详情
     */
    RoleDetailDTO updateRole(RoleUpdateDTO updateDTO);
    
    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    void deleteRole(String id);
    
    /**
     * 批量删除角色
     * 
     * @param ids 角色ID列表
     */
    void deleteRoles(List<String> ids);
    
    /**
     * 更新角色状态
     * 
     * @param id 角色ID
     * @param status 状态
     */
    void updateStatus(String id, String status);
    
    /**
     * 为角色配置权限
     * 
     * @param roleId 角色ID
     * @param resourceIds 资源ID列表
     */
    void configurePermissions(String roleId, List<String> resourceIds);
    
    /**
     * 获取角色的权限列表
     * 
     * @param roleId 角色ID
     * @return 资源ID列表
     */
    List<String> getRolePermissions(String roleId);
}