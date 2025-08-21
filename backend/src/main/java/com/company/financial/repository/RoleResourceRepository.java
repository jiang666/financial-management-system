package com.company.financial.repository;

import com.company.financial.entity.RoleResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色资源关联数据访问接口
 * 
 * @author System
 */
@Repository
public interface RoleResourceRepository extends JpaRepository<RoleResource, String> {
    
    /**
     * 根据角色ID查询角色资源关联
     * 
     * @param roleId 角色ID
     * @return 角色资源关联列表
     */
    List<RoleResource> findByRoleId(String roleId);
    
    /**
     * 根据资源ID查询角色资源关联
     * 
     * @param resourceId 资源ID
     * @return 角色资源关联列表
     */
    List<RoleResource> findByResourceId(String resourceId);
    
    /**
     * 根据角色ID删除角色资源关联
     * 
     * @param roleId 角色ID
     */
    void deleteByRoleId(String roleId);
    
    /**
     * 根据资源ID删除角色资源关联
     * 
     * @param resourceId 资源ID
     */
    void deleteByResourceId(String resourceId);
    
    /**
     * 检查角色资源关联是否存在
     * 
     * @param roleId 角色ID
     * @param resourceId 资源ID
     * @return 是否存在
     */
    boolean existsByRoleIdAndResourceId(String roleId, String resourceId);
    
    /**
     * 根据角色ID和资源ID查询角色资源关联
     * 
     * @param roleId 角色ID
     * @param resourceId 资源ID
     * @return 角色资源关联
     */
    java.util.Optional<RoleResource> findByRoleIdAndResourceId(String roleId, String resourceId);
}