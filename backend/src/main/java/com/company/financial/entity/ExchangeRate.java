package com.company.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 汇率实体类
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Entity
@Table(name = "exchange_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "from_currency_id")
    private String fromCurrencyId;
    
    @Column(name = "to_currency_id")
    private String toCurrencyId;
    
    @Column(name = "rate", precision = 20, scale = 8)
    private BigDecimal rate;
    
    @Column(name = "rate_type")
    private String rateType;
    
    @Column(name = "effective_date")
    private Long effectiveDate;
    
    @Column(name = "expiry_date")
    private Long expiryDate;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "remark")
    private String remark;
    
    @Column(name = "status")
    private Integer status;
    
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