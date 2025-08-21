package com.company.financial.dto.auth;

import lombok.Data;

import java.util.List;

/**
 * 资源DTO
 * 
 * @author System
 */
@Data
public class ResourceDTO {
    
    private String id;
    
    private String resourceCode;
    
    private String resourceName;
    
    private String resourceType;
    
    private String parentId;
    
    private String url;
    
    private String icon;
    
    private Integer sortOrder;
    
    private String path;
    
    private Integer level;
    
    private String description;
    
    private List<ResourceDTO> children;
}