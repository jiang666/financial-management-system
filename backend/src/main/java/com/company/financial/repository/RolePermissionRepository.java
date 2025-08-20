package com.company.financial.repository;

import com.company.financial.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限关联表Repository
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    
    /**
     * 检查角色权限关联是否存在
     */
    boolean existsByRoleIdAndPermissionId(String roleId, String permissionId);
    
    /**
     * 根据角色ID查询权限关联
     */
    List<RolePermission> findByRoleId(String roleId);
    
    /**
     * 根据权限ID查询角色关联
     */
    List<RolePermission> findByPermissionId(String permissionId);
    
    /**
     * 删除角色的所有权限
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") String roleId);
    
    /**
     * 删除角色的指定权限
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId IN :permissionIds")
    void deleteByRoleIdAndPermissionIdIn(@Param("roleId") String roleId, @Param("permissionIds") List<String> permissionIds);
    
    /**
     * 删除权限的所有角色关联
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") String permissionId);
}