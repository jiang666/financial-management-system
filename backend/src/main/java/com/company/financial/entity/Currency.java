package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 币种实体类
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Entity
@Table(name = "currencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "code")
    private String code;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "name_en")
    private String nameEn;
    
    @Column(name = "symbol")
    private String symbol;
    
    @Column(name = "is_base")
    private Integer isBase;
    
    @Column(name = "decimal_places")
    private Integer decimalPlaces;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "updated_at")
    private Long updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "deleted")
    private Integer deleted;
}