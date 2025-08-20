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
import java.util.Set;

/**
 * 角色数据访问接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    
    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByCode(String code);
    
    /**
     * 根据编码查找未删除的角色
     */
    Optional<Role> findByCodeAndDeletedFalse(String code);
    
    /**
     * 根据ID查找未删除的角色
     */
    Optional<Role> findByIdAndDeletedFalse(String id);
    
    /**
     * 根据状态查找未删除的角色
     */
    List<Role> findByDeletedFalseAndStatus(Integer status);
    
    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据角色编码查找角色（包含权限信息）
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.code = :code")
    Optional<Role> findByCodeWithPermissions(@Param("code") String code);
    
    /**
     * 根据ID查找角色（包含权限信息）
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") String id);
    
    /**
     * 查找所有启用的角色
     */
    List<Role> findByStatus(Integer status);
    
    /**
     * 根据角色编码批量查找
     */
    List<Role> findByCodeIn(Set<String> codes);
    
    /**
     * 分页查询角色
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:code IS NULL OR r.code LIKE %:code%) AND " +
           "(:name IS NULL OR r.name LIKE %:name%) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<Role> findByConditions(@Param("code") String code,
                                @Param("name") String name,
                                @Param("status") Integer status,
                                Pageable pageable);
    
    /**
     * 根据用户ID查找角色
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") String userId);
    
    /**
     * 查找拥有指定权限的角色
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.code = :permissionCode")
    List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);
}