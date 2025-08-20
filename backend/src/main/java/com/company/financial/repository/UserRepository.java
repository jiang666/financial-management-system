package com.company.financial.repository;

import com.company.financial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名查找未删除的用户
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);
    
    /**
     * 根据邮箱查找未删除的用户
     */
    Optional<User> findByEmailAndDeletedFalse(String email);
    
    /**
     * 根据ID查找未删除的用户
     */
    Optional<User> findByIdAndDeletedFalse(String id);
    
    /**
     * 检查用户是否存在（未删除）
     */
    boolean existsByIdAndDeletedFalse(String id);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查用户名是否存在（未删除）
     */
    boolean existsByUsernameAndDeletedFalse(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查邮箱是否存在（未删除）
     */
    boolean existsByEmailAndDeletedFalse(String email);
    
    /**
     * 根据用户名查找用户（包含角色信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    
    /**
     * 根据ID查找用户（包含角色信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") String id);
    
    /**
     * 分页查询用户
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:realName IS NULL OR u.realName LIKE %:realName%) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findByConditions(@Param("username") String username,
                                @Param("realName") String realName,
                                @Param("status") Integer status,
                                Pageable pageable);
    
    /**
     * 更新最后登录时间
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") String userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * 启用用户
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 1, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void enableUser(@Param("userId") String userId);
    
    /**
     * 禁用用户
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 0, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void disableUser(@Param("userId") String userId);
    
    /**
     * 逻辑删除用户
     */
    @Modifying
    @Query("UPDATE User u SET u.deleted = true, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void softDelete(@Param("userId") String userId);
    
    /**
     * 更新密码
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updatePassword(@Param("userId") String userId, @Param("password") String password);
}