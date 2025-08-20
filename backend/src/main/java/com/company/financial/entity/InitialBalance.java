package com.company.financial.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 期初余额实体类
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "initial_balances", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "fiscal_year"}))
public class InitialBalance {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /**
     * 科目ID
     */
    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;

    /**
     * 会计年度
     */
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /**
     * 年初余额
     */
    @Column(name = "beginning_balance", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal beginningBalance = BigDecimal.ZERO;

    /**
     * 年初借方余额
     */
    @Column(name = "beginning_debit", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal beginningDebit = BigDecimal.ZERO;

    /**
     * 年初贷方余额
     */
    @Column(name = "beginning_credit", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal beginningCredit = BigDecimal.ZERO;

    /**
     * 本年借方累计
     */
    @Column(name = "ytd_debit", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal ytdDebit = BigDecimal.ZERO;

    /**
     * 本年贷方累计
     */
    @Column(name = "ytd_credit", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal ytdCredit = BigDecimal.ZERO;

    /**
     * 币种代码
     */
    @Column(name = "currency_code", length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'CNY'")
    private String currencyCode = "CNY";

    /**
     * 辅助核算信息(JSON格式)
     */
    @Column(name = "auxiliary_info", columnDefinition = "JSON")
    private String auxiliaryInfo;

    /**
     * 是否已确认
     */
    @Column(name = "is_confirmed", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isConfirmed = false;

    /**
     * 确认时间
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * 确认人
     */
    @Column(name = "confirmed_by", length = 36)
    private String confirmedBy;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
     * 关联的会计科目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private ChartOfAccount account;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.beginningBalance == null) {
            this.beginningBalance = BigDecimal.ZERO;
        }
        if (this.beginningDebit == null) {
            this.beginningDebit = BigDecimal.ZERO;
        }
        if (this.beginningCredit == null) {
            this.beginningCredit = BigDecimal.ZERO;
        }
        if (this.ytdDebit == null) {
            this.ytdDebit = BigDecimal.ZERO;
        }
        if (this.ytdCredit == null) {
            this.ytdCredit = BigDecimal.ZERO;
        }
        if (this.isConfirmed == null) {
            this.isConfirmed = false;
        }
        if (this.currencyCode == null || this.currencyCode.isEmpty()) {
            this.currencyCode = "CNY";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}