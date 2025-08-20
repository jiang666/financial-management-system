package com.company.financial.exception;

/**
 * 资源未找到异常（404）
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
}