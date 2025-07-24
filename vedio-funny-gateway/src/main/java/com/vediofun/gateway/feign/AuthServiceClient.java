package com.vediofun.gateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * 认证服务Feign客户端
 * 
 * 用于Gateway调用认证服务的接口
 * 
 * @author VedioFun Team
 */
@FeignClient(
    name = "vedio-funny-auth",
    fallback = AuthServiceClientFallback.class
)
public interface AuthServiceClient {

    /**
     * 验证用户Token
     */
    @PostMapping("/auth/validate")
    Map<String, Object> validateToken(@RequestHeader("Authorization") String token);

    /**
     * 获取认证服务健康状态
     */
    @GetMapping("/auth/health")
    Map<String, Object> getAuthHealth();
} 