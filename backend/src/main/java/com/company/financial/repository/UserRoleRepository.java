package com.company.financial.repository;

import com.company.financial.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联数据访问接口
 * 
 * @author System
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    
    /**
     * 根据用户ID查询用户角色关联
     * 
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserId(String userId);
    
    /**
     * 根据角色ID查询用户角色关联
     * 
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleId(String roleId);
    
    /**
     * 根据用户ID删除用户角色关联
     * 
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
    
    /**
     * 根据角色ID删除用户角色关联
     * 
     * @param roleId 角色ID
     */
    void deleteByRoleId(String roleId);
    
    /**
     * 检查用户角色关联是否存在
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否存在
     */
    boolean existsByUserIdAndRoleId(String userId, String roleId);
}