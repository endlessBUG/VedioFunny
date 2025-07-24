package com.vediofun.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web通用配置类
 * 
 * 配置请求日志、拦截器等Web相关功能
 * 
 * @author VedioFun Team
 */
@Slf4j
@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
public class WebConfig implements WebMvcConfigurer {

    /**
     * 请求日志过滤器
     * 记录详细的HTTP请求信息，便于调试
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        
        // 包含查询字符串
        filter.setIncludeQueryString(true);
        
        // 包含请求载荷（POST数据）
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        
        // 包含请求头
        filter.setIncludeHeaders(true);
        
        // 包含客户端信息
        filter.setIncludeClientInfo(true);
        
        // 记录请求前和请求后
        filter.setBeforeMessagePrefix("==== 请求开始 ");
        filter.setAfterMessagePrefix("==== 请求结束 ");
        filter.setBeforeMessageSuffix(" ====");
        filter.setAfterMessageSuffix(" ====");
        
        return filter;
    }
} 