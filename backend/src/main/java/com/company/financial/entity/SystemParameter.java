package com.company.financial.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 系统参数实体类
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "system_parameters")
public class SystemParameter extends BaseEntity {
    
    /**
     * 参数键
     */
    @Column(name = "param_key", nullable = false, unique = true, length = 100)
    private String paramKey;
    
    /**
     * 参数值
     */
    @Column(name = "param_value", columnDefinition = "TEXT")
    private String paramValue;
    
    /**
     * 参数类型: STRING, INTEGER, DECIMAL, BOOLEAN, JSON, DATE, DATETIME, TIME, ENUM
     */
    @Column(name = "param_type", nullable = false, length = 20)
    private String paramType;
    
    /**
     * 参数分类: BASIC, FINANCIAL, CUSTOMER_SUPPLIER, REPORT, SYSTEM, INTEGRATION
     */
    @Column(name = "category", length = 50)
    private String category;
    
    /**
     * 参数描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 是否系统参数
     */
    @Column(name = "is_system", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isSystem = false;
    
    /**
     * 是否加密
     */
    @Column(name = "is_encrypted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isEncrypted = false;
    
    /**
     * 排序号
     */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;
    
    /**
     * 验证规则
     */
    @Column(name = "validation_rule", length = 500)
    private String validationRule;
    
    /**
     * 最小值
     */
    @Column(name = "min_value", length = 50)
    private String minValue;
    
    /**
     * 最大值
     */
    @Column(name = "max_value", length = 50)
    private String maxValue;
    
    /**
     * 可选值列表(JSON格式)
     */
    @Column(name = "options", columnDefinition = "TEXT")
    private String options;
    
    /**
     * 默认值
     */
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;
    
    /**
     * 是否必填
     */
    @Column(name = "required", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean required = false;
    
    /**
     * 是否可见
     */
    @Column(name = "visible", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean visible = true;
    
    /**
     * 是否可编辑
     */
    @Column(name = "editable", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean editable = true;
    
    /**
     * 是否立即生效
     */
    @Column(name = "effective_immediately", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean effectiveImmediately = true;
    
    /**
     * 是否需要重启
     */
    @Column(name = "restart_required", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean restartRequired = false;
}