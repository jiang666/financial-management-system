package com.company.financial.service;

import com.company.financial.entity.Resource;

import java.util.List;

/**
 * 资源服务接口
 * 
 * @author System
 */
public interface ResourceService {
    
    /**
     * 获取资源树形结构
     * 
     * @return 资源树
     */
    List<Resource> getResourceTree();
    
    /**
     * 根据用户ID获取用户菜单
     * 
     * @param userId 用户ID
     * @return 用户菜单
     */
    List<Resource> getUserMenus(String userId);
    
    /**
     * 根据用户ID获取用户权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(String userId);
    
    /**
     * 检查用户是否有指定权限
     * 
     * @param userId 用户ID
     * @param permission 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(String userId, String permission);
    
    /**
     * 根据资源类型获取资源列表
     * 
     * @param resourceType 资源类型
     * @return 资源列表
     */
    List<Resource> getResourcesByType(String resourceType);
}