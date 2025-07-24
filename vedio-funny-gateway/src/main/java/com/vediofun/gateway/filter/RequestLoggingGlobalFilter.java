package com.vediofun.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 请求日志全局过滤器
 * 记录所有经过网关的请求和响应信息
 */
@Slf4j
@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        
        // 记录请求信息
        logRequest(request);
        
        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long endTime = System.currentTimeMillis();
                logResponse(request, response, endTime - startTime);
            })
        );
    }

    private void logRequest(ServerHttpRequest request) {
        log.info("================== 🚀 GATEWAY REQUEST ==================");
        log.info("⏰ Time: {}", LocalDateTime.now().format(formatter));
        log.info("🔗 Method: {}", request.getMethod());
        log.info("🌐 URL: {}", request.getURI());
        log.info("📍 Path: {}", request.getPath().value());
        log.info("🔍 Query: {}", request.getQueryParams());
        log.info("🏠 Remote Address: {}", request.getRemoteAddress());
        log.info("🔤 Headers:");
        request.getHeaders().forEach((name, values) -> {
            if (!name.toLowerCase().contains("authorization")) {
                log.info("   {}: {}", name, values);
            } else {
                log.info("   {}: [MASKED]", name);
            }
        });
        log.info("=======================================================");
    }

    private void logResponse(ServerHttpRequest request, ServerHttpResponse response, long duration) {
        log.info("================== 📤 GATEWAY RESPONSE =================");
        log.info("⏰ Time: {}", LocalDateTime.now().format(formatter));
        log.info("🔗 Method: {}", request.getMethod());
        log.info("🌐 URL: {}", request.getURI());
        log.info("📊 Status: {}", response.getStatusCode());
        log.info("⏱️ Duration: {}ms", duration);
        log.info("🔤 Response Headers:");
        response.getHeaders().forEach((name, values) -> 
            log.info("   {}: {}", name, values)
        );
        log.info("=======================================================");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
} 