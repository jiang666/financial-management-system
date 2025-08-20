package com.company.financial.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 创建科目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreateDTO {

    /**
     * 科目编码
     */
    @NotBlank(message = "科目编码不能为空")
    @Pattern(regexp = "^[0-9]{4,20}$", message = "科目编码只能包含数字，长度4-20位")
    private String code;

    /**
     * 科目名称
     */
    @NotBlank(message = "科目名称不能为空")
    @Size(max = 200, message = "科目名称长度不能超过200")
    private String name;

    /**
     * 英文名称
     */
    @Size(max = 200, message = "英文名称长度不能超过200")
    private String englishName;

    /**
     * 父级科目ID
     */
    private String parentId;

    /**
     * 科目类型
     */
    @NotNull(message = "科目类型不能为空")
    private String accountType;

    /**
     * 余额方向
     */
    @NotNull(message = "余额方向不能为空")
    private String balanceDirection;

    /**
     * 辅助核算类型列表
     */
    private List<String> auxiliaryTypes;

    /**
     * 助记码
     */
    @Size(max = 20, message = "助记码长度不能超过20")
    private String memoCode;

    /**
     * 币种代码
     */
    private String currencyCode = "CNY";

    /**
     * 是否数量核算
     */
    private Boolean isQuantity = false;

    /**
     * 数量单位
     */
    @Size(max = 20, message = "数量单位长度不能超过20")
    private String unit;

    /**
     * 状态: 1-启用 0-停用
     */
    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    private Integer status = 1;
}