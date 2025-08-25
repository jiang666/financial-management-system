package com.company.financial.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * 基础实体类
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {
    
    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private Long createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;
    
    /**
     * 创建人
     */
    @Column(name = "created_by", length = 36)
    private String createdBy;
    
    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 36)
    private String updatedBy;
    
    /**
     * 删除标记 0-未删除 1-已删除
     */
    @Column(name = "deleted", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer deleted = 0;
}