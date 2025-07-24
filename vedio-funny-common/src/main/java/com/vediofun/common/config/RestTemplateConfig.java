package com.vediofun.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 通用配置类
 * 
 * 提供负载均衡的RestTemplate Bean，供所有微服务使用
 * 
 * @author VedioFun Team
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置负载均衡的RestTemplate
     * 支持服务发现和负载均衡
     * 使用@Primary确保优先使用此配置
     */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 可以在这里添加更多配置，如超时设置、拦截器等
        // ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // factory.setConnectTimeout(5000);
        // factory.setReadTimeout(30000);
        // restTemplate.setRequestFactory(factory);
        
        return restTemplate;
    }
} 