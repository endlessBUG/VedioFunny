package com.vediofun.model.controller;

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
 * Model服务测试接口
 */
@Tag(name = "测试接口", description = "Model服务HelloWorld测试接口")
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

    @Operation(summary = "Hello World", description = "模型服务Hello World接口")
    @GetMapping("/world")
    public Map<String, Object> helloWorld() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前实例信息
        String hostAddress = getLocalHostAddress();
        int actualPort = getActualPort();
        String instanceId = hostAddress + ":" + actualPort;
        String nacosInstanceId = getNacosInstanceId();
        
        result.put("service", applicationName);
        result.put("message", "Hello World from Model Service!");
        result.put("host_address", hostAddress);
        result.put("port", actualPort);
        result.put("instance_id", instanceId);
        result.put("nacos_instance_id", nacosInstanceId);
        result.put("timestamp", LocalDateTime.now());
        result.put("version", "1.0.0");
        
        // 控制台打印负载均衡信息
        System.out.println("🎥 Model Service - Hello World 请求");
        System.out.println("   服务名称: " + applicationName);
        System.out.println("   主机地址: " + hostAddress);
        System.out.println("   服务端口: " + actualPort);
        System.out.println("   实例ID: " + instanceId);
        System.out.println("   Nacos实例: " + nacosInstanceId);
        System.out.println("   请求时间: " + LocalDateTime.now());
        System.out.println("   ----------------------------------------");
        
        return result;
    }

    @Operation(summary = "模型处理", description = "模拟AI模型处理接口")
    @GetMapping("/process/{type}")
    public Map<String, Object> processModel(@PathVariable String type) {
        Map<String, Object> result = new HashMap<>();
        result.put("service", "vedio-funny-model");
        result.put("message", "Processing " + type + " model...");
        result.put("model_type", type);
        result.put("port", port);
        result.put("timestamp", LocalDateTime.now());
        result.put("processing_time", "2.5s");
        result.put("status", "completed");
        return result;
    }

    @Operation(summary = "服务信息", description = "获取模型服务基本信息")
    @GetMapping("/info")
    public Map<String, Object> serviceInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("service_name", "vedio-funny-model");
        result.put("description", "AI模型处理服务");
        result.put("port", port);
        result.put("status", "running");
        result.put("endpoints", new String[]{
            "/hello/world",
            "/hello/process/{type}",
            "/hello/info",
            "/actuator/health"
        });
        result.put("gateway_access", "http://localhost:8082/api/model/hello/*");
        result.put("supported_models", new String[]{"video", "image", "text", "audio"});
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