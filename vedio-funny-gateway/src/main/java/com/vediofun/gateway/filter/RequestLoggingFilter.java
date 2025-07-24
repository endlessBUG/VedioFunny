package com.vediofun.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志记录过滤器
 * 
 * 记录通过网关的所有请求信息
 * 
 * @author VedioFun Team
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        
        // 记录请求信息
        log.info("🌐 Gateway Request - Method: {}, Path: {}, RemoteAddr: {}", 
                request.getMethod(),
                request.getPath().value(),
                request.getRemoteAddress());
        
        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                log.info("✅ Gateway Response - Path: {}, Duration: {}ms, Status: {}", 
                        request.getPath().value(),
                        duration,
                        exchange.getResponse().getStatusCode());
            })
        );
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
} 