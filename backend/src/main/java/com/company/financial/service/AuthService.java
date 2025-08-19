package com.company.financial.service;

import com.company.financial.dto.auth.ChangePasswordDTO;
import com.company.financial.dto.auth.LoginRequestDTO;
import com.company.financial.dto.auth.LoginResponseDTO;
import com.company.financial.dto.auth.RegisterRequestDTO;
import com.company.financial.dto.auth.UserInfoDTO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应（包含令牌）
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 用户信息
     */
    UserInfoDTO register(RegisterRequestDTO registerRequest);
    
    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    UserInfoDTO getCurrentUserInfo();
    
    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    LoginResponseDTO refreshToken(String refreshToken);
    
    /**
     * 修改密码
     * @param changePasswordDTO 修改密码请求
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
    
    /**
     * 登出
     */
    void logout();
}