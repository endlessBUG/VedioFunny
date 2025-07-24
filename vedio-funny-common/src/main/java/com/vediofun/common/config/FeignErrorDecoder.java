package com.vediofun.common.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * FeignClient自定义错误解码器
 * 
 * 将HTTP错误响应转换为具体的异常类型，并记录详细的错误日志
 * 
 * @author VedioFun Team
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String requestUrl = response.request().url();
        int status = response.status();
        String reason = response.reason();
        
        // 记录详细的错误信息
        log.error("🚨 FeignClient调用失败:");
        log.error("   📍 方法: {}", methodKey);
        log.error("   🌐 URL: {}", requestUrl);
        log.error("   📊 状态码: {} {}", status, reason);
        
        // 尝试读取响应体
        String responseBody = getResponseBody(response);
        if (responseBody != null && !responseBody.isEmpty()) {
            log.error("   📋 响应体: {}", responseBody);
        }
        
        // 记录请求头信息
        if (response.request().headers() != null) {
            log.error("   📤 请求头: {}", response.request().headers());
        }
        
        // 记录响应头信息
        if (response.headers() != null) {
            log.error("   📥 响应头: {}", response.headers());
        }

        // 根据状态码返回具体的异常
        switch (status) {
            case 400:
                log.error("   ❌ 错误类型: 请求参数错误 (400 Bad Request)");
                return new FeignClientException("请求参数错误: " + responseBody, status, requestUrl);
                
            case 401:
                log.error("   ❌ 错误类型: 认证失败 (401 Unauthorized)");
                return new FeignClientException("认证失败，令牌无效或已过期: " + responseBody, status, requestUrl);
                
            case 403:
                log.error("   ❌ 错误类型: 权限不足 (403 Forbidden)");
                return new FeignClientException("权限不足，无法访问资源: " + responseBody, status, requestUrl);
                
            case 404:
                log.error("   ❌ 错误类型: 资源不存在 (404 Not Found)");
                return new FeignClientException("请求的资源不存在: " + requestUrl, status, requestUrl);
                
            case 500:
                log.error("   ❌ 错误类型: 服务器内部错误 (500 Internal Server Error)");
                return new FeignClientException("目标服务内部错误: " + responseBody, status, requestUrl);
                
            case 503:
                log.error("   ❌ 错误类型: 服务不可用 (503 Service Unavailable)");
                return new FeignClientException("目标服务暂时不可用，请稍后重试", status, requestUrl);
                
            default:
                log.error("   ❌ 错误类型: 未知错误 ({} {})", status, reason);
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }

    /**
     * 读取响应体内容
     */
    private String getResponseBody(Response response) {
        try {
            if (response.body() != null) {
                byte[] bodyBytes = new byte[response.body().length()];
                response.body().asInputStream().read(bodyBytes);
                return new String(bodyBytes, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("⚠️  无法读取FeignClient响应体: {}", e.getMessage());
        }
        return null;
    }

    /**
     * FeignClient自定义异常类
     */
    public static class FeignClientException extends RuntimeException {
        private final int status;
        private final String url;

        public FeignClientException(String message, int status, String url) {
            super(String.format("[%d] %s (URL: %s)", status, message, url));
            this.status = status;
            this.url = url;
        }

        public int getStatus() {
            return status;
        }

        public String getUrl() {
            return url;
        }
    }
} 