package com.company.financial.service;

import com.company.financial.common.ResponsePageDataEntity;
import com.company.financial.dto.user.*;
import org.springframework.data.domain.Page;

/**
 * 用户管理服务接口
 */
public interface UserService {
    
    /**
     * 创建用户
     */
    UserDetailDTO createUser(UserCreateDTO createDTO);
    
    /**
     * 更新用户信息
     */
    UserDetailDTO updateUser(String userId, UserUpdateDTO updateDTO);
    
    /**
     * 根据ID获取用户详细信息
     */
    UserDetailDTO getUserById(String userId);
    
    /**
     * 根据用户名获取用户详细信息
     */
    UserDetailDTO getUserByUsername(String username);
    
    /**
     * 分页查询用户
     */
    ResponsePageDataEntity<UserDetailDTO> queryUsers(UserQueryDTO queryDTO);
    
    /**
     * 删除用户（软删除）
     */
    void deleteUser(String userId);
    
    /**
     * 启用/禁用用户
     */
    void updateUserStatus(String userId, Integer status);
    
    /**
     * 重置用户密码
     */
    void resetUserPassword(String userId, String newPassword);
    
    /**
     * 分配用户角色
     */
    void assignUserRoles(String userId, java.util.List<String> roleIds);
    
    /**
     * 移除用户角色
     */
    void removeUserRoles(String userId, java.util.List<String> roleIds);
}