package com.company.financial.exception;

/**
 * 错误请求异常（400）
 */
public class BadRequestException extends BusinessException {
    
    public BadRequestException(String message) {
        super(400, message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(400, message, cause);
    }
}