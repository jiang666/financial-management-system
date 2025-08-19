package com.company.financial.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";
    
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 刷新令牌（可选）
     */
    private String refreshToken;
    
    /**
     * 用户信息
     */
    private UserInfoDTO userInfo;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色列表
     */
    private Set<String> roles;
    
    /**
     * 权限列表
     */
    private Set<String> permissions;
    
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}