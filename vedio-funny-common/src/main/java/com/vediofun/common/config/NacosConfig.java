package com.vediofun.common.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos 配置类
 * 
 * @author VedioFun Team
 */
@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.vediofun")
public class NacosConfig {
    
    // Nacos 相关配置可以在这里进行自定义
    // 例如：负载均衡策略、服务发现配置等
    
} 