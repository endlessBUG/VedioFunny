package com.vediofun.model.controller;

import com.vediofun.model.feign.UserServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务间调用示例控制器
 * 
 * @author VedioFun Team
 */
@RestController
@RequestMapping("/api/service")
@Tag(name = "服务调用", description = "微服务间调用示例")
public class ServiceCallController {

    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 调用用户服务获取用户信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户信息", description = "通过Feign调用用户服务获取用户信息")
    public Map<String, Object> getUserFromUserService(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用用户服务
            Map<String, Object> userInfo = userServiceFeign.getUserInfo(userId);
            
            result.put("success", true);
            result.put("userInfo", userInfo);
            result.put("calledFrom", "vedio-funny-model");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "调用用户服务失败: " + e.getMessage());
            result.put("calledFrom", "vedio-funny-model");
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * 检查用户服务健康状态
     */
    @GetMapping("/user/health")
    @Operation(summary = "用户服务健康检查", description = "检查用户服务的健康状态")
    public Map<String, Object> checkUserServiceHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> healthInfo = userServiceFeign.health();
            
            result.put("success", true);
            result.put("userServiceHealth", healthInfo);
            result.put("checkedFrom", "vedio-funny-model");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "用户服务健康检查失败: " + e.getMessage());
            result.put("checkedFrom", "vedio-funny-model");
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * 模拟基于用户数据的模型推理
     */
    @PostMapping("/user/{userId}/inference")
    @Operation(summary = "用户模型推理", description = "基于用户数据进行AI模型推理")
    public Map<String, Object> userModelInference(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 首先获取用户信息
            Map<String, Object> userInfo = userServiceFeign.getUserInfo(userId);
            
            // 模拟基于用户数据的推理
            String modelName = (String) request.getOrDefault("modelName", "user-behavior-model");
            Object inputData = request.get("inputData");
            
            result.put("success", true);
            result.put("userId", userId);
            result.put("userInfo", userInfo);
            result.put("modelName", modelName);
            result.put("inputData", inputData);
            result.put("inferenceResult", "基于用户特征的推理结果: " + Math.random());
            result.put("confidence", Math.random());
            result.put("personalizedScore", Math.random());
            result.put("processTime", "250ms");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "用户模型推理失败: " + e.getMessage());
            result.put("userId", userId);
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }
} 