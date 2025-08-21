package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 资源实体类
 * 
 * @author System
 */
@Entity
@Table(name = "tb_resource")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "create_time")
    private Long createTime;
    
    @Column(name = "update_time")
    private Long updateTime;
    
    @Column(name = "create_by")
    private String createBy;
    
    @Column(name = "update_by")
    private String updateBy;
    
    @Column(name = "resource_code")
    private String resourceCode;
    
    @Column(name = "resource_name")
    private String resourceName;
    
    @Column(name = "resource_type")
    private String resourceType;
    
    @Column(name = "parent_id")
    private String parentId;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "icon")
    private String icon;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(name = "path")
    private String path;
    
    @Column(name = "level")
    private Integer level;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "deleted")
    private Integer deleted;
}