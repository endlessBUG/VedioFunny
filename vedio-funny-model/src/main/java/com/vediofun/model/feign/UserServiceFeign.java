package com.vediofun.model.feign;

// 暂时注释掉FeignClient，等待common模块正确加载
// import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务Feign客户端
 * 
 * @author VedioFun Team
 */
// @FeignClient(
//     name = "vedio-funny-user",
//     fallback = UserServiceFeignFallback.class
// )
public interface UserServiceFeign {

    /**
     * 获取用户信息
     */
    @GetMapping("/api/user/{userId}")
    Map<String, Object> getUserInfo(@PathVariable("userId") Long userId);

    /**
     * 检查用户服务健康状态
     */
    @GetMapping("/api/user/health")
    Map<String, Object> health();
} 