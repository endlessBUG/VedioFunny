package com.vediofun.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vediofun.gateway.feign.AuthServiceClient;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 网关全局认证过滤器 - 增强版SSO支持
 * 实现统一认证、令牌验证、用户信息传递等功能
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    @Lazy
    private AuthServiceClient authFeignClient;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 不需要认证的路径白名单
    private static final List<String> SKIP_AUTH_PATHS = Arrays.asList(
        "/api/auth/auth/login",
        "/api/auth/auth/register",
        "/api/auth/auth/health", 
        "/api/auth/auth/test",
        "/api/auth/auth/check-username",
        "/api/auth/auth/validate",
        "/api/auth/auth/refresh",
        "/gateway/info",
        "/gateway/routes", 
        "/gateway/status",
        "/actuator",
        "/doc.html",
        "/webjars",
        "/swagger-resources",
        "/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        // 跳过不需要认证的路径
        if (shouldSkipAuth(path)) {
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("请求缺少认证令牌: {} {}", method, path);
            return unauthorizedResponse(response, "缺少认证令牌", 40101);
        }

        // SSO令牌验证
        return validateTokenWithSSO(authHeader)
            .flatMap(validationResult -> {
                if (validationResult.isValid()) {
                    // 令牌有效，添加用户信息到请求头，传递给下游服务
                    ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", validationResult.getUserId())
                        .header("X-Username", validationResult.getUsername())
                        .header("X-User-Type", validationResult.getUserType())
                        .header("X-Auth-Source", "gateway-sso")
                        .build();
                    
                    ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();
                    
                    log.info("认证成功: 用户={}, 路径={}", 
                        validationResult.getUsername(), path);
                    
                    return chain.filter(modifiedExchange);
                } else {
                    log.warn("SSO令牌验证失败: 路径={}, 原因={}", path, validationResult.getErrorMessage());
                    return unauthorizedResponse(response, validationResult.getErrorMessage(), 40102);
                }
            })
            .onErrorResume(error -> {
                log.error("SSO认证服务调用异常: {} {}, 错误: {}", method, path, error.getMessage());
                return unauthorizedResponse(response, "认证服务暂时不可用", 40103);
            });
    }

    /**
     * 检查是否跳过认证
     */
    private boolean shouldSkipAuth(String path) {
        return SKIP_AUTH_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * SSO令牌验证（FeignClient版本）
     */
    private Mono<ValidationResult> validateTokenWithSSO(String authHeader) {
        return Mono.fromCallable(() -> {
                // 使用FeignClient进行阻塞调用
                Map<String, Object> response = authFeignClient.validateToken(authHeader);
                return parseValidationResponseFromMap(response);
            })
            .onErrorReturn(FeignException.Unauthorized.class, ValidationResult.invalid("令牌无效或已过期"))
            .onErrorReturn(FeignException.Forbidden.class, ValidationResult.invalid("权限不足"))
            .onErrorResume(FeignException.class, ex -> {
                log.warn("令牌验证服务异常: 状态码={}, 响应={}", ex.status(), ex.contentUTF8());
                return Mono.just(ValidationResult.invalid("认证服务异常"));
            })
            .onErrorResume(Exception.class, ex -> {
                log.error("令牌验证过程异常: {}", ex.getMessage());
                return Mono.just(ValidationResult.invalid("令牌验证失败"));
            })
            .subscribeOn(Schedulers.boundedElastic()); // 在弹性线程池执行阻塞调用
    }

    /**
     * 解析验证响应 (String版本)
     */
    private ValidationResult parseValidationResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("code") && jsonNode.get("code").asInt() == 200) {
                JsonNode dataNode = jsonNode.get("data");
                if (dataNode != null && dataNode.has("valid") && dataNode.get("valid").asBoolean()) {
                    return ValidationResult.valid(
                        dataNode.get("userId").asText(),
                        dataNode.get("username").asText(),
                        dataNode.get("userType").asText()
                    );
                }
            }
            
            String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "令牌验证失败";
            return ValidationResult.invalid(message);
            
        } catch (Exception e) {
            log.warn("解析令牌验证响应失败: {}", e.getMessage());
            return ValidationResult.invalid("令牌验证响应格式错误");
        }
    }

    /**
     * 解析验证响应 (Map版本 - FeignClient返回)
     */
    private ValidationResult parseValidationResponseFromMap(Map<String, Object> response) {
        try {
            if (response.containsKey("code") && Integer.valueOf(200).equals(response.get("code"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
                if (dataMap != null && Boolean.TRUE.equals(dataMap.get("valid"))) {
                    return ValidationResult.valid(
                        String.valueOf(dataMap.get("userId")),
                        String.valueOf(dataMap.get("username")),
                        String.valueOf(dataMap.get("userType"))
                    );
                }
            }
            
            String message = response.containsKey("message") ? 
                String.valueOf(response.get("message")) : "令牌验证失败";
            return ValidationResult.invalid(message);
            
        } catch (Exception e) {
            log.warn("解析令牌验证响应失败: {}", e.getMessage());
            return ValidationResult.invalid("令牌验证响应格式错误");
        }
    }

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message, int code) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
        response.getHeaders().add("Pragma", "no-cache");
        response.getHeaders().add("Expires", "0");
        
        String body = String.format("""
            {
                "code": %d,
                "message": "%s",
                "timestamp": %d,
                "path": "%s",
                "sso": true
            }
            """, 
            code, message, System.currentTimeMillis(), "gateway-auth"
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 确保在其他过滤器之前执行
    }

    /**
     * 验证结果封装类
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String userId;
        private final String username; 
        private final String userType;
        private final String errorMessage;

        private ValidationResult(boolean valid, String userId, String username, String userType, String errorMessage) {
            this.valid = valid;
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid(String userId, String username, String userType) {
            return new ValidationResult(true, userId, username, userType, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, null, null, null, errorMessage);
        }

        public boolean isValid() { return valid; }
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getUserType() { return userType; }
        public String getErrorMessage() { return errorMessage; }
    }
} 