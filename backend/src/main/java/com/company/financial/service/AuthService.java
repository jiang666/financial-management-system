package com.company.financial.service;

import com.company.financial.dto.auth.*;

/**
 * 认证服务接口
 * 
 * @author System
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    /**
     * 用户登出
     * 
     * @param token JWT Token
     */
    void logout(String token);
    
    /**
     * 刷新Token
     * 
     * @param refreshToken 刷新Token
     * @return 新的Token信息
     */
    LoginResponseDTO refreshToken(String refreshToken);
    
    /**
     * 修改密码
     * 
     * @param changePasswordDTO 修改密码信息
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
    
    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    UserInfoDTO getCurrentUserInfo();
    
    /**
     * 获取当前用户权限资源
     * 
     * @return 权限资源树
     */
    ResourceDTO getCurrentUserResources();
}