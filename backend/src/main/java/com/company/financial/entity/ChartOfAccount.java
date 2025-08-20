package com.company.financial.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 会计科目实体类
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chart_of_accounts")
@Where(clause = "deleted = false")
public class ChartOfAccount {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /**
     * 科目编码
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 科目名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 英文名称
     */
    @Column(name = "english_name", length = 200)
    private String englishName;

    /**
     * 父级科目ID
     */
    @Column(name = "parent_id", length = 36)
    private String parentId;

    /**
     * 科目级次
     */
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * 科目类型: ASSET(资产), LIABILITY(负债), EQUITY(权益), COST(成本), REVENUE(收入)
     */
    @Column(name = "account_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    /**
     * 余额方向: DEBIT(借方), CREDIT(贷方)
     */
    @Column(name = "balance_direction", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private BalanceDirection balanceDirection;

    /**
     * 辅助核算类型(多个用逗号分隔): DEPARTMENT, CUSTOMER, SUPPLIER, PROJECT, EMPLOYEE
     */
    @Column(name = "auxiliary_type", length = 50)
    private String auxiliaryType;

    /**
     * 是否叶子节点
     */
    @Column(name = "is_leaf", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isLeaf = true;

    /**
     * 状态: 1-启用 0-停用
     */
    @Column(name = "status", columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /**
     * 助记码
     */
    @Column(name = "memo_code", length = 20)
    private String memoCode;

    /**
     * 币种代码
     */
    @Column(name = "currency_code", length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'CNY'")
    private String currencyCode = "CNY";

    /**
     * 是否数量核算
     */
    @Column(name = "is_quantity", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isQuantity = false;

    /**
     * 数量单位
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * 科目路径(如: 1001/100101/10010101)
     */
    @Column(name = "path", length = 500)
    private String path;

    /**
     * 完整科目名称(包含上级科目名称)
     */
    @Column(name = "full_name", length = 500)
    private String fullName;

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
     * 是否删除
     */
    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;

    /**
     * 版本号
     */
    @Version
    @Column(name = "version", columnDefinition = "INT DEFAULT 0")
    private Integer version = 0;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.deleted == null) {
            this.deleted = false;
        }
        if (this.status == null) {
            this.status = 1;
        }
        if (this.isLeaf == null) {
            this.isLeaf = true;
        }
        if (this.isQuantity == null) {
            this.isQuantity = false;
        }
        if (this.currencyCode == null || this.currencyCode.isEmpty()) {
            this.currencyCode = "CNY";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 科目类型枚举
     */
    public enum AccountType {
        ASSET("资产"),
        LIABILITY("负债"),
        EQUITY("权益"),
        COST("成本"),
        REVENUE("收入");

        private final String description;

        AccountType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 余额方向枚举
     */
    public enum BalanceDirection {
        DEBIT("借方"),
        CREDIT("贷方");

        private final String description;

        BalanceDirection(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}