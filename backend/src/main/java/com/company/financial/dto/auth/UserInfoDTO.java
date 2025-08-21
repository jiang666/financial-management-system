package com.company.financial.dto.auth;

import lombok.Data;

/**
 * 用户信息DTO
 * 
 * @author System
 */
@Data
public class UserInfoDTO {
    
    private String id;
    
    private String username;
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private String department;
    
    private String position;
    
    private String status;
    
    private Long createTime;
    
    private Long lastLoginTime;
}