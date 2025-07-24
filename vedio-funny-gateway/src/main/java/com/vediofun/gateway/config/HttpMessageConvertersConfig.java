package com.vediofun.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP消息转换器配置类
 * 解决Spring Cloud Gateway + Feign集成时HttpMessageConverters缺失问题
 */
@Configuration
public class HttpMessageConvertersConfig {
    
    /**
     * 配置HttpMessageConverters Bean
     * 解决Feign客户端DecodeException问题
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        
        // 添加Jackson JSON消息转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        converters.add(jsonConverter);
        
        return new HttpMessageConverters(converters);
    }
}