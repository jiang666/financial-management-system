package com.company.financial.util;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * UUID工具类
 */
@Slf4j
public class UUIDUtils {

    /**
     * 生成32位UUID（去掉连字符）
     */
    public static String generate32() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成标准36位UUID
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /**
     * 验证UUID格式
     */
    public static boolean isValid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 尝试解析UUID
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            // 检查是否是32位格式（无连字符）
            if (uuid.length() == 32) {
                try {
                    // 将32位格式转换为标准格式再验证
                    String formattedUuid = formatUuid(uuid);
                    UUID.fromString(formattedUuid);
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * 将32位UUID格式化为标准36位格式
     */
    public static String formatUuid(String uuid32) {
        if (uuid32 == null || uuid32.length() != 32) {
            throw new IllegalArgumentException("Invalid 32-character UUID format");
        }
        
        return uuid32.substring(0, 8) + "-" +
               uuid32.substring(8, 12) + "-" +
               uuid32.substring(12, 16) + "-" +
               uuid32.substring(16, 20) + "-" +
               uuid32.substring(20, 32);
    }

    /**
     * 标准化UUID格式（统一返回36位格式）
     */
    public static String normalize(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = uuid.trim();
        
        // 如果是32位格式，转换为36位
        if (trimmed.length() == 32 && !trimmed.contains("-")) {
            return formatUuid(trimmed);
        }
        
        // 如果是36位格式，直接返回
        if (trimmed.length() == 36 && isValid(trimmed)) {
            return trimmed.toLowerCase();
        }
        
        throw new IllegalArgumentException("Invalid UUID format: " + uuid);
    }

    /**
     * 生成带前缀的ID
     */
    public static String generateWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return generate();
        }
        return prefix + "_" + generate32();
    }

    /**
     * 批量生成UUID
     */
    public static String[] generateBatch(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        
        String[] uuids = new String[count];
        for (int i = 0; i < count; i++) {
            uuids[i] = generate();
        }
        
        log.debug("Generated {} UUIDs", count);
        return uuids;
    }
}