package com.company.financial.utils;

import java.util.UUID;

/**
 * UUID工具类
 * 
 * @author Claude
 * @date 2025-08-25
 */
public class UUIDUtils {
    
    /**
     * 生成UUID
     * 
     * @return UUID字符串（不含横线）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成标准UUID
     * 
     * @return UUID字符串（含横线）
     */
    public static String generateStandardUUID() {
        return UUID.randomUUID().toString();
    }
}