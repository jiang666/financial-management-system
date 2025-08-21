package com.company.financial.repository;

import com.company.financial.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 * 
 * @author System
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    
    /**
     * 根据角色编码查询角色
     * 
     * @param roleCode 角色编码
     * @param deleted 删除标记
     * @return 角色信息
     */
    Optional<Role> findByRoleCodeAndDeleted(String roleCode, Integer deleted);
    
    /**
     * 根据角色名称查询角色
     * 
     * @param name 角色名称
     * @param deleted 删除标记
     * @return 角色信息
     */
    Optional<Role> findByNameAndDeleted(String name, Integer deleted);
    
    /**
     * 分页查询角色列表
     * 
     * @param roleCode 角色编码
     * @param name 角色名称
     * @param status 状态
     * @param deleted 删除标记
     * @param pageable 分页参数
     * @return 角色分页数据
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:roleCode IS NULL OR r.roleCode LIKE %:roleCode%) AND " +
           "(:name IS NULL OR r.name LIKE %:name%) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "r.deleted = :deleted")
    Page<Role> findRolesByConditions(@Param("roleCode") String roleCode,
                                   @Param("name") String name,
                                   @Param("status") String status,
                                   @Param("deleted") Integer deleted,
                                   Pageable pageable);
    
    /**
     * 查询所有启用的角色
     * 
     * @param status 状态
     * @param deleted 删除标记
     * @return 角色列表
     */
    List<Role> findByStatusAndDeletedOrderByCreateTime(String status, Integer deleted);
    
    /**
     * 检查角色编码是否已存在
     * 
     * @param roleCode 角色编码
     * @param deleted 删除标记
     * @return 是否存在
     */
    boolean existsByRoleCodeAndDeleted(String roleCode, Integer deleted);
    
    /**
     * 检查角色名称是否已存在
     * 
     * @param name 角色名称
     * @param deleted 删除标记
     * @return 是否存在
     */
    boolean existsByNameAndDeleted(String name, Integer deleted);
    
    /**
     * 根据用户ID查询用户的角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r, UserRole ur WHERE r.id = ur.roleId AND ur.userId = :userId AND r.deleted = 0")
    List<Role> findRolesByUserId(@Param("userId") String userId);
}