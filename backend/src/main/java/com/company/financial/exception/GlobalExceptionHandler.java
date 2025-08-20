package com.company.financial.exception;

import com.company.financial.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.fail(e.getCode(), e.getMessage());
    }

    /**
     * 数据校验异常处理
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("数据校验异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.fail(e.getCode(), e.getMessage());
    }

    /**
     * 数据未找到异常处理
     */
    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleDataNotFoundException(DataNotFoundException e, HttpServletRequest request) {
        log.warn("数据未找到异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.fail(e.getCode(), e.getMessage());
    }

    /**
     * Bean Validation 校验失败异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 [{}]: {}", request.getRequestURI(), errorMessage);
        return ResponseEntity.fail(400, "参数校验失败: " + errorMessage);
    }

    /**
     * Bean Validation 绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBindException(BindException e, HttpServletRequest request) {
        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("数据绑定失败 [{}]: {}", request.getRequestURI(), errorMessage);
        return ResponseEntity.fail(400, "数据绑定失败: " + errorMessage);
    }

    /**
     * 约束违反异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束违反异常 [{}]: {}", request.getRequestURI(), errorMessage);
        return ResponseEntity.fail(400, "约束违反: " + errorMessage);
    }

    /**
     * 参数类型不匹配异常处理
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String errorMessage = String.format("参数 [%s] 类型不正确，期望类型: %s", 
                e.getName(), e.getRequiredType().getSimpleName());
        log.warn("参数类型错误 [{}]: {}", request.getRequestURI(), errorMessage);
        return ResponseEntity.fail(400, errorMessage);
    }

    /**
     * 非法参数异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.fail(400, "参数错误: " + e.getMessage());
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.fail(500, "系统内部错误，请联系管理员");
    }
}