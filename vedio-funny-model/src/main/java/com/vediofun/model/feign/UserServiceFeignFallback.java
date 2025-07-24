package com.vediofun.model.feign;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务Feign客户端降级处理
 * 
 * @author VedioFun Team
 */
@Component
public class UserServiceFeignFallback implements UserServiceFeign {

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", "用户服务暂时不可用");
        result.put("userId", userId);
        result.put("fallback", true);
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "vedio-funny-model");
        return result;
    }

    @Override
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "DOWN");
        result.put("error", "用户服务连接失败");
        result.put("fallback", true);
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "vedio-funny-model");
        return result;
    }
} 