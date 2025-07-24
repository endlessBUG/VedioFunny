package com.vediofun.gateway.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentinel")
public class SentinelTestController {

    @GetMapping("/test")
    @SentinelResource(value = "gateway-test", blockHandler = "handleBlock", fallback = "handleFallback")
    public String testSentinel() {
        return "Gateway Service - Sentinel连接正常! 请查看Dashboard: http://localhost:8084";
    }
    
    @GetMapping("/status")
    public String sentinelStatus() {
        return "Gateway Service - Sentinel已激活";
    }
    
    // 阻塞处理方法
    public String handleBlock(String name, Exception ex) {
        return "Gateway Service - 请求被限流了: " + ex.getMessage();
    }
    
    // 降级处理方法
    public String handleFallback(String name, Throwable throwable) {
        return "Gateway Service - 服务降级: " + throwable.getMessage();
    }
} 