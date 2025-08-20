package com.company.financial.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据校验异常类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends RuntimeException {
    
    private Integer code;
    private String message;
    
    public ValidationException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }
    
    public ValidationException(String field, String message) {
        super(String.format("字段 [%s] %s", field, message));
        this.code = 400;
        this.message = String.format("字段 [%s] %s", field, message);
    }
}