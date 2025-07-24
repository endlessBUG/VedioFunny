package com.vediofun.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = {"com.vediofun"})
@EnableDiscoveryClient
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(@Autowired Environment environment) {
        return args -> {
            String port = environment.getProperty("local.server.port");
            System.out.println("👥 VedioFun User Service Started Successfully!");
            System.out.println("👤 User API running on port: " + port);
            System.out.println("👤 User API Docs: http://localhost:"+ port +"/doc.html");
            System.out.println("📋 Health Check: http://localhost:" + port + "/actuator/health");

        };
    }
} 