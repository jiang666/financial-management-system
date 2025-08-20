package com.company.financial.repository;

import com.company.financial.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联Repository
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    
    /**
     * 根据用户ID查找用户角色关联
     */
    List<UserRole> findByUserId(String userId);
    
    /**
     * 根据角色ID查找用户角色关联
     */
    List<UserRole> findByRoleId(String roleId);
    
    /**
     * 根据用户ID和角色ID查找
     */
    UserRole findByUserIdAndRoleId(String userId, String roleId);
    
    /**
     * 检查用户是否拥有指定角色
     */
    boolean existsByUserIdAndRoleId(String userId, String roleId);
    
    /**
     * 根据用户ID删除所有角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    /**
     * 根据用户ID和角色ID列表删除角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId IN :roleIds")
    void deleteByUserIdAndRoleIdIn(@Param("userId") String userId, @Param("roleIds") List<String> roleIds);
    
    /**
     * 根据角色ID删除所有用户关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") String roleId);
    
    /**
     * 获取用户的所有角色ID
     */
    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId")
    List<String> findRoleIdsByUserId(@Param("userId") String userId);
    
    /**
     * 获取角色的所有用户ID
     */
    @Query("SELECT ur.userId FROM UserRole ur WHERE ur.roleId = :roleId")
    List<String> findUserIdsByRoleId(@Param("roleId") String roleId);
}