package com.company.financial.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 期初余额DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitialBalanceDTO {

    /**
     * 期初余额ID
     */
    private String id;

    /**
     * 科目ID
     */
    @NotNull(message = "科目ID不能为空")
    private String accountId;

    /**
     * 科目编码
     */
    private String accountCode;

    /**
     * 科目名称
     */
    private String accountName;

    /**
     * 科目类型
     */
    private String accountType;

    /**
     * 余额方向
     */
    private String balanceDirection;

    /**
     * 会计年度
     */
    @NotNull(message = "会计年度不能为空")
    private Integer fiscalYear;

    /**
     * 年初余额
     */
    @DecimalMin(value = "0", message = "年初余额不能为负数")
    private BigDecimal beginningBalance;

    /**
     * 年初借方余额
     */
    @DecimalMin(value = "0", message = "年初借方余额不能为负数")
    private BigDecimal beginningDebit;

    /**
     * 年初贷方余额
     */
    @DecimalMin(value = "0", message = "年初贷方余额不能为负数")
    private BigDecimal beginningCredit;

    /**
     * 本年借方累计
     */
    @DecimalMin(value = "0", message = "本年借方累计不能为负数")
    private BigDecimal ytdDebit;

    /**
     * 本年贷方累计
     */
    @DecimalMin(value = "0", message = "本年贷方累计不能为负数")
    private BigDecimal ytdCredit;

    /**
     * 币种代码
     */
    private String currencyCode = "CNY";

    /**
     * 辅助核算信息
     */
    private String auxiliaryInfo;

    /**
     * 是否已确认
     */
    private Boolean isConfirmed;
}