package com.vediofun.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Gateway网关服务启动类
 * 
 * 基于Spring Cloud Gateway的响应式网关
 * 集成Nacos服务发现、Sentinel流量控制、Feign客户端
 * 
 * @author VedioFun Team
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.vediofun.gateway.feign")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(@Autowired Environment environment) {
        return args -> {
            String port = environment.getProperty("server.port", "8082");
            String applicationName = environment.getProperty("spring.application.name", "vedio-funny-gateway");
            
            System.out.println("========================================");
            System.out.println("🚀 VedioFun Gateway服务启动成功！");
            System.out.println("📋 服务信息:");
            System.out.println("   应用名称: " + applicationName);
            System.out.println("   服务端口: " + port);
            System.out.println("   访问地址: http://localhost:" + port);
            System.out.println("   健康检查: http://localhost:" + port + "/actuator/health");
            System.out.println("   路由信息: http://localhost:" + port + "/actuator/gateway/routes");
            System.out.println("========================================");
        };
    }
} 