package com.vediofun.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway状态和健康检查控制器
 * 
 * 提供网关状态信息和基本的健康检查接口
 * 
 * @author VedioFun Team
 */
@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    private final RouteLocator routeLocator;

    public GatewayController(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    /**
     * 网关健康检查
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", applicationName);
        response.put("port", serverPort);
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Gateway is running normally");
        
        log.info("🔍 Gateway Health Check - Status: UP");
        return Mono.just(response);
    }

    /**
     * 网关状态信息
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", applicationName);
        response.put("port", serverPort);
        response.put("timestamp", LocalDateTime.now());
        response.put("description", "VedioFun API Gateway - 基于Spring Cloud Gateway的响应式网关");
        response.put("features", new String[]{
            "服务发现与路由",
            "流量控制与熔断",
            "负载均衡",
            "请求日志记录",
            "CORS支持"
        });
        
        return Mono.just(response);
    }

    /**
     * Sentinel测试接口
     */
    @GetMapping("/sentinel/test")
    public Mono<Map<String, Object>> sentinelTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", applicationName);
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Sentinel integration test successful");
        response.put("sentinel_enabled", true);
        
        log.info("🛡️ Sentinel Test - Gateway sentinel integration working");
        return Mono.just(response);
    }
} 