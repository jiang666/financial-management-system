package com.company.financial.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试算平衡DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialBalanceDTO {

    /**
     * 会计年度
     */
    private Integer fiscalYear;

    /**
     * 借方合计
     */
    private BigDecimal totalDebit;

    /**
     * 贷方合计
     */
    private BigDecimal totalCredit;

    /**
     * 差额
     */
    private BigDecimal difference;

    /**
     * 是否平衡
     */
    private Boolean isBalanced;

    /**
     * 资产类合计
     */
    private BigDecimal assetTotal;

    /**
     * 负债类合计
     */
    private BigDecimal liabilityTotal;

    /**
     * 权益类合计
     */
    private BigDecimal equityTotal;

    /**
     * 成本类合计
     */
    private BigDecimal costTotal;

    /**
     * 收入类合计
     */
    private BigDecimal revenueTotal;

    /**
     * 不平衡的科目列表
     */
    private List<UnbalancedAccountDTO> unbalancedAccounts;

    /**
     * 不平衡科目DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnbalancedAccountDTO {
        /**
         * 科目编码
         */
        private String accountCode;

        /**
         * 科目名称
         */
        private String accountName;

        /**
         * 借方金额
         */
        private BigDecimal debitAmount;

        /**
         * 贷方金额
         */
        private BigDecimal creditAmount;

        /**
         * 差额
         */
        private BigDecimal difference;

        /**
         * 建议调整方向
         */
        private String suggestedAdjustment;
    }
}