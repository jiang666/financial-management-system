package com.company.financial.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 科目树形结构DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTreeDTO {

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
     * 科目全称（编码-名称）
     */
    private String label;

    /**
     * 父级科目ID
     */
    private String parentId;

    /**
     * 科目级次
     */
    private Integer level;

    /**
     * 科目类型
     */
    private String accountType;

    /**
     * 余额方向
     */
    private String balanceDirection;

    /**
     * 是否叶子节点
     */
    private Boolean isLeaf;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 辅助核算类型
     */
    private List<String> auxiliaryTypes;

    /**
     * 子科目列表
     */
    @Builder.Default
    private List<AccountTreeDTO> children = new ArrayList<>();

    /**
     * 是否有子节点
     */
    private Boolean hasChildren;

    /**
     * 是否展开
     */
    @Builder.Default
    private Boolean expanded = false;

    /**
     * 是否可选
     */
    @Builder.Default
    private Boolean selectable = true;

    /**
     * 是否禁用
     */
    private Boolean disabled;
}