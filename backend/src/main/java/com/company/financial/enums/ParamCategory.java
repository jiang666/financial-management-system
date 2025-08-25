package com.company.financial.enums;

import lombok.Getter;

/**
 * 系统参数分类枚举
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Getter
public enum ParamCategory {
    BASIC("BASIC", "基础设置"),
    FINANCIAL("FINANCIAL", "财务核算"),
    CUSTOMER_SUPPLIER("CUSTOMER_SUPPLIER", "往来管理"),
    REPORT("REPORT", "报表参数"),
    SYSTEM("SYSTEM", "系统管理"),
    INTEGRATION("INTEGRATION", "接口集成");
    
    private final String code;
    private final String description;
    
    ParamCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据code获取枚举
     * 
     * @param code 分类代码
     * @return 枚举对象
     */
    public static ParamCategory fromCode(String code) {
        for (ParamCategory category : ParamCategory.values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
    
    /**
     * 检查code是否有效
     * 
     * @param code 分类代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}