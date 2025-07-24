package com.vediofun.common.result;

/**
 * 响应码枚举
 * 
 * @author VedioFun Team
 */
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误
    ERROR(500, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),
    UNPROCESSABLE_ENTITY(422, "请求参数验证失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务器错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // 业务错误
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_INVALID(1004, "Token无效"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    PERMISSION_DENIED(1006, "权限不足"),

    // 系统错误
    DATABASE_ERROR(2001, "数据库操作失败"),
    REDIS_ERROR(2002, "Redis操作失败"),
    FILE_UPLOAD_ERROR(2003, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(2004, "文件下载失败"),
    NETWORK_ERROR(2005, "网络连接异常"),

    // 业务逻辑错误
    PARAM_VALIDATION_ERROR(3001, "参数校验失败"),
    DATA_NOT_FOUND(3002, "数据不存在"),
    DATA_ALREADY_EXISTS(3003, "数据已存在"),
    OPERATION_NOT_ALLOWED(3004, "操作不被允许"),
    STATUS_ERROR(3005, "状态异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ResultCode{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
} 