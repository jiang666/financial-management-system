package com.company.financial.common.response;

import lombok.Data;

/**
 * 通用响应结果类
 */
@Data
public class ResponseEntity<T> {
    
    private int code;
    private String message;
    private T data;
    private boolean success;
    
    private ResponseEntity() {}
    
    private ResponseEntity(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseEntity<T> success() {
        return new ResponseEntity<>(200, "操作成功", null, true);
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<>(200, "操作成功", data, true);
    }
    
    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> ResponseEntity<T> success(String message, T data) {
        return new ResponseEntity<>(200, message, data, true);
    }
    
    /**
     * 失败响应
     */
    public static <T> ResponseEntity<T> fail(int code, String message) {
        return new ResponseEntity<>(code, message, null, false);
    }
    
    /**
     * 失败响应（默认错误码500）
     */
    public static <T> ResponseEntity<T> fail(String message) {
        return new ResponseEntity<>(500, message, null, false);
    }
    
    /**
     * 失败响应（带数据）
     */
    public static <T> ResponseEntity<T> fail(int code, String message, T data) {
        return new ResponseEntity<>(code, message, data, false);
    }
    
    /**
     * 错误响应（error别名方法）
     */
    public static <T> ResponseEntity<T> error(int code, String message) {
        return fail(code, message);
    }
}