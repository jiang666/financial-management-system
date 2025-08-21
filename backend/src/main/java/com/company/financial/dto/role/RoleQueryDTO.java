package com.company.financial.dto.role;

import lombok.Data;

/**
 * 角色查询DTO
 * 
 * @author System
 */
@Data
public class RoleQueryDTO {
    
    private String roleCode;
    
    private String name;
    
    private String status;
    
    private Integer page = 0;
    
    private Integer size = 10;
}