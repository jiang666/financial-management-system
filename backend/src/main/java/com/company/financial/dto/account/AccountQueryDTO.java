package com.company.financial.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 科目查询DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountQueryDTO {

    /**
     * 科目编码
     */
    private String code;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 科目类型
     */
    private String accountType;

    /**
     * 科目级次
     */
    private Integer level;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 父级科目ID
     */
    private String parentId;

    /**
     * 是否叶子节点
     */
    private Boolean isLeaf;

    /**
     * 辅助核算类型
     */
    private String auxiliaryType;

    /**
     * 页码
     */
    private Integer page = 0;

    /**
     * 每页大小
     */
    private Integer size = 20;
}