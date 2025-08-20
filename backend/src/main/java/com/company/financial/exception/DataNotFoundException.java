package com.company.financial.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据未找到异常类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataNotFoundException extends RuntimeException {
    
    private Integer code;
    private String message;
    
    public DataNotFoundException(String message) {
        super(message);
        this.code = 404;
        this.message = message;
    }
    
    public DataNotFoundException(String entityName, String id) {
        super(String.format("%s [ID: %s] 不存在", entityName, id));
        this.code = 404;
        this.message = String.format("%s [ID: %s] 不存在", entityName, id);
    }
}