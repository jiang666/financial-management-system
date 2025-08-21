package com.company.financial.service;

import com.company.financial.dto.user.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 用户服务接口
 * 
 * @author System
 */
public interface UserService {
    
    /**
     * 分页查询用户列表
     * 
     * @param queryDTO 查询条件
     * @return 用户分页数据
     */
    Page<UserDetailDTO> findUsers(UserQueryDTO queryDTO);
    
    /**
     * 根据ID查询用户详情
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    UserDetailDTO findUserById(String id);
    
    /**
     * 创建用户
     * 
     * @param createDTO 创建信息
     * @return 用户详情
     */
    UserDetailDTO createUser(UserCreateDTO createDTO);
    
    /**
     * 更新用户
     * 
     * @param updateDTO 更新信息
     * @return 用户详情
     */
    UserDetailDTO updateUser(UserUpdateDTO updateDTO);
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    void deleteUser(String id);
    
    /**
     * 批量删除用户
     * 
     * @param ids 用户ID列表
     */
    void deleteUsers(List<String> ids);
    
    /**
     * 重置用户密码
     * 
     * @param id 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(String id, String newPassword);
    
    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 状态
     */
    void updateStatus(String id, String status);
    
    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(String userId, List<String> roleIds);
    
    /**
     * 移除用户角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void removeRoles(String userId, List<String> roleIds);
    
    /**
     * 导出用户数据
     * 
     * @param queryDTO 查询条件
     * @return 用户数据
     */
    List<UserDetailDTO> exportUsers(UserQueryDTO queryDTO);
}