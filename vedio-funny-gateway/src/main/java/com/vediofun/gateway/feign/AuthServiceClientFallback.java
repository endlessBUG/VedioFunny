package com.vediofun.gateway.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务Feign客户端降级处理
 * 
 * 当认证服务不可用时的降级逻辑
 * 
 * @author VedioFun Team
 */
@Slf4j
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public Map<String, Object> validateToken(String token) {
        log.warn("认证服务不可用，Token验证降级处理");
        Map<String, Object> result = new HashMap<>();
        result.put("valid", false);
        result.put("error", "认证服务暂时不可用");
        result.put("fallback", true);
        return result;
    }

    @Override
    public Map<String, Object> getAuthHealth() {
        log.warn("认证服务不可用，健康检查降级处理");
        Map<String, Object> result = new HashMap<>();
        result.put("service", "vedio-funny-auth");
        result.put("status", "DOWN");
        result.put("error", "认证服务暂时不可用");
        result.put("fallback", true);
        return result;
    }
} 