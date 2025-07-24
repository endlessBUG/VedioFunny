package com.vediofun.common.exception;

import com.vediofun.common.result.ResultCode;

/**
 * 业务异常类
 * 
 * @author VedioFun Team
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 创建业务异常
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /**
     * 创建业务异常
     */
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 创建业务异常
     */
    public static BusinessException of(ResultCode resultCode) {
        return new BusinessException(resultCode);
    }
} 