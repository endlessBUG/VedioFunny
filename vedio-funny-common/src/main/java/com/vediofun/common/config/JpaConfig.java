package com.vediofun.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.persistence.EntityManager;

/**
 * JPA 通用配置类
 * 
 * 只有在JPA相关类存在时才生效
 * 这样Gateway等不需要数据库的服务就不会加载此配置
 * 
 * 包含JPA Repository扫描和审计功能配置
 * 所有使用JPA的微服务都会自动继承这些配置
 * 
 * @author VedioFun Team
 */
@Configuration
@ConditionalOnClass({EntityManager.class, EnableJpaRepositories.class})
@EnableJpaRepositories(basePackages = {"com.vediofun"})
@EnableJpaAuditing
public class JpaConfig {
    
    // JPA Repository 自动扫描所有 com.vediofun 包下的 Repository 接口
    // JPA 审计功能自动处理 @CreatedDate, @LastModifiedDate 等注解
    
} 