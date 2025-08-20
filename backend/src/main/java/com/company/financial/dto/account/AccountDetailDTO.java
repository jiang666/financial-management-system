package com.company.financial.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 科目详情DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDetailDTO {

    /**
     * 科目ID
     */
    private String id;

    /**
     * 科目编码
     */
    private String code;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 英文名称
     */
    private String englishName;

    /**
     * 父级科目ID
     */
    private String parentId;

    /**
     * 父级科目编码
     */
    private String parentCode;

    /**
     * 父级科目名称
     */
    private String parentName;

    /**
     * 科目级次
     */
    private Integer level;

    /**
     * 科目类型
     */
    private String accountType;

    /**
     * 科目类型名称
     */
    private String accountTypeName;

    /**
     * 余额方向
     */
    private String balanceDirection;

    /**
     * 余额方向名称
     */
    private String balanceDirectionName;

    /**
     * 辅助核算类型列表
     */
    private List<String> auxiliaryTypes;

    /**
     * 是否叶子节点
     */
    private Boolean isLeaf;

    /**
     * 状态: 1-启用 0-停用
     */
    private Integer status;

    /**
     * 助记码
     */
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
    private String unit;

    /**
     * 科目路径
     */
    private String path;

    /**
     * 完整科目名称
     */
    private String fullName;

    /**
     * 子科目数量
     */
    private Integer childrenCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;
}