package com.vediofun.common.exception;

import com.vediofun.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器（仅适用于Servlet环境）
 * 
 * 只有在Servlet环境中才生效，WebFlux环境（如Gateway）不会加载此配置
 * 统一处理系统异常，提供详细的错误信息和日志记录
 * 
 * @author VedioFun Team
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({HttpServletRequest.class})
public class GlobalExceptionHandler {

    /**
     * 处理系统通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        log.error("系统异常 - URI: {} {}, 错误信息: {}", method, requestUri, e.getMessage(), e);
        
        // 打印详细堆栈信息
        log.error("异常堆栈:", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统内部错误: " + e.getMessage()));
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<String>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        log.error("运行时异常 - URI: {} {}, 错误信息: {}", method, requestUri, e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("运行时错误: " + e.getMessage()));
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("参数校验异常: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error("参数校验失败", errors));
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Map<String, String>>> handleBindException(BindException e) {
        log.error("绑定异常: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error("数据绑定失败", errors));
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<String>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        log.error("空指针异常 - URI: {} {}, 错误信息: {}", method, requestUri, e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("空指针异常: " + e.getMessage()));
    }

    /**
     * 处理类型转换异常
     */
    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<Result<String>> handleClassCastException(ClassCastException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        log.error("类型转换异常 - URI: {} {}, 错误信息: {}", method, requestUri, e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("类型转换错误: " + e.getMessage()));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<String>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        log.error("非法参数异常 - URI: {} {}, 错误信息: {}", method, requestUri, e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error("参数错误: " + e.getMessage()));
    }
} 