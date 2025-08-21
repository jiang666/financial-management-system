package com.company.financial.dto.role;

import lombok.Data;

import java.util.List;

/**
 * 角色详情DTO
 * 
 * @author System
 */
@Data
public class RoleDetailDTO {
    
    private String id;
    
    private String roleCode;
    
    private String name;
    
    private String description;
    
    private String status;
    
    private Integer canDelete;
    
    private Integer canModify;
    
    private Integer userCount;
    
    private Long createTime;
    
    private Long updateTime;
    
    private String createBy;
    
    private String updateBy;
    
    private List<ResourceInfo> resources;
    
    @Data
    public static class ResourceInfo {
        private String id;
        private String resourceCode;
        private String resourceName;
        private String resourceType;
        private String parentId;
        private String url;
        private String icon;
        private Integer sortOrder;
        private Integer level;
        private String description;
    }
}