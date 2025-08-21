package com.company.financial.common.response;

import lombok.Data;

/**
 * 统一响应格式
 * 
 * @author System
 */
@Data
public class ResponseData<T> {
    
    private Integer code;
    
    private String message;
    
    private T data;
    
    private Long timestamp;
    
    public ResponseData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ResponseData(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> ResponseData<T> success() {
        return new ResponseData<>(200, "操作成功", null);
    }
    
    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<>(200, "操作成功", data);
    }
    
    public static <T> ResponseData<T> success(String message, T data) {
        return new ResponseData<>(200, message, data);
    }
    
    public static <T> ResponseData<T> error(String message) {
        return new ResponseData<>(500, message, null);
    }
    
    public static <T> ResponseData<T> error(Integer code, String message) {
        return new ResponseData<>(code, message, null);
    }
}