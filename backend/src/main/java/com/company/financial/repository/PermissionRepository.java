package com.company.financial.repository;

import com.company.financial.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限数据访问接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    
    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * 查找所有未删除的权限
     */
    List<Permission> findByDeletedFalse();
    
    /**
     * 检查权限编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据权限编码批量查找
     */
    List<Permission> findByCodeIn(Set<String> codes);
    
    /**
     * 根据资源和操作查找权限
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);
    
    /**
     * 根据资源查找权限
     */
    List<Permission> findByResource(String resource);
    
    /**
     * 根据角色ID查找权限
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据用户ID查找权限
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") String userId);
    
    /**
     * 根据用户名查找权限
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.username = :username")
    List<Permission> findByUsername(@Param("username") String username);
    
    /**
     * 根据用户名查找权限编码列表
     */
    @Query("SELECT DISTINCT p.code FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.username = :username")
    Set<String> findPermissionCodesByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID查找权限编码列表
     */
    @Query("SELECT DISTINCT p.code FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId")
    Set<String> findPermissionCodesByUserId(@Param("userId") String userId);
    
    /**
     * 按资源分组查询权限
     */
    @Query("SELECT p.resource, COUNT(p) FROM Permission p GROUP BY p.resource")
    List<Object[]> countByResource();
}