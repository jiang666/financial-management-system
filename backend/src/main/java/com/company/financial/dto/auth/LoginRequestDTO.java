package com.company.financial.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author System
 */
@Data
public class LoginRequestDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private Boolean rememberMe = false;
}