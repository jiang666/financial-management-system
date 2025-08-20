package com.company.financial.entity;

/**
 * 辅助核算类型枚举
 */
public enum AuxiliaryType {
    DEPARTMENT("部门核算"),
    CUSTOMER("客户核算"),
    SUPPLIER("供应商核算"),
    PROJECT("项目核算"),
    EMPLOYEE("员工核算"),
    PRODUCT("产品核算");

    private final String description;

    AuxiliaryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}