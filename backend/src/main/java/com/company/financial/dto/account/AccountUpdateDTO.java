package com.company.financial.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 更新科目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUpdateDTO {

    /**
     * 科目ID
     */
    @NotBlank(message = "科目ID不能为空")
    private String id;

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
    private String currencyCode;

    /**
     * 是否数量核算
     */
    private Boolean isQuantity;

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
    private Integer status;
}