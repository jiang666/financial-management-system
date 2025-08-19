package com.company.financial.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    
    private String id;
    private String username;
    private String email;
    private String realName;
    private String phone;
    private String departmentId;
    private String departmentName;
    private Integer status;
    private String statusText;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Set<RoleDTO> roles;
    private Set<String> permissions;
    
    /**
     * 角色信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDTO {
        private String id;
        private String code;
        private String name;
        private String description;
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "启用" : "禁用";
    }
}