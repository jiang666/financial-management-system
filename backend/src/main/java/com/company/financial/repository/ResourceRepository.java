package com.company.financial.repository;

import com.company.financial.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资源数据访问接口
 * 
 * @author System
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    
    /**
     * 查询所有资源树形结构
     * 
     * @param deleted 删除标记
     * @return 资源列表
     */
    List<Resource> findByDeletedOrderByLevelAscSortOrderAsc(Integer deleted);
    
    /**
     * 根据父资源ID查询子资源
     * 
     * @param parentId 父资源ID
     * @param deleted 删除标记
     * @return 子资源列表
     */
    List<Resource> findByParentIdAndDeletedOrderBySortOrderAsc(String parentId, Integer deleted);
    
    /**
     * 根据资源类型查询资源
     * 
     * @param resourceType 资源类型
     * @param deleted 删除标记
     * @return 资源列表
     */
    List<Resource> findByResourceTypeAndDeletedOrderBySortOrderAsc(String resourceType, Integer deleted);
    
    /**
     * 根据用户ID查询用户有权限的资源列表
     * 
     * @param userId 用户ID
     * @return 资源列表
     */
    @Query("SELECT DISTINCT r FROM Resource r, RoleResource rr, UserRole ur " +
           "WHERE r.id = rr.resourceId AND rr.roleId = ur.roleId AND ur.userId = :userId " +
           "AND r.deleted = 0 ORDER BY r.level ASC, r.sortOrder ASC")
    List<Resource> findResourcesByUserId(@Param("userId") String userId);
    
    /**
     * 根据角色ID查询角色的资源列表
     * 
     * @param roleId 角色ID
     * @return 资源列表
     */
    @Query("SELECT r FROM Resource r, RoleResource rr " +
           "WHERE r.id = rr.resourceId AND rr.roleId = :roleId " +
           "AND r.deleted = 0 ORDER BY r.level ASC, r.sortOrder ASC")
    List<Resource> findResourcesByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据资源编码查询资源
     * 
     * @param resourceCode 资源编码
     * @param deleted 删除标记
     * @return 资源信息
     */
    Resource findByResourceCodeAndDeleted(String resourceCode, Integer deleted);
}