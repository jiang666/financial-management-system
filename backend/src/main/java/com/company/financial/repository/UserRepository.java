package com.company.financial.repository;

import com.company.financial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author System
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsernameAndDeleted(String username, Integer deleted);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmailAndDeleted(String email, Integer deleted);
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @param deleted 删除标记
     * @return 用户信息
     */
    Optional<User> findByIdAndDeleted(String id, Integer deleted);
    
    /**
     * 分页查询用户列表
     * 
     * @param username 用户名
     * @param realName 真实姓名
     * @param department 部门
     * @param status 状态
     * @param deleted 删除标记
     * @param pageable 分页参数
     * @return 用户分页数据
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:realName IS NULL OR u.realName LIKE %:realName%) AND " +
           "(:department IS NULL OR u.department LIKE %:department%) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "u.deleted = :deleted")
    Page<User> findUsersByConditions(@Param("username") String username,
                                   @Param("realName") String realName,
                                   @Param("department") String department,
                                   @Param("status") String status,
                                   @Param("deleted") Integer deleted,
                                   Pageable pageable);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @param deleted 删除标记
     * @return 是否存在
     */
    boolean existsByUsernameAndDeleted(String username, Integer deleted);
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @param deleted 删除标记
     * @return 是否存在
     */
    boolean existsByEmailAndDeleted(String email, Integer deleted);
    
    /**
     * 统计部门下的用户数量
     * 
     * @param departmentId 部门ID
     * @param deleted 删除标记
     * @return 用户数量
     */
    Integer countByDepartmentIdAndDeleted(String departmentId, Integer deleted);
    
    /**
     * 根据部门ID查询用户列表
     * 
     * @param departmentId 部门ID
     * @param deleted 删除标记
     * @return 用户列表
     */
    List<User> findByDepartmentIdAndDeleted(String departmentId, Integer deleted);
    
    /**
     * 查询未分配部门的员工
     * 
     * @return 可用员工列表
     */
    @Query("SELECT u FROM User u WHERE u.departmentId IS NULL AND u.deleted = 0")
    List<User> findAvailableEmployees();
    
    /**
     * 根据部门ID和岗位ID查询用户
     * 
     * @param departmentId 部门ID
     * @param positionId 岗位ID
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.departmentId = :departmentId AND u.positionId = :positionId AND u.deleted = 0")
    List<User> findByDepartmentIdAndPositionId(@Param("departmentId") String departmentId, @Param("positionId") String positionId);
    
    /**
     * 统计岗位下的用户数量
     * 
     * @param positionId 岗位ID
     * @param deleted 删除标记
     * @return 用户数量
     */
    Integer countByPositionIdAndDeleted(String positionId, Integer deleted);
}