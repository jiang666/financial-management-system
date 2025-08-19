package com.company.financial.common;

import lombok.Data;

@Data
public class ResponseEntity<T> {
    
    private int code;
    private String message;
    private T data;
    
    private ResponseEntity(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<>(200, "操作成功", data);
    }
    
    public static <T> ResponseEntity<T> success(String message, T data) {
        return new ResponseEntity<>(200, message, data);
    }
    
    public static <T> ResponseEntity<T> fail(String message) {
        return new ResponseEntity<>(500, message, null);
    }
    
    public static <T> ResponseEntity<T> fail(int code, String message) {
        return new ResponseEntity<>(code, message, null);
    }
}