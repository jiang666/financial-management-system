package com.company.financial.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会计期间实体类
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounting_periods")
public class AccountingPeriod {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /**
     * 会计年度
     */
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /**
     * 会计期间(1-12月)
     */
    @Column(name = "period", nullable = false)
    private Integer period;

    /**
     * 期间名称(如: 2025年1月)
     */
    @Column(name = "period_name", nullable = false, length = 50)
    private String periodName;

    /**
     * 期间开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 期间结束日期
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 期间状态: OPEN(开启), CLOSED(已结账), LOCKED(已锁定)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PeriodStatus status = PeriodStatus.OPEN;

    /**
     * 是否当前期间
     */
    @Column(name = "is_current", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isCurrent = false;

    /**
     * 是否调整期
     */
    @Column(name = "is_adjustment", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdjustment = false;

    /**
     * 结账时间
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * 结账人
     */
    @Column(name = "closed_by", length = 36)
    private String closedBy;

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

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = PeriodStatus.OPEN;
        }
        if (this.isCurrent == null) {
            this.isCurrent = false;
        }
        if (this.isAdjustment == null) {
            this.isAdjustment = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 期间状态枚举
     */
    public enum PeriodStatus {
        OPEN("开启"),
        CLOSED("已结账"),
        LOCKED("已锁定");

        private final String description;

        PeriodStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}