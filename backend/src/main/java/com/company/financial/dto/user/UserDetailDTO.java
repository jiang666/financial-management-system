package com.company.financial.dto.user;

import lombok.Data;

import java.util.List;

/**
 * 用户详情DTO
 * 
 * @author System
 */
@Data
public class UserDetailDTO {
    
    private String id;
    
    private String username;
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private String department;
    
    private String departmentId;
    
    private String position;
    
    private String status;
    
    private Long lastLoginAt;
    
    private String lastLoginIp;
    
    private Long createTime;
    
    private Long updateTime;
    
    private String createBy;
    
    private String updateBy;
    
    private List<RoleInfo> roles;
    
    @Data
    public static class RoleInfo {
        private String id;
        private String roleCode;
        private String name;
        private String description;
    }
}