package com.vediofun.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auth服务测试接口
 */
@Tag(name = "测试接口", description = "Auth服务HelloWorld测试接口")
@RestController
@RequestMapping("/hello")
public class HelloController {

    @Value("${server.port:0}")
    private String port;
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Operation(summary = "Hello World", description = "认证服务Hello World接口")
    @GetMapping("/world")
    public Map<String, Object> helloWorld() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前实例信息
        String hostAddress = getLocalHostAddress();
        int actualPort = getActualPort();
        String instanceId = hostAddress + ":" + actualPort;
        String nacosInstanceId = getNacosInstanceId();
        
        result.put("service", applicationName);
        result.put("message", "Hello World from Auth Service!");
        result.put("host_address", hostAddress);
        result.put("port", actualPort);
        result.put("instance_id", instanceId);
        result.put("nacos_instance_id", nacosInstanceId);
        result.put("timestamp", LocalDateTime.now());
        result.put("version", "1.0.0");
        
        // 控制台打印负载均衡信息
        System.out.println("🔐 Auth Service - Hello World 请求");
        System.out.println("   服务名称: " + applicationName);
        System.out.println("   主机地址: " + hostAddress);
        System.out.println("   服务端口: " + actualPort);
        System.out.println("   实例ID: " + instanceId);
        System.out.println("   Nacos实例: " + nacosInstanceId);
        System.out.println("   请求时间: " + LocalDateTime.now());
        System.out.println("   ----------------------------------------");
        
        return result;
    }

    @Operation(summary = "认证测试", description = "模拟认证检查接口")
    @GetMapping("/check/{token}")
    public Map<String, Object> checkAuth(@PathVariable String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("service", "vedio-funny-auth");
        result.put("message", "Checking authentication for token: " + token);
        result.put("token", token);
        result.put("port", port);
        result.put("timestamp", LocalDateTime.now());
        result.put("valid", token.length() > 5); // 简单的验证逻辑
        result.put("expires_in", "3600s");
        return result;
    }

    @Operation(summary = "服务信息", description = "获取认证服务基本信息")
    @GetMapping("/info")
    public Map<String, Object> serviceInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("service_name", "vedio-funny-auth");
        result.put("description", "用户认证授权服务");
        result.put("port", port);
        result.put("status", "running");
        result.put("endpoints", new String[]{
            "/hello/world",
            "/hello/check/{token}",
            "/hello/info",
            "/actuator/health"
        });
        result.put("gateway_access", "http://localhost:8082/api/auth/hello/*");
        result.put("auth_types", new String[]{"JWT", "OAuth2", "Session"});
        return result;
    }
    
    /**
     * 获取本地主机地址
     */
    private String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
    
    /**
     * 获取实际运行端口
     */
    private int getActualPort() {
        try {
            return webServerAppCtxt.getWebServer().getPort();
        } catch (Exception e) {
            return Integer.parseInt(port);
        }
    }
    
    /**
     * 获取Nacos实例ID
     */
    private String getNacosInstanceId() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(applicationName);
            String currentHost = getLocalHostAddress();
            int currentPort = getActualPort();
            
            for (ServiceInstance instance : instances) {
                if (instance.getHost().equals(currentHost) && instance.getPort() == currentPort) {
                    return instance.getInstanceId();
                }
            }
            return currentHost + ":" + currentPort + "#" + System.currentTimeMillis() % 1000;
        } catch (Exception e) {
            return "unknown-instance";
        }
    }
} 