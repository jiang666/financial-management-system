package com.company.financial.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 系统参数历史记录实体类
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Data
@Entity
@Table(name = "system_parameter_history")
public class SystemParameterHistory {
    
    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    
    /**
     * 参数键
     */
    @Column(name = "param_key", nullable = false, length = 100)
    private String paramKey;
    
    /**
     * 旧值
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    /**
     * 新值
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    /**
     * 操作类型: CREATE, UPDATE, DELETE
     */
    @Column(name = "operation", nullable = false, length = 20)
    private String operation;
    
    /**
     * 操作人
     */
    @Column(name = "operator", length = 50)
    private String operator;
    
    /**
     * 操作时间
     */
    @Column(name = "operate_time", nullable = false)
    private Long operateTime;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
    /**
     * 操作IP
     */
    @Column(name = "operate_ip", length = 50)
    private String operateIp;
    
    /**
     * 参数类型
     */
    @Column(name = "param_type", length = 20)
    private String paramType;
    
    /**
     * 参数分类
     */
    @Column(name = "category", length = 50)
    private String category;
}