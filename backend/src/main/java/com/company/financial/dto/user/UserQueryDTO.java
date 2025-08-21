package com.company.financial.dto.user;

import lombok.Data;

/**
 * 用户查询DTO
 * 
 * @author System
 */
@Data
public class UserQueryDTO {
    
    private String username;
    
    private String realName;
    
    private String department;
    
    private String status;
    
    private Integer page = 0;
    
    private Integer size = 10;
}