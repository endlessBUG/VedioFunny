package com.vediofun.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import org.springframework.core.env.Environment;

// Model服务需要数据库访问，启用JPA自动配置
@SpringBootApplication(
    scanBasePackages = {"com.vediofun"}
)
@EnableDiscoveryClient
public class ModelApplication {

    public static void main(String[] args) {
        // 设置系统属性，强制使用Druid连接池
        System.setProperty("spring.datasource.type", "com.alibaba.druid.pool.DruidDataSource");
        SpringApplication.run(ModelApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(@Autowired Environment environment) {
        return args -> {
            String port = environment.getProperty("local.server.port");
            System.out.println("🎬 VedioFun Model Service Started Successfully!");
            System.out.println("🤖 Model API running on port: " + port);
            System.out.println("📚 API Docs: http://localhost:" + port + "/doc.html");
            System.out.println("🌐 Access via Gateway: http://localhost:8082/api/model/**");
            System.out.println("📋 Health Check: http://localhost:" + port + "/actuator/health");
            System.out.println("🔍 Druid Monitor: http://localhost:" + port + "/druid/");
            
            // 打印数据源信息
            String dataSourceType = environment.getProperty("spring.datasource.type");
            System.out.println("💾 DataSource Type: " + dataSourceType);
            System.out.println("🤖 AI模型推理服务 - 数据库已连接");
        };
    }
}