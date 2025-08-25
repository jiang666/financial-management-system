package com.company.financial.enums;

import lombok.Getter;

/**
 * 系统参数类型枚举
 * 
 * @author Claude
 * @date 2025-08-25
 */
@Getter
public enum ParamType {
    STRING("STRING", "字符串"),
    INTEGER("INTEGER", "整数"),
    DECIMAL("DECIMAL", "小数"),
    BOOLEAN("BOOLEAN", "布尔值"),
    JSON("JSON", "JSON对象"),
    DATE("DATE", "日期"),
    DATETIME("DATETIME", "日期时间"),
    TIME("TIME", "时间"),
    ENUM("ENUM", "枚举");
    
    private final String code;
    private final String description;
    
    ParamType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据code获取枚举
     * 
     * @param code 类型代码
     * @return 枚举对象
     */
    public static ParamType fromCode(String code) {
        for (ParamType type : ParamType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 检查code是否有效
     * 
     * @param code 类型代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}