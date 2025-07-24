package com.vediofun.common.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign 配置类
 * 
 * @author VedioFun Team
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign 超时配置
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(5000, 30000);  // 连接超时时间5s, 读取超时时间30s
    }

    /**
     * Feign 重试配置
     */
    @Bean
    public Retryer retryer() {
        // 最大重试次数为3，初始重试间隔100ms，最大重试间隔1000ms
        return new Retryer.Default(100, 1000, 3);
    }

    /**
     * FeignClient日志级别配置
     * NONE: 不记录任何日志 (默认)
     * BASIC: 仅记录请求方法、URL以及响应状态码和执行时间
     * HEADERS: 记录BASIC级别的信息，以及请求和响应的头信息
     * FULL: 记录所有请求和响应的明细，包括头信息、请求体、元数据
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        log.info("🔧 配置FeignClient日志级别: FULL (完整日志)");
        return Logger.Level.FULL;
    }

    /**
     * FeignClient请求选项配置
     * 设置连接超时和读取超时时间
     */
    @Bean
    public Request.Options feignRequestOptions() {
        int connectTimeout = 5000; // 5秒连接超时
        int readTimeout = 10000;   // 10秒读取超时
        log.info("🔧 配置FeignClient超时: 连接{}ms, 读取{}ms", connectTimeout, readTimeout);
        return new Request.Options(connectTimeout, readTimeout);
    }

    /**
     * 自定义错误解码器
     * 将HTTP错误响应转换为具体的异常类型
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        log.info("🔧 配置FeignClient自定义错误解码器");
        return new FeignErrorDecoder();
    }
} 